package dev.southside.render;

import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_9799;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

public class RenderBuffer {
   private final class_4598 mConsumers;
   private Matrix4f mMatrix = new Matrix4f().identity();

   public RenderBuffer(int bufferSize) {
      class_9799 allocator = new class_9799(bufferSize);
      this.mConsumers = class_4597.method_22991(allocator);
   }

   public RenderBuffer use(class_4587 stack) {
      return this.use(stack.method_23760().method_23761());
   }

   public RenderBuffer use(Matrix4f mat) {
      this.mMatrix = mat;
      return this;
   }

   public Matrix4f getPositionMat() {
      return this.mMatrix;
   }

   public class_4598 getVertexConsumer() {
      return this.mConsumers;
   }
}
