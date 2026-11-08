package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.gui.hud.ScoreBoardHud;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class ScoreBoard extends Module {
   public final Dropdown style = new Dropdown("Style", "Modern", "Modern", "Vanilla", "Simple");
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD(
      "ScoreBoard", new ScoreBoardHud(20.0F, 20.0F, 1, 1.0F)
   );

   public ScoreBoard() {
      super(Category.Render, "ScoreBoard", "计分板");
   }
}
