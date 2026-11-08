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
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1842;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2879;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;

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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.restoreTicks >= 0) {
            if (this.restoreTicks > 0) {
               RotationUtils.setRotation(new Rotation(mc.field_1724.method_36454(), 89.0F), this.rotationPriority());
               this.restoreTicks--;
            } else {
               this.restoreTicks = -1;
            }
         } else if (mc.field_1724.field_6012 > 10) {
            if (!this.onlyGround.getValue() || mc.field_1724.method_24828()) {
               if (this.timer.finished(this.delay.getValue().longValue())) {
                  if (this.autoRefill.getValue() && !mc.field_1724.field_3919 && this.refillTimer.finished(50L)) {
                     this.refillPotions();
                     this.refillTimer.reset();
                  }

                  this.potSlot = -1;
                  if (this.healing.getValue() && mc.field_1724.method_6032() < this.health.getValue().floatValue()) {
                     this.potSlot = this.findHealingPotionInHotbar();
                  }

                  if (this.potSlot == -1 && this.speed.getValue() && !mc.field_1724.method_6059(class_1294.field_5904)) {
                     this.potSlot = this.findPotionInHotbar(class_1847.field_9005, class_1847.field_8983, class_1847.field_8966);
                  }

                  if (this.potSlot == -1 && this.jumpBoost.getValue() && !mc.field_1724.method_6059(class_1294.field_5913)) {
                     this.potSlot = this.findPotionInHotbar(class_1847.field_8979, class_1847.field_8971, class_1847.field_8998);
                  }

                  if (this.potSlot == -1 && this.fireResistance.getValue() && !mc.field_1724.method_6059(class_1294.field_5918)) {
                     this.potSlot = this.findPotionInHotbar(class_1847.field_8987, class_1847.field_8969);
                  }

                  if (this.potSlot == -1) {
                     this.throwReady = false;
                  } else {
                     RotationUtils.setRotation(new Rotation(mc.field_1724.method_36454(), 80.0F), this.rotationPriority());
                     this.throwReady = true;
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.throwReady && this.potSlot != -1 && mc.field_1724 != null) {
         this.throwReady = false;
         this.oldSlot = mc.field_1724.method_31548().field_7545;
         mc.field_1724.method_31548().field_7545 = this.potSlot;
         if (mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808) instanceof class_9860 success && success.comp_2909() == class_9861.field_52427) {
            PacketUtils.sendPacket(new class_2879(class_1268.field_5808));
         }

         mc.field_1724.method_31548().field_7545 = this.oldSlot;
         mc.field_1761.method_2911();
         this.potSlot = -1;
         this.oldSlot = -1;
         this.restoreTicks = 1;
         this.timer.reset();
      } else {
         this.throwReady = false;
      }
   }

   private int findHealingPotionInHotbar() {
      return this.findPotionInHotbar(class_1847.field_8963, class_1847.field_8980);
   }

   @SafeVarargs
   private int findPotionInHotbar(class_6880<class_1842>... potions) {
      for (int i = 0; i < 9; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.isSplashPotion(stack, potions)) {
            return i;
         }
      }

      return -1;
   }

   @SafeVarargs
   private int findPotionInInventory(class_6880<class_1842>... potions) {
      for (int i = 9; i < 36; i++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(i);
         if (this.isSplashPotion(stack, potions)) {
            return i;
         }
      }

      return -1;
   }

   @SafeVarargs
   private boolean isSplashPotion(class_1799 stack, class_6880<class_1842>... potions) {
      if (!stack.method_7960() && stack.method_7909() == class_1802.field_8436) {
         class_1844 contents = (class_1844)stack.method_57824(class_9334.field_49651);
         if (contents == null) {
            return false;
         } else {
            for (class_6880<class_1842> potion : potions) {
               if (contents.method_57401(potion)) {
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

      this.healingRefillSlot = this.validateRefillSlot(this.healingRefillSlot, class_1847.field_8963, class_1847.field_8980);
      this.speedRefillSlot = this.validateRefillSlot(this.speedRefillSlot, class_1847.field_9005, class_1847.field_8983, class_1847.field_8966);
      this.jumpBoostRefillSlot = this.validateRefillSlot(this.jumpBoostRefillSlot, class_1847.field_8979, class_1847.field_8971, class_1847.field_8998);
      this.fireResRefillSlot = this.validateRefillSlot(this.fireResRefillSlot, class_1847.field_8987, class_1847.field_8969);
      if (this.healing.getValue() && this.findHealingPotionInHotbar() == -1) {
         int invSlot = this.findPotionInInventory(class_1847.field_8963, class_1847.field_8980);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.field_1761.method_2906(0, invSlot, freeSlot, class_1713.field_7791, mc.field_1724);
               this.healingRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.speed.getValue() && this.findPotionInHotbar(class_1847.field_9005, class_1847.field_8983, class_1847.field_8966) == -1) {
         int invSlot = this.findPotionInInventory(class_1847.field_9005, class_1847.field_8983, class_1847.field_8966);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.field_1761.method_2906(0, invSlot, freeSlot, class_1713.field_7791, mc.field_1724);
               this.speedRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.jumpBoost.getValue() && this.findPotionInHotbar(class_1847.field_8979, class_1847.field_8971, class_1847.field_8998) == -1) {
         int invSlot = this.findPotionInInventory(class_1847.field_8979, class_1847.field_8971, class_1847.field_8998);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.field_1761.method_2906(0, invSlot, freeSlot, class_1713.field_7791, mc.field_1724);
               this.jumpBoostRefillSlot = freeSlot;
               return;
            }
         }
      }

      if (this.fireResistance.getValue() && this.findPotionInHotbar(class_1847.field_8987, class_1847.field_8969) == -1) {
         int invSlot = this.findPotionInInventory(class_1847.field_8987, class_1847.field_8969);
         if (invSlot != -1) {
            int freeSlot = this.findFreeHotbarSlot(reservedSlots);
            if (freeSlot != -1) {
               mc.field_1761.method_2906(0, invSlot, freeSlot, class_1713.field_7791, mc.field_1724);
               this.fireResRefillSlot = freeSlot;
            }
         }
      }
   }

   @SafeVarargs
   private int validateRefillSlot(int slot, class_6880<class_1842>... potions) {
      if (slot == -1) {
         return -1;
      } else {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         return this.isSplashPotion(stack, potions) ? slot : -1;
      }
   }

   private int findFreeHotbarSlot(Set<Integer> usedSlots) {
      for (int i = 0; i < 9; i++) {
         if (!usedSlots.contains(i) && mc.field_1724.method_31548().method_5438(i).method_7960()) {
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
