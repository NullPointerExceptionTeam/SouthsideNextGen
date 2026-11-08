package cc.polyfrost.oneconfig.internal.hud.utils;

import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import java.awt.Color;

public class SnappingLine {
   private static final int COLOR = new Color(138, 43, 226).getRGB();
   private final float line;
   private final float distance;
   private final float position;

   public SnappingLine(float line, float left, float size, boolean multipleSides) {
      this.line = line;
      float center = left + size / 2.0F;
      float right = left + size;
      float leftDistance = Math.abs(line - left);
      float centerDistance = Math.abs(line - center);
      float rightDistance = Math.abs(line - right);
      if (multipleSides && (!(leftDistance <= centerDistance) || !(leftDistance <= rightDistance))) {
         if (centerDistance <= rightDistance) {
            this.distance = centerDistance;
            this.position = line - size / 2.0F;
         } else {
            this.distance = rightDistance;
            this.position = line - size;
         }
      } else {
         this.distance = leftDistance;
         this.position = line;
      }
   }

   public void drawLine(long vg, float lineWidth, boolean isX) {
      float pos = this.line * UResolution.getScaleFactor() - lineWidth / 2.0F;
      if (isX) {
         NanoVGHelper.INSTANCE.drawLine(vg, pos, 0.0F, pos, UResolution.getWindowHeight(), lineWidth, COLOR);
      } else {
         NanoVGHelper.INSTANCE.drawLine(vg, 0.0F, pos, UResolution.getWindowWidth(), pos, lineWidth, COLOR);
      }
   }

   public float getPosition() {
      return this.position;
   }

   public float getDistance() {
      return this.distance;
   }
}
