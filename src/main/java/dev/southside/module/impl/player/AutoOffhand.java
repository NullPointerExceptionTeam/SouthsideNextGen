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
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potions;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;

public class AutoOffhand extends Module {
   private static final List<Item> THROWABLE_PRIORITY = Arrays.asList(Items.SNOWBALL, Items.EGG);
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
      if (this.needStopSprint && mc.player != null) {
         if (!mc.options.jumpKey.isPressed() || !mc.player.isOnGround()) {
            mc.options.sprintKey.setPressed(false);
            mc.player.setSprinting(false);
            event.setSprint(false);
         }
      }
   }

   @EventHandler
   private void onMotion(MotionEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.player != null && mc.world != null && mc.getNetworkHandler() != null) {
            if (!mc.player.isDead()) {
               if (!(mc.currentScreen instanceof InventoryScreen)) {
                  if (mc.player.isUsingItem() || mc.player.handSwinging || mc.options.useKey.isPressed()) {
                     this.swapCooldown = 4;
                  } else if (this.swapCooldown > 0) {
                     this.swapCooldown--;
                  } else {
                     ItemStack offhandStack = mc.player.getOffHandStack();
                     double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
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

   private boolean isSelectedHealItem(ItemStack stack) {
      return this.healItem.isMode("Healing Potion") ? this.isHealingPotion(stack) : this.isGapple(stack);
   }

   private int findGappleSlot() {
      int enchanted = this.findHotbarItem(Items.ENCHANTED_GOLDEN_APPLE);
      return enchanted != -1 ? enchanted : this.findHotbarItem(Items.GOLDEN_APPLE);
   }

   private int findHotbarItem(Item item) {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && stack.getItem() == item) {
            return slot;
         }
      }

      return -1;
   }

   private int findHealingPotionSlot() {
      int bestSlot = -1;
      int bestScore = Integer.MIN_VALUE;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
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
      ItemStack bestProjectile = InventoryUtils.getBestProjectile();
      if (bestProjectile != null) {
         int slot = InventoryUtils.getItemStackSlot(bestProjectile);
         if (this.isHotbarSlot(slot)) {
            return slot;
         }
      }

      for (Item throwable : THROWABLE_PRIORITY) {
         int slot = this.findHotbarItem(throwable);
         if (slot != -1) {
            return slot;
         }
      }

      return -1;
   }

   private int findBlockSlot() {
      for (int slot = 8; slot >= 0; slot--) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (this.isBridgeBlock(stack)) {
            return slot;
         }
      }

      return -1;
   }

   private boolean isGapple(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
      }
   }

   private boolean isProjectile(ItemStack stack) {
      return stack.isEmpty() ? false : THROWABLE_PRIORITY.contains(stack.getItem());
   }

   private boolean isHealingPotion(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return false;
         } else {
            PotionContentsComponent contents = (PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            return contents.matches(Potions.HEALING) || contents.matches(Potions.STRONG_HEALING);
         }
      }
   }

   private int getHealingPotionScore(ItemStack stack) {
      Item item = stack.getItem();
      int base;
      if (item == Items.SPLASH_POTION) {
         base = 3000;
      } else if (item == Items.POTION) {
         base = 2000;
      } else if (item == Items.LINGERING_POTION) {
         base = 1000;
      } else {
         base = 0;
      }

      return base + stack.getCount();
   }

   private boolean isBridgeBlock(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         return !(item instanceof BlockItem) ? false : SlotUtils.isGoodForBridging(item);
      }
   }

   private void silentSwapOffhand(int hotbarSlot) {
      if (this.isHotbarSlot(hotbarSlot)) {
         if (mc.interactionManager != null && mc.player.networkHandler != null) {
            if (!Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
               this.needStopSprint = true;
            }

            if (!mc.player.lastSprinting) {
               ItemStack targetStack = mc.player.getInventory().getStack(hotbarSlot);
               ItemStack offhandStack = mc.player.getOffHandStack();
               if (targetStack.isEmpty() || !ItemStack.areItemsEqual(targetStack, offhandStack)) {
                  int containerSlot = hotbarSlot + 36;
                  mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, containerSlot, 40, SlotActionType.SWAP, mc.player);
                  mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
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
