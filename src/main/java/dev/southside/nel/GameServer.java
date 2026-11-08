package dev.southside.nel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.nel.OpenNELApi;
import dev.southside.nel.open_nel.InterceptorBody;
import dev.southside.nel.open_nel.InterceptorInfo;
import dev.southside.nel.open_nel.ResponsePack;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import org.apache.logging.log4j.Logger;

public class GameServer {
    private static final Logger LOGGER = NeteaseLauncher.LOGGER;
    private static final Gson GSON = new Gson();
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final int port;
    public final List<GameConnection> activeConnections = new CopyOnWriteArrayList<GameConnection>();

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        if (this.isRunning) {
            return;
        }
        this.isRunning = true;
        new Thread(() -> {
            try {
                this.serverSocket = new ServerSocket(this.port);
                LOGGER.info("GameServer started on port " + this.port + ", waiting for .NET client...");
                while (this.isRunning && !this.serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = this.serverSocket.accept();
                        this.handleNewConnection(clientSocket);
                    }
                    catch (IOException e) {
                        if (!this.isRunning) continue;
                        LOGGER.warn("Accept error: " + e.getMessage());
                    }
                }
            }
            catch (IOException e) {
                LOGGER.error("Could not start server on port " + this.port, (Throwable)e);
            }
        }, "GameServer-Acceptor").start();
    }

    private void handleNewConnection(Socket socket) {
        try {
            LOGGER.info("New connection from: " + String.valueOf(socket.getInetAddress()));
            GameConnection connection = new GameConnection(socket, this);
            this.activeConnections.add(connection);
        }
        catch (Exception e) {
            LOGGER.error("Failed to initialize connection", (Throwable)e);
        }
    }

    public void stop() {
        this.isRunning = false;
        try {
            for (GameConnection conn : this.activeConnections) {
                conn.close();
            }
            this.activeConnections.clear();
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
            LOGGER.info("GameServer stopped.");
        }
        catch (IOException e) {
            LOGGER.error("Error stopping server", (Throwable)e);
        }
    }

    public static class GameConnection {
        private static final AtomicInteger INDEX = new AtomicInteger(0);
        private final int id;
        private final Socket socket;
        private final PrintWriter out;
        private final GameServer server;
        private final byte[] restBuffer = new byte[0];
        public OpenNELApi api;
        private boolean handshaked = false;
        public final List<InterceptorInfo> interceptors = new ArrayList<InterceptorInfo>();

        public GameConnection(Socket socket, GameServer server) throws IOException {
            this.id = INDEX.getAndIncrement();
            this.socket = socket;
            this.server = server;
            this.socket.setSoTimeout(0);
            this.out = new PrintWriter((Writer)new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.api = new OpenNELApi(NeteaseLauncher.get().getExecutor(), new OpenNELApi.NelIO(){
                @Override
                public synchronized void send(Object obj) {
                    String result = null;
                    if (obj instanceof String) {
                        String s;
                        result = s = (String)obj;
                    } else if (obj instanceof JsonElement) {
                        JsonElement e = (JsonElement)obj;
                        result = GSON.toJson(e);
                    }
                    if (result != null) {
                        out.println(result);
                    }
                }

                @Override
                public synchronized String read(long timedOutMs) {
                    try {
                        ReadLineResult result = GameConnection.readLine(socket, restBuffer, (int)(timedOutMs / 1000L));
                        return result.line;
                    }
                    catch (IOException e) {
                        return null;
                    }
                }
            });
        }

        public boolean isAvailable() {
            return this.socket != null && !this.socket.isClosed() && this.socket.isConnected();
        }

        public CompletableFuture<String> handshake(String crcSalt, String version, long timedOut, TimeUnit unit) {
            return this.api.handshake(crcSalt, version, timedOut, unit).thenApply(s -> {
                if (s != null) {
                    return s;
                }
                this.handshaked = true;
                return null;
            });
        }

        public CompletableFuture<ResponsePack<InterceptorBody>> joinGame(String serverId, String roleId, String serverName, long timedOut, TimeUnit unit) {
            CompletableFuture<ResponsePack<InterceptorBody>> future = new CompletableFuture<ResponsePack<InterceptorBody>>();
            NeteaseLauncher.get().getExecutor().submit(() -> {
                InterceptorInfo info = new InterceptorInfo(serverId, roleId, null, null);
                if (this.interceptors.contains(info)) {
                    InterceptorInfo i = this.interceptors.get(this.interceptors.indexOf(info));
                    future.complete(new ResponsePack<InterceptorBody>(null, new InterceptorBody(i.address(), i.uuid())));
                    return;
                }
                this.api.joinGame(serverId, roleId, serverName, timedOut, unit).thenAccept(response -> {
                    future.complete((ResponsePack<InterceptorBody>)response);
                    if (response.obj() != null) {
                        this.interceptors.add(new InterceptorInfo(serverId, roleId, ((InterceptorBody)response.obj()).address(), ((InterceptorBody)response.obj()).identifier()));
                    }
                });
            });
            return future;
        }

        public CompletableFuture<ResponsePack<List<String>>> shutdownGame(List<String> identifiers, long timedOut, TimeUnit unit) {
            CompletableFuture<ResponsePack<List<String>>> future = new CompletableFuture<ResponsePack<List<String>>>();
            NeteaseLauncher.get().getExecutor().submit(() -> this.api.shutdownGame(identifiers, timedOut, unit).thenAccept(response -> {
                if (response.msg() == null) {
                    for (String identifier : identifiers) {
                        this.interceptors.removeIf(i -> i.uuid().equals(identifier));
                    }
                }
                future.complete((ResponsePack<List<String>>)response);
            }));
            return future;
        }

        public void close() {
            try {
                this.handshaked = false;
                if (this.out != null) {
                    this.out.close();
                }
                if (this.socket != null && !this.socket.isClosed()) {
                    this.socket.close();
                }
                this.server.activeConnections.remove(this);
                LOGGER.info("'{}' connection closed", (Object)this.getName());
            }
            catch (IOException e) {
                LOGGER.warn("Error closing '{}': ", (Object)this.getName(), (Object)e);
            }
        }

        public String getName() {
            return "nel-conn-" + this.id;
        }

        private static ReadLineResult readLine(Socket sock, byte[] initial, int timeoutSeconds) throws IOException {
            InputStream in = sock.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            buf.write(initial);
            long deadline = System.currentTimeMillis() + (long)timeoutSeconds * 1000L;
            while (System.currentTimeMillis() < deadline) {
                byte[] currentBytes = buf.toByteArray();
                int newlineIndex = GameConnection.findNewline(currentBytes);
                if (newlineIndex >= 0) {
                    String line = new String(currentBytes, 0, newlineIndex, StandardCharsets.UTF_8);
                    byte[] rest = Arrays.copyOfRange(currentBytes, newlineIndex + 1, currentBytes.length);
                    return new ReadLineResult(line, rest);
                }
                long remainingMillis = deadline - System.currentTimeMillis();
                if (remainingMillis <= 0L) break;
                try {
                    byte[] readData = new byte[4096];
                    int n = in.read(readData);
                    if (n < 0) {
                        return new ReadLineResult(null, new byte[0]);
                    }
                    buf.write(readData, 0, n);
                }
                catch (SocketTimeoutException socketTimeoutException) {}
            }
            return new ReadLineResult(null, buf.toByteArray());
        }

        private static int findNewline(byte[] arr) {
            for (int i = 0; i < arr.length; ++i) {
                if (arr[i] != 10) continue;
                return i;
            }
            return -1;
        }

        @Generated
        public boolean isHandshaked() {
            return this.handshaked;
        }

        private static class ReadLineResult {
            final String line;
            final byte[] rest;

            ReadLineResult(String line, byte[] rest) {
                this.line = line;
                this.rest = rest;
            }
        }
    }
}

