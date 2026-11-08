package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

public class BlockIn extends Module {
   public Slider delay = new Slider("Delay", 50.0, 0.0, 500.0, 10.0);
   public Switch autoCenter = new Switch("AutoCenter", false);
   private static final double CENTER_THRESHOLD = 0.15;
   private static final List<class_2350> CARDINAL = List.of(class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039);
   private static final class_2350[] PLACE_FACES = new class_2350[]{
      class_2350.field_11036, class_2350.field_11043, class_2350.field_11035, class_2350.field_11034, class_2350.field_11039
   };
   private static final int MAX_PLAN_ATTEMPTS = 6;
   private final MSTimer timer = new MSTimer();
   private BlockIn.Phase phase = BlockIn.Phase.DONE;
   private class_2338 basePos;
   private class_2338 headTarget;
   private class_2350 tallDirection;
   private final List<class_2338> columnTargets = new ArrayList<>();
   private BlockIn.PlacePlan pendingPlan;
   private int planAttempts;
   private boolean jumpIssued;
   private boolean jumpKeyHeld;
   private boolean headAlreadyExists;
   private boolean centering;

   public BlockIn() {
      super(Category.Player, "BlockIn");
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 == null) {
            if (this.phase != BlockIn.Phase.DONE) {
               if (this.basePos == null) {
                  this.basePos = mc.field_1724.method_24515();
                  this.headTarget = this.basePos.method_10086(2);
               }

               switch (this.phase) {
                  case CENTER:
                     this.handleCenter();
                     break;
                  case PLACE_COLUMNS:
                     this.handlePlaceColumns();
                     break;
                  case JUMP:
                     this.handleJump();
                     break;
                  case PLACE_THIRD:
                     this.handlePlaceThird();
                     break;
                  case PLACE_HEAD:
                     this.handlePlaceHead();
                  case DONE:
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.pendingPlan != null) {
         if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
            if (this.findBlockSlot() == -1) {
               this.finish();
            } else {
               Rotation rotation = RotationUtils.getRotation();
               if (rotation != null) {
                  ClientRayTraceUtil.updateEyePos();
                  if (!ClientRayTraceUtil.didHitBlockFace(mc.field_1724, rotation.yaw, rotation.pitch, this.pendingPlan.placeOn, this.pendingPlan.face, true)) {
                     this.planAttempts++;
                     if (this.planAttempts >= 6) {
                        this.failCurrentPlan();
                     }
                  } else {
                     class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
                     if (hit == null) {
                        this.planAttempts++;
                        if (this.planAttempts >= 6) {
                           this.failCurrentPlan();
                        }
                     } else {
                        int slot = this.findBlockSlot();
                        if (slot == -1) {
                           this.finish();
                        } else {
                           int previousSlot = mc.field_1724.method_31548().field_7545;
                           boolean success = false;
                           class_2338 placedTarget = this.pendingPlan.target;
                           mc.field_1724.method_31548().field_7545 = slot;

                           try {
                              success = mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit).method_23665();
                              if (success) {
                                 mc.field_1724.method_6104(class_1268.field_5808);
                              }
                           } finally {
                              mc.field_1724.method_31548().field_7545 = previousSlot;
                           }

                           if (success) {
                              this.pendingPlan = null;
                              this.planAttempts = 0;
                              this.timer.reset();
                              this.onPlaced(placedTarget);
                           } else {
                              this.planAttempts++;
                              if (this.planAttempts >= 6) {
                                 this.failCurrentPlan();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void startSequence() {
      if (mc.field_1724 != null) {
         this.basePos = mc.field_1724.method_24515();
         this.headTarget = this.basePos.method_10086(2);
         this.columnTargets.clear();
         this.prepareColumnTargets();
         this.tallDirection = null;
         this.pendingPlan = null;
         this.planAttempts = 0;
         this.jumpIssued = false;
         this.centering = false;
         this.timer.reset();
         this.headAlreadyExists = mc.field_1687 != null && !this.canPlaceAt(this.headTarget);
         if (this.autoCenter.getValue()) {
            this.phase = BlockIn.Phase.CENTER;
            this.centering = true;
         } else {
            this.phase = BlockIn.Phase.PLACE_COLUMNS;
         }
      }
   }

   private void handleCenter() {
      if (mc.field_1724 != null) {
         double centerX = this.basePos.method_10263() + 0.5;
         double centerZ = this.basePos.method_10260() + 0.5;
         double dx = centerX - mc.field_1724.method_23317();
         double dz = centerZ - mc.field_1724.method_23321();
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist <= 0.15) {
            this.centering = false;
            this.advancePhase(BlockIn.Phase.PLACE_COLUMNS);
         } else {
            class_243 eyePos = mc.field_1724.method_33571();
            class_243 target = new class_243(centerX, eyePos.field_1351, centerZ);
            Rotation rot = RotationUtils.getRotationTo(eyePos, target);
            if (rot != null) {
               RotationUtils.setRotation(rot, 0, MoveFixMode.Strict);
            }
         }
      }
   }

   private void handlePlaceColumns() {
      if (this.findBlockSlot() == -1) {
         this.finish();
      } else if (this.pendingPlan != null) {
         RotationUtils.setRotation(this.pendingPlan.rotation);
      } else if (this.timer.hasTimePassed(this.delay.getValue().longValue())) {
         while (!this.columnTargets.isEmpty()) {
            class_2338 target = this.columnTargets.get(0);
            if (!this.canPlaceAt(target)) {
               this.columnTargets.remove(0);
            } else {
               BlockIn.PlacePlan plan = this.buildPlan(target, null, null);
               if (plan != null) {
                  this.queuePlan(plan);
                  return;
               }

               this.columnTargets.remove(0);
            }
         }

         this.advancePhase(this.headAlreadyExists ? BlockIn.Phase.DONE : BlockIn.Phase.JUMP);
      }
   }

   private void handleJump() {
      if (!this.jumpIssued && mc.field_1724.method_24828()) {
         mc.field_1690.field_1903.method_23481(true);
         this.jumpIssued = true;
         this.jumpKeyHeld = true;
         this.timer.reset();
      } else {
         if (this.jumpKeyHeld && (!mc.field_1724.method_24828() || this.timer.hasTimePassed(150L))) {
            mc.field_1690.field_1903.method_23481(false);
            this.jumpKeyHeld = false;
         }

         if (this.jumpIssued && !mc.field_1724.method_24828()) {
            this.advancePhase(BlockIn.Phase.PLACE_THIRD);
         }
      }
   }

   private void handlePlaceThird() {
      if (this.findBlockSlot() == -1) {
         this.finish();
      } else if (this.pendingPlan != null) {
         RotationUtils.setRotation(this.pendingPlan.rotation);
      } else if (this.timer.hasTimePassed(this.delay.getValue().longValue())) {
         if (this.tallDirection == null) {
            this.tallDirection = this.chooseTallDirection();
            if (this.tallDirection == null) {
               this.finish();
               return;
            }
         }

         class_2338 target = this.basePos.method_10093(this.tallDirection).method_10086(2);
         if (!this.canPlaceAt(target)) {
            this.advancePhase(BlockIn.Phase.PLACE_HEAD);
         } else {
            class_2338 placeOn = this.basePos.method_10093(this.tallDirection).method_10086(1);
            BlockIn.PlacePlan plan = this.buildPlan(target, placeOn, class_2350.field_11036);
            if (plan == null) {
               plan = this.buildPlan(target, null, null);
            }

            if (plan == null) {
               this.advancePhase(BlockIn.Phase.PLACE_HEAD);
            } else {
               this.queuePlan(plan);
            }
         }
      }
   }

   private void handlePlaceHead() {
      if (this.headTarget == null) {
         this.headTarget = this.basePos.method_10086(2);
      }

      if (!this.canPlaceAt(this.headTarget)) {
         this.finish();
      } else if (mc.field_1724.method_24828() && this.jumpIssued) {
         if (this.findBlockSlot() == -1) {
            this.finish();
         } else if (this.pendingPlan != null) {
            RotationUtils.setRotation(this.pendingPlan.rotation);
         } else if (this.timer.hasTimePassed(this.delay.getValue().longValue())) {
            if (this.tallDirection == null) {
               this.tallDirection = this.chooseTallDirection();
               if (this.tallDirection == null) {
                  this.finish();
                  return;
               }
            }

            class_2338 placeOn = this.basePos.method_10093(this.tallDirection).method_10086(2);
            class_2350 face = this.tallDirection.method_10153();
            BlockIn.PlacePlan plan = this.buildPlan(this.headTarget, placeOn, face);
            if (plan == null) {
               this.finish();
            } else {
               this.queuePlan(plan);
            }
         }
      }
   }

   private void advancePhase(BlockIn.Phase next) {
      this.pendingPlan = null;
      this.planAttempts = 0;
      this.timer.reset();
      if (next == BlockIn.Phase.DONE) {
         this.finish();
      } else {
         this.phase = next;
      }
   }

   private void queuePlan(BlockIn.PlacePlan plan) {
      this.pendingPlan = plan;
      this.planAttempts = 0;
      RotationUtils.setRotation(plan.rotation);
   }

   private void failCurrentPlan() {
      class_2338 target = this.pendingPlan != null ? this.pendingPlan.target : null;
      this.pendingPlan = null;
      this.planAttempts = 0;
      this.timer.reset();
      if (this.phase == BlockIn.Phase.PLACE_COLUMNS && target != null) {
         this.columnTargets.remove(target);
         if (this.columnTargets.isEmpty()) {
            this.advancePhase(this.headAlreadyExists ? BlockIn.Phase.DONE : BlockIn.Phase.JUMP);
         }
      } else if (this.phase == BlockIn.Phase.PLACE_THIRD) {
         this.advancePhase(BlockIn.Phase.PLACE_HEAD);
      } else {
         if (this.phase == BlockIn.Phase.PLACE_HEAD) {
            this.finish();
         }
      }
   }

   private void onPlaced(class_2338 target) {
      if (this.phase == BlockIn.Phase.PLACE_COLUMNS) {
         this.columnTargets.remove(target);
         if (this.columnTargets.isEmpty()) {
            this.advancePhase(this.headAlreadyExists ? BlockIn.Phase.DONE : BlockIn.Phase.JUMP);
         }
      } else if (this.phase == BlockIn.Phase.PLACE_THIRD) {
         this.advancePhase(BlockIn.Phase.PLACE_HEAD);
      } else {
         if (this.phase == BlockIn.Phase.PLACE_HEAD) {
            this.finish();
         }
      }
   }

   private void finish() {
      this.phase = BlockIn.Phase.DONE;
      this.pendingPlan = null;
      this.planAttempts = 0;
      this.timer.reset();
      if (this.jumpKeyHeld) {
         mc.field_1690.field_1903.method_23481(false);
         this.jumpKeyHeld = false;
      }

      this.setEnable(false);
   }

   private void prepareColumnTargets() {
      this.columnTargets.clear();
      class_2350 facing = mc.field_1724.method_5735();
      this.columnTargets.add(this.basePos.method_10093(facing));

      for (class_2350 dir : CARDINAL) {
         if (dir != facing) {
            this.columnTargets.add(this.basePos.method_10093(dir));
         }
      }

      this.columnTargets.add(this.basePos.method_10093(facing).method_10084());

      for (class_2350 dirx : CARDINAL) {
         if (dirx != facing) {
            this.columnTargets.add(this.basePos.method_10093(dirx).method_10084());
         }
      }
   }

   private class_2350 chooseTallDirection() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_2350 facing = mc.field_1724.method_5735();
         if (this.hasTwoHighColumn(facing)) {
            return facing;
         } else {
            for (class_2350 dir : CARDINAL) {
               if (dir != facing && this.hasTwoHighColumn(dir)) {
                  return dir;
               }
            }

            if (this.hasBaseBlock(facing)) {
               return facing;
            } else {
               for (class_2350 dirx : CARDINAL) {
                  if (dirx != facing && this.hasBaseBlock(dirx)) {
                     return dirx;
                  }
               }

               return facing;
            }
         }
      } else {
         return null;
      }
   }

   private boolean hasTwoHighColumn(class_2350 dir) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2338 base = this.basePos.method_10093(dir);
         return !mc.field_1687.method_8320(base).method_26215() && !mc.field_1687.method_8320(base.method_10084()).method_26215();
      }
   }

   private boolean hasBaseBlock(class_2350 dir) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2338 base = this.basePos.method_10093(dir);
         return !mc.field_1687.method_8320(base).method_26215();
      }
   }

   private BlockIn.PlacePlan buildPlan(class_2338 target, class_2338 preferredPlaceOn, class_2350 preferredFace) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.canPlaceAt(target)) {
            return null;
         } else {
            if (preferredPlaceOn != null && preferredFace != null && this.isClickable(preferredPlaceOn)) {
               Rotation rotation = RotationUtils.getRotationToBlock(preferredPlaceOn, preferredFace);
               if (rotation != null) {
                  return new BlockIn.PlacePlan(target, preferredPlaceOn, preferredFace, rotation);
               }
            }

            for (class_2350 face : PLACE_FACES) {
               class_2338 placeOn = target.method_10093(face.method_10153());
               if (this.isClickable(placeOn)) {
                  Rotation rotation = RotationUtils.getRotationToBlock(placeOn, face);
                  if (rotation != null) {
                     return new BlockIn.PlacePlan(target, placeOn, face, rotation);
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private boolean canPlaceAt(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 state = mc.field_1687.method_8320(pos);
         return state.method_26215() || state.method_26204() == class_2246.field_10382 || state.method_26204() == class_2246.field_10164;
      }
   }

   private boolean isClickable(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 state = mc.field_1687.method_8320(pos);
         if (state.method_26215()) {
            return false;
         } else {
            class_2248 block = state.method_26204();
            return block != class_2246.field_10382 && block != class_2246.field_10164;
         }
      }
   }

   private int findBlockSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (this.isValidBlock(stack)) {
               return i;
            }
         }

         return -1;
      }
   }

