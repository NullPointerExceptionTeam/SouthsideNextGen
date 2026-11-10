package dev.southside.utils.network;

import lombok.Generated;
import net.minecraft.network.packet.Packet;

public class TimedPacket {
   private final Packet packet;
   private final long time;

   public TimedPacket(Packet<?> packet) {
      this.packet = packet;
      this.time = System.currentTimeMillis();
   }

   @Generated
   public Packet getPacket() {
      return this.packet;
   }

   @Generated
   public long getTime() {
      return this.time;
   }
}
