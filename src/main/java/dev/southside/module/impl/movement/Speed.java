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
      if (mc.field_1724.method_24828()) {
         mc.field_1724.method_6043();
      }

      mc.field_1690.field_1832.method_23481(PlayerUtils.offGroundTicks >= 2 && PlayerUtils.offGroundTicks % 2 == 0 && !mc.field_1724.method_24828());
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
