package dev.southside.utils.player;

import dev.southside.Southside;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_476;

public class ReportUtils {
   private static final class_310 mc = class_310.method_1551();
   private static boolean waiting = false;

   public static void report(String playerName) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         waiting = true;
         Southside.INSTANCE.getEventBus().register(ReportUtils.class);
         mc.field_1724.field_3944.method_45731("report " + playerName);
         ChatUtils.info("Sending report for " + playerName + "...");
      } else {
         ChatUtils.error("Player is not in game.");
      }
   }

   @EventHandler
   private static void onScreen(SetScreenEvent event) {
      if (waiting) {
         if (event.screen instanceof class_476 screen) {
            waiting = false;
            new Thread(() -> {
               long start = System.currentTimeMillis();

               while (System.currentTimeMillis() - start < 2000L) {
                  for (class_1735 slot : ((class_1707)screen.method_17577()).field_7761) {
                     if (slot.method_7677().method_7909() == class_1802.field_8802) {
                        mc.execute(() -> {
                           mc.field_1761.method_2906(((class_1707)screen.method_17577()).field_7763, slot.field_7874, 0, class_1713.field_7790, mc.field_1724);
                           ChatUtils.info("Report submitted.");
                           Southside.INSTANCE.getEventBus().unregister(ReportUtils.class);
                        });
                        return;
                     }
                  }

                  try {
                     Thread.sleep(50L);
                  } catch (InterruptedException var5) {
                  }
               }

               mc.execute(() -> {
                  ChatUtils.error("Diamond sword not found in report menu (timeout 2s).");
                  Southside.INSTANCE.getEventBus().unregister(ReportUtils.class);
               });
            }).start();
         }
      }
   }
}
