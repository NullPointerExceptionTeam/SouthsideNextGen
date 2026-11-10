package dev.southside.module.impl.world.sca;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public record NormalizedPlane(Vec3d pos, Vec3d normalVec) {
   public Double intersectionPhi(Line line) {
      double d = this.pos.dotProduct(this.normalVec);
      double e = line.direction.dotProduct(this.normalVec);
      return MathHelper.approximatelyEquals(e, 0.0) ? null : (d - line.position.dotProduct(this.normalVec)) / e;
   }

   public Vec3d intersection(Line line) {
      Double phi = this.intersectionPhi(line);
      return phi != null ? line.getPositionChcked(phi) : null;
   }

   public Line intersection(NormalizedPlane other) {
      double x1 = other.normalVec.x;
      double y1 = other.normalVec.y;
      double z1 = other.normalVec.z;
      double v1 = other.normalVec.dotProduct(other.pos);
      double x2 = this.normalVec.x;
      double y2 = this.normalVec.y;
      double z2 = this.normalVec.z;
      double v2 = this.normalVec.dotProduct(this.pos);
      double dY = x2 * z1 - x1 * z2;
      double dXZ = x2 * y1 - x1 * y2;
      if (!MathHelper.approximatelyEquals(dY, 0.0)) {
         return new Line(
            new Vec3d((-v1 * z2 + v2 * z1) / dY, 0.0, (v1 * x2 - v2 * x1) / dY), new Vec3d((-z1 * y2 + z2 * y1) / dY, 1.0, (x1 * y2 - x2 * y1) / dY)
         );
      } else {
         return !MathHelper.approximatelyEquals(dXZ, 0.0)
            ? new Line(
               new Vec3d((-v1 * y2 + v2 * y1) / dXZ, (v1 * x2 - v2 * x1) / dXZ, 0.0),
               new Vec3d((-y1 * z2 + y2 * z1) / dXZ, (x1 * z2 - x2 * z1) / dXZ, 1.0)
            )
            : null;
      }
   }

   public static NormalizedPlane fromPoints(Vec3d a, Vec3d b, Vec3d c) {
      Vec3d ab = b.subtract(a);
      Vec3d ac = c.subtract(a);
      return fromParams(a, ab, ac);
   }

   public static NormalizedPlane fromParams(Vec3d base, Vec3d directionA, Vec3d directionB) {
      Vec3d normalVec = directionA.crossProduct(directionB).normalize();
      if (MathHelper.approximatelyEquals(normalVec.lengthSquared(), 0.0)) {
         throw new IllegalArgumentException("Points must not be on the same line");
      } else {
         return new NormalizedPlane(base, normalVec);
      }
   }
}
