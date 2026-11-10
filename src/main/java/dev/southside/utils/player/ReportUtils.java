package dev.southside.utils.player;

import dev.southside.Southside;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

public class ReportUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static boolean waiting = false;

   public static void report(String playerName) {
      if (mc.player != null && mc.player.networkHandler != null) {
         waiting = true;
         Southside.INSTANCE.getEventBus().register(ReportUtils.class);
         mc.player.networkHandler.sendCommand("report " + playerName);
         ChatUtils.info("Sending report for " + playerName + "...");
      } else {
         ChatUtils.error("Player is not in game.");
      }
   }

   @EventHandler
   private static void onScreen(SetScreenEvent event) {
      if (waiting) {
         if (event.screen instanceof GenericContainerScreen screen) {
            waiting = false;
            new Thread(() -> {
               long start = System.currentTimeMillis();

               while (System.currentTimeMillis() - start < 2000L) {
                  for (Slot slot : ((GenericContainerScreenHandler)screen.getScreenHandler()).slots) {
                     if (slot.getStack().getItem() == Items.DIAMOND_SWORD) {
                        mc.execute(() -> {
                           mc.interactionManager.clickSlot(((GenericContainerScreenHandler)screen.getScreenHandler()).syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
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
