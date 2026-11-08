package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ChatUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2813;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_746;

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
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      class_1703 screenHandler = player.field_7512;
      if (player != null && screenHandler != null) {
         this.running.set(true);
         this.pitchProgress = 0.8F;
         this.dupeThread = new Thread(
            () -> {
               Int2ObjectMap<class_1799> stackMap = new Int2ObjectOpenHashMap();
               ChatUtils.info("Bundle dupe started");

               while (this.running.get() && mc.field_1724 != null) {
                  try {
                     mc.method_1562()
                        .method_52787(
                           new class_2813(
                              screenHandler.field_7763, 0, 36, 1, class_1713.field_7790, player.method_31548().method_5438(0).method_7972(), stackMap
                           )
                        );

                     for (int i = 0; i < this.packetCount.getValue().intValue(); i++) {
                        mc.method_1562()
                           .method_52787(
                              new class_2813(
                                 screenHandler.field_7763, screenHandler.method_37421(), 36, -1, class_1713.field_7790, class_1799.field_8037, stackMap
                              )
                           );
                     }

                     Thread.sleep(this.apDelay.getValue().intValue());
                     mc.method_1562()
                        .method_52787(
                           new class_2813(
                              screenHandler.field_7763, 0, 36, 0, class_1713.field_7790, player.method_31548().method_5438(0).method_7972(), stackMap
                           )
                        );
                     Thread.sleep(this.apbDelay.getValue().intValue());
                     class_1799 firstStack = player.method_31548().method_5438(0);
                     if (!firstStack.method_31574(class_1802.field_27023) && !firstStack.method_7960()) {
                        if (this.enableSounds.getValue()) {
                           mc.execute(() -> {
                              if (mc.field_1724 != null) {
                                 mc.field_1724.method_5783(class_3417.field_14627, 1.0F, 1.0F);
                              }
                           });
                           this.pitchProgress = 0.8F;
                        }

                        ChatUtils.info("Duping items...");
                        mc.method_1562()
                           .method_52787(
                              new class_2813(
                                 screenHandler.field_7763, 0, 36, 1, class_1713.field_7790, player.method_31548().method_5438(0).method_7972(), stackMap
                              )
                           );
                        Thread.sleep(50L);
                        mc.method_1562()
                           .method_52787(
                              new class_2813(
                                 screenHandler.field_7763, 0, -999, 0, class_1713.field_7790, player.method_31548().method_5438(0).method_7972(), stackMap
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
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null) {
         ChatUtils.error("Player is null");
         return false;
      } else if (mc.field_1724.field_7512 == null) {
         ChatUtils.error("Screen handler is null");
         return false;
      } else {
         class_1799 firstStack = mc.field_1724.method_31548().method_5438(0);
         if (!firstStack.method_31574(class_1802.field_27023)) {
            ChatUtils.error("First item must be a bundle");
            return false;
         } else if (firstStack.method_7960()) {
            ChatUtils.error("Bundle is empty");
            return false;
         } else {
            return true;
         }
      }
   }
}
