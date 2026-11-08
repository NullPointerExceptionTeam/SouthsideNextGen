package dev.southside.module.impl.misc;

import dev.southside.gui.minegame.Gobang.GobangScreen;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class MiniGame extends Module {
   public MiniGame() {
      super(Category.Misc, "MineGame");
   }

   @Override
   public void onEnable() {
      mc.method_1507(new GobangScreen());
      this.setEnable(false);
      super.onEnable();
   }
}
