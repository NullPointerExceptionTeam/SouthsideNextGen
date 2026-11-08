package dev.southside.gui.netease.pages;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import dev.southside.gui.netease.AbsPage;
import dev.southside.gui.netease.ActionButton;
import dev.southside.gui.netease.NeteaseScreen;
import dev.southside.module.impl.client.Notification;
import dev.southside.nel.GameServer;
import dev.southside.nel.NeteaseLauncher;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UnavailablePage extends AbsPage {
   private static final String CRC_SALT = "B73962A7833192F9CAD0D68A2AA4462E";
   private static final String CRC_VER = "1.15.18.46492";
   private final ActionButton retryButton = new ActionButton(200, 50, "Retry", 2, ColorPalette.PRIMARY);
   private static CompletableFuture<Void> cfuture;
   private final AbsPage parent;

   public UnavailablePage(AbsPage parent) {
      super("Transport");
      this.retryButton.setClickAction(() -> {
         if (cfuture == null || cfuture.isDone()) {
            cfuture = new CompletableFuture<>();
            if (!NeteaseLauncher.get().isProcessLaunched()) {
               CompletableFuture<Void> future = NeteaseLauncher.get().restart();
               NeteaseScreen.INSTANCE.dialog.setTask(future, "Restarting..");
               future.thenRunAsync(() -> {
                  CompletableFuture<Void> post = new CompletableFuture<>();
                  NeteaseScreen.INSTANCE.dialog.setTask(post, "Handshaking..");

                  try {
                     Thread.sleep(2000L);
                  } catch (InterruptedException var2) {
                     throw new RuntimeException(var2);
                  }

                  CompletableFuture<GameServer.GameConnection> clientFuture = NeteaseLauncher.get().makeClient();
                  clientFuture.whenComplete((client, t) -> {
                     if (t != null) {
                        post.complete(null);
                        Notification.addNotification(t.getMessage(), "Handshake failed", Notification.NotificationType.DISABLED);
                     } else {
                        if (!client.isAvailable()) {
                           post.complete(null);
                           Notification.addNotification("再开一次", "Connect failed", Notification.NotificationType.DISABLED);
                           cfuture.complete(null);
                           return;
                        }

                        if (!client.isHandshaked()) {
                           client.handshake("B73962A7833192F9CAD0D68A2AA4462E", "1.15.18.46492", 5L, TimeUnit.SECONDS).thenAccept(response -> {
                              post.complete(null);
                              if (response != null) {
                                 Notification.addNotification(response, "Handshake failed", Notification.NotificationType.DISABLED);
                              } else {
                                 Notification.addNotification("Handshake successful", "Handshake", Notification.NotificationType.ENABLED);
                              }
                           });
                        } else {
                           post.complete(null);
                        }
                     }

                     cfuture.complete(null);
                  });
               }, NeteaseLauncher.get().getExecutor());
            } else {
               CompletableFuture<GameServer.GameConnection> future = NeteaseLauncher.get().makeClient();
               NeteaseScreen.INSTANCE.dialog.setTask(future, "Connecting..");
               future.whenComplete((client, t) -> {
                  CompletableFuture<Void> post = new CompletableFuture<>();
                  NeteaseScreen.INSTANCE.dialog.setTask(post, "Handshaking..");
                  if (t != null) {
                     post.complete(null);
                     Notification.addNotification(t.getMessage(), "Handshake failed", Notification.NotificationType.DISABLED);
                  } else {
                     if (!client.isAvailable()) {
                        post.complete(null);
                        Notification.addNotification("再开一次", "Connect failed", Notification.NotificationType.DISABLED);
                        cfuture.complete(null);
                        return;
                     }

                     if (!client.isHandshaked()) {
                        client.handshake("B73962A7833192F9CAD0D68A2AA4462E", "1.15.18.46492", 5L, TimeUnit.SECONDS).thenAccept(response -> {
                           post.complete(null);
                           if (response != null) {
                              Notification.addNotification(response, "Handshake failed", Notification.NotificationType.DISABLED);
                           } else {
                              Notification.addNotification("Handshake successful", "Handshake", Notification.NotificationType.ENABLED);
                           }
                        });
                     } else {
                        post.complete(null);
                     }
                  }

                  cfuture.complete(null);
               });
            }
         }
      });
      this.parent = parent;
   }

   @Override
   public void init() {
      if (!NeteaseLauncher.get().isAvailable()) {
         this.retryButton.getRunnable().run();
      }
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      if (!NeteaseLauncher.get().isAvailable()) {
         this.drawServiceUnavailable(vg, x, y, inputHandler);
      } else {
         NeteaseScreen.INSTANCE.openPage(this.parent);
      }

      if (cfuture != null) {
      }
   }

   private void drawServiceUnavailable(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float centerX = x + 528.0F;
      float centerY = y + 364.0F;
      nanoVGHelper.drawCenteredText(vg, "Service Unavailable", centerX - 100.0F, centerY - 130.0F, -1, 25.0F, Fonts.BOLD);
      this.retryButton.draw(vg, centerX - this.retryButton.getWidth() / 2.0F - 100.0F, centerY - this.retryButton.getHeight() / 2.0F - 80.0F, inputHandler);
   }
}
