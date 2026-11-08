package dev.southside.module.impl.render;

import dev.southside.gui.hud.TestHUD;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class RenderTest extends Module {
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD(
      "Potion HUD", new TestHUD(10.0F, 80.0F, 1, 1.0F)
   );

   public RenderTest() {
      super(Category.Render, "RenderTest", "绘制测试。");
   }
}
