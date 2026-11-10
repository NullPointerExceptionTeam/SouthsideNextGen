package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import net.minecraft.client.gui.DrawContext;

public abstract class BasicHud extends Hud {
   public Switch background = new Switch("Background", "If the background of the HUD is enabled.", false);
   public Switch rounded = new Switch("Rounded corners", "If the background has rounded corners.", false);
   public Switch border = new Switch("Outline/border", "If the hud has an outline.", false);
   public Color bgColor = new Color("Background color:", "The color of the background.", new OneColor("#00000000"));
   public Color borderColor = new Color("Border color:", "The color of the border.", new OneColor("#00000000"));
   public Slider cornerRadius = new Slider("Corner radius:", 0.0F, 10.0F, 0, "The corner radius of the background.", false, 0);
   public Slider borderSize = new Slider("Border thickness:", 0.0F, 10.0F, 0, "The thickness of the outline.", false, 0);
   public Slider paddingX = new Slider("X-Padding", 0.0F, 10.0F, 0, "The horizontal padding of the HUD.", false, 0);
   public Slider paddingY = new Slider("Y-Padding", 0.0F, 10.0F, 0, "The vertical padding of the HUD.", false, 0);
   protected float defaultPaddingX;
   protected float defaultPaddingY;
   private boolean loaded = false;

   public BasicHud(
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
      super(x, y, scale);
      this.background.setValue(background);
      this.rounded.setValue(rounded);
      this.cornerRadius.setValue(cornerRadius);
      this.paddingX.setValue(paddingX);
      this.paddingY.setValue(paddingY);
      this.bgColor.setValue(bgColor);
      this.border.setValue(border);
      this.borderSize.setValue(borderSize);
      this.borderColor.setValue(borderColor);
      this.position.getValue().setSize(this.getWidth(scale, true) + paddingX * scale * 2.0F, this.getHeight(scale, true) + paddingY * scale * 2.0F);
      if (!this.loaded) {
         this.defaultPaddingX = paddingX;
         this.defaultPaddingY = paddingY;
         this.loaded = true;
      }
   }

   public BasicHud(float x, float y, float scale) {
      this(x, y, scale, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, 120), false, 2.0F, new OneColor(0, 0, 0));
   }

   public BasicHud(float x, float y) {
      this(x, y, 1.0F, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, 120), false, 2.0F, new OneColor(0, 0, 0));
   }

   public BasicHud() {
      this(0.0F, 0.0F, 1.0F, true, false, 2.0F, 5.0F, 5.0F, new OneColor(0, 0, 0, 120), false, 2.0F, new OneColor(0, 0, 0));
   }

   @Override
   public void drawAll(DrawContext context, UMatrixStack matrices, boolean example) {
      if (example || this.shouldShow()) {
         this.preRender(example);
         this.position
            .getValue()
            .setSize(
               this.getWidth(this.scale.getValue().floatValue(), example) + this.paddingX.getValue().floatValue() * this.scale.getValue().floatValue() * 2.0F,
               this.getHeight(this.scale.getValue().floatValue(), example) + this.paddingY.getValue().floatValue() * this.scale.getValue().floatValue() * 2.0F
            );
         if (this.shouldDrawBackground() && this.background.getValue()) {
            this.drawBackground(
               this.position.getValue().getX(),
               this.position.getValue().getY(),
               this.position.getValue().getWidth(),
               this.position.getValue().getHeight(),
               this.scale.getValue().floatValue()
            );
         }

         this.draw(
            context,
            matrices,
            this.position.getValue().getX() + this.paddingX.getValue().floatValue() * this.scale.getValue().floatValue(),
            this.position.getValue().getY() + this.paddingY.getValue().floatValue() * this.scale.getValue().floatValue(),
            this.scale.getValue().floatValue(),
            example
         );
      }
   }

   @Override
   protected void resetPosition() {
      this.paddingX.setValue(this.defaultPaddingX);
      this.paddingY.setValue(this.defaultPaddingY);
      super.resetPosition();
   }

   @Override
   public void setScale(float scale, boolean example) {
      this.scale.setValue(scale);
      this.position
         .getValue()
         .updateSizePosition(
            this.getWidth(scale, example) + this.paddingX.getValue().floatValue() * scale * 2.0F,
            this.getHeight(scale, example) + this.paddingY.getValue().floatValue() * scale * 2.0F
         );
   }

   protected boolean shouldDrawBackground() {
      return true;
   }

   protected void drawBackground(float x, float y, float width, float height, float scale) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      nanoVGHelper.setupAndDraw(
         true,
         vg -> {
            if (this.rounded.getValue()) {
               nanoVGHelper.drawRoundedRect(vg, x, y, width, height, this.bgColor.getValue().getRGB(), this.cornerRadius.getValue().floatValue() * scale);
               if (this.border.getValue()) {
                  nanoVGHelper.drawHollowRoundRect(
                     vg,
                     x - this.borderSize.getValue().floatValue() * scale,
                     y - this.borderSize.getValue().floatValue() * scale,
                     width + this.borderSize.getValue().floatValue() * scale,
                     height + this.borderSize.getValue().floatValue() * scale,
                     this.borderColor.getValue().getRGB(),
                     this.cornerRadius.getValue().floatValue() * scale,
                     this.borderSize.getValue().floatValue() * scale
                  );
               }
            } else {
               nanoVGHelper.drawRect(vg, x, y, width, height, this.bgColor.getValue().getRGB());
               if (this.border.getValue()) {
                  nanoVGHelper.drawHollowRoundRect(
                     vg,
                     x - this.borderSize.getValue().floatValue() * scale,
                     y - this.borderSize.getValue().floatValue() * scale,
                     width + this.borderSize.getValue().floatValue() * scale,
                     height + this.borderSize.getValue().floatValue() * scale,
                     this.borderColor.getValue().getRGB(),
                     0.0F,
                     this.borderSize.getValue().floatValue() * scale
                  );
               }
            }
         }
      );
   }
}
