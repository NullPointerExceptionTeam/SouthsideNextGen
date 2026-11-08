package cc.polyfrost.oneconfig.gui.pages;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;

public class CreditsPage extends Page {
   public CreditsPage() {
      super("Credits");
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      nanoVGHelper.drawSvg(vg, SVGs.SOUTHSIDE, x + 15.0F, (float)y, 110.0F, 110.0F);
      y += 10;
      nanoVGHelper.drawText(vg, "OneConfig 兼容层编写团队", x + 20, y + 110, -1, 24.0F, Fonts.SEMIBOLD);
      nanoVGHelper.drawText(vg, " - 花花派对 & ...", x + 20, y + 135, -1, 12.0F, Fonts.REGULAR);
      nanoVGHelper.drawText(vg, "版权说明", x + 20, y + 175, -1, 24.0F, Fonts.SEMIBOLD);
      nanoVGHelper.drawText(vg, "   OneConfig 以 LGPL 协议开源于 https://github.com/Polyfrost/OneConfig。", x + 20, y + 200, -1, 12.0F, Fonts.REGULAR);
      nanoVGHelper.drawText(vg, "   SouthsideNG 团队以编写 fabric 兼容层的方法给客户端导入了 OneConfig 作为配置管理系统。", x + 20, y + 215, -1, 12.0F, Fonts.REGULAR);
      nanoVGHelper.drawText(vg, "   符合 LGPL 的标准，仅仅用到了 OneConfig 的功能，故可保持本软件闭源。", x + 20, y + 230, -1, 12.0F, Fonts.REGULAR);
      y += 240;
      nanoVGHelper.drawSvg(vg, SVGs.ONECONFIG_FULL_DARK, x + 15.0F, y + 20.0F, 474.0F, 102.0F);
      y -= 52;
      String[] dev_lines = new String[]{
         " - Wyvest - OG Team - Gradle, NanoVGHelper, VCAL, Utilities, GUI Frontend",
         " - Caledonian - Designer",
         " - nextdaydelivery - OG Team - GUI Frontend, NanoVGHelper, Utilities",
         " - Pauline - Utilities",
         " - DeDiamondPro - OG Team -  Config Backend, GUI Frontend, HUD",
         " - xtrm - Multiversion support, GUI Frontend",
         " - MoonTidez - OG Team - Designer"
      };
      float yy = y + 205;

      for (String l : dev_lines) {
         nanoVGHelper.drawText(vg, l, x + 20, yy, -1, 12.0F, Fonts.REGULAR);
         yy += 15.0F;
      }

      nanoVGHelper.drawText(vg, "Libraries", x + 20, y + 333, -1, 24.0F, Fonts.SEMIBOLD);
      String[] lib_lines = new String[]{
         " - #getResourceAsStream (SpinyOwl) - IO Utility and shadow", " - NanoVG (memononen) - NanoVG Library", " - https://easings.net/ - Easing functions"
      };
      yy = y + 355;

      for (String l : lib_lines) {
         nanoVGHelper.drawText(vg, l, x + 20, yy, -1, 12.0F, Fonts.REGULAR);
         yy += 15.0F;
      }
   }

   @Override
   public boolean isBase() {
      return true;
   }
}
