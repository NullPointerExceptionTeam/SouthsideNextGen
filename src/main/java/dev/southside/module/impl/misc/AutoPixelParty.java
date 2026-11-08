package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.AIWalkController;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2806;
import net.minecraft.class_310;

public class AutoPixelParty extends Module {
   public Slider searchChunkRadius = new Slider("Search Chunk Radius", 6, 5, 10);
   public Slider hotbarIndex = new Slider("HotBar Index", 5, 1, 9, 1);
   private static final int SEARCH_Y_DOWN = 10;
   private static final long SEARCH_THREAD_SLEEP_MS = 8L;
   private static final long STOP_PATH_DELAY_MS = 3000L;
   private final class_310 mc = class_310.method_1551();
   private final ExecutorService searchExec = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "AutoPixelParty-Search");
      t.setDaemon(true);
      return t;
   });
   private Future<?> searchFuture = null;
   private final AtomicBoolean searchCancel = new AtomicBoolean(false);
   private class_2248 targetBlock = null;
   private volatile boolean walkingToTarget = false;
   private volatile long scheduledStopWalkingAt = -1L;
   private final AtomicInteger stopTaskGeneration = new AtomicInteger(0);

   public AutoPixelParty() {
      super(Category.Misc, "AutoPixelParty", "Automatically goes to pixel party block (hotbar slot 5)");
   }

   @Override
   public void onEnable() {
      this.stopTaskGeneration.incrementAndGet();
      this.scheduledStopWalkingAt = -1L;
      super.onEnable();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         this.processScheduledStopWalking();
         if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            class_1799 stack = this.getHotbarStack(this.hotbarIndex.getValue().intValue() - 1);
            if (stack != null && !stack.method_7960()) {
               this.cancelScheduledStopWalking();
               if (!(stack.method_7909() instanceof class_1747)) {
                  this.cancelSearch();
                  this.targetBlock = null;
               } else {
                  class_1747 blockItem = (class_1747)stack.method_7909();
                  class_2248 block = blockItem.method_7711();
                  if (!this.walkingToTarget || this.targetBlock != block) {
                     this.targetBlock = block;
                     this.startSearchForBlock(block);
                  }
               }
            } else {
               this.scheduleStopWalkingIn3s();
               this.cancelSearch();
               this.targetBlock = null;
            }
         }
      }
   }

   private class_1799 getHotbarStack(int hotbarIndex) {
      try {
         return this.mc.field_1724.method_31548().method_5438(hotbarIndex);
      } catch (Exception var5) {
         try {
            return (class_1799)this.mc.field_1724.method_31548().field_7547.get(hotbarIndex);
         } catch (Exception var4) {
            return class_1799.field_8037;
         }
      }
   }

   private void startSearchForBlock(class_2248 block) {
      this.cancelSearch();
      this.searchCancel.set(false);
      this.searchFuture = this.searchExec.submit(() -> {
         try {
            class_1937 world = this.mc.field_1687;
            class_2338 playerPos = this.mc.field_1724.method_24515();
            int playerChunkX = playerPos.method_10263() >> 4;
            int playerChunkZ = playerPos.method_10260() >> 4;
            class_2338 bestPos = null;
            double bestDistSqr = Double.MAX_VALUE;

            for (int r = 0; r <= this.searchChunkRadius.getValue().intValue() && !this.searchCancel.get(); r++) {
               int cx = playerChunkX - r;

               while (cx <= playerChunkX + r && !this.searchCancel.get()) {
                  for (int cz = playerChunkZ - r; cz <= playerChunkZ + r && !this.searchCancel.get(); cz++) {
                     class_2791 chunk = world.method_8402(cx, cz, class_2806.field_12803, false);
                     if (chunk != null) {
                        int baseX = cx << 4;
                        int baseZ = cz << 4;
                        int py = playerPos.method_10264();
                        int minY = Math.max(world.method_31607(), py - 10);
                        int maxY = Math.min(world.method_31607() + world.method_31605() - 1, py);

                        for (int lx = 0; lx < 16 && !this.searchCancel.get(); lx++) {
                           for (int lz = 0; lz < 16 && !this.searchCancel.get(); lz++) {
                              int gx = baseX + lx;
                              int gz = baseZ + lz;
                              double dx = gx + 0.5 - this.mc.field_1724.method_23317();
                              double dz = gz + 0.5 - this.mc.field_1724.method_23321();
                              double horizSqr = dx * dx + dz * dz;
                              if (bestPos == null || !(horizSqr > bestDistSqr)) {
                                 for (int gy = minY; gy <= maxY && !this.searchCancel.get(); gy++) {
                                    class_2338 check = new class_2338(gx, gy, gz);
                                    class_2680 st = world.method_8320(check);
                                    if (st.method_26204() == block) {
                                       double distSqr = this.mc.field_1724.method_19538().method_1025(check.method_46558());
                                       if (bestPos == null || distSqr < bestDistSqr) {
                                          bestPos = check;
                                          bestDistSqr = distSqr;
                                       }
                                       break;
                                    }
                                 }
                              }
                           }
                        }

                        try {
                           Thread.sleep(8L);
                        } catch (InterruptedException var33) {
                        }
                     }
                  }

                  cx++;
               }
               if (bestPos != null) break;
            }

            if (this.searchCancel.get()) {
               return;
            }

            if (bestPos != null) {
               class_2338 finalBestPos = bestPos;
               this.mc.execute(() -> {
                  if (this.mc.field_1724 != null) {
                     this.mc.field_1724.method_7353(class_2561.method_43470("[AutoPixelParty] 找到方块, 前往: " + finalBestPos.method_23854()), false);
                  }

                  AIWalkController.walkTo(finalBestPos);
                  this.walkingToTarget = true;
               });
            } else {
               this.mc.execute(() -> {
                  if (this.mc.field_1724 != null) {
                     this.mc.field_1724.method_7353(class_2561.method_43470("[AutoPixelParty] 未在附近找到该方块"), false);
                  }

                  this.walkingToTarget = false;
               });
            }
         } catch (Throwable var34) {
            var34.printStackTrace();
            this.mc.execute(() -> {
               if (this.mc.field_1724 != null) {
                  this.mc.field_1724.method_7353(class_2561.method_43470("[AutoPixelParty] 搜索时发生错误: " + var34.getMessage()), false);
               }
            });
         }
      });
   }

   private void cancelSearch() {
      this.searchCancel.set(true);
      if (this.searchFuture != null) {
         this.searchFuture.cancel(true);
         this.searchFuture = null;
      }

      this.walkingToTarget = false;
   }

   private void scheduleStopWalkingIn3s() {
      if (this.scheduledStopWalkingAt < 0L) {
         this.scheduledStopWalkingAt = System.currentTimeMillis() + 3000L;
      }
   }

   private void processScheduledStopWalking() {
      long stopAt = this.scheduledStopWalkingAt;
      if (stopAt >= 0L) {
         if (System.currentTimeMillis() >= stopAt) {
            this.scheduledStopWalkingAt = -1L;
            AIWalkController.stopWalking();
         }
      }
   }

   private void cancelScheduledStopWalking() {
      if (this.scheduledStopWalkingAt >= 0L) {
         this.scheduledStopWalkingAt = -1L;
         this.stopTaskGeneration.incrementAndGet();
      }
   }

   private void scheduleDisableStopWalkingIn3s() {
      int generation = this.stopTaskGeneration.incrementAndGet();
      CompletableFuture.delayedExecutor(3000L, TimeUnit.MILLISECONDS).execute(() -> {
         if (this.stopTaskGeneration.get() == generation) {
            this.mc.execute(AIWalkController::stopWalking);
         }
      });
   }

   @Override
   public void onDisable() {
      this.cancelSearch();
      this.targetBlock = null;
      this.scheduleDisableStopWalkingIn3s();
      super.onDisable();
   }
}
