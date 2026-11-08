package dev.southside.nel;

import dev.southside.Southside;
import dev.southside.nanovg.util.ResourceUtil;
import dev.southside.nel.GameServer;
import dev.southside.nel.protocol.ForwardServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NeteaseLauncher {
    private static final NeteaseLauncher _instance = new NeteaseLauncher();
    private static final boolean REMOTE = false;
    public static final Logger LOGGER = LogManager.getLogger((String)"NeteaseLauncher");
    private static final String SERVER_RESOURCE_DIR = "/assets/nel/server/";
    private static final String TARGET_DIR_NAME = "nel_server_runtime";
    private static final List<String> SERVER_FILES = List.of("OpenNEL_Lite.exe");
    private static final List<String> PLUGIN_FILES = Arrays.asList("Base1200.UG", "HeypixelProtocol.UG");
    private final ExecutorService executor = Executors.newWorkStealingPool();
    private CompletableFuture<Void> initializeFuture = new CompletableFuture();
    private boolean available = false;
    private Process process;
    private Path runtimePath;
    private GameServer server;
    private volatile GameServer.GameConnection activeConnection;

    public static NeteaseLauncher get() {
        return _instance;
    }

    public boolean isProcessLaunched() {
        return this.available;
    }

    public boolean isAvailable() {
        return this.isProcessLaunched() && this.activeConnection != null && this.activeConnection.isAvailable() && this.activeConnection.isHandshaked();
    }

    public CompletableFuture<GameServer.GameConnection> makeClient() {
        if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("OpenNEL_Lite.exe requires Windows; Netease integration is unavailable on macOS."));
        }
        return CompletableFuture.supplyAsync(() -> {
            if (this.activeConnection != null && this.activeConnection.isAvailable()) {
                return this.activeConnection;
            }
            if (this.server == null) {
                this.server = new GameServer(35234);
                this.server.start();
            }
            while (this.server.activeConnections.isEmpty()) {
            }
            this.activeConnection = this.server.activeConnections.getFirst();
            return this.activeConnection;
        }, this.executor);
    }

    public void init() {
        if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
            LOGGER.warn("macOS: Windows-only OpenNEL helper is unavailable; Netease integration disabled.");
            this.initializeFuture.completeExceptionally(new UnsupportedOperationException("OpenNEL_Lite.exe requires Windows; Netease integration is unavailable on macOS."));
            return;
        }
        this.runtimePath = Southside.FOLDER.toPath().resolve("nel");
        Path pluginsPath = this.runtimePath.resolve("plugins");
        long start = System.currentTimeMillis();
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(this.runtimePath, new LinkOption[0]) || this.md5Check(SERVER_RESOURCE_DIR, this.runtimePath, SERVER_FILES)) {
                    this.extractDirectory(SERVER_RESOURCE_DIR, this.runtimePath, SERVER_FILES);
                }
                if (!Files.exists(pluginsPath, new LinkOption[0]) || this.md5Check("/assets/nel/server/plugins/", pluginsPath, PLUGIN_FILES)) {
                    this.extractDirectory("/assets/nel/server/plugins/", pluginsPath, PLUGIN_FILES);
                }
                return null;
            }
            catch (Throwable t) {
                return t;
            }
        }).thenAccept(t -> {
            if (t == null) {
                Path serverExecPath = this.runtimePath.resolve("OpenNEL_Lite.exe");
                if (!Files.exists(serverExecPath, new LinkOption[0])) {
                    LOGGER.warn("Missing OpenNEL_Lite");
                    return;
                }
                try {
                    ForwardServer.start();
                }
                catch (Throwable ee) {
                    ee.printStackTrace();
                }
                try {
                    if (this.server == null) {
                        LOGGER.info("Starting NEL Server...");
                        this.server = new GameServer(35234);
                        this.server.start();
                    }
                }
                catch (Exception e) {
                    LOGGER.error("NEL Server Start Failed.", (Throwable)e);
                    return;
                }
                LOGGER.info("\u5c1d\u8bd5\u542f\u52a8\u5916\u90e8\u5ba2\u6237\u7aef\u8fdb\u7a0b: {}", (Object)serverExecPath);
                try {
                    this.process = new ProcessBuilder(serverExecPath.toAbsolutePath().toString()).directory(this.runtimePath.toFile()).redirectInput(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start();
                    this.executor.submit(this::process);
                }
                catch (Throwable r) {
                    LOGGER.warn("error in creating process: ", r);
                }
            } else {
                LOGGER.warn("error in initializing: ", t);
            }
        })).thenRun(() -> {
            this.initializeFuture.complete(null);
            LOGGER.info("nel initialized: {}ms.", (Object)(System.currentTimeMillis() - start));
        });
    }

    private void process() {
        try {
            this.available = true;
            LOGGER.info("nel process created - pid: {}", (Object)this.process.pid());
            int ret = this.process.waitFor();
            this.available = false;
            LOGGER.info("nel process exit code: {}", (Object)ret);
        }
        catch (Throwable throwable) {
        }
    }

    public Future<?> stopProcess() {
        return this.executor.submit(() -> {
            if (this.process != null && this.process.isAlive()) {
                this.process.destroyForcibly();
            }
            if (this.server != null) {
                this.server.stop();
                this.server = null;
            }
            this.activeConnection = null;
            LOGGER.info("nel process stopped;");
        });
    }

    public CompletableFuture<Void> restart() {
        try {
            this.killNEL();
            this.initializeFuture = new CompletableFuture();
            this.available = false;
            this.init();
        }
        catch (Throwable t) {
            LOGGER.warn("error in restarting: ", t);
        }
        return this.initializeFuture;
    }

    public void stop() {
        this.killNEL();
        try {
            this.executor.awaitTermination(1L, TimeUnit.MILLISECONDS);
            this.executor.shutdownNow();
        }
        catch (Throwable throwable) {
        }
    }

    private void extractDirectory(String resourceDir, Path targetDir, List<String> files) throws IOException {
        LOGGER.info("\u6b63\u5728\u521b\u5efa\u6216\u786e\u4fdd\u670d\u52a1\u5668\u8fd0\u884c\u65f6\u76ee\u5f55\u5b58\u5728: {}", (Object)targetDir);
        Files.createDirectories(targetDir, new FileAttribute[0]);
        for (String fileName : files) {
            String resourcePath = resourceDir + fileName;
            Path targetPath = targetDir.resolve(fileName);
            InputStream is = ResourceUtil.class.getResourceAsStream(resourcePath);
            try {
                if (is == null) {
                    LOGGER.error("\u8d44\u6e90\u672a\u627e\u5230\uff0c\u65e0\u6cd5\u63d0\u53d6: {}", (Object)resourcePath);
                    throw new IOException("\u65e0\u6cd5\u5728 Mod \u8d44\u6e90\u4e2d\u627e\u5230\u6587\u4ef6: " + fileName + "\u3002\u8bf7\u68c0\u67e5 Mod JAR \u6587\u4ef6\u7ed3\u6784\u3002");
                }
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.debug("\u6210\u529f\u63d0\u53d6\u6587\u4ef6: {}", (Object)fileName);
            }
            finally {
                if (is == null) continue;
                is.close();
            }
        }
    }

    private boolean md5Check(String resourceDir, Path targetDir, List<String> files) throws IOException {
        for (String fileName : files) {
            String resourcePath = resourceDir + fileName;
            Path targetPath = targetDir.resolve(fileName);
            InputStream is = ResourceUtil.class.getResourceAsStream(resourcePath);
            try {
                if (is == null) {
                    LOGGER.error("\u8d44\u6e90\u672a\u627e\u5230\uff0c\u65e0\u6cd5\u63d0\u53d6: {}", (Object)resourcePath);
                    throw new IOException("\u65e0\u6cd5\u5728 Mod \u8d44\u6e90\u4e2d\u627e\u5230\u6587\u4ef6: " + fileName + "\u3002\u8bf7\u68c0\u67e5 Mod JAR \u6587\u4ef6\u7ed3\u6784\u3002");
                }
                InputStream targetIs = Files.newInputStream(targetPath, new OpenOption[0]);
                if (DigestUtils.md5Hex((InputStream)targetIs).equals(DigestUtils.md5Hex((InputStream)is))) {
                    targetIs.close();
                    continue;
                }
                targetIs.close();
                boolean bl = true;
                return bl;
            }
            finally {
                if (is == null) continue;
                is.close();
            }
        }
        return false;
    }

    public void killNEL() {
        if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
            return;
        }
        ProcessHandle.allProcesses().filter(NeteaseLauncher::isTargetProcess).forEach(ph -> {
            try {
                ph.destroyForcibly();
                this.activeConnection = null;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        if (this.server != null) {
            this.server.stop();
            this.server = null;
        }
    }

    private static boolean isTargetProcess(ProcessHandle ph) {
        if (!ph.isAlive()) {
            return false;
        }
        ProcessHandle.Info info = ph.info();
        Optional<String> command = info.command();
        if (command.isPresent()) {
            String fullPath = command.get();
            return fullPath.endsWith("OpenNEL_Lite.exe");
        }
        return false;
    }

    @Generated
    public ExecutorService getExecutor() {
        return this.executor;
    }

    @Generated
    public CompletableFuture<Void> getInitializeFuture() {
        return this.initializeFuture;
    }
}

