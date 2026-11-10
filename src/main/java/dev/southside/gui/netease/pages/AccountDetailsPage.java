package dev.southside.gui.netease.pages;

import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutExpo;
import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtils;
import dev.southside.MinecraftAccessor;
import dev.southside.gui.netease.AbsPage;
import dev.southside.gui.netease.ActionButton;
import dev.southside.gui.netease.NeteaseScreen;
import dev.southside.module.impl.client.Notification;
import dev.southside.nel.NeteaseLauncher;
import dev.southside.nel.open_nel.GameCharacter;
import dev.southside.nel.open_nel.NeteaseAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import org.jetbrains.annotations.NotNull;

public class AccountDetailsPage extends AbsPage {
   private final AccountsPage parent;
   private final NeteaseAccount account;
   private final String serverId;
   private final ActionButton joinButton = new ActionButton(100, 35, "Join", 2, ColorPalette.PRIMARY);
   private final TextInputField name = new TextInputField(300, 50, "name", false, false);
   private final ActionButton addButton = new ActionButton(100, 50, "Add", 2, ColorPalette.PRIMARY);
   private final ActionButton refreshButton = new ActionButton(100, 35, "Refresh", 2, ColorPalette.PRIMARY);
   private final ActionButton cancelButton = new ActionButton(100, 35, "Cancel", 2, ColorPalette.PRIMARY);
   private final List<GameCharacter> characters = new ArrayList<>();
   private CompletableFuture<Void> characterFuture;
   private AccountDetailsPage.CharacterSelector selector;

