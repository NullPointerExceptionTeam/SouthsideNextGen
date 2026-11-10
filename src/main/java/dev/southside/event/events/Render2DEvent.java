package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.client.gui.DrawContext;

public class Render2DEvent {
   private DrawContext drawContext;
   private float renderTick;

   @Generated
   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public float getRenderTick() {
      return this.renderTick;
   }

   @Generated
   public Render2DEvent(DrawContext drawContext, float renderTick) {
      this.drawContext = drawContext;
      this.renderTick = renderTick;
   }
}
