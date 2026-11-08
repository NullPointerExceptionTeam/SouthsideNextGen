package dev.southside.gui.hud.jello;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import java.util.Comparator;

public class ModuleSorter implements Comparator<ActiveMods.ModuleNode> {
   private final Font font;

   public ModuleSorter(Font font) {
      this.font = font;
   }

   public int compare(ActiveMods.ModuleNode var1, ActiveMods.ModuleNode var2) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float[] sizes = new float[2];
      nanoVGHelper.setupAndDraw(true, vg -> {
         sizes[0] = nanoVGHelper.getTextWidth(vg, var1.getModule().getName(), 20.0F, this.font);
         sizes[1] = nanoVGHelper.getTextWidth(vg, var2.getModule().getName(), 20.0F, this.font);
      });
      if (sizes[0] <= sizes[1]) {
         return sizes[0] != sizes[1] ? 1 : 0;
      } else {
         return -1;
      }
   }
}
