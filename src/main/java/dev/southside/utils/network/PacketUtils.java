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
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;

public class PacketUtils implements MinecraftAccessor {
   public static int noMovePackets = 0;
   private static final HashMap<Class<?>, NetworkSide> sideCache = new HashMap<>();

   public static void sendPacketNoEvent(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().getConnection().send(packet, null);
         Southside.INSTANCE.getEventBus().call(new PacketSendPostEvent(packet));
      }
   }

   public static void callPacket(Packet<?> packet) {
      if (packet instanceof PlayerMoveC2SPacket movePacket) {
         if (movePacket.changesPosition()) {
            noMovePackets = 0;
         } else {
            noMovePackets++;
         }
      }
   }

   public static void handlePacket(Packet<?> packet) {
      try {
         if (mc.getNetworkHandler() == null) {
            return;
         }

         NetworkSide side = getPacketSide(packet.getClass());
         switch (side) {
            case CLIENTBOUND:
               ((Packet)packet).apply(mc.getNetworkHandler());
               break;
            case SERVERBOUND:
               sendPacket(packet);
         }
      } catch (OffThreadException var2) {
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   public static void handlePacketNoEvent(Packet<?> packet) {
      try {
         if (mc.getNetworkHandler() == null) {
            return;
         }

         NetworkSide side = getPacketSide(packet.getClass());
         switch (side) {
            case CLIENTBOUND:
               ((Packet)packet).apply(mc.getNetworkHandler());
               break;
            case SERVERBOUND:
               sendPacketNoEvent(packet);
         }
      } catch (OffThreadException var2) {
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   public static void sendPacket(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(packet);
      }
   }

   public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
      mc.interactionManager.sendSequencedPacket(mc.world, packetCreator);
   }

   public static NetworkSide getPacketSide(Class<?> clazz) {
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
                  if (ServerPacketListener.class.isAssignableFrom(listenerClass)) {
                     possibleServerSide++;
                  } else if (ClientPacketListener.class.isAssignableFrom(listenerClass)) {
                     possibleClientSide++;
                  }
               } else {
                  Class<?> superClazz = Class.forName(generic);
                  if (Packet.class.isAssignableFrom(superClazz)) {
                     NetworkSide side = getPacketSide(superClazz);
                     switch (side) {
                        case CLIENTBOUND:
                           possibleClientSide++;
                           break;
                        case SERVERBOUND:
                           possibleServerSide++;
                     }
                  }
               }
            }
         } catch (Throwable var12) {
         }

         NetworkSide side = possibleClientSide >= possibleServerSide ? NetworkSide.CLIENTBOUND : NetworkSide.SERVERBOUND;
         sideCache.put(clazz, side);
         return side;
      }
   }

   public static boolean canDelay(Packet packet) {
      return !(packet instanceof ChatMessageS2CPacket)
         && !(packet instanceof ScoreboardDisplayS2CPacket)
         && !(packet instanceof EntityEquipmentUpdateS2CPacket)
         && !(packet instanceof ClearTitleS2CPacket)
         && !(packet instanceof TitleS2CPacket)
         && !(packet instanceof BossBarS2CPacket)
         && !(packet instanceof EntitiesDestroyS2CPacket)
         && !(packet instanceof HealthUpdateS2CPacket)
         && !(packet instanceof ScoreboardObjectiveUpdateS2CPacket)
         && !(packet instanceof ScoreboardScoreResetS2CPacket)
         && !(packet instanceof ScoreboardScoreUpdateS2CPacket)
         && !(packet instanceof SubtitleS2CPacket)
         && !(packet instanceof TitleFadeS2CPacket)
         && !(packet instanceof PlaySoundS2CPacket)
         && !(packet instanceof PlaySoundFromEntityS2CPacket)
         && !(packet instanceof EntitySetHeadYawS2CPacket)
         && !(packet instanceof EntityS2CPacket)
         && !(packet instanceof EntityPositionS2CPacket)
         && !(packet instanceof EntityPositionSyncS2CPacket)
         && !(packet instanceof EntityTrackerUpdateS2CPacket trackerUpdate && (mc.player == null || trackerUpdate.id() != mc.player.getId()))
         && !(packet instanceof KeepAliveS2CPacket)
         && !(packet instanceof EntityDamageS2CPacket)
         && !(packet instanceof EntityVelocityUpdateS2CPacket velocity && (mc.player == null || velocity.getEntityId() != mc.player.getId()))
         && !(packet instanceof PlayerPositionLookS2CPacket);
   }

   public static void spoofRotation(Rotation target) {
      if (mc.interactionManager != null) {
         Rotation old = RotationUtils.rotation.copy();
         RotationUtils.rotation = target;
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         RotationUtils.rotation = old;
      }
   }
}
