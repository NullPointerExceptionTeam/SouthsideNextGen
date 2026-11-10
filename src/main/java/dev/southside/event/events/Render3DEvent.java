package dev.southside.event.events;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Render3DEvent {
   private final Camera camera;
   private final MatrixStack matrixStack;
   private final float tickDelta;

   public Render3DEvent(Camera camera, MatrixStack matrixStack, float tickDelta) {
      this.tickDelta = tickDelta;
      this.camera = camera;
      this.matrixStack = matrixStack;
   }

   public Camera getCamera() {
      return this.camera;
   }

   public float getTickCounter() {
      return this.tickDelta;
   }

   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }

   public Matrix4f getProjectionMatrix() {
      return this.matrixStack.peek().getPositionMatrix();
   }
}
