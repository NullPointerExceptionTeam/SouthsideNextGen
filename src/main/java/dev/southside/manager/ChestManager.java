package dev.southside.manager;

import dev.southside.MinecraftAccessor;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatAdapter;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Generated;
import net.minecraft.block.ChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

public class ChestManager implements MinecraftAccessor, ChatAdapter {
   private final Queue<BlockPos> tryQueue = new LinkedBlockingQueue<>();
   private final Queue<BlockPos> stealQueue = new LinkedBlockingQueue<>();
   public final Set<BlockPos> selfStolen = new LinkedHashSet<>();
   private final Set<BlockPos> stolenChests = new LinkedHashSet<>();

   public void trySteal(BlockPos pos) {
      if (!this.stealQueue.contains(pos)) {
         if (!this.tryQueue.contains(pos)) {
            this.tryQueue.offer(pos);
         }
      }
   }

   public void onSteal(BlockPos pos) {
      this.stealQueue.offer(pos);
   }

   public void onPacketNotify(BlockPos pos) {
      if (this.tryQueue.remove(pos)) {
         this.stealQueue.offer(pos);
      } else if (!this.stealQueue.contains(pos)) {
         this.stolenChests.add(pos);
      }
   }

   public void stolenAll() {
      while (!this.stealQueue.isEmpty()) {
         BlockPos poll = this.stealQueue.poll();
         if (poll != null) {
            this.selfStolen.add(poll);
            this.onStolen(poll);
         }
      }
   }

   public void onStolen(BlockPos pos) {
      if (this.stealQueue.remove(pos)) {
         this.selfStolen.add(pos);
      }

      this.stolenChests.add(pos);
   }

   public BlockPos getStealingChest() {
      Screen currentScreen = mc.currentScreen;
      return currentScreen instanceof GenericContainerScreen && !this.stealQueue.isEmpty() ? this.stealQueue.peek() : null;
   }

   public boolean isStolen(BlockPos pos) {
      return this.stolenChests.contains(pos);
   }

   public boolean isSelfStolen(BlockPos pos) {
      return this.selfStolen.contains(pos);
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof BlockEventS2CPacket sb) {
         if (mc.world != null) {
            BlockState state = mc.world.getBlockState(sb.getPos());
            if (state.getBlock() instanceof ChestBlock && sb.getType() == 1 && sb.getData() == 1) {
               this.onPacketNotify(sb.getPos());
            }
         }
      } else if (event.getPacket() instanceof PlayerInteractBlockC2SPacket ib) {
         BlockPos pos = ib.getBlockHitResult().getBlockPos();
         if (mc.world != null) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock) {
               this.trySteal(pos);
            }
         }
      }
   }

   @EventHandler
   private void onWorld(WorldEvent event) {
      this.tryQueue.clear();
      this.selfStolen.clear();
      this.stolenChests.clear();
      this.stealQueue.clear();
   }

   @Generated
   public Queue<BlockPos> getTryQueue() {
      return this.tryQueue;
   }

   @Generated
   public Queue<BlockPos> getStealQueue() {
      return this.stealQueue;
   }

   @Generated
   public Set<BlockPos> getSelfStolen() {
      return this.selfStolen;
   }

   @Generated
   public Set<BlockPos> getStolenChests() {
      return this.stolenChests;
   }
}
