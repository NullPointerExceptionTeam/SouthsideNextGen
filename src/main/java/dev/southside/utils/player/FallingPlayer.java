package dev.southside.utils.player;

import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.RotationUtils;
import lombok.Generated;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class FallingPlayer {
   private double x;
   private double y;
   private double z;
   private class_243 motion;
   private class_243 eyePos;
   private float yaw;
   private float strafe;
   private float forward;
   private float jumpMovementFactor;
   private float movementSpeed;
   private boolean onGround;
   private final class_310 mc = class_310.method_1551();

   public FallingPlayer(class_1657 player) {
      this.x = player.method_23317();
      this.y = player.method_23318();
      this.z = player.method_23321();
      this.motion = player.method_18798();
      this.yaw = RotationUtils.getRotationSafely().yaw;
      this.strafe = this.mc.field_1724.field_3913.field_3907;
      this.forward = this.mc.field_1724.field_3913.field_3905;
      this.movementSpeed = player.method_6029();
      this.onGround = player.method_24828();
      this.jumpMovementFactor = player.method_5624() ? 0.026F : 0.02F;
      this.eyePos = this.mc.field_1724.method_33571();
   }

   private void calculateForTick() {
      float dragX = 0.91F;
      float dragY = 0.98F;
      float acceleration = this.jumpMovementFactor;
      this.updateVelocity(acceleration, new class_243(this.strafe, 0.0, this.forward));
      this.x = this.x + this.motion.field_1352;
      this.y = this.y + this.motion.field_1351;
      this.z = this.z + this.motion.field_1350;
      this.updateGroundState();
      double gravity = 0.08;
      this.motion = this.motion.method_1031(0.0, -gravity, 0.0);
      this.eyePos = new class_243(this.x, this.y + this.mc.field_1724.field_18065.comp_2187(), this.z);
      this.motion = new class_243(this.motion.field_1352 * dragX, this.motion.field_1351 * dragY, this.motion.field_1350 * dragX);
   }

   private void updateGroundState() {
      class_243 center = new class_243(this.x, this.y, this.z);
      class_243 down = center.method_1031(0.0, -0.2, 0.0);
      class_3965 result = this.rayTraceHit(center, down);
      if (result != null && result.method_17783() == class_240.field_1332 && result.method_17780() == class_2350.field_11036) {
         this.onGround = true;
      } else {
         this.onGround = false;
      }
   }

   private void updateVelocity(float speed, class_243 input) {
      double lengthSquared = input.method_1027();
      if (!(lengthSquared < 1.0E-7)) {
         class_243 normalizedInput = (lengthSquared > 1.0 ? input.method_1029() : input).method_1021(speed);
         float f = class_3532.method_15374(this.yaw * (float) (Math.PI / 180.0));
         float g = class_3532.method_15362(this.yaw * (float) (Math.PI / 180.0));
         double inputX = normalizedInput.field_1352 * g - normalizedInput.field_1350 * f;
         double inputZ = normalizedInput.field_1350 * g + normalizedInput.field_1352 * f;
         this.motion = this.motion.method_1031(inputX, 0.0, inputZ);
      }
   }

   public void calculate(int ticks) {
      for (int i = 0; i < ticks; i++) {
         this.calculateForTick();
      }
   }

   public class_2338 findCollision(int ticks) {
      float w = this.mc.field_1724 != null ? this.mc.field_1724.method_17681() / 2.0F : 0.3F;

      for (int i = 0; i < ticks; i++) {
         class_243 start = new class_243(this.x, this.y, this.z);
         this.calculateForTick();
         class_243 end = new class_243(this.x, this.y, this.z);
         class_2338 raytracedBlock;
         if ((raytracedBlock = this.rayTrace(start, end)) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(w, 0.0, w), end.method_1031(w, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(-w, 0.0, w), end.method_1031(-w, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(w, 0.0, -w), end.method_1031(w, 0.0, -w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(-w, 0.0, -w), end.method_1031(-w, 0.0, -w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(w, 0.0, 0.0), end.method_1031(w, 0.0, 0.0))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(-w, 0.0, 0.0), end.method_1031(-w, 0.0, 0.0))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(0.0, 0.0, w), end.method_1031(0.0, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.method_1031(0.0, 0.0, -w), end.method_1031(0.0, 0.0, -w))) != null) {
            return raytracedBlock;
         }
      }

      return null;
   }

   private class_3965 rayTraceHit(class_243 start, class_243 end) {
      double distance = start.method_1022(end);
      return distance < 1.0E-7 ? null : RayCastUtils.raycast(distance, RotationUtils.getRotationByVector(start, end, false), false, 1.0F);
   }

   private class_2338 rayTrace(class_243 start, class_243 end) {
      class_3965 result = this.rayTraceHit(start, end);
      return result != null && result.method_17783() == class_240.field_1332 && result.method_17780() == class_2350.field_11036 ? result.method_17777() : null;
   }

   public class_243 getEyePos() {
      return this.eyePos.method_61889(0.0);
   }

   public class_243 getPos() {
      return new class_243(this.x, this.y, this.z);
   }

   @Generated
   public double getX() {
      return this.x;
   }

   @Generated
   public double getY() {
      return this.y;
   }

   @Generated
   public double getZ() {
      return this.z;
   }

   @Generated
   public class_243 getMotion() {
      return this.motion;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getStrafe() {
      return this.strafe;
   }

   @Generated
   public float getForward() {
      return this.forward;
   }

   @Generated
   public float getJumpMovementFactor() {
      return this.jumpMovementFactor;
   }

   @Generated
   public float getMovementSpeed() {
      return this.movementSpeed;
   }

   @Generated
   public boolean isOnGround() {
      return this.onGround;
   }

   @Generated
   public class_310 getMc() {
      return this.mc;
   }

   @Generated
   public void setX(double x) {
      this.x = x;
   }

   @Generated
   public void setY(double y) {
      this.y = y;
   }

   @Generated
   public void setZ(double z) {
      this.z = z;
   }

   @Generated
   public void setMotion(class_243 motion) {
      this.motion = motion;
   }

   @Generated
   public void setEyePos(class_243 eyePos) {
      this.eyePos = eyePos;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   @Generated
   public void setForward(float forward) {
      this.forward = forward;
   }

   @Generated
   public void setJumpMovementFactor(float jumpMovementFactor) {
      this.jumpMovementFactor = jumpMovementFactor;
   }

   @Generated
   public void setMovementSpeed(float movementSpeed) {
      this.movementSpeed = movementSpeed;
   }

   @Generated
   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
