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
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;

public final class ServerPacketManager {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
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

   public static boolean onPacket(Packet packet) {
      if (mc.currentScreen instanceof ConnectScreen) {
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
         if (mc.getNetworkHandler() == null) {
            stop();
            return;
         }

         while (true) {
            Packet packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof Packet incoming) {
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

            if (!(packet instanceof KeepAliveC2SPacket)) {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.apply(mc.getNetworkHandler());
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
      if (mc.getNetworkHandler() == null) {
         stop();
      } else {
         releasing = true;

         while (true) {
            Packet packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof Packet incoming) {
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

            if (packet instanceof KeepAliveC2SPacket) {
               deSyncTick--;
               break;
            }

            if (event) {
               PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
               Southside.INSTANCE.getEventBus().call(event1);
               if (event1.isCancelled()) {
                  continue;
               }
            }

            MovementUtils.onPacketRec(packet);

            try {
               packet.apply(mc.getNetworkHandler());
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }

         releasing = false;
      }
   }

   public static void releaseUntil(Predicate<Packet> flag, boolean event) {
      if (mc.getNetworkHandler() == null) {
         stop();
      } else {
         releasing = true;
         boolean stopRelease = false;

         while (true) {
            Packet packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof Packet incoming) {
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

            if (packet instanceof KeepAliveC2SPacket) {
               deSyncTick--;
               if (stopRelease) {
                  break;
               }
            } else {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.apply(mc.getNetworkHandler());
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

   public static void filteredRelease(Predicate<Packet> flag, boolean event) {
      if (mc.getNetworkHandler() == null) {
         stop();
      } else {
         while (true) {
            Packet packet;
            synchronized (packets) {
               Object obj = packets.poll();
               if (obj instanceof Packet incoming) {
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

            if (packet instanceof KeepAliveC2SPacket) {
               deSyncTick--;
            } else if (!flag.test(packet)) {
               if (event) {
                  PacketEvent event1 = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
                  Southside.INSTANCE.getEventBus().call(event1);
                  if (event1.isCancelled()) {
                     continue;
                  }
               }

               MovementUtils.onPacketRec(packet);

               try {
                  packet.apply(mc.getNetworkHandler());
               } catch (Exception var7) {
                  var7.printStackTrace();
               }
            }
         }
      }
   }

   public static void postTick() {
      if (deSyncing) {
         packets.add(new KeepAliveC2SPacket(-1337L));
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
