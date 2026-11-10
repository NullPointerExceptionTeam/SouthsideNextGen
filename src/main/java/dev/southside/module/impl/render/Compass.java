package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.MSAA;
import dev.southside.utils.misc.ClientUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public class Compass extends Module {
   public Switch compassOnly = new Switch("Compass Only", true);
   public Switch noPlayerOnly = new Switch("No Player Only", true);
   private boolean hasCompass = false;
   private BlockPos spawnPosition;
   private float renderYaw;
   private double renderX;
   private double renderZ;

   public Compass() {
      super(Category.Render, "Compass");
   }

   private BlockPos getSpawnPosition(ClientWorld level) {
      return new BlockPos(level.getSpawnPos().getX(), level.getSpawnPos().getY(), level.getSpawnPos().getZ());
   }

   private boolean hasPlayer() {
      if (ClientUtils.nullCheck()) {
         return false;
      } else {
         for (Entity entity : mc.world.getEntities()) {
            if (entity != mc.player && entity instanceof PlayerEntity) {
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!ClientUtils.nullCheck()) {
         this.spawnPosition = this.getSpawnPosition(mc.world);

         for (int a = 0; a <= 44; a++) {
            if (mc.player.getInventory().getStack(a).getItem() == Items.COMPASS) {
               this.hasCompass = true;
               return;
            }
         }

         this.hasCompass = false;
      }
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (!ClientUtils.nullCheck()) {
         this.renderX = MathHelper.lerp(event.getTickCounter(), mc.player.lastRenderX, mc.player.getX());
         this.renderZ = MathHelper.lerp(event.getTickCounter(), mc.player.lastRenderZ, mc.player.getZ());
         this.renderYaw = MathHelper.lerp(event.getTickCounter(), mc.player.lastRenderYaw, mc.player.renderYaw);
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      this.draw(event.getDrawContext().getMatrices());
   }

   private void draw(MatrixStack stack) {
      if (!ClientUtils.nullCheck()) {
         if (this.hasCompass || !this.compassOnly.getValue()) {
            if (!this.hasPlayer() || !this.noPlayerOnly.getValue()) {
               if (this.spawnPosition != null) {
                  float yaw = (float)(
                     Math.toDegrees(Math.atan2(this.spawnPosition.getZ() - this.renderZ, this.spawnPosition.getX() - this.renderX))
                        - 90.0
                        - this.renderYaw
                  );
                  float x = mc.getWindow().getScaledWidth() / 2.0F;
                  float y = mc.getWindow().getScaledHeight() / 2.0F;
                  MSAA.use(() -> {
                     stack.push();
                     stack.translate(x, y, 0.0F);
                     stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                     stack.translate(-x, -y, 0.0F);
                     drawTracer(stack, x, y - 45.0F, 10.0F, 2.0F, 1.0F, -1);
                     stack.translate(x, y, 0.0F);
                     stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                     stack.translate(-x, -y, 0.0F);
                     stack.pop();
                  });
               }
            }
         }
      }
   }

   public static void drawTracer(MatrixStack poseStack, float x, float y, float size, float widthDiv, float heightDiv, int color) {
      Matrix4f matrix = poseStack.peek().getPositionMatrix();
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x - size / widthDiv, y + size, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x, y + size / heightDiv, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x + size / widthDiv, y + size, 0.0F).color(r, g, b, a);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
