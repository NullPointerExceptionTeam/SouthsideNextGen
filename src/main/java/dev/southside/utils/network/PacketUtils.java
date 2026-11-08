package dev.southside.utils.network;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.events.PacketSendPostEvent;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.class_10264;
import net.minecraft.class_1268;
import net.minecraft.class_2596;
import net.minecraft.class_2598;
import net.minecraft.class_2629;
import net.minecraft.class_2670;
import net.minecraft.class_2684;
import net.minecraft.class_2708;
import net.minecraft.class_2716;
import net.minecraft.class_2726;
import net.minecraft.class_2736;
import net.minecraft.class_2739;
import net.minecraft.class_2743;
import net.minecraft.class_2744;
import net.minecraft.class_2749;
import net.minecraft.class_2751;
import net.minecraft.class_2757;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_2777;
import net.minecraft.class_2828;
import net.minecraft.class_2987;
import net.minecraft.class_5888;
import net.minecraft.class_5903;
import net.minecraft.class_5904;
import net.minecraft.class_5905;
import net.minecraft.class_7204;
import net.minecraft.class_7438;
import net.minecraft.class_8143;
import net.minecraft.class_8697;
import net.minecraft.class_8701;
import net.minecraft.class_9006;

public class PacketUtils implements MinecraftAccessor {
   public static int noMovePackets = 0;
   private static final HashMap<Class<?>, class_2598> sideCache = new HashMap<>();

   public static void sendPacketNoEvent(class_2596<?> packet) {
      if (mc.method_1562() != null) {
         mc.method_1562().method_48296().method_10752(packet, null);
         Southside.INSTANCE.getEventBus().call(new PacketSendPostEvent(packet));
      }
   }

   public static void callPacket(class_2596<?> packet) {
      if (packet instanceof class_2828 movePacket) {
         if (movePacket.method_36171()) {
            noMovePackets = 0;
         } else {
            noMovePackets++;
         }
      }
   }

   public static void handlePacket(class_2596<?> packet) {
      try {
         if (mc.method_1562() == null) {
            return;
         }

         class_2598 side = getPacketSide(packet.getClass());
         switch (side) {
            case field_11942:
               ((class_2596)packet).method_65081(mc.method_1562());
               break;
            case field_11941:
               sendPacket(packet);
         }
      } catch (class_2987 var2) {
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   public static void handlePacketNoEvent(class_2596<?> packet) {
      try {
         if (mc.method_1562() == null) {
            return;
         }

         class_2598 side = getPacketSide(packet.getClass());
         switch (side) {
            case field_11942:
               ((class_2596)packet).method_65081(mc.method_1562());
               break;
            case field_11941:
               sendPacketNoEvent(packet);
         }
      } catch (class_2987 var2) {
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   public static void sendPacket(class_2596<?> packet) {
      if (mc.method_1562() != null) {
         mc.method_1562().method_52787(packet);
      }
   }

   public static void sendSequencedPacket(class_7204 packetCreator) {
      mc.field_1761.method_41931(mc.field_1687, packetCreator);
   }

   public static class_2598 getPacketSide(Class<?> clazz) {
      if (sideCache.containsKey(clazz)) {
         return sideCache.get(clazz);
      } else {
         int possibleServerSide = 0;
         int possibleClientSide = 0;

         try {
            List<String> generics = new ArrayList<>();
            Type superClass = clazz.getGenericSuperclass();
            if (!superClass.getTypeName().equals(Object.class.getTypeName()) && !superClass.getTypeName().equals(Record.class.getTypeName())) {
               generics.add(superClass.getTypeName());
            }

            Type[] genericInterfaces = clazz.getGenericInterfaces();

            for (Type genericInterface : genericInterfaces) {
               if (!genericInterface.getTypeName().equals(Object.class.getTypeName()) && !genericInterface.getTypeName().equals(Record.class.getTypeName())) {
                  generics.add(genericInterface.getTypeName());
               }
            }

            for (String generic : generics) {
               if (generic.contains("<") && generic.contains(">")) {
                  int genericTypeLeft = generic.lastIndexOf("<") + 1;
                  int genericTypeRight = generic.lastIndexOf(">");
                  String listener = generic.substring(genericTypeLeft, genericTypeRight);
                  Class<?> listenerClass = Class.forName(listener);
                  if (class_8701.class.isAssignableFrom(listenerClass)) {
                     possibleServerSide++;
                  } else if (class_8697.class.isAssignableFrom(listenerClass)) {
                     possibleClientSide++;
                  }
               } else {
                  Class<?> superClazz = Class.forName(generic);
                  if (class_2596.class.isAssignableFrom(superClazz)) {
                     class_2598 side = getPacketSide(superClazz);
                     switch (side) {
                        case field_11942:
                           possibleClientSide++;
                           break;
                        case field_11941:
                           possibleServerSide++;
                     }
                  }
               }
            }
         } catch (Throwable var12) {
         }

         class_2598 side = possibleClientSide >= possibleServerSide ? class_2598.field_11942 : class_2598.field_11941;
         sideCache.put(clazz, side);
         return side;
      }
   }

   public static boolean canDelay(class_2596 packet) {
      return !(packet instanceof class_7438)
         && !(packet instanceof class_2736)
         && !(packet instanceof class_2744)
         && !(packet instanceof class_5888)
         && !(packet instanceof class_5904)
         && !(packet instanceof class_2629)
         && !(packet instanceof class_2716)
         && !(packet instanceof class_2749)
         && !(packet instanceof class_2751)
         && !(packet instanceof class_9006)
         && !(packet instanceof class_2757)
         && !(packet instanceof class_5903)
         && !(packet instanceof class_5905)
         && !(packet instanceof class_2767)
         && !(packet instanceof class_2765)
         && !(packet instanceof class_2726)
         && !(packet instanceof class_2684)
         && !(packet instanceof class_2777)
         && !(packet instanceof class_10264)
         && !(packet instanceof class_2739 trackerUpdate && (mc.field_1724 == null || trackerUpdate.comp_1127() != mc.field_1724.method_5628()))
         && !(packet instanceof class_2670)
         && !(packet instanceof class_8143)
         && !(packet instanceof class_2743 velocity && (mc.field_1724 == null || velocity.method_11818() != mc.field_1724.method_5628()))
         && !(packet instanceof class_2708);
   }

   public static void spoofRotation(Rotation target) {
      if (mc.field_1761 != null) {
         Rotation old = RotationUtils.rotation.copy();
         RotationUtils.rotation = target;
         mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
         RotationUtils.rotation = old;
      }
   }
}
