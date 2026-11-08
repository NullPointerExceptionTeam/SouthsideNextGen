package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1541;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_4587;

public class TNTips extends Module {
   private final Switch tntWarning = new Switch("TNT Warning", true);
   private final Slider warningRange = new Slider("Warning Range", 30.0F, 1.0F, 10.0F, 1.0F);
   private final Switch warningSound = new Switch("Warning Sound", true);
   private final Switch tntCountdown = new Switch("TNT Countdown", true);
   private final Slider countdownScale = new Slider("Countdown Scale", 1.5F, 0.5F, 5.0F, 0.1F);
   private int warningTicks = 0;
   private static final int WARNING_DISPLAY_DURATION = 40;
   private final Set<Integer> alertedTnts = new HashSet<>();

   public TNTips() {
      super(Category.Render, "TNTips", "TNT warning & countdown");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addDependency(this.warningRange.getLabel(), this.tntWarning.getLabel());
      this.addDependency(this.warningSound.getLabel(), this.tntWarning.getLabel());
      this.addDependency(this.countdownScale.getLabel(), this.tntCountdown.getLabel());
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.warningTicks = 0;
      this.alertedTnts.clear();
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            if (this.warningTicks > 0) {
               this.warningTicks--;
            }

            if (this.tntWarning.getValue()) {
               double range = this.warningRange.getValue().doubleValue();
               double rangeSq = range * range;
               this.alertedTnts.removeIf(id -> mc.field_1687.method_8469(id) == null);

               for (class_1297 entity : mc.field_1687.method_18112()) {
                  if (entity instanceof class_1541 && !this.alertedTnts.contains(entity.method_5628())) {
                     double distSq = mc.field_1724.method_5858(entity);
                     if (distSq <= rangeSq) {
                        this.alertedTnts.add(entity.method_5628());
                        this.warningTicks = 40;
                        if (this.warningSound.getValue()) {
                           mc.field_1724.method_5783(class_3417.field_17265, 1.0F, 1.0F);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_332 context = event.getDrawContext();
         if (this.tntWarning.getValue() && this.warningTicks > 0) {
            this.drawWarningTitle(context);
         }

         if (this.tntCountdown.getValue()) {
            this.drawTntCountdowns(context, event.getRenderTick());
         }
      }
   }

   private void drawWarningTitle(class_332 context) {
      class_327 textRenderer = mc.field_1772;
      int screenWidth = mc.method_22683().method_4486();
      int screenHeight = mc.method_22683().method_4502();
      String warningText = "TNT is coming!!!";
      float titleScale = 4.0F;
      float alpha = this.warningTicks > 10 ? 1.0F : this.warningTicks / 10.0F;
      int alphaInt = (int)(alpha * 255.0F);
      int color = alphaInt << 24 | 16733525;
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      float centerX = screenWidth / 2.0F;
      float centerY = screenHeight / 4.0F;
      matrices.method_46416(centerX, centerY, 0.0F);
      matrices.method_22905(titleScale, titleScale, 1.0F);
      int textWidth = textRenderer.method_1727(warningText);
      int textX = -textWidth / 2;
      int textY = -9 / 2;
      context.method_51439(textRenderer, class_2561.method_43470(warningText), textX + 1, textY + 1, alphaInt << 24 | 4134165, false);
      context.method_51439(textRenderer, class_2561.method_43470(warningText), textX, textY, color, false);
      matrices.method_22909();
   }

   private void drawTntCountdowns(class_332 context, float partialTicks) {
      float scaleValue = this.countdownScale.getValue().floatValue();

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1541 tnt) {
            int fuse = tnt.method_6969();
            float seconds = fuse / 20.0F;
            class_243 pos = tnt.method_30950(partialTicks).method_1031(0.0, tnt.method_17682() + 0.5, 0.0);
            class_241 screenPos = ProjectionUtil.worldScreenPos(pos);
            if (screenPos != null) {
               String countdownText = String.format("%.1fs", seconds);
               int textColor;
               if (seconds > 2.0F) {
                  textColor = -11141291;
               } else if (seconds > 1.0F) {
                  textColor = -171;
               } else {
                  textColor = -43691;
               }

               class_327 textRenderer = mc.field_1772;
               class_4587 matrices = context.method_51448();
               matrices.method_22903();
               matrices.method_46416(screenPos.field_1343, screenPos.field_1342, 0.0F);
               matrices.method_22905(scaleValue, scaleValue, 1.0F);
               int textWidth = textRenderer.method_1727(countdownText);
               int textX = -textWidth / 2;
               int textY = -9;
               int bgLeft = textX - 3;
               int bgTop = textY - 2;
               int bgRight = textX + textWidth + 3;
               int bgBottom = textY + 9 + 2;
               context.method_25294(bgLeft, bgTop, bgRight, bgBottom, Integer.MIN_VALUE);
               context.method_51439(textRenderer, class_2561.method_43470(countdownText), textX, textY, textColor, true);
               matrices.method_22909();
            }
         }
      }
   }
}
