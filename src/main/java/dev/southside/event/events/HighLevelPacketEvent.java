package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import dev.southside.event.TransferOrigin;
import lombok.Generated;
import net.minecraft.class_2535;
import net.minecraft.class_2547;
import net.minecraft.class_2596;

public class HighLevelPacketEvent extends CancellableEvent {
   public final class_2535 connection;
   public final class_2547 listener;
   public final class_2596<?> packet;
   public final TransferOrigin origin;

   @Generated
   public HighLevelPacketEvent(class_2535 connection, class_2547 listener, class_2596<?> packet, TransferOrigin origin) {
      this.connection = connection;
      this.listener = listener;
      this.packet = packet;
      this.origin = origin;
   }
}
