package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.event.events.PacketSendPostEvent;
import dev.southside.event.events.RawMouseEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class KeyPearl extends Module {
   private Dropdown modes = new Dropdown("Key Modes", "Mouse3", "Mouse3", "Mouse4", "Middle");
   private int originalSlot = -1;
   private int pearlSlot = -1;
   private boolean isHoldingPearl = false;
   private boolean shouldRelease;
   private boolean shouldHandleButtonPress;
   private boolean shouldHandleButtonRelease;
   private boolean pendingThrow;
   private int pendingSync = -1;
   private int key;

   public KeyPearl() {
      super(Category.Player, "KeyPearl");
   }

   @EventHandler
   public void onKey(RawMouseEvent event) {
      if (!ClientUtils.nullCheck()) {
         String var2 = this.modes.getMode();
         switch (var2) {
            case "Mouse3":
               this.key = 3;
               break;
            case "Mouse4":
               this.key = 4;
               break;
            case "Middle":
               this.key = 2;
         }

         if (event.getAction() == 1) {
            if (event.getButton() == this.key) {
               this.shouldHandleButtonPress = true;
               this.shouldRelease = true;
            }
         } else if (event.getAction() == 0 && event.getButton() == this.key && this.shouldRelease) {
            this.shouldHandleButtonRelease = true;
            this.shouldRelease = false;
         }
      }
   }

   @EventHandler(
      priority = -7891
   )
   public void onRotation(RotationEvent event) {
      if (this.shouldHandleButtonPress) {
         this.handleButtonPress();
         event.setDisableRotation(true);
         this.shouldHandleButtonPress = false;
      }

      if (mc.player != null && mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
         event.setDisableRotation(true);
      }

      if (this.shouldHandleButtonRelease) {
         this.shouldHandleButtonRelease = false;
         event.setDisableRotation(true);
         this.pendingThrow = this.isHoldingPearl && this.pearlSlot >= 0;
         this.isHoldingPearl = false;
      }

      if (this.isBusy()) {
         PacketOrderManager.rightClicking = true;
      }
   }

   @EventHandler
   public void onPacketSend(PacketSendPostEvent event) {
      if (!(event.getPacket() instanceof PlayerMoveC2SPacket)) {
         return;
      }

      this.flush();
   }

   private void flush() {
      if (this.pendingThrow) {
         this.pendingThrow = false;
         this.syncPending();
         PacketOrderManager.rightClicking = true;
         this.throwPearl();
         return;
      }

      this.syncPending();
   }

   private void syncPending() {
      if (this.pendingSync < 0) {
         return;
      }

      int slot = this.pendingSync;
      this.pendingSync = -1;
      PacketUtils.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
   }

   private boolean isBusy() {
      return this.isHoldingPearl || this.pendingThrow || this.pendingSync >= 0;
   }

   private void handleButtonPress() {
      if (mc.player == null) {
         return;
      }

      this.originalSlot = mc.player.getInventory().selectedSlot;
      this.pearlSlot = -1;

      for (int i = 0; i < 9; i++) {
         Item item = mc.player.getInventory().getStack(i).getItem();
         if (item == Items.ENDER_PEARL) {
            this.pearlSlot = i;
            break;
         }
      }

      if (this.pearlSlot < 0) {
         this.originalSlot = -1;
         return;
      }

      mc.player.getInventory().selectedSlot = this.pearlSlot;
      this.pendingSync = this.pearlSlot;
      this.isHoldingPearl = true;
   }

   private void throwPearl() {
      int slot = this.pearlSlot;
      int back = this.originalSlot;
      this.pearlSlot = -1;
      this.originalSlot = -1;

      if (slot < 0) {
         return;
      }

      ActionResult res = this.interactItem(mc.player, Hand.MAIN_HAND);
      if (res.isAccepted()) {
         mc.player.swingHand(Hand.MAIN_HAND);
      }

      if (back >= 0) {
         mc.player.getInventory().selectedSlot = back;
         this.pendingSync = back;
      }
   }

   public ActionResult interactItem(PlayerEntity player, Hand hand) {
      return (ActionResult)(mc.interactionManager != null ? mc.interactionManager.interactItem(player, hand) : ActionResult.FAIL);
   }

   @Override
   public void onDisable() {
      this.originalSlot = -1;
      this.pearlSlot = -1;
      this.isHoldingPearl = false;
      this.pendingThrow = false;
      this.pendingSync = -1;
      this.shouldHandleButtonPress = false;
      this.shouldHandleButtonRelease = false;
      this.shouldRelease = false;
   }

   public static boolean isWorking() {
      KeyPearl kp = Southside.get().getModuleManager().get(KeyPearl.class);
      return kp != null && kp.isEnabled() && kp.isBusy();
   }
}
