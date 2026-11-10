package dev.southside.utils.player;

import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.RotationUtils;
import lombok.Generated;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class FallingPlayer {
   private double x;
   private double y;
   private double z;
   private Vec3d motion;
   private Vec3d eyePos;
   private float yaw;
   private float strafe;
   private float forward;
   private float jumpMovementFactor;
   private float movementSpeed;
   private boolean onGround;
   private final MinecraftClient mc = MinecraftClient.getInstance();

   public FallingPlayer(PlayerEntity player) {
      this.x = player.getX();
      this.y = player.getY();
      this.z = player.getZ();
      this.motion = player.getVelocity();
      this.yaw = RotationUtils.getRotationSafely().yaw;
      this.strafe = this.mc.player.input.movementSideways;
      this.forward = this.mc.player.input.movementForward;
      this.movementSpeed = player.getMovementSpeed();
      this.onGround = player.isOnGround();
      this.jumpMovementFactor = player.isSprinting() ? 0.026F : 0.02F;
      this.eyePos = this.mc.player.getEyePos();
   }

   private void calculateForTick() {
      float dragX = 0.91F;
      float dragY = 0.98F;
      float acceleration = this.jumpMovementFactor;
      this.updateVelocity(acceleration, new Vec3d(this.strafe, 0.0, this.forward));
      this.x = this.x + this.motion.x;
      this.y = this.y + this.motion.y;
      this.z = this.z + this.motion.z;
      this.updateGroundState();
      double gravity = 0.08;
      this.motion = this.motion.add(0.0, -gravity, 0.0);
      this.eyePos = new Vec3d(this.x, this.y + this.mc.player.dimensions.eyeHeight(), this.z);
      this.motion = new Vec3d(this.motion.x * dragX, this.motion.y * dragY, this.motion.z * dragX);
   }

   private void updateGroundState() {
      Vec3d center = new Vec3d(this.x, this.y, this.z);
      Vec3d down = center.add(0.0, -0.2, 0.0);
      BlockHitResult result = this.rayTraceHit(center, down);
      if (result != null && result.getType() == Type.BLOCK && result.getSide() == Direction.UP) {
         this.onGround = true;
      } else {
         this.onGround = false;
      }
   }

   private void updateVelocity(float speed, Vec3d input) {
      double lengthSquared = input.lengthSquared();
      if (!(lengthSquared < 1.0E-7)) {
         Vec3d normalizedInput = (lengthSquared > 1.0 ? input.normalize() : input).multiply(speed);
         float f = MathHelper.sin(this.yaw * (float) (Math.PI / 180.0));
         float g = MathHelper.cos(this.yaw * (float) (Math.PI / 180.0));
         double inputX = normalizedInput.x * g - normalizedInput.z * f;
         double inputZ = normalizedInput.z * g + normalizedInput.x * f;
         this.motion = this.motion.add(inputX, 0.0, inputZ);
      }
   }

   public void calculate(int ticks) {
      for (int i = 0; i < ticks; i++) {
         this.calculateForTick();
      }
   }

   public BlockPos findCollision(int ticks) {
      float w = this.mc.player != null ? this.mc.player.getWidth() / 2.0F : 0.3F;

      for (int i = 0; i < ticks; i++) {
         Vec3d start = new Vec3d(this.x, this.y, this.z);
         this.calculateForTick();
         Vec3d end = new Vec3d(this.x, this.y, this.z);
         BlockPos raytracedBlock;
         if ((raytracedBlock = this.rayTrace(start, end)) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(w, 0.0, w), end.add(w, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(-w, 0.0, w), end.add(-w, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(w, 0.0, -w), end.add(w, 0.0, -w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(-w, 0.0, -w), end.add(-w, 0.0, -w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(w, 0.0, 0.0), end.add(w, 0.0, 0.0))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(-w, 0.0, 0.0), end.add(-w, 0.0, 0.0))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(0.0, 0.0, w), end.add(0.0, 0.0, w))) != null) {
            return raytracedBlock;
         }

         if ((raytracedBlock = this.rayTrace(start.add(0.0, 0.0, -w), end.add(0.0, 0.0, -w))) != null) {
            return raytracedBlock;
         }
      }

      return null;
   }

   private BlockHitResult rayTraceHit(Vec3d start, Vec3d end) {
      double distance = start.distanceTo(end);
      return distance < 1.0E-7 ? null : RayCastUtils.raycast(distance, RotationUtils.getRotationByVector(start, end, false), false, 1.0F);
   }

   private BlockPos rayTrace(Vec3d start, Vec3d end) {
      BlockHitResult result = this.rayTraceHit(start, end);
      return result != null && result.getType() == Type.BLOCK && result.getSide() == Direction.UP ? result.getBlockPos() : null;
   }

   public Vec3d getEyePos() {
      return this.eyePos.add(0.0);
   }

   public Vec3d getPos() {
      return new Vec3d(this.x, this.y, this.z);
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
   public Vec3d getMotion() {
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
   public MinecraftClient getMc() {
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
   public void setMotion(Vec3d motion) {
      this.motion = motion;
   }

   @Generated
   public void setEyePos(Vec3d eyePos) {
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
