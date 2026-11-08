package dev.southside.render;

import dev.southside.MinecraftAccessor;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.nanovg.NanoFontLoader;
import dev.southside.nanovg.NanoFontRenderer;
import dev.southside.nanovg.NanoUtil;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.BezierUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.jetbrains.annotations.Nullable;

public class ProgressBarOverlay implements MinecraftAccessor {
   private static final float PANEL_WIDTH = 160.0F;
   private static final float PANEL_RADIUS = 8.0F;
   private static final float BAR_HEIGHT = 3.0F;
   private static final float BAR_RADIUS = 2.0F;
   private static final float PADDING = 8.0F;
   private static final float TITLE_FONT_SIZE = 10.0F;
   private static final float PERCENT_FONT_SIZE = 9.0F;
   private static final float ROW_INNER_HEIGHT = 16.0F;
   private static final float ROW_GAP = 4.0F;
   private static final float ROW_STRIDE = 20.0F;
   private static final Color PANEL_BG = new Color(10, 10, 10, 160);
   private static final Color BAR_TRACK = new Color(255, 255, 255, 20);
   private static final Color BAR_FILL_START = new Color(120, 180, 255, 220);
   private static final Color BAR_FILL_END = new Color(180, 120, 255, 220);
   private static final Color BLOOM_COLOR = new Color(140, 160, 255, 50);
   private static final Color TEXT_PRIMARY = new Color(240, 240, 255, 230);
   private static final Color TEXT_SECONDARY = new Color(160, 165, 180);
   private static final Color TEXT_COMPLETE = new Color(180, 255, 200);
   private static final int CLASSIC_MIN_BAR_WIDTH = 120;
   private static final int CLASSIC_BAR_HEIGHT = 2;
   private static final int CLASSIC_STACK_OFFSET = 20;
   private static final long COMPLETE_HOLD_MS = 1200L;
   private static final long FADE_IN_MS = 350L;
   private static final long FADE_OUT_MS = 500L;
   private final LinkedHashMap<Long, ProgressBarOverlay.ProgressEntry> entries = new LinkedHashMap<>();
   private long sessionCounter = 0L;
   private final BezierUtil animatedPanelHeight = new BezierUtil(6.0F, 0.0F);
   private final BezierUtil animatedAlpha = new BezierUtil(6.0F, 0.0F);
   private boolean wasRendering = false;

   public synchronized ProgressBarOverlay.ProgressHandle show(int totalSteps, @Nullable String title) {
      this.sessionCounter++;
      ProgressBarOverlay.ProgressEntry entry = new ProgressBarOverlay.ProgressEntry(this.sessionCounter, Math.max(1, totalSteps), title);
      this.entries.put(entry.sessionId, entry);
      return new ProgressBarOverlay.ProgressHandle(entry.sessionId);
   }

   public ProgressBarOverlay.ProgressHandle show(int totalSteps) {
      return this.show(totalSteps, null);
   }

   public synchronized void cancelActive() {
      this.entries.clear();
   }

   public synchronized boolean isRendering() {
      this.ensureAutoHide();
      return !this.entries.isEmpty();
   }

   private synchronized void setStep(long sessionId, int step) {
      ProgressBarOverlay.ProgressEntry entry = this.entries.get(sessionId);
      if (entry != null) {
         entry.currentStep = class_3532.method_15340(step, 0, entry.totalSteps);
         if (entry.currentStep >= entry.totalSteps) {
            entry.completionTimestamp = System.currentTimeMillis();
         } else {
            entry.completionTimestamp = -1L;
         }
      }
   }

   private synchronized void advance(long sessionId, int amount) {
      ProgressBarOverlay.ProgressEntry entry = this.entries.get(sessionId);
      if (entry != null) {
         this.setStep(sessionId, entry.currentStep + amount);
      }
   }

   private synchronized void finish(long sessionId) {
      ProgressBarOverlay.ProgressEntry entry = this.entries.get(sessionId);
      if (entry != null) {
         entry.currentStep = entry.totalSteps;
         entry.completionTimestamp = System.currentTimeMillis();
      }
   }

