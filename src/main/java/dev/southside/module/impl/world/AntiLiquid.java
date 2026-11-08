package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.player.AutoBucket;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.SlotUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_3965;

public class AntiLiquid extends Module {
   private final Slider range = new Slider("Range", 4.0, 3.0, 5.0, 0.5);
   private final Switch water = new Switch("Water", true);
   private final Switch lava = new Switch("Lava", true);
   private static final int RETRY_COOLDOWN = 15;
   private final Map<class_2338, Integer> attempted = new HashMap<>();
   private static final class_2350[] PLACE_DIRECTIONS = new class_2350[]{
      class_2350.field_11033, class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039, class_2350.field_11036
   };
   private AntiLiquid.Phase phase = AntiLiquid.Phase.SCAN;
   private class_2338 targetFluidPos;
   private class_2338 supportPos;
   private class_2350 placeFace;
   private int delayTicks;
   private int savedSlot = -1;
   private boolean placeReady;

   public AntiLiquid() {
      super(Category.World, "AntiLiquid", "Automatically places blocks on liquid sources in view.");
   }

   @Override
   public void onEnable() {
      this.attempted.clear();
      this.reset();
   }

   @Override
   public void onDisable() {
      if (this.savedSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().field_7545 = this.savedSlot;
      }

      this.reset();
   }

   @Override
   public void reset() {
      this.phase = AntiLiquid.Phase.SCAN;
      this.targetFluidPos = null;
      this.supportPos = null;
      this.placeFace = null;
      this.delayTicks = 0;
      this.savedSlot = -1;
      this.placeReady = false;
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.attempted.clear();
      this.reset();
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         switch (this.phase) {
            case SCAN:
               this.scan();
               break;
            case DELAY:
               if (--this.delayTicks <= 0) {
                  this.phase = AntiLiquid.Phase.ROTATE;
               }
               break;
            case ROTATE:
               this.rotate();
            case PLACE:
            default:
               break;
            case RESTORE:
               if (this.savedSlot != -1) {
                  mc.field_1724.method_31548().field_7545 = this.savedSlot;
                  this.savedSlot = -1;
               }

               this.phase = AntiLiquid.Phase.SCAN;
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.phase == AntiLiquid.Phase.PLACE && this.placeReady) {
            this.placeReady = false;
            ClientRayTraceUtil.updateEyePos();
            Rotation rotation = RotationUtils.getRotation();
            if (rotation == null) {
               this.phase = AntiLiquid.Phase.RESTORE;
            } else if (!ClientRayTraceUtil.didHitBlockFace(rotation, this.supportPos, this.placeFace, false)) {
               this.attempted.put(this.targetFluidPos, 15);
               this.phase = AntiLiquid.Phase.RESTORE;
            } else {
               class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
               if (hit == null) {
                  this.attempted.put(this.targetFluidPos, 15);
                  this.phase = AntiLiquid.Phase.RESTORE;
               } else {
                  class_1269 result = mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
                  if (result.method_23665()) {
                     mc.field_1724.method_6104(class_1268.field_5808);
                  }

                  this.attempted.put(this.targetFluidPos, 15);
                  this.phase = AntiLiquid.Phase.RESTORE;
               }
            }
         }
      }
   }

   private void scan() {
      this.attempted.entrySet().removeIf(e -> e.setValue(e.getValue() - 1) <= 0);
      ClientRayTraceUtil.updateEyePos();
      class_2338 playerPos = mc.field_1724.method_24515();
      int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
      class_243 eyePos = mc.field_1724.method_33571();

      for (int x = -rangeInt; x <= rangeInt; x++) {
         for (int y = -rangeInt; y <= rangeInt; y++) {
            for (int z = -rangeInt; z <= rangeInt; z++) {
               class_2338 pos = playerPos.method_10069(x, y, z);
               if (!this.attempted.containsKey(pos)) {
                  double distance = eyePos.method_1022(class_243.method_24953(pos));
                  if (!(distance > this.range.getValue().floatValue())) {
                     class_3610 fluidState = mc.field_1687.method_8316(pos);
                     if (fluidState.method_15771()) {
                        boolean isWater = mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10382;
                        boolean isLava = mc.field_1687.method_8320(pos).method_26204() == class_2246.field_10164;
                        if ((!isWater || this.water.getValue())
                           && (!isLava || this.lava.getValue())
                           && (isWater || isLava)
                           && (!isWater || !this.isAutoBucketWater())
                           && !this.hasAdjacentSource(pos)
                           && this.canSeeFluid(pos, eyePos)
                           && this.findSupport(pos)) {
                           this.targetFluidPos = pos;
                           this.delayTicks = 1;
                           this.phase = AntiLiquid.Phase.DELAY;
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void rotate() {
      if (this.targetFluidPos != null && this.supportPos != null) {
         int blockSlot = this.findBlockSlot();
         if (blockSlot == -1) {
            this.phase = AntiLiquid.Phase.SCAN;
         } else {
            Rotation rotation = RotationUtils.getRotationToBlock(this.supportPos, this.placeFace);
            if (rotation == null) {
               this.attempted.put(this.targetFluidPos, 15);
               this.phase = AntiLiquid.Phase.SCAN;
            } else {
               this.savedSlot = mc.field_1724.method_31548().field_7545;
               mc.field_1724.method_31548().field_7545 = blockSlot;
               RotationUtils.setRotation(rotation, this.rotationPriority());
               this.placeReady = true;
               this.phase = AntiLiquid.Phase.PLACE;
            }
         }
      } else {
         this.phase = AntiLiquid.Phase.SCAN;
      }
   }

   @Override
   public int rotationPriority() {
      return 2;
   }

   private boolean findSupport(class_2338 fluidPos) {
      for (class_2350 dir : PLACE_DIRECTIONS) {
         class_2338 neighbor = fluidPos.method_10093(dir);
         class_2680 state = mc.field_1687.method_8320(neighbor);
         if (!state.method_26215() && !state.method_51176() && !state.method_45474()) {
            this.supportPos = neighbor;
            this.placeFace = dir.method_10153();
            return true;
         }
      }

      return false;
   }

   private boolean canSeeFluid(class_2338 pos, class_243 eyePos) {
      class_243 blockCenter = class_243.method_24953(pos);
      Rotation rotation = RotationUtils.calculate(blockCenter);
      double fluidDist = eyePos.method_1025(blockCenter);
      class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
      return hit == null ? true : eyePos.method_1025(hit.method_17784()) >= fluidDist;
   }

   private boolean isAutoBucketWater() {
      AutoBucket autoBucket = Southside.get().getModuleManager().get(AutoBucket.class);
      return !autoBucket.isEnabled() ? false : autoBucket.shouldReceive || autoBucket.interactRequired;
   }

   private boolean hasAdjacentSource(class_2338 pos) {
      for (class_2350 dir : class_2350.values()) {
         if (dir != class_2350.field_11036 && dir != class_2350.field_11033) {
            class_2338 neighbor = pos.method_10093(dir);
            class_3610 neighborFluid = mc.field_1687.method_8316(neighbor);
            if (neighborFluid.method_15771()) {
               return true;
            }
         }
      }

      return false;
   }

   private int findBlockSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i <= 8; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7909() instanceof class_1747 && SlotUtils.isGoodForBridging(stack.method_7909())) {
               return i;
            }
         }

         return -1;
      }
   }

   private static enum Phase {
      SCAN,
      DELAY,
      ROTATE,
      PLACE,
      RESTORE;
   }
}
