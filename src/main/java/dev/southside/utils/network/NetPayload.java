package dev.southside.utils.network;

import dev.southside.MinecraftAccessor;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public class NetPayload implements MinecraftAccessor {
   public static final List<Runnable> preTasks = new ArrayList<>();
   public static final List<Identifier> REGISTERED_CHANNELS = new ArrayList<>();

   public static void pre(Runnable task) {
      preTasks.add(task);
   }

   @EventHandler(
      priority = 11451
   )
   private static void onTick(TickEvent event) {
      if (event.state == Stage.PRE && canUpdate() && mc.getNetworkHandler() != null) {
         for (Runnable task : preTasks) {
            task.run();
         }

         preTasks.clear();
      }
   }

   public static void register(Identifier channel) {
      if (!REGISTERED_CHANNELS.contains(channel)) {
         REGISTERED_CHANNELS.add(channel);
      }
   }

   public static void send(Identifier channel, PacketByteBuf buf) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new NetPayload.PayloadPacket(channel, buf)));
      }
   }

   public static void write(PacketByteBuf byteBuf, PacketByteBuf data) {
      byteBuf.writeBytes(data.copy());
   }

   public static PacketByteBuf read(PacketByteBuf byteBuf, int maxSize) {
      assertSize(byteBuf, maxSize);
      PacketByteBuf newBuf = new PacketByteBuf(Unpooled.buffer());
      newBuf.writeBytes(byteBuf.copy());
      byteBuf.skipBytes(byteBuf.readableBytes());
      return newBuf;
   }

   private static void assertSize(PacketByteBuf buf, int maxSize) {
      int size = buf.readableBytes();
      if (size < 0 || size > maxSize) {
         throw new IllegalArgumentException("Payload may not be larger than " + maxSize + " bytes");
      }
   }

   public static boolean canUpdate() {
      return mc != null && mc.player != null && mc.world != null;
   }

   public static class PayloadPacket implements CustomPayload {
      public Identifier channel;
      public PacketByteBuf buf;

      public static <T extends PacketByteBuf> PacketCodec<T, NetPayload.PayloadPacket> createCodec(Identifier id) {
         return CustomPayload.codecOf(NetPayload.PayloadPacket::write, buf -> new NetPayload.PayloadPacket(id, NetPayload.read(buf, 1145140)));
      }

      public PayloadPacket(Identifier channel, PacketByteBuf buf) {
         this.channel = channel;
         this.buf = buf;
      }

      public void write(PacketByteBuf buf) {
         NetPayload.write(buf, this.buf);
      }

      public Id<? extends CustomPayload> getId() {
         return new Id(this.channel);
      }
   }
}
