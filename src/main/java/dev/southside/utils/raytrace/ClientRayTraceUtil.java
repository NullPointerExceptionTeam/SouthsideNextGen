package dev.southside.utils.raytrace;

import dev.southside.utils.types.Rotation;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2189;
import net.minecraft.class_2248;
import net.minecraft.class_2261;
import net.minecraft.class_2281;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2488;
import net.minecraft.class_2526;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import org.jetbrains.annotations.Nullable;

public final class ClientRayTraceUtil {
   private static final double EPSILON = 1.0E-7;
   public static class_243 eyePos = null;
   private static class_310 mc = class_310.method_1551();

   public static boolean didHitBlockFace(Rotation rotation, class_2338 targetPos, class_2350 expectedFace, boolean strict) {
      return didHitBlockFace(mc.field_1724, rotation.yaw, rotation.pitch, targetPos, expectedFace, strict, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static boolean didHitBlockFace(class_1657 player, float yaw, float pitch, class_2338 targetPos, class_2350 expectedFace, boolean strict) {
      return didHitBlockFace(player, yaw, pitch, targetPos, expectedFace, strict, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static boolean didHitBlockFace(
      Rotation rotation, class_2338 targetPos, class_2350 expectedFace, boolean strict, Predicate<class_2680> ignorePredicate
   ) {
      return didHitBlockFace(mc.field_1724, rotation.yaw, rotation.pitch, targetPos, expectedFace, strict, ignorePredicate);
   }

   public static boolean didHitBlockFace(
      class_1657 player, float yaw, float pitch, class_2338 targetPos, class_2350 expectedFace, boolean strict, Predicate<class_2680> ignorePredicate
   ) {
      if (player != null && expectedFace != null) {
         class_3965 result = getFacedBlock(yaw, pitch, ignorePredicate);
         return result != null
            && targetPos.method_10263() == result.method_17777().method_10263()
            && targetPos.method_10264() == result.method_17777().method_10264()
            && targetPos.method_10260() == result.method_17777().method_10260()
            && (expectedFace == result.method_17780() || !strict);
      } else {
         return false;
      }
   }

   public static void updateEyePos() {
      if (mc.field_1724 != null) {
         eyePos = mc.field_1724.method_33571();
      }
   }

   @Nullable
   private static class_2350 getHitFace(class_243 hitPos, class_238 box) {
      if (Math.abs(hitPos.field_1352 - box.field_1323) < 1.0E-7) {
         return class_2350.field_11039;
      } else if (Math.abs(hitPos.field_1352 - box.field_1320) < 1.0E-7) {
         return class_2350.field_11034;
      } else if (Math.abs(hitPos.field_1351 - box.field_1322) < 1.0E-7) {
         return class_2350.field_11033;
      } else if (Math.abs(hitPos.field_1351 - box.field_1325) < 1.0E-7) {
         return class_2350.field_11036;
      } else if (Math.abs(hitPos.field_1350 - box.field_1321) < 1.0E-7) {
         return class_2350.field_11043;
      } else {
         return Math.abs(hitPos.field_1350 - box.field_1324) < 1.0E-7 ? class_2350.field_11035 : null;
      }
   }

   public static class_3965 getFacedBlock(float yaw, float pitch) {
      return getFacedBlock(yaw, pitch, ClientRayTraceUtil::isIgnoredBlock);
   }

   public static class_3965 getFacedBlock(float yaw, float pitch, Predicate<class_2680> ignorePredicate) {
      class_310 client = class_310.method_1551();
      double reachDistance = client.field_1724.method_55754();
      class_243 startPos = eyePos;
      class_243 direction = class_243.method_1030(pitch, yaw);
      class_243 endPos = startPos.method_1019(direction.method_1021(reachDistance));
      if (direction.field_1352 == 0.0) {
         direction = new class_243(1.0E-7, direction.field_1351, direction.field_1350);
      }

      if (direction.field_1351 == 0.0) {
         direction = new class_243(direction.field_1352, 1.0E-7, direction.field_1350);
      }

      if (direction.field_1350 == 0.0) {
         direction = new class_243(direction.field_1352, direction.field_1351, 1.0E-7);
      }

      class_2338 currentPos = class_2338.method_49638(startPos);
      int stepX = (int)Math.signum(direction.field_1352);
      int stepY = (int)Math.signum(direction.field_1351);
      int stepZ = (int)Math.signum(direction.field_1350);
      double nextBoundaryX = stepX > 0 ? currentPos.method_10263() + 1 : currentPos.method_10263();
      double nextBoundaryY = stepY > 0 ? currentPos.method_10264() + 1 : currentPos.method_10264();
      double nextBoundaryZ = stepZ > 0 ? currentPos.method_10260() + 1 : currentPos.method_10260();
      double tMaxX = (nextBoundaryX - startPos.field_1352) / direction.field_1352;
      double tMaxY = (nextBoundaryY - startPos.field_1351) / direction.field_1351;
      double tMaxZ = (nextBoundaryZ - startPos.field_1350) / direction.field_1350;
      double tDeltaX = stepX / direction.field_1352;
      double tDeltaY = stepY / direction.field_1351;
      double tDeltaZ = stepZ / direction.field_1350;
      class_1937 world = mc.field_1724.method_37908();

      while (startPos.method_1022(currentPos.method_46558()) <= reachDistance) {
         if (!world.method_22347(currentPos)) {
            class_2680 state = world.method_8320(currentPos);
            if (!ignorePredicate.test(state)) {
               class_3610 fluidState = world.method_8316(currentPos);
               class_265 shape;
               if (fluidState != null && fluidState.method_15771() && fluidState.method_15772() == class_3612.field_15910) {
                  shape = class_259.method_1077();
               } else {
                  shape = state.method_26194(world, currentPos, class_3726.method_16195(client.field_1724));
               }

               if (!shape.method_1110()) {
                  for (class_238 localBox : shape.method_1090()) {
                     class_238 box = localBox.method_996(currentPos);
                     Optional<class_243> intercept = box.method_992(startPos, endPos);
                     if (intercept.isPresent()) {
                        class_243 hitVec = intercept.get();
                        class_2350 side = getHitFaceFromBox(hitVec, box);
                        boolean isInside = box.method_1006(startPos);
                        return new class_3965(hitVec, side, currentPos, isInside);
                     }
                  }
               }
            }
         }

         if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
               currentPos = currentPos.method_10069(stepX, 0, 0);
               tMaxX += tDeltaX;
            } else {
               currentPos = currentPos.method_10069(0, 0, stepZ);
               tMaxZ += tDeltaZ;
            }
         } else if (tMaxY < tMaxZ) {
            currentPos = currentPos.method_10069(0, stepY, 0);
            tMaxY += tDeltaY;
         } else {
            currentPos = currentPos.method_10069(0, 0, stepZ);
            tMaxZ += tDeltaZ;
         }
      }

      return null;
   }

   public static boolean isIgnoredBlock(class_2680 state) {
      class_2248 block = state.method_26204();
      return block instanceof class_2261
         || block instanceof class_2488
         || block instanceof class_2189
         || block instanceof class_2526
         || block instanceof class_2404;
   }

   private static class_2350 getHitFaceFromBox(class_243 hit, class_238 box) {
      double eps = 1.0E-7;
      if (Math.abs(hit.field_1352 - box.field_1323) <= 1.0E-7) {
         return class_2350.field_11039;
      } else if (Math.abs(hit.field_1352 - box.field_1320) <= 1.0E-7) {
         return class_2350.field_11034;
      } else if (Math.abs(hit.field_1351 - box.field_1322) <= 1.0E-7) {
         return class_2350.field_11033;
      } else if (Math.abs(hit.field_1351 - box.field_1325) <= 1.0E-7) {
         return class_2350.field_11036;
      } else if (Math.abs(hit.field_1350 - box.field_1321) <= 1.0E-7) {
         return class_2350.field_11043;
      } else if (Math.abs(hit.field_1350 - box.field_1324) <= 1.0E-7) {
         return class_2350.field_11035;
      } else {
         double dxMin = Math.abs(hit.field_1352 - box.field_1323);
         double dxMax = Math.abs(hit.field_1352 - box.field_1320);
         double dyMin = Math.abs(hit.field_1351 - box.field_1322);
         double dyMax = Math.abs(hit.field_1351 - box.field_1325);
         double dzMin = Math.abs(hit.field_1350 - box.field_1321);
         double dzMax = Math.abs(hit.field_1350 - box.field_1324);
         double m = dxMin;
         class_2350 d = class_2350.field_11039;
         if (dxMax < dxMin) {
            m = dxMax;
            d = class_2350.field_11034;
         }

         if (dyMin < m) {
            m = dyMin;
            d = class_2350.field_11033;
         }

         if (dyMax < m) {
            m = dyMax;
            d = class_2350.field_11036;
         }

         if (dzMin < m) {
            m = dzMin;
            d = class_2350.field_11043;
         }

         if (dzMax < m) {
            d = class_2350.field_11035;
         }

         return d;
      }
   }

   public static class_3965 getFacedContainerBlock(float yaw, float pitch) {
      class_310 client = class_310.method_1551();
      double reachDistance = client.field_1724.method_55754();
      class_243 startPos = eyePos;
      class_243 direction = class_243.method_1030(pitch, yaw);
      class_243 endPos = startPos.method_1019(direction.method_1021(reachDistance));
      if (direction.field_1352 == 0.0) {
         direction = new class_243(1.0E-7, direction.field_1351, direction.field_1350);
      }

      if (direction.field_1351 == 0.0) {
         direction = new class_243(direction.field_1352, 1.0E-7, direction.field_1350);
      }

      if (direction.field_1350 == 0.0) {
         direction = new class_243(direction.field_1352, direction.field_1351, 1.0E-7);
      }

      class_2338 currentPos = class_2338.method_49638(startPos);
      int stepX = (int)Math.signum(direction.field_1352);
      int stepY = (int)Math.signum(direction.field_1351);
      int stepZ = (int)Math.signum(direction.field_1350);
      double nextBoundaryX = stepX > 0 ? currentPos.method_10263() + 1 : currentPos.method_10263();
      double nextBoundaryY = stepY > 0 ? currentPos.method_10264() + 1 : currentPos.method_10264();
      double nextBoundaryZ = stepZ > 0 ? currentPos.method_10260() + 1 : currentPos.method_10260();
      double tMaxX = (nextBoundaryX - startPos.field_1352) / direction.field_1352;
      double tMaxY = (nextBoundaryY - startPos.field_1351) / direction.field_1351;
      double tMaxZ = (nextBoundaryZ - startPos.field_1350) / direction.field_1350;
      double tDeltaX = stepX / direction.field_1352;
      double tDeltaY = stepY / direction.field_1351;
      double tDeltaZ = stepZ / direction.field_1350;

      while (startPos.method_1022(currentPos.method_46558()) <= reachDistance) {
         if (!mc.field_1724.method_37908().method_22347(currentPos) && isContainerBlock(mc.field_1724.method_37908().method_8320(currentPos))) {
            class_238 currentBox = new class_238(currentPos);
            Optional<class_243> intercept = currentBox.method_992(startPos, endPos);
            if (intercept.isPresent()) {
               class_243 hitVec = intercept.get();
               class_2350 side = getHitFace(hitVec, currentBox);
               boolean isInside = currentBox.method_1006(startPos);
               return new class_3965(hitVec, side, currentPos, isInside);
            }
         }

         if (tMaxX < tMaxY) {
            if (tMaxX < tMaxZ) {
               currentPos = currentPos.method_10069(stepX, 0, 0);
               tMaxX += tDeltaX;
            } else {
               currentPos = currentPos.method_10069(0, 0, stepZ);
               tMaxZ += tDeltaZ;
            }
         } else if (tMaxY < tMaxZ) {
            currentPos = currentPos.method_10069(0, stepY, 0);
            tMaxY += tDeltaY;
         } else {
            currentPos = currentPos.method_10069(0, 0, stepZ);
            tMaxZ += tDeltaZ;
         }
      }

      return null;
   }

   private static boolean isContainerBlock(class_2680 state) {
      return state.method_26204() instanceof class_2281 || state.method_26204() instanceof class_2336 || state.method_26204() instanceof class_2480;
   }

   public static double getDistance(float yaw, float pitch, class_1297 target) {
      class_243 dir = class_243.method_1030(pitch, yaw).method_1029();
      class_238 targetBox = target.method_5829();
      return intersectRayAabb(eyePos, dir, targetBox);
   }

   public static boolean didHitEntity(float yaw, float pitch, double range, class_1297 target) {
      return overBox(yaw, pitch, range, target.method_5829());
   }

   public static boolean overBox(float yawDeg, float pitchDeg, double range, class_238 box) {
      if (box == null) {
         return false;
      } else {
         class_243 start = eyePos;
         class_1937 world = class_310.method_1551().field_1687;
         class_243 dir = class_243.method_1030(pitchDeg, yawDeg).method_1029();
         if (dir.method_1027() < 1.0E-12) {
            return false;
         } else {
            double tBox = intersectRayAabb(start, dir, box);
            return !Double.isFinite(tBox) || tBox < 0.0 || tBox > range ? false : !isOccludedBefore(world, start, dir, tBox, range, mc.field_1724);
         }
      }
   }

   private static boolean isOccludedBefore(class_1937 world, class_243 origin, class_243 dir, double tStop, double range, class_1297 viewer) {
      int x = class_3532.method_15357(origin.field_1352);
      int y = class_3532.method_15357(origin.field_1351);
      int z = class_3532.method_15357(origin.field_1350);
      int stepX = dir.field_1352 > 0.0 ? 1 : (dir.field_1352 < 0.0 ? -1 : 0);
      int stepY = dir.field_1351 > 0.0 ? 1 : (dir.field_1351 < 0.0 ? -1 : 0);
      int stepZ = dir.field_1350 > 0.0 ? 1 : (dir.field_1350 < 0.0 ? -1 : 0);
      double tMaxX = nextBoundaryT(origin.field_1352, dir.field_1352, x, stepX);
      double tMaxY = nextBoundaryT(origin.field_1351, dir.field_1351, y, stepY);
      double tMaxZ = nextBoundaryT(origin.field_1350, dir.field_1350, z, stepZ);
      double tDeltaX = stepX != 0 ? 1.0 / Math.abs(dir.field_1352) : Double.POSITIVE_INFINITY;
      double tDeltaY = stepY != 0 ? 1.0 / Math.abs(dir.field_1351) : Double.POSITIVE_INFINITY;
      double tDeltaZ = stepZ != 0 ? 1.0 / Math.abs(dir.field_1350) : Double.POSITIVE_INFINITY;
      double tLimit = Math.min(tStop, range);
      if (blockOccludesHere(world, origin, dir, new class_2338(x, y, z), tStop, viewer)) {
         return true;
      } else {
         while (true) {
            if (tMaxX < tMaxY) {
               if (tMaxX < tMaxZ) {
                  if (tMaxX > tLimit) {
                     break;
                  }

                  x += stepX;
                  if (blockOccludesHere(world, origin, dir, new class_2338(x, y, z), tStop, viewer)) {
                     return true;
                  }

                  tMaxX += tDeltaX;
               } else {
                  if (tMaxZ > tLimit) {
                     break;
                  }

                  z += stepZ;
                  if (blockOccludesHere(world, origin, dir, new class_2338(x, y, z), tStop, viewer)) {
                     return true;
                  }

                  tMaxZ += tDeltaZ;
               }
            } else if (tMaxY < tMaxZ) {
               if (tMaxY > tLimit) {
                  break;
               }

               y += stepY;
               if (blockOccludesHere(world, origin, dir, new class_2338(x, y, z), tStop, viewer)) {
                  return true;
               }

               tMaxY += tDeltaY;
            } else {
               if (tMaxZ > tLimit) {
                  break;
               }

               z += stepZ;
               if (blockOccludesHere(world, origin, dir, new class_2338(x, y, z), tStop, viewer)) {
                  return true;
               }

               tMaxZ += tDeltaZ;
            }
         }

         return false;
      }
   }

   private static double nextBoundaryT(double o, double d, int cell, int step) {
      if (step == 0) {
         return Double.POSITIVE_INFINITY;
      } else {
         double boundary = step > 0 ? cell + 1 : cell;
         return (boundary - o) / d;
      }
   }

   private static boolean blockOccludesHere(class_1937 world, class_243 origin, class_243 dir, class_2338 pos, double tEntity, class_1297 viewer) {
      class_2680 state = world.method_8320(pos);
      if (state.method_26215()) {
         return false;
      } else {
         class_265 shape = state.method_26172(world, pos, viewer != null ? class_3726.method_16195(viewer) : class_3726.method_16194());
         if (shape.method_1110()) {
            return false;
         } else {
            for (class_238 local : shape.method_1090()) {
               class_238 box = local.method_996(pos);
               double tBox = intersectRayAabb(origin, dir, box);
               if (Double.isFinite(tBox) && tBox >= 0.0 && tBox < tEntity) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static double intersectRayAabb(class_243 o, class_243 d, class_238 b) {
      double tMin = 0.0;
      double tMax = Double.POSITIVE_INFINITY;
      if (!axisSlab(o.field_1352, d.field_1352, b.field_1323, b.field_1320, ClientRayTraceUtil.Holder.tmp)) {
         return Double.POSITIVE_INFINITY;
      } else {
         tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
         tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
         if (tMax < tMin) {
            return Double.POSITIVE_INFINITY;
         } else if (!axisSlab(o.field_1351, d.field_1351, b.field_1322, b.field_1325, ClientRayTraceUtil.Holder.tmp)) {
            return Double.POSITIVE_INFINITY;
         } else {
            tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
            tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
            if (tMax < tMin) {
               return Double.POSITIVE_INFINITY;
            } else if (!axisSlab(o.field_1350, d.field_1350, b.field_1321, b.field_1324, ClientRayTraceUtil.Holder.tmp)) {
               return Double.POSITIVE_INFINITY;
            } else {
               tMin = Math.max(tMin, ClientRayTraceUtil.Holder.tmp[0]);
               tMax = Math.min(tMax, ClientRayTraceUtil.Holder.tmp[1]);
               return tMax < tMin ? Double.POSITIVE_INFINITY : tMin;
            }
         }
      }
   }

   private static boolean axisSlab(double o, double d, double min, double max, double[] out) {
      if (Math.abs(d) < 1.0E-12) {
         if (!(o < min) && !(o > max)) {
            out[0] = Double.NEGATIVE_INFINITY;
            out[1] = Double.POSITIVE_INFINITY;
            return true;
         } else {
            return false;
         }
      } else {
         double inv = 1.0 / d;
         double t1 = (min - o) * inv;
         double t2 = (max - o) * inv;
         if (t1 > t2) {
            double tmp = t1;
            t1 = t2;
            t2 = tmp;
         }

         out[0] = t1;
         out[1] = t2;
         return true;
      }
   }

   private static final class Holder {
      static final double[] tmp = new double[2];
   }
}
