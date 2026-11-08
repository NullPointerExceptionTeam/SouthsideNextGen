package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class HitBox extends Module {
   public Slider XZExpand = new Slider("XZExpand", 1.0, 0.0, 5.0, 0.01);
   public Slider YExpand = new Slider("YExpand", 0.0, 0.0, 5.0, 0.01);

   public HitBox() {
      super(Category.Combat, "HitBox", "HitBox");
   }
}
