package dev.southside.module.impl.render;

import dev.southside.gui.hud.ItemHud;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class ItemHudModule extends Module {
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD(
      "Item HUD", new ItemHud(20.0F, 120.0F, 1, 1.0F)
   );

   public ItemHudModule() {
      super(Category.Render, "ItemHud", "物资统计HUD");
   }
}
