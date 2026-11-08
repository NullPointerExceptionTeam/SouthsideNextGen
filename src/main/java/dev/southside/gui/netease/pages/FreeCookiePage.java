package dev.southside.gui.netease.pages;

import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.gui.netease.AbsPage;
import dev.southside.gui.netease.ActionButton;
import dev.southside.gui.netease.NeteaseScreen;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.client.Account;
import dev.southside.module.impl.client.Notification;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class FreeCookiePage extends AbsPage {
   private static Account.Credentials lastCredentials;
   private final ActionButton getButton = new ActionButton(100, 35, "Get", 2, ColorPalette.PRIMARY);
   private final ActionButton copyNameButton = new ActionButton(35, 35, "C", 2, ColorPalette.PRIMARY);
   private final ActionButton copyPasswordButton = new ActionButton(35, 35, "C", 2, ColorPalette.PRIMARY);
   private final ActionButton addToNELButton = new ActionButton(100, 35, "ToNEL", 2, ColorPalette.PRIMARY);
   private final TextInputField account = new TextInputField(300, 50, "Account", false, false);
   private final TextInputField password = new TextInputField(300, 50, "Password", false, false);

   public FreeCookiePage() {
      super("FreeCookie");
      this.account.disable(true);
      this.password.disable(true);
      this.getButton.setClickAction(() -> {
         CompletableFuture<Void> future = new CompletableFuture<>();
         Account accountModule = this.getAccountModule();
         if (accountModule == null) {
            Notification.addNotification("Account 模块不可用", "FreeCookie", Notification.NotificationType.DISABLED);
         } else {
            this.copyNameButton.disable(true);
            this.copyPasswordButton.disable(true);
            this.getButton.disable(true);
            this.addToNELButton.disable(true);
            accountModule.fetchAccount(credentials -> {
               lastCredentials = credentials;
               this.account.setInput(credentials.account());
               this.password.setInput(credentials.password());
               this.copyNameButton.disable(false);
               this.copyPasswordButton.disable(false);
               this.getButton.disable(false);
               this.addToNELButton.disable(false);
               Notification.addNotification("获取成功", "FreeCookie", Notification.NotificationType.ENABLED);
               future.complete(null);
            }, error -> Notification.addNotification(error, "FreeCookie", Notification.NotificationType.DISABLED));
            NeteaseScreen.INSTANCE.dialog.setTask(future, "正在获取..");
         }
      });
      this.copyNameButton.setClickAction(() -> this.copyToClipboard(this.account.getInput(), "已复制账号"));
      this.copyPasswordButton.setClickAction(() -> this.copyToClipboard(this.password.getInput(), "已复制密码"));
      this.addToNELButton.setClickAction(() -> {
         if (lastCredentials != null) {
            Notification.addNotification("正在转到Add界面", "FreeCookie", Notification.NotificationType.ENABLED);
            NeteaseScreen.INSTANCE.openPage(new AddAccountPage(this, lastCredentials));
         }
      });
   }

   @Override
   public void init() {
      if (lastCredentials != null) {
         this.account.setInput(lastCredentials.account());
         this.password.setInput(lastCredentials.password());
      }
   }

   private void copyToClipboard(String value, String successMessage) {
      String statusText = "";
      if (value != null && !value.isEmpty()) {
         if (MinecraftAccessor.mc != null && MinecraftAccessor.mc.field_1774 != null) {
            MinecraftAccessor.mc.field_1774.method_1455(value);
            statusText = successMessage;
         }

         Notification.addNotification(statusText, "FreeCookie", Notification.NotificationType.ERROR);
      } else {
         statusText = "没有可复制的内容";
      }
   }

   @Nullable
   private Account getAccountModule() {
      Southside southside = Southside.INSTANCE;
      if (southside == null) {
         return null;
      } else {
         ModuleManager moduleManager = southside.getModuleManager();
         return moduleManager == null ? null : moduleManager.get(Account.class);
      }
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float centerX = x + 528.0F - 70.0F;
      float startY = y + 130.0F;
      nanoVGHelper.drawCenteredText(vg, "Free Cookie", centerX, startY, -1, 32.0F, Fonts.BOLD);
      float fieldX = centerX - this.account.getWidth() / 2.0F;
      float fieldY = startY + 60.0F;
      this.account.draw(vg, fieldX, fieldY, inputHandler);
      this.password.draw(vg, fieldX, fieldY + this.account.getHeight() + 20.0F, inputHandler);
      float baseY = fieldY + this.account.getHeight() + this.password.getHeight() + 40.0F;
      float rightX = centerX + this.account.getWidth() / 2.0F;
      this.getButton.disable(true);
      this.addToNELButton.draw(vg, rightX - this.getButton.getWidth(), baseY, inputHandler);
      this.getButton.draw(vg, rightX - this.getButton.getWidth() - this.addToNELButton.getWidth() - 10.0F, baseY, inputHandler);
      this.copyNameButton.draw(vg, fieldX + this.account.getWidth() + 10.0F, fieldY + 7.0F, inputHandler);
      this.copyPasswordButton.draw(vg, fieldX + this.password.getWidth() + 10.0F, fieldY + this.account.getHeight() + 27.0F, inputHandler);
   }

   @Override
   public boolean needServiceAvailable() {
      return true;
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      super.keyTyped(keyCode, scanCode, modifiers);
      this.account.keyTyped(keyCode, scanCode, modifiers);
      this.password.keyTyped(keyCode, scanCode, modifiers);
   }

   @Override
   public void charTyped(char c, int modifiers) {
      super.charTyped(c, modifiers);
      this.account.charTyped(c, modifiers);
      this.password.charTyped(c, modifiers);
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      this.account.mouseReleased(mouseX, mouseY, state);
      this.password.mouseReleased(mouseX, mouseY, state);
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.account.mouseClicked(mouseX, mouseY, mouseButton);
      this.password.mouseClicked(mouseX, mouseY, mouseButton);
   }
}
