package dev.southside.manager;

import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PacketReceiveAsyncEvent;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.MovementUtils;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.class_2596;
import net.minecraft.class_2827;
import net.minecraft.class_310;
import net.minecraft.class_412;

public final class ServerPacketManager {
   private static final class_310 mc = class_310.method_1551();
   public static boolean deSyncing = false;
   public static boolean releasing = false;
   public static final LinkedBlockingQueue<Object> packets = new LinkedBlockingQueue<>();
   public static int deSyncTick;

   public static void setup() {
      if (!deSyncing) {
         deSyncing = true;
      }
   }

   public static void onTick() {
      if (deSyncing) {
         deSyncTick++;
      }
   }

   public static boolean onPacket(class_2596 packet) {
      if (mc.field_1755 instanceof class_412) {
         stop();
         return false;
      } else {
         synchronized (packets) {
            if (deSyncing) {
               Southside.INSTANCE.getEventBus().call(new PacketReceiveAsyncEvent(packet));
               if (PacketUtils.canDelay(packet)) {
                  packets.add(packet);
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static void reset() {
      reset(true);
   }

   public static void reset(boolean event) {
      releasing = true;

      try {
         if (mc.method_1562() == null) {
            stop();
            return;
         }

         while (true) {
            class_2596 packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof class_2596 incoming) {
                  packet = incoming;
               } else {
                  if (obj instanceof Runnable runnable) {
                     runnable.run();
                     continue;
                  }

                  packet = null;
               }

               if (packet == null) {
                  deSyncing = false;
                  deSyncTick = 0;
                  break;
               }
            }

            if (!(packet instanceof class_2827)) {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.method_1562().method_48296());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.method_65081(mc.method_1562());
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }
         }
      } catch (Exception var8) {
      }

      releasing = false;
   }

   public static void releaseTick(boolean event) {
      if (mc.method_1562() == null) {
         stop();
      } else {
         releasing = true;

         while (true) {
            class_2596 packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof class_2596 incoming) {
                  packet = incoming;
               } else {
                  if (obj instanceof Runnable runnable) {
                     runnable.run();
                     continue;
                  }

                  packet = null;
               }

               if (packet == null) {
                  deSyncing = false;
                  deSyncTick = 0;
                  break;
               }
            }

            if (packet instanceof class_2827) {
               deSyncTick--;
               break;
            }

            if (event) {
               PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.method_1562().method_48296());
               Southside.INSTANCE.getEventBus().call(event1);
               if (event1.isCancelled()) {
                  continue;
               }
            }

            MovementUtils.onPacketRec(packet);

            try {
               packet.method_65081(mc.method_1562());
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }

         releasing = false;
      }
   }

   public static void releaseUntil(Predicate<class_2596> flag, boolean event) {
      if (mc.method_1562() == null) {
         stop();
      } else {
         releasing = true;
         boolean stopRelease = false;

         while (true) {
            class_2596 packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof class_2596 incoming) {
                  packet = incoming;
               } else {
                  if (obj instanceof Runnable runnable) {
                     runnable.run();
                     continue;
                  }

                  packet = null;
               }

               if (packet == null) {
                  deSyncTick = 0;
                  break;
               }
            }

            if (packet instanceof class_2827) {
               deSyncTick--;
               if (stopRelease) {
                  break;
               }
            } else {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.method_1562().method_48296());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.method_65081(mc.method_1562());
               } catch (Exception var8) {
                  var8.printStackTrace();
               }

               if (flag.test(packet)) {
                  stopRelease = true;
               }
            }
         }

         releasing = false;
      }
   }

   public static void filteredRelease(Predicate<class_2596> flag, boolean event) {
      if (mc.method_1562() == null) {
         stop();
      } else {
         while (true) {
            class_2596 packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof class_2596 incoming) {
                  packet = incoming;
               } else {
                  if (obj instanceof Runnable runnable) {
                     runnable.run();
                     continue;
                  }

                  packet = null;
               }

               if (packet == null) {
                  deSyncing = false;
                  deSyncTick = 0;
                  return;
               }
            }

            if (packet instanceof class_2827) {
               deSyncTick--;
            } else if (!flag.test(packet)) {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.method_1562().method_48296());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.method_65081(mc.method_1562());
               } catch (Exception var7) {
                  var7.printStackTrace();
               }
            }
         }
      }
   }

   public static void postTick() {
      if (deSyncing) {
         packets.add(new class_2827(-1337L));
      }
   }

   public static void stop() {
      deSyncing = false;
      deSyncTick = 0;
      packets.clear();
   }

   @Generated
   private ServerPacketManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
