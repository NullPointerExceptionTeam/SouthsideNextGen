package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.Multithreading;
import dev.southside.Southside;
import dev.southside.module.impl.client.Theme;
import dev.southside.module.impl.client.Watermark;
import dev.southside.nanovg.NanoFontLoader;
import dev.southside.nanovg.NanoUtil;
import dev.southside.utils.network.Http;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.gui.DrawContext;

public class WatermarkHud extends Hud {
   public final Dropdown subtitleContent = new Dropdown(
      "Subtitle Content", new String[]{"None", "FPS", "Player Role name", "Playtime", "Ping", "Hitokoto", "Auto Switch"}, "Choose the watermark subtitle", 6, 1
   );
   public Switch glow = new Switch("Background Glow", false);
   private float targetWidth = 0.0F;
   private float currentWidth = 0.0F;
   private float lastTextWidth = 0.0F;
   private float lastTextHeight = 0.0F;
   private static final int AUTO_SWITCH_OPTION_INDEX = 6;
   private static final int AUTO_SWITCH_MIN_MODE = 1;
   private static final int AUTO_SWITCH_MAX_MODE = 5;
   private static final String HITOKOTO_API_URL = "https://v1.hitokoto.cn/?encode=text&charset=utf-8";
   private static final long HITOKOTO_REFRESH_INTERVAL = 20000L;
   private static final String HITOKOTO_FALLBACK_TEXT = "曹家豪130185200910260034";
   private int autoSwitchMode = 1;
   private long lastSwitchTime = 0L;
   private static final long SWITCH_INTERVAL = 3000L;
   private static final long FADE_DURATION = 300L;
   private float textAlpha = 1.0F;
   private String nextSubtitle = "";
   private boolean isFading = false;
   private final long startTime = System.currentTimeMillis();
   private volatile String hitokotoSubtitle = "曹家豪130185200910260034";
   private volatile long lastHitokotoFetch = 0L;
   private final AtomicBoolean hitokotoFetching = new AtomicBoolean(false);
   private Watermark module = null;

   private float easeInOutCubic(float t) {
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
   }

   private float leap(float a, float b, float t) {
      return a + (b - a) * t;
   }

   private String getSubtitleByMode(int mode) {
      switch (mode) {
         case 0:
         default:
            return "";
         case 1:
            int fps = mc.getCurrentFps();
            return fps + " FPS";
         case 2:
            return mc.player != null ? mc.player.getName().getString() : "Player";
         case 3:
            long playTime = (System.currentTimeMillis() - this.startTime) / 1000L;
            long hours = playTime / 3600L;
            long minutes = playTime % 3600L / 60L;
            long seconds = playTime % 60L;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
         case 4:
            return mc.player != null ? mc.player.getPlayerListEntry().getLatency() + " ms" : "Ping";
         case 5:
         case 6:
            return this.getHitokotoSubtitle();
      }
   }

   private String getHitokotoSubtitle() {
      this.refreshHitokotoIfNeeded();
      return this.hitokotoSubtitle != null && !this.hitokotoSubtitle.isBlank() ? this.hitokotoSubtitle : "曹家豪130185200910260034";
   }

   private void refreshHitokotoIfNeeded() {
      long now = System.currentTimeMillis();
      boolean requiresRefresh = now - this.lastHitokotoFetch >= 20000L || "曹家豪130185200910260034".equals(this.hitokotoSubtitle);
      if (requiresRefresh && this.hitokotoFetching.compareAndSet(false, true)) {
         Multithreading.runAsync(() -> {
            try {
               String response = Http.get("https://v1.hitokoto.cn/?encode=text&charset=utf-8").acceptAll().sendString();
               if (response != null) {
                  response = response.trim();
               }

               if (response != null && !response.isEmpty()) {
                  this.hitokotoSubtitle = response;
               } else {
                  this.hitokotoSubtitle = "曹家豪130185200910260034";
               }

               this.lastHitokotoFetch = System.currentTimeMillis();
            } catch (Exception var5) {
               this.hitokotoSubtitle = "曹家豪130185200910260034";
               this.lastHitokotoFetch = System.currentTimeMillis();
            } finally {
               this.hitokotoFetching.set(false);
            }
         });
      }
   }

