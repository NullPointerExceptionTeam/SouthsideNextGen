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
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

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

   private void mineFeetBlock(BlockPos blockPos) {
      if (mc.world != null && mc.interactionManager != null) {
         if (mc.player != null) {
            BlockState state = mc.world.getBlockState(blockPos);
            if (!state.isAir() && !(state.getHardness(mc.world, blockPos) < 0.0F)) {
               this.switchToBestTool(state);
               Direction face = this.getClientFacing(blockPos);
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

                  if (this.swing.getValue() && mc.player.handSwingProgress <= 0.0F) {
                     mc.player.swingHand(Hand.MAIN_HAND);
                  }
               }
            } else {
               this.blockBreakHelper.cancel();
               this.restorePreviousSlot();
            }
         }
      }
   }

   private boolean isWithinRange(BlockPos pos) {
      if (mc.player == null) {
         return false;
      } else {
         double distanceSq = mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(pos));
         double max = this.range.getValue().doubleValue();
         return distanceSq <= max * max;
      }
   }

   private boolean hasLineOfSight(BlockPos pos) {
      if (mc.player != null && mc.world != null) {
         Vec3d eye = mc.player.getEyePos();
         Vec3d center = Vec3d.ofCenter(pos);
         HitResult hitResult = mc.world.raycast(new RaycastContext(eye, center, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
         if (hitResult.getType() == Type.MISS) {
            return true;
         } else if (hitResult.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)hitResult;
            return blockHitResult.getBlockPos().equals(pos);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private FeetCity.TargetData findTarget() {
      if (mc.world != null && mc.player != null) {
         PlayerEntity bestPlayer = null;
         BlockPos bestBlock = null;
         double bestDistance = Double.MAX_VALUE;

         for (PlayerEntity other : mc.world.getPlayers()) {
            if (other != mc.player && other != null && other.isAlive() && !other.isSpectator() && TargetUtils.isTarget(other, true, true)) {
               BlockPos feetBlock = this.getFeetBlock(other);
               if (feetBlock != null) {
                  double distance = mc.player.getPos().squaredDistanceTo(Vec3d.ofCenter(feetBlock));
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

   private BlockPos getFeetBlock(PlayerEntity entity) {
      if (mc.world == null) {
         return null;
      } else {
         BlockPos feet = BlockPos.ofFloored(entity.getX(), entity.getY() - 0.2, entity.getZ());
         if (this.isBreakable(feet)) {
            return feet;
         } else {
            BlockPos below = feet.down();
            return this.isBreakable(below) ? below : null;
         }
      }
   }

   private boolean isBreakable(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState state = mc.world.getBlockState(pos);
         return state.isAir() ? false : state.getHardness(mc.world, pos) >= 0.0F;
      }
   }

   private Direction getClientFacing(BlockPos pos) {
      if (mc.player == null) {
         return null;
      } else if (mc.player.raycast(5.0, mc.getRenderTickCounter().getTickDelta(false), false) instanceof BlockHitResult blockHit
         && blockHit.getBlockPos().equals(pos)) {
         return blockHit.getSide();
      } else {
         Vec3d eye = mc.player.getEyePos();
         Vec3d center = Vec3d.ofCenter(pos);
         Vec3d diff = center.subtract(eye);
         return Direction.getFacing(diff.x, diff.y, diff.z).getOpposite();
      }
   }

   private void switchToBestTool(BlockState state) {
      if (this.autoTool.getValue()) {
         if (mc.player != null) {
            int bestSlot = -1;
            float bestSpeed = 1.0F;

            for (int i = 0; i < 9; i++) {
               ItemStack stack = mc.player.getInventory().getStack(i);
               if (!stack.isEmpty()) {
                  float speed = stack.getMiningSpeedMultiplier(state);
                  if (speed > bestSpeed) {
                     bestSpeed = speed;
                     bestSlot = i;
                  }
               }
            }

            if (bestSlot != -1 && mc.player.getInventory().selectedSlot != bestSlot) {
               if (this.previousSlot == -1) {
                  this.previousSlot = mc.player.getInventory().selectedSlot;
               }

               mc.player.getInventory().selectedSlot = bestSlot;
            }
         }
      }
   }

   private void restorePreviousSlot() {
      if (mc.player != null) {
         if (this.previousSlot != -1) {
            mc.player.getInventory().selectedSlot = this.previousSlot;
            this.previousSlot = -1;
         }
      }
   }

   private record TargetData(PlayerEntity player, BlockPos blockPos) {
   }
}