   private synchronized void cancel(long sessionId) {
      this.entries.remove(sessionId);
   }

   private synchronized boolean isSessionActive(long sessionId) {
      this.ensureAutoHide();
      return this.entries.containsKey(sessionId);
   }

   private synchronized List<ProgressBarOverlay.ProgressSnapshot> snapshots() {
      this.ensureAutoHide();
      List<ProgressBarOverlay.ProgressSnapshot> list = new ArrayList<>();
      int index = 0;
      long now = System.currentTimeMillis();

      for (ProgressBarOverlay.ProgressEntry entry : this.entries.values()) {
         float entryAlpha = 1.0F;
         long age = now - entry.creationTimestamp;
         if (age < 350L) {
            entryAlpha = class_3532.method_15363((float)age / 350.0F, 0.0F, 1.0F);
         }

         if (entry.completionTimestamp > 0L) {
            long fadeAge = now - entry.completionTimestamp;
            long fadeStart = 700L;
            if (fadeAge > fadeStart) {
               entryAlpha = class_3532.method_15363(1.0F - (float)(fadeAge - fadeStart) / 500.0F, 0.0F, 1.0F);
            }
         }

         float targetRatio = entry.totalSteps <= 0 ? 0.0F : (float)entry.currentStep / entry.totalSteps;
         targetRatio = class_3532.method_15363(targetRatio, 0.0F, 1.0F);
         entry.animatedRatio.update(targetRatio);
         list.add(
            new ProgressBarOverlay.ProgressSnapshot(
               entry.sessionId, entry.totalSteps, entry.currentStep, entry.title, entry.titleText, entry.hasTitle, index, entryAlpha, entry.animatedRatio.get()
            )
         );
         index++;
      }

      return list;
   }

   private synchronized void ensureAutoHide() {
      if (!this.entries.isEmpty()) {
         long now = System.currentTimeMillis();
         this.entries.values().removeIf(e -> e.completionTimestamp > 0L && now - e.completionTimestamp >= 1200L);
      }
   }

   @EventHandler
   private void onRender(Render2DEvent event) {
      List<ProgressBarOverlay.ProgressSnapshot> snapshots = this.snapshots();
      boolean rendering = !snapshots.isEmpty();
      if (rendering && !this.wasRendering) {
         this.animatedAlpha.update(1.0F);
      } else if (!rendering && this.wasRendering) {
         this.animatedAlpha.update(0.0F);
      }

      this.wasRendering = rendering;
      float globalAlpha = this.animatedAlpha.get();
      if (!(globalAlpha <= 0.01F) || rendering) {
         if (ClientSettings.progressBarStyle.isMode("Classic")) {
            this.renderClassic(event.getDrawContext(), snapshots, globalAlpha);
         } else {
            this.renderModern(snapshots, globalAlpha);
         }
      }
   }

   private static Color withAlpha(Color c, float alpha) {
      int a = class_3532.method_15340((int)(c.getAlpha() * alpha), 0, 255);
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
   }

   private static int withAlpha(int argb, float alpha) {
      int a = class_3532.method_15340((int)((argb >> 24 & 0xFF) * alpha), 0, 255);
      return a << 24 | argb & 16777215;
   }