   private boolean isValidBlock(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else if (stack.method_7909() instanceof class_1747 blockItem) {
         class_2248 block = blockItem.method_7711();
         return block != class_2246.field_10375
            && block != class_2246.field_10102
            && block != class_2246.field_10255
            && block != class_2246.field_10382
            && block != class_2246.field_10164
            && !block.toString().toLowerCase().contains("door")
            && !block.toString().toLowerCase().contains("trapdoor");
      } else {
         return false;
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.startSequence();
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
      if (mc.field_1724 != null && this.phase != BlockIn.Phase.DONE) {
         if (this.centering) {
            event.setForward(false);
            event.setBack(false);
            event.setLeft(false);
            event.setRight(false);
            event.setJump(false);
            event.setSneak(false);
            event.setSprint(false);
            double centerX = this.basePos.method_10263() + 0.5;
            double centerZ = this.basePos.method_10260() + 0.5;
            double dx = centerX - mc.field_1724.method_23317();
            double dz = centerZ - mc.field_1724.method_23321();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.15) {
               event.setForward(true);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.phase = BlockIn.Phase.DONE;
      this.basePos = null;
      this.headTarget = null;
      this.tallDirection = null;
      this.columnTargets.clear();
      this.pendingPlan = null;
      this.planAttempts = 0;
      this.jumpIssued = false;
      if (this.jumpKeyHeld) {
         mc.field_1690.field_1903.method_23481(false);
      }

      this.jumpKeyHeld = false;
      this.headAlreadyExists = false;
      this.centering = false;
      this.timer.reset();
   }

   private static enum Phase {
      CENTER,
      PLACE_COLUMNS,
      JUMP,
      PLACE_THIRD,
      PLACE_HEAD,
      DONE;
   }

   private static class PlacePlan {
      private final class_2338 target;
      private final class_2338 placeOn;
      private final class_2350 face;
      private final Rotation rotation;

      private PlacePlan(class_2338 target, class_2338 placeOn, class_2350 face, Rotation rotation) {
         this.target = target;
         this.placeOn = placeOn;
         this.face = face;
         this.rotation = rotation;
      }
   }
}
