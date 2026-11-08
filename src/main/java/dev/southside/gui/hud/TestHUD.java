package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import java.awt.Color;
import net.minecraft.class_2561;
import net.minecraft.class_332;

public class TestHUD extends Hud {
   private static final class_2561 COLON_TEXT = class_2561.method_43470(": ");

   public TestHUD(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      nanoVGHelper.setupAndDraw(
         true,
         vg -> nanoVGHelper.drawSmoothRoundRect(
            vg, x, y, this.getWidth(scale, example), this.getHeight(scale, example), new Color(0, 0, 255, 200).getRGB(), 40.0F
         )
      );
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return 400.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return 200.0F * scale;
   }
}
