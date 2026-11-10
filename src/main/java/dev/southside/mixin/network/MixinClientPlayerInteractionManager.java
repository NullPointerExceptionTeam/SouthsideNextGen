package dev.southside.mixin.network;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class MixinClientPlayerInteractionManager {
   @Unique
   private float originalYaw;
   @Unique
   private float originalPitch;
   @Shadow
   @Final
   public MinecraftClient client;

   @Inject(
      method = {"attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target, Stage.PRE);
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V"},
      at = {@At("TAIL")}
   )
   private void hookAttackPost(PlayerEntity player, Entity target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target, Stage.POST);
      Southside.INSTANCE.getEventBus().call(event);
   }

   @Inject(
      method = {"interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("HEAD")}
   )
   private void hookItemRotSet(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.setRot(player);
      }
   }

   @Inject(
      method = {"interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("RETURN")}
   )
   private void hookItemRotRestore(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.restore(player);
      }
   }

   @Inject(
      method = {"interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("HEAD")}
   )
   private void hookBlockRotSet(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.setRot(player);
      }
   }

   @Inject(
      method = {"syncSelectedSlot()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSyncSlot(CallbackInfo ci) {
      if (PacketOrderManager.rightClicking) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"},
      at = {@At("RETURN")}
   )
   private void hookBlockRotRestore(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      if (MinecraftClient.getInstance().player == player) {
         this.restore(player);
      }
   }

   @Unique
   private void setRot(PlayerEntity player) {
      if (this.client.player != null) {
         this.originalYaw = player.getYaw();
         this.originalPitch = player.getPitch();
         player.setYaw(Disabler.normalizeYawDiff(RotationUtils.getRotationSafely().yaw, RotationUtils.getServerRotation().yaw));
         player.setPitch(RotationUtils.getRotationSafely().pitch);
         Disabler.interacting = true;
      }
   }

   @Unique
   private void restore(PlayerEntity player) {
      if (this.client.player != null) {
         player.setYaw(this.originalYaw);
         player.setPitch(this.originalPitch);
         Disabler.interacting = false;
      }
   }
}
