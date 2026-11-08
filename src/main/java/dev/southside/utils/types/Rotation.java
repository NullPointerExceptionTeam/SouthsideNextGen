package dev.southside.utils.types;

import dev.southside.MinecraftAccessor;
import lombok.Generated;
import net.minecraft.class_243;
import net.minecraft.class_3532;

public final class Rotation {
   public float yaw;
   public float pitch;

   public static Rotation from(class_243 origin, class_243 target) {
      double dx = target.field_1352 - origin.field_1352;
      double dy = target.field_1351 - origin.field_1351;
      double dz = target.field_1350 - origin.field_1350;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
      return new Rotation(yaw, pitch);
   }

   public class_243 getRotationVec() {
      float yawCos = class_3532.method_15362(-this.yaw * (float) (Math.PI / 180.0));
      float yawSin = class_3532.method_15374(-this.yaw * (float) (Math.PI / 180.0));
      float pitchCos = class_3532.method_15362(this.pitch * (float) (Math.PI / 180.0));
      float pitchSin = class_3532.method_15374(this.pitch * (float) (Math.PI / 180.0));
      return new class_243(yawSin * pitchCos, -pitchSin, yawCos * pitchCos);
   }

   public float distance(Rotation other) {
      return (float)Math.hypot(this.getAngleDifference(this.yaw, other.pitch), this.pitch - other.pitch);
   }

   private float getAngleDifference(float a, float b) {
      return ((a - b) % 360.0F + 540.0F) % 360.0F - 180.0F;
   }

   public Rotation copy() {
      return new Rotation(this.yaw, this.pitch);
   }

   public void apply() {
      MinecraftAccessor.mc.field_1724.method_36456(this.yaw);
      MinecraftAccessor.mc.field_1724.method_36457(this.pitch);
   }

   public void fixPitch() {
      if (this.pitch > 90.0F) {
         this.pitch = 90.0F;
      }

      if (this.pitch < -90.0F) {
         this.pitch = -90.0F;
      }
   }

   public void fixedSensitivity(float sensitivity) {
      float f = sensitivity * 0.6F + 0.2F;
      float gcd = f * f * f * 1.2F;
      this.yaw = this.yaw - this.yaw % gcd;
      this.pitch = this.pitch - this.pitch % gcd;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   public Rotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
