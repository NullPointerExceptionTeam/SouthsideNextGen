package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import net.minecraft.class_4587;
import net.minecraft.class_804;

public class ApplyTransformationEvent extends CancellableEvent {
   private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();
   public class_804 transformation;
   public boolean leftHanded;
   public class_4587 matrices;

   public static ApplyTransformationEvent get(class_804 transformation, boolean leftHanded, class_4587 matrices) {
      INSTANCE.setCancelled(false);
      INSTANCE.transformation = transformation;
      INSTANCE.leftHanded = leftHanded;
      INSTANCE.matrices = matrices;
      return INSTANCE;
   }
}
