package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.CPSManager;
import dev.southside.mixininterfaces.IMinecraftClient;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class TriggerBot extends Module {
   public Slider minCPS = new Slider("Min CPS", 8, 0, 20, 1);
   public Slider maxCps = new Slider("Max CPS", 10, 0, 20, 1);
   private final Switch attack1_9 = new Switch("Attack 1.9+", false);
   public static final MSTimer attackTimer = new MSTimer();
   public static class_1297 target;
   private long nextAttackDelay = 100L;

   public TriggerBot() {
      super(Category.Combat, "TriggerBot");
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1765 instanceof class_3966 cnm && cnm.method_17783().equals(class_240.field_1331)) {
            target = cnm.method_17782();
         }

         if (target != null && this.canAttack((class_1309)target) && attackTimer.hasTimePassed(this.nextAttackDelay)) {
            if (!this.attack1_9.getValue() || mc.field_1724.method_7261(0.0F) >= 1.0) {
               ((IMinecraftClient)mc).leftClick();
               CPSManager.INSTANCE.click();
            }

            this.nextAttackDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minCPS.getValue().intValue(), this.maxCps.getValue().intValue()));
         }
      }
   }

   public boolean canAttack(class_1309 target) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null && !mc.field_1724.method_6115()) {
         return RotationUtils.getDistanceToEntity(target) > 5.0F ? false : this.getReach(target) <= 3.0;
      } else {
         return false;
      }
   }

   public double getReach(class_1309 target) {
      return mc.field_1724 == null
         ? 0.0
         : mc.field_1724
            .method_33571()
            .method_1022(RotationUtils.getClosestPoint(mc.field_1724.method_33571(), target.method_5829().method_1014(target.method_5871())));
   }
}
