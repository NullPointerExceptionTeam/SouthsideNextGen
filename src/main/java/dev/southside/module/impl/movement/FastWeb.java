package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.EntityCobwebCollisionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.player.PlayerUtils;

public class FastWeb extends Module {
   public final Switch noYMotion = new Switch("Disable Vertical Motion", true);

   public FastWeb() {
      super(Category.Movement, "FastWeb", "Fast Web");
   }

   @EventHandler
   public void onEntityCobwebCollision(EntityCobwebCollisionEvent event) {
      if (event.getEntity().method_5628() == mc.field_1724.method_5628()) {
         if (!ClientUtils.nullCheck()) {
            if (PlayerUtils.onGroundTicks >= 2) {
               PlayerUtils.strafe(0.5F);
            } else {
               PlayerUtils.strafe(0.35F);
            }

            if (!this.noYMotion.getValue()) {
               mc.field_1724
                  .method_18800(
                     mc.field_1724.method_18798().field_1352, mc.field_1690.field_1903.method_1434() ? 1.4 : -3.6, mc.field_1724.method_18798().field_1350
                  );
            }
         }
      }
   }
}
