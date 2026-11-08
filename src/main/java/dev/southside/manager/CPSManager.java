package dev.southside.manager;

import dev.southside.MinecraftAccessor;
import dev.southside.event.events.WorldEvent;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;

public class CPSManager implements MinecraftAccessor {
   public static final CPSManager INSTANCE = new CPSManager();
   private final List<Integer> click_ticks = new ArrayList<>();

   public void click() {
      if (mc.field_1724 == null) {
         this.click_ticks.clear();
      } else {
         this.click_ticks.add(mc.field_1724.field_6012);
      }
   }

   public boolean cancelAttack(int limit) {
      return this.getCurrentCPS() >= limit;
   }

   public int getCurrentCPS() {
      if (mc.field_1724 == null) {
         this.click_ticks.clear();
      }

      this.click_ticks.removeIf(cpsData -> mc.field_1724.field_6012 < cpsData || mc.field_1724.field_6012 - cpsData > 20);
      return this.click_ticks.size();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.click_ticks.clear();
   }
}
