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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

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
         if (mc.player != null && mc.world != null && mc.getNetworkHandler() != null) {
            if (mc.currentScreen == null) {
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
      boolean using = mc.player.isUsingItem() || mc.player.handSwinging || mc.options.useKey.isPressed();
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
      return autoOffhand != null && autoOffhand.isEnabled() ? isGoldenApple(mc.player.getOffHandStack()) : false;
   }

   private boolean tryEnsureSword() {
      return this.ensureSlot(this.swordSlot, stack -> stack.getItem() instanceof SwordItem, InventoryUtils::getSwordDamage);
   }

   private boolean tryEnsurePickaxe() {
      return this.ensureSlot(this.pickaxeSlot, stack -> stack.getItem() instanceof PickaxeItem, InventoryUtils::getToolScore);
   }

   private boolean tryEnsureAxe() {
      return this.ensureSlot(this.axeSlot, stack -> stack.getItem() instanceof AxeItem, InventoryUtils::getToolScore);
   }

   private boolean tryEnsureShears() {
      return this.ensureSlot(this.shearsSlot, stack -> stack.getItem() instanceof ShearsItem, QuickbarManager::getDurabilityScore);
   }

   private boolean tryEnsureGoldenApple() {
      return this.ensureSlot(this.goldenAppleSlot, QuickbarManager::isGoldenApple, QuickbarManager::getGoldenAppleScore);
   }

   private boolean tryEnsureFireball() {
      return this.ensureSlot(this.fireballSlot, stack -> stack.getItem() == Items.FIRE_CHARGE, stack -> stack.getCount());
   }

   private boolean ensureSlot(Slider slotSetting, Predicate<ItemStack> predicate, ToDoubleFunction<ItemStack> priority) {
      int targetSlot = this.sliderToSlot(slotSetting);
      if (!this.isHotbarSlot(targetSlot)) {
         return false;
      } else {
         ItemStack currentStack = mc.player.getInventory().getStack(targetSlot);
         if (predicate.test(currentStack)) {
            return false;
         } else {
            int bestSlot = this.findBestInventoryMatch(predicate, priority);
            return bestSlot == -1 ? false : this.silentSwapIntoHotbar(bestSlot, targetSlot);
         }
      }
   }

   private int findBestInventoryMatch(Predicate<ItemStack> predicate, ToDoubleFunction<ItemStack> priority) {
      int bestSlot = -1;
      double bestScore = Double.NEGATIVE_INFINITY;

      for (int slot = 0; slot < mc.player.getInventory().main.size(); slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && predicate.test(stack)) {
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
      } else if (mc.interactionManager != null && mc.player.networkHandler != null) {
         if (!this.swapTimer.hasPassed(this.swapDelay.getValue().longValue())) {
            return true;
         } else if (mc.player.lastSprinting) {
            return true;
         } else {
            int sourceContainerSlot = this.toContainerSlotId(fromInventorySlot);
            if (sourceContainerSlot == -1) {
               return false;
            } else {
               mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, sourceContainerSlot, targetHotbarSlot, SlotActionType.SWAP, mc.player);
               mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
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
      if (inventorySlot >= 0 && inventorySlot < mc.player.getInventory().main.size()) {
         return inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
      } else {
         return -1;
      }
   }

   private static boolean isGoldenApple(ItemStack stack) {
      return stack.isEmpty() ? false : stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
   }

   private static double getGoldenAppleScore(ItemStack stack) {
      return stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE ? 1000 + stack.getCount() : stack.getCount();
   }

   private static double getDurabilityScore(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0.0;
      } else {
         return !stack.isDamageable() ? stack.getCount() : stack.getMaxDamage() - stack.getDamage();
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
