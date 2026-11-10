package dev.southside.mixininterfaces;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
   void ss$set(double var1, double var3, double var5);

   default void set(Vec3i vec) {
      this.ss$set(vec.getX(), vec.getY(), vec.getZ());
   }

   default void set(Vector3d vec) {
      this.ss$set(vec.x, vec.y, vec.z);
   }

   void setXZ(double var1, double var3);

   void setY(double var1);
}
