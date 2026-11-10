package dev.southside.module.impl.movement;

import dev.southside.event.events.StrafeEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.PlayerUtils;

public class Speed extends Module {
   public Speed() {
      super(Category.Movement, "Speed", "fake.", false);
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventHandler
   public void onStrafe(StrafeEvent event) {
      if (mc.player.isOnGround()) {
         mc.player.jump();
      }

      mc.options.sneakKey.setPressed(PlayerUtils.offGroundTicks >= 2 && PlayerUtils.offGroundTicks % 2 == 0 && !mc.player.isOnGround());
   }

   @Override
   public int rotationPriority() {
      return 0 + this.extraRotationPriority();
   }

   @Override
   public int extraRotationPriority() {
      return 0;
   }
}
