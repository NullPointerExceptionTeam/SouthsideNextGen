package dev.southside.mixininterfaces;

import net.minecraft.util.math.Vec3d;

public interface IBoxUpdate {
   Vec3d getServerBoxPos();

   Vec3d getBoxPos();

   void setServerBoxPos(Vec3d var1);

   void setBoxPos(Vec3d var1);

   void setBoxTrackingIncrements(int var1);

   int getBoxTrackingIncrements();
}
