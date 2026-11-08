package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.FontHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import org.lwjgl.nanovg.NanoVG;

public class FontHelperImpl implements FontHelper {
   @Override
   public void initialize(long vg) {
      for (Font font : Fonts.FONTS) {
         try {
            this.loadFont(vg, font);
         } catch (Exception var6) {
            var6.printStackTrace();
            throw new RuntimeException("Could not initialize fonts");
         }
      }
   }

   @Override
   public void loadFont(long vg, Font font) {
      if (!font.isLoaded()) {
         font.setId(NanoVG.nvgCreateFontMem(vg, font.getName(), font.getBuffer(), false));
         if (font.getId() == -1) {
            throw new RuntimeException("Failed to initialize font " + font.getName());
         } else {
            font.setLoaded(true);
         }
      }
   }
}
