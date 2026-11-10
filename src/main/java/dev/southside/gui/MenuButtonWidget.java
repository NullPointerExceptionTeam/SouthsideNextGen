package dev.southside.gui;

import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import org.jetbrains.annotations.Nullable;

public final class MenuButtonWidget extends PressableWidget {
   private final ColorAnimation backgroundColor = new ColorAnimation(
      new ColorPalette(new Color(32, 32, 32, 150).getRGB(), new Color(255, 255, 255, 100).getRGB(), new Color(21, 21, 21, 205).getRGB()), 200
   );
   private static final MenuButtonWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> textSupplier.get();
   private final MenuButtonWidget.PressAction onPress;
   private final MenuButtonWidget.NarrationSupplier narrationSupplier;
   private final MenuButtonWidget.Position position;

   public static MenuButtonWidget.Builder builder(Text message, MenuButtonWidget.PressAction onPress, MenuButtonWidget.Position position) {
      return new MenuButtonWidget.Builder(message, onPress, position);
   }

   public MenuButtonWidget(
      int x,
      int y,
      int width,
      int height,
      MenuButtonWidget.Position position,
      Text message,
      MenuButtonWidget.PressAction onPress,
      MenuButtonWidget.NarrationSupplier narrationSupplier
   ) {
      super(x, y, width, height, message);
      this.onPress = onPress;
      this.narrationSupplier = narrationSupplier;
      this.position = position;
   }

   public void onPress() {
      this.onPress.onPress(this);
   }

   protected MutableText getNarrationMessage() {
      return this.narrationSupplier.createNarrationMessage(() -> super.getNarrationMessage());
   }

   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      if (this.visible) {
         NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         int fontSize = Math.max(10, Math.round(this.height * 0.4F));
         float corner = this.height * 0.22F;
         int sideClip = Math.max(6, Math.round(this.height * 0.3F));
         boolean hovered = withinBox(this.getX(), this.getY(), this.width, this.height, mouseX, mouseY);
         int baseColor = this.backgroundColor.getColor(hovered, this.isFocused());
         if (!this.active) {
            Color c = new Color(baseColor, true);
            baseColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(120, c.getAlpha() / 2)).getRGB();
         }

         int textColor = this.active ? new Color(255, 255, 255, 190).getRGB() : new Color(255, 255, 255, 90).getRGB();
         int finalBaseColor = baseColor;
         nanovg.setupAndDraw(
            true,
            vg -> {
               float textWidth = nanovg.getTextWidth(vg, this.getMessage().getString(), fontSize, Fonts.REGULAR);
               nanovg.drawRoundedRect(vg, this.getX(), this.getY(), this.width, this.height, finalBaseColor, corner);
               switch (this.position) {
                  case LEFT:
                     nanovg.drawText(
                        vg,
                        this.getMessage().getString(),
                        this.getX() + this.height / 2.0F,
                        this.getY() + this.height / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     scissorHelper.scissor(vg, this.getX(), this.getY(), sideClip, this.height);
                     nanovg.drawRoundedRect(vg, this.getX(), this.getY(), this.width, this.height, finalBaseColor, corner);
                     scissorHelper.clearScissors(vg);
                     break;
                  case CENTER:
                     nanovg.drawCenteredText(
                        vg,
                        this.getMessage().getString(),
                        this.getX() + this.width / 2.0F,
                        this.getY() + this.height / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     break;
                  case RIGHT:
                     nanovg.drawText(
                        vg,
                        this.getMessage().getString(),
                        this.getX() + this.width - this.height / 2.0F - textWidth,
                        this.getY() + this.height / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     scissorHelper.scissor(vg, this.getX() + this.width - sideClip, this.getY(), sideClip, this.height);
                     nanovg.drawRoundedRect(vg, this.getX(), this.getY(), this.width, this.height, finalBaseColor, corner);
                     scissorHelper.clearScissors(vg);
               }
            }
         );
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.active
         && this.visible
         && withinBox(this.getX(), this.getY(), this.width, this.height, (int)mouseX, (int)mouseY)) {
         this.onClick(mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
      this.appendDefaultNarrations(builder);
   }

   public static boolean withinBox(int x, int y, int w, int h, int mouseX, int mouseY) {
      return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
   }

   public static class Builder {
      private final Text message;
      private final MenuButtonWidget.PressAction onPress;
      @Nullable
      private Tooltip tooltip;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private MenuButtonWidget.Position position;
      private MenuButtonWidget.NarrationSupplier narrationSupplier = MenuButtonWidget.DEFAULT_NARRATION_SUPPLIER;

      public Builder(Text message, MenuButtonWidget.PressAction onPress, MenuButtonWidget.Position position) {
         this.message = message;
         this.onPress = onPress;
         this.position = position;
      }

      public MenuButtonWidget.Builder position(int x, int y) {
         this.x = x;
         this.y = y;
         return this;
      }

      public MenuButtonWidget.Builder width(int width) {
         this.width = width;
         return this;
      }

      public MenuButtonWidget.Builder size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public MenuButtonWidget.Builder dimensions(int x, int y, int width, int height) {
         return this.position(x, y).size(width, height);
      }

      public MenuButtonWidget.Builder tooltip(Tooltip tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public MenuButtonWidget.Builder narrationSupplier(MenuButtonWidget.NarrationSupplier narrationSupplier) {
         this.narrationSupplier = narrationSupplier;
         return this;
      }

      public MenuButtonWidget.Builder position(MenuButtonWidget.Position position) {
         this.position = position;
         return this;
      }

      public MenuButtonWidget build() {
         MenuButtonWidget buttonWidget = new MenuButtonWidget(
            this.x, this.y, this.width, this.height, this.position, this.message, this.onPress, this.narrationSupplier
         );
         buttonWidget.setTooltip(this.tooltip);
         return buttonWidget;
      }
   }

   public interface NarrationSupplier {
      MutableText createNarrationMessage(Supplier<MutableText> var1);
   }

   public static enum Position {
      LEFT,
      CENTER,
      RIGHT;
   }

   public interface PressAction {
      void onPress(MenuButtonWidget var1);
   }
}
