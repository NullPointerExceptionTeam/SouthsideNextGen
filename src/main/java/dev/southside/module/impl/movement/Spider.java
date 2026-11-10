package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.MovementUtils;
import net.minecraft.util.math.MathHelper;

public class Spider extends Module {
   public Dropdown mode = new Dropdown("Mode", "Test", "Test");
   private boolean colled = false;

   public Spider() {
      super(Category.Movement, "Spider", "Allows you to walk on walls");
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      this.setSuffix(this.mode.getMode());
      if (this.mode.isMode("Test")) {
         if (mc.player.horizontalCollision) {
            if (mc.player.isSprinting()) {
               mc.player.setSprinting(false);
            }

            if (!this.colled && mc.player.isOnGround()) {
               this.colled = true;
               double yaw = MovementUtils.direction();
               event.setX(event.getX() - -MathHelper.sin((float)yaw) * 1.0E-4F);
               event.setZ(event.getZ() - MathHelper.cos((float)yaw) * 1.0E-4F);
            }

            if (mc.player.getVelocity().y <= 0.0 && this.colled) {
               event.setOnGround(true);
               mc.player.jump();
            }
         }

         if (mc.player.isOnGround() && !mc.player.horizontalCollision) {
            this.colled = false;
         }
      }
   }
}
