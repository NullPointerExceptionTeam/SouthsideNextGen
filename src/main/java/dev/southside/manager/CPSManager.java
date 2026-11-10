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
      if (mc.player == null) {
         this.click_ticks.clear();
      } else {
         this.click_ticks.add(mc.player.age);
      }
   }

   public boolean cancelAttack(int limit) {
      return this.getCurrentCPS() >= limit;
   }

   public int getCurrentCPS() {
      if (mc.player == null) {
         this.click_ticks.clear();
      }

      this.click_ticks.removeIf(cpsData -> mc.player.age < cpsData || mc.player.age - cpsData > 20);
      return this.click_ticks.size();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.click_ticks.clear();
   }
}
