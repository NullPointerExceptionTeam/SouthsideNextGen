package dev.southside.manager;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.module.impl.player.StealerN;
import dev.southside.utils.network.TimedPacket;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Generated;
import net.minecraft.class_2596;
import net.minecraft.class_2670;

public final class PingSpoofManager implements MinecraftAccessor {
   public static final LinkedBlockingQueue<TimedPacket> packets = new LinkedBlockingQueue<>();
   public static boolean enabled = false;

   public static boolean onPacket(class_2596<?> packet) {
      if (!enabled) {
         return false;
      } else if (packet instanceof class_2670) {
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
                  timedPacket.getPacket().method_65081(mc.method_1562());
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
                     keepAlive.getPacket().method_65081(mc.method_1562());
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
