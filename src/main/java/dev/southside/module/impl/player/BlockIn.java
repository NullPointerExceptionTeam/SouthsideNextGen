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
import net.minecraft.util.Hand;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;

public class BlockIn extends Module {
   public Slider delay = new Slider("Delay", 50.0, 0.0, 500.0, 10.0);
   public Switch autoCenter = new Switch("AutoCenter", false);
   private static final double CENTER_THRESHOLD = 0.15;
   private static final List<Direction> CARDINAL = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
   private static final Direction[] PLACE_FACES = new Direction[]{
      Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
   };
   private static final int MAX_PLAN_ATTEMPTS = 6;
   private final MSTimer timer = new MSTimer();
   private BlockIn.Phase phase = BlockIn.Phase.DONE;
   private BlockPos basePos;
   private BlockPos headTarget;
   private Direction tallDirection;
   private final List<BlockPos> columnTargets = new ArrayList<>();
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
      if (mc.player != null && mc.world != null) {
         if (mc.currentScreen == null) {
            if (this.phase != BlockIn.Phase.DONE) {
               if (this.basePos == null) {
                  this.basePos = mc.player.getBlockPos();
                  this.headTarget = this.basePos.up(2);
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
         if (mc.player != null && mc.world != null && mc.interactionManager != null) {
            if (this.findBlockSlot() == -1) {
               this.finish();
            } else {
               Rotation rotation = RotationUtils.getRotation();
               if (rotation != null) {
                  ClientRayTraceUtil.updateEyePos();
                  if (!ClientRayTraceUtil.didHitBlockFace(mc.player, rotation.yaw, rotation.pitch, this.pendingPlan.placeOn, this.pendingPlan.face, true)) {
                     this.planAttempts++;
                     if (this.planAttempts >= 6) {
                        this.failCurrentPlan();
                     }
                  } else {
                     BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
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
                           int previousSlot = mc.player.getInventory().selectedSlot;
                           boolean success = false;
                           BlockPos placedTarget = this.pendingPlan.target;
                           mc.player.getInventory().selectedSlot = slot;

                           try {
                              success = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit).isAccepted();
                              if (success) {
                                 mc.player.swingHand(Hand.MAIN_HAND);
                              }
                           } finally {
                              mc.player.getInventory().selectedSlot = previousSlot;
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
      if (mc.player != null) {
         this.basePos = mc.player.getBlockPos();
         this.headTarget = this.basePos.up(2);
         this.columnTargets.clear();
         this.prepareColumnTargets();
         this.tallDirection = null;
         this.pendingPlan = null;
         this.planAttempts = 0;
         this.jumpIssued = false;
         this.centering = false;
         this.timer.reset();
         this.headAlreadyExists = mc.world != null && !this.canPlaceAt(this.headTarget);
         if (this.autoCenter.getValue()) {
            this.phase = BlockIn.Phase.CENTER;
            this.centering = true;
         } else {
            this.phase = BlockIn.Phase.PLACE_COLUMNS;
         }
      }
   }

   private void handleCenter() {
      if (mc.player != null) {
         double centerX = this.basePos.getX() + 0.5;
         double centerZ = this.basePos.getZ() + 0.5;
         double dx = centerX - mc.player.getX();
         double dz = centerZ - mc.player.getZ();
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist <= 0.15) {
            this.centering = false;
            this.advancePhase(BlockIn.Phase.PLACE_COLUMNS);
         } else {
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d target = new Vec3d(centerX, eyePos.y, centerZ);
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
            BlockPos target = this.columnTargets.get(0);
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
      if (!this.jumpIssued && mc.player.isOnGround()) {
         mc.options.jumpKey.setPressed(true);
         this.jumpIssued = true;
         this.jumpKeyHeld = true;
         this.timer.reset();
      } else {
         if (this.jumpKeyHeld && (!mc.player.isOnGround() || this.timer.hasTimePassed(150L))) {
            mc.options.jumpKey.setPressed(false);
            this.jumpKeyHeld = false;
         }

         if (this.jumpIssued && !mc.player.isOnGround()) {
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

         BlockPos target = this.basePos.offset(this.tallDirection).up(2);
         if (!this.canPlaceAt(target)) {
            this.advancePhase(BlockIn.Phase.PLACE_HEAD);
         } else {
            BlockPos placeOn = this.basePos.offset(this.tallDirection).up(1);
            BlockIn.PlacePlan plan = this.buildPlan(target, placeOn, Direction.UP);
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
         this.headTarget = this.basePos.up(2);
      }

      if (!this.canPlaceAt(this.headTarget)) {
         this.finish();
      } else if (mc.player.isOnGround() && this.jumpIssued) {
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

            BlockPos placeOn = this.basePos.offset(this.tallDirection).up(2);
            Direction face = this.tallDirection.getOpposite();
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
      BlockPos target = this.pendingPlan != null ? this.pendingPlan.target : null;
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

   private void onPlaced(BlockPos target) {
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
         mc.options.jumpKey.setPressed(false);
         this.jumpKeyHeld = false;
      }

      this.setEnable(false);
   }

   private void prepareColumnTargets() {
      this.columnTargets.clear();
      Direction facing = mc.player.getHorizontalFacing();
      this.columnTargets.add(this.basePos.offset(facing));

      for (Direction dir : CARDINAL) {
         if (dir != facing) {
            this.columnTargets.add(this.basePos.offset(dir));
         }
      }

      this.columnTargets.add(this.basePos.offset(facing).up());

      for (Direction dirx : CARDINAL) {
         if (dirx != facing) {
            this.columnTargets.add(this.basePos.offset(dirx).up());
         }
      }
   }

   private Direction chooseTallDirection() {
      if (mc.world != null && mc.player != null) {
         Direction facing = mc.player.getHorizontalFacing();
         if (this.hasTwoHighColumn(facing)) {
            return facing;
         } else {
            for (Direction dir : CARDINAL) {
               if (dir != facing && this.hasTwoHighColumn(dir)) {
                  return dir;
               }
            }

            if (this.hasBaseBlock(facing)) {
               return facing;
            } else {
               for (Direction dirx : CARDINAL) {
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

   private boolean hasTwoHighColumn(Direction dir) {
      if (mc.world == null) {
         return false;
      } else {
         BlockPos base = this.basePos.offset(dir);
         return !mc.world.getBlockState(base).isAir() && !mc.world.getBlockState(base.up()).isAir();
      }
   }

   private boolean hasBaseBlock(Direction dir) {
      if (mc.world == null) {
         return false;
      } else {
         BlockPos base = this.basePos.offset(dir);
         return !mc.world.getBlockState(base).isAir();
      }
   }

   private BlockIn.PlacePlan buildPlan(BlockPos target, BlockPos preferredPlaceOn, Direction preferredFace) {
      if (mc.player != null && mc.world != null) {
         if (!this.canPlaceAt(target)) {
            return null;
         } else {
            if (preferredPlaceOn != null && preferredFace != null && this.isClickable(preferredPlaceOn)) {
               Rotation rotation = RotationUtils.getRotationToBlock(preferredPlaceOn, preferredFace);
               if (rotation != null) {
                  return new BlockIn.PlacePlan(target, preferredPlaceOn, preferredFace, rotation);
               }
            }

            for (Direction face : PLACE_FACES) {
               BlockPos placeOn = target.offset(face.getOpposite());
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

   private boolean canPlaceAt(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState state = mc.world.getBlockState(pos);
         return state.isAir() || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA;
      }
   }

   private boolean isClickable(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState state = mc.world.getBlockState(pos);
         if (state.isAir()) {
            return false;
         } else {
            Block block = state.getBlock();
            return block != Blocks.WATER && block != Blocks.LAVA;
         }
      }
   }

   private int findBlockSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isValidBlock(stack)) {
               return i;
            }
         }

         return -1;
      }
   }

   private boolean isValidBlock(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (stack.getItem() instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         return block != Blocks.TNT
            && block != Blocks.SAND
            && block != Blocks.GRAVEL
            && block != Blocks.WATER
            && block != Blocks.LAVA
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
      if (mc.player != null && this.phase != BlockIn.Phase.DONE) {
         if (this.centering) {
            event.setForward(false);
            event.setBack(false);
            event.setLeft(false);
            event.setRight(false);
            event.setJump(false);
            event.setSneak(false);
            event.setSprint(false);
            double centerX = this.basePos.getX() + 0.5;
            double centerZ = this.basePos.getZ() + 0.5;
            double dx = centerX - mc.player.getX();
            double dz = centerZ - mc.player.getZ();
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
         mc.options.jumpKey.setPressed(false);
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
      private final BlockPos target;
      private final BlockPos placeOn;
      private final Direction face;
      private final Rotation rotation;

      private PlacePlan(BlockPos target, BlockPos placeOn, Direction face, Rotation rotation) {
         this.target = target;
         this.placeOn = placeOn;
         this.face = face;
         this.rotation = rotation;
      }
   }
}
