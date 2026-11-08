package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2846;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;

public final class BlockBreakUtils implements MinecraftAccessor {
   private static class_2338 lastPos;
   private static class_2350 lastFace;
   private static boolean breaking;
   private static float destroyProgress;

   private BlockBreakUtils() {
   }

   public static void doBreak(class_3965 hitResult, boolean immediate) {
      doBreak(hitResult, immediate, false);
   }

   public static void startBreaking(class_3965 hitResult) {
      if (hitResult != null && mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         class_2338 pos = hitResult.method_17777();
         class_2350 face = hitResult.method_17780();
         class_2680 state = mc.field_1687.method_8320(pos);
         if (!state.method_26215() && !(state.method_26214(mc.field_1687, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               stopBreaking();
               lastPos = pos.method_10062();
               lastFace = face;
               destroyProgress = 0.0F;
            }

            if (!breaking) {
               swingOnce(mc.field_1724);
               sendPacket(class_2847.field_12968, lastPos, lastFace);
               breaking = true;
            }
         }
      }
   }

   public static void finishBreaking(class_3965 hitResult) {
      if (hitResult != null && mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         class_2338 pos = hitResult.method_17777();
         class_2350 face = hitResult.method_17780();
         class_2680 state = mc.field_1687.method_8320(pos);
         if (!state.method_26215() && !(state.method_26214(mc.field_1687, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               lastPos = pos.method_10062();
               lastFace = face;
            }

            swingOnce(mc.field_1724);
            sendPacket(class_2847.field_12973, lastPos, lastFace);
            mc.field_1761.method_2899(lastPos);
            mc.field_1687.method_8517(mc.field_1724.method_5628(), lastPos, -1);
            clearLast();
         } else {
            clearLast();
         }
      }
   }

   public static void doBreak(class_3965 hitResult, boolean immediate, boolean ignoredWaitForServerSync) {
      if (hitResult != null && mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         class_2338 pos = hitResult.method_17777();
         class_2350 face = hitResult.method_17780();
         class_2680 state = mc.field_1687.method_8320(pos);
         if (!state.method_26215() && !(state.method_26214(mc.field_1687, pos) < 0.0F)) {
            if (lastPos == null || !lastPos.equals(pos) || lastFace != face) {
               stopBreaking();
               lastPos = pos.method_10062();
               lastFace = face;
               breaking = false;
               destroyProgress = 0.0F;
            }

            if (!immediate && !mc.field_1724.method_31549().field_7477) {
               if (!breaking) {
                  sendPacket(class_2847.field_12968, lastPos, lastFace);
                  breaking = true;
               }

               float delta = state.method_26165(mc.field_1724, mc.field_1724.field_17892, lastPos);
               if (!(delta <= 0.0F)) {
                  swingOnce(mc.field_1724);
                  destroyProgress += delta;
                  mc.field_1687.method_8517(mc.field_1724.method_5628(), lastPos, Math.max(0, (int)(destroyProgress * 10.0F) - 1));
                  if (destroyProgress >= 1.0F) {
                     sendPacket(class_2847.field_12973, lastPos, lastFace);
                     mc.field_1761.method_2899(lastPos);
                     mc.field_1687.method_8517(mc.field_1724.method_5628(), lastPos, -1);
                     clearLast();
                  }
               }
            } else {
               swingOnce(mc.field_1724);
               sendPacket(class_2847.field_12968, pos, face);
               sendPacket(class_2847.field_12973, pos, face);
               mc.field_1761.method_2899(pos);
               mc.field_1687.method_8517(mc.field_1724.method_5628(), pos, -1);
               clearLast();
            }
         }
      }
   }

   public static void stopBreaking() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         if (breaking && lastPos != null && !mc.field_1687.method_8320(lastPos).method_26215()) {
            sendPacket(class_2847.field_12971, lastPos, lastFace == null ? class_2350.field_11036 : lastFace);
            mc.field_1687.method_8517(mc.field_1724.method_5628(), lastPos, -1);
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

   private static void sendPacket(class_2847 action, class_2338 pos, class_2350 face) {
      if (mc.field_1761 != null && mc.field_1687 != null) {
         mc.field_1761.method_41931(mc.field_1687, seq -> new class_2846(action, pos, face, seq));
      }
   }

   private static void swingOnce(class_746 player) {
      player.method_6104(class_1268.field_5808);
   }
}
