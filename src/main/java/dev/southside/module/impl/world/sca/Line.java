package dev.southside.module.impl.world.sca;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Direction.Axis;

public class Line {
   public final Vec3d position;
   public final Vec3d direction;

   public Line(Vec3d position, Vec3d direction) {
      this.position = position;
      this.direction = direction;
   }

   public static Line fromPoints(Vec3d p1, Vec3d p2, boolean normalized) {
      Vec3d direction = p2.subtract(p1);
      Vec3d finalDirection = normalized ? direction.normalize() : direction;
      return new Line(p1, finalDirection);
   }

   public Vec3d getNearestPointTo(Vec3d point) {
      NormalizedPlane plane = new NormalizedPlane(point, this.direction);
      Vec3d intersection = plane.intersection(this);
      return intersection != null ? intersection : point;
   }

   public double squaredDistanceTo(Vec3d point) {
      return this.getNearestPointTo(point).squaredDistanceTo(point);
   }

   public Vec3d getPositionChcked(double phi) {
      return this.getPosition(phi);
   }

   public Vec3d getPosition(double phi) {
      return this.position.add(this.direction.multiply(phi));
   }

   public double getPhiForPoint(Vec3d point) {
      Vec3d fromPosition = point.subtract(this.position);
      double[][] possibleCoordinates = new double[][]{
         {fromPosition.x, this.direction.x},
         {fromPosition.y, this.direction.y},
         {fromPosition.z, this.direction.z}
      };
      List<double[]> filtered = Arrays.stream(possibleCoordinates).filter(arr -> !MathHelper.approximatelyEquals(arr[1], 0.0)).collect(Collectors.toList());
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

   public Vec3d getPointOnBoxInDirection(Box box) {
      return Arrays.stream(Direction.values()).map(dir -> {
         double positionCoordinate = getComponentAlongAxis(this.position, dir.getAxis());
         double directionCoordinate = getComponentAlongAxis(this.direction, dir.getAxis());
         Double factor = this.computeIntersection(getCoordinate(box, dir), positionCoordinate, directionCoordinate);
         if (factor == null) {
            return null;
         } else {
            Vec3d pointOnFace = Vec3d.of(dir.getVector()).multiply(factor);
            Vec3d directionalPointsOnFace = this.position.add(this.direction.normalize().multiply(factor));
            return preferOver(pointOnFace, directionalPointsOnFace);
         }
      }).filter(vec -> vec != null).min(Comparator.comparingDouble(vec -> this.position.squaredDistanceTo(vec))).orElse(null);
   }

   private Double computeIntersection(double plane, double pos, double dir) {
      if (MathHelper.approximatelyEquals(dir, 0.0)) {
         return null;
      } else {
         double t = (plane - pos) / dir;
         return t > 0.0 ? t : null;
      }
   }

   protected Double calculateNearestPhiTo(Line other) {
      double pos1X = other.position.x;
      double pos1Y = other.position.y;
      double pos1Z = other.position.z;
      double dir1X = other.direction.x;
      double dir1Y = other.direction.y;
      double dir1Z = other.direction.z;
      double pos2X = this.position.x;
      double pos2Y = this.position.y;
      double pos2Z = this.position.z;
      double dir2X = this.direction.x;
      double dir2Y = this.direction.y;
      double dir2Z = this.direction.z;
      double divisor = (dir1Y * dir1Y + dir1X * dir1X) * dir2Z * dir2Z
         + (-2.0 * dir1Y * dir1Z * dir2Y - 2.0 * dir1X * dir1Z * dir2X) * dir2Z
         + (dir1Z * dir1Z + dir1X * dir1X) * dir2Y * dir2Y
         - 2.0 * dir1X * dir1Y * dir2X * dir2Y
         + (dir1Z * dir1Z + dir1Y * dir1Y) * dir2X * dir2X;
      if (MathHelper.approximatelyEquals(divisor, 0.0)) {
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

   private static double getComponentAlongAxis(Vec3d vec, Axis axis) {
      return axis.choose(vec.x, vec.y, vec.z);
   }

   private static double getCoordinate(Box box, Direction dir) {
      return switch (dir) {
         case DOWN -> box.minY;
         case UP -> box.maxY;
         case NORTH -> box.minZ;
         case SOUTH -> box.maxZ;
         case WEST -> box.minX;
         case EAST -> box.maxX;
         default -> throw new MatchException(null, null);
      };
   }

   private static Vec3d preferOver(Vec3d a, Vec3d b) {
      double x = a.x == 0.0 ? b.x : a.x;
      double y = a.y == 0.0 ? b.y : a.y;
      double z = a.z == 0.0 ? b.z : a.z;
      return new Vec3d(x, y, z);
   }

   public static class NearestPointsResult {
      public final Vec3d point1;
      public final Vec3d point2;

      public NearestPointsResult(Vec3d point1, Vec3d point2) {
         this.point1 = point1;
         this.point2 = point2;
      }
   }
}
