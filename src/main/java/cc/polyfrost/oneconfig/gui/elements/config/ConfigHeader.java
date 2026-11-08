package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import java.lang.reflect.Field;

public class ConfigHeader extends BasicOption {
   public ConfigHeader(Field field, Object parent, String name, String category, String subcategory, int size) {
      super(field, parent, name, "", category, subcategory, size);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      Scissor scissor = scissorHelper.scissor(vg, x, y, this.size == 1 ? 480.0F : 992.0F, 32.0F);
      NanoVGHelper.INSTANCE.drawText(vg, this.name, x, y + 17, this.nameColor, 24.0F, Fonts.MEDIUM);
      scissorHelper.resetScissor(vg, scissor);
   }

   @Override
   public int getHeight() {
      return 32;
   }
}
