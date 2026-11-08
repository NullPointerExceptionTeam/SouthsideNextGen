package cc.polyfrost.oneconfig.internal.utils;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionRenderer {
   private static final NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

   public static int drawDescription(
      long vg,
      int x,
      int y,
      @NotNull String description,
      @Nullable String warningDescription,
      @NotNull Supplier<Animation> animationSupplier,
      @NotNull Consumer<Animation> animationSetter,
      @Nullable SVG icon,
      boolean shouldDrawDescription,
      @NotNull DescriptionRenderer.DescriptionPosition position,
      @NotNull InputHandler inputHandler
   ) {
      Animation animation = animationSupplier.get();
      Animation targetAnim = null;
      if (animation.getEnd() != 1.0F && shouldDrawDescription) {
         targetAnim = new EaseOutQuad(150, animation.get(0.0F), 1.0F, false);
      } else if (animation.getEnd() != 0.0F && !shouldDrawDescription) {
         targetAnim = new EaseOutQuad(150, animation.get(0.0F), 0.0F, false);
      }

      if (targetAnim != null) {
         animation = targetAnim;
         animationSetter.accept(targetAnim);
      }

      if (!shouldDrawDescription && animation.isFinished()) {
         return -1;
      } else {
         float[] descriptionBounds = nanoVGHelper.getWrappedStringBounds(vg, description, 400.0F, 16.0F, 1.0F, Fonts.MEDIUM);
         float[] warningBounds = new float[0];
         float textHeight;
         float textWidth;
         if (warningDescription == null) {
            textHeight = descriptionBounds[3] - descriptionBounds[1];
            textWidth = descriptionBounds[2] - descriptionBounds[0];
         } else {
            warningBounds = nanoVGHelper.getWrappedStringBounds(vg, warningDescription, 400.0F, 16.0F, 1.0F, Fonts.MEDIUM);
            textHeight = descriptionBounds[3] - descriptionBounds[1] + (warningBounds[3] - warningBounds[1]);
            textWidth = Math.max(descriptionBounds[2] - descriptionBounds[0], warningBounds[2] - warningBounds[0]);
         }

         nanoVGHelper.setAlpha(vg, animation.get());
         switch (position) {
            case MIDDLE:
               nanoVGHelper.translate(vg, -(textWidth / 2.0F + 34.0F), 0.0F);
               break;
            case RIGHT:
               nanoVGHelper.translate(vg, -(textWidth + 68.0F), 0.0F);
         }

         int returnValue;
         if (warningDescription != null) {
            returnValue = (int)(91.0F + (textHeight - 32.0F));
            nanoVGHelper.drawRoundedRect(vg, x - 1.0F, y - 42.0F - 47.0F - (textHeight - 32.0F), textWidth + 70.0F, returnValue, Colors.GRAY_700, 8.0F);
            nanoVGHelper.drawRoundedRect(
               vg, x - 1.0F, y - 42.0F - 47.0F - (textHeight - 32.0F), textWidth + 70.0F, returnValue, ColorUtils.getColor(204, 204, 204, 25), 8.0F
            );
            nanoVGHelper.drawRoundedRect(vg, x, y - 42.0F - 46.0F - (textHeight - 32.0F), textWidth + 68.0F, returnValue - 2.0F, Colors.GRAY_700, 8.0F);
            nanoVGHelper.drawSvg(
               vg, SVGs.WARNING, (float)(x + 16), y - 30.0F - (warningBounds[3] - warningBounds[1] - 16.0F), 20.0F, 20.0F, ColorUtils.getColor(223, 51, 39)
            );
            nanoVGHelper.drawWrappedString(
               vg,
               warningDescription,
               x + 52,
               y - 19.0F - (warningBounds[3] - warningBounds[1] - 16.0F),
               400.0F,
               ColorUtils.getColor(223, 51, 39),
               16.0F,
               1.0F,
               Fonts.MEDIUM
            );
            nanoVGHelper.drawLine(
               vg,
               x + 16.0F,
               y - 42.0F - 47.0F - (textHeight - 32.0F) + (91.0F + (textHeight - 32.0F)) / 2.0F,
               x + textWidth + 68.0F - 16.0F,
               y - 42.0F - 47.0F - (textHeight - 32.0F) + (91.0F + (textHeight - 32.0F)) / 2.0F,
               1.0F,
               ColorUtils.getColor(204, 204, 204, 25)
            );
            nanoVGHelper.drawSvg(vg, SVGs.INFO_ARROW, (float)(x + 16), y - 14.0F - 45.0F - (textHeight - 16.0F), 20.0F, 20.0F, Colors.WHITE_80);
            nanoVGHelper.drawWrappedString(vg, description, x + 52, y - 3.0F - 45.0F - (textHeight - 16.0F), 400.0F, Colors.WHITE_80, 16.0F, 1.0F, Fonts.MEDIUM);
         } else {
            returnValue = (int)(46.0F + (textHeight - 16.0F));
            nanoVGHelper.drawRoundedRect(vg, x - 1.0F, y - 42.0F - 1.0F - (textHeight - 16.0F), textWidth + 70.0F, returnValue, Colors.GRAY_700, 8.0F);
            nanoVGHelper.drawRoundedRect(
               vg, x - 1.0F, y - 42.0F - 1.0F - (textHeight - 16.0F), textWidth + 70.0F, returnValue, ColorUtils.getColor(204, 204, 204, 25), 8.0F
            );
            nanoVGHelper.drawRoundedRect(vg, x, y - 42.0F - (textHeight - 16.0F), textWidth + 68.0F, returnValue - 2.0F, Colors.GRAY_700, 8.0F);
            nanoVGHelper.drawSvg(vg, icon == null ? SVGs.INFO_ARROW : icon, (float)(x + 16), y - 30.0F - (textHeight - 16.0F), 20.0F, 20.0F, Colors.WHITE_80);
            nanoVGHelper.drawWrappedString(vg, description, x + 52, y - 19 - (textHeight - 16.0F), 400.0F, Colors.WHITE_80, 16.0F, 1.0F, Fonts.MEDIUM);
         }

         switch (position) {
            case MIDDLE:
               nanoVGHelper.translate(vg, textWidth / 2.0F + 34.0F, 0.0F);
               break;
            case RIGHT:
               nanoVGHelper.translate(vg, textWidth + 68.0F, 0.0F);
         }

         nanoVGHelper.setAlpha(vg, 1.0F);
         return returnValue;
      }
   }

   public static enum DescriptionPosition {
      LEFT,
      MIDDLE,
      RIGHT;
   }
}
