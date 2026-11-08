package dev.southside.utils.math;

public class Vector3d {
   public double x;
   public double y;
   public double z;

   public Vector3d(double d, double d2, double d3) {
      this.x = d;
      this.y = d2;
      this.z = d3;
   }

   public Vector3d add(double d, double d2, double d3) {
      return new Vector3d(this.x + d, this.y + d2, this.z + d3);
   }

   public Vector3d add(Vector3d vector3d) {
      return this.add(vector3d.x, vector3d.y, vector3d.z);
   }

   public Vector3d subtract(double d, double d2, double d3) {
      return this.add(-d, -d2, -d3);
   }

   public Vector3d subtract(Vector3d vector3d) {
      return this.add(-vector3d.x, -vector3d.y, -vector3d.z);
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public Vector3d multiply(double d) {
      return new Vector3d(this.x * d, this.y * d, this.z * d);
   }

   public double distance(Vector3d vector3d) {
      return Math.sqrt(Math.pow(vector3d.x - this.x, 2.0) + Math.pow(vector3d.y - this.y, 2.0) + Math.pow(vector3d.z - this.z, 2.0));
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Vector3d vector3d)
         ? false
         : Math.floor(this.x) == Math.floor(vector3d.x) && Math.floor(this.y) == Math.floor(vector3d.y) && Math.floor(this.z) == Math.floor(vector3d.z);
   }

   public void setX(double d) {
      this.x = d;
   }

   public void setY(double d) {
      this.y = d;
   }

   public void setZ(double d) {
      this.z = d;
   }
}
