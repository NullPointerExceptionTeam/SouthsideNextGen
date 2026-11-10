package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.Southside;
import dev.southside.event.events.JumpEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class MixinLivingEntity extends Entity {
   @Unique
   public boolean wasDesyncing = false;
   @Unique
   private TrackedPosition serverTrackedPos;
   @Unique
   private Vec3d serverPos = Vec3d.ZERO;
   @Unique
   public int legitTrackingTick = 0;
   @Shadow
   protected double serverX;
   @Shadow
   protected double serverY;
   @Shadow
   protected double serverZ;
   @Shadow
   protected double serverYaw;
   @Shadow
   protected double serverPitch;
   @Shadow
   protected int bodyTrackingIncrements;
   @Unique
   private JumpEvent jumpEvent;

   public MixinLivingEntity() {
      super(null, null);
   }

   @Shadow
   public abstract float getYaw(float var1);

   @Inject(
      method = {"jump()V"},
      at = {@At("HEAD")}
   )
   private void onJump(CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && (LivingEntity)(Object)this == MinecraftClient.getInstance().player) {
         this.jumpEvent = new JumpEvent(this.getYaw());
         Southside.get().getEventBus().post(this.jumpEvent);
      }
   }

   @ModifyExpressionValue(
      method = {"jump()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
      )}
   )
   private float hookFixRotation(float original) {
      return this.jumpEvent != null ? this.jumpEvent.getYaw() : original;
   }

   @ModifyExpressionValue(
      method = {"tick()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
      )},
      slice = {@Slice(
         to = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getYaw()F",
            ordinal = 1
         )
      )}
   )
   private float hookBodyRotationsA(float original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @ModifyExpressionValue(
      method = {"turnHead(FF)F"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"
      )}
   )
   private float hookBodyRotationsB(float original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @Inject(
      method = {"getHandSwingDuration()I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void modifySwingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         if (Southside.get().getModuleManager().isEnabled(OldHitting.class)) {
            OldHitting oldHitting = Southside.get().getModuleManager().get(OldHitting.class);
            double speed = oldHitting.speed.getDouble();
            if (speed != 0.0) {
               cir.setReturnValue((int)Math.max(1.0, ((Integer)cir.getReturnValue()).intValue() / Math.abs(speed)));
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = {"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"
      )}
   )
   private float hookModifyFallFlyingPitch(float original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().pitch : original;
      }
   }

   @ModifyExpressionValue(
      method = {"calcGlidingVelocity(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"
      )}
   )
   private Vec3d hookModifyFallFlyingRotationVector(Vec3d original) {
      if ((Object)this != MinecraftClient.getInstance().player) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().getRotationVec() : original;
      }
   }

   @Inject(
      method = {"tickMovement()V"},
      at = {@At("HEAD")}
   )
   public void onTickMove(CallbackInfo ci) {
      if (this.serverTrackedPos == null) {
         this.serverTrackedPos = new TrackedPosition();
      }

      this.serverTrackedPos.setPos(this.getTrackedPosition().getPos());
      if (ServerPacketManager.deSyncing && !this.wasDesyncing) {
         this.legitTrackingTick = this.bodyTrackingIncrements;
      }

      if (ServerPacketManager.deSyncing && this.legitTrackingTick > 0) {
         this.legitTrackingTick--;
      }

      this.wasDesyncing = ServerPacketManager.deSyncing;
   }

   @Inject(
      method = {"updateTrackedPositionAndAngles(DDDFFI)V"},
      at = {@At("TAIL")}
   )
   public void onUpdatePos(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
      if ((Object)this instanceof OtherClientPlayerEntity otherClientPlayer) {
         IBoxUpdate player = (IBoxUpdate)otherClientPlayer;
         if (ServerPacketManager.deSyncing) {
            synchronized (ServerPacketManager.packets) {
               ServerPacketManager.packets.add((Runnable)() -> {
                  player.setServerBoxPos(new Vec3d(x, y, z));
                  player.setBoxTrackingIncrements(interpolationSteps);
               });
            }
         } else {
            player.setServerBoxPos(new Vec3d(x, y, z));
            player.setBoxTrackingIncrements(interpolationSteps);
         }
      }
   }

   @Inject(
      method = {"resetLerp()V"},
      at = {@At("HEAD")}
   )
   private void hookResetLerp(CallbackInfo ci) {
      if ((Object)this instanceof OtherClientPlayerEntity otherClientPlayer) {
         IBoxUpdate player = (IBoxUpdate)otherClientPlayer;
         if (ServerPacketManager.deSyncing) {
            synchronized (ServerPacketManager.packets) {
               ServerPacketManager.packets.add((Runnable)() -> player.setBoxTrackingIncrements(0));
            }
         } else {
            player.setBoxTrackingIncrements(0);
         }
      }
   }
}
