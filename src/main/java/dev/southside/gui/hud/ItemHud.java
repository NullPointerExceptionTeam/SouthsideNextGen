package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.player.SlotUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class ItemHud extends Hud {
   public final Switch showDiamond = new Switch("Diamond", true);
   public final Switch showEmerald = new Switch("Emerald", true);
   public final Switch showIron = new Switch("Iron Ingot", true);
   public final Switch showGold = new Switch("Gold Ingot", true);
   public final Switch showMushroomStew = new Switch("Mushroom Stew", true);
   public final Switch showBlocks = new Switch("Scaffold Blocks", true);
   public final Switch showGodItems = new Switch("God Items", false);
   public final Switch hideZeroCount = new Switch("Hide Zero Count", false);

   public ItemHud(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (mc.field_1772 != null) {
         if (this.optionsReady()) {
            List<ItemHud.ItemEntry> entries = this.buildEntries(example);
            if (!entries.isEmpty()) {
               float baseWidth = this.getBaseWidth(entries);
               float baseHeight = this.getBaseHeight(entries);
               int padding = 5;
               int rowHeight = 20;
               int iconSize = 16;
               int iconGap = 6;
               int panelColor = 1711276032;
               int textColor = -1;
               int countColor = -8064;
               class_4587 matrix = context.method_51448();
               matrix.method_22903();
               matrix.method_46416(x, y, 0.0F);
               matrix.method_22905(scale, scale, 1.0F);
               context.method_25294(0, 0, Math.round(baseWidth), Math.round(baseHeight), 1711276032);
               int countMaxWidth = this.getMaxCountWidth(entries);
               int nameX = 27;
               int countLeft = Math.round(baseWidth) - 5 - countMaxWidth;

               for (int i = 0; i < entries.size(); i++) {
                  ItemHud.ItemEntry entry = entries.get(i);
                  int rowY = 5 + i * 20;
                  int iconY = rowY + 2;
                  int textY = rowY + (20 - 9) / 2;
                  context.method_51427(entry.stack(), 5, iconY);
                  context.method_51433(mc.field_1772, entry.name(), nameX, textY, -1, false);
                  int countWidth = mc.field_1772.method_1727(entry.countText());
                  int countX = countLeft + (countMaxWidth - countWidth);
                  context.method_51433(mc.field_1772, entry.countText(), countX, textY, -8064, false);
               }

               matrix.method_22909();
            }
         }
      }
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      if (mc.field_1772 == null) {
         return 140.0F * scale;
      } else {
         return !this.optionsReady() ? 180.0F * scale : this.getBaseWidth(this.buildEntries(example)) * scale;
      }
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      if (mc.field_1772 == null) {
         return 20.0F * scale;
      } else {
         return !this.optionsReady() ? 70.0F * scale : this.getBaseHeight(this.buildEntries(example)) * scale;
      }
   }

   private List<ItemHud.ItemEntry> buildEntries(boolean example) {
      List<ItemHud.ItemEntry> entries = new ArrayList<>();
      if (!this.optionsReady()) {
         return this.defaultEntries(example);
      } else {
         if (this.showDiamond.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8477), "Diamond", this.getCount(class_1802.field_8477, example), example);
         }

         if (this.showEmerald.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8687), "Emerald", this.getCount(class_1802.field_8687, example), example);
         }

         if (this.showIron.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8620), "Iron", this.getCount(class_1802.field_8620, example), example);
         }

         if (this.showGold.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8695), "Gold", this.getCount(class_1802.field_8695, example), example);
         }

         if (this.showMushroomStew.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8208), "Stew", this.getCount(class_1802.field_8208, example), example);
         }

         if (this.showBlocks.getValue()) {
            this.addEntry(entries, this.getBlockDisplayStack(example), "Blocks", this.getScaffoldBlockCount(example), example);
         }

         if (this.showGodItems.getValue()) {
            this.addEntry(entries, new class_1799(class_1802.field_8825), "God Axe", this.getGodAxeCount(example), example);
            this.addEntry(entries, new class_1799(class_1802.field_49814), "Mace", this.getCount(class_1802.field_49814, example), example);
            this.addEntry(entries, new class_1799(class_1802.field_8367), "E-GApple", this.getCount(class_1802.field_8367, example), example);
            this.addEntry(entries, new class_1799(class_1802.field_8301), "Crystal", this.getCount(class_1802.field_8301, example), example);
         }

         return entries;
      }
   }

   private void addEntry(List<ItemHud.ItemEntry> entries, class_1799 stack, String name, int count, boolean example) {
      if (example || !this.hideZeroCount.getValue() || count > 0) {
         entries.add(new ItemHud.ItemEntry(stack, name, count));
      }
   }

   private List<ItemHud.ItemEntry> defaultEntries(boolean example) {
      List<ItemHud.ItemEntry> entries = new ArrayList<>();
      this.addEntry(entries, new class_1799(class_1802.field_8477), "Diamond", this.getCount(class_1802.field_8477, example), example);
      this.addEntry(entries, new class_1799(class_1802.field_8687), "Emerald", this.getCount(class_1802.field_8687, example), example);
      this.addEntry(entries, this.getBlockDisplayStack(example), "Blocks", this.getScaffoldBlockCount(example), example);
      return entries;
   }

   private boolean optionsReady() {
      return this.showDiamond != null
         && this.showEmerald != null
         && this.showIron != null
         && this.showGold != null
         && this.showMushroomStew != null
         && this.showBlocks != null
         && this.showGodItems != null
         && this.hideZeroCount != null;
   }

   private int getCount(class_1792 item, boolean example) {
      if (!example && mc.field_1724 != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7909() == item) {
               total += stack.method_7947();
            }
         }

         class_1799 offhand = mc.field_1724.method_6079();
         if (!offhand.method_7960() && offhand.method_7909() == item) {
            total += offhand.method_7947();
         }

         return total;
      } else {
         return 64;
      }
   }

   private int getScaffoldBlockCount(boolean example) {
      if (!example && mc.field_1724 != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (this.isScaffoldValid(stack)) {
               total += stack.method_7947();
            }
         }

         class_1799 offhand = mc.field_1724.method_6079();
         if (this.isScaffoldValid(offhand)) {
            total += offhand.method_7947();
         }

         return total;
      } else {
         return 128;
      }
   }

   private int getGodAxeCount(boolean example) {
      if (!example && mc.field_1724 != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (this.isGodAxe(stack)) {
               total += stack.method_7947();
            }
         }

         class_1799 offhand = mc.field_1724.method_6079();
         if (this.isGodAxe(offhand)) {
            total += offhand.method_7947();
         }

         return total;
      } else {
         return 1;
      }
   }

   private boolean isGodAxe(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         return !stack.method_7909().equals(class_1802.field_8825) ? false : stack.method_57353() != null && stack.method_57353().toString().contains("一刀999!");
      } else {
         return false;
      }
   }

   private class_1799 getBlockDisplayStack(boolean example) {
      if (!example && mc.field_1724 != null) {
         class_1799 main = mc.field_1724.method_6047();
         if (this.isScaffoldValid(main)) {
            return main;
         } else {
            class_1799 offhand = mc.field_1724.method_6079();
            if (this.isScaffoldValid(offhand)) {
               return offhand;
            } else {
               for (int i = 0; i <= 35; i++) {
                  class_1799 stack = mc.field_1724.method_31548().method_5438(i);
                  if (this.isScaffoldValid(stack)) {
                     return stack;
                  }
               }

               return new class_1799(class_1802.field_20391);
            }
         }
      } else {
         return new class_1799(class_1802.field_20391);
      }
   }

   private boolean isScaffoldValid(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         return !(stack.method_7909() instanceof class_1747 blockItem)
            ? false
            : !Scaffold.invalidBlocks.contains(blockItem.method_7711()) && SlotUtils.isGoodForBridging(stack.method_7909());
      } else {
         return false;
      }
   }

   private float getBaseWidth(List<ItemHud.ItemEntry> entries) {
      if (entries.isEmpty()) {
         return 0.0F;
      } else {
         int padding = 5;
         int iconSize = 16;
         int iconGap = 6;
         int centerGap = 10;
         int nameWidth = this.getMaxNameWidth(entries);
         int countWidth = this.getMaxCountWidth(entries);
         return 27 + nameWidth + 10 + countWidth + 5;
      }
   }

   private float getBaseHeight(List<ItemHud.ItemEntry> entries) {
      if (entries.isEmpty()) {
         return 0.0F;
      } else {
         int padding = 5;
         int rowHeight = 20;
         return 10.0F + 20 * entries.size();
      }
   }

   private int getMaxNameWidth(List<ItemHud.ItemEntry> entries) {
      if (mc.field_1772 == null) {
         return 40;
      } else {
         int width = 0;

         for (ItemHud.ItemEntry entry : entries) {
            width = Math.max(width, mc.field_1772.method_1727(entry.name()));
         }

         return width;
      }
   }

   private int getMaxCountWidth(List<ItemHud.ItemEntry> entries) {
      if (mc.field_1772 == null) {
         return 20;
      } else {
         int width = 0;

         for (ItemHud.ItemEntry entry : entries) {
            width = Math.max(width, mc.field_1772.method_1727(entry.countText()));
         }

         return width;
      }
   }

   private record ItemEntry(class_1799 stack, String name, int count) {
      String countText() {
         return String.valueOf(this.count);
      }
   }
}
