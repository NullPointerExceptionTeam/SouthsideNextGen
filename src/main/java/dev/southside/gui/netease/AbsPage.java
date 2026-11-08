package dev.southside.gui.netease;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.util.ArrayList;

public abstract class AbsPage {
   public static final float WIDTH = 1056.0F;
   public static final float HEIGHT = 728.0F;
   protected final String title;
   protected Animation scrollAnimation;
   private final ColorAnimation colorAnimation = new ColorAnimation(new ColorPalette(Colors.TRANSPARENT, Colors.GRAY_400_60, Colors.GRAY_400_60), 200);
   protected float scrollTarget;
   private long scrollTime;
   private boolean mouseWasDown;
   private boolean dragging;
   private float yStart;
   protected float scroll;
   public final ArrayList<AbsPage> parents = new ArrayList<>();
   private String previousSearch = "";

   public AbsPage(String title) {
      this.title = title;
   }

   public abstract void draw(long var1, int var3, int var4, InputHandler var5);

   public int drawStatic(long vg, int x, int y, InputHandler inputHandler) {
      return 0;
   }

   public void finishUpAndClose() {
      this.scroll = 0.0F;
      this.scrollTarget = 0.0F;
      this.scrollTime = 0L;
      this.scrollAnimation = null;
   }

   public void scrollWithDraw(long vg, int x, int y, InputHandler inputHandler) {
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      int maxScroll = this.getMaxScrollHeight();
      int scissorOffset = this.drawStatic(vg, x, y, inputHandler);
      this.scroll = this.scrollAnimation == null ? this.scrollTarget : this.scrollAnimation.get();
      float scrollBarLength = 728.0F / maxScroll * 728.0F;
      Scissor scissor = scissorHelper.scissor(vg, x, y + scissorOffset, 1056.0F, 728 - scissorOffset);
      Scissor inputScissor = inputHandler.blockInputArea(x, y, 1056.0F, scissorOffset);
      if (maxScroll <= 728) {
         this.draw(vg, x, y, inputHandler);
         scissorHelper.resetScissor(vg, scissor);
         inputHandler.stopBlock(inputScissor);
      } else {
         this.draw(vg, x, (int)(y + this.scroll), inputHandler);
         if (this.dragging && inputHandler.isClicked(true)) {
            this.dragging = false;
         }

         scissorHelper.resetScissor(vg, scissor);
         inputHandler.stopBlock(inputScissor);
         float dWheel = (float)inputHandler.getDWheel();
         if (dWheel != 0.0F) {
            this.scrollTarget += dWheel;
            if (this.scrollTarget > 0.0F) {
               this.scrollTarget = 0.0F;
            } else if (this.scrollTarget < -maxScroll + 728) {
               this.scrollTarget = -maxScroll + 728;
            }

            this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
            this.scrollTime = System.currentTimeMillis();
         } else if (this.scrollAnimation != null && this.scrollAnimation.isFinished()) {
            this.scrollAnimation = null;
         }

         if (!(scrollBarLength > 727.0F)) {
            float scrollBarY = this.scroll / maxScroll * 720.0F;
            boolean isMouseDown = Platform.getMousePlatform().isButtonDown(0);
            boolean scrollHover = inputHandler.isAreaHovered(x + 1042, (int)(y - scrollBarY), 12.0F, (int)scrollBarLength);
            boolean scrollTimePeriod = System.currentTimeMillis() - this.scrollTime < 1000L;
            if (scrollHover && isMouseDown && !this.mouseWasDown) {
               this.yStart = inputHandler.mouseY();
               this.dragging = true;
            }

            this.mouseWasDown = isMouseDown;
            if (this.dragging) {
               this.scrollTarget = -(inputHandler.mouseY() - this.yStart) * maxScroll / 728.0F;
               if (this.scrollTarget > 0.0F) {
                  this.scrollTarget = 0.0F;
               } else if (this.scrollTarget < -maxScroll + 728) {
                  this.scrollTarget = -maxScroll + 728;
               }

               this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
            }

            NanoVGHelper.INSTANCE
               .drawRoundedRect(
                  vg, x + 1048, y - scrollBarY, 4.0F, scrollBarLength, this.colorAnimation.getColor(scrollHover || scrollTimePeriod, this.dragging), 4.0F
               );
         }
      }
   }

   public String getTitle() {
      return this.title;
   }

   public void init() {
   }

   public void keyTyped(int keyCode, int scanCode, int modifiers) {
   }

   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
   }

   public void mouseReleased(double mouseX, double mouseY, int state) {
   }

   public boolean isBase() {
      return false;
   }

   public int getMaxScrollHeight() {
      return 728;
   }

   public void charTyped(char c, int modifiers) {
   }

   public boolean needServiceAvailable() {
      return false;
   }
}
