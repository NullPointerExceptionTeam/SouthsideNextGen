package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.TransferOrigin;
import lombok.Generated;
import net.minecraft.class_2535;
import net.minecraft.class_2596;

public class PacketEvent extends CancellableEvent {
   private final TransferOrigin origin;
   public class_2596<?> packet;
   private final class_2535 connection;

   public boolean isSend() {
      return this.origin == TransferOrigin.SEND;
   }

   @Generated
   public TransferOrigin getOrigin() {
      return this.origin;
   }

   @Generated
   public class_2596<?> getPacket() {
      return this.packet;
   }

   @Generated
   public class_2535 getConnection() {
      return this.connection;
   }

   @Generated
   public PacketEvent(TransferOrigin origin, class_2596<?> packet, class_2535 connection) {
      this.origin = origin;
      this.packet = packet;
      this.connection = connection;
   }

   @Generated
   public void setPacket(class_2596<?> packet) {
      this.packet = packet;
   }
}
