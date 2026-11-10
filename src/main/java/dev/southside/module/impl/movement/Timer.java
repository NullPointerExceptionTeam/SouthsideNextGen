package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.utils.MinecraftTickTimerUtils;
import dev.southside.utils.misc.ClientUtils;
import org.lwjgl.glfw.GLFW;

public class Timer extends Module {
   public Slider timerSpeed = new Slider("Timer Speed", 1.6F, 1.0F, 5.0F, 0.1F);
   public Slider ticks = new Slider("Ticks", 8, 1, 20, 1);
   public Switch showText = new Switch("ProgressBar", true);
   public Switch autoHide = new Switch("Auto Hide", false);
   private float balance = 0.0F;
   private ProgressBarOverlay.ProgressHandle progressHandle;

   public Timer() {
      super(Category.Movement, "Timer", "Allows you to speed up or slow down time");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.autoHide.getLabel(), () -> !this.showText.getValue());
   }

   @Override
   public void onDisable() {
      this.balance = 0.0F;
      MinecraftTickTimerUtils.setMultiplier(1.0F);
      this.cancelProgressBar();
      super.onDisable();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (ClientUtils.nullCheck()) {
         this.balance = 0.0F;
         MinecraftTickTimerUtils.setMultiplier(1.0F);
      } else {
         if (event.getState() == Stage.PRE) {
            boolean isMoving = mc.options.forwardKey.isPressed()
               || mc.options.backKey.isPressed()
               || mc.options.leftKey.isPressed()
               || mc.options.rightKey.isPressed();
            boolean onGround = mc.player.isOnGround();
            if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 3) == 1 && this.balance > 0.1F) {
               float timer = this.timerSpeed.getValue().floatValue();
               if (this.balance >= timer - 1.0F) {
                  MinecraftTickTimerUtils.setMultiplier(timer);
                  this.balance -= timer - 1.0F;
               } else {
                  MinecraftTickTimerUtils.setMultiplier(1.0F);
               }
            } else if (!(this.balance < this.ticks.getValue().doubleValue())) {
               MinecraftTickTimerUtils.setMultiplier(1.0F);
            } else {
               float timer = !isMoving && onGround ? 0.8F : 0.96F;
               MinecraftTickTimerUtils.setMultiplier(timer);
               this.balance += 1.0F - timer;
            }

            this.balance = (float)Math.max(0.0, Math.min((double)this.balance, this.ticks.getValue().doubleValue()));
         }

         this.updateProgressBar();
      }
   }

   private void updateProgressBar() {
      if (!this.showText.getValue()) {
         this.cancelProgressBar();
      } else {
         float ratio = (float)(this.balance / this.ticks.getValue().doubleValue());
         ratio = Math.max(0.0F, Math.min(1.0F, ratio));
         int percent = Math.round(ratio * 100.0F);
         if (this.autoHide.getValue() && percent >= 100) {
            this.cancelProgressBar();
         } else {
            ProgressBarOverlay overlay = Southside.get().getProgressBarOverlay();
            if (overlay != null) {
               int totalSteps = Math.max(1, this.ticks.getValue().intValue() * 1000);
               if (this.progressHandle == null || !this.progressHandle.isActive()) {
                  this.progressHandle = overlay.show(totalSteps, "Timer");
               }

               this.progressHandle.update((int)(ratio * totalSteps));
            }
         }
      }
   }

   private void cancelProgressBar() {
      if (this.progressHandle != null) {
         this.progressHandle.cancel();
         this.progressHandle = null;
      }
   }
}
