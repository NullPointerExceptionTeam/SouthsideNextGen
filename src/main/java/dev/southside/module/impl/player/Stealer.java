package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.InvUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.timer.StopWatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot.Type;

public class Stealer extends Module {
   private final Slider startDelay = new Slider("Start Delay", 120, 0, 520, 1);
   private final Slider minDelay = new Slider("Min Delay", 30, 0, 1145, 1);
   private final Slider maxDelay = new Slider("Max Delay", 60, 0, 1145, 1);
   private final Switch auto_disable = new Switch("Auto Disable", false);
   private final Switch pickTrash = new Switch("Pick Trash", false);
   private final Switch onlyGapples = new Switch("Only Gold Apples", true);
   private final Switch noMove = new Switch("No Move", false);
   private final Switch random = new Switch("Random", false);
   private final Switch checkInv = new Switch("Check Inv", false);
   private final Switch stealOtherContainers = new Switch("Steal Other Containers", false);
   private final Switch antiCheatAddition = new Switch("AntiCheatAddition", false);
   private final Dropdown preClick = new Dropdown("PreClick", "Middle", "Middle", "None");
   private final Dropdown transfer = new Dropdown("Transfer", "Number", "Number", "Hotbar Swap");
   private final MSTimer startTime = new MSTimer();
   private final StopWatch watch = new StopWatch();
   private boolean isOpened = false;
   private long nextClickDelay = 0L;
   private final int[] bestArmorPieces = new int[]{-1, -1, -1, -1, -1, -1, -1, -1};
   private int bestSwordSlot = -1;

   public Stealer() {
      super(Category.Player, "Stealer", "箱子小偷 自动偷东西");
   }

