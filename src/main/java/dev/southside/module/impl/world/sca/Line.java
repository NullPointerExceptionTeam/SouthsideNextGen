package dev.southside.module.impl.world.sca;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_2350.class_2351;

public class Line {
   public final class_243 position;
   public final class_243 direction;

   public Line(class_243 position, class_243 direction) {
      this.position = position;
      this.direction = direction;
   }

   public static Line fromPoints(class_243 p1, class_243 p2, boolean normalized) {
      class_243 direction = p2.method_1020(p1);
      class_243 finalDirection = normalized ? direction.method_1029() : direction;
      return new Line(p1, finalDirection);
   }

   public class_243 getNearestPointTo(class_243 point) {
      NormalizedPlane plane = new NormalizedPlane(point, this.direction);
      class_243 intersection = plane.intersection(this);
      return intersection != null ? intersection : point;
   }

   public double squaredDistanceTo(class_243 point) {
      return this.getNearestPointTo(point).method_1025(point);
   }

   public class_243 getPositionChcked(double phi) {
      return this.getPosition(phi);
   }

   public class_243 getPosition(double phi) {
      return this.position.method_1019(this.direction.method_1021(phi));
   }

   public double getPhiForPoint(class_243 point) {
      class_243 fromPosition = point.method_1020(this.position);
      double[][] possibleCoordinates = new double[][]{
         {fromPosition.field_1352, this.direction.field_1352},
         {fromPosition.field_1351, this.direction.field_1351},
         {fromPosition.field_1350, this.direction.field_1350}
      };
      List<double[]> filtered = Arrays.stream(possibleCoordinates).filter(arr -> !class_3532.method_20390(arr[1], 0.0)).collect(Collectors.toList());
      double directionAvg = filtered.stream().mapToDouble(arr -> arr[1]).average().orElse(0.0);
      double[] minAvgDistPair = filtered.stream().min(Comparator.comparingDouble(arr -> Math.abs(arr[1] - directionAvg))).orElse(filtered.get(0));
      return minAvgDistPair[0] / minAvgDistPair[1];
   }

   public Line.NearestPointsResult getNearestPointsTo(Line other) {
      double[] phis = this.getNearestPhisTo(other);
      return phis == null ? null : new Line.NearestPointsResult(this.getPosition(phis[0]), other.getPosition(phis[1]));
   }

   private double[] getNearestPhisTo(Line other) {
      Double phi1 = this.calculateNearestPhiTo(other);
      Double phi2 = other.calculateNearestPhiTo(this);
      return phi1 != null && phi2 != null ? new double[]{phi1, phi2} : null;
   }

   public class_243 getPointOnBoxInDirection(class_238 box) {
      return Arrays.stream(class_2350.values()).map(dir -> {
         double positionCoordinate = getComponentAlongAxis(this.position, dir.method_10166());
         double directionCoordinate = getComponentAlongAxis(this.direction, dir.method_10166());
         Double factor = this.computeIntersection(getCoordinate(box, dir), positionCoordinate, directionCoordinate);
         if (factor == null) {
            return null;
         } else {
            class_243 pointOnFace = class_243.method_24954(dir.method_62675()).method_1021(factor);
            class_243 directionalPointsOnFace = this.position.method_1019(this.direction.method_1029().method_1021(factor));
            return preferOver(pointOnFace, directionalPointsOnFace);
         }
      }).filter(vec -> vec != null).min(Comparator.comparingDouble(vec -> this.position.method_1025(vec))).orElse(null);
   }

   private Double computeIntersection(double plane, double pos, double dir) {
      if (class_3532.method_20390(dir, 0.0)) {
         return null;
      } else {
         double t = (plane - pos) / dir;
         return t > 0.0 ? t : null;
      }
   }

   protected Double calculateNearestPhiTo(Line other) {
      double pos1X = other.position.field_1352;
      double pos1Y = other.position.field_1351;
      double pos1Z = other.position.field_1350;
      double dir1X = other.direction.field_1352;
      double dir1Y = other.direction.field_1351;
      double dir1Z = other.direction.field_1350;
      double pos2X = this.position.field_1352;
      double pos2Y = this.position.field_1351;
      double pos2Z = this.position.field_1350;
      double dir2X = this.direction.field_1352;
      double dir2Y = this.direction.field_1351;
      double dir2Z = this.direction.field_1350;
      double divisor = (dir1Y * dir1Y + dir1X * dir1X) * dir2Z * dir2Z
         + (-2.0 * dir1Y * dir1Z * dir2Y - 2.0 * dir1X * dir1Z * dir2X) * dir2Z
         + (dir1Z * dir1Z + dir1X * dir1X) * dir2Y * dir2Y
         - 2.0 * dir1X * dir1Y * dir2X * dir2Y
         + (dir1Z * dir1Z + dir1Y * dir1Y) * dir2X * dir2X;
      if (class_3532.method_20390(divisor, 0.0)) {
         return null;
      } else {
         double t2 = -(
               ((dir1Y * dir1Y + dir1X * dir1X) * dir2Z - dir1Y * dir1Z * dir2Y - dir1X * dir1Z * dir2X) * pos2Z
                  + (-dir1Y * dir1Z * dir2Z + (dir1Z * dir1Z + dir1X * dir1X) * dir2Y - dir1X * dir1Y * dir2X) * pos2Y
                  + (-dir1X * dir1Z * dir2Z - dir1X * dir1Y * dir2Y + (dir1Z * dir1Z + dir1Y * dir1Y) * dir2X) * pos2X
                  + ((-dir1Y * dir1Y - dir1X * dir1X) * dir2Z + dir1Y * dir1Z * dir2Y + dir1X * dir1Z * dir2X) * pos1Z
                  + (dir1Y * dir1Z * dir2Z + (-dir1Z * dir1Z - dir1X * dir1X) * dir2Y + dir1X * dir1Y * dir2X) * pos1Y
                  + (dir1X * dir1Z * dir2Z + dir1X * dir1Y * dir2Y + (-dir1Z * dir1Z - dir1Y * dir1Y) * dir2X) * pos1X
            )
            / divisor;
         return t2;
      }
   }

   @Override
   public String toString() {
      return "Line(position=" + this.position + ", direction=" + this.direction + ")";
   }

   private static double getComponentAlongAxis(class_243 vec, class_2351 axis) {
      return axis.method_10172(vec.field_1352, vec.field_1351, vec.field_1350);
   }

   private static double getCoordinate(class_238 box, class_2350 dir) {
      return switch (dir) {
         case field_11033 -> box.field_1322;
         case field_11036 -> box.field_1325;
         case field_11043 -> box.field_1321;
         case field_11035 -> box.field_1324;
         case field_11039 -> box.field_1323;
         case field_11034 -> box.field_1320;
         default -> throw new MatchException(null, null);
      };
   }

   private static class_243 preferOver(class_243 a, class_243 b) {
      double x = a.field_1352 == 0.0 ? b.field_1352 : a.field_1352;
      double y = a.field_1351 == 0.0 ? b.field_1351 : a.field_1351;
      double z = a.field_1350 == 0.0 ? b.field_1350 : a.field_1350;
      return new class_243(x, y, z);
   }

   public static class NearestPointsResult {
      public final class_243 point1;
      public final class_243 point2;

      public NearestPointsResult(class_243 point1, class_243 point2) {
         this.point1 = point1;
         this.point2 = point2;
      }
   }
}
