package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.lang.reflect.Field;

public class ConfigInfo extends BasicOption {
   private final InfoType type;

   public ConfigInfo(Field field, Object parent, String name, String category, String subcategory, int size, InfoType type) {
      super(field, parent, name, "", category, subcategory, size);
      this.type = type;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      Scissor scissor = scissorHelper.scissor(vg, x, y, this.size == 1 ? 448.0F : 960.0F, 32.0F);
      nanoVGHelper.drawInfo(vg, this.type, x, y + 4, 24.0F);
      nanoVGHelper.drawText(vg, this.name, x + 32, y + 18, this.nameColor, 14.0F, Fonts.MEDIUM);
      scissorHelper.resetScissor(vg, scissor);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