   public AccountDetailsPage(AccountsPage parent, NeteaseAccount account, String serverId) {
      super("AccountDetails");
      this.parent = parent;
      this.account = account;
      this.serverId = serverId;
      this.cancelButton.setClickAction(() -> NeteaseScreen.INSTANCE.openPage(parent));
      this.joinButton
         .setClickAction(
            () -> {
               if (this.selector != null) {
                  int selectedId = this.selector.selected;
                  GameCharacter ac = this.selector.buttons.get(selectedId).character;
                  this.cancelButton.disable(true);
                  this.joinButton.disable(true);
                  CompletableFuture<Void> post = new CompletableFuture<>();
                  NeteaseLauncher.get()
                     .makeClient()
                     .thenAccept(
                        client -> client.joinGame(serverId, ac.id, "布吉岛", 100L, TimeUnit.SECONDS)
                           .thenAccept(
                              response -> {
                                 this.joinButton.disable(false);
                                 this.cancelButton.disable(false);
                                 if (response.obj() != null) {
                                    String addr = response.obj().address();
                                    MinecraftAccessor.mc
                                       .execute(
                                          () -> {
                                             Notification.addNotification("Joining: '" + addr + "'", "Join", Notification.NotificationType.DISABLED);
                                             ConnectScreen.connect(
                                                MinecraftAccessor.mc.currentScreen,
                                                MinecraftAccessor.mc,
                                                ServerAddress.parse(addr),
                                                new ServerInfo("jiji岛", addr, ServerType.OTHER),
                                                false,
                                                null
                                             );
                                          }
                                       );
                                 } else {
                                    Notification.addNotification(response.msg(), "Join failed", Notification.NotificationType.DISABLED);
                                 }

                                 post.complete(null);
                              }
                           )
                     );
                  NeteaseScreen.INSTANCE.dialog.setTask(post, "Starting...");
               } else {
                  Notification.addNotification("未获取到角色信息", "Join failed", Notification.NotificationType.DISABLED);
               }
            }
         );
      this.refreshButton.setClickAction(() -> {
         if (this.characterFuture == null || this.characterFuture.isDone()) {
            this.refreshButton.disable(true);
            CompletableFuture<Void> future = new CompletableFuture<>();
            NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.openServerAndGetRoles(serverId, 5L, TimeUnit.SECONDS).thenAccept(pack -> {
               future.complete(null);
               this.refreshButton.disable(false);
               if (pack.msg() == null) {
                  this.characters.clear();
                  this.characters.addAll(pack.obj());
                  Notification.addNotification("", "Fetching success", Notification.NotificationType.ENABLED);
                  this.selector = new AccountDetailsPage.CharacterSelector(300, 120, ColorPalette.SECONDARY, false);
               } else {
                  Notification.addNotification(pack.msg(), "Fetching failed", Notification.NotificationType.DISABLED);
               }
            }));
            this.characterFuture = future;
            NeteaseScreen.INSTANCE.dialog.setTask(future, "Fetching roles..");
         }
      });
      this.addButton
         .setClickAction(
            () -> {
               if (this.name.getInput().isEmpty()) {
                  Notification.addNotification("名字为空", "Add", Notification.NotificationType.DISABLED);
               } else {
                  this.addButton.disable(true);
                  CompletableFuture<Void> future = new CompletableFuture<>();
                  NeteaseLauncher.get()
                     .makeClient()
                     .thenAccept(client -> client.api.createRole(serverId, this.name.getInput(), 10L, TimeUnit.SECONDS).thenAccept(pack -> {
                        if (pack.msg() == null) {
                           Notification.addNotification("Role '" + this.name.getInput() + "' created", "Role", Notification.NotificationType.ENABLED);
                           client.api.openServerAndGetRoles(serverId, 5L, TimeUnit.SECONDS).thenAccept(pack1 -> {
                              future.complete(null);
                              this.addButton.disable(false);
                              if (pack1.msg() == null) {
                                 this.characters.clear();
                                 this.characters.addAll(pack1.obj());
                                 Notification.addNotification("", "Fetching success", Notification.NotificationType.ENABLED);
                                 this.selector = new AccountDetailsPage.CharacterSelector(300, 120, ColorPalette.SECONDARY, false);
                              } else {
                                 Notification.addNotification(pack1.msg(), "Fetching failed", Notification.NotificationType.DISABLED);
                              }
                           });
                        } else {
                           Notification.addNotification(pack.msg(), "Create failed", Notification.NotificationType.DISABLED);
                        }
                     }));
                  this.characterFuture = future;
                  NeteaseScreen.INSTANCE.dialog.setTask(future, "Creating role..");
               }
            }
         );
   }

   @Override
   public void init() {
      super.init();
      this.addButton.disable(true);
      this.refreshButton.disable(true);
      CompletableFuture<Void> future = new CompletableFuture<>();
      NeteaseLauncher.get().makeClient().thenAccept(client -> client.api.openServerAndGetRoles(this.serverId, 5L, TimeUnit.SECONDS).thenAccept(pack -> {
         future.complete(null);
         this.addButton.disable(false);
         this.refreshButton.disable(false);
         if (pack.msg() == null) {
            this.characters.clear();
            this.characters.addAll(pack.obj());
            Notification.addNotification("", "Fetching success", Notification.NotificationType.ENABLED);
            this.selector = new AccountDetailsPage.CharacterSelector(300, 120, ColorPalette.SECONDARY, false);
         } else {
            Notification.addNotification(pack.msg(), "Fetching failed", Notification.NotificationType.DISABLED);
         }
      }));
      this.characterFuture = future;
      NeteaseScreen.INSTANCE.dialog.setTask(future, "Fetching roles..");
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      x += 15;
      y += 10;
      nanoVGHelper.drawText(vg, "Account - " + this.account.entityId, x + 30.0F, y + 30.0F, -1, 28.0F, Fonts.BOLD);
      y += 60;
      nanoVGHelper.drawLine(vg, x + 40.0F, y, x + 1056.0F - 50.0F, y, 1.0F, Colors.GRAY_700);
      float xStart = x + 80.0F;
      float yStart = y + 45.0F;
      nanoVGHelper.drawText(vg, "SERVER ID: ", xStart, yStart, Colors.WHITE_50, 20.0F, Fonts.SEMIBOLD);
      float xAdvance = nanoVGHelper.getTextWidth(vg, "CHARACTERS: ", 20.0F, Fonts.SEMIBOLD);
      nanoVGHelper.drawText(vg, this.serverId, xStart + xAdvance + 15.0F, yStart, Colors.WHITE_80, 20.0F, Fonts.SEMIBOLD);
      yStart += 60.0F;
      nanoVGHelper.drawText(vg, "CHANNEL: ", xStart, yStart, Colors.WHITE_50, 20.0F, Fonts.SEMIBOLD);
      nanoVGHelper.drawText(vg, this.account.channel.toUpperCase(Locale.ROOT), xStart + xAdvance + 15.0F, yStart, Colors.WHITE_80, 20.0F, Fonts.SEMIBOLD);
      yStart += 60.0F;
      nanoVGHelper.drawText(vg, "CHARACTERS: ", xStart, yStart, Colors.WHITE_50, 20.0F, Fonts.SEMIBOLD);
      this.refreshButton.draw(vg, xStart + xAdvance + 15.0F, yStart + 140.0F, inputHandler);
      this.joinButton.draw(vg, xStart + xAdvance + 15.0F + this.refreshButton.getWidth() + 10.0F, yStart + 140.0F, inputHandler);
      this.name.draw(vg, xStart + xAdvance + 15.0F, yStart + 140.0F + this.joinButton.getHeight() + 20.0F, inputHandler);
      this.addButton.draw(vg, xStart + xAdvance + 15.0F + this.name.getWidth() + 10.0F, yStart + 140.0F + this.joinButton.getHeight() + 20.0F, inputHandler);
      this.cancelButton
         .draw(vg, xStart + xAdvance + 15.0F, yStart + 140.0F + this.joinButton.getHeight() + 20.0F + this.name.getHeight() + 20.0F, inputHandler);
      if (this.characterFuture != null && this.characterFuture.isDone() && this.selector != null) {
         this.selector.draw(vg, xStart + xAdvance + 15.0F, yStart - 10.0F, inputHandler);
      } else {
         nanoVGHelper.drawText(vg, "Unfetched", xStart + xAdvance + 15.0F, yStart, Colors.WHITE_80, 20.0F, Fonts.SEMIBOLD);
      }
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      super.keyTyped(keyCode, scanCode, modifiers);
      this.name.keyTyped(keyCode, scanCode, modifiers);
   }

   @Override
   public void charTyped(char c, int modifiers) {
      super.charTyped(c, modifiers);
      this.name.charTyped(c, modifiers);
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      this.name.mouseReleased(mouseX, mouseY, state);
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.name.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   public boolean needServiceAvailable() {
      return true;
   }

   public static class CharacterButton extends BasicElement {
      public final GameCharacter character;
      protected String text;
      private final int alignment;
      private float fontSize;
      private float cornerRadius;
      private final float xSpacing;
      private final float xPadding;
      public float x;
      public float y;
      public static final int ALIGNMENT_LEFT = 0;
      public static final int ALIGNMENT_CENTER = 2;
      public static final int ALIGNMENT_JUSTIFIED = 3;
      private boolean toggleable = false;
      private Runnable runnable;

      public CharacterButton(int width, int height, @NotNull ColorPalette colorPalette, GameCharacter character) {
         super(width, height, colorPalette, true);
         this.character = character;
         this.alignment = 2;
         this.xSpacing = 0.0F;
         this.xPadding = 0.0F;
         this.cornerRadius = 3.0F;
         this.fontSize = 20.0F;
      }

      @Override
      public void draw(long vg, float x, float y, InputHandler inputHandler) {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         this.x = x;
         this.y = y;
         this.update(x, y, inputHandler);
         float lastAlpha = nanoVGHelper.lastAlpha();
         if (this.disabled) {
            nanoVGHelper.setAlpha(vg, 0.5F);
         }

         float contentWidth = 0.0F;
         int color;
         if (this.colorPalette != ColorPalette.TERTIARY && this.colorPalette != ColorPalette.TERTIARY_DESTRUCTIVE) {
            nanoVGHelper.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, this.cornerRadius);
            color = ColorUtils.setAlpha(Colors.WHITE, (int)(this.colorAnimation.getAlpha() * 255.0F));
         } else {
            color = this.currentColor;
         }

         float middle = x + this.width / 2.0F;
         float middleYText = y + this.height / 2.0F + this.fontSize / 8.0F;
         if (this.character != null) {
            contentWidth += nanoVGHelper.getTextWidth(vg, this.character.name, this.fontSize, Fonts.MEDIUM);
         }

         if (this.alignment == 2) {
            if (this.character != null) {
               nanoVGHelper.drawText(vg, this.character.name, middle - contentWidth / 2.0F, middleYText, color, this.fontSize, Fonts.MEDIUM);
            }
         } else if (this.alignment == 3) {
            if (this.character != null) {
               nanoVGHelper.drawText(vg, this.character.name, middle - contentWidth / 2.0F, middleYText, color, this.fontSize, Fonts.MEDIUM);
            }
         } else if (this.alignment == 0) {
            contentWidth = this.xPadding;
            if (this.character != null) {
               nanoVGHelper.drawText(vg, this.character.name, x + contentWidth, middleYText, color, this.fontSize, Fonts.MEDIUM);
            }
         }

         if (this.disabled) {
            nanoVGHelper.setAlpha(vg, lastAlpha);
         }
      }

      @Override
      public void onClick() {
         if (!this.disabled) {
            if (this.runnable != null) {
               this.runnable.run();
            }

            if (this.toggleable && this.toggled) {
               this.setColorPalette(ColorPalette.PRIMARY);
            } else if (this.toggleable) {
               this.setColorPalette(ColorPalette.SECONDARY);
            }
         }
      }

      @Override
      public void setToggled(boolean toggled) {
         this.toggled = toggled;
         if (toggled && this.toggleable) {
            this.setColorPalette(ColorPalette.PRIMARY);
         } else if (this.toggleable) {
            this.setColorPalette(ColorPalette.SECONDARY);
         }
      }

      public void setToggleable(boolean state) {
         this.toggleable = state;
      }

      public void setClickAction(Runnable runnable) {
         this.runnable = runnable;
      }

      public String getText() {
         return this.text;
      }

      public void setText(String text) {
         this.text = text;
      }

      public boolean hasClickAction() {
         return this.runnable != null;
      }

      @Generated
      public void setFontSize(float fontSize) {
         this.fontSize = fontSize;
      }

      @Generated
      public void setCornerRadius(float cornerRadius) {
         this.cornerRadius = cornerRadius;
      }
   }

   public class CharacterSelector extends BasicElement {
      private boolean init;
      private final List<AccountDetailsPage.CharacterButton> buttons = new ArrayList<>();
      private int selected;
      private Animation moveAnimation;
      private Animation sizeAnimation;
      private int y;
      private int sidebarY;

      public CharacterSelector(int width, int height, @NotNull ColorPalette colorPalette, boolean hoverFx) {
         super(width, height, colorPalette, hoverFx);

         for (GameCharacter character : AccountDetailsPage.this.characters) {
            this.buttons.add(new AccountDetailsPage.CharacterButton(this.getWidth(), this.getHeight() / 3, ColorPalette.TERTIARY, character));
         }

         if (!this.buttons.isEmpty()) {
            this.buttons.get(0).setColorPalette(ColorPalette.PRIMARY);
         }

         this.selected = 0;
         this.moveAnimation = null;
         this.sizeAnimation = null;
      }

      @Override
      public void draw(long vg, float x, float y, InputHandler inputHandler) {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         this.update(x, y, inputHandler);
         nanoVGHelper.drawRoundedRect(vg, x, y, this.width, this.height, this.currentColor, 3.0F);
         this.y = (int)y;

         for (AccountDetailsPage.CharacterButton button : this.buttons) {
            if (button.isClicked()) {
               this.moveSideBar(button, false);
               break;
            }
         }

         if (this.moveAnimation != null) {
            nanoVGHelper.drawRoundedRect(
               vg,
               x,
               y + this.moveAnimation.get() - (this.sizeAnimation.get() - 36.0F) / 2.0F,
               this.getWidth(),
               this.sizeAnimation.get(0.0F),
               Colors.PRIMARY_600,
               3.0F
            );
            if (this.moveAnimation.isFinished() && this.sizeAnimation.isFinished()) {
               this.moveAnimation = null;
               this.sizeAnimation = null;
               this.buttons.get(this.selected).setColorPalette(ColorPalette.PRIMARY);
            }
         }

         float yStart = y;

         for (AccountDetailsPage.CharacterButton buttonx : this.buttons) {
            buttonx.draw(vg, x, yStart, inputHandler);
            yStart += buttonx.getHeight();
         }

         this.sidebarY = 0;
      }

      private void moveSideBar(AccountDetailsPage.CharacterButton button) {
         this.moveSideBar(button, false);
      }

      private void moveSideBar(AccountDetailsPage.CharacterButton button, boolean instant) {
         if (!button.equals(this.buttons.get(this.selected)) || !this.init) {
            this.init = true;
            this.buttons.get(this.selected).setColorPalette(ColorPalette.TERTIARY);
            if (instant) {
               this.moveAnimation = new DummyAnimation(button.y - this.y);
            } else {
               this.moveAnimation = new EaseOutExpo(300, this.buttons.get(this.selected).y - this.y, button.y - this.y, false);
            }

            this.sizeAnimation = new DummyAnimation(this.getHeight() / 3.0F);
            this.selected = this.buttons.indexOf(button);
         }
      }
   }
}
