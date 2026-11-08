package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.player.AutoBucket;
import dev.southside.module.impl.player.StealerN;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public class AimAssist extends Module {
   private final Slider range = new Slider("范围", 4.5, 1.0, 8.0, 0.1);
   private final Slider fov = new Slider("视野", 90, 10, 180, 1);
   private final Slider smoothness = new Slider("平滑度", 8.0, 2.0, 20.0, 0.5);
   private final Switch onlyWhileAttacking = new Switch("仅攻击时", true);
   private class_1657 target;
   private Rotation pendingRotation;

   public AimAssist() {
      super(Category.Combat, "AimAssist", "平滑地将视角拉向准星附近的玩家");
   }

   @Override
   public void onEnable() {
      this.target = null;
      this.pendingRotation = null;
   }

   @Override
   public void onDisable() {
      this.target = null;
      this.pendingRotation = null;
   }

   @EventHandler(
      priority = -200
   )
   public void onRotation(RotationEvent event) {
      this.pendingRotation = null;
      if (!this.shouldPause() && !event.isDisableRotation() && RotationUtils.accepted(this.rotationPriority())) {
         if (!this.isValidTarget(this.target)) {
            this.target = this.findTarget();
         }

         if (this.target != null) {
            this.pendingRotation = this.getSmoothedRotation(this.target);
            RotationUtils.setRotation(this.pendingRotation, this.rotationPriority());
         }
      } else {
         this.target = null;
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.pendingRotation != null && RotationUtils.getRotation() == this.pendingRotation) {
         this.pendingRotation.apply();
         this.pendingRotation = null;
      }
   }

   private boolean shouldPause() {
      if (mc.field_1724 == null || mc.field_1687 == null || !mc.field_1724.method_5805() || mc.field_1755 != null) {
         return true;
      } else if (this.onlyWhileAttacking.getValue() && !mc.field_1690.field_1886.method_1434()) {
         return true;
      } else if (!mc.field_1724.method_6115() && !InventoryUtils.shouldDisableFeatures()) {
         ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
         if (moduleManager.isEnabled(AutoBucket.class) && moduleManager.get(AutoBucket.class).isBusy()) {
            return true;
         } else {
            return moduleManager.isEnabled(StealerN.class) && StealerN.isWorking() ? true : moduleManager.isEnabled(KillAura.class) && KillAura.target != null;
         }
      } else {
         return true;
      }
   }

   private class_1657 findTarget() {
      class_1657 bestTarget = null;
      double bestAngle = Double.MAX_VALUE;

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (this.isValidTarget(player)) {
            double angle = this.getAngleDistance(player);
            if (angle < bestAngle) {
               bestAngle = angle;
               bestTarget = player;
            }
         }
      }

      return bestTarget;
   }

   private boolean isValidTarget(class_1657 player) {
      return player != null
         && player.method_5805()
         && !player.method_31481()
         && !player.method_7325()
         && mc.field_1724.method_5739(player) <= this.range.getValue().doubleValue()
         && mc.field_1724.method_6057(player)
         && TargetUtils.isTarget(player, true, true)
         && this.getAngleDistance(player) <= this.fov.getValue().doubleValue() / 2.0;
   }

   private Rotation getSmoothedRotation(class_1657 player) {
      Rotation desired = Rotation.from(mc.field_1724.method_33571(), this.getAimPoint(player));
      float currentYaw = mc.field_1724.method_36454();
      float currentPitch = mc.field_1724.method_36455();
      float divisor = this.smoothness.getValue().floatValue();
      float nextYaw = currentYaw + class_3532.method_15393(desired.yaw - currentYaw) / divisor;
      float nextPitch = class_3532.method_15363(currentPitch + (desired.pitch - currentPitch) / divisor, -90.0F, 90.0F);
      float[] fixed = RotationUtils.fixMouseSensitivity(nextYaw, nextPitch, currentYaw, currentPitch);
      return new Rotation(fixed[0], fixed[1]);
   }

   private double getAngleDistance(class_1657 player) {
      Rotation desired = Rotation.from(mc.field_1724.method_33571(), this.getAimPoint(player));
      float yawDifference = class_3532.method_15393(desired.yaw - mc.field_1724.method_36454());
      float pitchDifference = desired.pitch - mc.field_1724.method_36455();
      return Math.hypot(yawDifference, pitchDifference);
   }

   private class_243 getAimPoint(class_1657 player) {
      return new class_243(player.method_23317(), player.method_23318() + player.method_17682() * 0.75, player.method_23321());
   }

   @Override
   public int rotationPriority() {
      return -1;
   }
}
