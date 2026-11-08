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
import dev.southside.nel.open_nel.InterceptorInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_412;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_642.class_8678;
import org.jetbrains.annotations.NotNull;

public class InterceptorsPage extends AbsPage {
   private final List<InterceptorsPage.InterceptorElement> elements = new ArrayList<>();

   public InterceptorsPage() {
      super("Interceptors");
   }

   public void refresh() {
      NeteaseLauncher.get().makeClient().thenAccept(client -> {
         this.elements.clear();

         for (InterceptorInfo interceptor : client.interceptors) {
            this.elements.add(new InterceptorsPage.InterceptorElement(936, 60, ColorPalette.TRANSPARENT, interceptor));
         }
      });
   }

   @Override
   public void init() {
      this.refresh();
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      x += 15;
      y += 10;
      nanoVGHelper.drawText(vg, "Interceptors", x + 30.0F, y + 30.0F, -1, 28.0F, Fonts.BOLD);
      y += 60;
      nanoVGHelper.drawLine(vg, x + 40.0F, y, x + 1056.0F - 50.0F, y, 1.0F, Colors.GRAY_700);
      float xStart = x + 40.0F;
      float yStart = y + 15.0F;
      if (this.elements.isEmpty()) {
         nanoVGHelper.drawCenteredText(vg, "No interceptors", xStart + 500.5F - 40.0F, y + 321.5F - 60.0F, -1, 28.0F, Fonts.BOLD);
      } else {
         yStart += 10.0F;
         nanoVGHelper.drawText(vg, "SERVER", xStart + 15.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         float xAdvance = nanoVGHelper.getTextWidth(vg, "AAAAAAAAAA", 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, "ROLE", xStart + xAdvance + 15.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         nanoVGHelper.drawText(vg, "ADDRESS", xStart + 936.0F - 195.0F, yStart - 8.0F, Colors.WHITE_50, 12.0F, Fonts.SEMIBOLD);
         yStart += 5.0F;

         for (InterceptorsPage.InterceptorElement element : this.elements) {
            element.draw(vg, xStart, yStart, inputHandler);
            yStart += element.getHeight() + 10.0F;
         }
      }
   }

   @Override
   public boolean needServiceAvailable() {
      return true;
   }

   public class InterceptorElement extends BasicElement {
      private final InterceptorInfo interceptor;
      private float hoverAlpha = 0.0F;
      private final ActionButton select = new ActionButton(45, 45, "▶", 2, ColorPalette.TRANSPARENT);
      private final ActionButton delete = new ActionButton(45, 45, "×", 2, ColorPalette.TRANSPARENT);

      public InterceptorElement(int width, int height, @NotNull ColorPalette colorPalette, InterceptorInfo interceptor) {
         super(width, height, colorPalette, true);
         this.select.setCornerRadius(22.5F);
         this.delete.setCornerRadius(22.5F);
         this.select.setFontSize(18.0F);
         this.delete.setFontSize(18.0F);
         ColorPalette button = new ColorPalette(Colors.GRAY_700, Colors.GRAY_500, Colors.GRAY_400);
         this.select.setColorPalette(button);
         this.delete.setColorPalette(button);
         this.interceptor = interceptor;
         this.setColorPalette(new ColorPalette(Colors.GRAY_700, Colors.GRAY_600, Colors.GRAY_500));
         this.select
            .setClickAction(
               () -> MinecraftAccessor.mc
                  .execute(
                     () -> {
                        Notification.addNotification("Joining: '" + this.interceptor.address() + "'", "Join", Notification.NotificationType.DISABLED);
                        class_412.method_36877(
                           MinecraftAccessor.mc.field_1755,
                           MinecraftAccessor.mc,
                           class_639.method_2950(this.interceptor.address()),
                           new class_642("jiji岛", this.interceptor.address(), class_8678.field_45611),
                           false,
                           null
                        );
                     }
                  )
            );
         this.delete
            .setClickAction(
               () -> {
                  CompletableFuture<Void> future = new CompletableFuture<>();
                  NeteaseLauncher.get()
                     .makeClient()
                     .thenAccept(client -> client.shutdownGame(List.of(this.interceptor.uuid()), 5L, TimeUnit.SECONDS).thenAccept(r -> {
                        future.complete(null);
                        if (r.msg() == null) {
                           Notification.addNotification("'" + this.interceptor.address() + "' stopped", "Stop", Notification.NotificationType.ENABLED);
                           InterceptorsPage.this.refresh();
                        }
                     }));
                  NeteaseScreen.INSTANCE.dialog.setTask(future, "Loading");
               }
            );
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
         nanoVGHelper.drawText(vg, "布吉岛", x + 15.0F, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         float xAdvance = nanoVGHelper.getTextWidth(vg, "AAAAAAAAAA", 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, this.interceptor.roleId(), x + 15.0F + xAdvance, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         nanoVGHelper.drawText(vg, this.interceptor.address(), x + this.getWidth() - 195.0F, centerY, Colors.WHITE_80, 24.0F, Fonts.REGULAR);
         float targetAlpha = this.hovered ? 1.0F : 0.0F;
         float alphaSpeed = 0.3F;
         this.hoverAlpha = this.hoverAlpha + (targetAlpha - this.hoverAlpha) * alphaSpeed;
         if (this.hoverAlpha > 0.01F) {
            float alpha = nanoVGHelper.lastAlpha();
            nanoVGHelper.setAlpha(vg, this.hoverAlpha);
            this.select.draw(vg, x + this.getWidth() - 360.0F, centerY - this.select.getHeight() / 2.0F, inputHandler);
            this.delete.draw(vg, x + this.getWidth() - 360.0F + this.select.getWidth() + 10.0F, centerY - this.delete.getHeight() / 2.0F, inputHandler);
            nanoVGHelper.setAlpha(vg, alpha);
         }
      }
   }
}
