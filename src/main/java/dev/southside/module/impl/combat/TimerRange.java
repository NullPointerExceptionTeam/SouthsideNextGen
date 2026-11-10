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
import net.minecraft.entity.player.PlayerEntity;

public class TimerRange extends Module {
   private final Slider range = new Slider("Range", 3.0, 3.0, 6.0, 0.02);
   private final Slider boost = new Slider("Boost", 4.0, 1.0, 9.0, 0.1);
   private final Slider slowSpeed = new Slider("SlowSpeed", 0.2, 0.1, 0.9, 0.02);
   private final Switch onlyGround = new Switch("OnlyGround", true);
   private final Switch notOnEat = new Switch("NotOnEat", true);
   private final Switch onlyKillaura = new Switch("OnlyKillaura", true);
   private String inRange = "out";
   private PlayerEntity target;

   public TimerRange() {
      super(Category.Combat, "TimerRange", "Boost timer when enemies enter range");
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         MinecraftTickTimerUtils.setMultiplier(1.0F);
      }
   }

   @EventHandler
   public void onPlayerTick(TickEvent event) {
      if (event.getState() != Stage.POST) {
         if (mc.world != null && mc.player != null) {
            if (this.onlyGround.getValue() && !mc.player.isOnGround()) {
               MinecraftTickTimerUtils.setMultiplier(1.0F);
            } else if (this.notOnEat.getValue() && mc.player.isUsingItem()) {
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

   private PlayerEntity searchTargets() {
      if (KillAura.target != null && mc.player.distanceTo(KillAura.target) < this.range.getValue().floatValue() && KillAura.target instanceof PlayerEntity) {
         return (PlayerEntity)KillAura.target;
      } else {
         double closestDist = Double.MAX_VALUE;
         PlayerEntity closest = null;

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && TargetUtils.isTarget(player, true, true)) {
               double dist = mc.player.distanceTo(player);
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