   private void renderModern(List<ProgressBarOverlay.ProgressSnapshot> snapshots, float globalAlpha) {
      int screenWidth = mc.method_22683().method_4486();
      int screenHeight = mc.method_22683().method_4502();
      int count = snapshots.size();
      float scale = ClientSettings.progressBarScale.getValue().floatValue();
      float sPADDING = 8.0F * scale;
      float sROW_INNER_HEIGHT = 16.0F * scale;
      float sROW_GAP = 4.0F * scale;
      float sROW_STRIDE = sROW_INNER_HEIGHT + sROW_GAP;
      float sPANEL_WIDTH = 160.0F * scale;
      float sPANEL_RADIUS = 8.0F * scale;
      float sBAR_HEIGHT = 3.0F * scale;
      float sBAR_RADIUS = 2.0F * scale;
      float sTITLE_FONT_SIZE = 10.0F * scale;
      float sPERCENT_FONT_SIZE = 9.0F * scale;
      float targetHeight = count > 0 ? sPADDING + count * sROW_INNER_HEIGHT + (count - 1) * sROW_GAP + sPADDING : 0.0F;
      this.animatedPanelHeight.update(targetHeight);
      float panelHeight = this.animatedPanelHeight.get();
      if (!(panelHeight < 1.0F)) {
         float panelX = (screenWidth - sPANEL_WIDTH) / 2.0F;
         float yOffset = ClientSettings.progressBarOffset.getValue().floatValue();
         float panelY = screenHeight / 2.0F + yOffset;
         NanoUtil.beginFrame();
         float slideOffset = (1.0F - globalAlpha) * 6.0F * scale;
         panelY += slideOffset;
         NanoUtil.drawShadowRound(panelX, panelY, sPANEL_WIDTH, panelHeight, 12.0F * scale, (int)(50.0F * globalAlpha), sPANEL_RADIUS);
         NanoUtil.drawRoundedRect(panelX, panelY, sPANEL_WIDTH, panelHeight, sPANEL_RADIUS, withAlpha(PANEL_BG, globalAlpha));

         for (ProgressBarOverlay.ProgressSnapshot snap : snapshots) {
            float rowAlpha = globalAlpha * snap.entryAlpha;
            if (!(rowAlpha <= 0.01F)) {
               float rowY = panelY + sPADDING + snap.index * sROW_STRIDE;
               this.drawModernRow(
                  snap,
                  panelX,
                  rowY,
                  rowAlpha,
                  scale,
                  sPADDING,
                  sPANEL_WIDTH,
                  sROW_INNER_HEIGHT,
                  sBAR_HEIGHT,
                  sBAR_RADIUS,
                  sTITLE_FONT_SIZE,
                  sPERCENT_FONT_SIZE
               );
            }
         }

         NanoUtil.endFrame();
      }
   }

   private void drawModernRow(
      ProgressBarOverlay.ProgressSnapshot snap,
      float panelX,
      float rowY,
      float alpha,
      float scale,
      float sPADDING,
      float sPANEL_WIDTH,
      float sROW_INNER_HEIGHT,
      float sBAR_HEIGHT,
      float sBAR_RADIUS,
      float sTITLE_FONT_SIZE,
      float sPERCENT_FONT_SIZE
   ) {
      NanoFontRenderer font = NanoFontLoader.harmony;
      float ratio = snap.animatedRatio;
      int percent = (int)(ratio * 100.0F);
      float contentX = panelX + sPADDING;
      float contentWidth = sPANEL_WIDTH - sPADDING * 2.0F;
      if (snap.hasTitle) {
         font.drawString(snap.title, contentX, rowY, sTITLE_FONT_SIZE, withAlpha(TEXT_PRIMARY, alpha), true);
      }

      String percentText = percent + "%";
      float percentWidth = font.getStringWidth(percentText, sPERCENT_FONT_SIZE);
      Color percentColor = percent >= 100 ? withAlpha(TEXT_COMPLETE, alpha) : withAlpha(TEXT_SECONDARY, alpha);
      font.drawString(percentText, contentX + contentWidth - percentWidth, rowY + 0.5F * scale, sPERCENT_FONT_SIZE, percentColor);
      float barY = rowY + sROW_INNER_HEIGHT - sBAR_HEIGHT;
      NanoUtil.drawRoundedRect(contentX, barY, contentWidth, sBAR_HEIGHT, sBAR_RADIUS, withAlpha(BAR_TRACK, alpha));
      float filledWidth = contentWidth * ratio;
      if (filledWidth > 0.5F) {
         NanoUtil.drawShadowRound(contentX, barY - 2.0F * scale, filledWidth, sBAR_HEIGHT + 4.0F * scale, 6.0F * scale, (int)(30.0F * alpha), sBAR_RADIUS);
         NanoUtil.drawGradientRoundedRect(
            contentX, barY, filledWidth, sBAR_HEIGHT, sBAR_RADIUS, withAlpha(BAR_FILL_START, alpha), withAlpha(BAR_FILL_END, alpha)
         );
      }
   }

