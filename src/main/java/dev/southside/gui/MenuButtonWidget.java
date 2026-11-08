package dev.southside.gui;

import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4264;
import net.minecraft.class_5250;
import net.minecraft.class_6382;
import net.minecraft.class_7919;
import org.jetbrains.annotations.Nullable;

public final class MenuButtonWidget extends class_4264 {
   private final ColorAnimation backgroundColor = new ColorAnimation(
      new ColorPalette(new Color(32, 32, 32, 150).getRGB(), new Color(255, 255, 255, 100).getRGB(), new Color(21, 21, 21, 205).getRGB()), 200
   );
   private static final MenuButtonWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> textSupplier.get();
   private final MenuButtonWidget.PressAction onPress;
   private final MenuButtonWidget.NarrationSupplier narrationSupplier;
   private final MenuButtonWidget.Position position;

   public static MenuButtonWidget.Builder builder(class_2561 message, MenuButtonWidget.PressAction onPress, MenuButtonWidget.Position position) {
      return new MenuButtonWidget.Builder(message, onPress, position);
   }

   public MenuButtonWidget(
      int x,
      int y,
      int width,
      int height,
      MenuButtonWidget.Position position,
      class_2561 message,
      MenuButtonWidget.PressAction onPress,
      MenuButtonWidget.NarrationSupplier narrationSupplier
   ) {
      super(x, y, width, height, message);
      this.onPress = onPress;
      this.narrationSupplier = narrationSupplier;
      this.position = position;
   }

   public void method_25306() {
      this.onPress.onPress(this);
   }

   protected class_5250 method_25360() {
      return this.narrationSupplier.createNarrationMessage(() -> super.method_25360());
   }

   protected void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      if (this.field_22764) {
         NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
         ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
         int fontSize = Math.max(10, Math.round(this.field_22759 * 0.4F));
         float corner = this.field_22759 * 0.22F;
         int sideClip = Math.max(6, Math.round(this.field_22759 * 0.3F));
         boolean hovered = withinBox(this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, mouseX, mouseY);
         int baseColor = this.backgroundColor.getColor(hovered, this.method_25370());
         if (!this.field_22763) {
            Color c = new Color(baseColor, true);
            baseColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(120, c.getAlpha() / 2)).getRGB();
         }

         int textColor = this.field_22763 ? new Color(255, 255, 255, 190).getRGB() : new Color(255, 255, 255, 90).getRGB();
         int finalBaseColor = baseColor;
         nanovg.setupAndDraw(
            true,
            vg -> {
               float textWidth = nanovg.getTextWidth(vg, this.method_25369().getString(), fontSize, Fonts.REGULAR);
               nanovg.drawRoundedRect(vg, this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, finalBaseColor, corner);
               switch (this.position) {
                  case LEFT:
                     nanovg.drawText(
                        vg,
                        this.method_25369().getString(),
                        this.method_46426() + this.field_22759 / 2.0F,
                        this.method_46427() + this.field_22759 / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     scissorHelper.scissor(vg, this.method_46426(), this.method_46427(), sideClip, this.field_22759);
                     nanovg.drawRoundedRect(vg, this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, finalBaseColor, corner);
                     scissorHelper.clearScissors(vg);
                     break;
                  case CENTER:
                     nanovg.drawCenteredText(
                        vg,
                        this.method_25369().getString(),
                        this.method_46426() + this.field_22758 / 2.0F,
                        this.method_46427() + this.field_22759 / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     break;
                  case RIGHT:
                     nanovg.drawText(
                        vg,
                        this.method_25369().getString(),
                        this.method_46426() + this.field_22758 - this.field_22759 / 2.0F - textWidth,
                        this.method_46427() + this.field_22759 / 2.0F,
                        textColor,
                        fontSize,
                        Fonts.REGULAR
                     );
                     scissorHelper.scissor(vg, this.method_46426() + this.field_22758 - sideClip, this.method_46427(), sideClip, this.field_22759);
                     nanovg.drawRoundedRect(vg, this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, finalBaseColor, corner);
                     scissorHelper.clearScissors(vg);
               }
            }
         );
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.field_22763
         && this.field_22764
         && withinBox(this.method_46426(), this.method_46427(), this.field_22758, this.field_22759, (int)mouseX, (int)mouseY)) {
         this.method_25348(mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   protected void method_47399(class_6382 builder) {
      this.method_37021(builder);
   }

   public static boolean withinBox(int x, int y, int w, int h, int mouseX, int mouseY) {
      return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
   }

   public static class Builder {
      private final class_2561 message;
      private final MenuButtonWidget.PressAction onPress;
      @Nullable
      private class_7919 tooltip;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private MenuButtonWidget.Position position;
      private MenuButtonWidget.NarrationSupplier narrationSupplier = MenuButtonWidget.DEFAULT_NARRATION_SUPPLIER;

      public Builder(class_2561 message, MenuButtonWidget.PressAction onPress, MenuButtonWidget.Position position) {
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

      public MenuButtonWidget.Builder tooltip(class_7919 tooltip) {
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
         buttonWidget.method_47400(this.tooltip);
         return buttonWidget;
      }
   }

   public interface NarrationSupplier {
      class_5250 createNarrationMessage(Supplier<class_5250> var1);
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
