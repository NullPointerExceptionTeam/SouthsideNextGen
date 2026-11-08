package dev.southside.gui;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.MinecraftAccessor;
import dev.southside.compat.IASCompat;
import dev.southside.gui.netease.NeteaseScreen;
import java.awt.Color;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_4749;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_7919;

public class SouthsideMenuScreen extends class_437 {
   MenuButtonWidget.Position position;
   private float uiScale;
   private float paddingVerticalFactor = 0.3F;
   private float paddingHorizontalFactor = 0.3F;
   private float buttonGapFactor = 0.25F;

   public SouthsideMenuScreen setPaddingFactors(float vertical, float horizontal) {
      this.paddingVerticalFactor = vertical;
      this.paddingHorizontalFactor = horizontal;
      return this;
   }

   public SouthsideMenuScreen setButtonGapFactor(float gapFactor) {
      this.buttonGapFactor = gapFactor;
      return this;
   }

   private int scaled(int base) {
      return Math.round(base * this.uiScale);
   }

   private int scaledFont(int base) {
      return Math.max(12, Math.round(base * this.uiScale));
   }

   public SouthsideMenuScreen() {
      super(class_2561.method_43470("Southside Main Menu"));
   }

   public void method_25426() {
      this.position = MenuButtonWidget.Position.LEFT;
      this.uiScale = Math.max(0.6F, Math.min(this.field_22789 / 1920.0F, this.field_22790 / 1080.0F));
      int panelX = Math.round(this.field_22789 * 0.2F);
      int panelY = Math.round(this.field_22790 * 0.33F);
      int buttonBase = 40;
      int buttonHeight = this.scaled(buttonBase);
      int verticalGap = Math.max(this.scaled(8), Math.round(buttonHeight * this.buttonGapFactor));
      int spacingY = buttonHeight + verticalGap;
      int buttonWidth = this.scaled(200);
      this.initWidgetsScaled(panelX, panelY, buttonWidth, buttonHeight, spacingY);
   }

   private void initWidgetsScaled(int x, int y, int w, int h, int spacingY) {
      if (this.field_22787 != null) {
         this.method_37063(
            MenuButtonWidget.builder(class_2561.method_43470("Single Player"), button -> this.field_22787.method_1507(new class_526(this)), this.position)
               .dimensions(x, y, w, h)
               .build()
         );
         this.method_37063(MenuButtonWidget.builder(class_2561.method_43470("Multiplayer"), button -> {
            class_437 screen = (class_437)(this.field_22787.field_1690.field_21840 ? new class_500(this) : new class_4749(this));
            this.field_22787.method_1507(screen);
         }, this.position).dimensions(x, y + spacingY, w, h).build());
         boolean iasLoaded = FabricLoader.getInstance().isModLoaded("ias");
         MenuButtonWidget accountBtn = (MenuButtonWidget)this.method_37063(MenuButtonWidget.builder(class_2561.method_43470("Account Manager"), button -> {
            if (iasLoaded) {
               this.field_22787.method_1507(IASCompat.accountScreen(this));
            }
         }, this.position).dimensions(x, y + spacingY * 2, w, h).build());
         if (!iasLoaded) {
            accountBtn.method_47400(class_7919.method_47407(class_2561.method_43470("主播你不装IAS我怎么manage。")));
         }

         this.method_37063(
            MenuButtonWidget.builder(class_2561.method_43470("NEL"), button -> this.field_22787.method_1507(NeteaseScreen.create()), this.position)
               .dimensions(x, y + spacingY * 3, w, h)
               .build()
         );
         this.method_37063(
            MenuButtonWidget.builder(class_2561.method_43470("Exit"), button -> MinecraftAccessor.mc.method_1592(), this.position)
               .dimensions(x, y + spacingY * 4, w, h)
               .build()
         );
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.uiScale = Math.max(0.6F, Math.min(this.field_22789 / 1920.0F, this.field_22790 / 1080.0F));
      int panelX = Math.round(this.field_22789 * 0.2F);
      int panelY = Math.round(this.field_22790 * 0.33F);
      int buttonBase = 40;
      int buttonHeight = this.scaled(buttonBase);
      int verticalGap = Math.max(this.scaled(8), Math.round(buttonHeight * this.buttonGapFactor));
      int buttonWidth = this.scaled(200);
      int verticalPadding = Math.max(this.scaled(12), Math.round(buttonHeight * this.paddingVerticalFactor));
      int horizontalPadding = Math.max(this.scaled(12), Math.round(buttonHeight * this.paddingHorizontalFactor));
      int buttonCount = (int)this.field_33816.stream().filter(d -> d instanceof MenuButtonWidget).count();
      int containerHeight = (buttonCount > 0 ? buttonCount * buttonHeight + (buttonCount - 1) * verticalGap : 0) + verticalPadding * 2;
      int containerWidth = buttonWidth + horizontalPadding * 2;
      int logoSize = this.scaled(80);
      float logoX = (float)(this.field_22789 * 0.5555555555555556);
      float logoY = this.field_22790 / 2.0F - this.scaled(40);
      int titleFont = this.scaledFont(34);
      int subFont = this.scaledFont(22);
      int titleOffsetX = this.scaled(74);
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      nanovg.setupAndDraw(
         true,
         vg -> {
            nanovg.drawRect(vg, 0.0F, 0.0F, (float)this.field_22789, (float)this.field_22790, new Color(20, 25, 30).getRGB());
            nanovg.drawRoundedRect(
               vg, panelX - horizontalPadding, panelY - verticalPadding, containerWidth, containerHeight, new Color(35, 40, 45, 235).getRGB(), this.scaled(12)
            );
            nanovg.drawSvg(vg, SVGs.SOUTHSIDE, logoX, logoY, (float)logoSize, (float)logoSize, new Color(255, 255, 255, 190).getRGB(), (float)this.scaled(22));
            nanovg.drawText(
               vg,
               "Southside",
               logoX + titleOffsetX,
               this.field_22790 / 2.0F - this.scaled(12),
               new Color(255, 255, 255, 220).getRGB(),
               titleFont,
               Fonts.SEMIBOLD
            );
            nanovg.drawText(
               vg,
               "Next Generation",
               logoX + titleOffsetX,
               this.field_22790 / 2.0F - this.scaled(12) + nanovg.getTextHeight(vg, titleFont, Fonts.SEMIBOLD) * 0.9F,
               new Color(255, 255, 255, 140).getRGB(),
               subFont,
               Fonts.REGULAR
            );
         }
      );

      for (class_4068 drawable : this.field_33816) {
         drawable.method_25394(context, mouseX, mouseY, delta);
      }
   }

   public void method_25410(class_310 client, int width, int height) {
      super.method_25410(client, width, height);
      this.method_41843();
   }

   public void method_25419() {
      super.method_25419();
   }
}
