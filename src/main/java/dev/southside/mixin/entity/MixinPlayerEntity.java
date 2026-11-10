package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.southside.Southside;
import dev.southside.event.events.PlayerTickEvent;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.KeepSprint;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({PlayerEntity.class})
public abstract class MixinPlayerEntity extends Entity {
   public MixinPlayerEntity(EntityType<?> type, World world) {
      super(type, world);
   }

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTick(CallbackInfo ci) {
      PlayerTickEvent event = new PlayerTickEvent();
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"getAttackCooldownProgress(F)F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCooldown(float baseTime, CallbackInfoReturnable<Float> cir) {
      if (Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         cir.setReturnValue(1.0F);
      }
   }

   @Inject(
      method = {"getAttackCooldownProgressPerTick()F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCooldownPreTick(CallbackInfoReturnable<Float> cir) {
      if (Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         cir.setReturnValue(1.0F);
      }
   }

   @ModifyExpressionValue(
      method = {"attack(Lnet/minecraft/entity/Entity;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"
      )}
   )
   private float hookFixRotation(float original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @ModifyArgs(
      method = {"attack(Lnet/minecraft/entity/Entity;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
      )
   )
   private void hookSlow(Args args) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         if (KeepSprint.canKeepSprint()) {
            args.set(0, this.getVelocity());
         }
      }
   }

   @ModifyArgs(
      method = {"attack(Lnet/minecraft/entity/Entity;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"
      )
   )
   private void hookSlowSprint(Args args) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         if (KeepSprint.canKeepSprint() || KillAura.keepSprint.getValue()) {
            args.set(0, true);
         }
      }
   }

   @WrapWithCondition(
      method = {"attack(Lnet/minecraft/entity/Entity;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
         ordinal = 0
      )}
   )
   private boolean hookSlowVelocity(PlayerEntity instance, Vec3d vec3d) {
      return (Object)this != MinecraftClient.getInstance().player || !KeepSprint.canKeepSprint();
   }

   @WrapWithCondition(
      method = {"attack(Lnet/minecraft/entity/Entity;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V",
         ordinal = 0
      )}
   )
   private boolean hookSlowVelocity(PlayerEntity instance, boolean b) {
      return (Object)this != MinecraftClient.getInstance().player ? true : !KeepSprint.canKeepSprint() || b;
   }
}
