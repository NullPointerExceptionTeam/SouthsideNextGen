package dev.southside.utils.network;

import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.slf4j.Logger;

public class ProtectionUtils {
   public static <T extends PacketListener> void onEnsureRunningOnSameThread(Logger LOGGER, Packet<T> packet, T listener, ThreadExecutor<?> executor) throws OffThreadException {
      if (!executor.isOnThread()) {
         executor.executeSync(() -> {
            if (listener.accepts(packet)) {
               try {
                  if (packet instanceof TeamS2CPacket teamPacket && listener instanceof ClientPlayPacketListener clientListener) {
                     handleTeamPacketSafely(LOGGER, teamPacket, clientListener);
                     return;
                  }

                  packet.apply(listener);
               } catch (Exception var5) {
                  if (var5 instanceof NullPointerException) {
                     LOGGER.warn("Safely suppressed NullPointerException while handling packet {}: {}", packet, var5.getMessage());
                     return;
                  }

                  if (var5 instanceof CrashException crashException && crashException.getCause() instanceof OutOfMemoryError) {
                     return;
                  }

                  listener.onPacketException(packet, var5);
               }
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
            }
         });
         throw OffThreadException.INSTANCE;
      }
   }

   private static void handleTeamPacketSafely(Logger LOGGER, TeamS2CPacket packet, ClientPlayPacketListener listener) {
      try {
         packet.apply(listener);
      } catch (IllegalStateException var4) {
         LOGGER.warn("Failed to handle team packet: {}", var4.getMessage());
      } catch (NullPointerException var5) {
         LOGGER.warn("Safely suppressed NullPointerException while handling team packet: {}", var5.getMessage());
      } catch (Exception var6) {
         LOGGER.error("Error handling team packet", var6);
      }
   }
}
