package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.TransferOrigin;
import lombok.Generated;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

public class PacketEvent extends CancellableEvent {
   private final TransferOrigin origin;
   public Packet<?> packet;
   private final ClientConnection connection;

   public boolean isSend() {
      return this.origin == TransferOrigin.SEND;
   }

   @Generated
   public TransferOrigin getOrigin() {
      return this.origin;
   }

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public ClientConnection getConnection() {
      return this.connection;
   }

   @Generated
   public PacketEvent(TransferOrigin origin, Packet<?> packet, ClientConnection connection) {
      this.origin = origin;
      this.packet = packet;
      this.connection = connection;
   }

   @Generated
   public void setPacket(Packet<?> packet) {
      this.packet = packet;
   }
}
