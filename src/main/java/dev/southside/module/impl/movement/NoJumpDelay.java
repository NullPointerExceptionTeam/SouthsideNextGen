package dev.southside.module.impl.movement;

import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;

public class NoJumpDelay extends Module {
   public NoJumpDelay() {
      super(Category.Movement, "NoJumpDelay", "Removes the delay between jumping and falling");
   }

   @EventHandler(
      priority = 8964
   )
   public void onRotate(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         mc.player.jumpingCooldown = 0;
      }
   }
}
