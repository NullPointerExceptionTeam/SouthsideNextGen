package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.class_2596;

public class PacketReceiveAsyncEvent extends CancellableEvent {
   private class_2596 packet;

   @Generated
   public PacketReceiveAsyncEvent(class_2596 packet) {
      this.packet = packet;
   }

   @Generated
   public class_2596 getPacket() {
      return this.packet;
   }
}
