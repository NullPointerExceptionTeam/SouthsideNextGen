package dev.southside.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.events.ChatEvent;
import dev.southside.event.events.CustomPayloadEvent;
import dev.southside.event.events.GameJoinedAddEntityEvent;
import dev.southside.event.events.GameJoinedEvent;
import dev.southside.event.events.GameLeftEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.misc.ChatBypass;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.rotation.RotationUtils;
import java.util.Set;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayNetworkHandler.class})
public class MixinClientPlayNetworkHandler {
   @Unique
   private float originalYaw;
   @Unique
   private float originalPitch;
   @Shadow
   private ClientWorld world;
   @Unique
   private boolean worldNotNull;

   @ModifyVariable(
      method = {"sendChatMessage(Ljava/lang/String;)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String handleSendMessage(String content) {
      return Southside.INSTANCE.getModuleManager().get(ChatBypass.class).isEnabled() ? ChatBypass.bypass(content) : content;
   }

   @Inject(
      method = {"onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V"},
      at = {@At("HEAD")}
   )
   private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
      this.worldNotNull = this.world != null;
   }

   @Inject(
      method = {"onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/world/ClientWorld;addEntity(Lnet/minecraft/entity/Entity;)V",
         shift = Shift.BEFORE
      )}
   )
   private void onGameJoinAddEntity(GameJoinS2CPacket packet, CallbackInfo info) {
      Southside.get().getEventBus().post(GameJoinedAddEntityEvent.INSTANCE);
   }

   @Inject(
      method = {"onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;init()V",
         shift = Shift.AFTER
      )}
   )
   private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
      if (this.worldNotNull) {
         Southside.get().getEventBus().post(GameLeftEvent.INSTANCE);
      }

      Southside.get().getEventBus().post(GameJoinedEvent.INSTANCE);
   }

   @Inject(
      method = {"onEnterReconfiguration(Lnet/minecraft/network/packet/s2c/play/EnterReconfigurationS2CPacket;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )}
   )
   private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
      Southside.get().getEventBus().post(GameLeftEvent.INSTANCE);
   }

   @WrapOperation(
      method = {"onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;warnOnUnknownPayload(Lnet/minecraft/network/packet/CustomPayload;)V"
      )}
   )
   private void onCustomPayload(ClientPlayNetworkHandler instance, CustomPayload payload, Operation<Void> original) {
      CustomPayloadEvent event = new CustomPayloadEvent(payload);
      Southside.get().getEventBus().post(event);
      original.call(new Object[]{instance, payload});
   }

   @Inject(
      method = {"sendChatMessage(Ljava/lang/String;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendChatMessage(String content, CallbackInfo info) {
      ChatEvent event = new ChatEvent(content);
      Southside.INSTANCE.getEventBus().call(event);
      String prefix = ".";
      if (content.startsWith(prefix)) {
         try {
            Southside.get().getCommandManager().dispatch(content.substring(prefix.length()));
         } catch (CommandSyntaxException var6) {
            ChatUtils.error(var6.getMessage());
         }

         MinecraftAccessor.mc.inGameHud.getChatHud().addToMessageHistory(content);
         info.cancel();
      }

      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(
      method = {"onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;setPosition(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;Lnet/minecraft/entity/Entity;Z)Z",
         shift = Shift.BEFORE
      )}
   )
   public void onSetPos(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null) {
         this.originalYaw = MinecraftClient.getInstance().player.getYaw();
         this.originalPitch = MinecraftClient.getInstance().player.getPitch();
         MinecraftClient.getInstance()
            .player
            .setYaw(Disabler.normalizeYawDiff(RotationUtils.getRotationSafely().yaw, RotationUtils.getServerRotation().yaw));
         MinecraftClient.getInstance().player.setPitch(RotationUtils.getRotationSafely().getPitch());
      }
   }

   @Inject(
      method = {"onPlayerPositionLook(Lnet/minecraft/network/packet/s2c/play/PlayerPositionLookS2CPacket;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;setPosition(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;Lnet/minecraft/entity/Entity;Z)Z",
         shift = Shift.AFTER
      )}
   )
   public void onResetPos(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null) {
         MinecraftClient.getInstance().player.setYaw(this.originalYaw);
         MinecraftClient.getInstance().player.setPitch(this.originalPitch);
      }
   }

   @Inject(
      method = {"setPosition(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;Lnet/minecraft/entity/Entity;Z)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void replaceSetPos(PlayerPosition pos, Set<PositionFlag> flags, Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof OtherClientPlayerEntity otherClientPlayer) {
         IBoxUpdate player = (IBoxUpdate)otherClientPlayer;
         PlayerPosition playerPosition = PlayerPosition.fromEntityLerpTarget(entity);
         PlayerPosition playerPosition2 = PlayerPosition.apply(playerPosition, pos, flags);
         boolean bl2 = playerPosition.position().squaredDistanceTo(playerPosition2.position()) > 4096.0;
         if (bl && !bl2) {
            entity.updateTrackedPositionAndAngles(
               playerPosition2.position().getX(),
               playerPosition2.position().getY(),
               playerPosition2.position().getZ(),
               playerPosition2.yaw(),
               playerPosition2.pitch(),
               3
            );
            entity.setVelocity(playerPosition2.deltaMovement());
            cir.setReturnValue(true);
         } else {
            if (ServerPacketManager.deSyncing) {
               synchronized (ServerPacketManager.packets) {
                  ServerPacketManager.packets.add((Runnable)() -> {
                     player.setBoxPos(playerPosition2.position());
                     player.setServerBoxPos(playerPosition2.position());
                     entity.setPosition(playerPosition2.position());
                  });
               }

               entity.setPos(playerPosition2.position().x, playerPosition2.position().y, playerPosition2.position().z);
            } else {
               player.setBoxPos(playerPosition2.position());
               player.setServerBoxPos(playerPosition2.position());
               entity.setPosition(playerPosition2.position());
            }

            entity.setVelocity(playerPosition2.deltaMovement());
            entity.setYaw(playerPosition2.yaw());
            entity.setPitch(playerPosition2.pitch());
            PlayerPosition playerPosition3 = new PlayerPosition(entity.getLastRenderPos(), Vec3d.ZERO, entity.prevYaw, entity.prevPitch);
            PlayerPosition playerPosition4 = PlayerPosition.apply(playerPosition3, pos, flags);
            entity.setPrevPositionAndAngles(playerPosition4.position(), playerPosition4.yaw(), playerPosition4.pitch());
            cir.setReturnValue(false);
         }
      }
   }
}
