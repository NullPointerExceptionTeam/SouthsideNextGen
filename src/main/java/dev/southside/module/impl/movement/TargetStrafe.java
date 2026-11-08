package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class TargetStrafe extends Module {
   public Switch onlyJump = new Switch("Only Jump", true);

   public TargetStrafe() {
      super(Category.Movement, "TargetStrafe");
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
   }
}
