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
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class AntiWeb extends Module {
   private final MinecraftClient mc = MinecraftClient.getInstance();
   public Slider recycleTick = new Slider("Recycle Tick", 10, 5, 20, 1);
   private AntiWeb.Phase phase = AntiWeb.Phase.IDLE;
   private BlockPos waterPos;
   private BlockPos pickupPos;
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
         if (this.mc.player != null && this.mc.world != null && this.mc.interactionManager != null) {
            switch (this.phase) {
               case IDLE:
                  BlockPos webPos = this.findLowestWebPos();
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
      if (this.mc.player != null) {
         Vec3d aim;
         if (this.phase == AntiWeb.Phase.POUR) {
            aim = this.waterPos.toCenterPos();
         } else {
            if (this.phase != AntiWeb.Phase.PICKUP) {
               return;
            }

            if (this.waitingRetry || !this.snapRequested) {
               return;
            }

            BlockPos source = this.pickupPos != null ? this.pickupPos : this.waterPos;
            aim = source.toCenterPos();
         }

         Rotation rot = RotationUtils.calculate(this.mc.player.getEyePos(), aim);
         RotationUtils.setRotation(rot, this.rotationPriority(), MoveFixMode.Silent);
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.mc.player != null) {
         if (RotationUtils.accepted(this.rotationPriority())) {
            if (this.phase == AntiWeb.Phase.POUR && this.isRotationOnTarget(this.waterPos.toCenterPos())) {
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

               BlockPos source = this.pickupPos != null ? this.pickupPos : this.waterPos;
               if (this.mc.world.getBlockState(this.mc.player.getBlockPos()).getBlock() != Blocks.WATER
                  && !this.isRotationOnTarget(source.toCenterPos())) {
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

   private boolean isRotationOnTarget(Vec3d aimPoint) {
      if (RotationUtils.getRotation() == null) {
         return false;
      } else {
         Rotation current = RotationUtils.getRotationSafely();
         Rotation wanted = RotationUtils.calculate(this.mc.player.getEyePos(), aimPoint);
         return Math.abs(RotationUtils.getAngleDifference(current.yaw, wanted.yaw)) < 12.0F
            && Math.abs(RotationUtils.getAngleDifference(current.pitch, wanted.pitch)) < 12.0F;
      }
   }

   private boolean isEyeInCobweb() {
      return this.mc.world.getBlockState(BlockPos.ofFloored(this.mc.player.getEyePos())).getBlock() == Blocks.COBWEB;
   }

   private boolean pourWater() {
      int waterBucketSlot = this.findWaterBucket();
      if (waterBucketSlot == -1) {
         return false;
      } else {
         int previousSlot = this.mc.player.getInventory().selectedSlot;
         this.mc.player.getInventory().selectedSlot = waterBucketSlot;
         ActionResult result = this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
         this.mc.player.getInventory().selectedSlot = previousSlot;
         return result.isAccepted();
      }
   }

   private boolean pickupWater() {
      int emptyBucketSlot = this.findEmptyBucket();
      if (emptyBucketSlot == -1) {
         return false;
      } else {
         int previousSlot = this.mc.player.getInventory().selectedSlot;
         this.mc.player.getInventory().selectedSlot = emptyBucketSlot;
         this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
         this.mc.player.getInventory().selectedSlot = previousSlot;
         return this.mc.player.getInventory().getStack(emptyBucketSlot).isOf(Items.WATER_BUCKET);
      }
   }

   private BlockPos findWaterSource() {
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (int x = -2; x <= 2; x++) {
         for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
               BlockPos pos = this.waterPos.add(x, y, z);
               if (this.mc.world.getBlockState(pos).getBlock() == Blocks.WATER) {
                  double dist = this.mc.player.getBlockPos().getSquaredDistance(pos);
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

   private BlockPos findLowestWebPos() {
      Box box = this.mc.player.getBoundingBox();
      BlockPos best = null;

      for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); x++) {
         for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); z++) {
            for (int y = (int)Math.floor(box.minY); y <= (int)Math.floor(box.maxY); y++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (this.mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB && (best == null || y < best.getY())) {
                  best = pos;
               }
            }
         }
      }

      return best;
   }

   private int findWaterBucket() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (stack.getItem() == Items.WATER_BUCKET) {
            return i;
         }
      }

      return -1;
   }

   private int findEmptyBucket() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (stack.getItem() == Items.BUCKET) {
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
