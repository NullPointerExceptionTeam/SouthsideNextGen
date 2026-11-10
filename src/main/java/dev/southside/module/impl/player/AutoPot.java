package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.timer.StopWatch;
import dev.southside.utils.types.Rotation;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potions;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;

public class AutoPot extends Module {
   public final Slider health = new Slider("Health", 15, 0, 20, 1);
   public final Switch healing = new Switch("Healing", true);
   public final Switch speed = new Switch("Speed", false);
   public final Switch jumpBoost = new Switch("Jump Boost", false);
   public final Switch fireResistance = new Switch("Fire Resistance", false);
   public final Switch onlyGround = new Switch("Only Ground", true);
   public final Switch autoRefill = new Switch("Auto Refill", false);
   public final Slider delay = new Slider("Delay", 500, 100, 2000, 50);
   private final StopWatch timer = new StopWatch();
   private final StopWatch refillTimer = new StopWatch();
   private boolean throwReady = false;
   private int potSlot = -1;
   private int oldSlot = -1;
   private int restoreTicks = -1;
   private int healingRefillSlot = -1;
   private int speedRefillSlot = -1;
   private int jumpBoostRefillSlot = -1;
   private int fireResRefillSlot = -1;

   public AutoPot() {
      super(Category.Player, "AutoPot", "Auto throw splash potions");
   }

   @Override
   public void onDisable() {
      this.throwReady = false;
      this.potSlot = -1;
      this.oldSlot = -1;
      this.restoreTicks = -1;
      this.healingRefillSlot = -1;
      this.speedRefillSlot = -1;
      this.jumpBoostRefillSlot = -1;
      this.fireResRefillSlot = -1;
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (mc.player != null && mc.world != null) {
         if (this.restoreTicks >= 0) {
            if (this.restoreTicks > 0) {
               RotationUtils.setRotation(new Rotation(mc.player.getYaw(), 89.0F), this.rotationPriority());
               this.restoreTicks--;
            } else {
               this.restoreTicks = -1;
            }
         } else if (mc.player.age > 10) {
            if (!this.onlyGround.getValue() || mc.player.isOnGround()) {
               if (this.timer.finished(this.delay.getValue().longValue())) {
                  if (this.autoRefill.getValue() && !mc.player.lastSprinting && this.refillTimer.finished(50L)) {
                     this.refillPotions();
                     this.refillTimer.reset();
                  }

                  this.potSlot = -1;
                  if (this.healing.getValue() && mc.player.getHealth() < this.health.getValue().floatValue()) {
                     this.potSlot = this.findHealingPotionInHotbar();
                  }

                  if (this.potSlot == -1 && this.speed.getValue() && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                     this.potSlot = this.findPotionInHotbar(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS);
                  }

                  if (this.potSlot == -1 && this.jumpBoost.getValue() && !mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                     this.potSlot = this.findPotionInHotbar(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING);
                  }

                  if (this.potSlot == -1 && this.fireResistance.getValue() && !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                     this.potSlot = this.findPotionInHotbar(Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE);
                  }

                  if (this.potSlot == -1) {
                     this.throwReady = false;
                  } else {
                     RotationUtils.setRotation(new Rotation(mc.player.getYaw(), 80.0F), this.rotationPriority());
                     this.throwReady = true;
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.throwReady && this.potSlot != -1 && mc.player != null) {
         this.throwReady = false;
         this.oldSlot = mc.player.getInventory().selectedSlot;
         mc.player.getInventory().selectedSlot = this.potSlot;
         if (mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND) instanceof Success success && success.swingSource() == SwingSource.CLIENT) {
            PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
         }

         mc.player.getInventory().selectedSlot = this.oldSlot;
         mc.interactionManager.syncSelectedSlot();
         this.potSlot = -1;
         this.oldSlot = -1;
         this.restoreTicks = 1;
         this.timer.reset();
      } else {
         this.throwReady = false;
      }
   }

   private int findHealingPotionInHotbar() {
      return this.findPotionInHotbar(Potions.HEALING, Potions.STRONG_HEALING);
   }

   @SafeVarargs
   private int findPotionInHotbar(RegistryEntry<Potion>... potions) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (this.isSplashPotion(stack, potions)) {
            return i;
         }
      }

      return -1;
   }

   @SafeVarargs
   private int findPotionInInventory(RegistryEntry<Potion>... potions) {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (this.isSplashPotion(stack, potions)) {
            return i;
         }
      }

      return -1;
   }

