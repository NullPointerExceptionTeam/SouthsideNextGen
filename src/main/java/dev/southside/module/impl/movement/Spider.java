package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.MovementUtils;
import net.minecraft.class_3532;

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
         if (mc.field_1724.field_5976) {
            if (mc.field_1724.method_5624()) {
               mc.field_1724.method_5728(false);
            }

            if (!this.colled && mc.field_1724.method_24828()) {
               this.colled = true;
               double yaw = MovementUtils.direction();
               event.setX(event.getX() - -class_3532.method_15374((float)yaw) * 1.0E-4F);
               event.setZ(event.getZ() - class_3532.method_15362((float)yaw) * 1.0E-4F);
            }

            if (mc.field_1724.method_18798().field_1351 <= 0.0 && this.colled) {
               event.setOnGround(true);
               mc.field_1724.method_6043();
            }
         }

         if (mc.field_1724.method_24828() && !mc.field_1724.field_5976) {
            this.colled = false;
         }
      }
   }
}
