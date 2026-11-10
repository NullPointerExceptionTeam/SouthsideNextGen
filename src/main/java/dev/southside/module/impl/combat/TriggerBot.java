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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class TriggerBot extends Module {
   public Slider minCPS = new Slider("Min CPS", 8, 0, 20, 1);
   public Slider maxCps = new Slider("Max CPS", 10, 0, 20, 1);
   private final Switch attack1_9 = new Switch("Attack 1.9+", false);
   public static final MSTimer attackTimer = new MSTimer();
   public static Entity target;
   private long nextAttackDelay = 100L;

   public TriggerBot() {
      super(Category.Combat, "TriggerBot");
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.crosshairTarget instanceof EntityHitResult cnm && cnm.getType().equals(Type.ENTITY)) {
            target = cnm.getEntity();
         }

         if (target != null && this.canAttack((LivingEntity)target) && attackTimer.hasTimePassed(this.nextAttackDelay)) {
            if (!this.attack1_9.getValue() || mc.player.getAttackCooldownProgress(0.0F) >= 1.0) {
               ((IMinecraftClient)mc).leftClick();
               CPSManager.INSTANCE.click();
            }

            this.nextAttackDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minCPS.getValue().intValue(), this.maxCps.getValue().intValue()));
         }
      }
   }

   public boolean canAttack(LivingEntity target) {
      if (mc.player != null && mc.world != null && target != null && !mc.player.isUsingItem()) {
         return RotationUtils.getDistanceToEntity(target) > 5.0F ? false : this.getReach(target) <= 3.0;
      } else {
         return false;
      }
   }

   public double getReach(LivingEntity target) {
      return mc.player == null
         ? 0.0
         : mc.player
            .getEyePos()
            .distanceTo(RotationUtils.getClosestPoint(mc.player.getEyePos(), target.getBoundingBox().expand(target.getTargetingMargin())));
   }
}
