package cc.polyfrost.oneconfig.libs.universal;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Quaternionf;

public class UGraphics {
   public class GL {
      public static void pushMatrix() {
         RenderSystem.getModelViewStack().pushMatrix();
      }

      public static void popMatrix() {
         RenderSystem.getModelViewStack().popMatrix();
      }

      public static void translate(float x, float y, float z) {
         translate((double)x, (double)y, (double)z);
      }

      public static void translate(double x, double y, double z) {
         RenderSystem.getModelViewStack().translate((float)x, (float)y, (float)z);
      }

      public static void rotate(float angle, float x, float y, float z) {
         RenderSystem.getModelViewStack().rotate(new Quaternionf(x, y, z, angle));
      }

      public static void scale(float x, float y, float z) {
         RenderSystem.getModelViewStack().scale(x, y, z);
      }

      public static void scale(double x, double y, double z) {
         RenderSystem.getModelViewStack().scale((float)x, (float)y, (float)z);
      }
   }
}
