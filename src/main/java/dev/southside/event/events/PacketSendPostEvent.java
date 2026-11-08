package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_2596;

public class PacketSendPostEvent {
   private final class_2596 packet;

   @Generated
   public PacketSendPostEvent(class_2596 packet) {
      this.packet = packet;
   }

   @Generated
   public class_2596 getPacket() {
      return this.packet;
   }
}
