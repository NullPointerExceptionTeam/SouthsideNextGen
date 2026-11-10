package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public final class BlockBreakUtils implements MinecraftAccessor {
   private static BlockPos lastPos;
   private static Direction lastFace;
   private static boolean breaking;
   private static float destroyProgress;

   private BlockBreakUtils() {
   }

   public static void doBreak(BlockHitResult hitResult, boolean immediate) {
      doBreak(hitResult, immediate, false);
   }

   public static void startBreaking(BlockHitResult hitResult) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos pos = hitResult.getBlockPos();
         Direction face = hitResult.getSide();
         BlockState state = mc.world.getBlockState(pos);
         if (!state.isAir() && !(state.getHardness(mc.world, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               stopBreaking();
               lastPos = pos.toImmutable();
               lastFace = face;
               destroyProgress = 0.0F;
            }

            if (!breaking) {
               swingOnce(mc.player);
               sendPacket(Action.START_DESTROY_BLOCK, lastPos, lastFace);
               breaking = true;
            }
         }
      }
   }

   public static void finishBreaking(BlockHitResult hitResult) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos pos = hitResult.getBlockPos();
         Direction face = hitResult.getSide();
         BlockState state = mc.world.getBlockState(pos);
         if (!state.isAir() && !(state.getHardness(mc.world, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               lastPos = pos.toImmutable();
               lastFace = face;
            }

            swingOnce(mc.player);
            sendPacket(Action.STOP_DESTROY_BLOCK, lastPos, lastFace);
            mc.interactionManager.breakBlock(lastPos);
            mc.world.setBlockBreakingInfo(mc.player.getId(), lastPos, -1);
            clearLast();
         } else {
            clearLast();
         }
      }
   }

   public static void doBreak(BlockHitResult hitResult, boolean immediate, boolean ignoredWaitForServerSync) {
      if (hitResult != null && mc.player != null && mc.interactionManager != null && mc.world != null) {
         BlockPos pos = hitResult.getBlockPos();
         Direction face = hitResult.getSide();
         BlockState state = mc.world.getBlockState(pos);
         if (!state.isAir() && !(state.getHardness(mc.world, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               stopBreaking();
               lastPos = pos.toImmutable();
               lastFace = face;
               breaking = false;
               destroyProgress = 0.0F;
            }

            if (!immediate && !mc.player.getAbilities().creativeMode) {
               if (!breaking) {
                  sendPacket(Action.START_DESTROY_BLOCK, lastPos, lastFace);
                  breaking = true;
               }

               float delta = state.calcBlockBreakingDelta(mc.player, mc.player.clientWorld, lastPos);
               if (!(delta <= 0.0F)) {
                  swingOnce(mc.player);
                  destroyProgress += delta;
                  mc.world.setBlockBreakingInfo(mc.player.getId(), lastPos, Math.max(0, (int)(destroyProgress * 10.0F) - 1));
                  if (destroyProgress >= 1.0F) {
                     sendPacket(Action.STOP_DESTROY_BLOCK, lastPos, lastFace);
                     mc.interactionManager.breakBlock(lastPos);
                     mc.world.setBlockBreakingInfo(mc.player.getId(), lastPos, -1);
                     clearLast();
                  }
               }
            } else {
               swingOnce(mc.player);
               sendPacket(Action.START_DESTROY_BLOCK, pos, face);
               sendPacket(Action.STOP_DESTROY_BLOCK, pos, face);
               mc.interactionManager.breakBlock(pos);
               mc.world.setBlockBreakingInfo(mc.player.getId(), pos, -1);
               clearLast();
            }
         }
      }
   }

   public static void stopBreaking() {
      if (mc.player != null && mc.interactionManager != null && mc.world != null) {
         if (breaking && lastPos != null && !mc.world.getBlockState(lastPos).isAir()) {
            sendPacket(Action.ABORT_DESTROY_BLOCK, lastPos, lastFace == null ? Direction.UP : lastFace);
            mc.world.setBlockBreakingInfo(mc.player.getId(), lastPos, -1);
         }

         clearLast();
      } else {
         clearLast();
      }
   }

   private static void clearLast() {
      lastPos = null;
      lastFace = null;
      breaking = false;
      destroyProgress = 0.0F;
   }

   private static void sendPacket(Action action, BlockPos pos, Direction face) {
      if (mc.interactionManager != null && mc.world != null) {
         mc.interactionManager.sendSequencedPacket(mc.world, seq -> new PlayerActionC2SPacket(action, pos, face, seq));
      }
   }

   private static void swingOnce(ClientPlayerEntity player) {
      player.swingHand(Hand.MAIN_HAND);
   }
}
