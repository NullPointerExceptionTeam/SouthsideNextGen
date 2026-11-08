package dev.southside.utils.player;

import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.orbit.EventHandler;
import net.minecraft.class_1802;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_310;

public class BedwarsCheckUtils {
   public static final BedwarsCheckUtils INSTANCE = new BedwarsCheckUtils();
   private static final class_310 mc = class_310.method_1551();
   private static final int SCAN_RADIUS = 20;
   private class_2338 selfBed = null;

   private BedwarsCheckUtils() {
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            if (this.selfBed == null) {
               boolean hasWoodenSword = false;

               for (int i = 0; i < 9; i++) {
                  if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8091) {
                     hasWoodenSword = true;
                     break;
                  }
               }

               if (hasWoodenSword) {
                  class_2338 playerPos = mc.field_1724.method_24515();

                  for (int x = -20; x <= 20; x++) {
                     for (int y = -20; y <= 20; y++) {
                        for (int z = -20; z <= 20; z++) {
                           class_2338 pos = playerPos.method_10069(x, y, z);
                           if (mc.field_1687.method_8320(pos).method_26204() instanceof class_2244) {
                              this.selfBed = pos;
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onWorld(WorldEvent event) {
      this.selfBed = null;
   }

   public boolean isBedwarsGaming() {
      return this.selfBed != null;
   }

   public class_2338 getSelfBed() {
      return this.selfBed;
   }
}
