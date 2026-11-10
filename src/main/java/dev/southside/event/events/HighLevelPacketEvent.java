package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.TransferOrigin;
import lombok.Generated;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public class HighLevelPacketEvent extends CancellableEvent {
   public final ClientConnection connection;
   public final PacketListener listener;
   public final Packet<?> packet;
   public final TransferOrigin origin;

   @Generated
   public HighLevelPacketEvent(ClientConnection connection, PacketListener listener, Packet<?> packet, TransferOrigin origin) {
      this.connection = connection;
      this.listener = listener;
      this.packet = packet;
      this.origin = origin;
   }
}
