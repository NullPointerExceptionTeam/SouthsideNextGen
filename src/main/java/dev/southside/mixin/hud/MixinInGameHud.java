package dev.southside.mixin.hud;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.utils.render.jello.animation.Animation;
import dev.southside.utils.render.jello.animation.Easing;
import java.awt.Color;
import net.minecraft.class_10209;
import net.minecraft.class_1294;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_1657;
import net.minecraft.class_1702;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_266;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_4061;
import net.minecraft.class_5134;
import net.minecraft.class_5819;
import net.minecraft.class_9779;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_329.class})
public abstract class MixinInGameHud {
   @Shadow
   @Final
   private static class_2960 field_45314;
   @Shadow
   @Final
   private static class_2960 field_45315;
   @Shadow
   @Final
   private static class_2960 field_45319;
   @Shadow
   @Final
   private static class_2960 field_45320;
   @Shadow
   @Final
   private static class_2960 field_45324;
   @Shadow
   @Final
   private static class_2960 field_45325;
   @Shadow
   @Final
   private static class_2960 field_45326;
   @Shadow
   @Final
   private static class_2960 field_54912;
   @Shadow
   @Final
   private static class_2960 field_45321;
   @Shadow
   @Final
   private static class_2960 field_45327;
   @Shadow
   @Final
   private static class_2960 field_45323;
   @Shadow
   @Final
   private static class_2960 field_45322;
   @Shadow
   @Final
   private static class_2960 field_45328;
   @Shadow
   @Final
   private static class_2960 field_45298;
   @Shadow
   @Final
   private static class_2960 field_45299;
   @Shadow
   @Final
   private class_310 field_2035;
   @Shadow
   @Final
   private class_5819 field_2034;
   @Shadow
   private long field_2032;
   @Shadow
   private int field_2042;
   @Shadow
   private int field_2014;
   @Shadow
   private long field_2012;
   @Shadow
   private int field_2033;
   @Unique
   private Animation animation;

   @Shadow
   public class_327 method_1756() {
      return null;
   }

   @Shadow
   private class_1657 method_1737() {
      return null;
   }

   @Shadow
   private int method_1733(int heartCount) {
      return 0;
   }

   @Shadow
   private int method_1744(@Nullable class_1309 entity) {
      return 0;
   }

   @Shadow
   private class_1309 method_1734() {
      return null;
   }

   @Shadow
   private void method_37298(
      class_332 context,
      class_1657 player,
      int x,
      int y,
      int lines,
      int regeneratingHeartIndex,
      float maxHealth,
      int lastHealth,
      int health,
      int absorption,
      boolean blinking
   ) {
   }

   @Shadow
   protected abstract void method_1762(class_332 var1, int var2, int var3, class_9779 var4, class_1657 var5, class_1799 var6, int var7);

