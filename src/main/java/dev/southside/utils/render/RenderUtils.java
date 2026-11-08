package dev.southside.utils.render;

import dev.southside.MinecraftAccessor;
import dev.southside.mixin.accessors.WorldRendererAccessor;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4604;

public final class RenderUtils {
   public static boolean isInViewFrustrum(class_1297 entity) {
      class_243 camera = MinecraftAccessor.mc.field_1719.method_19538();
      return isInViewFrustrum(entity.method_5829()) || entity.method_5727(camera.field_1352, camera.field_1351, camera.field_1350);
   }

   public static class_4604 getFrustrum() {
      return ((WorldRendererAccessor)MinecraftAccessor.mc.field_1769).getFrustum();
   }

   public static class_243 interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
      float tickDelta = MinecraftAccessor.mc.method_61966().method_60637(false);
      double x = prevposX + (posX - prevposX) * tickDelta - MinecraftAccessor.mc.method_1561().field_4686.method_19326().method_10216();
      double y = prevposY + (posY - prevposY) * tickDelta - MinecraftAccessor.mc.method_1561().field_4686.method_19326().method_10214();
      double z = prevposZ + (posZ - prevposZ) * tickDelta - MinecraftAccessor.mc.method_1561().field_4686.method_19326().method_10215();
      return new class_243(x, y, z);
   }

   public static boolean isInViewFrustrum(class_238 bb) {
      class_4184 current = MinecraftAccessor.mc.field_1773.method_19418();
      class_4604 frustrum = getFrustrum();
      frustrum.method_23088(current.method_19326().field_1352, current.method_19326().field_1351, current.method_19326().field_1350);
      return frustrum.method_23093(bb);
   }

   @Generated
   private RenderUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
