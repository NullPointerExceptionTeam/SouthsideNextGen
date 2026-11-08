package cc.polyfrost.oneconfig.gui;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.gui.elements.BasicButton;
import cc.polyfrost.oneconfig.gui.pages.CreditsPage;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_437;

public class SideBar {
   private final List<BasicButton> buttons = new ArrayList<BasicButton>() {
      {
         int width = 192;
         this.add(new BasicButton(width, 36, "Credits", SVGs.COPYRIGHT_FILL, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Client", SVGs.CLIENT, null, 0, ColorPalette.PRIMARY));
         this.add(new BasicButton(width, 36, "Combat", SVGs.COMBAT, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Misc", SVGs.MISC, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Movement", SVGs.MOVEMENT, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Player", SVGs.PLAYER, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Render", SVGs.RENDER, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "World", SVGs.WORLD, null, 0, ColorPalette.TERTIARY));
         this.add(new BasicButton(width, 36, "Preferences", SVGs.SETTINGS_02, null, 0, ColorPalette.TERTIARY));
      }
   };
   private final BasicButton hudButton = new BasicButton(192, 36, "Edit HUD", SVGs.LAYOUT_ALT, null, 0, ColorPalette.TERTIARY);
   private final BasicButton closeButton = new BasicButton(192, 36, "Close", SVGs.X_CLOSE, null, 0, ColorPalette.TERTIARY_DESTRUCTIVE);
   private int selected = 1;
   private Animation moveAnimation = null;
   private Animation sizeAnimation = null;
   private int y;
   private int sidebarY;

   public SideBar() {
      this.buttons.get(0).setClickAction(new CreditsPage());

      for (int i = 1; i <= 7; i++) {
         int finalI = i - 1;
         this.buttons.get(i).setClickAction(new ModsPage(finalI));
      }

      this.buttons.get(8).setClickAction(new ModConfigPage(Preferences.getInstance().mod.defaultPage, true));
      this.hudButton.setClickAction(() -> GuiUtils.displayScreen((class_437)(new HudGui())));
      this.closeButton.setClickAction(GuiUtils::closeScreen);

      for (BasicButton button : this.buttons) {
         if (!button.hasClickAction()) {
            button.disable(true);
         }
      }
   }

   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.y = y;

      for (BasicButton button : this.buttons) {
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
      this.buttons.get(0).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      nanoVGHelper.drawText(vg, "MOD CONFIG", x + 16, this.calcAndIncrementLn(this.sidebarY + 26), Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(1).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(2).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(3).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(4).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(5).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(6).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.buttons.get(7).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      nanoVGHelper.drawText(vg, "PERSONALIZATION", x + 16, this.calcAndIncrementLn(this.sidebarY + 26), Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
      this.sidebarY -= 26;
      this.buttons.get(8).draw(vg, x + 16, this.calcAndIncrementLn(this.sidebarY), inputHandler);
      this.sidebarY = 0;
      this.hudButton.draw(vg, x + 16, y + 704, inputHandler);
      this.closeButton.draw(vg, x + 16, y + 748, inputHandler);
   }

   public void pageOpened(String page) {
      this.pageOpened(page, false);
   }

   public void pageOpened(String page, boolean instant) {
      for (BasicButton button : this.buttons) {
         if (button.getText().equalsIgnoreCase(page)) {
            this.moveSideBar(button, instant);
            return;
         }
      }
   }

   private void moveSideBar(BasicButton button) {
      this.moveSideBar(button, false);
   }

   private void moveSideBar(BasicButton button, boolean instant) {
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

   private int calcAndIncrementLn(int n) {
      this.sidebarY = n + 36;
      return this.sidebarY;
   }
}
