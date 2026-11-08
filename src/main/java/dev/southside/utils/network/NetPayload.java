package dev.southside.utils.network;

import dev.southside.MinecraftAccessor;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2540;
import net.minecraft.class_2817;
import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9139;
import net.minecraft.class_8710.class_9154;

public class NetPayload implements MinecraftAccessor {
   public static final List<Runnable> preTasks = new ArrayList<>();
   public static final List<class_2960> REGISTERED_CHANNELS = new ArrayList<>();

   public static void pre(Runnable task) {
      preTasks.add(task);
   }

   @EventHandler(
      priority = 11451
   )
   private static void onTick(TickEvent event) {
      if (event.state == Stage.PRE && canUpdate() && mc.method_1562() != null) {
         for (Runnable task : preTasks) {
            task.run();
         }

         preTasks.clear();
      }
   }

   public static void register(class_2960 channel) {
      if (!REGISTERED_CHANNELS.contains(channel)) {
         REGISTERED_CHANNELS.add(channel);
      }
   }

   public static void send(class_2960 channel, class_2540 buf) {
      if (mc.method_1562() != null) {
         mc.method_1562().method_52787(new class_2817(new NetPayload.PayloadPacket(channel, buf)));
      }
   }

   public static void write(class_2540 byteBuf, class_2540 data) {
      byteBuf.method_52975(data.copy());
   }

   public static class_2540 read(class_2540 byteBuf, int maxSize) {
      assertSize(byteBuf, maxSize);
      class_2540 newBuf = new class_2540(Unpooled.buffer());
      newBuf.method_52975(byteBuf.copy());
      byteBuf.method_52994(byteBuf.readableBytes());
      return newBuf;
   }

   private static void assertSize(class_2540 buf, int maxSize) {
      int size = buf.readableBytes();
      if (size < 0 || size > maxSize) {
         throw new IllegalArgumentException("Payload may not be larger than " + maxSize + " bytes");
      }
   }

   public static boolean canUpdate() {
      return mc != null && mc.field_1724 != null && mc.field_1687 != null;
   }

   public static class PayloadPacket implements class_8710 {
      public class_2960 channel;
      public class_2540 buf;

      public static <T extends class_2540> class_9139<T, NetPayload.PayloadPacket> createCodec(class_2960 id) {
         return class_8710.method_56484(NetPayload.PayloadPacket::write, buf -> new NetPayload.PayloadPacket(id, NetPayload.read(buf, 1145140)));
      }

      public PayloadPacket(class_2960 channel, class_2540 buf) {
         this.channel = channel;
         this.buf = buf;
      }

      public void write(class_2540 buf) {
         NetPayload.write(buf, this.buf);
      }

      public class_9154<? extends class_8710> method_56479() {
         return new class_9154(this.channel);
      }
   }
}
