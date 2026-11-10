package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.SprintPacketSendEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.mixininterfaces.IVec3d;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.Set;
import lombok.Generated;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.client.input.Input;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class PlayerUtils implements MinecraftAccessor {
   public static int playerStuckTicks = 0;
   public static int offGroundTicks = 0;
   public static int onGroundTicks = 0;
   public static int lastOnGroundTicks = 0;
   public static int ticksSinceTeleport = 0;
   public static int ticksSinceSprint = 0;
   public static double lastPitchDiff = Double.MIN_VALUE;
   public static double lastPlacePitchDiff = Double.MIN_VALUE;
   public static boolean serverSprinting = false;
   private static Vec3d lastVelocity = new Vec3d(0.0, 0.0, 0.0);

   public void init() {
      Southside.INSTANCE.getEventBus().register(this);
   }

   public static boolean isMoving() {
      return mc.player == null ? false : mc.player.sidewaysSpeed != 0.0F || mc.player.forwardSpeed != 0.0F;
   }

   public static void stopSprint() {
      mc.player.setSprinting(false);
      mc.options.sprintKey.setPressed(false);
   }

   public static boolean isSprintMovementKeyHold() {
      return mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed();
   }

   public static PlayerPosition resolvePos(PlayerPosition newPos, Set<PositionFlag> flags) {
      if (mc.player == null) {
         return newPos;
      } else {
         PlayerPosition current = PlayerPosition.fromEntity(mc.player);
         return PlayerPosition.apply(current, newPos, flags);
      }
   }

   public static float getSpeed(PlayerEntity player) {
      return player != null && mc.world != null
         ? (float)Math.sqrt(
            player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z
         )
         : 0.0F;
   }

   public static int getMoveSpeedEffectAmplifier() {
      return mc.player.hasStatusEffect(StatusEffects.SPEED) ? mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
   }

   public static boolean isOnLadder() {
      BlockState state = mc.world.getBlockState(mc.player.getBlockPos());
      return state.isIn(BlockTags.CLIMBABLE)
         || !mc.player.isOnGround() && mc.player.getVelocity().y < 0.0 && state.getBlock() instanceof LadderBlock;
   }

   public static boolean inFov(Entity entity, double fov) {
      if (fov >= 360.0) {
         return true;
      } else {
         Rotation angle = RotationUtils.calculate(mc.player.getCameraPosVec(1.0F), entity.getBoundingBox().getCenter());
         double xDist = MathHelper.angleBetween(angle.yaw, mc.player.getYaw());
         double yDist = MathHelper.angleBetween(angle.pitch, mc.player.getPitch());
         double angleDistance = Math.hypot(xDist, yDist);
         return angleDistance <= fov;
      }
   }

   public static boolean isAboveVoid() {
      if (mc.player == null) {
         return false;
      } else if (mc.player.getY() < 0.0) {
         return true;
      } else {
         for (int i = (int)(mc.player.getY() - 1.0); i >= 1; i--) {
            if (!mc.world.getBlockState(BlockPos.ofFloored(mc.player.getX(), i, mc.player.getZ())).isAir()) {
               return false;
            }
         }

         return !mc.player.isOnGround();
      }
   }

   public static double[] forward(double d) {
      float f = mc.player.input.movementForward;
      float f2 = mc.player.input.movementSideways;
      float f3 = mc.player.getYaw();
      if (f != 0.0F) {
         if (f2 > 0.0F) {
            f3 += f > 0.0F ? -45 : 45;
         } else if (f2 < 0.0F) {
            f3 += f > 0.0F ? 45 : -45;
         }

         f2 = 0.0F;
         if (f > 0.0F) {
            f = 1.0F;
         } else if (f < 0.0F) {
            f = -1.0F;
         }
      }

      double d2 = Math.sin(Math.toRadians(f3 + 90.0F));
      double d3 = Math.cos(Math.toRadians(f3 + 90.0F));
      double d4 = f * d * d3 + f2 * d * d2;
      double d5 = f * d * d2 - f2 * d * d3;
      return new double[]{d4, d5};
   }

   public static boolean isInLiquid() {
      if (mc.player.isSubmergedInWater()) {
         return true;
      } else {
         boolean inLiquid = false;
         int box = (int)mc.player.getBoundingBox().minY;

         for (int x = MathHelper.floor(mc.player.getBoundingBox().minX);
            x < MathHelper.floor(mc.player.getBoundingBox().maxX) + 1;
            x++
         ) {
            for (int z = MathHelper.floor(mc.player.getBoundingBox().minZ);
               z < MathHelper.floor(mc.player.getBoundingBox().maxZ) + 1;
               z++
            ) {
               BlockPos pos = new BlockPos(x, box, z);
               BlockState state = mc.world.getBlockState(pos);
               Block b = state.getBlock();
               if (b != Blocks.AIR && state.getBlock() != Blocks.AIR) {
                  if (!(b instanceof FluidBlock)) {
                     return false;
                  }

                  inLiquid = true;
               }
            }
         }

         return inLiquid;
      }
   }

   public static boolean canSeeEntity(Entity entity) {
      Vec3d vec1 = new Vec3d(0.0, 0.0, 0.0);
      Vec3d vec2 = new Vec3d(0.0, 0.0, 0.0);
      ((IVec3d)vec1).ss$set(mc.player.getX(), mc.player.getY() + mc.player.getStandingEyeHeight(), mc.player.getZ());
      ((IVec3d)vec2).ss$set(entity.getX(), entity.getY(), entity.getZ());
      boolean canSeeFeet = mc.world.raycast(new RaycastContext(vec1, vec2, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType()
         == Type.MISS;
      ((IVec3d)vec2).ss$set(entity.getX(), entity.getY() + entity.getStandingEyeHeight(), entity.getZ());
      boolean canSeeEyes = mc.world.raycast(new RaycastContext(vec1, vec2, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType()
         == Type.MISS;
      return canSeeFeet || canSeeEyes;
   }

   public static Block blockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
      return mc.world.getBlockState(mc.player.getBlockPos().add((int)offsetX, (int)offsetY, (int)offsetZ)).getBlock();
   }

   public static void strafe(Float speed) {
      if (mc.player != null) {
         if (isMoving()) {
            double yaw = Math.toRadians(getPlayerDirection());
            double xSpeed = -Math.sin(yaw);
            double zSpeed = Math.cos(yaw);
            double magnitude = Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed);
            if (magnitude > 0.0) {
               xSpeed /= magnitude;
               zSpeed /= magnitude;
            }

            mc.player.setVelocity(xSpeed * speed.floatValue(), mc.player.getVelocity().y, zSpeed * speed.floatValue());
         } else {
            stopMoving();
         }
      }
   }

   public static float getPlayerDirection() {
      if (mc.player == null) {
         return 0.0F;
      } else {
         float yaw = mc.player.getYaw();
         float strafe = 45.0F;
         Input input = mc.player.input;
         if (input.movementForward < 0.0F) {
            strafe = -45.0F;
            yaw += 180.0F;
         }

         if (input.playerInput.left()) {
            yaw -= strafe;
            if (input.movementForward == 0.0F) {
               yaw -= 45.0F;
            }
         } else if (input.playerInput.right()) {
            yaw += strafe;
            if (input.movementForward == 0.0F) {
               yaw += 45.0F;
            }
         }

         return (yaw % 360.0F + 360.0F) % 360.0F;
      }
   }

   public static void stopMoving() {
      if (mc.player != null) {
         mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
         mc.player.input.movementForward = 0.0F;
         mc.player.input.movementSideways = 0.0F;
      }
   }

   public static boolean isBlockUnder(double height) {
      for (int offset = 0; offset < height; offset += 2) {
         Box bb = mc.player.getBoundingBox().offset(0.0, -offset, 0.0);
         if (!mc.world.canCollide(mc.player, bb)) {
            return true;
         }
      }

      return false;
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin().equals(TransferOrigin.RECEIVE) && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         ticksSinceTeleport = 0;
      }
   }

   @EventHandler
   public void onPostTick(TickEvent e) {
      if (e.getState().equals(Stage.POST) && mc.player != null) {
         lastVelocity = mc.player.getVelocity();
      }
   }

   public static float wrapDegrees(float degrees) {
      float f = degrees % 360.0F;
      if (f >= 180.0F) {
         f -= 360.0F;
      }

      if (f < -180.0F) {
         f += 360.0F;
      }

      return f;
   }

   public static float direction(float forward, float strafe) {
      float direction = MinecraftClient.getInstance().player.getYaw();
      boolean isMovingForward = forward > 0.0F;
      boolean isMovingBack = forward < 0.0F;
      boolean isMovingRight = strafe > 0.0F;
      boolean isMovingLeft = strafe < 0.0F;
      boolean isMovingSideways = isMovingRight || isMovingLeft;
      boolean isMovingStraight = isMovingForward || isMovingBack;
      if (forward != 0.0F || strafe != 0.0F) {
         if (isMovingBack && !isMovingSideways) {
            return direction + 180.0F;
         }

         if (isMovingForward && isMovingLeft) {
            return direction + 45.0F;
         }

         if (isMovingForward && isMovingRight) {
            return direction - 45.0F;
         }

         if (!isMovingStraight && isMovingLeft) {
            return direction + 90.0F;
         }

         if (!isMovingStraight && isMovingRight) {
            return direction - 90.0F;
         }

         if (isMovingBack && isMovingLeft) {
            return direction + 135.0F;
         }

         if (isMovingBack) {
            return direction - 135.0F;
         }
      }

      return direction;
   }

   public static void flushBoundingBoxes() {
      if (mc.world != null) {
         for (Entity entity : mc.world.getEntities()) {
            entity.setPosition(entity.getPos());
         }
      }
   }

   public static void moveFlying(double speed) {
      PlayerEntity player = mc.player;
      if (player != null && isMoving()) {
         double yawRad = Math.toRadians(direction(mc.player.input.movementForward, mc.player.input.movementSideways));
         double x = -MathHelper.sin((float)yawRad) * speed;
         double z = MathHelper.cos((float)yawRad) * speed;
         player.addVelocity(x, 0.0, z);
      }
   }

   public static boolean willFallNextTick() {
      if (mc.player == null) {
         return false;
      } else {
         FallingPlayer pred = new FallingPlayer(mc.player);
         pred.calculate(1);
         BlockPos frontPos1 = new BlockPos((int)pred.getX(), (int)(mc.player.getY() - 0.1), (int)pred.getZ());
         return mc.world.getBlockState(frontPos1).getBlock() instanceof AirBlock && pred.getMotion().y < 0.0;
      }
   }

   public static void setSkipTicks(int i) {
      playerStuckTicks = i;
   }

   @EventHandler(
      priority = 200
   )
   public void onSprint(SprintPacketSendEvent event) {
      if (!event.isSprinting()) {
         ticksSinceSprint = 0;
      } else {
         ticksSinceSprint++;
      }
   }

   @Generated
   public static Vec3d getLastVelocity() {
      return lastVelocity;
   }
}
