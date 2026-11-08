package cc.polyfrost.oneconfig.gui;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionCategory;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInBack;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.gui.elements.ColorSelector;
import cc.polyfrost.oneconfig.gui.elements.IFocusable;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import cc.polyfrost.oneconfig.gui.pages.Page;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.MathUtils;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen;
import dev.southside.event.Stage;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class OneConfigGui extends OneUIScreen {
   private static final InputHandler DUMMY_HANDLER = new InputHandler();
   public static OneConfigGui INSTANCE;
   private final SideBar sideBar = new SideBar();
   private final TextInputField textInputField = new TextInputField(248, 40, "Search...", false, false, SVGs.SEARCH_SM, 12.0F);
   private final ArrayList<Page> previousPages = new ArrayList<>();
   private final ArrayList<Page> nextPages = new ArrayList<>();
   private final BasicElement backArrow = new BasicElement(40, 40, ColorPalette.TERTIARY, true);
   private final BasicElement forwardArrow = new BasicElement(40, 40, ColorPalette.TERTIARY, true);
   public ColorSelector currentColorSelector;
   public boolean allowClose = true;
   protected Page currentPage;
   protected Page prevPage;
   private Animation pageAnimation;
   private long lastClosedTime = -1L;
   private Animation containerAnimation = new DummyAnimation(0.0F);
   public boolean isClosed = true;
   private boolean shouldDisplayHud = false;
   public float transparencyFactor = 0.0F;
   public float animationScaleFactor = 0.0F;
   private boolean isDrawing;

   public OneConfigGui() {
      if (INSTANCE != null) {
         EventManager.INSTANCE.unregister(INSTANCE);
      }

      INSTANCE = this;
      EventManager.INSTANCE.register(INSTANCE);
   }

   public OneConfigGui(Page page) {
      this();
      this.currentPage = page;
   }

   @Override
   public void initScreen(int width, int height) {
      super.initScreen(width, height);
      if (this.currentPage == null) {
         this.currentPage = new ModsPage(0);
         this.currentPage.parents.add(this.currentPage);
      }

      this.handleOpeningPage();
      if (Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         this.shouldDisplayHud = false;
      }
   }

   private void handleOpeningPage() {
      boolean instant = !Preferences.INSTANCE.showPageAnimationOnOpen.getValue();
      switch (Preferences.INSTANCE.openingBehavior.getValue()) {
         case 0:
            if (!(this.currentPage instanceof ModsPage)) {
               this.previousPages.clear();
               this.openPage(new ModsPage(0), false, instant);
            }
            break;
         case 1:
            OptionPage preferencesPage = Preferences.getInstance().mod.defaultPage;
            if (!(this.currentPage instanceof ModConfigPage modConfigPage && modConfigPage.getPage() == preferencesPage)) {
               this.previousPages.clear();
               this.openPage(new ModConfigPage(Preferences.getInstance().mod.defaultPage, true), false, instant);
            }
         case 2:
         default:
            break;
         case 3:
            if (!(this.currentPage instanceof ModsPage)) {
               long current = System.currentTimeMillis();
               long diff = current - this.lastClosedTime;
               if (this.lastClosedTime != -1L && diff > Preferences.INSTANCE.timeUntilReset.getValue().longValue() * 1000L) {
                  this.previousPages.clear();
                  this.openPage(new ModsPage(0), false, instant);
               }
            }
      }
   }

   @Override
   public void draw(long vg, float partialTicks, InputHandler inputHandler) {
      this.isDrawing = true;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      boolean renderedInHud = inputHandler == null;
      if (Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         int animationTime = (int)(Preferences.INSTANCE.animationTime.getValue().floatValue() * 1000.0F);
         if (renderedInHud && Preferences.INSTANCE.guiClosingAnimation.getValue() && this.shouldDisplayHud) {
            if (this.containerAnimation.getEnd() != 0.0F) {
               switch (Preferences.INSTANCE.animationType.getValue()) {
                  case 0:
                     this.containerAnimation = new EaseOutExpo(animationTime, MathUtils.clamp(this.animationScaleFactor - 0.9F, 0.0F, 0.1F), 0.0F, false);
                     break;
                  case 1:
                     this.containerAnimation = new EaseInBack(animationTime, MathUtils.clamp(this.animationScaleFactor, 0.0F, 1.0F), 0.0F, false);
               }
            }
         } else if (!renderedInHud && this.isClosed) {
            boolean forceFinished = this.containerAnimation.isFinished() && this.containerAnimation.getEnd() != 0.0F;
            switch (Preferences.INSTANCE.animationType.getValue()) {
               case 0:
                  this.containerAnimation = new EaseOutExpo(
                     animationTime, MathUtils.clamp(forceFinished ? 0.1F : this.animationScaleFactor - 0.9F, 0.0F, 0.1F), 0.1F, false
                  );
                  break;
               case 1:
                  this.containerAnimation = new EaseOutExpo(
                     animationTime, MathUtils.clamp(forceFinished ? 1.0F : this.animationScaleFactor, 0.0F, 1.0F), 1.0F, false
                  );
            }

            this.isClosed = false;
         }
      }

      float animationValue = Math.max(0.0F, Preferences.INSTANCE.guiOpenAnimation.getValue() ? this.containerAnimation.get() : 1.0F);
      switch (Preferences.INSTANCE.animationType.getValue()) {
         case 0:
            this.animationScaleFactor = MathUtils.clamp(0.9F + animationValue, 0.9F, 1.0F);
            this.transparencyFactor = MathUtils.clamp(animationValue * 10.0F, 0.0F, 1.0F);
            break;
         case 1:
            this.animationScaleFactor = this.transparencyFactor = animationValue;
      }

      nanoVGHelper.setAlpha(vg, this.transparencyFactor);
      if (OneConfigConfig.australia) {
         nanoVGHelper.translate(vg, UResolution.getWindowWidth(), UResolution.getWindowHeight());
         nanoVGHelper.rotate(vg, 180.0);
      }

      if (inputHandler == null) {
         inputHandler = DUMMY_HANDLER;
      }

      float scale = getScaleFactor() * this.animationScaleFactor;
      int x = (int)((UResolution.getWindowWidth() - 1280.0F * scale) / 2.0F / scale);
      int y = (int)((UResolution.getWindowHeight() - 800.0F * scale) / 2.0F / scale);
      nanoVGHelper.scale(vg, scale, scale);
      inputHandler.scale(scale, scale);
      nanoVGHelper.drawDropShadow(vg, x, y, 1280.0F, 800.0F, 64.0F, 0.0F, 20.0F);
      Scissor mainPanel = scissorHelper.scissor(vg, x, y, 224.0F, 800.0F);
      nanoVGHelper.drawRoundedRect(vg, x, y, 244.0F, 800.0F, Colors.GRAY_800_95, 20.0F);
      scissorHelper.resetScissor(vg, mainPanel);
      Scissor contentPanel = scissorHelper.scissor(vg, x + 224, y, 1056.0F, 800.0F);
      nanoVGHelper.drawRoundedRect(vg, x + 224 - 20, y, 1076.0F, 800.0F, Colors.GRAY_800, 20.0F);
      scissorHelper.resetScissor(vg, contentPanel);
      nanoVGHelper.drawLine(vg, x + 224, y + 72, x + 1280, y + 72, 1.0F, Colors.GRAY_700);
      nanoVGHelper.drawLine(vg, x + 224, y, x + 222, y + 800, 1.0F, Colors.GRAY_700);
      nanoVGHelper.drawSvg(vg, SVGs.SOUTHSIDE, x + 27.0F, y + 18.0F, 40.0F, 40.0F);
      nanoVGHelper.drawText(vg, "SOUTHSIDE", x + 67.0F, y + 40.0F, Colors.WHITE, 15.0F, Fonts.BOLD);
      this.textInputField.draw(vg, x + 1020, y + 16, inputHandler);
      this.sideBar.draw(vg, x, y, inputHandler);
      this.backArrow.update(x + 240, y + 16, inputHandler);
      this.forwardArrow.update(x + 280, y + 16, inputHandler);
      if (this.previousPages.isEmpty()) {
         this.backArrow.disable(true);
         nanoVGHelper.setAlpha(vg, 0.5F);
      } else {
         this.backArrow.disable(false);
         if (!this.backArrow.isHovered() || Platform.getMousePlatform().isButtonDown(0)) {
            nanoVGHelper.setAlpha(vg, 0.8F);
         }
      }

      nanoVGHelper.drawSvg(vg, SVGs.ARROW_LEFT, (float)(x + 250), (float)(y + 26), 20.0F, 20.0F, this.backArrow.currentColor);
      nanoVGHelper.setAlpha(vg, 1.0F);
      if (this.nextPages.isEmpty()) {
         this.forwardArrow.disable(true);
         nanoVGHelper.setAlpha(vg, 0.5F);
      } else {
         this.forwardArrow.disable(false);
         if (!this.forwardArrow.isHovered() || Platform.getMousePlatform().isButtonDown(0)) {
            nanoVGHelper.setAlpha(vg, 0.8F);
         }
      }

      nanoVGHelper.drawSvg(vg, SVGs.ARROW_RIGHT, (float)(x + 290), (float)(y + 26), 20.0F, 20.0F, this.forwardArrow.currentColor);
      nanoVGHelper.setAlpha(vg, 1.0F);
      this.handleHistoryMovement(this.backArrow.isClicked(), this.forwardArrow.isClicked());
      scissorHelper.scissor(vg, x + 224, y + 72, 1056.0F, 728.0F);
      Scissor blockedClicks = inputHandler.blockInputArea(x, y, 224.0F, 728.0F);
      if (this.prevPage != null && this.pageAnimation != null) {
         float pageProgress = this.pageAnimation.get(GuiUtils.getDeltaTime());
         if (!this.pageAnimation.isReversed()) {
            this.prevPage.scrollWithDraw(vg, (int)(x + pageProgress), y + 72, inputHandler);
            this.currentPage.scrollWithDraw(vg, (int)(x - 1904 + pageProgress), y + 72, inputHandler);
         } else {
            this.prevPage.scrollWithDraw(vg, (int)(x - 1904 + pageProgress), y + 72, inputHandler);
            this.currentPage.scrollWithDraw(vg, (int)(x + pageProgress), y + 72, inputHandler);
         }

         if (this.pageAnimation.isFinished()) {
            this.prevPage = null;
         }
      } else {
         this.currentPage.scrollWithDraw(vg, x + 224, y + 72, inputHandler);
      }

      scissorHelper.clearScissors(vg);
      inputHandler.stopBlock(blockedClicks);
      float breadcrumbX = x + 336;

      for (int i = 0; i < this.currentPage.parents.size(); i++) {
         String title = this.currentPage.parents.get(i).getTitle();
         float width = nanoVGHelper.getTextWidth(vg, title, 24.0F, Fonts.SEMIBOLD);
         boolean hovered = inputHandler.isAreaHovered((int)breadcrumbX, y + 24, (int)width, 36.0F);
         int color = Colors.WHITE_60;
         if (i == this.currentPage.parents.size() - 1) {
            color = Colors.WHITE;
         } else if (hovered && !Platform.getMousePlatform().isButtonDown(0)) {
            color = Colors.WHITE_80;
         }

         nanoVGHelper.drawText(vg, title, breadcrumbX, y + 38, color, 24.0F, Fonts.SEMIBOLD);
         if (i != 0) {
            nanoVGHelper.drawSvg(vg, SVGs.CARET_RIGHT, breadcrumbX - 28.0F, (float)(y + 25), 24.0F, 24.0F, color);
         }

         if (hovered && inputHandler.isClicked()) {
            this.openPage(this.currentPage.parents.get(i));
         }

         breadcrumbX += width + 32.0F;
      }

      if (this.currentColorSelector != null) {
         this.currentColorSelector.draw(vg);
      }

      GuiNotifications.INSTANCE.draw(vg, x + 224 + 528, y + 720 + 72, inputHandler);
      nanoVGHelper.resetTransform(vg);
      this.isDrawing = false;
   }

   @Override
   public void onCharTyped(char c, int modifiers) {
      try {
         if (this.allowClose) {
            super.onCharTyped(c, modifiers);
         }

         this.textInputField.charTyped(c, modifiers);
         if (this.currentColorSelector != null) {
            this.currentColorSelector.charTyped(c, modifiers);
         }

         this.currentPage.charTyped(c, modifiers);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      try {
         if (this.allowClose) {
            super.onKeyPressed(keyCode, scanCode, modifiers);
         }

         this.textInputField.keyTyped(keyCode, scanCode, modifiers);
         if (this.currentColorSelector != null) {
            this.currentColorSelector.keyTyped(keyCode, scanCode, modifiers);
         }

         this.currentPage.keyTyped(keyCode, scanCode, modifiers);
         if (this.textInputField.isToggled()) {
            return;
         }

         if (this.currentPage instanceof ModConfigPage modConfigPage) {
            OptionCategory category = modConfigPage.getPage().categories.get(modConfigPage.getSelectedCategory());
            if (category != null) {
               for (OptionSubcategory subCategory : category.subcategories) {
                  for (BasicOption option : subCategory.options) {
                     if (option.isEnabled() && option instanceof IFocusable && ((IFocusable)option).hasFocus()) {
                        return;
                     }
                  }
               }
            }
         }

         if (!TextInputField.isAnySelected()) {
            this.handleHistoryMovement(keyCode == 263, keyCode == 262);
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, int mouseButton) {
      try {
         super.onMouseClicked(mouseX, mouseY, mouseButton);
         this.currentPage.mouseClicked(mouseX, mouseY, mouseButton);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, int state) {
      try {
         super.onMouseReleased(mouseX, mouseY, state);
         this.currentPage.mouseReleased(mouseX, mouseY, state);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   private void handleHistoryMovement(boolean back, boolean forward) {
      if (!back || !forward) {
         try {
            if (back && !this.previousPages.isEmpty()) {
               this.nextPages.add(0, this.currentPage);
               this.openPage(this.previousPages.get(0), false);
               this.previousPages.remove(0);
            }

            if (forward && !this.nextPages.isEmpty()) {
               this.previousPages.add(0, this.currentPage);
               this.openPage(
                  this.nextPages.get(0),
                  new EaseOutExpo((int)(Preferences.INSTANCE.pageAnimationDuration.getValue().floatValue() * 1000.0F), 224.0F, 2128.0F, true),
                  false
               );
               this.nextPages.remove(0);
            }
         } catch (Throwable var4) {
         }
      }
   }

   public void openPage(@NotNull Page page) {
      this.openPage(page, true);
   }

   public void openPage(@NotNull Page page, boolean addToPrevious) {
      this.openPage(
         page, new EaseOutExpo((int)(Preferences.INSTANCE.pageAnimationDuration.getValue().floatValue() * 1000.0F), 224.0F, 2128.0F, false), addToPrevious
      );
   }

   private void openPage(@NotNull Page page, boolean addToPrevious, boolean instant) {
      if (instant) {
         this.openPageInstant(page, addToPrevious);
      } else {
         this.openPage(page, addToPrevious);
      }
   }

   private void openPageInstant(@NotNull Page page, boolean addToPrevious) {
      this.openPage(page, new DummyAnimation(2128.0F), addToPrevious);
   }

   public void openPage(@NotNull Page page, Animation animation, boolean addToPrevious) {
      if (!Preferences.INSTANCE.showPageAnimations.getValue()) {
         animation = new DummyAnimation(animation.getEnd());
      }

      if (page != this.currentPage) {
         this.currentPage.finishUpAndClose();
         this.textInputField.setInput("");
         if (page.parents.isEmpty()) {
            page.parents.addAll(this.currentPage.parents);
            if (!page.isBase()) {
               boolean alreadyInParents = false;

               for (int i = 0; i < page.parents.size(); i++) {
                  Page parent = page.parents.get(i);
                  if (parent == page) {
                     alreadyInParents = true;
                     page.parents.subList(i + 1, page.parents.size()).clear();
                     break;
                  }
               }

               if (!alreadyInParents) {
                  page.parents.add(page);
               }
            } else {
               page.parents.clear();
               page.parents.add(page);
            }
         }

         this.sideBar.pageOpened(page.parents.get(0).getTitle(), animation instanceof DummyAnimation);
         if (addToPrevious) {
            this.previousPages.add(0, this.currentPage);
            this.nextPages.clear();
         }

         if (this.prevPage == null) {
            this.prevPage = this.currentPage;
         }

         this.currentPage = page;
         this.pageAnimation = animation;
      }
   }

   public void initColorSelector(ColorSelector colorSelector) {
      if (this.currentColorSelector != null) {
         this.closeColorSelector();
      }

      this.currentColorSelector = colorSelector;
   }

   public OneColor closeColorSelector() {
      if (this.currentColorSelector == null) {
         return null;
      } else {
         this.currentColorSelector.onClose();
         OneColor color = this.currentColorSelector.getColor();
         this.currentColorSelector = null;
         return color;
      }
   }

   public OneColor getColor() {
      return this.currentColorSelector == null ? null : this.currentColorSelector.getColor();
   }

   public static float getScaleFactor() {
      float scale = Preferences.INSTANCE.enableCustomScale.getValue()
         ? Preferences.INSTANCE.customScale.getValue().floatValue()
         : Math.min(UResolution.getWindowWidth() / 1920.0F, UResolution.getWindowHeight() / 1080.0F);
      if (scale < 1.0F && !Preferences.INSTANCE.enableCustomScale.getValue()) {
         scale = Math.min(Math.min(1.0F, UResolution.getWindowWidth() / 1280.0F), Math.min(1.0F, UResolution.getWindowHeight() / 800.0F));
      }

      return (float)(Math.floor(scale / 0.05F) * 0.05F);
   }

   public static float getNotificationScaleFactor() {
      float scale = Preferences.INSTANCE.enableCustomNotificationScale.getValue()
         ? Preferences.INSTANCE.customNotificationScale.getValue().floatValue()
         : Math.min(UResolution.getWindowWidth() / 1920.0F, UResolution.getWindowHeight() / 1080.0F);
      if (scale < 1.0F && !Preferences.INSTANCE.enableCustomNotificationScale.getValue()) {
         scale = Math.min(Math.min(1.0F, UResolution.getWindowWidth() / 1280.0F), Math.min(1.0F, UResolution.getWindowHeight() / 800.0F));
      }

      return (float)(Math.floor(scale / 0.05F) * 0.05F);
   }

   public String getSearchValue() {
      return this.textInputField.getInput();
   }

   @Override
   public void onScreenClose() {
      this.currentPage.finishUpAndClose();
      this.lastClosedTime = System.currentTimeMillis();
      this.isClosed = true;
      if (Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         if (Preferences.INSTANCE.guiClosingAnimation.getValue()) {
            this.shouldDisplayHud = true;
         } else {
            this.animationScaleFactor = this.transparencyFactor = 0.0F;
         }
      }

      super.onScreenClose();
   }

   @EventHandler
   private void onRenderHUD(GameRendererDrawEvent event) {
      if (this.shouldDisplayHud && event.stage != Stage.PRE) {
         if (Platform.getGuiPlatform().getCurrentScreen() != this) {
            NanoVGHelper.INSTANCE.setupAndDraw(vg -> this.draw(vg, event.tickDelta, null));
            if (this.transparencyFactor <= 0.01) {
               this.shouldDisplayHud = false;
            }
         }
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public boolean hasBackgroundBlur() {
      return Preferences.INSTANCE.enableBlur.getValue();
   }

   public boolean isDrawing() {
      return this.isDrawing;
   }

   public static OneConfigGui create() {
      return INSTANCE == null ? new OneConfigGui() : INSTANCE;
   }

   public static boolean isOpen() {
      return Platform.getGuiPlatform().getCurrentScreen() instanceof OneConfigGui;
   }

   static {
      DUMMY_HANDLER.blockAllInput();
   }
}
