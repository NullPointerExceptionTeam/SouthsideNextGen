package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.player.BlockBreakHelper;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class FeetCity extends Module {
   private final Slider range = new Slider("Range", 4.0, 2.5, 6.0, 0.1);
   private final Dropdown mode = new Dropdown("Mode", "Legit", "Legit", "ThroughWall");
   private final Switch autoTool = new Switch("Auto Tool", true);
   private final Switch swing = new Switch("Swing", true);
   private final Slider extraDelay = new Slider("Extra Delay (ms)", 0.0, 0.0, 500.0, 5.0);
   private int previousSlot = -1;
   private final BlockBreakHelper blockBreakHelper = new BlockBreakHelper();

   public FeetCity() {
      super(Category.Combat, "FeetCity", "挖掘敌人脚下的方块");
   }

   @Override
   public void onDisable() {
      this.blockBreakHelper.cancel();
      this.restorePreviousSlot();
      super.onDisable();
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() != Stage.POST && !ClientUtils.nullCheck()) {
         this.blockBreakHelper.tick();
         FeetCity.TargetData data = this.findTarget();
         if (data == null) {
            this.blockBreakHelper.cancel();
            this.restorePreviousSlot();
         } else if (!this.mode.isMode("ThroughWall") && !this.hasLineOfSight(data.blockPos())) {
            this.blockBreakHelper.cancel();
            this.restorePreviousSlot();
         } else if (!this.isWithinRange(data.blockPos())) {
            this.blockBreakHelper.cancel();
            this.restorePreviousSlot();
         } else {
            this.mineFeetBlock(data.blockPos());
         }
      }
   }

   private void mineFeetBlock(class_2338 blockPos) {
      if (mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1724 != null) {
            class_2680 state = mc.field_1687.method_8320(blockPos);
            if (!state.method_26215() && !(state.method_26214(mc.field_1687, blockPos) < 0.0F)) {
               this.switchToBestTool(state);
               class_2350 face = this.getClientFacing(blockPos);
               if (face == null) {
                  this.blockBreakHelper.cancel();
                  this.restorePreviousSlot();
               } else {
                  Rotation rotation = RotationUtils.getRotationBlock(blockPos, face, 0.0F);
                  if (rotation != null) {
                     RotationUtils.setRotation(rotation, 1);
                  }

                  if (!this.blockBreakHelper.isBreaking(blockPos)) {
                     this.blockBreakHelper.begin(blockPos, face, this.extraDelay.getValue().longValue(), (pos, brokenState) -> this.restorePreviousSlot());
                  }

                  if (this.swing.getValue() && mc.field_1724.field_6251 <= 0.0F) {
                     mc.field_1724.method_6104(class_1268.field_5808);
                  }
               }
            } else {
               this.blockBreakHelper.cancel();
               this.restorePreviousSlot();
            }
         }
      }
   }

   private boolean isWithinRange(class_2338 pos) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double distanceSq = mc.field_1724.method_19538().method_1025(class_243.method_24953(pos));
         double max = this.range.getValue().doubleValue();
         return distanceSq <= max * max;
      }
   }

   private boolean hasLineOfSight(class_2338 pos) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 eye = mc.field_1724.method_33571();
         class_243 center = class_243.method_24953(pos);
         class_239 hitResult = mc.field_1687.method_17742(new class_3959(eye, center, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         if (hitResult.method_17783() == class_240.field_1333) {
            return true;
         } else if (hitResult.method_17783() == class_240.field_1332) {
            class_3965 blockHitResult = (class_3965)hitResult;
            return blockHitResult.method_17777().equals(pos);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private FeetCity.TargetData findTarget() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_1657 bestPlayer = null;
         class_2338 bestBlock = null;
         double bestDistance = Double.MAX_VALUE;

         for (class_1657 other : mc.field_1687.method_18456()) {
            if (other != mc.field_1724 && other != null && other.method_5805() && !other.method_7325() && TargetUtils.isTarget(other, true, true)) {
               class_2338 feetBlock = this.getFeetBlock(other);
               if (feetBlock != null) {
                  double distance = mc.field_1724.method_19538().method_1025(class_243.method_24953(feetBlock));
                  double max = this.range.getValue().doubleValue();
                  if (!(distance > max * max) && (bestPlayer == null || distance < bestDistance)) {
                     bestPlayer = other;
                     bestBlock = feetBlock;
                     bestDistance = distance;
                  }
               }
            }
         }

         return bestPlayer != null && bestBlock != null ? new FeetCity.TargetData(bestPlayer, bestBlock) : null;
      } else {
         return null;
      }
   }

   private class_2338 getFeetBlock(class_1657 entity) {
      if (mc.field_1687 == null) {
         return null;
      } else {
         class_2338 feet = class_2338.method_49637(entity.method_23317(), entity.method_23318() - 0.2, entity.method_23321());
         if (this.isBreakable(feet)) {
            return feet;
         } else {
            class_2338 below = feet.method_10074();
            return this.isBreakable(below) ? below : null;
         }
      }
   }

   private boolean isBreakable(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 state = mc.field_1687.method_8320(pos);
         return state.method_26215() ? false : state.method_26214(mc.field_1687, pos) >= 0.0F;
      }
   }

   private class_2350 getClientFacing(class_2338 pos) {
      if (mc.field_1724 == null) {
         return null;
      } else if (mc.field_1724.method_5745(5.0, mc.method_61966().method_60637(false), false) instanceof class_3965 blockHit
         && blockHit.method_17777().equals(pos)) {
         return blockHit.method_17780();
      } else {
         class_243 eye = mc.field_1724.method_33571();
         class_243 center = class_243.method_24953(pos);
         class_243 diff = center.method_1020(eye);
         return class_2350.method_10142(diff.field_1352, diff.field_1351, diff.field_1350).method_10153();
      }
   }

   private void switchToBestTool(class_2680 state) {
      if (this.autoTool.getValue()) {
         if (mc.field_1724 != null) {
            int bestSlot = -1;
            float bestSpeed = 1.0F;

            for (int i = 0; i < 9; i++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(i);
               if (!stack.method_7960()) {
                  float speed = stack.method_7924(state);
                  if (speed > bestSpeed) {
                     bestSpeed = speed;
                     bestSlot = i;
                  }
               }
            }

            if (bestSlot != -1 && mc.field_1724.method_31548().field_7545 != bestSlot) {
               if (this.previousSlot == -1) {
                  this.previousSlot = mc.field_1724.method_31548().field_7545;
               }

               mc.field_1724.method_31548().field_7545 = bestSlot;
            }
         }
      }
   }

   private void restorePreviousSlot() {
      if (mc.field_1724 != null) {
         if (this.previousSlot != -1) {
            mc.field_1724.method_31548().field_7545 = this.previousSlot;
            this.previousSlot = -1;
         }
      }
   }

   private record TargetData(class_1657 player, class_2338 blockPos) {
   }
}
