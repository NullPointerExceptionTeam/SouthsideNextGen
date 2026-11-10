package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.Southside;
import dev.southside.manager.StickerManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.DrawContext;

public class StickerHud extends Hud {
   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      StickerManager manager = Southside.get().getStickerManager();
      if (manager != null) {
         StickerManager.StickerRenderInfo info = manager.getCurrentFrame();
         if (info == null) {
            if (example) {
               context.fill((int)x, (int)y, (int)(x + 64.0F * scale), (int)(y + 64.0F * scale), 1090519039);
            }
         } else {
            int drawW = (int)(info.drawWidth() * scale);
            int drawH = (int)(info.drawHeight() * scale);
            context.drawTexture(RenderLayer::getGuiTextured, info.textureId(), (int)x, (int)y, 0.0F, 0.0F, drawW, drawH, drawW, drawH);
         }
      }
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      StickerManager manager = Southside.get().getStickerManager();
      if (manager != null) {
         StickerManager.StickerRenderInfo info = manager.getCurrentFrame();
         if (info != null) {
            return info.drawWidth() * scale;
         }
      }

      return 64.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      StickerManager manager = Southside.get().getStickerManager();
      if (manager != null) {
         StickerManager.StickerRenderInfo info = manager.getCurrentFrame();
         if (info != null) {
            return info.drawHeight() * scale;
         }
      }

      return 64.0F * scale;
   }
}
