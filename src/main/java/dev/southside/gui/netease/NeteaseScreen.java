package dev.southside.gui.netease;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.gui.GuiNotifications;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseInBack;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
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
import dev.southside.gui.netease.pages.AccountsPage;
import dev.southside.gui.netease.pages.UnavailablePage;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.orbit.EventHandler;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class NeteaseScreen extends OneUIScreen {
   private static final InputHandler DUMMY_HANDLER = new InputHandler();
   private static final Logger LOGGER = NeteaseLauncher.LOGGER;
   private static final String BJD_SERVER_ID = "4661334467366178884";
   public static NeteaseScreen INSTANCE;
   private Animation containerAnimation = new DummyAnimation(0.0F);
   public boolean isClosed = true;
   private boolean shouldDisplayHud = false;
   public float transparencyFactor = 0.0F;
   public float animationScaleFactor = 0.0F;
   public ActionButton restartNEL = new ActionButton(100, 35, "Restart NEL", 2, ColorPalette.SECONDARY);
   public final LoadingDialog dialog = new LoadingDialog(300, 50, ColorPalette.PRIMARY);
   private boolean isDrawing;
   protected AbsPage currentPage;
   protected AbsPage prevPage;
   private Animation pageAnimation;
   private final NELSideBar sideBar = new NELSideBar();

   public NeteaseScreen() {
      if (INSTANCE != null) {
         EventManager.INSTANCE.unregister(INSTANCE);
      }

      INSTANCE = this;
      EventManager.INSTANCE.register(INSTANCE);
      this.restartNEL.setClickAction(() -> {
         CompletableFuture<Void> future = NeteaseLauncher.get().restart();
         this.dialog.setTask(future, "Restarting..");
      });
   }

   @Override
   public void initScreen(int width, int height) {
      super.initScreen(width, height);
      if (this.currentPage == null) {
         if (NeteaseLauncher.get().isAvailable()) {
            this.currentPage = new AccountsPage();
         } else {
            this.currentPage = new UnavailablePage(new AccountsPage());
         }

         this.currentPage.parents.add(this.currentPage);
      }

      this.currentPage.init();
      if (Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         this.shouldDisplayHud = false;
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
      nanoVGHelper.drawText(vg, "SOUTHSIDE", x + 67.0F, y + 34.0F, Colors.WHITE, 16.0F, Fonts.BOLD);
      nanoVGHelper.drawText(vg, "Open NEL", x + 67.0F, y + 50.0F, Colors.WHITE_50, 12.0F, Fonts.REGULAR);
      this.restartNEL.draw(vg, x + 1280 - this.restartNEL.getWidth() - 10.0F, y + 20.0F, inputHandler);
      this.sideBar.draw(vg, x, y, inputHandler);
      this.dialog.draw(vg, (UResolution.getWindowWidth() - this.dialog.getWidth() * scale) / 2.0F / scale, y + 10.0F, inputHandler);
      if (this.currentPage != null) {
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
      }

      GuiNotifications.INSTANCE.draw(vg, x + 224 + 528, y + 720 + 72, inputHandler);
      nanoVGHelper.resetTransform(vg);
      this.isDrawing = false;
   }

   @Override
   public void onCharTyped(char c, int modifiers) {
      try {
         super.onCharTyped(c, modifiers);
         this.currentPage.charTyped(c, modifiers);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      try {
         super.onKeyPressed(keyCode, scanCode, modifiers);
         this.currentPage.keyTyped(keyCode, scanCode, modifiers);
      } catch (Exception var5) {
         var5.printStackTrace();
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

   public void openPage(@NotNull AbsPage page) {
      this.openPage(page, new EaseOutExpo((int)(Preferences.INSTANCE.pageAnimationDuration.getValue().floatValue() * 1000.0F), 224.0F, 2128.0F, false));
   }

   private void openPage(@NotNull AbsPage page, boolean instant) {
      if (instant) {
         this.openPageInstant(page);
      } else {
         this.openPage(page);
      }
   }

   private void openPageInstant(@NotNull AbsPage page) {
      this.openPage(page, new DummyAnimation(2128.0F));
   }

   public void openPage(@NotNull AbsPage page, Animation animation) {
      if (page.needServiceAvailable() && !NeteaseLauncher.get().isAvailable()) {
         page = new UnavailablePage(page);
      }

      if (!Preferences.INSTANCE.showPageAnimations.getValue()) {
         animation = new DummyAnimation(animation.getEnd());
      }

      if (page != this.currentPage) {
         this.currentPage.finishUpAndClose();
         if (page.parents.isEmpty()) {
            page.parents.addAll(this.currentPage.parents);
            if (!page.isBase()) {
               boolean alreadyInParents = false;

               for (int i = 0; i < page.parents.size(); i++) {
                  AbsPage parent = page.parents.get(i);
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

         page.init();
         this.sideBar.pageOpened(page.parents.get(0).getTitle(), animation instanceof DummyAnimation);
         if (this.prevPage == null) {
            this.prevPage = this.currentPage;
         }

         this.currentPage = page;
         this.pageAnimation = animation;
      }
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
   public void onScreenClose() {
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

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public boolean hasBackgroundBlur() {
      return Preferences.INSTANCE.enableBlur.getValue();
   }

   public static NeteaseScreen create() {
      return INSTANCE == null ? new NeteaseScreen() : INSTANCE;
   }

   public static boolean isOpen() {
      return Platform.getGuiPlatform().getCurrentScreen() instanceof NeteaseScreen;
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

   @Generated
   public boolean isDrawing() {
      return this.isDrawing;
   }
}
