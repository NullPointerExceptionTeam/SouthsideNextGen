package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.UpdateFoVEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class CustomFOV extends Module {
   public final Slider fovValue = new Slider("FOV", 90, 10, 300, 1);

   public CustomFOV() {
      super(Category.Render, "CustomFOV", "自定义视角场大小。");
   }

   @EventHandler
   public void onFoV(UpdateFoVEvent event) {
      float targetFov = this.fovValue.getValue().floatValue();
      float baseFov = ((Integer)mc.options.getFov().getValue()).intValue();
      if (baseFov != 0.0F) {
         event.setFov(targetFov / baseFov);
      }
   }
}