   private String getCurrentSubtitle() {
      if (this.subtitleContent.getValue() == 6) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastSwitchTime > 3000L && !this.isFading) {
            this.nextSubtitle = this.getSubtitleByMode(this.autoSwitchMode);
            this.autoSwitchMode = this.autoSwitchMode >= 5 ? 1 : this.autoSwitchMode + 1;
            String newSubtitle = this.getSubtitleByMode(this.autoSwitchMode);
            if (!this.nextSubtitle.equals(newSubtitle)) {
               this.isFading = true;
               this.lastSwitchTime = currentTime;
            }
         }

         String currentSubtitle = "";
         if (this.isFading) {
            long fadeTime = currentTime - this.lastSwitchTime;
            if (fadeTime < 300L) {
               this.textAlpha = 1.0F - this.easeInOutCubic((float)fadeTime / 300.0F);
               currentSubtitle = this.nextSubtitle;
            } else if (fadeTime < 600L) {
               this.textAlpha = this.easeInOutCubic((float)(fadeTime - 300L) / 300.0F);
               currentSubtitle = this.getSubtitleByMode(this.autoSwitchMode);
            } else {
               this.isFading = false;
               this.textAlpha = 1.0F;
               currentSubtitle = this.getSubtitleByMode(this.autoSwitchMode);
               this.lastSwitchTime = currentTime;
            }
         } else {
            currentSubtitle = this.getSubtitleByMode(this.autoSwitchMode);
         }

