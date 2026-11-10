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
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.client.MinecraftClient;

public class AutoPixelParty extends Module {
   public Slider searchChunkRadius = new Slider("Search Chunk Radius", 6, 5, 10);
   public Slider hotbarIndex = new Slider("HotBar Index", 5, 1, 9, 1);
   private static final int SEARCH_Y_DOWN = 10;
   private static final long SEARCH_THREAD_SLEEP_MS = 8L;
   private static final long STOP_PATH_DELAY_MS = 3000L;
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private final ExecutorService searchExec = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "AutoPixelParty-Search");
      t.setDaemon(true);
      return t;
   });
   private Future<?> searchFuture = null;
   private final AtomicBoolean searchCancel = new AtomicBoolean(false);
   private Block targetBlock = null;
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
         if (this.mc.player != null && this.mc.world != null) {
            ItemStack stack = this.getHotbarStack(this.hotbarIndex.getValue().intValue() - 1);
            if (stack != null && !stack.isEmpty()) {
               this.cancelScheduledStopWalking();
               if (!(stack.getItem() instanceof BlockItem)) {
                  this.cancelSearch();
                  this.targetBlock = null;
               } else {
                  BlockItem blockItem = (BlockItem)stack.getItem();
                  Block block = blockItem.getBlock();
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

   private ItemStack getHotbarStack(int hotbarIndex) {
      try {
         return this.mc.player.getInventory().getStack(hotbarIndex);
      } catch (Exception var5) {
         try {
            return (ItemStack)this.mc.player.getInventory().main.get(hotbarIndex);
         } catch (Exception var4) {
            return ItemStack.EMPTY;
         }
      }
   }

   private void startSearchForBlock(Block block) {
      this.cancelSearch();
      this.searchCancel.set(false);
      this.searchFuture = this.searchExec.submit(() -> {
         try {
            World world = this.mc.world;
            BlockPos playerPos = this.mc.player.getBlockPos();
            int playerChunkX = playerPos.getX() >> 4;
            int playerChunkZ = playerPos.getZ() >> 4;
            BlockPos bestPos = null;
            double bestDistSqr = Double.MAX_VALUE;

            for (int r = 0; r <= this.searchChunkRadius.getValue().intValue() && !this.searchCancel.get(); r++) {
               int cx = playerChunkX - r;

               while (cx <= playerChunkX + r && !this.searchCancel.get()) {
                  for (int cz = playerChunkZ - r; cz <= playerChunkZ + r && !this.searchCancel.get(); cz++) {
                     Chunk chunk = world.getChunk(cx, cz, ChunkStatus.FULL, false);
                     if (chunk != null) {
                        int baseX = cx << 4;
                        int baseZ = cz << 4;
                        int py = playerPos.getY();
                        int minY = Math.max(world.getBottomY(), py - 10);
                        int maxY = Math.min(world.getBottomY() + world.getHeight() - 1, py);

                        for (int lx = 0; lx < 16 && !this.searchCancel.get(); lx++) {
                           for (int lz = 0; lz < 16 && !this.searchCancel.get(); lz++) {
                              int gx = baseX + lx;
                              int gz = baseZ + lz;
                              double dx = gx + 0.5 - this.mc.player.getX();
                              double dz = gz + 0.5 - this.mc.player.getZ();
                              double horizSqr = dx * dx + dz * dz;
                              if (bestPos == null || !(horizSqr > bestDistSqr)) {
                                 for (int gy = minY; gy <= maxY && !this.searchCancel.get(); gy++) {
                                    BlockPos check = new BlockPos(gx, gy, gz);
                                    BlockState st = world.getBlockState(check);
                                    if (st.getBlock() == block) {
                                       double distSqr = this.mc.player.getPos().squaredDistanceTo(check.toCenterPos());
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
               BlockPos finalBestPos = bestPos;
               this.mc.execute(() -> {
                  if (this.mc.player != null) {
                     this.mc.player.sendMessage(Text.literal("[AutoPixelParty] 找到方块, 前往: " + finalBestPos.toShortString()), false);
                  }

                  AIWalkController.walkTo(finalBestPos);
                  this.walkingToTarget = true;
               });
            } else {
               this.mc.execute(() -> {
                  if (this.mc.player != null) {
                     this.mc.player.sendMessage(Text.literal("[AutoPixelParty] 未在附近找到该方块"), false);
                  }

                  this.walkingToTarget = false;
               });
            }
         } catch (Throwable var34) {
            var34.printStackTrace();
            this.mc.execute(() -> {
               if (this.mc.player != null) {
                  this.mc.player.sendMessage(Text.literal("[AutoPixelParty] 搜索时发生错误: " + var34.getMessage()), false);
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