   @Override
   public void onEnable() {
      this.watch.reset();
      this.isOpened = false;
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.preClick.getLabel(), () -> !this.antiCheatAddition.getValue());
      this.hideIf(this.transfer.getLabel(), () -> !this.antiCheatAddition.getValue());
   }

   @Override
   public void onDisable() {
      this.watch.reset();
      this.isOpened = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getState() != Stage.PRE) {
            if (this.auto_disable.getValue() && mc.world != null) {
               if (!mc.player.isAlive()) {
                  this.setEnable(false);
                  return;
               }

               if (mc.player.age <= 1) {
                  this.setEnable(false);
               }
            }

            if (!this.noMove.getValue() || !PlayerUtils.isMoving()) {
               if (mc.currentScreen == null) {
                  this.isOpened = false;
               } else if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler
                  || this.stealOtherContainers.getValue() && (mc.player.currentScreenHandler instanceof FurnaceScreenHandler || mc.player.currentScreenHandler instanceof BrewingStandScreenHandler)
                  )
                {
                  if (!this.isOpened) {
                     this.startTime.reset();
                     this.isOpened = true;
                  } else {
                     if (this.startTime.hasTimePassed(this.startDelay.getValue().longValue())) {
                        this.take_items();
                     }
                  }
               } else {
                  this.isOpened = false;
               }
            }
         }
      }
   }

   private void take_items() {
      if (mc.currentScreen instanceof HandledScreen screen) {
         if (mc.currentScreen instanceof InventoryScreen || !this.watch.finished(this.nextClickDelay)) {
            return;
         }

         for (int i = 0; i < this.bestArmorPieces.length; i++) {
            this.bestArmorPieces[i] = -1;
         }

         this.bestSwordSlot = -1;
         if (screen.getScreenHandler() instanceof GenericContainerScreenHandler
            || this.stealOtherContainers.getValue() && (screen.getScreenHandler() instanceof FurnaceScreenHandler || screen.getScreenHandler() instanceof BrewingStandScreenHandler)) {
            List<Integer> slots = new ArrayList<>();
            int slotCount = screen.getScreenHandler() instanceof GenericContainerScreenHandler container ? container.getRows() * 9 : screen.getScreenHandler().slots.size();

            for (int i = 0; i < slotCount; i++) {
               try {
                  ItemStack itemStack = screen.getScreenHandler().getSlot(i).getStack();
                  if (!itemStack.isEmpty()) {
                     Item item = itemStack.getItem();
                     if (item instanceof ArmorItem armor
                        && InvUtils.isBestArmor(screen.getScreenHandler(), itemStack, this.checkInv.getValue())
                        && this.isStrictlyBetterArmor(itemStack)) {
                        EquipmentSlot armorSlotx = ItemUtils.getEquippableSlotType(itemStack);
                        if (armorSlotx.getType() == Type.HUMANOID_ARMOR) {
                           int index = armorSlotx.ordinal();
                           if (index >= 0 && index < this.bestArmorPieces.length) {
                              int pieceSlot = this.bestArmorPieces[index];
                              if (pieceSlot == -1 || i != pieceSlot) {
                                 this.bestArmorPieces[index] = i;
                              }
                           }
                        }
                     }

                     if ((item instanceof SwordItem || item instanceof TridentItem)
                        && InvUtils.isBestSword(screen.getScreenHandler(), itemStack, this.checkInv.getValue())
                        && this.isStrictlyBetterSword(itemStack)) {
                        this.bestSwordSlot = i;
                     }
                  }
               } catch (Exception var12) {
               }
            }

            for (int i = 0; i < slotCount; i++) {
               slots.add(i);
            }

            if (this.random.getValue()) {
               Collections.shuffle(slots);
            }

            for (int i : slots) {
               try {
                  ItemStack itemStack = screen.getScreenHandler().getSlot(i).getStack();
                  if ((
                        itemStack.isEmpty()
                           || (!(itemStack.getItem() instanceof ArmorItem) || this.isStrictlyBetterArmor(itemStack) || this.isBestItem(i))
                              && (
                                 !(itemStack.getItem() instanceof SwordItem) && !(itemStack.getItem() instanceof TridentItem)
                                    || this.isStrictlyBetterSword(itemStack)
                                    || this.isBestItem(i)
                              )
                     )
                     && !itemStack.isEmpty()
                     && (this.isItemUseful(screen.getScreenHandler(), itemStack) || this.pickTrash.getValue() || this.isBestItem(i))) {
                     if (this.antiCheatAddition.getValue()) {
                        this.transferSlotAnticheat(screen.getScreenHandler(), i);
                     } else {
                        mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                     }
                     this.watch.reset();
                     this.nextClickDelay = (long)(
                        this.random.getValue()
                           ? RandomUtils.nextFloat(this.minDelay.getDouble(), this.maxDelay.getDouble()) + (float)RandomUtils.nextLong(-35L, 35L)
                           : RandomUtils.nextFloat(this.minDelay.getDouble(), this.maxDelay.getDouble())
                     );
                     return;
                  }
               } catch (Exception var11) {
               }
            }

            mc.player.closeHandledScreen();
         }
      }
   }

   private int getEmptyHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }
      return -1;
   }

   private void transferSlotAnticheat(ScreenHandler container, int slot) {
      int windowId = container.syncId;
      if (this.preClick.isMode("Middle")) {
         mc.interactionManager.clickSlot(windowId, slot, 0, SlotActionType.CLONE, mc.player);
      }
      int hotbarIndex;
      if (this.transfer.isMode("Number")) {
         hotbarIndex = this.getEmptyHotbarSlot();
         if (hotbarIndex == -1) {
            hotbarIndex = mc.player.getInventory().selectedSlot;
         }
      } else {
         hotbarIndex = mc.player.getInventory().selectedSlot;
      }
      mc.interactionManager.clickSlot(windowId, slot, hotbarIndex, SlotActionType.SWAP, mc.player);
   }

   private boolean isBestItem(int slot) {
      for (int armorSlot : this.bestArmorPieces) {
         if (armorSlot == slot) {
            return true;
         }
      }

      return slot == this.bestSwordSlot;
   }

   private boolean isItemUseful(ScreenHandler containerMenu, ItemStack itemStack) {
      Item item = itemStack.getItem();
      return item == Items.TOTEM_OF_UNDYING
         || item == Items.SLIME_BALL
         || item == Items.END_CRYSTAL
         || item == Items.FIRE_CHARGE
         || item == Items.TNT
         || item == Items.ARROW
         || item == Items.ENDER_PEARL
         || item == Items.COMPASS
         || item == Items.ELYTRA
         || item == Items.FISHING_ROD
         || item == Items.SNOWBALL
         || item == Items.EGG
         || item instanceof AxeItem
         || item instanceof PickaxeItem
         || item instanceof BowItem
         || item instanceof CrossbowItem
         || item instanceof PotionItem
         || item instanceof BlockItem
         || item == Items.WATER_BUCKET
         || (
            this.onlyGapples.getValue()
               ? item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE
               : itemStack.get(DataComponentTypes.FOOD) != null
         );
   }

   private boolean isStrictlyBetterArmor(ItemStack stack) {
      if (stack.getItem() instanceof ArmorItem armor) {
         double var7 = InvUtils.getDamageReduction(stack);
         double bestOwned = this.getBestArmorReductionOwned(ItemUtils.getEquippableSlotType(stack));
         return var7 > bestOwned + 1.0E-6;
      } else {
         return false;
      }
   }

   private double getBestArmorReductionOwned(EquipmentSlot slotType) {
      if (mc.player == null) {
         return -1.0;
      } else {
         double best = -1.0;
         ItemStack equipped = mc.player.getEquippedStack(slotType);
         if (!equipped.isEmpty() && equipped.getItem() instanceof ArmorItem) {
            best = Math.max(best, InvUtils.getDamageReduction(equipped));
         }

         for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack inv = mc.player.getInventory().getStack(i);
            if (!inv.isEmpty() && inv.getItem() instanceof ArmorItem armor && ItemUtils.getEquippableSlotType(inv) == slotType) {
               best = Math.max(best, InvUtils.getDamageReduction(inv));
            }
         }

         return best;
      }
   }

   private boolean isStrictlyBetterSword(ItemStack stack) {
      if (!(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof TridentItem)) {
         return false;
      } else {
         double dmg = InvUtils.getItemDamage(stack);
         double bestOwned = this.getBestSwordDamageOwned();
         return dmg > bestOwned + 1.0E-6;
      }
   }

   private double getBestSwordDamageOwned() {
      if (mc.player == null) {
         return -1.0;
      } else {
         double best = -1.0;

         for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack inv = mc.player.getInventory().getStack(i);
            if (!inv.isEmpty() && (inv.getItem() instanceof SwordItem || inv.getItem() instanceof TridentItem)) {
               best = Math.max(best, InvUtils.getItemDamage(inv));
            }
         }

         return best;
      }
   }
}
