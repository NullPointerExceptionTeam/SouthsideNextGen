package dev.southside.mixin.network;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_636.class})
public abstract class MixinClientPlayerInteractionManager {
   @Unique
   private float originalYaw;
   @Unique
   private float originalPitch;
   @Shadow
   @Final
   public class_310 field_3712;

   @Inject(
      method = {"method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookAttack(class_1657 player, class_1297 target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target, Stage.PRE);
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V"},
      at = {@At("TAIL")}
   )
   private void hookAttackPost(class_1657 player, class_1297 target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target, Stage.POST);
      Southside.INSTANCE.getEventBus().call(event);
   }

   @Inject(
      method = {"method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"},
      at = {@At("HEAD")}
   )
   private void hookItemRotSet(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      if (class_310.method_1551().field_1724 == player) {
         this.setRot(player);
      }
   }

   @Inject(
      method = {"method_2919(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"},
      at = {@At("RETURN")}
   )
   private void hookItemRotRestore(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
      if (class_310.method_1551().field_1724 == player) {
         this.restore(player);
      }
   }

   @Inject(
      method = {"method_2896(Lnet/minecraft/class_746;Lnet/minecraft/class_1268;Lnet/minecraft/class_3965;)Lnet/minecraft/class_1269;"},
      at = {@At("HEAD")}
   )
   private void hookBlockRotSet(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
      if (class_310.method_1551().field_1724 == player) {
         this.setRot(player);
      }
   }

   @Inject(
      method = {"method_2911()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hookSyncSlot(CallbackInfo ci) {
      if (PacketOrderManager.rightClicking) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_2896(Lnet/minecraft/class_746;Lnet/minecraft/class_1268;Lnet/minecraft/class_3965;)Lnet/minecraft/class_1269;"},
      at = {@At("RETURN")}
   )
   private void hookBlockRotRestore(class_746 player, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir) {
      if (class_310.method_1551().field_1724 == player) {
         this.restore(player);
      }
   }

   @Unique
   private void setRot(class_1657 player) {
      if (this.field_3712.field_1724 != null) {
         this.originalYaw = player.method_36454();
         this.originalPitch = player.method_36455();
         player.method_36456(Disabler.normalizeYawDiff(RotationUtils.getRotationSafely().yaw, RotationUtils.getServerRotation().yaw));
         player.method_36457(RotationUtils.getRotationSafely().pitch);
         Disabler.interacting = true;
      }
   }

   @Unique
   private void restore(class_1657 player) {
      if (this.field_3712.field_1724 != null) {
         player.method_36456(this.originalYaw);
         player.method_36457(this.originalPitch);
         Disabler.interacting = false;
      }
   }
}
