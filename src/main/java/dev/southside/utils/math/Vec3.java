package dev.southside.utils.math;

import net.minecraft.class_2338;
import net.minecraft.class_243;

public class Vec3 {
   public double xCoord;
   public double yCoord;
   public double zCoord;

   public Vec3(double x, double y, double z) {
      if (x == -0.0) {
         x = 0.0;
      }

      if (y == -0.0) {
         y = 0.0;
      }

      if (z == -0.0) {
         z = 0.0;
      }

      this.xCoord = x;
      this.yCoord = y;
      this.zCoord = z;
   }

   public Vec3 subtractReverse(Vec3 vec) {
      return new Vec3(vec.xCoord - this.xCoord, vec.yCoord - this.yCoord, vec.zCoord - this.zCoord);
   }

   public static Vec3 atLowerCornerOf(class_2338 pos) {
      return new Vec3(pos.method_10263(), pos.method_10264(), pos.method_10260());
   }

   public double dotProduct(Vec3 vec) {
      return this.xCoord * vec.xCoord + this.yCoord * vec.yCoord + this.zCoord * vec.zCoord;
   }

   public Vec3 crossProduct(Vec3 vec) {
      return new Vec3(
         this.yCoord * vec.zCoord - this.zCoord * vec.yCoord,
         this.zCoord * vec.xCoord - this.xCoord * vec.zCoord,
         this.xCoord * vec.yCoord - this.yCoord * vec.xCoord
      );
   }

   public Vec3 subtract(Vec3 vec) {
      return this.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
   }

   public Vec3 subtract(double x, double y, double z) {
      return this.addVector(-x, -y, -z);
   }

   public Vec3 add(Vec3 vec) {
      return this.addVector(vec.xCoord, vec.yCoord, vec.zCoord);
   }

   public Vec3 addVector(double x, double y, double z) {
      return new Vec3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
   }

   public double squareDistanceTo(Vec3 vec) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public Vec3 getIntermediateWithXValue(Vec3 vec, double x) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d0 * d0 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (x - this.xCoord) / d0;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   public Vec3 getIntermediateWithYValue(Vec3 vec, double y) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d1 * d1 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (y - this.yCoord) / d1;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   public Vec3 getIntermediateWithZValue(Vec3 vec, double z) {
      double d0 = vec.xCoord - this.xCoord;
      double d1 = vec.yCoord - this.yCoord;
      double d2 = vec.zCoord - this.zCoord;
      if (d2 * d2 < 1.0E-7F) {
         return null;
      } else {
         double d3 = (z - this.zCoord) / d2;
         return d3 >= 0.0 && d3 <= 1.0 ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
      }
   }

   @Override
   public String toString() {
      return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
   }

   public Vec3 flat() {
      return new Vec3(this.xCoord, 0.0, this.zCoord);
   }

   public Vec3 scale(double factor) {
      return this.mul(factor, factor, factor);
   }

   public Vec3 mul(double factorX, double factorY, double factorZ) {
      return new Vec3(this.xCoord * factorX, this.yCoord * factorY, this.zCoord * factorZ);
   }

   public double lengthVector() {
      return (float)Math.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
   }

   public void of(class_243 input) {
      this.xCoord = input.method_10216();
      this.yCoord = input.method_10214();
      this.zCoord = input.method_10215();
   }
}