   @SafeVarargs
   private boolean isSplashPotion(ItemStack stack, RegistryEntry<Potion>... potions) {
      if (!stack.isEmpty() && stack.getItem() == Items.SPLASH_POTION) {
         PotionContentsComponent contents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
         if (contents == null) {
            return false;
         } else {
            for (RegistryEntry<Potion> potion : potions) {
               if (contents.matches(potion)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private void refillPotions() {
      Set<Integer> usedSlots = this.getInvManagerSlots();
      Set<Integer> reservedSlots = new HashSet<>(usedSlots);
      if (this.healingRefillSlot != -1) {
         reservedSlots.add(this.healingRefillSlot);
      }

      if (this.speedRefillSlot != -1) {
         reservedSlots.add(this.speedRefillSlot);
      }

      if (this.jumpBoostRefillSlot != -1) {
         reservedSlots.add(this.jumpBoostRefillSlot);
      }

      if (this.fireResRefillSlot != -1) {
         reservedSlots.add(this.fireResRefillSlot);
      }

      this.healingRefillSlot = this.validateRefillSlot(this.healingRefillSlot, Potions.HEALING, Potions.STRONG_HEALING);
      this.speedRefillSlot = this.validateRefillSlot(this.speedRefillSlot, Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS);
      this.jumpBoostRefillSlot = this.validateRefillSlot(this.jumpBoostRefillSlot, Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING);
      this.fireResRefillSlot = this.validateRefillSlot(this.fireResRefillSlot, Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE);
      if (this.healing.getValue() && this.findHealingPotionInHotbar() == -1) {
         int invSlot = this.findPotionInInventory(Potions.HEALING, Potions.STRONG_HEALING);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.interactionManager.clickSlot(0, invSlot, freeSlot, SlotActionType.SWAP, mc.player);
               this.healingRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.speed.getValue() && this.findPotionInHotbar(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS) == -1) {
         int invSlot = this.findPotionInInventory(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.interactionManager.clickSlot(0, invSlot, freeSlot, SlotActionType.SWAP, mc.player);
               this.speedRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.jumpBoost.getValue() && this.findPotionInHotbar(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING) == -1) {
         int invSlot = this.findPotionInInventory(Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.interactionManager.clickSlot(0, invSlot, freeSlot, SlotActionType.SWAP, mc.player);
               this.jumpBoostRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.fireResistance.getValue() && this.findPotionInHotbar(Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE) == -1) {
         int invSlot = this.findPotionInInventory(Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.interactionManager.clickSlot(0, invSlot, freeSlot, SlotActionType.SWAP, mc.player);
               this.fireResRefillSlot = freeSlot;
            }
         }
      }
   }

   @SafeVarargs
   private int validateRefillSlot(int slot, RegistryEntry<Potion>... potions) {
      if (slot == -1) {
         return -1;
      } else {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         return this.isSplashPotion(stack, potions) ? slot : -1;
      }
   }

   private int findFreeHotbarSlot(Set<Integer> usedSlots) {
      for (int i = 0; i < 9; i++) {
         if (!usedSlots.contains(i) && mc.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }

      for (int ix = 0; ix < 9; ix++) {
         if (!usedSlots.contains(ix)) {
            return ix;
         }
      }

      return -1;
   }

   private Set<Integer> getInvManagerSlots() {
      Set<Integer> slots = new HashSet<>();
      InvManagerN inv = Southside.get().getModuleManager().get(InvManagerN.class);
      if (inv != null && inv.isEnabled()) {
         if (inv.switchSword.getValue()) {
            slots.add(inv.swordSlot.getValue().intValue() - 1);
         }

         if (inv.switchBlock.getValue()) {
            slots.add(inv.blockSlot.getValue().intValue() - 1);
         }

         if (inv.switchPickaxe.getValue()) {
            slots.add(inv.pickaxeSlot.getValue().intValue() - 1);
         }

         if (inv.switchAxe.getValue()) {
            slots.add(inv.axeSlot.getValue().intValue() - 1);
         }

         if (inv.switchBow.getValue()) {
            slots.add(inv.bowSlot.getValue().intValue() - 1);
         }

         if (inv.switchWaterBucket.getValue()) {
            slots.add(inv.waterBucketSlot.getValue().intValue() - 1);
         }

         if (inv.switchEnderPearl.getValue()) {
            slots.add(inv.enderPearlSlot.getValue().intValue() - 1);
         }

         if (inv.switchFireball.getValue()) {
            slots.add(inv.fireballSlot.getValue().intValue() - 1);
         }

         if (inv.switchGoldenApple.getValue() && !inv.offhandItems.isMode("Golden Apple")) {
            slots.add(inv.goldenAppleSlot.getValue().intValue() - 1);
         }

         if (inv.switchProjectile.getValue() && InvManagerN.keepProjectile.getValue() && !inv.offhandItems.isMode("Projectile")) {
            slots.add(inv.projectileSlot.getValue().intValue() - 1);
         }

         if (inv.switchRod.getValue() && !inv.offhandItems.isMode("Fishing Rod")) {
            slots.add(inv.rodSlot.getValue().intValue() - 1);
         }

         return slots;
      } else {
         return slots;
      }
   }
}
