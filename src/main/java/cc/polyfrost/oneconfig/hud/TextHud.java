package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public abstract class TextHud extends BasicHud {
   protected transient List<String> lines = new ArrayList<>();
   public Color color = new Color("Text Color", new OneColor(255, 255, 255));
   public Dropdown textType = new Dropdown("Text Type", new String[]{"No Shadow", "Shadow", "Full Shadow"}, "", 1, 0);

   public TextHud(
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
      EventManager.INSTANCE.register(new TextHud.TickHandler());
   }

   public TextHud(int x, int y) {
      super(x, y);
      EventManager.INSTANCE.register(new TextHud.TickHandler());
   }

   public TextHud() {
      this(0, 0);
   }

   protected abstract void getLines(List<String> var1, boolean var2);

   protected void getLinesFrequent(List<String> lines, boolean example) {
   }

   @Override
   public void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (this.lines != null && this.lines.size() != 0) {
         float textY = y;

         for (String line : this.lines) {
            this.drawLine(line, x, textY, scale);
            textY += 12.0F * scale;
         }
      }
   }

   protected void drawLine(String line, float x, float y, float scale) {
      TextRenderer.drawScaledString(line, x, y, this.color.getValue().getRGB(), TextRenderer.TextType.toType(this.textType.getValue()), scale);
   }

   protected float getLineWidth(String line, float scale) {
      return Platform.getGLPlatform().getStringWidth(line) * scale;
   }

   @Override
   protected void preRender(boolean example) {
      this.getLinesFrequent(this.lines, example);
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      if (this.lines == null) {
         return 0.0F;
      } else {
         float width = 0.0F;

         for (String line : this.lines) {
            width = Math.max(width, this.getLineWidth(line, scale));
         }

         return width;
      }
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return this.lines == null ? 0.0F : (this.lines.size() * 12 - 4) * scale;
   }

   @Override
   public boolean shouldDrawBackground() {
      return super.shouldDrawBackground() && this.lines != null && this.lines.size() > 0;
   }

   private class TickHandler {
      @EventHandler
      private void onTick(TickEvent event) {
         if (event.getState() == Stage.PRE && TextHud.this.isEnabled()) {
            TextHud.this.lines.clear();
            TextHud.this.getLines(TextHud.this.lines, HudCore.editing);
         }
      }
   }
}
