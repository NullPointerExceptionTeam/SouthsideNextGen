package cc.polyfrost.oneconfig.internal.utils;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ChainedAnimation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInOutQuad;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.Icon;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.MathUtils;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.MinecraftAccessor;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.Nullable;

public final class Notification {
   private final String title;
   private final String message;
   private final Icon icon;
   private final Animation animation;
   private final Callable<Float> progressBar;
   private final Runnable action;
   private final InputHandler inputHandler = new InputHandler();
   private final ColorAnimation bgColor = new ColorAnimation(new ColorPalette(Colors.GRAY_800, Colors.GRAY_700, Colors.GRAY_900));
   private final ColorAnimation titleColor = new ColorAnimation(new ColorPalette(Colors.WHITE_80, Colors.WHITE, Colors.WHITE));
   private final ColorAnimation messageColor = new ColorAnimation(new ColorPalette(Colors.WHITE_60, Colors.WHITE_90, Colors.WHITE_90));
   private boolean hovered = false;

   public Notification(String title, String message, @Nullable Icon icon, float duration, @Nullable Callable<Float> progressBar, @Nullable Runnable action) {
      this.title = title;
      this.message = message;
      this.icon = icon;
      this.animation = new ChainedAnimation(
         new EaseInOutQuad(250, 0.0F, 330.0F, false),
         progressBar == null ? new DummyAnimation(330.0F, duration) : new DummyAnimation(330.0F, () -> progressBar.call() >= 1.0F),
         new EaseInOutQuad(250, 330.0F, 0.0F, false)
      );
      this.progressBar = progressBar;
      this.action = action;
   }

   public float draw(long vg, float y, float scale, float deltaTime) {
      NanoVGHelper renderer = NanoVGHelper.INSTANCE;
      renderer.scale(vg, scale, scale);
      this.inputHandler.scale(scale, scale);
      float x = UResolution.getWindowWidth() / scale - this.animation.get(deltaTime);
      float textX = this.icon == null ? x + 16.0F : x + 64.0F;
      float textMaxLength = this.icon == null ? 268.0F : 220.0F;
      float messageHeight = renderer.getWrappedStringHeight(vg, this.message, textMaxLength, 12.0F, 1.0F, Fonts.REGULAR);
      float height = this.getHeight(messageHeight);
      y -= height;
      this.hovered = this.inputHandler.isAreaHovered(x, y, 314.0F, height);
      if (this.hovered && this.inputHandler.isClicked() && this.action != null) {
         this.action.run();
      }

      int bgColor = this.bgColor.getColor(this.hovered, this.hovered && this.inputHandler.isMouseDown());
      int titleColor = this.titleColor.getColor(this.hovered, this.hovered && this.inputHandler.isMouseDown());
      int messageColor = this.messageColor.getColor(this.hovered, this.hovered && this.inputHandler.isMouseDown());
      renderer.drawRoundedRect(vg, x, y, 314.0F, height, bgColor, 8.0F);
      if (this.icon != null) {
         this.icon.draw(vg, x + 16.0F, y + (height - (this.progressBar == null ? 0.0F : 5.0F)) / 2.0F - 16.0F, 32.0F, 32.0F, titleColor, scale);
      }

      renderer.drawText(vg, this.title, textX, y + 30.0F, titleColor, 16.0F, Fonts.SEMIBOLD);
      renderer.drawWrappedString(vg, this.message, textX, y + 46.0F, textMaxLength, messageColor, 12.0F, 1.0F, Fonts.REGULAR);
      if (this.progressBar != null) {
         try {
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            float progress = MathUtils.clamp(this.progressBar.call());
            Scissor scissor1 = scissorHelper.scissor(vg, x + 314.0F * progress, y + height - 5.0F, 314.0F * (1.0F - progress), 5.0F);
            renderer.drawRoundedRect(vg, x, y, 314.0F, height, MinecraftAccessor.mc.world == null ? Colors.PRIMARY_800 : Colors.GRAY_400, 8.0F);
            scissorHelper.resetScissor(vg, scissor1);
            Scissor scissor2 = scissorHelper.scissor(
               vg, x, y + height - 5.0F, 314.0F * progress - (!(314.0F * progress < 2.5F) && !(311.5F * progress > 2.5F) ? 2.5F : 0.0F), 5.0F
            );
            renderer.drawRoundedRect(vg, x, y, 314.0F, height, MinecraftAccessor.mc.world == null ? Colors.PRIMARY_500 : Colors.WHITE, 8.0F);
            scissorHelper.resetScissor(vg, scissor2);
            if (314.0F * progress >= 2.5F && 311.5F * progress <= 2.5F) {
               renderer.drawRoundedRect(vg, x + 2.5F, y + height - 5.0F, Math.max(0.0F, 314.0F * progress - 5.0F), 5.0F, Colors.PRIMARY_500, 2.5F);
            }
         } catch (Exception var19) {
         }
      }

      renderer.resetTransform(vg);
      return height;
   }

   private float getHeight(float messageHeight) {
      float height = 68.0F + messageHeight;
      if (this.progressBar != null) {
         height += 5.0F;
      }

      return height;
   }

   public boolean isFinished() {
      return this.animation.isFinished();
   }
}
