package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.southside.Southside;
import dev.southside.event.events.PlayerTickEvent;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.KeepSprint;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({class_1657.class})
public abstract class MixinPlayerEntity extends class_1297 {
   public MixinPlayerEntity(class_1299<?> type, class_1937 world) {
      super(type, world);
   }

   @Inject(
      method = {"method_5773()V"},
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
      method = {"method_7261(F)F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCooldown(float baseTime, CallbackInfoReturnable<Float> cir) {
      if (Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         cir.setReturnValue(1.0F);
      }
   }

   @Inject(
      method = {"method_7279()F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCooldownPreTick(CallbackInfoReturnable<Float> cir) {
      if (Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
         cir.setReturnValue(1.0F);
      }
   }

   @ModifyExpressionValue(
      method = {"method_7324(Lnet/minecraft/class_1297;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_36454()F"
      )}
   )
   private float hookFixRotation(float original) {
      if ((Object)this != class_310.method_1551().field_1724) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @ModifyArgs(
      method = {"method_7324(Lnet/minecraft/class_1297;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_18799(Lnet/minecraft/class_243;)V"
      )
   )
   private void hookSlow(Args args) {
      if ((Object)this == class_310.method_1551().field_1724) {
         if (KeepSprint.canKeepSprint()) {
            args.set(0, this.method_18798());
         }
      }
   }

   @ModifyArgs(
      method = {"method_7324(Lnet/minecraft/class_1297;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_5728(Z)V"
      )
   )
   private void hookSlowSprint(Args args) {
      if ((Object)this == class_310.method_1551().field_1724) {
         if (KeepSprint.canKeepSprint() || KillAura.keepSprint.getValue()) {
            args.set(0, true);
         }
      }
   }

   @WrapWithCondition(
      method = {"method_7324(Lnet/minecraft/class_1297;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_18799(Lnet/minecraft/class_243;)V",
         ordinal = 0
      )}
   )
   private boolean hookSlowVelocity(class_1657 instance, class_243 vec3d) {
      return (Object)this != class_310.method_1551().field_1724 || !KeepSprint.canKeepSprint();
   }

   @WrapWithCondition(
      method = {"method_7324(Lnet/minecraft/class_1297;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1657;method_5728(Z)V",
         ordinal = 0
      )}
   )
   private boolean hookSlowVelocity(class_1657 instance, boolean b) {
      return (Object)this != class_310.method_1551().field_1724 ? true : !KeepSprint.canKeepSprint() || b;
   }
}
