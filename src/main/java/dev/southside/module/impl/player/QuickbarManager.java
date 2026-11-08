package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.InventoryUtils;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.class_1713;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1820;
import net.minecraft.class_1829;
import net.minecraft.class_2815;

public class QuickbarManager extends Module {
   private static final int POST_USE_DELAY_TICKS = 5;
   private final Slider swordSlot = new Slider("Sword Slot", 1.0F, 1.0F, 9.0F, 1.0F);
   private final Slider pickaxeSlot = new Slider("Pickaxe Slot", 2.0F, 1.0F, 9.0F, 1.0F);
   private final Slider axeSlot = new Slider("Axe Slot", 3.0F, 1.0F, 9.0F, 1.0F);
   private final Slider shearsSlot = new Slider("Shears Slot", 4.0F, 1.0F, 9.0F, 1.0F);
   private final Slider goldenAppleSlot = new Slider("Golden Apple Slot", 5.0F, 1.0F, 9.0F, 1.0F);
   private final Slider fireballSlot = new Slider("Fireball Slot", 6.0F, 1.0F, 9.0F, 1.0F);
   private final Slider swapDelay = new Slider("Swap Delay (ms)", 120.0F, 0.0F, 500.0F, 10.0F);
   private final MSTimer swapTimer = new MSTimer();
   private int useCooldownTicks;
   private AutoOffhand cachedAutoOffhand;

   public QuickbarManager() {
      super(Category.Player, "QuickbarManager", "无需打开背包的快捷栏管理");
   }

   @Override
   public void onDisable() {
      this.useCooldownTicks = 0;
   }

   @EventHandler
   private void onMotion(MotionEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.field_1724 != null && mc.field_1687 != null && mc.method_1562() != null) {
            if (mc.field_1755 == null) {
               if (!this.shouldDelayForUse()) {
                  if (!this.shouldPauseForAutoOffhand()) {
                     if (!this.tryEnsureSword()) {
                        if (!this.tryEnsurePickaxe()) {
                           if (!this.tryEnsureAxe()) {
                              if (!this.tryEnsureShears()) {
                                 if (!this.tryEnsureGoldenApple()) {
                                    this.tryEnsureFireball();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldDelayForUse() {
      boolean using = mc.field_1724.method_6115() || mc.field_1724.field_6252 || mc.field_1690.field_1904.method_1434();
      if (using) {
         this.useCooldownTicks = 5;
         return true;
      } else if (this.useCooldownTicks > 0) {
         this.useCooldownTicks--;
         return true;
      } else {
         return false;
      }
   }

   private boolean shouldPauseForAutoOffhand() {
      AutoOffhand autoOffhand = this.getAutoOffhand();
      return autoOffhand != null && autoOffhand.isEnabled() ? isGoldenApple(mc.field_1724.method_6079()) : false;
   }

   private boolean tryEnsureSword() {
      return this.ensureSlot(this.swordSlot, stack -> stack.method_7909() instanceof class_1829, InventoryUtils::getSwordDamage);
   }

   private boolean tryEnsurePickaxe() {
      return this.ensureSlot(this.pickaxeSlot, stack -> stack.method_7909() instanceof class_1810, InventoryUtils::getToolScore);
   }

   private boolean tryEnsureAxe() {
      return this.ensureSlot(this.axeSlot, stack -> stack.method_7909() instanceof class_1743, InventoryUtils::getToolScore);
   }

   private boolean tryEnsureShears() {
      return this.ensureSlot(this.shearsSlot, stack -> stack.method_7909() instanceof class_1820, QuickbarManager::getDurabilityScore);
   }

   private boolean tryEnsureGoldenApple() {
      return this.ensureSlot(this.goldenAppleSlot, QuickbarManager::isGoldenApple, QuickbarManager::getGoldenAppleScore);
   }

   private boolean tryEnsureFireball() {
      return this.ensureSlot(this.fireballSlot, stack -> stack.method_7909() == class_1802.field_8814, stack -> stack.method_7947());
   }

   private boolean ensureSlot(Slider slotSetting, Predicate<class_1799> predicate, ToDoubleFunction<class_1799> priority) {
      int targetSlot = this.sliderToSlot(slotSetting);
      if (!this.isHotbarSlot(targetSlot)) {
         return false;
      } else {
         class_1799 currentStack = mc.field_1724.method_31548().method_5438(targetSlot);
         if (predicate.test(currentStack)) {
            return false;
         } else {
            int bestSlot = this.findBestInventoryMatch(predicate, priority);
            return bestSlot == -1 ? false : this.silentSwapIntoHotbar(bestSlot, targetSlot);
         }
      }
   }

   private int findBestInventoryMatch(Predicate<class_1799> predicate, ToDoubleFunction<class_1799> priority) {
      int bestSlot = -1;
      double bestScore = Double.NEGATIVE_INFINITY;

      for (int slot = 0; slot < mc.field_1724.method_31548().field_7547.size(); slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && predicate.test(stack)) {
            double score = priority.applyAsDouble(stack);
            if (score > bestScore) {
               bestScore = score;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private boolean silentSwapIntoHotbar(int fromInventorySlot, int targetHotbarSlot) {
      if (fromInventorySlot == targetHotbarSlot || !this.isHotbarSlot(targetHotbarSlot)) {
         return false;
      } else if (mc.field_1761 != null && mc.field_1724.field_3944 != null) {
         if (!this.swapTimer.hasPassed(this.swapDelay.getValue().longValue())) {
            return true;
         } else if (mc.field_1724.field_3919) {
            return true;
         } else {
            int sourceContainerSlot = this.toContainerSlotId(fromInventorySlot);
            if (sourceContainerSlot == -1) {
               return false;
            } else {
               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, sourceContainerSlot, targetHotbarSlot, class_1713.field_7791, mc.field_1724);
               mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
               this.swapTimer.reset();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean isHotbarSlot(int slot) {
      return slot >= 0 && slot <= 8;
   }

   private int sliderToSlot(Slider slider) {
      return slider.getValue().intValue() - 1;
   }

   private int toContainerSlotId(int inventorySlot) {
      if (inventorySlot >= 0 && inventorySlot < mc.field_1724.method_31548().field_7547.size()) {
         return inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
      } else {
         return -1;
      }
   }

   private static boolean isGoldenApple(class_1799 stack) {
      return stack.method_7960() ? false : stack.method_7909() == class_1802.field_8463 || stack.method_7909() == class_1802.field_8367;
   }

   private static double getGoldenAppleScore(class_1799 stack) {
      return stack.method_7909() == class_1802.field_8367 ? 1000 + stack.method_7947() : stack.method_7947();
   }

   private static double getDurabilityScore(class_1799 stack) {
      if (stack.method_7960()) {
         return 0.0;
      } else {
         return !stack.method_7963() ? stack.method_7947() : stack.method_7936() - stack.method_7919();
      }
   }

   private AutoOffhand getAutoOffhand() {
      if (this.cachedAutoOffhand == null && Southside.get() != null) {
         try {
            this.cachedAutoOffhand = Southside.get().getModuleManager().get(AutoOffhand.class);
         } catch (Exception var2) {
         }
      }

      return this.cachedAutoOffhand;
   }
}
