package dev.southside.module.impl.combat;

import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3675;

public class SuperKnockBack extends Module {
   private int ticks = 0;

   public SuperKnockBack() {
      super(Category.Combat, "SuperKnockBack", "More KnockBack");
   }

   @Override
   public void onEnable() {
      this.ticks = 0;
   }

   @Override
   public void onDisable() {
      this.ticks = 0;
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.ticks = 0;
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (this.ticks == 2) {
         mc.field_1690.field_1894.method_23481(false);
         this.ticks = 1;
      } else if (this.ticks == 1) {
         mc.field_1690.field_1894.method_23481(class_3675.method_15987(mc.method_22683().method_4490(), mc.field_1690.field_1894.method_1429().method_1444()));
         this.ticks = 0;
      }
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      class_1297 entity = event.target;
      if (entity instanceof class_1309) {
         this.ticks = 2;
      }
   }
}
