package dev.southside.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;

public class RenderBuffer {
   private final Immediate mConsumers;
   private Matrix4f mMatrix = new Matrix4f().identity();

   public RenderBuffer(int bufferSize) {
      BufferAllocator allocator = new BufferAllocator(bufferSize);
      this.mConsumers = VertexConsumerProvider.immediate(allocator);
   }

   public RenderBuffer use(MatrixStack stack) {
      return this.use(stack.peek().getPositionMatrix());
   }

   public RenderBuffer use(Matrix4f mat) {
      this.mMatrix = mat;
      return this;
   }

   public Matrix4f getPositionMat() {
      return this.mMatrix;
   }

   public Immediate getVertexConsumer() {
      return this.mConsumers;
   }
}
