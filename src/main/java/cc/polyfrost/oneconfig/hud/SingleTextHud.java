package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public abstract class SingleTextHud extends TextHud {
   public Switch brackets = new Switch("Brackets", false);
   public Color bracketsColor = new Color("Brackets Color", new OneColor(-1));
   public Text title = new Text("Title", "", false, false, "", 1, "");
   protected Dropdown titleLocation = new Dropdown("Title Location", new String[]{"Left", "Right"}, "", 1, 0);

   public SingleTextHud(
      String title,
      boolean enabled,
      float x,
      float y,
      float scale,
      boolean background,
      boolean rounded,
      float cornerRadius,
      float paddingX,
      float paddingY,
      OneColor bgColor,
      boolean border,
      float borderSize,
      OneColor borderColor
   ) {
      super(x, y, scale, background, rounded, cornerRadius, paddingX, paddingY, bgColor, border, borderSize, borderColor);
      this.title.setValue(title);
   }

   public SingleTextHud(String title, boolean enabled, int x, int y) {
      this(title, enabled, x, y, 1.0F, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, 120), false, 2.0F, new OneColor(0, 0, 0));
   }

   public SingleTextHud(String title, boolean enabled) {
      this(title, enabled, 0, 0);
   }

   protected abstract String getText(boolean var1);

   protected String getTextFrequent(boolean example) {
      return null;
   }

   @Override
   protected void getLines(List<String> lines, boolean example) {
      lines.add(this.getCompleteText(this.getText(example)));
   }

   @Override
   protected void getLinesFrequent(List<String> lines, boolean example) {
      String text = this.getTextFrequent(example);
      if (text != null) {
         lines.clear();
         lines.add(this.getCompleteText(text));
      }
   }

   @Override
   public void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      float textX = x;
      if (this.brackets.getValue()) {
         this.drawLine("[", x, y, this.bracketsColor.getValue(), scale);
         textX = x + this.getLineWidth("[", scale);
      }

      this.drawLine(this.lines.get(0), textX, y, scale);
      if (this.brackets.getValue()) {
         textX += this.getLineWidth(this.lines.get(0), scale);
         this.drawLine("]", textX, y, this.bracketsColor.getValue(), scale);
      }
   }

   protected void drawLine(String line, float x, float y, OneColor color, float scale) {
      TextRenderer.drawScaledString(line, x, y, color.getRGB(), TextRenderer.TextType.toType(this.textType.getValue()), scale);
   }

   protected final String getCompleteText(String text) {
      boolean showTitle = !this.title.getValue().trim().isEmpty();
      StringBuilder builder = new StringBuilder();
      if (showTitle && this.titleLocation.getValue() == 0) {
         builder.append(this.title.getValue()).append(": ");
      }

      builder.append(text);
      if (showTitle && this.titleLocation.getValue() == 1) {
         builder.append(" ").append(this.title);
      }

      return builder.toString();
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      if (this.lines == null) {
         return 0.0F;
      } else {
         return !this.brackets.getValue() ? this.getLineWidth(this.lines.get(0), scale) : this.getLineWidth("[" + this.lines.get(0) + "]", scale);
      }
   }
}
