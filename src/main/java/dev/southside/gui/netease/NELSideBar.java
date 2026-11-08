package dev.southside.gui.netease;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.gui.netease.pages.AccountsPage;
import dev.southside.gui.netease.pages.FreeCookiePage;
import dev.southside.gui.netease.pages.InterceptorsPage;
import java.util.ArrayList;
import java.util.List;

public class NELSideBar {
   private final List<CategoryButton> buttons = new ArrayList<CategoryButton>() {
      {
         int width = 192;
         this.add(new CategoryButton(width, 36, "Accounts", 0, ColorPalette.TERTIARY));
         this.add(new CategoryButton(width, 36, "Interceptors", 0, ColorPalette.TERTIARY));
         this.add(new CategoryButton(width, 36, "FreeCookie", 0, ColorPalette.TERTIARY));
      }
   };
   private int selected = 0;
   private Animation moveAnimation = null;
   private Animation sizeAnimation = null;
   private int y;
   private int sidebarY;

   public NELSideBar() {
      this.buttons.get(0).setClickAction(new AccountsPage());
      this.buttons.get(1).setClickAction(new InterceptorsPage());
      this.buttons.get(2).setClickAction(new FreeCookiePage());
      this.moveSideBar(this.buttons.get(0), true);
   }

   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.y = y;

      for (CategoryButton button : this.buttons) {
         if (button.isClicked()) {
            this.moveSideBar(button, false);
            break;
         }
      }

      if (this.moveAnimation != null) {
         nanoVGHelper.drawRoundedRect(
            vg,
            x + 16,
            y + this.moveAnimation.get() - (this.sizeAnimation.get() - 36.0F) / 2.0F,
            192.0F,
            this.sizeAnimation.get(0.0F),
            Colors.PRIMARY_600,
            12.0F
         );
         if (this.moveAnimation.isFinished() && this.sizeAnimation.isFinished()) {
            this.moveAnimation = null;
            this.sizeAnimation = null;
            this.buttons.get(this.selected).setColorPalette(ColorPalette.PRIMARY);
         }
      }

      this.sidebarY = y + 44;
      nanoVGHelper.drawText(vg, "NETEASE", x + 16, this.calcAndIncrementLn(this.sidebarY + 26), Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(0).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(1).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      nanoVGHelper.drawText(vg, "COOKIES", x + 16, this.calcAndIncrementLn(this.sidebarY + 26), Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(2).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.sidebarY = 0;
   }

   private void moveSideBar(CategoryButton button) {
      this.moveSideBar(button, false);
   }

   private void moveSideBar(CategoryButton button, boolean instant) {
      if (!button.equals(this.buttons.get(this.selected))) {
         this.buttons.get(this.selected).setColorPalette(ColorPalette.TERTIARY);
         if (instant) {
            this.moveAnimation = new DummyAnimation(button.y - this.y);
         } else {
            this.moveAnimation = new EaseOutExpo(300, this.buttons.get(this.selected).y - this.y, button.y - this.y, false);
         }

         this.sizeAnimation = new DummyAnimation(36.0F);
         this.selected = this.buttons.indexOf(button);
      }
   }

   public void pageOpened(String page) {
      this.pageOpened(page, false);
   }

   public void pageOpened(String page, boolean instant) {
      for (CategoryButton button : this.buttons) {
         if (button.getText().equalsIgnoreCase(page)) {
            this.moveSideBar(button, instant);
            return;
         }
      }
   }

   private int calcAndIncrementLn(int n) {
      this.sidebarY = n + 36;
      return this.sidebarY;
   }
}
