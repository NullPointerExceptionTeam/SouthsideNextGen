package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class NoHurtCam extends Module {
      public final Switch hurtCam = new Switch("HurtCam", true);
      public final Slider strength = new Slider("Strength", 0.0, 0.0, 1.0, 0.01);
      public final Switch noBob = new Switch("NoBob", false);

   public NoHurtCam() {
      super(Category.Render, "NoHurtCam", "关闭受伤镜头摇晃");
   }

      public boolean shouldCancelTilt() {
      return this.isEnabled() && this.hurtCam.getValue() && this.strength.getDouble() <= 0.001;
   }

      public boolean shouldScaleTilt() {
      return this.isEnabled() && this.hurtCam.getValue() && this.strength.getDouble() > 0.001;
   }

   public float tiltScale() {
      return (float) this.strength.getDouble();
   }
}
