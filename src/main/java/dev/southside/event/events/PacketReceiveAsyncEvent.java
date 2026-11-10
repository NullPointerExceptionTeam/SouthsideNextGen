package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.network.packet.Packet;

public class PacketReceiveAsyncEvent extends CancellableEvent {
   private Packet packet;

   @Generated
   public PacketReceiveAsyncEvent(Packet packet) {
      this.packet = packet;
   }

   @Generated
   public Packet getPacket() {
      return this.packet;
   }
}
