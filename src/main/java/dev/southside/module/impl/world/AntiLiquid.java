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
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;

public class AntiLiquid extends Module {
   private final Slider range = new Slider("Range", 4.0, 3.0, 5.0, 0.5);
   private final Switch water = new Switch("Water", true);
   private final Switch lava = new Switch("Lava", true);
   private static final int RETRY_COOLDOWN = 15;
   private final Map<BlockPos, Integer> attempted = new HashMap<>();
   private static final Direction[] PLACE_DIRECTIONS = new Direction[]{
      Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
   };
   private AntiLiquid.Phase phase = AntiLiquid.Phase.SCAN;
   private BlockPos targetFluidPos;
   private BlockPos supportPos;
   private Direction placeFace;
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
      if (this.savedSlot != -1 && mc.player != null) {
         mc.player.getInventory().selectedSlot = this.savedSlot;
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
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
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
                  mc.player.getInventory().selectedSlot = this.savedSlot;
                  this.savedSlot = -1;
               }

               this.phase = AntiLiquid.Phase.SCAN;
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
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
               BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
               if (hit == null) {
                  this.attempted.put(this.targetFluidPos, 15);
                  this.phase = AntiLiquid.Phase.RESTORE;
               } else {
                  ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                  if (result.isAccepted()) {
                     mc.player.swingHand(Hand.MAIN_HAND);
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
      BlockPos playerPos = mc.player.getBlockPos();
      int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
      Vec3d eyePos = mc.player.getEyePos();

      for (int x = -rangeInt; x <= rangeInt; x++) {
         for (int y = -rangeInt; y <= rangeInt; y++) {
            for (int z = -rangeInt; z <= rangeInt; z++) {
               BlockPos pos = playerPos.add(x, y, z);
               if (!this.attempted.containsKey(pos)) {
                  double distance = eyePos.distanceTo(Vec3d.ofCenter(pos));
                  if (!(distance > this.range.getValue().floatValue())) {
                     FluidState fluidState = mc.world.getFluidState(pos);
                     if (fluidState.isStill()) {
                        boolean isWater = mc.world.getBlockState(pos).getBlock() == Blocks.WATER;
                        boolean isLava = mc.world.getBlockState(pos).getBlock() == Blocks.LAVA;
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
               this.savedSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = blockSlot;
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

   private boolean findSupport(BlockPos fluidPos) {
      for (Direction dir : PLACE_DIRECTIONS) {
         BlockPos neighbor = fluidPos.offset(dir);
         BlockState state = mc.world.getBlockState(neighbor);
         if (!state.isAir() && !state.isLiquid() && !state.isReplaceable()) {
            this.supportPos = neighbor;
            this.placeFace = dir.getOpposite();
            return true;
         }
      }

      return false;
   }

   private boolean canSeeFluid(BlockPos pos, Vec3d eyePos) {
      Vec3d blockCenter = Vec3d.ofCenter(pos);
      Rotation rotation = RotationUtils.calculate(blockCenter);
      double fluidDist = eyePos.squaredDistanceTo(blockCenter);
      BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
      return hit == null ? true : eyePos.squaredDistanceTo(hit.getPos()) >= fluidDist;
   }

   private boolean isAutoBucketWater() {
      AutoBucket autoBucket = Southside.get().getModuleManager().get(AutoBucket.class);
      return !autoBucket.isEnabled() ? false : autoBucket.shouldReceive || autoBucket.interactRequired;
   }

   private boolean hasAdjacentSource(BlockPos pos) {
      for (Direction dir : Direction.values()) {
         if (dir != Direction.UP && dir != Direction.DOWN) {
            BlockPos neighbor = pos.offset(dir);
            FluidState neighborFluid = mc.world.getFluidState(neighbor);
            if (neighborFluid.isStill()) {
               return true;
            }
         }
      }

      return false;
   }

   private int findBlockSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && SlotUtils.isGoodForBridging(stack.getItem())) {
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
