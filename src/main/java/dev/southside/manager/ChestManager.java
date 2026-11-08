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
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2623;
import net.minecraft.class_2680;
import net.minecraft.class_2885;
import net.minecraft.class_437;
import net.minecraft.class_476;

public class ChestManager implements MinecraftAccessor, ChatAdapter {
   private final Queue<class_2338> tryQueue = new LinkedBlockingQueue<>();
   private final Queue<class_2338> stealQueue = new LinkedBlockingQueue<>();
   public final Set<class_2338> selfStolen = new LinkedHashSet<>();
   private final Set<class_2338> stolenChests = new LinkedHashSet<>();

   public void trySteal(class_2338 pos) {
      if (!this.stealQueue.contains(pos)) {
         if (!this.tryQueue.contains(pos)) {
            this.tryQueue.offer(pos);
         }
      }
   }

   public void onSteal(class_2338 pos) {
      this.stealQueue.offer(pos);
   }

   public void onPacketNotify(class_2338 pos) {
      if (this.tryQueue.remove(pos)) {
         this.stealQueue.offer(pos);
      } else if (!this.stealQueue.contains(pos)) {
         this.stolenChests.add(pos);
      }
   }

   public void stolenAll() {
      while (!this.stealQueue.isEmpty()) {
         class_2338 poll = this.stealQueue.poll();
         if (poll != null) {
            this.selfStolen.add(poll);
            this.onStolen(poll);
         }
      }
   }

   public void onStolen(class_2338 pos) {
      if (this.stealQueue.remove(pos)) {
         this.selfStolen.add(pos);
      }

      this.stolenChests.add(pos);
   }

   public class_2338 getStealingChest() {
      class_437 currentScreen = mc.field_1755;
      return currentScreen instanceof class_476 && !this.stealQueue.isEmpty() ? this.stealQueue.peek() : null;
   }

   public boolean isStolen(class_2338 pos) {
      return this.stolenChests.contains(pos);
   }

   public boolean isSelfStolen(class_2338 pos) {
      return this.selfStolen.contains(pos);
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2623 sb) {
         if (mc.field_1687 != null) {
            class_2680 state = mc.field_1687.method_8320(sb.method_11298());
            if (state.method_26204() instanceof class_2281 && sb.method_11294() == 1 && sb.method_11296() == 1) {
               this.onPacketNotify(sb.method_11298());
            }
         }
      } else if (event.getPacket() instanceof class_2885 ib) {
         class_2338 pos = ib.method_12543().method_17777();
         if (mc.field_1687 != null) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (state.method_26204() instanceof class_2281) {
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
   public Queue<class_2338> getTryQueue() {
      return this.tryQueue;
   }

   @Generated
   public Queue<class_2338> getStealQueue() {
      return this.stealQueue;
   }

   @Generated
   public Set<class_2338> getSelfStolen() {
      return this.selfStolen;
   }

   @Generated
   public Set<class_2338> getStolenChests() {
      return this.stolenChests;
   }
}
