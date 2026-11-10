package dev.southside.manager;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.module.impl.player.StealerN;
import dev.southside.utils.network.TimedPacket;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Generated;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

public final class PingSpoofManager implements MinecraftAccessor {
   public static final LinkedBlockingQueue<TimedPacket> packets = new LinkedBlockingQueue<>();
   public static boolean enabled = false;

   public static boolean onPacket(Packet<?> packet) {
      if (!enabled) {
         return false;
      } else if (packet instanceof KeepAliveS2CPacket) {
         packets.add(new TimedPacket(packet));
         return true;
      } else {
         return false;
      }
   }

   public static void release() {
      checkEnabled();
      if (enabled) {
         synchronized (packets) {
            while (!packets.isEmpty()) {
               TimedPacket timedPacket = packets.peek();
               if (timedPacket == null || System.currentTimeMillis() - timedPacket.getTime() < 1000L) {
                  break;
               }

               packets.poll();

               try {
                  timedPacket.getPacket().apply(mc.getNetworkHandler());
               } catch (Exception var4) {
                  var4.printStackTrace();
               }
            }
         }
      }
   }

   private static void checkEnabled() {
      StealerN module = Southside.INSTANCE.getModuleManager().get(StealerN.class);
      if (module.instant.getValue()) {
         enabled = true;
      } else if (enabled) {
         enabled = false;
         synchronized (packets) {
            while (!packets.isEmpty()) {
               TimedPacket keepAlive = packets.poll();
               if (keepAlive != null) {
                  try {
                     keepAlive.getPacket().apply(mc.getNetworkHandler());
                  } catch (Exception var5) {
                     var5.printStackTrace();
                  }
               }
            }
         }
      }
   }

   @Generated
   private PingSpoofManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
