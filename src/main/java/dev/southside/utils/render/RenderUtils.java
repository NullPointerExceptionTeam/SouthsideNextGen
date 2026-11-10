package dev.southside.utils.render;

import dev.southside.MinecraftAccessor;
import dev.southside.mixin.accessors.WorldRendererAccessor;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;

public final class RenderUtils {
   public static boolean isInViewFrustrum(Entity entity) {
      Vec3d camera = MinecraftAccessor.mc.cameraEntity.getPos();
      return isInViewFrustrum(entity.getBoundingBox()) || entity.shouldRender(camera.x, camera.y, camera.z);
   }

   public static Frustum getFrustrum() {
      return ((WorldRendererAccessor)MinecraftAccessor.mc.worldRenderer).getFrustum();
   }

   public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
      float tickDelta = MinecraftAccessor.mc.getRenderTickCounter().getTickDelta(false);
      double x = prevposX + (posX - prevposX) * tickDelta - MinecraftAccessor.mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = prevposY + (posY - prevposY) * tickDelta - MinecraftAccessor.mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = prevposZ + (posZ - prevposZ) * tickDelta - MinecraftAccessor.mc.getEntityRenderDispatcher().camera.getPos().getZ();
      return new Vec3d(x, y, z);
   }

   public static boolean isInViewFrustrum(Box bb) {
      Camera current = MinecraftAccessor.mc.gameRenderer.getCamera();
      Frustum frustrum = getFrustrum();
      frustrum.setPosition(current.getPos().x, current.getPos().y, current.getPos().z);
      return frustrum.isVisible(bb);
   }

   @Generated
   private RenderUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
