package cc.polyfrost.oneconfig.libs.universal;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class UMatrixStack {
   private final Deque<MatrixPair> stack;
   public static final UMatrixStack UNIT = new UMatrixStack();

   private UMatrixStack(Deque<MatrixPair> stack) {
      this.stack = stack;
   }

   public UMatrixStack() {
      ArrayDeque<MatrixPair> stack = new ArrayDeque<>();
      stack.add(new MatrixPair(new Matrix4f().identity(), new Matrix3f().identity()));
      this.stack = stack;
   }

   public final void translate(double x, double y, double z) {
      this.translate((float)x, (float)y, (float)z);
   }

   public void translate(float x, float y, float z) {
      if (x != 0.0F || y != 0.0F || z != 0.0F) {
         this.stack.getLast().model().translate(x, y, z);
      }
   }

   public void scale(double x, double y, double z) {
      this.scale((float)x, (float)y, (float)z);
   }

   public void scale(float x, float y, float z) {
      if (x != 1.0F || y != 1.0F || z != 1.0F) {
         MatrixPair pair = this.stack.getLast();
         pair.model().scale(x, y, z);
         if (x != y || y != z) {
            float ix = 1.0F / x;
            float iy = 1.0F / y;
            float iz = 1.0F / z;
            float rt = (float)Math.cbrt(ix * iy * iz);
            Matrix3f scale = new Matrix3f();
            scale.m00 = rt * ix;
            scale.m11 = rt * iy;
            scale.m22 = rt * iz;
            pair.normal().mul(scale);
         } else if (x < 0.0F) {
            pair.normal().negateX().negateY().negateZ();
         }
      }
   }

   public final void rotate(float angle, float x, float y, float z, boolean degrees) {
      if (angle != 0.0F) {
         MatrixPair pair = this.stack.getLast();
         float angleRadians = degrees ? (float)Math.toRadians(angle) : angle;
         Vector3f axis = new Vector3f(x, y, z);
         pair.model().rotate(angleRadians, x, y, z);
         pair.normal().mul(makeRotationMatrix(angleRadians, axis));
      }
   }

   public static void rotate$default(UMatrixStack var0, float var1, float var2, float var3, float var4, boolean var5, int var6, Object var7) {
      if ((var6 & 16) != 0) {
         var5 = true;
      }

      var0.rotate(var1, var2, var3, var4, var5);
   }

   public void multiply(Quaternionfc quaternion) throws Throwable {
      MatrixPair pair = this.stack.getLast();
      pair.model().rotate(quaternion);
   }

   public UMatrixStack fork() {
      ArrayDeque<MatrixPair> stack = new ArrayDeque<>();
      stack.add(new MatrixPair(new Matrix4f().set(this.stack.getLast().model()), new Matrix3f().set(this.stack.getLast().normal())));
      return new UMatrixStack(stack);
   }

   public void push() {
      this.stack.addLast(new MatrixPair(new Matrix4f().set(this.stack.getLast().model()), new Matrix3f().set(this.stack.getLast().normal())));
   }

   public void pop() {
      this.stack.removeLast();
   }

   public MatrixPair peek() {
      return this.stack.getLast();
   }

   public boolean isEmpty() {
      return this.stack.size() == 1;
   }

   public void applyToGlobalState() {
      RenderSystem.getModelViewStack().mul(this.stack.getLast().model());
   }

   public void replaceGlobalState() {
      this.applyToGlobalState();
   }

   public Object runWithGlobalState(Callable<Object> block) {
      UGraphics.GL.pushMatrix();
      this.applyToGlobalState();
      Object obj = null;

      try {
         obj = block.call();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      UGraphics.GL.popMatrix();
      return obj;
   }

   public final Object runReplacingGlobalState(Callable<Object> block) {
      UGraphics.GL.pushMatrix();
      this.replaceGlobalState();
      Object obj = null;

      try {
         obj = block.call();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      UGraphics.GL.popMatrix();
      return obj;
   }

   private Object withGlobalStackPushed(Callable<Object> block) {
      UGraphics.GL.pushMatrix();
      Object obj = null;

      try {
         obj = block.call();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      boolean var5 = false;
      UGraphics.GL.popMatrix();
      return obj;
   }

   public final void rotate(float angle, float x, float y, float z) {
      rotate$default(this, angle, x, y, z, false, 16, null);
   }

   private static Matrix3f makeRotationMatrix(float angle, Vector3f axis) {
      Matrix3f matrix = new Matrix3f();
      float c = (float)Math.cos(angle);
      float s = (float)Math.sin(angle);
      float oneMinusC = 1.0F - c;
      float xx = axis.x * axis.x;
      float xy = axis.x * axis.y;
      float xz = axis.x * axis.z;
      float yy = axis.y * axis.y;
      float yz = axis.y * axis.z;
      float zz = axis.z * axis.z;
      float xs = axis.x * s;
      float ys = axis.y * s;
      float zs = axis.z * s;
      matrix.m00 = xx * oneMinusC + c;
      matrix.m01 = xy * oneMinusC + zs;
      matrix.m02 = xz * oneMinusC - ys;
      matrix.m10 = xy * oneMinusC - zs;
      matrix.m11 = yy * oneMinusC + c;
      matrix.m12 = yz * oneMinusC + xs;
      matrix.m20 = xz * oneMinusC + ys;
      matrix.m21 = yz * oneMinusC - xs;
      matrix.m22 = zz * oneMinusC + c;
      return matrix;
   }

   public static final class Compat {
      public static final UMatrixStack.Compat INSTANCE = new UMatrixStack.Compat();
      public static final String DEPRECATED = "For 1.17 this method requires you pass a UMatrixStack as the first argument.\n\nIf you are currently extending this method, you should instead extend the method with the added argument.\nNote however for this to be non-breaking, your parent class needs to transition before you do.\n\nIf you are calling this method and you cannot guarantee that your target class has been fully updated (such as when\ncalling an open method on an open class), you should instead call the method with the \"Compat\" suffix, which will\ncall both methods, the new and the deprecated one.\nIf you are sure that your target class has been updated (such as when calling the super method), you should\n(for super calls you must!) instead just call the method with the original name and added argument.";
      private static final List<UMatrixStack> stack = new ArrayList<>();

      public <R> R runLegacyMethod(UMatrixStack matrixStack, Callable<Object> block) {
         stack.add(matrixStack);
         R r = null;

         try {
            r = (R)block.call();
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         stack.remove(stack.size() - 1);
         return r;
      }

      public final UMatrixStack get() {
         return stack.isEmpty() ? new UMatrixStack() : stack.get(stack.size() - 1);
      }
   }
}
