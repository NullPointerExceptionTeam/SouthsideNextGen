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
import net.minecraft.class_1304;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1708;
import net.minecraft.class_1713;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1812;
import net.minecraft.class_1829;
import net.minecraft.class_1835;
import net.minecraft.class_3858;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_9334;
import net.minecraft.class_1304.class_1305;

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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getState() != Stage.PRE) {
            if (this.auto_disable.getValue() && mc.field_1687 != null) {
               if (!mc.field_1724.method_5805()) {
                  this.setEnable(false);
                  return;
               }

               if (mc.field_1724.field_6012 <= 1) {
                  this.setEnable(false);
               }
            }

            if (!this.noMove.getValue() || !PlayerUtils.isMoving()) {
               if (mc.field_1755 == null) {
                  this.isOpened = false;
               } else if (mc.field_1724.field_7512 instanceof class_1707
                  || this.stealOtherContainers.getValue() && (mc.field_1724.field_7512 instanceof class_3858 || mc.field_1724.field_7512 instanceof class_1708)
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
      if (mc.field_1755 instanceof class_465 screen) {
         if (mc.field_1755 instanceof class_490 || !this.watch.finished(this.nextClickDelay)) {
            return;
         }

         for (int i = 0; i < this.bestArmorPieces.length; i++) {
            this.bestArmorPieces[i] = -1;
         }

         this.bestSwordSlot = -1;
         if (screen.method_17577() instanceof class_1707
            || this.stealOtherContainers.getValue() && (screen.method_17577() instanceof class_3858 || screen.method_17577() instanceof class_1708)) {
            List<Integer> slots = new ArrayList<>();
            int slotCount = screen.method_17577() instanceof class_1707 container ? container.method_17388() * 9 : screen.method_17577().field_7761.size();

            for (int i = 0; i < slotCount; i++) {
               try {
                  class_1799 itemStack = screen.method_17577().method_7611(i).method_7677();
                  if (!itemStack.method_7960()) {
                     class_1792 item = itemStack.method_7909();
                     if (item instanceof class_1738 armor
                        && InvUtils.isBestArmor(screen.method_17577(), itemStack, this.checkInv.getValue())
                        && this.isStrictlyBetterArmor(itemStack)) {
                        class_1304 armorSlotx = ItemUtils.getEquippableSlotType(itemStack);
                        if (armorSlotx.method_5925() == class_1305.field_6178) {
                           int index = armorSlotx.ordinal();
                           if (index >= 0 && index < this.bestArmorPieces.length) {
                              int pieceSlot = this.bestArmorPieces[index];
                              if (pieceSlot == -1 || i != pieceSlot) {
                                 this.bestArmorPieces[index] = i;
                              }
                           }
                        }
                     }

                     if ((item instanceof class_1829 || item instanceof class_1835)
                        && InvUtils.isBestSword(screen.method_17577(), itemStack, this.checkInv.getValue())
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
                  class_1799 itemStack = screen.method_17577().method_7611(i).method_7677();
                  if ((
                        itemStack.method_7960()
                           || (!(itemStack.method_7909() instanceof class_1738) || this.isStrictlyBetterArmor(itemStack) || this.isBestItem(i))
                              && (
                                 !(itemStack.method_7909() instanceof class_1829) && !(itemStack.method_7909() instanceof class_1835)
                                    || this.isStrictlyBetterSword(itemStack)
                                    || this.isBestItem(i)
                              )
                     )
                     && !itemStack.method_7960()
                     && (this.isItemUseful(screen.method_17577(), itemStack) || this.pickTrash.getValue() || this.isBestItem(i))) {
                     if (this.antiCheatAddition.getValue()) {
                        this.transferSlotAnticheat(screen.method_17577(), i);
                     } else {
                        mc.field_1761.method_2906(screen.method_17577().field_7763, i, 0, class_1713.field_7794, mc.field_1724);
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

            mc.field_1724.method_7346();
         }
      }
   }

   private int getEmptyHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7960()) {
            return i;
         }
      }
      return -1;
   }

   private void transferSlotAnticheat(class_1703 container, int slot) {
      int windowId = container.field_7763;
      if (this.preClick.isMode("Middle")) {
         mc.field_1761.method_2906(windowId, slot, 0, class_1713.field_7796, mc.field_1724);
      }
      int hotbarIndex;
      if (this.transfer.isMode("Number")) {
         hotbarIndex = this.getEmptyHotbarSlot();
         if (hotbarIndex == -1) {
            hotbarIndex = mc.field_1724.method_31548().field_7545;
         }
      } else {
         hotbarIndex = mc.field_1724.method_31548().field_7545;
      }
      mc.field_1761.method_2906(windowId, slot, hotbarIndex, class_1713.field_7791, mc.field_1724);
   }

   private boolean isBestItem(int slot) {
      for (int armorSlot : this.bestArmorPieces) {
         if (armorSlot == slot) {
            return true;
         }
      }

      return slot == this.bestSwordSlot;
   }

   private boolean isItemUseful(class_1703 containerMenu, class_1799 itemStack) {
      class_1792 item = itemStack.method_7909();
      return item == class_1802.field_8288
         || item == class_1802.field_8777
         || item == class_1802.field_8301
         || item == class_1802.field_8814
         || item == class_1802.field_8626
         || item == class_1802.field_8107
         || item == class_1802.field_8634
         || item == class_1802.field_8251
         || item == class_1802.field_8833
         || item == class_1802.field_8378
         || item == class_1802.field_8543
         || item == class_1802.field_8803
         || item instanceof class_1743
         || item instanceof class_1810
         || item instanceof class_1753
         || item instanceof class_1764
         || item instanceof class_1812
         || item instanceof class_1747
         || item == class_1802.field_8705
         || (
            this.onlyGapples.getValue()
               ? item == class_1802.field_8463 || item == class_1802.field_8367
               : itemStack.method_57824(class_9334.field_50075) != null
         );
   }

   private boolean isStrictlyBetterArmor(class_1799 stack) {
      if (stack.method_7909() instanceof class_1738 armor) {
         double var7 = InvUtils.getDamageReduction(stack);
         double bestOwned = this.getBestArmorReductionOwned(ItemUtils.getEquippableSlotType(stack));
         return var7 > bestOwned + 1.0E-6;
      } else {
         return false;
      }
   }

   private double getBestArmorReductionOwned(class_1304 slotType) {
      if (mc.field_1724 == null) {
         return -1.0;
      } else {
         double best = -1.0;
         class_1799 equipped = mc.field_1724.method_6118(slotType);
         if (!equipped.method_7960() && equipped.method_7909() instanceof class_1738) {
            best = Math.max(best, InvUtils.getDamageReduction(equipped));
         }

         for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 inv = mc.field_1724.method_31548().method_5438(i);
            if (!inv.method_7960() && inv.method_7909() instanceof class_1738 armor && ItemUtils.getEquippableSlotType(inv) == slotType) {
               best = Math.max(best, InvUtils.getDamageReduction(inv));
            }
         }

         return best;
      }
   }

   private boolean isStrictlyBetterSword(class_1799 stack) {
      if (!(stack.method_7909() instanceof class_1829) && !(stack.method_7909() instanceof class_1835)) {
         return false;
      } else {
         double dmg = InvUtils.getItemDamage(stack);
         double bestOwned = this.getBestSwordDamageOwned();
         return dmg > bestOwned + 1.0E-6;
      }
   }

   private double getBestSwordDamageOwned() {
      if (mc.field_1724 == null) {
         return -1.0;
      } else {
         double best = -1.0;

         for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
            class_1799 inv = mc.field_1724.method_31548().method_5438(i);
            if (!inv.method_7960() && (inv.method_7909() instanceof class_1829 || inv.method_7909() instanceof class_1835)) {
               best = Math.max(best, InvUtils.getItemDamage(inv));
            }
         }

         return best;
      }
   }
}