         return currentSubtitle;
      } else {
         this.textAlpha = 1.0F;
         return this.getSubtitleByMode(this.subtitleContent.getValue());
      }
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (this.module == null) {
         this.module = Southside.INSTANCE.getModuleManager().get(Watermark.class);
      }

      if (mc.getNetworkHandler() != null) {
         NanoVGHelper nanovg = NanoVGHelper.INSTANCE;
         nanovg.setupAndDraw(
            true,
            vg -> {
               String var8 = this.module.getMode().getMode();
               switch (var8) {
                  case "Southside":
                     String clientNamex = this.module.getClientName();
                     String subtitle = this.getCurrentSubtitle();
                     String fullText = subtitle.isEmpty() ? clientNamex + " NextGen" : clientNamex + " NextGen · " + subtitle;
                     SVG logo = SVGs.SOUTHSIDE;
                     this.targetWidth = nanovg.getTextWidth(vg, fullText, 10.0F, Fonts.SEMIBOLD) + 6.0F + 22.0F;
                     this.lastTextHeight = nanovg.getTextHeight(vg, 10.0F, Fonts.SEMIBOLD) * 2.0F;
                     float smoothing = 0.15F;
                     this.currentWidth = this.leap(this.currentWidth, this.targetWidth, smoothing);
                     float rectWidth = this.getWidth(scale, example);
                     float rectHeight = this.getHeight(scale, example);
                     nanovg.drawRoundedRect(vg, x, y, rectWidth, rectHeight, new Color(129, 129, 129, 180).getRGB(), 5.0F * scale);
                     if (this.glow.getValue()) {
                        nanovg.drawDropShadow(vg, x, y, rectWidth, rectHeight, 10.0F * scale, 0.0F, 5.0F * scale, new Color(167, 166, 166, 180));
                     }

                     nanovg.drawSvg(vg, logo, x + 5.0F * scale, y + 2.0F * scale, 15.0F * scale, 15.0F * scale, Color.WHITE.getRGB(), 3.0F);
                     int textColor = new Color(255, 255, 255, (int)(255.0F * this.textAlpha)).getRGB();
                     nanovg.drawText(vg, fullText, x + 22.0F * scale, y + 10.5F * scale, textColor, 10.0F * scale, Fonts.SEMIBOLD);
                     break;
                  case "SigmaJello":
                     AssetHelper assetHelper = AssetHelper.INSTANCE;
                     AtomicBoolean loaded = new AtomicBoolean(false);
                     String watermarkTexture = "/assets/southside/textures/gui/hud/watermark/watermark.png";
                     float watermarkScale = 1.0F;
                     nanovg.setupAndDraw(vg1 -> loaded.set(assetHelper.loadImage(vg1, watermarkTexture, Watermark.class)));
                     if (!loaded.get()) {
                        return;
                     }

                     nanovg.drawImage(vg, watermarkTexture, 0.0F, 0.0F, 170.0F * watermarkScale, 104.0F * watermarkScale, Watermark.class);
                     break;
                  case "Retro": {
                     NanoVGHelper nvg = NanoVGHelper.INSTANCE;
                     String clientName = this.module.getClientName();
                     float textWidth = nvg.getTextWidth(vg, clientName, 25.0F * scale, Fonts.SEMIBOLD);
                     float width = Math.max(240.0F * scale, 89.0F * scale + textWidth);
                     float height = 75.0F * scale;
                     this.lastTextWidth = width / scale;
                     this.lastTextHeight = height / scale;
                     float cornerRadius = 12.0F * scale;
                     float leftWidth = 65.0F * scale;
                     SVG logoSvg = SVGs.SOUTHSIDE;
                     nvg.drawRoundedRectVaried(
                        vg, x + leftWidth, y, width - leftWidth, height, new Color(0, 0, 0, 127).getRGB(), 0.0F, cornerRadius, cornerRadius, 0.0F
                     );
                     nvg.drawRoundedRectVaried(vg, x, y, leftWidth, height, Color.WHITE.getRGB(), cornerRadius, 0.0F, 0.0F, cornerRadius);
                     nvg.drawText(vg, clientName, x + leftWidth + 12.0F * scale, y + 31.0F * scale, Color.WHITE.getRGB(), 25.0F * scale, Fonts.SEMIBOLD);
                     nvg.drawText(
                        vg,
                        "Next Generation",
                        x + leftWidth + 12.0F * scale,
                        y + 53.0F * scale,
                        new Color(200, 200, 200).getRGB(),
                        18.0F * scale,
                        Fonts.REGULAR
                     );
                     if (logoSvg != null) {
                        float iconSize = 50.0F * scale;
                        float iconX = x + (leftWidth - iconSize) / 2.0F;
                        float iconY = y + (height - iconSize) / 2.0F;
                        nvg.drawSvg(vg, logoSvg, iconX, iconY, iconSize, iconSize, new Color(0, 0, 0).getRGB(), 30.0F);
                     }
                     break;
                  }
                  default: {
                     String text = this.module.getClientName() + "   ";
                     String subtext = "nextgen";
                     float mainSize = 28.0F * scale;
                     float subSize = 20.0F * scale;
                     float width = NanoFontLoader.harmony.getStringWidth(text, mainSize) + NanoFontLoader.harmony.getStringWidth(subtext, subSize);
                     float mainWidth = NanoFontLoader.harmony.getStringWidth(text, mainSize);
                     float interval = 8.0F * scale;
                     NanoUtil.drawRoundedRect(
                        x, y, interval * 2.0F + width, NanoFontLoader.harmony.getHeight(mainSize) + interval * 2.0F, 8.0F * scale, new Color(0, 0, 0, 100)
                     );
                     NanoUtil.drawShadowRound(
                        x, y, interval * 2.0F + width, NanoFontLoader.harmony.getHeight(mainSize) + interval * 2.0F, 10.0F, 80, 8.0F * scale
                     );
                     NanoFontLoader.harmony.drawString(text, x + interval, y + interval, mainSize, Theme.getColor(0));
                     NanoFontLoader.harmony
                        .drawString(
                           subtext,
                           x + interval + mainWidth,
                           y + NanoFontLoader.harmony.getHeight(mainSize) / 2.0F + interval - NanoFontLoader.harmony.getHeight(subSize) / 2.0F,
                           subSize,
                           Color.WHITE,
                           true
                        );
                     this.lastTextWidth = interval * 2.0F + width;
                     this.lastTextHeight = NanoFontLoader.harmony.getHeight(mainSize) + interval * 2.0F;
                  }
               }
            }
         );
      }
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      if (this.module != null) {
         switch (this.module.getMode().getMode()) {
            case "Old":
               return 350.0F * scale + 16.0F * scale;
            case "Southside":
               float width = this.currentWidth > 0.0F ? this.currentWidth : this.targetWidth;
               if (width <= 0.0F) {
                  width = 200.0F;
               }

               return width * scale;
            case "Retro":
               return Math.max(240.0F, this.lastTextWidth) * scale;
            case null:
            default:
               break;
         }
      }

      return 200.0F * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      if (this.module != null) {
         switch (this.module.getMode().getMode()) {
            case "Old":
               return 28.0F * scale + 16.0F * scale;
            case "SigmaJello":
               return 104.0F * scale;
            case "Southside":
               float height = this.lastTextHeight > 0.0F ? this.lastTextHeight : 50.0F;
               return height * scale;
            case "Retro":
               return 75.0F * scale;
            case null:
            default:
               return this.lastTextHeight * scale;
         }
      } else {
         return 50.0F * scale;
      }
   }
}
