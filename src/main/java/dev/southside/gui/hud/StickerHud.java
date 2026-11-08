package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.Southside;
import dev.southside.manager.StickerManager;
import net.minecraft.class_1921;
import net.minecraft.class_332;

public class StickerHud extends Hud {
   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      StickerManager manager = Southside.get().getStickerManager();
      if (manager != null) {
         StickerManager.StickerRenderInfo info = manager.getCurrentFrame();
         if (info == null) {
            if (example) {
               context.method_25294((int)x, (int)y, (int)(x + 64.0F * scale), (int)(y + 64.0F * scale), 1090519039);
            }
         } else {
            int drawW = (int)(info.drawWidth() * scale);
            int drawH = (int)(info.drawHeight() * scale);
            context.method_25290(class_1921::method_62277, info.textureId(), (int)x, (int)y, 0.0F, 0.0F, drawW, drawH, drawW, drawH);
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
