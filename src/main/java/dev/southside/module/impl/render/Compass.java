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
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class Compass extends Module {
   public Switch compassOnly = new Switch("Compass Only", true);
   public Switch noPlayerOnly = new Switch("No Player Only", true);
   private boolean hasCompass = false;
   private class_2338 spawnPosition;
   private float renderYaw;
   private double renderX;
   private double renderZ;

   public Compass() {
      super(Category.Render, "Compass");
   }

   private class_2338 getSpawnPosition(class_638 level) {
      return new class_2338(level.method_43126().method_10263(), level.method_43126().method_10264(), level.method_43126().method_10260());
   }

   private boolean hasPlayer() {
      if (ClientUtils.nullCheck()) {
         return false;
      } else {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity != mc.field_1724 && entity instanceof class_1657) {
               return true;
            }
         }

         return false;
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!ClientUtils.nullCheck()) {
         this.spawnPosition = this.getSpawnPosition(mc.field_1687);

         for (int a = 0; a <= 44; a++) {
            if (mc.field_1724.method_31548().method_5438(a).method_7909() == class_1802.field_8251) {
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
         this.renderX = class_3532.method_16436(event.getTickCounter(), mc.field_1724.field_6038, mc.field_1724.method_23317());
         this.renderZ = class_3532.method_16436(event.getTickCounter(), mc.field_1724.field_5989, mc.field_1724.method_23321());
         this.renderYaw = class_3532.method_16439(event.getTickCounter(), mc.field_1724.field_3931, mc.field_1724.field_3932);
      }
   }

   @EventHandler
   public void onRender2D(Render2DEvent event) {
      this.draw(event.getDrawContext().method_51448());
   }

   private void draw(class_4587 stack) {
      if (!ClientUtils.nullCheck()) {
         if (this.hasCompass || !this.compassOnly.getValue()) {
            if (!this.hasPlayer() || !this.noPlayerOnly.getValue()) {
               if (this.spawnPosition != null) {
                  float yaw = (float)(
                     Math.toDegrees(Math.atan2(this.spawnPosition.method_10260() - this.renderZ, this.spawnPosition.method_10263() - this.renderX))
                        - 90.0
                        - this.renderYaw
                  );
                  float x = mc.method_22683().method_4486() / 2.0F;
                  float y = mc.method_22683().method_4502() / 2.0F;
                  MSAA.use(() -> {
                     stack.method_22903();
                     stack.method_46416(x, y, 0.0F);
                     stack.method_22907(class_7833.field_40718.rotationDegrees(yaw));
                     stack.method_46416(-x, -y, 0.0F);
                     drawTracer(stack, x, y - 45.0F, 10.0F, 2.0F, 1.0F, -1);
                     stack.method_46416(x, y, 0.0F);
                     stack.method_22907(class_7833.field_40718.rotationDegrees(-yaw));
                     stack.method_46416(-x, -y, 0.0F);
                     stack.method_22909();
                  });
               }
            }
         }
      }
   }

   public static void drawTracer(class_4587 poseStack, float x, float y, float size, float widthDiv, float heightDiv, int color) {
      Matrix4f matrix = poseStack.method_23760().method_23761();
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      RenderSystem.setShader(class_10142.field_53876);
      class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
      bufferBuilder.method_22918(matrix, x, y, 0.0F).method_22915(r, g, b, a);
      bufferBuilder.method_22918(matrix, x - size / widthDiv, y + size, 0.0F).method_22915(r, g, b, a);
      bufferBuilder.method_22918(matrix, x, y + size / heightDiv, 0.0F).method_22915(r, g, b, a);
      bufferBuilder.method_22918(matrix, x + size / widthDiv, y + size, 0.0F).method_22915(r, g, b, a);
      bufferBuilder.method_22918(matrix, x, y, 0.0F).method_22915(r, g, b, a);
      class_286.method_43433(bufferBuilder.method_60800());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
