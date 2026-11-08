package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.event.events.EntityMoveEvent;
import dev.southside.event.events.PostStrafeEvent;
import dev.southside.event.events.StrafeEvent;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.combat.HitBox;
import dev.southside.module.impl.movement.NoPush;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5569;
import net.minecraft.class_745;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({class_1297.class})
public abstract class MixinEntity {
   @Shadow
   private class_243 field_22467;
   @Shadow
   private class_238 field_6005;
   @Shadow
   private class_243 field_18276;
   @Shadow
   private int field_5986;
   @Shadow
   private class_5569 field_26996;

   @Shadow
   public abstract int method_5628();

   @Shadow
   public abstract float method_36454();

   @Shadow
   protected static class_243 method_18795(class_243 movementInput, float speed, float yaw) {
      return null;
   }

   @Shadow
   public abstract class_243 method_18798();

   @Shadow
   public abstract boolean method_5753();

   @Shadow
   public abstract boolean method_32314();

   @Shadow
   protected abstract void method_63613();

   @Shadow
   public abstract class_238 method_33332();

   @Shadow
   public abstract void method_5857(class_238 var1);

   @Shadow
   public abstract class_2561 method_5477();

   @Shadow
   public abstract void method_23327(double var1, double var3, double var5);

   @Shadow
   protected abstract void method_5710(float var1, float var2);

   @Shadow
   protected abstract class_238 method_65341(class_243 var1);

   @Shadow
   public abstract void method_33574(class_243 var1);

   @Redirect(
      method = {"method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_65038()Z"
      )
   )
   public boolean fall_disabler(class_1297 instance) {
      return instance instanceof class_746 ? false : instance.method_65038();
   }

   @Redirect(
      method = {"method_5724(FLnet/minecraft/class_243;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_18795(Lnet/minecraft/class_243;FF)Lnet/minecraft/class_243;"
      )
   )
   public class_243 hookVelocity(class_243 movementInput, float speed, float yaw) {
      if ((class_1297)(Object)this == class_310.method_1551().field_1724) {
         StrafeEvent event = Southside.INSTANCE.getEventBus().post(new StrafeEvent(yaw));
         return method_18795(movementInput, speed, event.getYaw());
      } else {
         return method_18795(movementInput, speed, yaw);
      }
   }

   @Inject(
      method = {"method_5724(FLnet/minecraft/class_243;)V"},
      at = {@At("TAIL")}
   )
   public void callPostStrafe(float speed, class_243 movementInput, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().post(new PostStrafeEvent());
   }

   @ModifyArgs(
      method = {"method_5697(Lnet/minecraft/class_1297;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1297;method_5762(DDD)V"
      )
   )
   private void onPushAwayFrom(Args args, class_1297 entity) {
      if (Southside.INSTANCE.getModuleManager().get(NoPush.class).isEnabled()) {
         args.set(0, 0.0);
         args.set(2, 0.0);
      }

      if (entity instanceof FakePlayerEntity player && player.doNotPush) {
         args.set(0, 0.0);
         args.set(2, 0.0);
      }
   }

   @Inject(
      method = {"method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void move(class_1313 movementType, class_243 movement, CallbackInfo ci) {
      EntityMoveEvent event = new EntityMoveEvent();
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_5829()Lnet/minecraft/class_238;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public final void getBoundingBox(CallbackInfoReturnable<class_238> cir) {
      HitBox hitBox = Southside.INSTANCE.getModuleManager().get(HitBox.class);
      if (hitBox.isEnabled()
         && class_310.method_1551() != null
         && class_310.method_1551().field_1724 != null
         && this.method_5628() != class_310.method_1551().field_1724.method_5628()) {
         cir.setReturnValue(
            new class_238(
               this.field_6005.field_1323 - hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.field_6005.field_1322 - hitBox.YExpand.getValue().floatValue() / 2.0F,
               this.field_6005.field_1321 - hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.field_6005.field_1320 + hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.field_6005.field_1325 + hitBox.YExpand.getValue().floatValue() / 2.0F,
               this.field_6005.field_1324 + hitBox.XZExpand.getValue().floatValue() / 2.0F
            )
         );
      }
   }

   @Inject(
      method = {"method_23311()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public final void redirectRefreshPosition(CallbackInfo ci) {
      if ((Object)this instanceof class_745 entity) {
         class_243 boxPos = ((IBoxUpdate)entity).getServerBoxPos();
         if (boxPos == null) {
            return;
         }

         this.method_5857(this.method_65341(boxPos));
         ci.cancel();
      }
   }
}
