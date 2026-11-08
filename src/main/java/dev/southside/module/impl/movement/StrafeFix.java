package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.rotation.MoveFixMode;

public class StrafeFix extends Module {
   public Dropdown mode = new Dropdown("Mode", "Silent", "Silent", "Strict", "Off");

   public StrafeFix() {
      super(Category.Movement, "StrafeFix", "MOVEFIX ", true);
   }

   public MoveFixMode getMode() {
      String currentMode = this.mode.getMode();

      return switch (currentMode) {
         case "Silent" -> MoveFixMode.Silent;
         case "Strict" -> MoveFixMode.Strict;
         default -> MoveFixMode.Off;
      };
   }
}
