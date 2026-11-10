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
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class NoSlowTest extends Module {
   private Hand currentHand = Hand.MAIN_HAND;
   private Hand lastHand = Hand.MAIN_HAND;
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
      if (mc.player != null && event.getState() != Stage.POST) {
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
      if (mc.player != null && !this.special && !this.skip && shouldEnable) {
         UseAction useAction = mc.player.getActiveItem().getUseAction();
         if (this.isValidActionForNoSlow(useAction)) {
            event.setCancelled(true);
            mc.player.setSprinting(true);
         }
      }
   }

   @EventHandler
   public void onItemUse(ItemUseEvent event) {
      if (mc.crosshairTarget != null && mc.crosshairTarget.getType() != Type.MISS) {
         this.skip = true;
      }
   }

   private void handleItemUseRelease() {
      if (mc.player != null) {
         int maxTick = EntityUtils.isRemote(mc.player.getActiveItem().getUseAction()) ? 35 : 32;
         if (ServerPacketManager.deSyncing && ServerPacketManager.deSyncTick >= maxTick) {
            this.shouldRelease = true;
            mc.options.useKey.setPressed(false);
         }
      }
   }

   private void handleSpecialStateSwap() {
      if (mc.player != null) {
         if (this.special && mc.player.isUsingItem() && !EntityUtils.isConflict() && !EntityUtils.isCharged(mc.player.getActiveItem())) {
            this.swap(mc.player.getActiveHand(), 0);
         }
      }
   }

   private void handleSwapRecovery() {
      if (mc.player != null) {
         if (this.swapped && !shouldEnable && !mc.player.isUsingItem() && !ServerPacketManager.deSyncing) {
            PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            PacketOrderManager.swap = true;
            this.swapped = false;
         }
      }
   }

   private boolean isStew(ItemStack stack) {
      return stack.isOf(Items.MUSHROOM_STEW)
         || stack.isOf(Items.RABBIT_STEW)
         || stack.isOf(Items.BEETROOT_SOUP)
         || stack.isOf(Items.SUSPICIOUS_STEW);
   }

   private boolean isValidActionForNoSlow(UseAction action) {
      if (mc.player != null && this.isStew(mc.player.getActiveItem())) {
         return false;
      } else {
         return switch (action) {
            case EAT, DRINK, SPEAR -> true;
            case BOW, CROSSBOW -> this.bowNoSlow.getValue();
            default -> false;
         };
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.player != null && !this.skip) {
         this.handlePlayerActionPacket(event);
         this.handlePlayerInteractItemPacket(event);
      }
   }

   private void handlePlayerActionPacket(PacketEvent event) {
      if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == Action.RELEASE_USE_ITEM && shouldEnable) {
         this.shouldRelease = true;
      }
   }

   private void handlePlayerInteractBlockPacket(PacketEvent event) {
      if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet
         && mc.player != null
         && shouldEnable
         && packet.getHand() == this.currentHand
         && mc.player.getInventory().selectedSlot == this.oldSlot) {
         Hand nextHand = packet.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
         PacketUtils.sendPacketNoEvent(new PlayerInteractBlockC2SPacket(nextHand, packet.getBlockHitResult(), packet.getSequence()));
      }
   }

   @EventHandler
   public void onInteractItem(InteractItemEvent event) {
      if (event.getInteractHand().equals(Hand.MAIN_HAND)
         && !this.isValidItem(event.getItem())
         && this.isValidItem(mc.player.getStackInHand(Hand.OFF_HAND))) {
         if (event.getItem().getItem().equals(Items.FIRE_CHARGE)) {
            this.skip = true;
            return;
         }

         ActionResult result = event.getItem().copy().getItem().use(mc.world, mc.player, Hand.MAIN_HAND);
         if (result instanceof Success) {
            this.skip = true;
            return;
         }

         event.setCancelled(true);
      }
   }

   private boolean isValidItem(ItemStack item) {
      if (item.getUseAction() != null) {
         switch (item.getUseAction()) {
            case EAT:
            case DRINK:
            case SPEAR:
            case BOW:
            case CROSSBOW:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   private void handlePlayerInteractItemPacket(PacketEvent event) {
      if (event.getPacket() instanceof PlayerInteractItemC2SPacket packet) {
         ItemStack stack = null;
         if (mc.player != null) {
            stack = mc.player.getStackInHand(packet.getHand());
         }

         if (EntityUtils.isCharged(stack)) {
            this.special = false;
            return;
         }

         UseAction useAction = null;
         if (stack != null) {
            useAction = stack.getUseAction();
         }

         if (EntityUtils.isConflict()) {
            if (useAction != null && !this.isStew(stack)) {
               switch (useAction) {
                  case EAT:
                  case DRINK:
                  case SPEAR:
                     this.special = false;
                     shouldEnable = true;
                     ServerPacketManager.setup();
                     break;
                  case BOW:
                  case CROSSBOW:
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
               case EAT:
               case DRINK:
                  event.setCancelled(true);
                  this.swap(packet.getHand(), packet.getSequence());
                  break;
               case SPEAR:
                  this.special = false;
                  shouldEnable = true;
                  ServerPacketManager.setup();
                  break;
               case BOW:
               case CROSSBOW:
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
         PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
         PacketOrderManager.swap = true;
         this.swapped = false;
      }
   }

   private void handleHandReset() {
      if (this.swapped) {
         PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
         PacketOrderManager.swap = true;
         this.swapped = false;
      }
   }

   private void swap(Hand targetHand, int sequence) {
      if (mc.player != null) {
         shouldEnable = true;
         this.special = false;
         this.lastHand = targetHand;
         this.oldSlot = mc.player.getInventory().selectedSlot;
         this.currentHand = this.lastHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
         this.sendHandSwapPacket();
         this.swapped = true;
         this.sendItemInteractPacket(this.currentHand, sequence);
         ServerPacketManager.setup();
      }
   }

   private void sendHandSwapPacket() {
      PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
      PacketOrderManager.swap = true;
   }

   private void sendItemInteractPacket(Hand hand, int sequence) {
      if (mc.player != null) {
         PacketUtils.sendPacketNoEvent(new PlayerInteractItemC2SPacket(hand, sequence, RotationUtils.getRotationSafely().getYaw(), RotationUtils.getRotationSafely().getPitch()));
      }
   }
}
