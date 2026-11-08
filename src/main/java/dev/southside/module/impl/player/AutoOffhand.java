package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.player.SlotUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_1713;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1847;
import net.minecraft.class_2815;
import net.minecraft.class_490;
import net.minecraft.class_9334;

public class AutoOffhand extends Module {
   private static final List<class_1792> THROWABLE_PRIORITY = Arrays.asList(class_1802.field_8543, class_1802.field_8803);
   private final Slider minHealth = new Slider("Min Health", 8.0F, 1.0F, 24.0F, 0.5F);
   private final Dropdown healItem = new Dropdown("Heal Item", "Golden Apple", "Golden Apple", "Healing Potion");
   private final Switch bedwars = new Switch("Bedwars", false);
   private int swapCooldown;
   private boolean needStopSprint;

   public AutoOffhand() {
      super(Category.Player, "AutoOffhand", "自动副手");
   }

   @Override
   public void onDisable() {
      this.swapCooldown = 0;
      this.needStopSprint = false;
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() == Stage.POST) {
         this.needStopSprint = false;
      }
   }

   @EventHandler
   private void onMoveInput(MovementInputEvent event) {
      if (this.needStopSprint && mc.field_1724 != null) {
         if (!mc.field_1690.field_1903.method_1434() || !mc.field_1724.method_24828()) {
            mc.field_1690.field_1867.method_23481(false);
            mc.field_1724.method_5728(false);
            event.setSprint(false);
         }
      }
   }

   @EventHandler
   private void onMotion(MotionEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.field_1724 != null && mc.field_1687 != null && mc.method_1562() != null) {
            if (!mc.field_1724.method_29504()) {
               if (!(mc.field_1755 instanceof class_490)) {
                  if (mc.field_1724.method_6115() || mc.field_1724.field_6252 || mc.field_1690.field_1904.method_1434()) {
                     this.swapCooldown = 4;
                  } else if (this.swapCooldown > 0) {
                     this.swapCooldown--;
                  } else {
                     class_1799 offhandStack = mc.field_1724.method_6079();
                     double health = mc.field_1724.method_6032() + mc.field_1724.method_6067();
                     if (health <= this.minHealth.getValue().doubleValue()) {
                        int healSlot = this.findHealItemSlot();
                        if (healSlot != -1 && !this.isSelectedHealItem(offhandStack)) {
                           this.silentSwapOffhand(healSlot);
                        }
                     } else {
                        if (this.bedwars.getValue()) {
                           int blockSlot = this.findBlockSlot();
                           if (blockSlot != -1) {
                              if (!this.isBridgeBlock(offhandStack)) {
                                 this.silentSwapOffhand(blockSlot);
                              }

                              return;
                           }

                           if (this.isBridgeBlock(offhandStack)) {
                              return;
                           }
                        }

                        boolean hasProjectileOffhand = this.isProjectile(offhandStack);
                        int projectileSlot = this.findProjectileSlot();
                        if (projectileSlot != -1) {
                           if (!hasProjectileOffhand) {
                              this.silentSwapOffhand(projectileSlot);
                           }
                        } else if (!hasProjectileOffhand) {
                           int healSlot = this.findHealItemSlot();
                           if (healSlot != -1 && !this.isSelectedHealItem(offhandStack)) {
                              this.silentSwapOffhand(healSlot);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private int findHealItemSlot() {
      return this.healItem.isMode("Healing Potion") ? this.findHealingPotionSlot() : this.findGappleSlot();
   }

   private boolean isSelectedHealItem(class_1799 stack) {
      return this.healItem.isMode("Healing Potion") ? this.isHealingPotion(stack) : this.isGapple(stack);
   }

   private int findGappleSlot() {
      int enchanted = this.findHotbarItem(class_1802.field_8367);
      return enchanted != -1 ? enchanted : this.findHotbarItem(class_1802.field_8463);
   }

   private int findHotbarItem(class_1792 item) {
      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && stack.method_7909() == item) {
            return slot;
         }
      }

      return -1;
   }

   private int findHealingPotionSlot() {
      int bestSlot = -1;
      int bestScore = Integer.MIN_VALUE;

      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (this.isHealingPotion(stack)) {
            int score = this.getHealingPotionScore(stack);
            if (score > bestScore) {
               bestScore = score;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private int findProjectileSlot() {
      class_1799 bestProjectile = InventoryUtils.getBestProjectile();
      if (bestProjectile != null) {
         int slot = InventoryUtils.getItemStackSlot(bestProjectile);
         if (this.isHotbarSlot(slot)) {
            return slot;
         }
      }

      for (class_1792 throwable : THROWABLE_PRIORITY) {
         int slot = this.findHotbarItem(throwable);
         if (slot != -1) {
            return slot;
         }
      }

      return -1;
   }

   private int findBlockSlot() {
      for (int slot = 8; slot >= 0; slot--) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (this.isBridgeBlock(stack)) {
            return slot;
         }
      }

      return -1;
   }

   private boolean isGapple(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         class_1792 item = stack.method_7909();
         return item == class_1802.field_8463 || item == class_1802.field_8367;
      }
   }

   private boolean isProjectile(class_1799 stack) {
      return stack.method_7960() ? false : THROWABLE_PRIORITY.contains(stack.method_7909());
   }

   private boolean isHealingPotion(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         class_1792 item = stack.method_7909();
         if (item != class_1802.field_8574 && item != class_1802.field_8436 && item != class_1802.field_8150) {
            return false;
         } else {
            class_1844 contents = (class_1844)stack.method_57825(class_9334.field_49651, class_1844.field_49274);
            return contents.method_57401(class_1847.field_8963) || contents.method_57401(class_1847.field_8980);
         }
      }
   }

   private int getHealingPotionScore(class_1799 stack) {
      class_1792 item = stack.method_7909();
      int base;
      if (item == class_1802.field_8436) {
         base = 3000;
      } else if (item == class_1802.field_8574) {
         base = 2000;
      } else if (item == class_1802.field_8150) {
         base = 1000;
      } else {
         base = 0;
      }

      return base + stack.method_7947();
   }

   private boolean isBridgeBlock(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else {
         class_1792 item = stack.method_7909();
         return !(item instanceof class_1747) ? false : SlotUtils.isGoodForBridging(item);
      }
   }

   private void silentSwapOffhand(int hotbarSlot) {
      if (this.isHotbarSlot(hotbarSlot)) {
         if (mc.field_1761 != null && mc.field_1724.field_3944 != null) {
            if (!Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
               this.needStopSprint = true;
            }

            if (!mc.field_1724.field_3919) {
               class_1799 targetStack = mc.field_1724.method_31548().method_5438(hotbarSlot);
               class_1799 offhandStack = mc.field_1724.method_6079();
               if (targetStack.method_7960() || !class_1799.method_7984(targetStack, offhandStack)) {
                  int containerSlot = hotbarSlot + 36;
                  mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, containerSlot, 40, class_1713.field_7791, mc.field_1724);
                  mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
                  this.swapCooldown = Math.max(this.swapCooldown, 3);
               }
            }
         }
      }
   }

   private boolean isHotbarSlot(int slot) {
      return slot >= 0 && slot <= 8;
   }
}
