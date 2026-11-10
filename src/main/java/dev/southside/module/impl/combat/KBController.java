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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.math.MathHelper;

public class KBController extends Module {
   public final Slider horizontalAngle = new Slider("Horizontal Angle", 0.0, -180.0, 180.0, 1.0);
   public final Switch requireKnockbackEnchant = new Switch("Only KB Enchant", false);
   private Entity pendingTarget;
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
      if (event.stage == Stage.PRE && mc.player != null && mc.world != null) {
         if (this.bypassAttackHook) {
            this.bypassAttackHook = false;
         } else if (event.target != null && event.target.isAlive()) {
            if (!this.requireKnockbackEnchant.getValue() || EnchantmentUtils.getLevel(Enchantments.KNOCKBACK, mc.player.getMainHandStack()) > 0) {
               event.setCancelled(true);
               this.pendingTarget = event.target;
               Rotation baseRotation = RotationUtils.getRotationSafely();
               float spoofYaw = MathHelper.wrapDegrees(baseRotation.yaw + this.horizontalAngle.getValue().floatValue());
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
      if (mc.player != null && mc.world != null && this.pendingTarget != null && this.pendingRotation != null) {
         if (!this.pendingTarget.isAlive()) {
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
      if (event.getState() == Stage.POST && mc.player != null && mc.world != null && this.pendingTarget != null && this.pendingRotation != null) {
         if (!this.pendingTarget.isAlive()) {
            this.resetState();
         } else {
            if (!this.spoofRotationSent) {
               float yawDiff = Math.abs(MathHelper.wrapDegrees(event.getYaw() - this.pendingRotation.yaw));
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
      if (this.spoofRotationSent && mc.player != null && mc.world != null && this.pendingTarget != null && mc.interactionManager != null) {
         if (!this.pendingTarget.isAlive()) {
            this.resetState();
         } else {
            this.bypassAttackHook = true;
            mc.interactionManager.attackEntity(mc.player, this.pendingTarget);
            mc.player.swingHand(Hand.MAIN_HAND);
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
