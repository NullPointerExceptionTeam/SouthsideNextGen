package dev.southside.gui;

import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.MinecraftAccessor;
import dev.southside.compat.IASCompat;
import dev.southside.gui.netease.NeteaseScreen;
import java.awt.Color;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;

public class SouthsideMenuScreen extends Screen {
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
      super(Text.literal("Southside Main Menu"));
   }

   public void init() {
      this.position = MenuButtonWidget.Position.LEFT;
      this.uiScale = Math.max(0.6F, Math.min(this.width / 1920.0F, this.height / 1080.0F));
      int panelX = Math.round(this.width * 0.2F);
      int panelY = Math.round(this.height * 0.33F);
      int buttonBase = 40;
      int buttonHeight = this.scaled(buttonBase);
      int verticalGap = Math.max(this.scaled(8), Math.round(buttonHeight * this.buttonGapFactor));
      int spacingY = buttonHeight + verticalGap;
      int buttonWidth = this.scaled(200);
      this.initWidgetsScaled(panelX, panelY, buttonWidth, buttonHeight, spacingY);
   }

   private void initWidgetsScaled(int x, int y, int w, int h, int spacingY) {
      if (this.client != null) {
         this.addDrawableChild(
            MenuButtonWidget.builder(Text.literal("Single Player"), button -> this.client.setScreen(new SelectWorldScreen(this)), this.position)
               .dimensions(x, y, w, h)
               .build()
         );
         this.addDrawableChild(MenuButtonWidget.builder(Text.literal("Multiplayer"), button -> {
            Screen screen = (Screen)(this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this));
            this.client.setScreen(screen);
         }, this.position).dimensions(x, y + spacingY, w, h).build());
         boolean iasLoaded = FabricLoader.getInstance().isModLoaded("ias");
         MenuButtonWidget accountBtn = (MenuButtonWidget)this.addDrawableChild(MenuButtonWidget.builder(Text.literal("Account Manager"), button -> {
            if (iasLoaded) {
               this.client.setScreen(IASCompat.accountScreen(this));
            }
         }, this.position).dimensions(x, y + spacingY * 2, w, h).build());
         if (!iasLoaded) {
            accountBtn.setTooltip(Tooltip.of(Text.literal("主播你不装IAS我怎么manage。")));
         }

         this.addDrawableChild(
            MenuButtonWidget.builder(Text.literal("NEL"), button -> this.client.setScreen(NeteaseScreen.create()), this.position)
               .dimensions(x, y + spacingY * 3, w, h)
               .build()
         );
         this.addDrawableChild(
            MenuButtonWidget.builder(Text.literal("Exit"), button -> MinecraftAccessor.mc.scheduleStop(), this.position)
               .dimensions(x, y + spacingY * 4, w, h)
               .build()
         );
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.uiScale = Math.max(0.6F, Math.min(this.width / 1920.0F, this.height / 1080.0F));
      int panelX = Math.round(this.width * 0.2F);
      int panelY = Math.round(this.height * 0.33F);
      int buttonBase = 40;
      int buttonHeight = this.scaled(buttonBase);
      int verticalGap = Math.max(this.scaled(8), Math.round(buttonHeight * this.buttonGapFactor));
      int buttonWidth = this.scaled(200);
      int verticalPadding = Math.max(this.scaled(12), Math.round(buttonHeight * this.paddingVerticalFactor));
      int horizontalPadding = Math.max(this.scaled(12), Math.round(buttonHeight * this.paddingHorizontalFactor));
      int buttonCount = (int)this.drawables.stream().filter(d -> d instanceof MenuButtonWidget).count();
      int containerHeight = (buttonCount > 0 ? buttonCount * buttonHeight + (buttonCount - 1) * verticalGap : 0) + verticalPadding * 2;
      int containerWidth = buttonWidth + horizontalPadding * 2;
      int logoSize = this.scaled(80);
      float logoX = (float)(this.width * 0.5555555555555556);
      float logoY = this.height / 2.0F - this.scaled(40);
      int titleFont = this.scaledFont(34);
      int subFont = this.scaledFont(22);
      int titleOffsetX = this.scaled(74);
      NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
      nanovg.setupAndDraw(
         true,
         vg -> {
            nanovg.drawRect(vg, 0.0F, 0.0F, (float)this.width, (float)this.height, new Color(20, 25, 30).getRGB());
            nanovg.drawRoundedRect(
               vg, panelX - horizontalPadding, panelY - verticalPadding, containerWidth, containerHeight, new Color(35, 40, 45, 235).getRGB(), this.scaled(12)
            );
            nanovg.drawSvg(vg, SVGs.SOUTHSIDE, logoX, logoY, (float)logoSize, (float)logoSize, new Color(255, 255, 255, 190).getRGB(), (float)this.scaled(22));
            nanovg.drawText(
               vg,
               "Southside",
               logoX + titleOffsetX,
               this.height / 2.0F - this.scaled(12),
               new Color(255, 255, 255, 220).getRGB(),
               titleFont,
               Fonts.SEMIBOLD
            );
            nanovg.drawText(
               vg,
               "Next Generation",
               logoX + titleOffsetX,
               this.height / 2.0F - this.scaled(12) + nanovg.getTextHeight(vg, titleFont, Fonts.SEMIBOLD) * 0.9F,
               new Color(255, 255, 255, 140).getRGB(),
               subFont,
               Fonts.REGULAR
            );
         }
      );

      for (Drawable drawable : this.drawables) {
         drawable.render(context, mouseX, mouseY, delta);
      }
   }

   public void resize(MinecraftClient client, int width, int height) {
      super.resize(client, width, height);
      this.clearAndInit();
   }

   public void close() {
      super.close();
   }
}
