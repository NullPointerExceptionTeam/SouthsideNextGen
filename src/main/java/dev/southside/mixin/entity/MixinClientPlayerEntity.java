package dev.southside.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MoveEvent;
import dev.southside.event.events.PlayerSlowdownEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.SprintPacketSendEvent;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.PlayerUtils;
import net.minecraft.class_1313;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2828.class_5911;
import net.minecraft.class_2848.class_2849;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_746.class})
public abstract class MixinClientPlayerEntity extends class_1657 {
   @Shadow
   public int field_3923;
   @Shadow
   public double field_3926;
   @Shadow
   public double field_3940;
   @Shadow
   public double field_3924;
   @Shadow
   public float field_3941;
   @Shadow
   public float field_3925;
   @Shadow
   public boolean field_3920;
   @Shadow
   public boolean field_3919;
   @Shadow
   public boolean field_3936;
   @Shadow
   private boolean field_3927 = true;
   @Shadow
   @Final
   public class_634 field_3944;
   @Shadow
   @Final
   protected class_310 field_3937;
   @Shadow
   private boolean field_53040;

   public MixinClientPlayerEntity(class_1937 world, class_2338 pos, float yaw, GameProfile gameProfile) {
      super(world, pos, yaw, gameProfile);
   }

   @Shadow
   public abstract boolean method_5715();

   @Shadow
   protected abstract boolean method_3134();

   @Inject(
      method = {"method_5773()V"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo callbackInfo) {
   }

   @Inject(
      method = {"method_5773()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_742;method_5773()V",
         shift = Shift.BEFORE,
         ordinal = 0
      )},
      cancellable = true
   )
   private void hookTickEvent(CallbackInfo ci) {
      PlayerUtils.ticksSinceTeleport++;
      PlayerUpdateEvent event = new PlayerUpdateEvent();
      Southside.INSTANCE.getEventBus().call(event);
      class_310.method_1551().method_1508();
      if (PlayerUtils.playerStuckTicks > 0) {
         PlayerUtils.playerStuckTicks--;
         ci.cancel();
      }
   }

   @Overwrite
   private void method_3136() {
      if (!this.method_24828()) {
         PlayerUtils.offGroundTicks++;
         PlayerUtils.onGroundTicks = 0;
      } else {
         PlayerUtils.offGroundTicks = 0;
         PlayerUtils.onGroundTicks++;
         PlayerUtils.lastOnGroundTicks = PlayerUtils.onGroundTicks;
      }

      this.method_46742();
      MotionEvent event = new MotionEvent(
         this.method_23317(),
         this.method_23318(),
         this.method_23321(),
         this.method_36454(),
         this.method_36455(),
         this.method_24828(),
         this.field_5976,
         Stage.PRE
      );
      Southside.INSTANCE.getEventBus().call(event);
      float yaw = Disabler.normalizeYawDiff(event.getYaw(), this.field_3941);
      if (this.method_3134()) {
         double d = event.getX() - this.field_3926;
         double e = event.getY() - this.field_3940;
         double f = event.getZ() - this.field_3924;
         double g = yaw - this.field_3941;
         double h = event.getPitch() - this.field_3925;
         PlayerUtils.lastPitchDiff = Math.abs(h);
         this.field_3923++;
         boolean bl = class_3532.method_41190(d, e, f) > class_3532.method_33723(2.0E-4) || this.field_3923 >= 20;
         boolean bl2 = g != 0.0 || h != 0.0;
         if (bl && bl2) {
            this.field_3944
               .method_52787(new class_2830(event.getX(), event.getY(), event.getZ(), yaw, event.getPitch(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (bl) {
            this.field_3944.method_52787(new class_2829(event.getX(), event.getY(), event.getZ(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (bl2) {
            this.field_3944.method_52787(new class_2831(yaw, event.getPitch(), event.isOnGround(), event.isHorizontalCollision()));
         } else if (this.field_3920 != event.isOnGround() || this.field_53040 != event.isHorizontalCollision()) {
            this.field_3944.method_52787(new class_5911(event.isOnGround(), event.isHorizontalCollision()));
         } else if (event.isShouldSendExtraC03()) {
            this.field_3944.method_52787(new class_5911(event.isOnGround(), event.isHorizontalCollision()));
         } else {
            PacketUtils.callPacket(new class_5911(event.isOnGround(), event.isHorizontalCollision()));
         }

         Disabler.processPackets();
         if (bl) {
            this.field_3926 = event.getX();
            this.field_3940 = event.getY();
            this.field_3924 = event.getZ();
            this.field_3923 = 0;
         }

         if (bl2) {
            this.field_3941 = yaw;
            this.field_3925 = event.getPitch();
         }

         this.field_3920 = event.isOnGround();
         this.field_53040 = event.isHorizontalCollision();
         this.field_3927 = (Boolean)this.field_3937.field_1690.method_42423().method_41753();
         event.setState(Stage.POST);
         Southside.INSTANCE.getEventBus().call(event);
      }
   }

   @ModifyExpressionValue(
      method = {"method_6007()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_6115()Z"
      )}
   )
   private boolean redirectUsingItem(boolean isUsingItem) {
      PlayerSlowdownEvent event = new PlayerSlowdownEvent();
      Southside.INSTANCE.getEventBus().call(event);
      return event.isCancelled() ? false : isUsingItem;
   }

   @Inject(
      method = {"method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_742;method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"
      )},
      cancellable = true
   )
   public void onMoveHook(class_1313 movementType, class_243 movement, CallbackInfo ci) {
      MoveEvent event = new MoveEvent(movement.field_1352, movement.field_1351, movement.field_1350);
      Southside.INSTANCE.getEventBus().call(event);
      if (event.isCancelled()) {
         super.method_5784(movementType, new class_243(event.getX(), event.getY(), event.getZ()));
         ci.cancel();
      }
   }

   @Overwrite
   private void method_46742() {
      SprintPacketSendEvent event = new SprintPacketSendEvent(this.method_5624());
      Southside.INSTANCE.getEventBus().call(event);
      if (!event.isCancelled()) {
         boolean bl = event.isSprinting();
         if (bl != this.field_3919) {
            class_2849 mode = bl ? class_2849.field_12981 : class_2849.field_12985;
            this.field_3944.method_52787(new class_2848(this, mode));
            this.field_3919 = bl;
         }
      }
   }
}
