package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.player.KeyPearl;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.render.animation.TimerUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import dev.southside.utils.world.ProjectilePredictionUtils;
import java.util.Optional;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.component.DataComponentTypes;

public class AutoKnockBack extends Module {
   public Slider minRangeValue = new Slider("Min Range", 3, 2, 10, 0.1F);
   public Slider maxRangeValue = new Slider("Max Range", 8, 2, 16, 0.1F);
   public Slider delayValue = new Slider("Delay", 500, 0, 1000, 50);
   public Switch autoSwitchValue = new Switch("AutoSwitch(OnlyMainHand)", true);
   public Switch noSpecialItems = new Switch("No Special Items", true);
   public Switch predictionEnabled = new Switch("Prediction", true);
   public Slider predictionTicks = new Slider("Predict Ticks", 4, 1, 10, 1);
   public Switch wallCheck = new Switch("Wall Check", true);
   TimerUtil delay = new TimerUtil();
   LivingEntity target;
   int currentSlot = -1;
   public boolean interactRequired;

   public AutoKnockBack() {
      super(Category.Combat, "AutoKnockBack", "test");
   }

   @Override
   public void initPostRunnable() {
      this.addRangedValueRestrict(this.minRangeValue, this.maxRangeValue);
      this.hideIf(this.predictionTicks.getLabel(), () -> !this.predictionEnabled.getValue());
      this.hideIf(this.wallCheck.getLabel(), () -> !this.predictionEnabled.getValue());
   }

   @Override
   public void onDisable() {
      super.onDisable();
      ProjectilePredictionUtils.clearHistory();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.player != null && mc.world != null && this.predictionEnabled.getValue()) {
            for (Entity entity : mc.world.getEntities()) {
               if (entity instanceof PlayerEntity player && player.isAlive() && TargetUtils.isTarget(player, true, true)) {
                  double distSq = mc.player.squaredDistanceTo(player);
                  double maxR = this.maxRangeValue.getValue().floatValue();
                  if (distSq <= maxR * maxR) {
                     ProjectilePredictionUtils.recordPosition(player);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (mc.player != null
         && mc.world != null
         && !mc.player.isUsingItem()
         && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
         && mc.currentScreen == null
         && !mc.options.attackKey.isPressed()) {
         if (!BowSpammer.isWorking) {
            if (!KeyPearl.isWorking()) {
               Optional<PlayerEntity> target = getTarget(this.minRangeValue.getValue().floatValue(), this.maxRangeValue.getValue().floatValue());
               this.target = null;
               if (!target.isEmpty()) {
                  this.target = (LivingEntity)target.get();
                  PlayerEntity player = target.get();
                  Rotation targetRotation;
                  if (this.predictionEnabled.getValue()) {
                     int ticks = this.predictionTicks.getValue().intValue();
                     Vec3d predictedPos = ProjectilePredictionUtils.predictTargetPosition(player, ticks, this.wallCheck.getValue());
                     targetRotation = ProjectilePredictionUtils.getThrowableRotation(predictedPos);
                  } else {
                     targetRotation = RotationUtils.getPlayerRotations(player);
                  }

                  if (mc.player.canSee(player)) {
                     if (this.delay.hasReached(this.delayValue.getValue().longValue())
                        && (this.autoSwitchValue.getValue() && this.getThrowSlot() != -1 || !this.isOffHandStackEmpty())) {
                        RotationUtils.setRotation(targetRotation);
                        this.interactRequired = true;
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (mc.player != null) {
         if (this.currentSlot != -1) {
            mc.player.getInventory().selectedSlot = this.currentSlot;
            this.currentSlot = -1;
         }

         if (this.interactRequired && !mc.player.usingItem) {
            if (this.delay.hasReached(this.delayValue.getValue().longValue()) && !this.isOffHandStackEmpty() && !this.autoSwitchValue.getValue()) {
               ActionResult res = this.interactItem(mc.player, Hand.OFF_HAND);
               if (res.isAccepted()) {
                  mc.player.swingHand(Hand.OFF_HAND);
                  this.delay.reset();
                  this.interactRequired = false;
               }
            }

            if (this.delay.hasReached(this.delayValue.getValue().longValue()) && this.getThrowSlot() != -1 && this.autoSwitchValue.getValue()) {
               if (KillAura.targets.isEmpty() && mc.player.getInventory().getMainHandStack().getItem() != Items.BOW) {
                  int slot = this.getThrowSlot();
                  if (slot != -1) {
                     this.currentSlot = mc.player.getInventory().selectedSlot;
                     mc.player.getInventory().selectedSlot = slot;
                  }
               }

               if (mc.player.getMainHandStack().getItem() == Items.SNOWBALL || mc.player.getMainHandStack().getItem() == Items.EGG) {
                  ActionResult res = this.interactItem(mc.player, Hand.MAIN_HAND);
                  if (res.isAccepted()) {
                     mc.player.swingHand(Hand.MAIN_HAND);
                     this.delay.reset();
                     this.interactRequired = false;
                  }
               }
            }
         }
      }
   }

   private boolean hasCustomName(ItemStack stack) {
      return stack.contains(DataComponentTypes.CUSTOM_NAME);
   }

   private boolean isThrowable(ItemStack stack) {
      return stack.getItem() == Items.SNOWBALL || stack.getItem() == Items.EGG;
   }

   public boolean isOffHandStackEmpty() {
      ItemStack offHandStack = mc.player.getOffHandStack();
      return !this.isThrowable(offHandStack) ? true : this.noSpecialItems.getValue() && this.hasCustomName(offHandStack);
   }

   int getThrowSlot() {
      if (!(mc.player.getInventory().getMainHandStack().getItem() instanceof EnderPearlItem)
         && !(mc.player.getInventory().getMainHandStack().getItem() instanceof BowItem)
         && !(mc.player.getInventory().getMainHandStack().getItem() instanceof CrossbowItem)) {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isThrowable(stack) && (!this.noSpecialItems.getValue() || !this.hasCustomName(stack))) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static Optional<PlayerEntity> getTarget(float min, float max) {
      double minRange = min * min;
      double maxRange = max * max;
      PlayerEntity closestTarget = null;
      double closestDistanceSq = Double.MAX_VALUE;

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof PlayerEntity player && player.isAlive() && TargetUtils.isTarget(player, true, true)) {
            double distanceSq = mc.player.squaredDistanceTo(player);
            if (distanceSq <= maxRange && distanceSq >= minRange && distanceSq < closestDistanceSq) {
               closestDistanceSq = distanceSq;
               closestTarget = player;
            }
         }
      }

      return Optional.ofNullable(closestTarget);
   }

   public ActionResult interactItem(PlayerEntity player, Hand hand) {
      return (ActionResult)(mc.interactionManager != null ? mc.interactionManager.interactItem(player, hand) : ActionResult.FAIL);
   }
}
