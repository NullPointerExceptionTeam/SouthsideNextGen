package dev.southside.utils.misc;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.network.PacketUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_2596;
import net.minecraft.class_2827;

public class BlinkUtils {
   public static final BlinkUtils INSTANCE = new BlinkUtils();
   public static boolean blinking = false;
   private static final List<Class<?>> blackList = new ArrayList<>();
   private static final Map<Class<?>, Predicate<class_2596<?>>> addReturnPredicateMap = new HashMap<>();
   private static final Map<Class<?>, Predicate<class_2596<?>>> cancelReturnPredicateMap = new HashMap<>();
   private static final Map<Class<?>, Predicate<class_2596<?>>> releaseReturnPredicateMap = new HashMap<>();
   private static final Map<Class<?>, Consumer<class_2596<?>>> cancelActionMap = new HashMap<>();
   private static final Map<Class<?>, Consumer<class_2596<?>>> releaseActionMap = new HashMap<>();
   private static final List<Class<?>> whitList = new ArrayList<>();
   public static LinkedBlockingQueue<class_2596<?>> packets = new LinkedBlockingQueue<>();
   public static boolean passEvent = false;

   public static boolean blink(Class<?>... fliterPackets) {
      if (blinking) {
         return false;
      } else {
         Arrays.asList(fliterPackets).forEach(e -> {
            blackList.add((Class<?>)e);
            cancelReturnPredicateMap.put((Class<?>)e, f -> true);
         });
         blinking = true;
         return true;
      }
   }

   public static void addWhiteList(Class<?>... classes) {
      whitList.addAll(Arrays.asList(classes));
   }

   public static void sendPacket(class_2596<?> packet, boolean event) {
      if (event) {
         PacketEvent event1 = new PacketEvent(TransferOrigin.SEND, packet, MinecraftAccessor.mc.method_1562().method_48296());
         Southside.INSTANCE.getEventBus().call(event1);
         if (event1.isCancelled()) {
            return;
         }
      }

      passEvent = true;
      PacketUtils.sendPacketNoEvent(packet);
      passEvent = false;
   }

   public static void removeBlackList(Class<?> packetClazz) {
      blackList.remove(packetClazz);
   }

   public static void resetBlackList() {
      blackList.clear();
   }

   public static void setCancelReturnPredicate(Class<?> clazz, Predicate<class_2596<?>> predicate) {
      boolean isIN = false;

      for (Class<?> classes : cancelReturnPredicateMap.keySet()) {
         if (classes == clazz) {
            isIN = true;
            break;
         }
      }

      if (isIN) {
         cancelReturnPredicateMap.replace(clazz, predicate);
      } else {
         cancelReturnPredicateMap.put(clazz, predicate);
      }
   }

   public static void setAddReturnPredicate(Class<?> clazz, Predicate<class_2596<?>> predicate) {
      boolean isIN = false;

      for (Class<?> classes : addReturnPredicateMap.keySet()) {
         if (classes == clazz) {
            isIN = true;
            break;
         }
      }

      if (isIN) {
         addReturnPredicateMap.replace(clazz, predicate);
      } else {
         addReturnPredicateMap.put(clazz, predicate);
      }
   }

   public static void setReleaseReturnPredicate(Class<?> clazz, Predicate<class_2596<?>> predicate) {
      boolean isIN = false;

      for (Class<?> classes : releaseReturnPredicateMap.keySet()) {
         if (classes == clazz) {
            isIN = true;
            break;
         }
      }

      if (isIN) {
         releaseReturnPredicateMap.replace(clazz, predicate);
      } else {
         releaseReturnPredicateMap.put(clazz, predicate);
      }
   }

   public static void setCancelAction(Class<?> clazz, Consumer<class_2596<?>> packetConsumer) {
      boolean isIN = false;

      for (Class<?> classes : cancelActionMap.keySet()) {
         if (classes == clazz) {
            isIN = true;
            break;
         }
      }

      if (isIN) {
         cancelActionMap.replace(clazz, packetConsumer);
      } else {
         cancelActionMap.put(clazz, packetConsumer);
      }
   }

   public static void setReleaseAction(Class<?> clazz, Consumer<class_2596<?>> packetConsumer) {
      boolean isIN = false;

      for (Class<?> classes : releaseActionMap.keySet()) {
         if (classes == clazz) {
            isIN = true;
            break;
         }
      }

      if (isIN) {
         releaseActionMap.replace(clazz, packetConsumer);
      } else {
         releaseActionMap.put(clazz, packetConsumer);
      }
   }

   public static void releasePacket(boolean sendOneTick) {
      releasePacket(packets.size(), true, sendOneTick);
   }

   public static void releasePacket() {
      releasePacket(packets.size(), true);
   }

   public static void releasePacket(int sendPackets, boolean noEvent) {
      releasePacket(sendPackets, noEvent, false);
   }

   public static void releasePacket(int sendPackets, boolean noEvent, boolean sendOneTick) {
      int sends = 0;

      try {
         label41:
         while (!packets.isEmpty()) {
            class_2596<?> packet = packets.take();
            if (packet instanceof class_2827) {
               if (sendOneTick) {
                  break;
               }
            } else {
               for (Entry<Class<?>, Predicate<class_2596<?>>> entries : releaseReturnPredicateMap.entrySet()) {
                  if (entries.getKey().isAssignableFrom(packet.getClass()) && entries.getValue().test(packet)) {
                     continue label41;
                  }
               }

               releaseActionMap.forEach((key, value) -> {
                  if (key.isAssignableFrom(packet.getClass())) {
                     value.accept(packet);
                  }
               });
               sends++;
               if (noEvent) {
                  passEvent = true;
                  PacketUtils.sendPacketNoEvent(packet);
                  passEvent = false;
               } else {
                  passEvent = true;
                  MinecraftAccessor.mc.method_1562().method_52787(packet);
                  passEvent = false;
               }

               if (sends >= sendPackets) {
                  break;
               }
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   public static void stopBlink() {
      blinking = false;
      passEvent = false;
      releasePacket();
      blackList.clear();
      cancelReturnPredicateMap.clear();
      cancelActionMap.clear();
      releaseActionMap.clear();
      releaseReturnPredicateMap.clear();
      whitList.clear();
      packets.clear();
   }

   public static boolean onPacket(class_2596<?> packet) {
      if (blinking && !passEvent) {
         cancelActionMap.forEach((aClass, packetConsumer) -> {
            if (aClass.isAssignableFrom(packet.getClass())) {
               packetConsumer.accept(packet);
            }
         });

         for (Class<?> clazz : blackList) {
            if (clazz.isAssignableFrom(packet.getClass())) {
               return true;
            }
         }

         for (Entry<Class<?>, Predicate<class_2596<?>>> entries : cancelReturnPredicateMap.entrySet()) {
            if (entries.getKey().isAssignableFrom(packet.getClass()) && entries.getValue().test(packet)) {
               return true;
            }
         }

         if (!whitList.isEmpty() && !whitList.contains(packet.getClass())) {
            return true;
         } else {
            boolean needAdd = true;

            for (Entry<Class<?>, Predicate<class_2596<?>>> entriesx : addReturnPredicateMap.entrySet()) {
               if (entriesx.getKey().isAssignableFrom(packet.getClass()) && entriesx.getValue().test(packet)) {
                  needAdd = false;
               }
            }

            if (needAdd) {
               packets.add(packet);
            }

            return false;
         }
      } else {
         return true;
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (MinecraftAccessor.mc.method_1562() == null) {
         stopBlink();
      }

      if (blinking) {
         packets.add(new class_2827(MathUtils.random.nextLong()));
      }
   }
}
