package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.model.json.Transformation;

public class ApplyTransformationEvent extends CancellableEvent {
   private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();
   public Transformation transformation;
   public boolean leftHanded;
   public MatrixStack matrices;

   public static ApplyTransformationEvent get(Transformation transformation, boolean leftHanded, MatrixStack matrices) {
      INSTANCE.setCancelled(false);
      INSTANCE.transformation = transformation;
      INSTANCE.leftHanded = leftHanded;
      INSTANCE.matrices = matrices;
      return INSTANCE;
   }
}
