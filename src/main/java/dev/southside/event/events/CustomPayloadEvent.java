package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_8710;

public class CustomPayloadEvent {
   private final class_8710 payload;

   @Generated
   public CustomPayloadEvent(class_8710 payload) {
      this.payload = payload;
   }

   @Generated
   public class_8710 getPayload() {
      return this.payload;
   }
}
