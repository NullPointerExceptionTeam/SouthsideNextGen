package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.types.Rotation;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class ProjectileUtil {
   public static ProjectileUtil.ProjectileHit predict(
      double posX, double posY, double posZ, double motionX, double motionY, double motionZ, double motionSlowdown, double size, double gravity, boolean draw
   ) {
      class_239 landingPosition = null;
      boolean hasLanded = false;
      boolean hitEntity = false;
      int ticks = 0;

      while (!hasLanded && posY > -60.0) {
         ticks++;
         class_243 posBefore = new class_243(posX, posY, posZ);
         class_243 posAfter = new class_243(posX + motionX, posY + motionY, posZ + motionZ);
         class_3965 blockHitResult = MinecraftAccessor.mc
            .field_1687
            .method_17742(new class_3959(posBefore, posAfter, class_3960.field_17558, class_242.field_1347, MinecraftAccessor.mc.field_1724));
         if (blockHitResult != null && blockHitResult.method_17783() == class_240.field_1332) {
            hasLanded = true;
            landingPosition = blockHitResult;
            posAfter = blockHitResult.method_17784();
         }

         class_238 arrowBox = new class_238(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);

         for (class_1297 entity : MinecraftAccessor.mc.field_1687.method_8335(null, arrowBox.method_1009(motionX, motionY, motionZ).method_1009(1.0, 1.0, 1.0))) {
            if (entity.method_5863() && entity != MinecraftAccessor.mc.field_1724) {
               class_238 var2 = entity.method_5829().method_1009(size, size, size);
               Optional<class_243> possibleEntityLanding = var2.method_992(posBefore, posAfter);
               if (possibleEntityLanding.isPresent()) {
                  hitEntity = true;
                  hasLanded = true;
                  landingPosition = new class_3965(possibleEntityLanding.get(), null, class_2338.field_10980, false);
               }
            }
         }

         posX += motionX;
         posY += motionY;
         posZ += motionZ;
         class_2338 var35 = new class_2338((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
         class_2680 var36 = MinecraftAccessor.mc.field_1687.method_8320(var35);
         if (var36.method_27852(class_2246.field_10382)) {
            motionX *= 0.6;
            motionY *= 0.6;
            motionZ *= 0.6;
         } else {
            motionX *= motionSlowdown;
            motionY *= motionSlowdown;
            motionZ *= motionSlowdown;
         }

         motionY -= gravity;
      }

      return new ProjectileUtil.ProjectileHit(posX, posY, posZ, hitEntity, hasLanded, (class_3965)landingPosition, ticks);
   }

   public static class EnderPearlPredictor {
      public double predictX;
      public double predictY;
      public double predictZ;
      public double minMotionY;
      public double maxMotionY;

      public EnderPearlPredictor(double predictX, double predictY, double predictZ, double minMotionY, double maxMotionY) {
         this.predictX = predictX;
         this.predictY = predictY;
         this.predictZ = predictZ;
         this.minMotionY = minMotionY;
         this.maxMotionY = maxMotionY;
      }

      public double assessRotation(Rotation rotation) {
         double sum = 0.0;
         int cnt = 0;

         for (int rateIndex = 0; rateIndex <= 3; rateIndex++) {
            double rate = rateIndex / 3.0;

            for (int yaw = -1; yaw <= 1; yaw++) {
               for (int pitch = -1; pitch <= 1; pitch++) {
                  sum += this.assessSingleRotation(
                     new Rotation(rotation.yaw + yaw * 0.5F, rotation.pitch + pitch * 0.5F), MathUtils.interpolate(this.minMotionY, this.maxMotionY, rate)
                  );
                  cnt++;
               }
            }

            if (this.minMotionY == this.maxMotionY) {
               break;
            }
         }

         return sum / cnt;
      }

      public double assessSingleRotation(Rotation rotation, double motionYOffset) {
         return this.assessSingleRotation(rotation, motionYOffset, false);
      }

      public double assessSingleRotation(Rotation rotation, double motionYOffset, boolean debug) {
         if (rotation.pitch > 90.0F) {
            rotation.pitch = 90.0F;
         }

         if (rotation.pitch < -90.0F) {
            rotation.pitch = -90.0F;
         }

         float motionFactor = 1.5F;
         float gravity = 0.03F;
         float size = 0.25F;
         float motionSlowdown = 0.99F;
         double posX = this.predictX - class_3532.method_15362(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
         double posY = this.predictY + MinecraftAccessor.mc.field_1724.method_18381(MinecraftAccessor.mc.field_1724.method_18376()) - 0.1F;
         double posZ = this.predictZ - class_3532.method_15374(rotation.yaw / 180.0F * (float) Math.PI) * 0.16F;
         double motionX = -class_3532.method_15374(rotation.yaw / 180.0F * (float) Math.PI)
               * class_3532.method_15362(rotation.pitch / 180.0F * (float) Math.PI)
            * 0.4;
         double motionY = -class_3532.method_15374(rotation.pitch / 180.0F * (float) Math.PI) * 0.4;
         double motionZ = class_3532.method_15362(rotation.yaw / 180.0F * (float) Math.PI) * class_3532.method_15362(rotation.pitch / 180.0F * (float) Math.PI)
            * 0.4;
         float distance = class_3532.method_15355((float)(motionX * motionX + motionY * motionY + motionZ * motionZ));
         motionX /= distance;
         motionY /= distance;
         motionZ /= distance;
         motionX *= 1.5;
         motionY *= 1.5;
         motionZ *= 1.5;
         motionY += motionYOffset;
         ProjectileUtil.ProjectileHit projectileHit = ProjectileUtil.predict(posX, posY, posZ, motionX, motionY, motionZ, 0.99F, 0.25, 0.03F, false);
         if (!projectileHit.hasLanded()) {
            if (debug) {
               ChatUtils.info(
                  String.format(
                     "§7[assess] yaw=%.0f pitch=%.0f → §cNO LANDING§7 endPos=(%.0f, %.0f, %.0f) ticks=%d",
                     rotation.yaw,
                     rotation.pitch,
                     projectileHit.posX(),
                     projectileHit.posY(),
                     projectileHit.posZ(),
                     projectileHit.ticks()
                  )
               );
            }

            return 0.05;
         } else {
            class_3965 hitBlock = projectileHit.landingPosition();
            if (hitBlock instanceof class_3965) {
               class_2338 hitBlockx = hitBlock.method_17777();
               class_2338 landPos = hitBlockx.method_10093(hitBlock.method_17780());
               class_2350 side = hitBlock.method_17780();
               double score;
               if (side == class_2350.field_11036) {
                  score = this.assessBlockPos(landPos) * this.distanceFunction(projectileHit.ticks);
               } else {
                  double nearbyScore = this.assessNearbyGround(hitBlockx, landPos);
                  score = nearbyScore * this.distanceFunction(projectileHit.ticks);
               }

               if (debug) {
                  ChatUtils.info(
                     String.format(
                        "§7[assess] yaw=%.0f pitch=%.0f → §a%s§7 at (%d,%d,%d) ticks=%d finalScore=%.3f",
                        rotation.yaw,
                        rotation.pitch,
                        side == class_2350.field_11036 ? "TOP" : side.name(),
                        landPos.method_10263(),
                        landPos.method_10264(),
                        landPos.method_10260(),
                        projectileHit.ticks(),
                        score
                     )
                  );
               }

               return score;
            } else {
               if (debug) {
                  ChatUtils.info(String.format("§7[assess] yaw=%.0f pitch=%.0f → §eHIT ENTITY§7 ticks=%d", rotation.yaw, rotation.pitch, projectileHit.ticks()));
               }

               return 0.05 * this.distanceFunction(projectileHit.ticks);
            }
         }
      }

      private double assessBlockPos(class_2338 pos) {
         for (int y = 0; y >= -5; y--) {
            class_2338 checkPos = pos.method_10069(0, y, 0);
            class_2680 blockState = MinecraftAccessor.mc.field_1687.method_8320(checkPos);
            if (blockState.method_26212(MinecraftAccessor.mc.field_1687, checkPos)) {
               return y == 0 ? 0.4 : 1.0;
            }

            if (blockState.method_26225()) {
               return 0.99;
            }
         }

         return 0.05;
      }

      private double assessNearbyGround(class_2338 hitBlock, class_2338 landPos) {
         if (MinecraftAccessor.mc.field_1687.method_8320(hitBlock).method_26212(MinecraftAccessor.mc.field_1687, hitBlock)
            && MinecraftAccessor.mc.field_1687.method_8320(hitBlock.method_10084()).method_26215()) {
            return 0.85;
         } else if (MinecraftAccessor.mc.field_1687.method_8320(hitBlock).method_26212(MinecraftAccessor.mc.field_1687, hitBlock)) {
            for (int y = 1; y <= 5; y++) {
               class_2338 above = hitBlock.method_10086(y);
               if (MinecraftAccessor.mc.field_1687.method_8320(above).method_26215()) {
                  return 0.75;
               }
            }

            return 0.6;
         } else {
            for (int yx = 0; yx >= -5; yx--) {
               class_2338 below = landPos.method_10069(0, yx, 0);
               if (MinecraftAccessor.mc.field_1687.method_8320(below).method_26212(MinecraftAccessor.mc.field_1687, below)) {
                  return yx == 0 ? 0.3 : 0.7;
               }
            }

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dz != 0) {
                     for (int dy = -1; dy <= 2; dy++) {
                        class_2338 nearby = landPos.method_10069(dx, dy, dz);
                        if (MinecraftAccessor.mc.field_1687.method_8320(nearby).method_26212(MinecraftAccessor.mc.field_1687, nearby)) {
                           return 0.55;
                        }
                     }
                  }
               }
            }

            return 0.4;
         }
      }

      private double distanceFunction(double d) {
         d /= 1000.0;
         return (d + 3.0) / (d + 2.0) / 1.5;
      }
   }

   public record ProjectileHit(double posX, double posY, double posZ, boolean hitEntity, boolean hasLanded, class_3965 landingPosition, int ticks) {
   }
}
