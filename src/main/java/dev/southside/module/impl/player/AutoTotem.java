package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class AutoTotem extends Module {
   public Slider health = new Slider("Health", 3, 0, 20);
   private int oldSlot = -1;
   private boolean hasSwapped = false;

   public AutoTotem() {
      super(Category.Player, "AutoTotem");
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mc.player != null && mc.interactionManager != null) {
         if (this.isTotemInOffhand()) {
            if (this.oldSlot != -1) {
               mc.player.getInventory().selectedSlot = this.oldSlot;
               this.oldSlot = -1;
            }

            this.hasSwapped = false;
         } else {
            if (this.shouldSwap()) {
               int totemSlot = this.findTotemSlot();
               if (totemSlot == -1) {
                  this.resetState();
                  return;
               }

               this.handleTotemSwap(totemSlot);
            } else {
               this.resetState();
            }
         }
      }
   }

   private boolean isTotemInOffhand() {
      return mc.player == null ? false : mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
   }

   private boolean isTotemInMainhand() {
      return mc.player == null ? false : mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING;
   }

   private int findTotemSlot() {
      if (mc.player == null) {
         return -1;
      } else if (this.isTotemInMainhand()) {
         return mc.player.getInventory().selectedSlot;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
               return i;
            }
         }

         return -1;
      }
   }

   private void handleTotemSwap(int totemSlot) {
      if (mc.player != null && mc.interactionManager != null) {
         if (this.oldSlot == -1) {
            this.oldSlot = mc.player.getInventory().selectedSlot;
         }

         if (mc.player.getInventory().selectedSlot != totemSlot) {
            mc.player.getInventory().selectedSlot = totemSlot;
            mc.interactionManager.syncSelectedSlot();
         }

         if (!this.hasSwapped && mc.player.getInventory().selectedSlot == totemSlot) {
            this.hasSwapped = true;
            PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
         }
      }
   }

   private void resetState() {
      if (this.hasSwapped && !this.isTotemInOffhand()) {
         this.hasSwapped = false;
      }

      this.oldSlot = -1;
   }

   private boolean shouldSwap() {
      if (mc.player == null) {
         return false;
      } else {
         float absorption = mc.player.hasStatusEffect(StatusEffects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0F;
         return mc.player.getHealth() + absorption <= this.health.getValue().doubleValue()
            ? true
            : mc.player.fallDistance > 10.0F && !mc.player.isGliding();
      }
   }
}