   @Inject(
      method = {"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderCustomScoreboard(class_332 context, class_266 objective, CallbackInfo ci) {
      if (MinecraftAccessor.mc.field_1687 != null && Southside.INSTANCE.getModuleManager().get(ScoreBoard.class).isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_1759(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void HookRenderHotbar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      HUD hud = Southside.get().getModuleManager().get(HUD.class);
      if (hud.isEnabled() && hud.hotbar.getValue()) {
         this.newHotbar(tickCounter.method_60637(false), context, tickCounter);
         ci.cancel();
      }
   }

   @Overwrite
   public void method_1754(class_332 context, int x) {
      boolean useCustomHotbar = Southside.get().getModuleManager().get(HUD.class).hotbar.getValue();
      int experience = this.field_2035.field_1724.method_7349();
      if (experience > 0) {
         class_10209.method_64146().method_15396("expBar");
         int progressWidth = (int)(this.field_2035.field_1724.field_7510 * 183.0F);
         int barY = context.method_51443() - 32;
         if (!useCustomHotbar) {
            barY += 3;
         }

         if (useCustomHotbar) {
            int finalBarY = barY;
            int experienceLevel = this.field_2035.field_1724.field_7520;
            NanoVGHelper.INSTANCE.setupAndDraw(true, vg -> {
               NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, finalBarY, 182.0F, 4.0F, -13421773, 1.0F);
               if (progressWidth > 0) {
                  NanoVGHelper.INSTANCE.drawRoundedRect(vg, x, finalBarY, progressWidth, 3.0F, -11154347, 1.0F);
               }

               if (experienceLevel > 0) {
                  class_10209.method_64146().method_15396("expLevel");
                  String levelTextx = String.valueOf(experienceLevel);
                  float textWidth = NanoVGHelper.INSTANCE.getTextWidth(vg, levelTextx, 9.0F, Fonts.BOLD);
                  float textXx = x + (182.0F - textWidth) / 2.0F;
                  float textYx = finalBarY - 5;
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx - 1.0F, textYx, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx + 1.0F, textYx, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx - 1.0F, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx + 1.0F, -16777216, 9.0F, Fonts.BOLD);
                  NanoVGHelper.INSTANCE.drawText(vg, levelTextx, textXx, textYx, -11154347, 9.0F, Fonts.BOLD);
                  class_10209.method_64146().method_15407();
               }
            });
         } else {
            context.method_52706(class_1921::method_62277, field_45319, x, barY, 182, 5);
            if (progressWidth > 0) {
               context.method_52708(class_1921::method_62277, field_45320, 182, 5, 0, 0, x, barY, progressWidth, 5);
            }

            if (this.field_2035.field_1724.field_7520 > 0) {
               class_10209.method_64146().method_15396("expLevel");
               String levelText = String.valueOf(this.field_2035.field_1724.field_7520);
               int textX = (context.method_51421() - this.method_1756().method_1727(levelText)) / 2;
               int textY = context.method_51443() - 31 - 4;
               context.method_51433(this.method_1756(), levelText, textX + 1, textY, 0, false);
               context.method_51433(this.method_1756(), levelText, textX - 1, textY, 0, false);
               context.method_51433(this.method_1756(), levelText, textX, textY + 1, 0, false);
               context.method_51433(this.method_1756(), levelText, textX, textY - 1, 0, false);
               context.method_51433(this.method_1756(), levelText, textX, textY, 8453920, false);
               class_10209.method_64146().method_15407();
            }
         }

         class_10209.method_64146().method_15407();
      }
   }

   @Overwrite
   private void method_1760(class_332 context) {
      class_1657 playerEntity = this.method_1737();
      if (playerEntity != null) {
         int i = class_3532.method_15386(playerEntity.method_6032());
         boolean bl = this.field_2032 > this.field_2042 && (this.field_2032 - this.field_2042) / 3L % 2L == 1L;
         long l = class_156.method_658();
         if (i < this.field_2014 && playerEntity.field_6008 > 0) {
            this.field_2012 = l;
            this.field_2032 = this.field_2042 + 20;
         } else if (i > this.field_2014 && playerEntity.field_6008 > 0) {
            this.field_2012 = l;
            this.field_2032 = this.field_2042 + 10;
         }

         if (l - this.field_2012 > 1000L) {
            this.field_2014 = i;
            this.field_2033 = i;
            this.field_2012 = l;
         }

         this.field_2014 = i;
         int j = this.field_2033;
         this.field_2034.method_43052(this.field_2042 * 312871);
         class_1702 hungerManager = playerEntity.method_7344();
         int k = hungerManager.method_7586();
         int m = context.method_51421() / 2 - 91;
         int n = context.method_51421() / 2 + 91;
         int o = context.method_51443() - 39;
         float f = Math.max((float)playerEntity.method_45325(class_5134.field_23716), (float)Math.max(j, i));
         int p = class_3532.method_15386(playerEntity.method_6067());
         int q = class_3532.method_15386((f + p) / 2.0F / 10.0F);
         int r = Math.max(10 - (q - 2), 3);
         int s = o - (q - 1) * r - 10;
         if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
            s -= 4;
         }

         int t = o - 10;
         int u = playerEntity.method_6096();
         int v = -1;
         if (playerEntity.method_6059(class_1294.field_5924)) {
            v = this.field_2042 % class_3532.method_15386(f + 5.0F);
         }

         class_10209.method_64146().method_15396("armor");

         for (int w = 0; w < 10; w++) {
            if (u > 0) {
               int x = m + w * 8;
               if (w * 2 + 1 < u) {
                  context.method_52706(class_1921::method_62277, field_45323, x, s, 9, 9);
               }

               if (w * 2 + 1 == u) {
                  context.method_52706(class_1921::method_62277, field_45322, x, s, 9, 9);
               }

               if (w * 2 + 1 > u) {
                  context.method_52706(class_1921::method_62277, field_45321, x, s, 9, 9);
               }
            }
         }

         class_10209.method_64146().method_15405("health");
         if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
            o -= 4;
         }

         this.method_37298(context, playerEntity, m, o, r, v, f, i, j, p, bl);
         class_1309 livingEntity = this.method_1734();
         int xx = this.method_1744(livingEntity);
         if (xx == 0) {
            class_10209.method_64146().method_15405("food");

            for (int y = 0; y < 10; y++) {
               int z = o;
               class_2960 identifier;
               class_2960 identifier2;
               class_2960 identifier3;
               if (playerEntity.method_6059(class_1294.field_5903)) {
                  identifier = field_45324;
                  identifier2 = field_45325;
                  identifier3 = field_45326;
               } else {
                  identifier = field_45327;
                  identifier2 = field_45328;
                  identifier3 = field_45298;
               }

               if (playerEntity.method_7344().method_7589() <= 0.0F && this.field_2042 % (k * 3 + 1) == 0) {
                  z = o + (this.field_2034.method_43048(3) - 1);
               }

               int aa = n - y * 8 - 9;
               context.method_52706(class_1921::method_62277, identifier, aa, z, 9, 9);
               if (y * 2 + 1 < k) {
                  context.method_52706(class_1921::method_62277, identifier3, aa, z, 9, 9);
               }

               if (y * 2 + 1 == k) {
                  context.method_52706(class_1921::method_62277, identifier2, aa, z, 9, 9);
               }
            }

            t -= 10;
         }

         class_10209.method_64146().method_15405("air");
         int y = playerEntity.method_5748();
         int zx = Math.min(playerEntity.method_5669(), y);
         if (playerEntity.method_5777(class_3486.field_15517) || zx < y) {
            int ab = this.method_1733(xx) - 1;
            t -= ab * 10;
            if (Southside.get().getModuleManager().get(HUD.class).hotbar.getValue()) {
               t -= 4;
            }

            int ac = class_3532.method_15384((zx - 2) * 10.0 / y);
            int ad = class_3532.method_15384(zx * 10.0 / y) - ac;

            for (int aax = 0; aax < ac + ad; aax++) {
               if (aax < ac) {
                  context.method_52706(class_1921::method_62277, field_45299, n - aax * 8 - 9, t, 9, 9);
               } else {
                  context.method_52706(class_1921::method_62277, field_54912, n - aax * 8 - 9, t, 9, 9);
               }
            }
         }

         class_10209.method_64146().method_15407();
      }
   }

   @Unique
   private void newHotbar(float tickDelta, class_332 context, class_9779 counter) {
      if (this.animation == null) {
         this.animation = new Animation(Easing.EASE_IN_SINE, 150L);
      }

      HUD hud = Southside.get().getModuleManager().get(HUD.class);
      class_1657 playerEntity = this.method_1737();
      if (playerEntity != null) {
         int centerX = context.method_51421() / 2;
         int hotbarBottom = context.method_51443() - 25;
         int slotSize = 22;
         int hotbarWidth = 9 * slotSize;
         int hotbarHeight = 22;
         float cornerRadius = 6.0F;
         int backgroundColor = new Color(34, 35, 38, 140).getRGB();
         NanoVGHelper.INSTANCE
            .setupAndDraw(
               true,
               vg -> NanoVGHelper.INSTANCE
                  .drawRoundedRect(vg, centerX - hotbarWidth / 2 - 1, hotbarBottom - 1, hotbarWidth + 2, hotbarHeight + 2, backgroundColor, cornerRadius)
            );
         int selectedSlot = playerEntity.method_31548().field_7545;
         int highlightX = centerX - hotbarWidth / 2 + selectedSlot * slotSize;
         this.animation.animate(highlightX);
         int highlightColor = new Color(255, 255, 255, 255).getRGB();
         NanoVGHelper.INSTANCE
            .setupAndDraw(
               true,
               vg -> NanoVGHelper.INSTANCE
                  .drawHollowRoundRect(
                     vg,
                     hud.hotbarAnimation.getValue() ? (float)this.animation.getValue() : highlightX,
                     hotbarBottom,
                     slotSize,
                     hotbarHeight,
                     highlightColor,
                     cornerRadius - 2.0F,
                     0.6F
                  )
            );

         for (int slot = 0; slot < 9; slot++) {
            int x = centerX - hotbarWidth / 2 + slot * slotSize + 3;
            int y = hotbarBottom + 3;
            class_1799 stack = (class_1799)playerEntity.method_31548().field_7547.get(slot);
            this.method_1762(context, x, y, counter, playerEntity, stack, slot + 1);
         }

         class_1799 offhandStack = playerEntity.method_6079();
         if (!offhandStack.method_7960()) {
            class_1306 mainArm = playerEntity.method_6068().method_5928();
            int offhandX = mainArm == class_1306.field_6182 ? centerX - hotbarWidth / 2 - 24 : centerX + hotbarWidth / 2 + 4;
            this.method_1762(context, offhandX, hotbarBottom + 3, counter, playerEntity, offhandStack, 10);
         }

         if (this.field_2035.field_1690.method_42565().method_41753() == class_4061.field_18153) {
            float cooldown = playerEntity.method_7261(0.0F);
            if (cooldown < 1.0F) {
               int indicatorY = hotbarBottom + 2;
               int indicatorX = playerEntity.method_6068() == class_1306.field_6183 ? centerX - hotbarWidth / 2 - 22 : centerX + hotbarWidth / 2 + 6;
               int progressHeight = (int)(cooldown * 19.0F);
               context.method_52706(class_1921::method_62277, field_45314, indicatorX, indicatorY, 18, 18);
               context.method_52708(
                  class_1921::method_62277, field_45315, 18, 18, 0, 18 - progressHeight, indicatorX, indicatorY + 18 - progressHeight, 18, progressHeight
               );
            }
         }
      }
   }

   @Inject(
      method = {"method_1765(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hookRenderEffects(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      NoRender noRender = Southside.get().getModuleManager().get(NoRender.class);
      if (noRender.isEnabled() && noRender.vanillaEffects.getValue()) {
         ci.cancel();
      }
   }
}
