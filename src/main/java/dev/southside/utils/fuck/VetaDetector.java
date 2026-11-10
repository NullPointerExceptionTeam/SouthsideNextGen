package dev.southside.utils.fuck;

import com.mojang.logging.LogUtils;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32.ByReference;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.handler.SplitterHandler;
import net.minecraft.network.handler.SizePrepender;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.state.HandshakeStates;
import net.minecraft.network.state.QueryStates;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.PacketType;
import org.slf4j.Logger;

public class VetaDetector {
   private static final EventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(1);
   private static final ExecutorService executor = Executors.newFixedThreadPool(2);

   public static boolean detect() {
      int score = 0;
      HANDLE snapshot = null;

      try {
         snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new DWORD(0L));
         ByReference processEntry = new ByReference();
         if (!Kernel32.INSTANCE.Process32First(snapshot, processEntry)) {
            return false;
         } else {
            do {
               try {
                  int pid = processEntry.th32ProcessID.intValue();
                  String exeName = Native.toString(processEntry.szExeFile);
                  HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(1040, false, pid);
                  if (hProcess != null) {
                     try {
                        HMODULE[] modules = new HMODULE[256];
                        IntByReference needed = new IntByReference();
                        if (Psapi.INSTANCE.EnumProcessModules(hProcess, modules, modules.length * Native.POINTER_SIZE, needed)) {
                           for (HMODULE mod : modules) {
                              if (mod == null) {
                                 break;
                              }

                              char[] name = new char[512];
                              Psapi.INSTANCE.GetModuleFileNameExW(hProcess, mod, name, name.length);
                              String moduleName = Native.toString(name);
                              if (moduleName != null && moduleName.contains("Veta")) {
                                 if (++score >= 5) {
                                    return true;
                                 }
                              }
                           }
                        }
                     } catch (Throwable var27) {
                     } finally {
                        Kernel32.INSTANCE.CloseHandle(hProcess);
                     }
                  }
               } catch (Throwable var29) {
               }
            } while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));

