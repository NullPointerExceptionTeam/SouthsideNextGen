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
import net.minecraft.class_1753;
import net.minecraft.class_1764;

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
         && mc.field_1724.method_6048() > 16
         && !(mc.field_1724.method_6030().method_7909() instanceof class_1753)
         && !(mc.field_1724.method_6030().method_7909() instanceof class_1764);
      if (BlinkUtils.blinking) {
         this.useTickCounter++;
      }

      if (isValid && !BlinkUtils.blinking) {
         BlinkUtils.blink();
         this.useTickCounter = mc.field_1724.method_6048();
         mc.field_1690.field_1904.method_23481(false);
      }

      if (this.useTickCounter >= 32) {
         BlinkUtils.stopBlink();
         this.useTickCounter = 0;
      }
   }

   @EventHandler
   public void onSlow(PlayerSlowdownEvent event) {
      boolean shouldActivate = mc.field_1724.method_6048() % 3 != 0
         && !(mc.field_1724.method_6030().method_7909() instanceof class_1753)
         && !(mc.field_1724.method_6030().method_7909() instanceof class_1764);
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
               mc.field_1724.method_5728(true);
            }

            return;
         }

         event.setCancelled(true);
         mc.field_1724.method_5728(true);
      } else {
         this.wasSlowdownActive = false;
         this.pausePhase = false;
         this.pauseTimer = 0;
      }
   }

   @EventHandler
   public void onFoV(UpdateFoVEvent event) {
      if (PlayerUtils.isMoving() && mc.field_1724.method_6115()) {
         event.setFov(1.0F);
      }
   }
}
