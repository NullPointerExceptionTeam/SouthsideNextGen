package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_2761;

public class TimeChange extends Module {
   private final Dropdown mode = new Dropdown("Mode", "Pinned", "Pinned", "Fast");
   public Slider timer = new Slider("Timer", 1, 0, 24, 1);
   public Slider speed = new Slider("Speed", 2.0, 0.5, 20.0, 0.5);
   private long oldTime;
   private boolean oldShould;
   private long fastTimeBase;
   private long lastRenderMs;

   public TimeChange() {
      super(Category.Render, "TimerChange");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.timer.getLabel(), () -> !this.mode.isMode("Pinned"));
      this.hideIf(this.speed.getLabel(), () -> !this.mode.isMode("Fast"));
   }

   @Override
   public void onEnable() {
      if (mc.field_1687 != null) {
         this.oldTime = mc.field_1687.method_8510();
         this.oldShould = mc.field_1687.field_54500;
         this.fastTimeBase = this.oldTime;
         this.lastRenderMs = System.currentTimeMillis();
      }
   }

   @Override
   public void onDisable() {
      if (mc.field_1687 != null) {
         mc.field_1687.method_29089(mc.field_1687.method_8510(), this.oldTime, this.oldShould);
      }
   }

   @EventHandler
   public void onUpdate(TickEvent event) {
      if (mc.field_1687 != null) {
         if (this.mode.isMode("Pinned")) {
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
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.packet instanceof class_2761 packet) {
         this.oldTime = packet.comp_3219();
         event.setCancelled(true);
      }
   }

   @Override
   public String getSuffix() {
      return this.mode.isMode("Pinned") ? "Pinned" : "Fast";
   }
}
