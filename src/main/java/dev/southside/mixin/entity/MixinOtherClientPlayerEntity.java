package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.mixininterfaces.IBoxUpdate;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_745;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_745.class})
public abstract class MixinOtherClientPlayerEntity extends class_1297 implements IBoxUpdate {
   @Unique
   private int boxTrackingIncrements;
   @Unique
   private class_243 boxPos;
   @Unique
   private class_243 serverBoxPos;

   public MixinOtherClientPlayerEntity() {
      super(null, null);
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/class_638;Lcom/mojang/authlib/GameProfile;)V"},
      at = {@At("TAIL")}
   )
   private void injectBoxPosInit(class_638 clientWorld, GameProfile gameProfile, CallbackInfo ci) {
      this.boxPos = this.serverBoxPos = class_243.field_1353;
   }

   @Redirect(
      method = {"method_6007()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_745;method_52532(IDDDDD)V"
      )
   )
   private void redirectLerp(class_745 instance, int trackingIncrements, double serverX, double serverY, double serverZ, double serverYaw, double serverPitch) {
      double d = 1.0 / trackingIncrements;
      double e = class_3532.method_16436(d, this.method_23317(), serverX);
      double f = class_3532.method_16436(d, this.method_23318(), serverY);
      double g = class_3532.method_16436(d, this.method_23321(), serverZ);
      float h = (float)class_3532.method_52468(d, this.method_36454(), serverYaw);
      float i = (float)class_3532.method_16436(d, this.method_36455(), serverPitch);
      this.method_23327(e, f, g);
      this.method_5710(h, i);
   }

   @Inject(
      method = {"method_6007()V"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/class_745;field_6265:I",
         opcode = 180,
         ordinal = 0,
         shift = Shift.BEFORE
      )}
   )
   private void injectBoxLerp(CallbackInfo ci) {
      if (this.boxTrackingIncrements > 0) {
         double d = 1.0 / this.boxTrackingIncrements;
         double e = class_3532.method_16436(d, this.boxPos.field_1352, this.serverBoxPos.field_1352);
         double f = class_3532.method_16436(d, this.boxPos.field_1351, this.serverBoxPos.field_1351);
         double g = class_3532.method_16436(d, this.boxPos.field_1350, this.serverBoxPos.field_1350);
         this.boxPos = new class_243(e, f, g);
         this.method_5857(this.field_18065.method_30757(this.boxPos));
         this.boxTrackingIncrements--;
      }
   }

   @Override
   public class_243 getServerBoxPos() {
      return this.serverBoxPos;
   }

   @Override
   public class_243 getBoxPos() {
      return this.boxPos;
   }

   @Override
   public void setServerBoxPos(class_243 pos) {
      this.serverBoxPos = pos;
   }

   @Override
   public void setBoxPos(class_243 pos) {
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
