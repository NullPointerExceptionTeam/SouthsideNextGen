package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.EnchantmentUtils;
import dev.southside.utils.player.MovementUtils;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class Criticals extends Module {
   public static final Dropdown mode = new Dropdown("Mode", "Switch", "Switch", "Stuck", "Packet");
   private int lastSlot;
   private int attacks;
   private boolean release;
   private boolean spamAttack;

   public Criticals() {
      super(Category.Combat, "Criticals", "idk");
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (mode.isMode("Stuck") && event.stage == Stage.PRE) {
         this.attacks++;
         if (this.attacks % 2 == 0) {
            this.release = true;
         }
      }

      if (mode.isMode("Switch")) {
         if (event.stage == Stage.PRE) {
            this.lastSlot = -1;
            int bestSword = this.getBestSword(mc.player.playerScreenHandler);
            if (bestSword == -1) {
               return;
            }

            if (mc.player.getInventory().selectedSlot != bestSword) {
               this.lastSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = bestSword;
            }
         } else if (this.lastSlot != -1) {
            mc.player.getInventory().selectedSlot = this.lastSlot;
            int i = mc.player.getInventory().selectedSlot;
            if (i != mc.interactionManager.lastSelectedSlot) {
               mc.interactionManager.lastSelectedSlot = i;
               mc.interactionManager.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.interactionManager.lastSelectedSlot));
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mode.isMode("Stuck")) {
         if (KillAura.target == null || !Southside.INSTANCE.getModuleManager().get(KillAura.class).inRaycast(KillAura.target)) {
            this.attacks = 0;
         } else if (mc.player.fallDistance > 0.0F && !this.release && mc.player.getVelocity().y < 0.05) {
            MovementUtils.cancelMove();
         } else if (this.release) {
            this.release = false;
         }
      }
   }

   private int getBestSword(ScreenHandler c) {
      double damage = 0.0;
      int slot = -1;

      for (int i = 36; i < 45; i++) {
         ItemStack stack = c.getSlot(i).getStack();
         if (!stack.isEmpty() && mc.player.getInventory().selectedSlot != i - 36) {
            double newDamage = this.getItemDamage(stack);
            if (newDamage > damage) {
               damage = newDamage;
               slot = i - 36;
            }
         }
      }

      return slot;
   }

   private double getItemDamage(ItemStack stack) {
      double damage = 0.0;
      if (stack.hasEnchantments()) {
         damage += EnchantmentUtils.getLevel(Enchantments.SHARPNESS, stack) * 0.5;
      }

      return damage;
   }
}
