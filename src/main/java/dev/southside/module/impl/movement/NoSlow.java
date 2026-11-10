package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.PlayerSlowdownEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.UpdateFoVEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.player.PlayerUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;

public class NoSlow extends Module {
   public Switch blink = new Switch("Use Blink", true);
   int useTickCounter = 0;
   private boolean wasSlowdownActive = false;
   private boolean pausePhase = false;
   private int pauseTimer = 0;
   private final int PAUSE_DURATION = 0;

   public NoSlow() {
      super(Category.Movement, "NoSlow", "666这个入开挂了，使用不减速。。");
   }

   @EventHandler
   public void onRotate(RotationEvent event) {
      boolean isValid = this.blink.getValue()
         && mc.player.getItemUseTime() > 16
         && !(mc.player.getActiveItem().getItem() instanceof BowItem)
         && !(mc.player.getActiveItem().getItem() instanceof CrossbowItem);
      if (BlinkUtils.blinking) {
         this.useTickCounter++;
      }

      if (isValid && !BlinkUtils.blinking) {
         BlinkUtils.blink();
         this.useTickCounter = mc.player.getItemUseTime();
         mc.options.useKey.setPressed(false);
      }

      if (this.useTickCounter >= 32) {
         BlinkUtils.stopBlink();
         this.useTickCounter = 0;
      }
   }

   @EventHandler
   public void onSlow(PlayerSlowdownEvent event) {
      boolean shouldActivate = mc.player.getItemUseTime() % 3 != 0
         && !(mc.player.getActiveItem().getItem() instanceof BowItem)
         && !(mc.player.getActiveItem().getItem() instanceof CrossbowItem);
      if (shouldActivate) {
         if (!this.wasSlowdownActive) {
            this.pausePhase = true;
            this.pauseTimer = 0;
            this.wasSlowdownActive = true;
            return;
         }

         if (this.pausePhase) {
            this.pauseTimer--;
            if (this.pauseTimer <= 0) {
               this.pausePhase = false;
               event.setCancelled(true);
               mc.player.setSprinting(true);
            }

            return;
         }

         event.setCancelled(true);
         mc.player.setSprinting(true);
      } else {
         this.wasSlowdownActive = false;
         this.pausePhase = false;
         this.pauseTimer = 0;
      }
   }

   @EventHandler
   public void onFoV(UpdateFoVEvent event) {
      if (PlayerUtils.isMoving() && mc.player.isUsingItem()) {
         event.setFov(1.0F);
      }
   }
}
