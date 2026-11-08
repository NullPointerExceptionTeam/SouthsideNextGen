package cc.polyfrost.oneconfig.gui.pages;

import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;

public class HomePage extends Page {
   public HomePage() {
      super("Home Dashboard");
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      nanoVGHelper.drawRoundedRect(vg, x, y, 184.0F, 36.0F, -1, 12.0F);
      nanoVGHelper.drawText(vg, "This is a cool string to test pages", x + 32, y + 72, -1, 36.0F, Fonts.BOLD);
      nanoVGHelper.drawRoundedRect(vg, x + 350, y + 310, 300.0F, 200.0F, Colors.PRIMARY_600, 14.0F);
      nanoVGHelper.drawSvg(vg, SVGs.INFO_CIRCLE, (float)(x + 20), (float)(y + 604), 24.0F, 24.0F);
      nanoVGHelper.drawText(vg, "Info", x + 52, y + 618, Colors.WHITE_90, 24.0F, Fonts.MEDIUM);
      nanoVGHelper.drawRoundedRect(vg, x + 16, y + 644, 1024.0F, 64.0F, Colors.GRAY_700, 20.0F);
      nanoVGHelper.drawURL(vg, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", x + 100, y + 205, 24.0F, Fonts.MEDIUM, inputHandler);
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
