package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.timer.StopWatch;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;
import net.minecraft.class_2846.class_2847;

public class AutoSoup extends Module {
   public Slider health = new Slider("Health", 15, 0, 20, 1);
   public Slider minDelay = new Slider("Min Delay", 300, 0, 1000, 1);
   public Slider maxDelay = new Slider("Max Delay", 500, 0, 1000, 1);
   public Switch autoRefill = new Switch("Auto Refill", false);
   private StopWatch timer = new StopWatch();
   private StopWatch refillTimer = new StopWatch();
   private int soup = -1;
   private int oldSlot;

   public AutoSoup() {
      super(Category.Player, "AutoSoup", "自动喝汤");
   }

   @Override
   public void onDisable() {
      this.soup = -1;
   }

   @EventHandler
   private void onRotationApplied(RotationAppliedEvent event) {
      if (this.autoRefill.getValue() && !mc.field_1724.field_3919 && this.refillTimer.finished(50L)) {
         this.refillSoup();
         this.refillTimer.reset();
      }

      if (this.timer.finished(this.getRandomDelay()) && mc.field_1724.field_6012 > 10 && mc.field_1724.method_6032() < this.health.getValue().floatValue()) {
         this.soup = this.findSoup();
         if (this.soup != -1) {
            this.oldSlot = mc.field_1724.method_31548().field_7545;
            mc.field_1724.method_31548().field_7545 = this.soup;
            if (mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808) instanceof class_9860 success3
               && success3.comp_2909() == class_9861.field_52427) {
               PacketUtils.sendPacket(new class_2879(class_1268.field_5808));
            }

            class_1799 afterUse = mc.field_1724.method_31548().method_5438(this.soup);
            if (afterUse.method_7909() == class_1802.field_8428 || afterUse.method_7909() == class_1802.field_8208) {
               mc.method_1562().method_52787(new class_2846(class_2847.field_12975, class_2338.field_10980, class_2350.field_11033));
               mc.field_1724.method_31548().method_5447(this.soup, class_1799.field_8037);
            }

            mc.field_1724.method_31548().field_7545 = this.oldSlot;
            mc.field_1761.method_2911();
         }

         this.timer.reset();
      }
   }

   private int findSoup() {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8208) {
            return i;
         }
      }

      return -1;
   }

   private long getRandomDelay() {
      double min = this.minDelay.getValue().doubleValue();
      double max = this.maxDelay.getValue().doubleValue();
      return (long)(min + Math.random() * (max - min));
   }

   private void refillSoup() {
      int soupSlot = InventoryUtils.findItem(9, 36, class_1802.field_8208);
      if (soupSlot != -1) {
         int emptySlot = this.findEmptyHotbarSlot();
         if (emptySlot != -1) {
            mc.field_1761.method_2906(0, soupSlot, emptySlot, class_1713.field_7791, mc.field_1724);
         }
      }
   }

   private int findEmptyHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7960()) {
            return i;
         }
      }

      return -1;
   }
}
