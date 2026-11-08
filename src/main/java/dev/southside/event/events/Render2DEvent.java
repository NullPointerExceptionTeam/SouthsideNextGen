package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_332;

public class Render2DEvent {
   private class_332 drawContext;
   private float renderTick;

   @Generated
   public class_332 getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public float getRenderTick() {
      return this.renderTick;
   }

   @Generated
   public Render2DEvent(class_332 drawContext, float renderTick) {
      this.drawContext = drawContext;
      this.renderTick = renderTick;
   }
}
