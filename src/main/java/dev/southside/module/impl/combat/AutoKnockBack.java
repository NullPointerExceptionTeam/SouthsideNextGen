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
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1776;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_9334;

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
   class_1309 target;
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
         if (mc.field_1724 != null && mc.field_1687 != null && this.predictionEnabled.getValue()) {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity instanceof class_1657 player && player.method_5805() && TargetUtils.isTarget(player, true, true)) {
                  double distSq = mc.field_1724.method_5858(player);
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
      if (mc.field_1724 != null
         && mc.field_1687 != null
         && !mc.field_1724.method_6115()
         && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
         && mc.field_1755 == null
         && !mc.field_1690.field_1886.method_1434()) {
         if (!BowSpammer.isWorking) {
            if (!KeyPearl.isWorking()) {
               Optional<class_1657> target = getTarget(this.minRangeValue.getValue().floatValue(), this.maxRangeValue.getValue().floatValue());
               this.target = null;
               if (!target.isEmpty()) {
                  this.target = (class_1309)target.get();
                  class_1657 player = target.get();
                  Rotation targetRotation;
                  if (this.predictionEnabled.getValue()) {
                     int ticks = this.predictionTicks.getValue().intValue();
                     class_243 predictedPos = ProjectilePredictionUtils.predictTargetPosition(player, ticks, this.wallCheck.getValue());
                     targetRotation = ProjectilePredictionUtils.getThrowableRotation(predictedPos);
                  } else {
                     targetRotation = RotationUtils.getPlayerRotations(player);
                  }

                  if (mc.field_1724.method_6057(player)) {
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
      if (mc.field_1724 != null) {
         if (this.currentSlot != -1) {
            mc.field_1724.method_31548().field_7545 = this.currentSlot;
            this.currentSlot = -1;
         }

         if (this.interactRequired && !mc.field_1724.field_3915) {
            if (this.delay.hasReached(this.delayValue.getValue().longValue()) && !this.isOffHandStackEmpty() && !this.autoSwitchValue.getValue()) {
               class_1269 res = this.interactItem(mc.field_1724, class_1268.field_5810);
               if (res.method_23665()) {
                  mc.field_1724.method_6104(class_1268.field_5810);
                  this.delay.reset();
                  this.interactRequired = false;
               }
            }

            if (this.delay.hasReached(this.delayValue.getValue().longValue()) && this.getThrowSlot() != -1 && this.autoSwitchValue.getValue()) {
               if (KillAura.targets.isEmpty() && mc.field_1724.method_31548().method_7391().method_7909() != class_1802.field_8102) {
                  int slot = this.getThrowSlot();
                  if (slot != -1) {
                     this.currentSlot = mc.field_1724.method_31548().field_7545;
                     mc.field_1724.method_31548().field_7545 = slot;
                  }
               }

               if (mc.field_1724.method_6047().method_7909() == class_1802.field_8543 || mc.field_1724.method_6047().method_7909() == class_1802.field_8803) {
                  class_1269 res = this.interactItem(mc.field_1724, class_1268.field_5808);
                  if (res.method_23665()) {
                     mc.field_1724.method_6104(class_1268.field_5808);
                     this.delay.reset();
                     this.interactRequired = false;
                  }
               }
            }
         }
      }
   }

   private boolean hasCustomName(class_1799 stack) {
      return stack.method_57826(class_9334.field_49631);
   }

   private boolean isThrowable(class_1799 stack) {
      return stack.method_7909() == class_1802.field_8543 || stack.method_7909() == class_1802.field_8803;
   }

   public boolean isOffHandStackEmpty() {
      class_1799 offHandStack = mc.field_1724.method_6079();
      return !this.isThrowable(offHandStack) ? true : this.noSpecialItems.getValue() && this.hasCustomName(offHandStack);
   }

   int getThrowSlot() {
      if (!(mc.field_1724.method_31548().method_7391().method_7909() instanceof class_1776)
         && !(mc.field_1724.method_31548().method_7391().method_7909() instanceof class_1753)
         && !(mc.field_1724.method_31548().method_7391().method_7909() instanceof class_1764)) {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (this.isThrowable(stack) && (!this.noSpecialItems.getValue() || !this.hasCustomName(stack))) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static Optional<class_1657> getTarget(float min, float max) {
      double minRange = min * min;
      double maxRange = max * max;
      class_1657 closestTarget = null;
      double closestDistanceSq = Double.MAX_VALUE;

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1657 player && player.method_5805() && TargetUtils.isTarget(player, true, true)) {
            double distanceSq = mc.field_1724.method_5858(player);
            if (distanceSq <= maxRange && distanceSq >= minRange && distanceSq < closestDistanceSq) {
               closestDistanceSq = distanceSq;
               closestTarget = player;
            }
         }
      }

      return Optional.ofNullable(closestTarget);
   }

   public class_1269 interactItem(class_1657 player, class_1268 hand) {
      return (class_1269)(mc.field_1761 != null ? mc.field_1761.method_2919(player, hand) : class_1269.field_5814);
   }
}
