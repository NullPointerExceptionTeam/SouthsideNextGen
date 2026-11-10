package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ChatUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.network.ClientPlayerEntity;

public class BundleDupe extends Module {
   private final Slider apDelay = new Slider("AP Delay", 30, 10, 100, 1);
   private final Slider apbDelay = new Slider("APB Delay", 20, 10, 100, 1);
   private final Slider packetCount = new Slider("Packet Count", 20, 5, 50, 1);
   private final Switch enableSounds = new Switch("Enable Sounds", false);
   private Thread dupeThread;
   private final AtomicBoolean running = new AtomicBoolean(false);
   private float pitchProgress = 0.8F;

   public BundleDupe() {
      super(Category.Misc, "Bundle Dupe", "Duplicates items using bundles");
   }

   @Override
   public void onEnable() {
      if (this.running.get()) {
         ChatUtils.info("Bundle dupe already started");
         this.setEnable(false);
      } else if (!this.checkConditions()) {
         this.setEnable(false);
      } else {
         this.startDupe();
      }
   }

   @Override
   public void onDisable() {
      this.stopDupe();
   }

   private void startDupe() {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientPlayerEntity player = mc.player;
      ScreenHandler screenHandler = player.currentScreenHandler;
      if (player != null && screenHandler != null) {
         this.running.set(true);
         this.pitchProgress = 0.8F;
         this.dupeThread = new Thread(
            () -> {
               Int2ObjectMap<ItemStack> stackMap = new Int2ObjectOpenHashMap();
               ChatUtils.info("Bundle dupe started");

               while (this.running.get() && mc.player != null) {
                  try {
                     mc.getNetworkHandler()
                        .sendPacket(
                           new ClickSlotC2SPacket(
                              screenHandler.syncId, 0, 36, 1, SlotActionType.PICKUP, player.getInventory().getStack(0).copy(), stackMap
                           )
                        );

                     for (int i = 0; i < this.packetCount.getValue().intValue(); i++) {
                        mc.getNetworkHandler()
                           .sendPacket(
                              new ClickSlotC2SPacket(
                                 screenHandler.syncId, screenHandler.getRevision(), 36, -1, SlotActionType.PICKUP, ItemStack.EMPTY, stackMap
                              )
                           );
                     }

                     Thread.sleep(this.apDelay.getValue().intValue());
                     mc.getNetworkHandler()
                        .sendPacket(
                           new ClickSlotC2SPacket(
                              screenHandler.syncId, 0, 36, 0, SlotActionType.PICKUP, player.getInventory().getStack(0).copy(), stackMap
                           )
                        );
                     Thread.sleep(this.apbDelay.getValue().intValue());
                     ItemStack firstStack = player.getInventory().getStack(0);
                     if (!firstStack.isOf(Items.BUNDLE) && !firstStack.isEmpty()) {
                        if (this.enableSounds.getValue()) {
                           mc.execute(() -> {
                              if (mc.player != null) {
                                 mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                              }
                           });
                           this.pitchProgress = 0.8F;
                        }

                        ChatUtils.info("Duping items...");
                        mc.getNetworkHandler()
                           .sendPacket(
                              new ClickSlotC2SPacket(
                                 screenHandler.syncId, 0, 36, 1, SlotActionType.PICKUP, player.getInventory().getStack(0).copy(), stackMap
                              )
                           );
                        Thread.sleep(50L);
                        mc.getNetworkHandler()
                           .sendPacket(
                              new ClickSlotC2SPacket(
                                 screenHandler.syncId, 0, -999, 0, SlotActionType.PICKUP, player.getInventory().getStack(0).copy(), stackMap
                              )
                           );
                        Thread.sleep(50L);
                     }
                  } catch (InterruptedException var6) {
                     break;
                  } catch (Exception var7) {
                     ChatUtils.error("Bundle dupe error: " + var7.getMessage());
                     break;
                  }
               }

               this.running.set(false);
               ChatUtils.info("Bundle dupe stopped");
            }
         );
         this.dupeThread.start();
      } else {
         ChatUtils.error("Player or screen handler is null");
         this.setEnable(false);
      }
   }

   private void stopDupe() {
      if (this.dupeThread != null && this.dupeThread.isAlive()) {
         this.running.set(false);

         try {
            this.dupeThread.join(1000L);
         } catch (InterruptedException var2) {
            this.dupeThread.interrupt();
         }

         this.dupeThread = null;
      }
   }

   private boolean checkConditions() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player == null) {
         ChatUtils.error("Player is null");
         return false;
      } else if (mc.player.currentScreenHandler == null) {
         ChatUtils.error("Screen handler is null");
         return false;
      } else {
         ItemStack firstStack = mc.player.getInventory().getStack(0);
         if (!firstStack.isOf(Items.BUNDLE)) {
            ChatUtils.error("First item must be a bundle");
            return false;
         } else if (firstStack.isEmpty()) {
            ChatUtils.error("Bundle is empty");
            return false;
         } else {
            return true;
         }
      }
   }
}
