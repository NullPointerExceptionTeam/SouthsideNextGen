package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.mixininterfaces.IBoxUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OtherClientPlayerEntity.class})
public abstract class MixinOtherClientPlayerEntity extends Entity implements IBoxUpdate {
   @Unique
   private int boxTrackingIncrements;
   @Unique
   private Vec3d boxPos;
   @Unique
   private Vec3d serverBoxPos;

   public MixinOtherClientPlayerEntity() {
      super(null, null);
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/client/world/ClientWorld;Lcom/mojang/authlib/GameProfile;)V"},
      at = {@At("TAIL")}
   )
   private void injectBoxPosInit(ClientWorld clientWorld, GameProfile gameProfile, CallbackInfo ci) {
      this.boxPos = this.serverBoxPos = Vec3d.ZERO;
   }

   @Redirect(
      method = {"tickMovement()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;lerpPosAndRotation(IDDDDD)V"
      )
   )
   private void redirectLerp(OtherClientPlayerEntity instance, int trackingIncrements, double serverX, double serverY, double serverZ, double serverYaw, double serverPitch) {
      double d = 1.0 / trackingIncrements;
      double e = MathHelper.lerp(d, this.getX(), serverX);
      double f = MathHelper.lerp(d, this.getY(), serverY);
      double g = MathHelper.lerp(d, this.getZ(), serverZ);
      float h = (float)MathHelper.lerpAngleDegrees(d, this.getYaw(), serverYaw);
      float i = (float)MathHelper.lerp(d, this.getPitch(), serverPitch);
      this.setPos(e, f, g);
      this.setRotation(h, i);
   }

   @Inject(
      method = {"tickMovement()V"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/network/OtherClientPlayerEntity;headTrackingIncrements:I",
         opcode = 180,
         ordinal = 0,
         shift = Shift.BEFORE
      )}
   )
   private void injectBoxLerp(CallbackInfo ci) {
      if (this.boxTrackingIncrements > 0) {
         double d = 1.0 / this.boxTrackingIncrements;
         double e = MathHelper.lerp(d, this.boxPos.x, this.serverBoxPos.x);
         double f = MathHelper.lerp(d, this.boxPos.y, this.serverBoxPos.y);
         double g = MathHelper.lerp(d, this.boxPos.z, this.serverBoxPos.z);
         this.boxPos = new Vec3d(e, f, g);
         this.setBoundingBox(this.dimensions.getBoxAt(this.boxPos));
         this.boxTrackingIncrements--;
      }
   }

   @Override
   public Vec3d getServerBoxPos() {
      return this.serverBoxPos;
   }

   @Override
   public Vec3d getBoxPos() {
      return this.boxPos;
   }

   @Override
   public void setServerBoxPos(Vec3d pos) {
      this.serverBoxPos = pos;
   }

   @Override
   public void setBoxPos(Vec3d pos) {
      this.boxPos = pos;
   }

   @Override
   public void setBoxTrackingIncrements(int increments) {
      this.boxTrackingIncrements = increments;
   }

   @Override
   public int getBoxTrackingIncrements() {
      return this.boxTrackingIncrements;
   }
}
