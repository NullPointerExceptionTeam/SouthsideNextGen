package dev.southside.module.impl.world.sca;

import net.minecraft.class_243;
import net.minecraft.class_3532;

public record NormalizedPlane(class_243 pos, class_243 normalVec) {
   public Double intersectionPhi(Line line) {
      double d = this.pos.method_1026(this.normalVec);
      double e = line.direction.method_1026(this.normalVec);
      return class_3532.method_20390(e, 0.0) ? null : (d - line.position.method_1026(this.normalVec)) / e;
   }

   public class_243 intersection(Line line) {
      Double phi = this.intersectionPhi(line);
      return phi != null ? line.getPositionChcked(phi) : null;
   }

   public Line intersection(NormalizedPlane other) {
      double x1 = other.normalVec.field_1352;
      double y1 = other.normalVec.field_1351;
      double z1 = other.normalVec.field_1350;
      double v1 = other.normalVec.method_1026(other.pos);
      double x2 = this.normalVec.field_1352;
      double y2 = this.normalVec.field_1351;
      double z2 = this.normalVec.field_1350;
      double v2 = this.normalVec.method_1026(this.pos);
      double dY = x2 * z1 - x1 * z2;
      double dXZ = x2 * y1 - x1 * y2;
      if (!class_3532.method_20390(dY, 0.0)) {
         return new Line(
            new class_243((-v1 * z2 + v2 * z1) / dY, 0.0, (v1 * x2 - v2 * x1) / dY), new class_243((-z1 * y2 + z2 * y1) / dY, 1.0, (x1 * y2 - x2 * y1) / dY)
         );
      } else {
         return !class_3532.method_20390(dXZ, 0.0)
            ? new Line(
               new class_243((-v1 * y2 + v2 * y1) / dXZ, (v1 * x2 - v2 * x1) / dXZ, 0.0),
               new class_243((-y1 * z2 + y2 * z1) / dXZ, (x1 * z2 - x2 * z1) / dXZ, 1.0)
            )
            : null;
      }
   }

   public static NormalizedPlane fromPoints(class_243 a, class_243 b, class_243 c) {
      class_243 ab = b.method_1020(a);
      class_243 ac = c.method_1020(a);
      return fromParams(a, ab, ac);
   }

   public static NormalizedPlane fromParams(class_243 base, class_243 directionA, class_243 directionB) {
      class_243 normalVec = directionA.method_1036(directionB).method_1029();
      if (class_3532.method_20390(normalVec.method_1027(), 0.0)) {
         throw new IllegalArgumentException("Points must not be on the same line");
      } else {
         return new NormalizedPlane(base, normalVec);
      }
   }
}