   private void renderClassic(class_332 context, List<ProgressBarOverlay.ProgressSnapshot> snapshots, float globalAlpha) {
      class_327 textRenderer = mc.field_1772;
      int screenWidth = mc.method_22683().method_4486();
      int screenHeight = mc.method_22683().method_4502();

      for (ProgressBarOverlay.ProgressSnapshot snap : snapshots) {
         float alpha = globalAlpha * snap.entryAlpha;
         if (!(alpha <= 0.01F)) {
            int contentWidth = Math.max(120, snap.hasTitle ? textRenderer.method_27525(snap.titleText) : 0);
            int barX = (screenWidth - contentWidth) / 2;
            int barY = screenHeight / 2 + ClientSettings.progressBarOffset.getValue().intValue() + snap.index * 20;
            float ratio = snap.animatedRatio;
            String percentText = (int)(ratio * 100.0F) + "%";
            int percentWidth = textRenderer.method_1727(percentText);
            int textY = barY - 12;
            if (snap.hasTitle) {
               context.method_51439(textRenderer, snap.titleText, barX, textY, withAlpha(-1, alpha), false);
            }

            context.method_51439(
               textRenderer, class_2561.method_43470(percentText), barX + contentWidth - percentWidth, textY, withAlpha(-3355444, alpha), false
            );
            this.drawClassicBar(context, ratio, barX, barY, contentWidth, alpha);
         }
      }
   }

   private void drawClassicBar(class_332 context, float ratio, int x, int y, int width, float alpha) {
      int filledWidth = (int)Math.ceil(width * ratio);
      int borderColor = withAlpha(1627389951, alpha);
      int progressColor = withAlpha(-11552513, alpha);
      context.method_25294(x - 1, y - 1, x + width + 1, y, borderColor);
      context.method_25294(x - 1, y + 2, x + width + 1, y + 2 + 1, borderColor);
      context.method_25294(x - 1, y, x, y + 2, borderColor);
      context.method_25294(x + width, y, x + width + 1, y + 2, borderColor);
      if (filledWidth > 0) {
         context.method_25294(x, y, x + filledWidth, y + 2, progressColor);
      }
   }

   private static final class ProgressEntry {
      final long sessionId;
      final int totalSteps;
      int currentStep;
      final String title;
      final class_2561 titleText;
      final boolean hasTitle;
      final long creationTimestamp;
      long completionTimestamp;
      final BezierUtil animatedRatio = new BezierUtil(8.0F, 0.0F);

      ProgressEntry(long sessionId, int totalSteps, @Nullable String title) {
         this.sessionId = sessionId;
         this.totalSteps = totalSteps;
         this.currentStep = 0;
         this.creationTimestamp = System.currentTimeMillis();
         this.completionTimestamp = -1L;
         if (title != null && !title.isBlank()) {
            this.title = title.trim();
            this.titleText = class_2561.method_43470(this.title);
            this.hasTitle = true;
         } else {
            this.title = "";
            this.titleText = class_2561.method_43473();
            this.hasTitle = false;
         }
      }
   }

   public final class ProgressHandle {
      private final long sessionId;

      private ProgressHandle(long sessionId) {
         this.sessionId = sessionId;
      }

      public void update(int step) {
         ProgressBarOverlay.this.setStep(this.sessionId, step);
      }

      public void advance() {
         this.advance(1);
      }

      public void advance(int amount) {
         ProgressBarOverlay.this.advance(this.sessionId, amount);
      }

      public void finish() {
         ProgressBarOverlay.this.finish(this.sessionId);
      }

      public void cancel() {
         ProgressBarOverlay.this.cancel(this.sessionId);
      }

      public boolean isActive() {
         return ProgressBarOverlay.this.isSessionActive(this.sessionId);
      }
   }

   private record ProgressSnapshot(
      long sessionId, int totalSteps, int currentStep, String title, class_2561 titleText, boolean hasTitle, int index, float entryAlpha, float animatedRatio
   ) {
   }
}
