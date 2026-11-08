package dev.southside.mixininterfaces;

import net.minecraft.class_243;

public interface IBoxUpdate {
   class_243 getServerBoxPos();

   class_243 getBoxPos();

   void setServerBoxPos(class_243 var1);

   void setBoxPos(class_243 var1);

   void setBoxTrackingIncrements(int var1);

   int getBoxTrackingIncrements();
}
