package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.network.packet.Packet;

public class PacketSendPostEvent {
   private final Packet packet;

   @Generated
   public PacketSendPostEvent(Packet packet) {
      this.packet = packet;
   }

   @Generated
   public Packet getPacket() {
      return this.packet;
   }
}
