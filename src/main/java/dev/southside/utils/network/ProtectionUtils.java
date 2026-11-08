package dev.southside.utils.network;

import net.minecraft.class_1255;
import net.minecraft.class_148;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import net.minecraft.class_2602;
import net.minecraft.class_2987;
import net.minecraft.class_5900;
import org.slf4j.Logger;

public class ProtectionUtils {
   public static <T extends class_2547> void onEnsureRunningOnSameThread(Logger LOGGER, class_2596<T> packet, T listener, class_1255<?> executor) throws class_2987 {
      if (!executor.method_18854()) {
         executor.method_40000(() -> {
            if (listener.method_52413(packet)) {
               try {
                  if (packet instanceof class_5900 teamPacket && listener instanceof class_2602 clientListener) {
                     handleTeamPacketSafely(LOGGER, teamPacket, clientListener);
                     return;
                  }

                  packet.method_65081(listener);
               } catch (Exception var5) {
                  if (var5 instanceof NullPointerException) {
                     LOGGER.warn("Safely suppressed NullPointerException while handling packet {}: {}", packet, var5.getMessage());
                     return;
                  }

                  if (var5 instanceof class_148 crashException && crashException.getCause() instanceof OutOfMemoryError) {
                     return;
                  }

                  listener.method_59807(packet, var5);
               }
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
            }
         });
         throw class_2987.field_13400;
      }
   }

   private static void handleTeamPacketSafely(Logger LOGGER, class_5900 packet, class_2602 listener) {
      try {
         packet.method_34173(listener);
      } catch (IllegalStateException var4) {
         LOGGER.warn("Failed to handle team packet: {}", var4.getMessage());
      } catch (NullPointerException var5) {
         LOGGER.warn("Safely suppressed NullPointerException while handling team packet: {}", var5.getMessage());
      } catch (Exception var6) {
         LOGGER.error("Error handling team packet", var6);
      }
   }
}
