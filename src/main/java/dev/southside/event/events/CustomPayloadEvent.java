package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.network.packet.CustomPayload;

public class CustomPayloadEvent {
   private final CustomPayload payload;

   @Generated
   public CustomPayloadEvent(CustomPayload payload) {
      this.payload = payload;
   }

   @Generated
   public CustomPayload getPayload() {
      return this.payload;
   }
}
