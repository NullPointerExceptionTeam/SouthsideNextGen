package dev.southside.utils;

import dev.southside.event.events.Render3DEvent;
import dev.southside.orbit.EventHandler;
import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;

public class TargetController {
   private static LivingEntity prevTarget;
   private static LivingEntity target;
   public static int headContext = -1;

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if (target != null) {
         if (prevTarget != target) {
            headContext = -1;
            prevTarget = target;
         }
      } else {
         headContext = -1;
      }
   }

   public static void drawPlayerHead(long nvg, Identifier location, float x, float y, float width, float height, float radius) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (location != null && mc.getTextureManager().getTexture(location) != null) {
         int texture = mc.getTextureManager().getTexture(location).getGlId();
         if (headContext == -1) {
            headContext = NanoVGGL2.nvglCreateImageFromHandle(nvg, texture, (int)width, -((int)height), 0);
         } else {
            radius--;
            int image = headContext;
            NanoVG.nvgImageSize(nvg, image, new int[]{(int)width}, new int[]{-((int)height)});
            NVGPaint p = NVGPaint.calloc();
            float sizeMultiplier = 8.0F;
            float oy = y - height / 4.0F * sizeMultiplier / 2.0F + 0.1F;
            float ex = width * sizeMultiplier;
            float ey = height * sizeMultiplier;
            NanoVG.nvgImagePattern(nvg, x - width / 4.0F * sizeMultiplier / 2.0F, oy, ex, ey, 0.0F, image, 1.0F, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);
            NanoVG.nvgImagePattern(nvg, x - width * 3.25F * sizeMultiplier / 2.0F, oy, ex, ey, 0.0F, image, 1.0F, p);
            NanoVG.nvgBeginPath(nvg);
            NanoVG.nvgRoundedRect(nvg, x, y - 0.1F, width, height, radius);
            NanoVG.nvgFillPaint(nvg, p);
            NanoVG.nvgFill(nvg);
            NanoVG.nvgClosePath(nvg);
            p.free();
         }
      }
   }

   @Generated
   public static LivingEntity getPrevTarget() {
      return prevTarget;
   }

   @Generated
   public static LivingEntity getTarget() {
      return target;
   }

   @Generated
   public static void setTarget(LivingEntity target) {
      TargetController.target = target;
   }
}
