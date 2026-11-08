package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.EnchantmentUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1893;
import net.minecraft.class_3532;

public class KBController extends Module {
   public final Slider horizontalAngle = new Slider("Horizontal Angle", 0.0, -180.0, 180.0, 1.0);
   public final Switch requireKnockbackEnchant = new Switch("Only KB Enchant", false);
   private class_1297 pendingTarget;
   private Rotation pendingRotation;
   private boolean spoofRotationSent;
   private boolean bypassAttackHook;

   public KBController() {
      super(Category.Combat, "KB Controller", "Control attack knockback direction");
   }

   public boolean isBuffering() {
      return this.isEnabled() && this.pendingTarget != null;
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.resetState();
   }

   @EventHandler(
      priority = 200
   )
   public void onAttack(AttackEvent event) {
      if (event.stage == Stage.PRE && mc.field_1724 != null && mc.field_1687 != null) {
         if (this.bypassAttackHook) {
            this.bypassAttackHook = false;
         } else if (event.target != null && event.target.method_5805()) {
            if (!this.requireKnockbackEnchant.getValue() || EnchantmentUtils.getLevel(class_1893.field_9121, mc.field_1724.method_6047()) > 0) {
               event.setCancelled(true);
               this.pendingTarget = event.target;
               Rotation baseRotation = RotationUtils.getRotationSafely();
               float spoofYaw = class_3532.method_15393(baseRotation.yaw + this.horizontalAngle.getValue().floatValue());
               this.pendingRotation = new Rotation(spoofYaw, baseRotation.pitch);
               this.spoofRotationSent = false;
            }
         }
      }
   }

   @EventHandler(
      priority = 100
   )
   public void onRotation(RotationEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.pendingTarget != null && this.pendingRotation != null) {
         if (!this.pendingTarget.method_5805()) {
            this.resetState();
         } else {
            if (!this.spoofRotationSent) {
               RotationUtils.setRotation(this.pendingRotation, this.rotationPriority());
            }
         }
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (event.getState() == Stage.POST && mc.field_1724 != null && mc.field_1687 != null && this.pendingTarget != null && this.pendingRotation != null) {
         if (!this.pendingTarget.method_5805()) {
            this.resetState();
         } else {
            if (!this.spoofRotationSent) {
               float yawDiff = Math.abs(class_3532.method_15393(event.getYaw() - this.pendingRotation.yaw));
               float pitchDiff = Math.abs(event.getPitch() - this.pendingRotation.pitch);
               if (yawDiff < 1.0F && pitchDiff < 1.0F) {
                  this.spoofRotationSent = true;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -200
   )
   public void onPlayerUpdate(PlayerUpdateEvent event) {
      if (this.spoofRotationSent && mc.field_1724 != null && mc.field_1687 != null && this.pendingTarget != null && mc.field_1761 != null) {
         if (!this.pendingTarget.method_5805()) {
            this.resetState();
         } else {
            this.bypassAttackHook = true;
            mc.field_1761.method_2918(mc.field_1724, this.pendingTarget);
            mc.field_1724.method_6104(class_1268.field_5808);
            this.resetState();
         }
      }
   }

   @Override
   public int rotationPriority() {
      return 2;
   }

   private void resetState() {
      this.pendingTarget = null;
      this.pendingRotation = null;
      this.spoofRotationSent = false;
      this.bypassAttackHook = false;
   }
}
