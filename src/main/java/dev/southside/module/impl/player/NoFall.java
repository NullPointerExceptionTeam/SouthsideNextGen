package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class NoFall extends Module {
   private final Slider distance = new Slider("Fall Distance", 3.0F, 1.0F, 15.0F, 1.0F);
   public static boolean falling = false;
   public boolean jump = false;

   public NoFall() {
      super(Category.Player, "NoFall", "NoFallNoFallNoFallNoFallNoFallNoFall");
   }

   @Override
   public void onEnable() {
      falling = false;
      this.jump = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.player != null) {
         double fallDistance = mc.player.fallDistance;
         if (fallDistance > this.distance.getValue().doubleValue()) {
            falling = true;
            this.jump = false;
         } else {
            falling = false;
         }
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.player != null) {
         if (event.getState() == Stage.PRE && falling && mc.player.isOnGround()) {
            event.setY(event.getY() + 1.0);
            this.jump = true;
         }
      }
   }

   @EventHandler
   public void onMovementInput(MovementInputEvent event) {
      if (mc.player != null) {
         if (this.jump) {
            event.setJump(true);
            this.jump = false;
         }
      }
   }
}
