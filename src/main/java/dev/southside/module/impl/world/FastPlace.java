package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1802;

public class FastPlace extends Module {
   public Slider tick = new Slider("Speed", 1, 0, 6, 1);

   public FastPlace() {
      super(Category.World, "FastPlace", "快速放方块");
   }

   @EventHandler
   public void onMotion(PlayerUpdateEvent event) {
      if (mc.field_1724 != null) {
         class_1792 mainHand = mc.field_1724.method_6047().method_7909();
         class_1792 offHand = mc.field_1724.method_6079().method_7909();
         if (mainHand instanceof class_1747
               && mainHand != class_1802.field_8634
               && mainHand != class_1802.field_8803
               && mainHand != class_1802.field_8543
               && mainHand != class_1802.field_8287
            || offHand instanceof class_1747
               && offHand != class_1802.field_8634
               && offHand != class_1802.field_8803
               && offHand != class_1802.field_8543
               && offHand != class_1802.field_8287) {
            mc.field_1752 = Math.min(mc.field_1752, this.tick.getValue().intValue());
         }
      }
   }
}
