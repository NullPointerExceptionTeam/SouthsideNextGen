package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.Stage;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;

public class AntiWeb extends Module {
   private final class_310 mc = class_310.method_1551();
   public Slider recycleTick = new Slider("Recycle Tick", 10, 5, 20, 1);
   private AntiWeb.Phase phase = AntiWeb.Phase.IDLE;
   private class_2338 waterPos;
   private class_2338 pickupPos;
   private int waitTicks;
   private int pickupAttempts;
   private boolean waitingRetry;
   private boolean snapRequested;
   private final MSTimer retryTimer = new MSTimer();

   public AntiWeb() {
      super(Category.Player, "AntiWeb", "Anti Webs");
   }

   @Override
   public void onEnable() {
      this.resetState();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetState();
      super.onDisable();
   }

   private void resetState() {
      this.phase = AntiWeb.Phase.IDLE;
      this.waterPos = null;
      this.pickupPos = null;
      this.waitTicks = 0;
      this.pickupAttempts = 0;
      this.waitingRetry = false;
      this.snapRequested = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
            switch (this.phase) {
               case IDLE:
                  class_2338 webPos = this.findLowestWebPos();
                  if (webPos == null) {
                     return;
                  }

                  if (this.findWaterBucket() == -1) {
                     return;
                  }

                  this.waterPos = webPos;
                  if (this.isEyeInCobweb()) {
                     if (this.pourWater()) {
                        this.phase = AntiWeb.Phase.WAIT;
                        this.waitTicks = 0;
                     }
                  } else {
                     this.phase = AntiWeb.Phase.POUR;
                  }
               case POUR:
               case PICKUP:
               default:
                  break;
               case WAIT:
                  if (++this.waitTicks >= this.recycleTick.getValue().intValue()) {
                     this.pickupPos = this.findWaterSource();
                     this.pickupAttempts = 0;
                     this.snapRequested = true;
                     this.phase = AntiWeb.Phase.PICKUP;
                  }
            }
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (this.mc.field_1724 != null) {
         class_243 aim;
         if (this.phase == AntiWeb.Phase.POUR) {
            aim = this.waterPos.method_46558();
         } else {
            if (this.phase != AntiWeb.Phase.PICKUP) {
               return;
            }

            if (this.waitingRetry || !this.snapRequested) {
               return;
            }

            class_2338 source = this.pickupPos != null ? this.pickupPos : this.waterPos;
            aim = source.method_46558();
         }

         Rotation rot = RotationUtils.calculate(this.mc.field_1724.method_33571(), aim);
         RotationUtils.setRotation(rot, this.rotationPriority(), MoveFixMode.Silent);
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.mc.field_1724 != null) {
         if (RotationUtils.accepted(this.rotationPriority())) {
            if (this.phase == AntiWeb.Phase.POUR && this.isRotationOnTarget(this.waterPos.method_46558())) {
               if (this.pourWater()) {
                  this.phase = AntiWeb.Phase.WAIT;
                  this.waitTicks = 0;
               } else {
                  this.resetState();
               }
            } else if (this.phase == AntiWeb.Phase.PICKUP) {
               if (this.waitingRetry) {
                  if (!this.retryTimer.hasPassed(1000L)) {
                     return;
                  }

                  this.waitingRetry = false;
               }

               if (!this.snapRequested) {
                  return;
               }

               class_2338 source = this.pickupPos != null ? this.pickupPos : this.waterPos;
               if (this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_26204() != class_2246.field_10382
                  && !this.isRotationOnTarget(source.method_46558())) {
                  return;
               }

               this.snapRequested = false;
               if (this.pickupWater()) {
                  this.resetState();
               } else if (++this.pickupAttempts >= 5) {
                  this.resetState();
               } else {
                  this.retryTimer.reset();
                  this.waitingRetry = true;
                  this.snapRequested = true;
                  this.pickupPos = this.findWaterSource();
                  this.phase = AntiWeb.Phase.PICKUP;
               }
            }
         }
      }
   }

   private boolean isRotationOnTarget(class_243 aimPoint) {
      if (RotationUtils.getRotation() == null) {
         return false;
      } else {
         Rotation current = RotationUtils.getRotationSafely();
         Rotation wanted = RotationUtils.calculate(this.mc.field_1724.method_33571(), aimPoint);
         return Math.abs(RotationUtils.getAngleDifference(current.yaw, wanted.yaw)) < 12.0F
            && Math.abs(RotationUtils.getAngleDifference(current.pitch, wanted.pitch)) < 12.0F;
      }
   }

   private boolean isEyeInCobweb() {
      return this.mc.field_1687.method_8320(class_2338.method_49638(this.mc.field_1724.method_33571())).method_26204() == class_2246.field_10343;
   }

   private boolean pourWater() {
      int waterBucketSlot = this.findWaterBucket();
      if (waterBucketSlot == -1) {
         return false;
      } else {
         int previousSlot = this.mc.field_1724.method_31548().field_7545;
         this.mc.field_1724.method_31548().field_7545 = waterBucketSlot;
         class_1269 result = this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
         this.mc.field_1724.method_31548().field_7545 = previousSlot;
         return result.method_23665();
      }
   }

   private boolean pickupWater() {
      int emptyBucketSlot = this.findEmptyBucket();
      if (emptyBucketSlot == -1) {
         return false;
      } else {
         int previousSlot = this.mc.field_1724.method_31548().field_7545;
         this.mc.field_1724.method_31548().field_7545 = emptyBucketSlot;
         this.mc.field_1761.method_2919(this.mc.field_1724, class_1268.field_5808);
         this.mc.field_1724.method_31548().field_7545 = previousSlot;
         return this.mc.field_1724.method_31548().method_5438(emptyBucketSlot).method_31574(class_1802.field_8705);
      }
   }

   private class_2338 findWaterSource() {
      class_2338 best = null;
      double bestDist = Double.MAX_VALUE;

      for (int x = -2; x <= 2; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
               class_2338 pos = this.waterPos.method_10069(x, y, z);
               if (this.mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10382) {
                  double dist = this.mc.field_1724.method_24515().method_10262(pos);
                  if (dist < bestDist) {
                     best = pos;
                     bestDist = dist;
                  }
               }
            }
         }
      }

      return best;
   }

   private class_2338 findLowestWebPos() {
      class_238 box = this.mc.field_1724.method_5829();
      class_2338 best = null;

      for (int x = (int)Math.floor(box.field_1323); x <= (int)Math.floor(box.field_1320); x++) {
         for (int z = (int)Math.floor(box.field_1321); z <= (int)Math.floor(box.field_1324); z++) {
            for (int y = (int)Math.floor(box.field_1322); y <= (int)Math.floor(box.field_1325); y++) {
               class_2338 pos = new class_2338(x, y, z);
               if (this.mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10343 && (best == null || y < best.method_10264())) {
                  best = pos;
               }
            }
         }
      }

      return best;
   }

   private int findWaterBucket() {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == class_1802.field_8705) {
            return i;
         }
      }

      return -1;
   }

   private int findEmptyBucket() {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = this.mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == class_1802.field_8550) {
            return i;
         }
      }

      return -1;
   }

   @Override
   public int rotationPriority() {
      return 3;
   }

   private static enum Phase {
      IDLE,
      POUR,
      WAIT,
      PICKUP;
   }
}
