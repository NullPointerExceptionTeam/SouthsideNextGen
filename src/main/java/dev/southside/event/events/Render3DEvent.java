package dev.southside.event.events;

import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Matrix4f;

public class Render3DEvent {
   private final class_4184 camera;
   private final class_4587 matrixStack;
   private final float tickDelta;

   public Render3DEvent(class_4184 camera, class_4587 matrixStack, float tickDelta) {
      this.tickDelta = tickDelta;
      this.camera = camera;
      this.matrixStack = matrixStack;
   }

   public class_4184 getCamera() {
      return this.camera;
   }

   public float getTickCounter() {
      return this.tickDelta;
   }

   public class_4587 getMatrixStack() {
      return this.matrixStack;
   }

   public Matrix4f getProjectionMatrix() {
      return this.matrixStack.method_23760().method_23761();
   }
}
