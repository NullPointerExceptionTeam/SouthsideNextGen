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
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.util.math.MatrixStack;

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
         if (mc.world != null && mc.player != null) {
            if (this.warningTicks > 0) {
               this.warningTicks--;
            }

            if (this.tntWarning.getValue()) {
               double range = this.warningRange.getValue().doubleValue();
               double rangeSq = range * range;
               this.alertedTnts.removeIf(id -> mc.world.getEntityById(id) == null);

               for (Entity entity : mc.world.getEntities()) {
                  if (entity instanceof TntEntity && !this.alertedTnts.contains(entity.getId())) {
                     double distSq = mc.player.squaredDistanceTo(entity);
                     if (distSq <= rangeSq) {
                        this.alertedTnts.add(entity.getId());
                        this.warningTicks = 40;
                        if (this.warningSound.getValue()) {
                           mc.player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0F, 1.0F);
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
      if (mc.world != null && mc.player != null) {
         DrawContext context = event.getDrawContext();
         if (this.tntWarning.getValue() && this.warningTicks > 0) {
            this.drawWarningTitle(context);
         }

         if (this.tntCountdown.getValue()) {
            this.drawTntCountdowns(context, event.getRenderTick());
         }
      }
   }

   private void drawWarningTitle(DrawContext context) {
      TextRenderer textRenderer = mc.textRenderer;
      int screenWidth = mc.getWindow().getScaledWidth();
      int screenHeight = mc.getWindow().getScaledHeight();
      String warningText = "TNT is coming!!!";
      float titleScale = 4.0F;
      float alpha = this.warningTicks > 10 ? 1.0F : this.warningTicks / 10.0F;
      int alphaInt = (int)(alpha * 255.0F);
      int color = alphaInt << 24 | 16733525;
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      float centerX = screenWidth / 2.0F;
      float centerY = screenHeight / 4.0F;
      matrices.translate(centerX, centerY, 0.0F);
      matrices.scale(titleScale, titleScale, 1.0F);
      int textWidth = textRenderer.getWidth(warningText);
      int textX = -textWidth / 2;
      int textY = -9 / 2;
      context.drawText(textRenderer, Text.literal(warningText), textX + 1, textY + 1, alphaInt << 24 | 4134165, false);
      context.drawText(textRenderer, Text.literal(warningText), textX, textY, color, false);
      matrices.pop();
   }

   private void drawTntCountdowns(DrawContext context, float partialTicks) {
      float scaleValue = this.countdownScale.getValue().floatValue();

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof TntEntity tnt) {
            int fuse = tnt.getFuse();
            float seconds = fuse / 20.0F;
            Vec3d pos = tnt.getLerpedPos(partialTicks).add(0.0, tnt.getHeight() + 0.5, 0.0);
            Vec2f screenPos = ProjectionUtil.worldScreenPos(pos);
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

               TextRenderer textRenderer = mc.textRenderer;
               MatrixStack matrices = context.getMatrices();
               matrices.push();
               matrices.translate(screenPos.x, screenPos.y, 0.0F);
               matrices.scale(scaleValue, scaleValue, 1.0F);
               int textWidth = textRenderer.getWidth(countdownText);
               int textX = -textWidth / 2;
               int textY = -9;
               int bgLeft = textX - 3;
               int bgTop = textY - 2;
               int bgRight = textX + textWidth + 3;
               int bgBottom = textY + 9 + 2;
               context.fill(bgLeft, bgTop, bgRight, bgBottom, Integer.MIN_VALUE);
               context.drawText(textRenderer, Text.literal(countdownText), textX, textY, textColor, true);
               matrices.pop();
            }
         }
      }
   }
}
