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
import net.minecraft.class_2668;
import net.minecraft.class_2761;

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
      if (mc.field_1687 != null) {
         this.oldTime = mc.field_1687.method_8510();
         this.oldShould = mc.field_1687.field_54500;
         this.fastTimeBase = this.oldTime;
         this.lastRenderMs = System.currentTimeMillis();
         this.oldRainGradient = mc.field_1687.method_8430(1.0F);
         this.oldThunderGradient = mc.field_1687.method_8478(1.0F);
         this.oldRaining = mc.field_1687.method_28104().method_156();
      }
   }

   @Override
   public void onDisable() {
      forceSnow = false;
      forceRain = false;
      if (mc.field_1687 != null) {
         mc.field_1687.method_29089(mc.field_1687.method_8510(), this.oldTime, this.oldShould);
         mc.field_1687.method_28104().method_157(this.oldRaining);
         mc.field_1687.method_8519(this.oldRainGradient);
         mc.field_1687.method_8496(this.oldThunderGradient);
      }
   }

   @EventHandler
   public void onUpdate(TickEvent event) {
      if (mc.field_1687 != null) {
         if (this.timeEnabled.getValue()) {
            if (this.timeMode.isMode("Pinned")) {
               mc.field_1687.method_29089(mc.field_1687.method_8510(), this.timer.getValue().longValue() * 1000L, false);
            } else {
               long now = System.currentTimeMillis();
               long deltaMs = now - this.lastRenderMs;
               this.lastRenderMs = now;
               double ticksDelta = deltaMs / 50.0 * this.speed.getValue().doubleValue();
               this.fastTimeBase += (long)(ticksDelta * 20.0);
               mc.field_1687.method_29089(mc.field_1687.method_8510(), this.fastTimeBase, false);
            }
         }

         if (this.weatherEnabled.getValue()) {
            if (this.weatherMode.isMode("Clear")) {
               forceSnow = false;
               forceRain = false;
               mc.field_1687.method_28104().method_157(false);
               mc.field_1687.method_8519(0.0F);
               mc.field_1687.method_8496(0.0F);
            } else if (this.weatherMode.isMode("Rain")) {
               forceSnow = false;
               forceRain = true;
               mc.field_1687.method_28104().method_157(true);
               mc.field_1687.method_8519(1.0F);
               mc.field_1687.method_8496(0.0F);
            } else if (this.weatherMode.isMode("Thunder")) {
               forceSnow = false;
               forceRain = true;
               mc.field_1687.method_28104().method_157(true);
               mc.field_1687.method_8519(1.0F);
               mc.field_1687.method_8496(1.0F);
            } else if (this.weatherMode.isMode("Snow")) {
               forceSnow = true;
               forceRain = false;
               mc.field_1687.method_28104().method_157(true);
               mc.field_1687.method_8519(1.0F);
               mc.field_1687.method_8496(0.0F);
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
         if (this.timeEnabled.getValue() && event.getPacket() instanceof class_2761 packet) {
            this.oldTime = packet.comp_3219();
            event.setCancelled(true);
         }

         if (this.weatherEnabled.getValue()
            && event.getPacket() instanceof class_2668 gs
            && (
               gs.method_11491() == class_2668.field_25646
                  || gs.method_11491() == class_2668.field_25647
                  || gs.method_11491() == class_2668.field_25652
                  || gs.method_11491() == class_2668.field_25653
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
