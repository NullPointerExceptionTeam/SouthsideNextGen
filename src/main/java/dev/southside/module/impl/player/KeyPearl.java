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
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2828;
import net.minecraft.class_2868;

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

      if (mc.field_1724 != null && mc.field_1724.method_6047().method_31574(class_1802.field_8634)) {
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
      if (!(event.getPacket() instanceof class_2828)) {
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
      PacketUtils.sendPacket(new class_2868(slot));
   }

   private boolean isBusy() {
      return this.isHoldingPearl || this.pendingThrow || this.pendingSync >= 0;
   }

   private void handleButtonPress() {
      if (mc.field_1724 == null) {
         return;
      }

      this.originalSlot = mc.field_1724.method_31548().field_7545;
      this.pearlSlot = -1;

      for (int i = 0; i < 9; i++) {
         class_1792 item = mc.field_1724.method_31548().method_5438(i).method_7909();
         if (item == class_1802.field_8634) {
            this.pearlSlot = i;
            break;
         }
      }

      if (this.pearlSlot < 0) {
         this.originalSlot = -1;
         return;
      }

      mc.field_1724.method_31548().field_7545 = this.pearlSlot;
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

      class_1269 res = this.interactItem(mc.field_1724, class_1268.field_5808);
      if (res.method_23665()) {
         mc.field_1724.method_6104(class_1268.field_5808);
      }

      if (back >= 0) {
         mc.field_1724.method_31548().field_7545 = back;
         this.pendingSync = back;
      }
   }

   public class_1269 interactItem(class_1657 player, class_1268 hand) {
      return (class_1269)(mc.field_1761 != null ? mc.field_1761.method_2919(player, hand) : class_1269.field_5814);
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
