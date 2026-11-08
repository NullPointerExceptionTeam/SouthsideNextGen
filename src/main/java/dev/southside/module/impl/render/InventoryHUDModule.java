package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.gui.hud.InventoryHud;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class InventoryHUDModule extends Module {
   public static final Dropdown style = new Dropdown("Style", "Default", "Default", "Simple");
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD("Inventory", new InventoryHud());

   public InventoryHUDModule() {
      super(Category.Render, "InventoryHUD", "Display inventory on screen");
   }
}
