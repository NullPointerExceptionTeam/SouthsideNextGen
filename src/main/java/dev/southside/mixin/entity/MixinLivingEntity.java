package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.southside.Southside;
import dev.southside.event.events.JumpEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_7422;
import net.minecraft.class_745;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_1309.class})
public abstract class MixinLivingEntity extends class_1297 {
   @Unique
   public boolean wasDesyncing = false;
   @Unique
   private class_7422 serverTrackedPos;
   @Unique
   private class_243 serverPos = class_243.field_1353;
   @Unique
   public int legitTrackingTick = 0;
   @Shadow
   protected double field_6224;
   @Shadow
   protected double field_6245;
   @Shadow
   protected double field_6263;
   @Shadow
   protected double field_6284;
   @Shadow
   protected double field_6221;
   @Shadow
   protected int field_6210;
   @Unique
   private JumpEvent jumpEvent;

   public MixinLivingEntity() {
      super(null, null);
   }

   @Shadow
   public abstract float method_5705(float var1);

   @Inject(
      method = {"method_6043()V"},
      at = {@At("HEAD")}
   )
   private void onJump(CallbackInfo ci) {
      if (class_310.method_1551().field_1724 != null && (class_1309)(Object)this == class_310.method_1551().field_1724) {
         this.jumpEvent = new JumpEvent(this.method_36454());
         Southside.get().getEventBus().post(this.jumpEvent);
      }
   }

   @ModifyExpressionValue(
      method = {"method_6043()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_36454()F"
      )}
   )
   private float hookFixRotation(float original) {
      return this.jumpEvent != null ? this.jumpEvent.getYaw() : original;
   }

   @ModifyExpressionValue(
      method = {"method_5773()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_36454()F"
      )},
      slice = {@Slice(
         to = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/class_1309;method_36454()F",
            ordinal = 1
         )
      )}
   )
   private float hookBodyRotationsA(float original) {
      if ((Object)this != class_310.method_1551().field_1724) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @ModifyExpressionValue(
      method = {"method_6031(FF)F"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_36454()F"
      )}
   )
   private float hookBodyRotationsB(float original) {
      if ((Object)this != class_310.method_1551().field_1724) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().yaw : original;
      }
   }

   @Inject(
      method = {"method_6028()I"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void modifySwingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == class_310.method_1551().field_1724) {
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
      method = {"method_61430(Lnet/minecraft/class_243;)Lnet/minecraft/class_243;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_36455()F"
      )}
   )
   private float hookModifyFallFlyingPitch(float original) {
      if ((Object)this != class_310.method_1551().field_1724) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().pitch : original;
      }
   }

   @ModifyExpressionValue(
      method = {"method_61430(Lnet/minecraft/class_243;)Lnet/minecraft/class_243;"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1309;method_5720()Lnet/minecraft/class_243;"
      )}
   )
   private class_243 hookModifyFallFlyingRotationVector(class_243 original) {
      if ((Object)this != class_310.method_1551().field_1724) {
         return original;
      } else if (!RotationUtils.isStrafeFix()) {
         return original;
      } else {
         return RotationUtils.getRotation() != null ? RotationUtils.getRotation().getRotationVec() : original;
      }
   }

   @Inject(
      method = {"method_6007()V"},
      at = {@At("HEAD")}
   )
   public void onTickMove(CallbackInfo ci) {
      if (this.serverTrackedPos == null) {
         this.serverTrackedPos = new class_7422();
      }

      this.serverTrackedPos.method_43494(this.method_43389().method_60933());
      if (ServerPacketManager.deSyncing && !this.wasDesyncing) {
         this.legitTrackingTick = this.field_6210;
      }

      if (ServerPacketManager.deSyncing && this.legitTrackingTick > 0) {
         this.legitTrackingTick--;
      }

      this.wasDesyncing = ServerPacketManager.deSyncing;
   }

   @Inject(
      method = {"method_5759(DDDFFI)V"},
      at = {@At("TAIL")}
   )
   public void onUpdatePos(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci) {
      if ((Object)this instanceof class_745 otherClientPlayer) {
         IBoxUpdate player = (IBoxUpdate)otherClientPlayer;
         if (ServerPacketManager.deSyncing) {
            synchronized (ServerPacketManager.packets) {
               ServerPacketManager.packets.add((Runnable)() -> {
                  player.setServerBoxPos(new class_243(x, y, z));
                  player.setBoxTrackingIncrements(interpolationSteps);
               });
            }
         } else {
            player.setServerBoxPos(new class_243(x, y, z));
            player.setBoxTrackingIncrements(interpolationSteps);
         }
      }
   }

   @Inject(
      method = {"method_64610()V"},
      at = {@At("HEAD")}
   )
   private void hookResetLerp(CallbackInfo ci) {
      if ((Object)this instanceof class_745 otherClientPlayer) {
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
