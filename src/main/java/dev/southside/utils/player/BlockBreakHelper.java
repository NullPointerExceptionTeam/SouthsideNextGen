package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.misc.ClientUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import org.jetbrains.annotations.Nullable;

public final class BlockBreakHelper implements MinecraftAccessor {
   private BlockPos currentPos;
   private Direction currentFace = Direction.UP;
   private float destroyProgress;
   private boolean breaking;
   private long extraDelayMs;
   private long nextAllowedStart;
   private BlockBreakHelper.Callback callback;
   private long predictedCooldownMs;
   private boolean swungThisBreak;

   public void tick() {
      if (this.breaking && !ClientUtils.nullCheck() && this.currentPos != null && this.currentFace != null) {
         BlockState state = mc.world.getBlockState(this.currentPos);
         ClientPlayerEntity player = mc.player;
         if (player == null || mc.interactionManager == null) {
            this.cancel();
         } else if (state.isAir()) {
            this.cancel();
         } else {
            float delta = state.calcBlockBreakingDelta(player, player.clientWorld, this.currentPos);
            if (!(delta <= 0.0F)) {
               player.swingHand(Hand.MAIN_HAND);
               this.swungThisBreak = true;
               this.destroyProgress += delta;
               mc.world.setBlockBreakingInfo(player.getId(), this.currentPos, (int)(this.destroyProgress * 10.0F) - 1);
               if (this.destroyProgress >= 1.0F) {
                  this.finish(state);
               }
            }
         }
      }
   }

   public boolean begin(BlockPos pos, Direction face, long additionalDelayMs, @Nullable BlockBreakHelper.Callback callback) {
      if (!ClientUtils.nullCheck() && mc.interactionManager != null && pos != null && face != null) {
         if (System.currentTimeMillis() < this.nextAllowedStart) {
            return false;
         } else {
            ClientPlayerEntity player = mc.player;
            if (player == null) {
               return false;
            } else {
               BlockState state = mc.world.getBlockState(pos);
               if (state.isAir()) {
                  return false;
               } else if (this.breaking && pos.equals(this.currentPos) && face == this.currentFace) {
                  return true;
               } else {
                  if (this.breaking) {
                     this.cancel();
                  }

                  this.currentPos = pos.toImmutable();
                  this.currentFace = face;
                  this.destroyProgress = 0.0F;
                  this.breaking = true;
                  this.extraDelayMs = Math.max(0L, additionalDelayMs);
                  this.callback = callback;
                  this.predictedCooldownMs = this.computeCooldownMs(player);
                  this.swungThisBreak = false;
                  this.sendPacket(Action.START_DESTROY_BLOCK, this.currentPos, this.currentFace);
                  float delta = state.calcBlockBreakingDelta(player, player.clientWorld, this.currentPos);
                  if (player.getAbilities().creativeMode || delta >= 1.0F) {
                     player.swingHand(Hand.MAIN_HAND);
                     this.swungThisBreak = true;
                     this.destroyProgress = 1.0F;
                     this.finish(state);
                  }

                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public void cancel() {
      if (this.breaking && this.currentPos != null && this.currentFace != null && mc.player != null) {
         if (!this.swungThisBreak && mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
         }

         this.sendPacket(Action.ABORT_DESTROY_BLOCK, this.currentPos, this.currentFace);
         mc.world.setBlockBreakingInfo(mc.player.getId(), this.currentPos, -1);
         this.markCooldown();
         this.resetState();
      } else {
         this.resetState();
      }
   }

   public boolean isBreaking() {
      return this.breaking;
   }

   public boolean isBreaking(BlockPos pos) {
      return this.breaking && this.currentPos != null && this.currentPos.equals(pos);
   }

   public BlockPos getCurrentPos() {
      return this.currentPos;
   }

   private void finish(BlockState state) {
      if (mc.player != null && mc.interactionManager != null && this.currentPos != null && this.currentFace != null) {
         if (!this.swungThisBreak && mc.player != null) {
            mc.player.swingHand(Hand.MAIN_HAND);
            this.swungThisBreak = true;
         }

         this.sendPacket(Action.STOP_DESTROY_BLOCK, this.currentPos, this.currentFace);
         mc.interactionManager.breakBlock(this.currentPos);
         mc.world.setBlockBreakingInfo(mc.player.getId(), this.currentPos, -1);
         this.markCooldown();
         if (this.callback != null) {
            this.callback.onBlockBroken(this.currentPos, state);
         }

         this.resetState();
      } else {
         this.resetState();
      }
   }

   private void markCooldown() {
      long base = this.predictedCooldownMs == 0L ? 0L : this.predictedCooldownMs;
      this.nextAllowedStart = System.currentTimeMillis() + Math.max(this.extraDelayMs, base);
   }

   private void resetState() {
      this.breaking = false;
      this.destroyProgress = 0.0F;
      this.currentPos = null;
      this.currentFace = Direction.UP;
      this.callback = null;
      this.predictedCooldownMs = 0L;
      this.swungThisBreak = false;
   }

   private void sendPacket(Action action, BlockPos pos, Direction face) {
      if (mc.player != null && mc.interactionManager != null && mc.world != null) {
         mc.interactionManager.sendSequencedPacket(mc.world, seq -> new PlayerActionC2SPacket(action, pos, face, seq));
      }
   }

   private long computeCooldownMs(ClientPlayerEntity player) {
      return player.getAbilities().creativeMode ? 50L : 250L;
   }

   public interface Callback {
      void onBlockBroken(BlockPos var1, BlockState var2);
   }
}
