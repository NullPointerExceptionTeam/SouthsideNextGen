package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.misc.ClientUtils;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;
import org.jetbrains.annotations.Nullable;

public final class BlockBreakHelper implements MinecraftAccessor {
   private class_2338 currentPos;
   private class_2350 currentFace = class_2350.field_11036;
   private float destroyProgress;
   private boolean breaking;
   private long extraDelayMs;
   private long nextAllowedStart;
   private BlockBreakHelper.Callback callback;
   private long predictedCooldownMs;
   private boolean swungThisBreak;

   public void tick() {
      if (this.breaking && !ClientUtils.nullCheck() && this.currentPos != null && this.currentFace != null) {
         class_2680 state = mc.field_1687.method_8320(this.currentPos);
         class_746 player = mc.field_1724;
         if (player == null || mc.field_1761 == null) {
            this.cancel();
         } else if (state.method_26215()) {
            this.cancel();
         } else {
            float delta = state.method_26165(player, player.field_17892, this.currentPos);
            if (!(delta <= 0.0F)) {
               player.method_6104(class_1268.field_5808);
               this.swungThisBreak = true;
               this.destroyProgress += delta;
               mc.field_1687.method_8517(player.method_5628(), this.currentPos, (int)(this.destroyProgress * 10.0F) - 1);
               if (this.destroyProgress >= 1.0F) {
                  this.finish(state);
               }
            }
         }
      }
   }

   public boolean begin(class_2338 pos, class_2350 face, long additionalDelayMs, @Nullable BlockBreakHelper.Callback callback) {
      if (!ClientUtils.nullCheck() && mc.field_1761 != null && pos != null && face != null) {
         if (System.currentTimeMillis() < this.nextAllowedStart) {
            return false;
         } else {
            class_746 player = mc.field_1724;
            if (player == null) {
               return false;
            } else {
               class_2680 state = mc.field_1687.method_8320(pos);
               if (state.method_26215()) {
                  return false;
               } else if (this.breaking && pos.equals(this.currentPos) && face == this.currentFace) {
                  return true;
               } else {
                  if (this.breaking) {
                     this.cancel();
                  }

                  this.currentPos = pos.method_10062();
                  this.currentFace = face;
                  this.destroyProgress = 0.0F;
                  this.breaking = true;
                  this.extraDelayMs = Math.max(0L, additionalDelayMs);
                  this.callback = callback;
                  this.predictedCooldownMs = this.computeCooldownMs(player);
                  this.swungThisBreak = false;
                  this.sendPacket(class_2847.field_12968, this.currentPos, this.currentFace);
                  float delta = state.method_26165(player, player.field_17892, this.currentPos);
                  if (player.method_31549().field_7477 || delta >= 1.0F) {
                     player.method_6104(class_1268.field_5808);
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
      if (this.breaking && this.currentPos != null && this.currentFace != null && mc.field_1724 != null) {
         if (!this.swungThisBreak && mc.field_1724 != null) {
            mc.field_1724.method_6104(class_1268.field_5808);
         }

         this.sendPacket(class_2847.field_12971, this.currentPos, this.currentFace);
         mc.field_1687.method_8517(mc.field_1724.method_5628(), this.currentPos, -1);
         this.markCooldown();
         this.resetState();
      } else {
         this.resetState();
      }
   }

   public boolean isBreaking() {
      return this.breaking;
   }

   public boolean isBreaking(class_2338 pos) {
      return this.breaking && this.currentPos != null && this.currentPos.equals(pos);
   }

   public class_2338 getCurrentPos() {
      return this.currentPos;
   }

   private void finish(class_2680 state) {
      if (mc.field_1724 != null && mc.field_1761 != null && this.currentPos != null && this.currentFace != null) {
         if (!this.swungThisBreak && mc.field_1724 != null) {
            mc.field_1724.method_6104(class_1268.field_5808);
            this.swungThisBreak = true;
         }

         this.sendPacket(class_2847.field_12973, this.currentPos, this.currentFace);
         mc.field_1761.method_2899(this.currentPos);
         mc.field_1687.method_8517(mc.field_1724.method_5628(), this.currentPos, -1);
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
      this.currentFace = class_2350.field_11036;
      this.callback = null;
      this.predictedCooldownMs = 0L;
      this.swungThisBreak = false;
   }

   private void sendPacket(class_2847 action, class_2338 pos, class_2350 face) {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         mc.field_1761.method_41931(mc.field_1687, seq -> new class_2846(action, pos, face, seq));
      }
   }

   private long computeCooldownMs(class_746 player) {
      return player.method_31549().field_7477 ? 50L : 250L;
   }

   public interface Callback {
      void onBlockBroken(class_2338 var1, class_2680 var2);
   }
}
