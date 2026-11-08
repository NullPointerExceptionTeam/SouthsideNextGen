package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.gui.hud.PotionHud;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class PotionEffects extends Module {
   public static final Dropdown style = new Dropdown("Style", "Default", "Default", "Simple");
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD(
      "Potion HUD", new PotionHud(10.0F, 80.0F, 1, 1.0F)
   );

   public PotionEffects() {
      super(Category.Render, "PotionEffects", "药水显示");
   }
}
