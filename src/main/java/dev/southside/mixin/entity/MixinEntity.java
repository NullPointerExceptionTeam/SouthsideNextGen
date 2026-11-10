package dev.southside.mixin.entity;

import dev.southside.Southside;
import dev.southside.event.events.EntityMoveEvent;
import dev.southside.event.events.PostStrafeEvent;
import dev.southside.event.events.StrafeEvent;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.combat.HitBox;
import dev.southside.module.impl.movement.NoPush;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({Entity.class})
public abstract class MixinEntity {
   @Shadow
   private Vec3d pos;
   @Shadow
   private Box boundingBox;
   @Shadow
   private Vec3d velocity;
   @Shadow
   private int id;
   @Shadow
   private EntityChangeListener changeListener;

   @Shadow
   public abstract int getId();

   @Shadow
   public abstract float getYaw();

   @Shadow
   protected static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      return null;
   }

   @Shadow
   public abstract Vec3d getVelocity();

   @Shadow
   public abstract boolean isFireImmune();

   @Shadow
   public abstract boolean isFrozen();

   @Shadow
   protected abstract void updatePrevPosition();

   @Shadow
   public abstract Box calculateBoundingBox();

   @Shadow
   public abstract void setBoundingBox(Box var1);

   @Shadow
   public abstract Text getName();

   @Shadow
   public abstract void setPos(double var1, double var3, double var5);

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract Box calculateDefaultBoundingBox(Vec3d var1);

   @Shadow
   public abstract void setPosition(Vec3d var1);

   @Redirect(
      method = {"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"
      )
   )
   public boolean fall_disabler(Entity instance) {
      return instance instanceof ClientPlayerEntity ? false : instance.isControlledByPlayer();
   }

   @Redirect(
      method = {"updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"
      )
   )
   public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw) {
      if ((Entity)(Object)this == MinecraftClient.getInstance().player) {
         StrafeEvent event = Southside.INSTANCE.getEventBus().post(new StrafeEvent(yaw));
         return movementInputToVelocity(movementInput, speed, event.getYaw());
      } else {
         return movementInputToVelocity(movementInput, speed, yaw);
      }
   }

   @Inject(
      method = {"updateVelocity(FLnet/minecraft/util/math/Vec3d;)V"},
      at = {@At("TAIL")}
   )
   public void callPostStrafe(float speed, Vec3d movementInput, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().post(new PostStrafeEvent());
   }

   @ModifyArgs(
      method = {"pushAwayFrom(Lnet/minecraft/entity/Entity;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"
      )
   )
   private void onPushAwayFrom(Args args, Entity entity) {
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
      method = {"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void move(MovementType movementType, Vec3d movement, CallbackInfo ci) {
      EntityMoveEvent event = new EntityMoveEvent();
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"getBoundingBox()Lnet/minecraft/util/math/Box;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
      HitBox hitBox = Southside.INSTANCE.getModuleManager().get(HitBox.class);
      if (hitBox.isEnabled()
         && MinecraftClient.getInstance() != null
         && MinecraftClient.getInstance().player != null
         && this.getId() != MinecraftClient.getInstance().player.getId()) {
         cir.setReturnValue(
            new Box(
               this.boundingBox.minX - hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.boundingBox.minY - hitBox.YExpand.getValue().floatValue() / 2.0F,
               this.boundingBox.minZ - hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.boundingBox.maxX + hitBox.XZExpand.getValue().floatValue() / 2.0F,
               this.boundingBox.maxY + hitBox.YExpand.getValue().floatValue() / 2.0F,
               this.boundingBox.maxZ + hitBox.XZExpand.getValue().floatValue() / 2.0F
            )
         );
      }
   }

   @Inject(
      method = {"refreshPosition()V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public final void redirectRefreshPosition(CallbackInfo ci) {
      if ((Object)this instanceof OtherClientPlayerEntity entity) {
         Vec3d boxPos = ((IBoxUpdate)entity).getServerBoxPos();
         if (boxPos == null) {
            return;
         }

         this.setBoundingBox(this.calculateDefaultBoundingBox(boxPos));
         ci.cancel();
      }
   }
}
