package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.MinecraftTickTimerUtils;
import dev.southside.utils.player.TargetUtils;
import java.util.Objects;
import net.minecraft.class_1657;

public class TimerRange extends Module {
   private final Slider range = new Slider("Range", 3.0, 3.0, 6.0, 0.02);
   private final Slider boost = new Slider("Boost", 4.0, 1.0, 9.0, 0.1);
   private final Slider slowSpeed = new Slider("SlowSpeed", 0.2, 0.1, 0.9, 0.02);
   private final Switch onlyGround = new Switch("OnlyGround", true);
   private final Switch notOnEat = new Switch("NotOnEat", true);
   private final Switch onlyKillaura = new Switch("OnlyKillaura", true);
   private String inRange = "out";
   private class_1657 target;

   public TimerRange() {
      super(Category.Combat, "TimerRange", "Boost timer when enemies enter range");
   }

   @Override
   public void onDisable() {
      if (mc.field_1724 != null) {
         MinecraftTickTimerUtils.setMultiplier(1.0F);
      }
   }

   @EventHandler
   public void onPlayerTick(TickEvent event) {
      if (event.getState() != Stage.POST) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            if (this.onlyGround.getValue() && !mc.field_1724.method_24828()) {
               MinecraftTickTimerUtils.setMultiplier(1.0F);
            } else if (this.notOnEat.getValue() && mc.field_1724.method_6115()) {
               MinecraftTickTimerUtils.setMultiplier(1.0F);
            } else if (this.onlyKillaura.getValue() && !Southside.INSTANCE.getModuleManager().isEnabled(KillAura.class)) {
               MinecraftTickTimerUtils.setMultiplier(1.0F);
            } else {
               this.target = this.searchTargets();
               if (this.target == null) {
                  if (Objects.equals(this.inRange, "in")) {
                     this.inRange = "slow";
                  } else {
                     this.inRange = "out";
                  }
               } else if (Objects.equals(this.inRange, "out")) {
                  this.inRange = "teleport";
               } else {
                  this.inRange = "in";
               }

               float speed;
               if (Objects.equals(this.inRange, "teleport")) {
                  speed = this.boost.getValue().floatValue();
               } else if (Objects.equals(this.inRange, "slow")) {
                  speed = this.slowSpeed.getValue().floatValue();
               } else {
                  speed = 1.0F;
               }

               MinecraftTickTimerUtils.setMultiplier(speed);
            }
         }
      }
   }

   private class_1657 searchTargets() {
      if (KillAura.target != null && mc.field_1724.method_5739(KillAura.target) < this.range.getValue().floatValue() && KillAura.target instanceof class_1657) {
         return (class_1657)KillAura.target;
      } else {
         double closestDist = Double.MAX_VALUE;
         class_1657 closest = null;

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (player != mc.field_1724 && TargetUtils.isTarget(player, true, true)) {
               double dist = mc.field_1724.method_5739(player);
               if (dist <= this.range.getValue().floatValue() && dist < closestDist) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         return closest;
      }
   }
}
