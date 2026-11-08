package dev.southside.gui.netease.pages;

import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.MinecraftAccessor;
import dev.southside.gui.netease.AbsPage;
import dev.southside.gui.netease.ActionButton;
import dev.southside.gui.netease.NeteaseScreen;
import dev.southside.module.impl.client.Notification;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.nel.open_nel.NeteaseAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class AccountsPage extends AbsPage {
   private static final String BJD_SERVER_ID = "4661334467366178884";
   private final List<NeteaseAccount> accounts = new ArrayList<>();
   private boolean changed;
   private final ActionButton refreshButton = new ActionButton(100, 35, "Refresh", 2, ColorPalette.PRIMARY);
   private final ActionButton addButton = new ActionButton(100, 35, "Add", 2, ColorPalette.PRIMARY);
   private CompletableFuture<Void> last;
   private final List<AccountsPage.AccountElement> elements;

   public AccountsPage() {
      super("Accounts");
      this.refreshButton.setClickAction(this::refresh);
      this.addButton.setClickAction(() -> NeteaseScreen.INSTANCE.openPage(new AddAccountPage(this)));
      this.elements = new ArrayList<>();
   }

   @Override
   public void init() {
      NeteaseLauncher.get().getExecutor().submit(this::refresh);
   }

   public CompletableFuture<Void> refresh() {
      this.refreshButton.disable(true);
      if (this.last == null) {
         this.last = new CompletableFuture<>();
         NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.fetchAccounts(5L, TimeUnit.SECONDS).thenAccept(pack -> {
            this.last.complete(null);
            if (pack.msg() != null) {
               Notification.addNotification(pack.msg(), "Fetch failed", Notification.NotificationType.DISABLED);
            } else {
               this.accounts.clear();
               this.accounts.addAll(pack.obj());
               this.changed = true;
            }

            this.refreshButton.disable(false);
            this.last = null;
         }));
      }

      NeteaseScreen.INSTANCE.dialog.setTask(this.last, "Fetching..");
      return this.last;
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      x += 15;
      y += 10;
      nanoVGHelper.drawText(vg, "Accounts", x + 30.0F, y + 30.0F, -1, 28.0F, Fonts.BOLD);
      this.refreshButton.draw(vg, x + 1056.0F - this.refreshButton.getWidth() * 2 - 40.0F - 10.0F, y, inputHandler);
      this.addButton.draw(vg, x + 1056.0F - this.refreshButton.getWidth() - 40.0F, y, inputHandler);
      y += 60;
      nanoVGHelper.drawLine(vg, x + 40.0F, y, x + 1056.0F - 50.0F, y, 1.0F, Colors.GRAY_700);
      float xStart = x + 40.0F;
      float yStart = y + 15.0F;
      if (this.changed) {
         this.elements.clear();

         for (NeteaseAccount account : this.accounts) {
            this.elements.add(new AccountsPage.AccountElement(936, 60, ColorPalette.TRANSPARENT, account));
         }

         this.changed = false;
      }

      if (this.accounts.isEmpty()) {
         nanoVGHelper.drawCenteredText(vg, "No accounts", xStart + 500.5F - 40.0F, y + 321.5F - 60.0F, -1, 28.0F, Fonts.BOLD);
      } else {
         yStart += 10.0F;
         nanoVGHelper.drawText(vg, "ID", xStart + 15.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         float xAdvance = nanoVGHelper.getTextWidth(vg, "AAAAAAAAAA", 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, "CHANNEL", xStart + xAdvance + 15.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         nanoVGHelper.drawText(vg, "STATUS", xStart + 936.0F - 85.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         yStart += 5.0F;

         for (AccountsPage.AccountElement element : this.elements) {
            element.draw(vg, xStart, yStart, inputHandler);
            yStart += element.getHeight() + 10.0F;
         }
      }
   }

   @Override
   public boolean needServiceAvailable() {
      return true;
   }

   public class AccountElement extends BasicElement {
      private final NeteaseAccount account;
      private float hoverAlpha = 0.0F;
      private final ActionButton select = new ActionButton(45, 45, "▶", 2, ColorPalette.TRANSPARENT);
      private final ActionButton delete = new ActionButton(45, 45, "×", 2, ColorPalette.TRANSPARENT);

      public AccountElement(int width, int height, @NotNull ColorPalette colorPalette, NeteaseAccount account) {
         super(width, height, colorPalette, true);
         this.select.setCornerRadius(22.5F);
         this.delete.setCornerRadius(22.5F);
         this.select.setFontSize(18.0F);
         this.delete.setFontSize(18.0F);
         ColorPalette button = new ColorPalette(Colors.GRAY_700, Colors.GRAY_500, Colors.GRAY_400);
         this.select.setColorPalette(button);
         this.delete.setColorPalette(button);
         this.account = account;
         this.setColorPalette(new ColorPalette(Colors.GRAY_700, Colors.GRAY_600, Colors.GRAY_500));
         this.select.setClickAction(() -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.selectAccount(this.account.entityId, 5L, TimeUnit.SECONDS).thenAccept(r -> {
               future.complete(null);
               if (r != null) {
                  Notification.addNotification(r, "Loading profile failed", Notification.NotificationType.DISABLED);
               } else {
                  NeteaseScreen.INSTANCE.openPage(new AccountDetailsPage(AccountsPage.this, account, "4661334467366178884"));
               }
            }));
            NeteaseScreen.INSTANCE.dialog.setTask(future, "Loading");
         });
         this.delete.setClickAction(() -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.deleteAccount(this.account.entityId, 5L, TimeUnit.SECONDS).thenAccept(r -> {
               future.complete(null);
               if (r.msg() == null) {
                  Notification.addNotification("'" + this.account.entityId + "' deleted", "Delete", Notification.NotificationType.ENABLED);
                  MinecraftAccessor.mc.execute(AccountsPage.this::refresh);
               }
            }));
            NeteaseScreen.INSTANCE.dialog.setTask(future, "Loading");
         });
      }

      @Override
      public void draw(long vg, float x, float y, InputHandler inputHandler) {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         this.update(x, y, inputHandler);
         if (this.select.isHovered() || this.delete.isHovered()) {
            this.currentColor = this.colorAnimation.getColor(false, false);
         }

         nanoVGHelper.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, 17.0F);
         float centerY = y + this.getHeight() / 2.0F;
         nanoVGHelper.drawText(vg, this.account.entityId, x + 15.0F, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         float xAdvance = nanoVGHelper.getTextWidth(vg, "AAAAAAAAAA", 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, this.account.channel, x + 15.0F + xAdvance, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, this.account.status, x + this.getWidth() - 85.0F, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         float targetAlpha = this.hovered ? 1.0F : 0.0F;
         float alphaSpeed = 0.3F;
         this.hoverAlpha = this.hoverAlpha + (targetAlpha - this.hoverAlpha) * alphaSpeed;
         if (this.hoverAlpha > 0.01F) {
            float alpha = nanoVGHelper.lastAlpha();
            nanoVGHelper.setAlpha(vg, this.hoverAlpha);
            this.select.draw(vg, x + this.getWidth() - 300.0F, centerY - this.select.getHeight() / 2.0F, inputHandler);
            this.delete.draw(vg, x + this.getWidth() - 300.0F + this.select.getWidth() + 10.0F, centerY - this.delete.getHeight() / 2.0F, inputHandler);
            nanoVGHelper.setAlpha(vg, alpha);
         }
      }
   }
}
