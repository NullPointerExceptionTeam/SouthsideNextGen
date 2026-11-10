package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class WorldManager extends Module {
   private final Switch timeEnabled = new Switch("Time", false);
   private final Dropdown timeMode = new Dropdown("Time Mode", "Pinned", "Pinned", "Fast");
   public Slider timer = new Slider("Time Value", 1, 0, 24, 1);
   public Slider speed = new Slider("Time Speed", 2.0, 0.5, 20.0, 0.5);
   private final Switch weatherEnabled = new Switch("Weather", false);
   private final Dropdown weatherMode = new Dropdown("Weather Mode", "Clear", "Clear", "Rain", "Thunder", "Snow");
   private long oldTime;
   private boolean oldShould;
   private long fastTimeBase;
   private long lastRenderMs;
   private float oldRainGradient;
   private float oldThunderGradient;
   private boolean oldRaining;
   public static boolean forceSnow = false;
   public static boolean forceRain = false;

   public WorldManager() {
      super(Category.Render, "WorldManager");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.timeMode.getLabel(), () -> !this.timeEnabled.getValue());
      this.hideIf(this.timer.getLabel(), () -> !this.timeEnabled.getValue() || !this.timeMode.isMode("Pinned"));
      this.hideIf(this.speed.getLabel(), () -> !this.timeEnabled.getValue() || !this.timeMode.isMode("Fast"));
      this.hideIf(this.weatherMode.getLabel(), () -> !this.weatherEnabled.getValue());
   }

   @Override
   public void onEnable() {
      if (mc.world != null) {
         this.oldTime = mc.world.getTime();
         this.oldShould = mc.world.shouldTickTimeOfDay;
         this.fastTimeBase = this.oldTime;
         this.lastRenderMs = System.currentTimeMillis();
         this.oldRainGradient = mc.world.getRainGradient(1.0F);
         this.oldThunderGradient = mc.world.getThunderGradient(1.0F);
         this.oldRaining = mc.world.getLevelProperties().isRaining();
      }
   }

   @Override
   public void onDisable() {
      forceSnow = false;
      forceRain = false;
      if (mc.world != null) {
         mc.world.setTime(mc.world.getTime(), this.oldTime, this.oldShould);
         mc.world.getLevelProperties().setRaining(this.oldRaining);
         mc.world.setRainGradient(this.oldRainGradient);
         mc.world.setThunderGradient(this.oldThunderGradient);
      }
   }

   @EventHandler
   public void onUpdate(TickEvent event) {
      if (mc.world != null) {
         if (this.timeEnabled.getValue()) {
            if (this.timeMode.isMode("Pinned")) {
               mc.world.setTime(mc.world.getTime(), this.timer.getValue().longValue() * 1000L, false);
            } else {
               long now = System.currentTimeMillis();
               long deltaMs = now - this.lastRenderMs;
               this.lastRenderMs = now;
               double ticksDelta = deltaMs / 50.0 * this.speed.getValue().doubleValue();
               this.fastTimeBase += (long)(ticksDelta * 20.0);
               mc.world.setTime(mc.world.getTime(), this.fastTimeBase, false);
            }
         }

         if (this.weatherEnabled.getValue()) {
            if (this.weatherMode.isMode("Clear")) {
               forceSnow = false;
               forceRain = false;
               mc.world.getLevelProperties().setRaining(false);
               mc.world.setRainGradient(0.0F);
               mc.world.setThunderGradient(0.0F);
            } else if (this.weatherMode.isMode("Rain")) {
               forceSnow = false;
               forceRain = true;
               mc.world.getLevelProperties().setRaining(true);
               mc.world.setRainGradient(1.0F);
               mc.world.setThunderGradient(0.0F);
            } else if (this.weatherMode.isMode("Thunder")) {
               forceSnow = false;
               forceRain = true;
               mc.world.getLevelProperties().setRaining(true);
               mc.world.setRainGradient(1.0F);
               mc.world.setThunderGradient(1.0F);
            } else if (this.weatherMode.isMode("Snow")) {
               forceSnow = true;
               forceRain = false;
               mc.world.getLevelProperties().setRaining(true);
               mc.world.setRainGradient(1.0F);
               mc.world.setThunderGradient(0.0F);
            }
         } else {
            forceSnow = false;
            forceRain = false;
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (!event.isSend()) {
         if (this.timeEnabled.getValue() && event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            this.oldTime = packet.time();
            event.setCancelled(true);
         }

         if (this.weatherEnabled.getValue()
            && event.getPacket() instanceof GameStateChangeS2CPacket gs
            && (
               gs.getReason() == GameStateChangeS2CPacket.RAIN_STARTED
                  || gs.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED
                  || gs.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
                  || gs.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED
            )) {
            event.setCancelled(true);
         }
      }
   }

   @Override
   public String getSuffix() {
      List<String> parts = new ArrayList<>();
      if (this.timeEnabled.getValue()) {
         parts.add(this.timeMode.isMode("Pinned") ? "Time:Pinned" : "Time:Fast");
      }

      if (this.weatherEnabled.getValue()) {
         parts.add("Weather:" + this.weatherMode.getMode());
      }

      return String.join(" ", parts);
   }
}
