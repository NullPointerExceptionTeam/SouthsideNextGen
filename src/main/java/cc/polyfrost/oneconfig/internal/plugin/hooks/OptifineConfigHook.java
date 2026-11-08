package cc.polyfrost.oneconfig.internal.plugin.hooks;

import cc.polyfrost.oneconfig.libs.universal.UScreen;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.gui.BlurScreen;

public class OptifineConfigHook {
   public static boolean shouldNotApplyFastRender() {
      return NanoVGHelper.INSTANCE.isDrawing()
         || UScreen.getCurrentScreen() instanceof BlurScreen && ((BlurScreen)UScreen.getCurrentScreen()).hasBackgroundBlur();
   }
}
