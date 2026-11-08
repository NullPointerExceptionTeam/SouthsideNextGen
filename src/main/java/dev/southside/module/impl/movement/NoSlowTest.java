package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.InteractItemEvent;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerSlowdownEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.world.EntityUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_2886;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2846.class_2847;

public class NoSlowTest extends Module {
   private class_1268 currentHand = class_1268.field_5808;
   private class_1268 lastHand = class_1268.field_5808;
   private final MSTimer timer = new MSTimer();
   public static boolean shouldEnable = false;
   private boolean special = false;
   private boolean shouldRelease = false;
   private boolean skip = false;
   private boolean swapped = false;
   private int oldSlot = -1;
   public final Switch bowNoSlow = new Switch("BowNoSlow", true);

   public NoSlowTest() {
      super(Category.Movement, "NoSlowTest", "Grim NoSlow Bypass via Inventory Desync");
   }

   @Override
   public void onDisable() {
      if (shouldEnable) {
         this.resetState();
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1724 != null && event.getState() != Stage.POST) {
         this.skip = false;
         if (this.shouldRelease) {
            this.resetState();
            this.shouldRelease = false;
         }

         this.handleItemUseRelease();
         this.handleSpecialStateSwap();
         this.handleSwapRecovery();
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (shouldEnable && Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
         this.special = false;
         ServerPacketManager.reset();
         shouldEnable = false;
         this.timer.reset();
      }
   }

   @EventHandler
   public void onSlowDown(PlayerSlowdownEvent event) {
      if (mc.field_1724 != null && !this.special && !this.skip && shouldEnable) {
         class_1839 useAction = mc.field_1724.method_6030().method_7976();
         if (this.isValidActionForNoSlow(useAction)) {
            event.setCancelled(true);
            mc.field_1724.method_5728(true);
         }
      }
   }

   @EventHandler
   public void onItemUse(ItemUseEvent event) {
      if (mc.field_1765 != null && mc.field_1765.method_17783() != class_240.field_1333) {
         this.skip = true;
      }
   }

   private void handleItemUseRelease() {
      if (mc.field_1724 != null) {
         int maxTick = EntityUtils.isRemote(mc.field_1724.method_6030().method_7976()) ? 35 : 32;
         if (ServerPacketManager.deSyncing && ServerPacketManager.deSyncTick >= maxTick) {
            this.shouldRelease = true;
            mc.field_1690.field_1904.method_23481(false);
         }
      }
   }

   private void handleSpecialStateSwap() {
      if (mc.field_1724 != null) {
         if (this.special && mc.field_1724.method_6115() && !EntityUtils.isConflict() && !EntityUtils.isCharged(mc.field_1724.method_6030())) {
            this.swap(mc.field_1724.method_6058(), 0);
         }
      }
   }

   private void handleSwapRecovery() {
      if (mc.field_1724 != null) {
         if (this.swapped && !shouldEnable && !mc.field_1724.method_6115() && !ServerPacketManager.deSyncing) {
            PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
            PacketOrderManager.swap = true;
            this.swapped = false;
         }
      }
   }

   private boolean isStew(class_1799 stack) {
      return stack.method_31574(class_1802.field_8208)
         || stack.method_31574(class_1802.field_8308)
         || stack.method_31574(class_1802.field_8515)
         || stack.method_31574(class_1802.field_8766);
   }

   private boolean isValidActionForNoSlow(class_1839 action) {
      if (mc.field_1724 != null && this.isStew(mc.field_1724.method_6030())) {
         return false;
      } else {
         return switch (action) {
            case field_8950, field_8946, field_8951 -> true;
            case field_8953, field_8947 -> this.bowNoSlow.getValue();
            default -> false;
         };
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1724 != null && !this.skip) {
         this.handlePlayerActionPacket(event);
         this.handlePlayerInteractItemPacket(event);
      }
   }

   private void handlePlayerActionPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2846 packet && packet.method_12363() == class_2847.field_12974 && shouldEnable) {
         this.shouldRelease = true;
      }
   }

   private void handlePlayerInteractBlockPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2885 packet
         && mc.field_1724 != null
         && shouldEnable
         && packet.method_12546() == this.currentHand
         && mc.field_1724.method_31548().field_7545 == this.oldSlot) {
         class_1268 nextHand = packet.method_12546() == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
         PacketUtils.sendPacketNoEvent(new class_2885(nextHand, packet.method_12543(), packet.method_42080()));
      }
   }

   @EventHandler
   public void onInteractItem(InteractItemEvent event) {
      if (event.getInteractHand().equals(class_1268.field_5808)
         && !this.isValidItem(event.getItem())
         && this.isValidItem(mc.field_1724.method_5998(class_1268.field_5810))) {
         if (event.getItem().method_7909().equals(class_1802.field_8814)) {
            this.skip = true;
            return;
         }

         class_1269 result = event.getItem().method_7972().method_7909().method_7836(mc.field_1687, mc.field_1724, class_1268.field_5808);
         if (result instanceof class_9860) {
            this.skip = true;
            return;
         }

         event.setCancelled(true);
      }
   }

   private boolean isValidItem(class_1799 item) {
      if (item.method_7976() != null) {
         switch (item.method_7976()) {
            case field_8950:
            case field_8946:
            case field_8951:
            case field_8953:
            case field_8947:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   private void handlePlayerInteractItemPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2886 packet) {
         class_1799 stack = null;
         if (mc.field_1724 != null) {
            stack = mc.field_1724.method_5998(packet.method_12551());
         }

         if (EntityUtils.isCharged(stack)) {
            this.special = false;
            return;
         }

         class_1839 useAction = null;
         if (stack != null) {
            useAction = stack.method_7976();
         }

         if (EntityUtils.isConflict()) {
            if (useAction != null && !this.isStew(stack)) {
               switch (useAction) {
                  case field_8950:
                  case field_8946:
                  case field_8951:
                     this.special = false;
                     shouldEnable = true;
                     ServerPacketManager.setup();
                     break;
                  case field_8953:
                  case field_8947:
                     if (this.bowNoSlow.getValue()) {
                        this.special = false;
                        shouldEnable = true;
                        ServerPacketManager.setup();
                     }
               }
            }

            return;
         }

         if (useAction != null) {
            if (this.isStew(stack)) {
               return;
            }

            switch (useAction) {
               case field_8950:
               case field_8946:
                  event.setCancelled(true);
                  this.swap(packet.method_12551(), packet.method_42081());
                  break;
               case field_8951:
                  this.special = false;
                  shouldEnable = true;
                  ServerPacketManager.setup();
                  break;
               case field_8953:
               case field_8947:
                  if (this.bowNoSlow.getValue()) {
                     this.special = false;
                     shouldEnable = true;
                     ServerPacketManager.setup();
                  }
            }
         }
      }
   }

   public void resetState() {
      this.special = false;
      ServerPacketManager.reset();
      if (shouldEnable) {
         this.handleHandReset();
         shouldEnable = false;
         this.timer.reset();
      }

      if (this.swapped) {
         PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
         PacketOrderManager.swap = true;
         this.swapped = false;
      }
   }

   private void handleHandReset() {
      if (this.swapped) {
         PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
         PacketOrderManager.swap = true;
         this.swapped = false;
      }
   }

   private void swap(class_1268 targetHand, int sequence) {
      if (mc.field_1724 != null) {
         shouldEnable = true;
         this.special = false;
         this.lastHand = targetHand;
         this.oldSlot = mc.field_1724.method_31548().field_7545;
         this.currentHand = this.lastHand == class_1268.field_5808 ? class_1268.field_5810 : class_1268.field_5808;
         this.sendHandSwapPacket();
         this.swapped = true;
         this.sendItemInteractPacket(this.currentHand, sequence);
         ServerPacketManager.setup();
      }
   }

   private void sendHandSwapPacket() {
      PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
      PacketOrderManager.swap = true;
   }

   private void sendItemInteractPacket(class_1268 hand, int sequence) {
      if (mc.field_1724 != null) {
         PacketUtils.sendPacketNoEvent(new class_2886(hand, sequence, RotationUtils.getRotationSafely().getYaw(), RotationUtils.getRotationSafely().getPitch()));
      }
   }
}