            return false;
         }
      } catch (Throwable var30) {
         return false;
      } finally {
         if (snapshot != null) {
            Kernel32.INSTANCE.CloseHandle(snapshot);
         }
      }
   }

   public static CompletableFuture<Boolean> detectProxy(int... ports) {
      CompletableFuture<Boolean> base = new CompletableFuture<>();
      AtomicBoolean cancelled = new AtomicBoolean(false);
      List<CompletableFuture<Boolean>> scans = new ArrayList<>();

      for (int port : ports) {
         if (cancelled.get()) {
            break;
         }

         CompletableFuture<Boolean> scan = new VetaDetector.MotdScanner("localhost", port).connect(cancelled).exceptionally(ignored -> false);
         scans.add(scan);
         scan.thenAccept(result -> {
            if (result && cancelled.compareAndSet(false, true)) {
               base.complete(true);

               for (CompletableFuture<Boolean> other : scans) {
                  if (other != scan) {
                     other.cancel(false);
                  }
               }
            }
         });
      }

      CompletableFuture.allOf(scans.toArray(new CompletableFuture[0])).whenComplete((v, t) -> {
         if (!base.isDone()) {
            base.complete(false);
         }
      });
      base.whenComplete((r, t) -> {
         if (t instanceof CancellationException || base.isCancelled()) {
            cancelled.set(true);

            for (CompletableFuture<Boolean> other : scans) {
               other.cancel(false);
            }
         }
      });
      return base;
   }

   public static class DecoderHandler<T extends PacketListener> extends ByteToMessageDecoder implements NetworkStateTransitionHandler {
      private static final Logger LOGGER = LogUtils.getLogger();
      private final NetworkState<T> state;

      public DecoderHandler(NetworkState<T> state) {
         this.state = state;
      }

      protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
         try {
            int readable = buf.readableBytes();
            if (readable == 0) {
               return;
            }

            PacketByteBuf b = new PacketByteBuf(buf);
            int id = b.readVarInt();
            if (this.state.equals(QueryStates.S2C) && id == 0) {
               out.add(new VetaDetector.FakePacket(b.readString()));
               ctx.close();
            }
         } catch (Throwable var7) {
            ctx.close();
         }
      }

      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         ctx.close();
      }
   }

   public static class EncoderHandler<T extends PacketListener> extends MessageToByteEncoder<Packet<T>> {
      private static final Logger LOGGER = LogUtils.getLogger();
      private final NetworkState<T> state;

      public EncoderHandler(NetworkState<T> state) {
         this.state = state;
      }

      protected void encode(ChannelHandlerContext ctx, Packet<T> packet, ByteBuf out) {
         try {
            this.state.codec().encode(out, packet);
         } catch (Throwable var5) {
            ctx.close();
         }
      }

      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         ctx.close();
      }
   }

   public record FakePacket(String text) implements Packet<ServerQueryPacketListener> {
      public PacketType<? extends Packet<ServerQueryPacketListener>> getPacketType() {
         return null;
      }

      @Override
      public void apply(ServerQueryPacketListener listener) {
         this.send(listener);
      }

      public void send(ServerQueryPacketListener listener) {
      }
   }

   private static class MotdScanner {
      private final String ip;
      private final int port;
      private Consumer<String> callback;

      MotdScanner(String ip, int port) {
         this.ip = ip;
         this.port = port;
      }

      CompletableFuture<Boolean> connect(AtomicBoolean cancelled) {
         CompletableFuture<Boolean> result = new CompletableFuture<>();
         Bootstrap bootstrap = this.bootstrap();
         if (cancelled.get()) {
            result.complete(false);
            return result;
         } else {
            ChannelFuture cf = bootstrap.connect();
            result.whenComplete((r, t) -> {
               if (cf.channel() != null && cf.channel().isActive()) {
                  cf.channel().close();
               }
            });
            cf.addListener(f -> {
               if (!f.isSuccess()) {
                  result.complete(false);
               } else if (cancelled.get()) {
                  cf.channel().close();
                  result.complete(false);
               } else {
                  this.callback = text -> {
                     boolean hasVeta = text != null && text.contains("Veta");
                     result.complete(hasVeta);
                  };
                  Channel ch = cf.channel();
                  ch.pipeline().fireChannelActive();
               }
            });
            return result;
         }
      }

      private Bootstrap bootstrap() {
         return (Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().channel(NioSocketChannel.class))
                        .group(VetaDetector.EVENT_LOOP_GROUP))
                     .remoteAddress(this.ip, this.port)
                     .option(ChannelOption.TCP_NODELAY, true))
                  .option(ChannelOption.IP_TOS, 24))
               .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500))
            .handler(new ChannelInitializer<Channel>() {
               protected void initChannel(Channel ch) {
                  ChannelPipeline pipeline = ch.pipeline();
                  pipeline.addLast("read-timeout", new ReadTimeoutHandler(1));
                  pipeline.addLast("write-timeout", new WriteTimeoutHandler(1));
                  pipeline.addLast("splitter", new SplitterHandler(null));
                  pipeline.addLast(new ChannelHandler[]{new FlowControlHandler()});
                  pipeline.addLast("decoder", new VetaDetector.DecoderHandler(QueryStates.S2C));
                  pipeline.addLast("prepender", new SizePrepender());
                  pipeline.addLast("encoder", new VetaDetector.EncoderHandler(HandshakeStates.C2S));
                  pipeline.addLast("manager", new VetaDetector.MotdScanner.MotdHandler(MotdScanner.this.ip, MotdScanner.this.port, MotdScanner.this.callback));
               }
            });
      }

      @Generated
      public void setCallback(Consumer<String> callback) {
         this.callback = callback;
      }

      private static class MotdHandler extends SimpleChannelInboundHandler<Packet> {
         private final String ip;
         private final int port;
         private final Consumer<String> callback;

         public void channelActive(ChannelHandlerContext ctx) {
            try {
               ctx.channel().config().setAutoRead(false);
               HandshakeC2SPacket handshake = new HandshakeC2SPacket(766, this.ip, this.port, ConnectionIntent.STATUS);
               ctx.write(handshake);
               ChannelPipeline p = ctx.pipeline();
               if (p.get("encoder") != null) {
                  p.remove("encoder");
               }

               p.addBefore("manager", "encoder", new VetaDetector.EncoderHandler(QueryStates.C2S));
               ctx.writeAndFlush(QueryRequestC2SPacket.INSTANCE, ctx.voidPromise());
               ctx.channel().config().setAutoRead(true);
            } catch (Throwable var4) {
               ctx.close();
            }
         }

         protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            try {
               if (msg instanceof VetaDetector.FakePacket response && this.callback != null) {
                  this.callback.accept(response.text);
               }
            } catch (Throwable var7) {
            } finally {
               ctx.close();
            }
         }

         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
         }

         @Generated
         public MotdHandler(String ip, int port, Consumer<String> callback) {
            this.ip = ip;
            this.port = port;
            this.callback = callback;
         }
      }
   }
}
