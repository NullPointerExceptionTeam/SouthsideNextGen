package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.utils.player.SlotUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

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
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      if (mc.textRenderer != null) {
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
               MatrixStack matrix = context.getMatrices();
               matrix.push();
               matrix.translate(x, y, 0.0F);
               matrix.scale(scale, scale, 1.0F);
               context.fill(0, 0, Math.round(baseWidth), Math.round(baseHeight), 1711276032);
               int countMaxWidth = this.getMaxCountWidth(entries);
               int nameX = 27;
               int countLeft = Math.round(baseWidth) - 5 - countMaxWidth;

               for (int i = 0; i < entries.size(); i++) {
                  ItemHud.ItemEntry entry = entries.get(i);
                  int rowY = 5 + i * 20;
                  int iconY = rowY + 2;
                  int textY = rowY + (20 - 9) / 2;
                  context.drawItem(entry.stack(), 5, iconY);
                  context.drawText(mc.textRenderer, entry.name(), nameX, textY, -1, false);
                  int countWidth = mc.textRenderer.getWidth(entry.countText());
                  int countX = countLeft + (countMaxWidth - countWidth);
                  context.drawText(mc.textRenderer, entry.countText(), countX, textY, -8064, false);
               }

               matrix.pop();
            }
         }
      }
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      if (mc.textRenderer == null) {
         return 140.0F * scale;
      } else {
         return !this.optionsReady() ? 180.0F * scale : this.getBaseWidth(this.buildEntries(example)) * scale;
      }
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      if (mc.textRenderer == null) {
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
            this.addEntry(entries, new ItemStack(Items.DIAMOND), "Diamond", this.getCount(Items.DIAMOND, example), example);
         }

         if (this.showEmerald.getValue()) {
            this.addEntry(entries, new ItemStack(Items.EMERALD), "Emerald", this.getCount(Items.EMERALD, example), example);
         }

         if (this.showIron.getValue()) {
            this.addEntry(entries, new ItemStack(Items.IRON_INGOT), "Iron", this.getCount(Items.IRON_INGOT, example), example);
         }

         if (this.showGold.getValue()) {
            this.addEntry(entries, new ItemStack(Items.GOLD_INGOT), "Gold", this.getCount(Items.GOLD_INGOT, example), example);
         }

         if (this.showMushroomStew.getValue()) {
            this.addEntry(entries, new ItemStack(Items.MUSHROOM_STEW), "Stew", this.getCount(Items.MUSHROOM_STEW, example), example);
         }

         if (this.showBlocks.getValue()) {
            this.addEntry(entries, this.getBlockDisplayStack(example), "Blocks", this.getScaffoldBlockCount(example), example);
         }

         if (this.showGodItems.getValue()) {
            this.addEntry(entries, new ItemStack(Items.GOLDEN_AXE), "God Axe", this.getGodAxeCount(example), example);
            this.addEntry(entries, new ItemStack(Items.MACE), "Mace", this.getCount(Items.MACE, example), example);
            this.addEntry(entries, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), "E-GApple", this.getCount(Items.ENCHANTED_GOLDEN_APPLE, example), example);
            this.addEntry(entries, new ItemStack(Items.END_CRYSTAL), "Crystal", this.getCount(Items.END_CRYSTAL, example), example);
         }

         return entries;
      }
   }

   private void addEntry(List<ItemHud.ItemEntry> entries, ItemStack stack, String name, int count, boolean example) {
      if (example || !this.hideZeroCount.getValue() || count > 0) {
         entries.add(new ItemHud.ItemEntry(stack, name, count));
      }
   }

   private List<ItemHud.ItemEntry> defaultEntries(boolean example) {
      List<ItemHud.ItemEntry> entries = new ArrayList<>();
      this.addEntry(entries, new ItemStack(Items.DIAMOND), "Diamond", this.getCount(Items.DIAMOND, example), example);
      this.addEntry(entries, new ItemStack(Items.EMERALD), "Emerald", this.getCount(Items.EMERALD, example), example);
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

   private int getCount(Item item, boolean example) {
      if (!example && mc.player != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
               total += stack.getCount();
            }
         }

         ItemStack offhand = mc.player.getOffHandStack();
         if (!offhand.isEmpty() && offhand.getItem() == item) {
            total += offhand.getCount();
         }

         return total;
      } else {
         return 64;
      }
   }

   private int getScaffoldBlockCount(boolean example) {
      if (!example && mc.player != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isScaffoldValid(stack)) {
               total += stack.getCount();
            }
         }

         ItemStack offhand = mc.player.getOffHandStack();
         if (this.isScaffoldValid(offhand)) {
            total += offhand.getCount();
         }

         return total;
      } else {
         return 128;
      }
   }

   private int getGodAxeCount(boolean example) {
      if (!example && mc.player != null) {
         int total = 0;

         for (int i = 0; i <= 35; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isGodAxe(stack)) {
               total += stack.getCount();
            }
         }

         ItemStack offhand = mc.player.getOffHandStack();
         if (this.isGodAxe(offhand)) {
            total += offhand.getCount();
         }

         return total;
      } else {
         return 1;
      }
   }

   private boolean isGodAxe(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         return !stack.getItem().equals(Items.GOLDEN_AXE) ? false : stack.getComponents() != null && stack.getComponents().toString().contains("一刀999!");
      } else {
         return false;
      }
   }

   private ItemStack getBlockDisplayStack(boolean example) {
      if (!example && mc.player != null) {
         ItemStack main = mc.player.getMainHandStack();
         if (this.isScaffoldValid(main)) {
            return main;
         } else {
            ItemStack offhand = mc.player.getOffHandStack();
            if (this.isScaffoldValid(offhand)) {
               return offhand;
            } else {
               for (int i = 0; i <= 35; i++) {
                  ItemStack stack = mc.player.getInventory().getStack(i);
                  if (this.isScaffoldValid(stack)) {
                     return stack;
                  }
               }

               return new ItemStack(Items.STONE);
            }
         }
      } else {
         return new ItemStack(Items.STONE);
      }
   }

   private boolean isScaffoldValid(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         return !(stack.getItem() instanceof BlockItem blockItem)
            ? false
            : !Scaffold.invalidBlocks.contains(blockItem.getBlock()) && SlotUtils.isGoodForBridging(stack.getItem());
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
      if (mc.textRenderer == null) {
         return 40;
      } else {
         int width = 0;

         for (ItemHud.ItemEntry entry : entries) {
            width = Math.max(width, mc.textRenderer.getWidth(entry.name()));
         }

         return width;
      }
   }

   private int getMaxCountWidth(List<ItemHud.ItemEntry> entries) {
      if (mc.textRenderer == null) {
         return 20;
      } else {
         int width = 0;

         for (ItemHud.ItemEntry entry : entries) {
            width = Math.max(width, mc.textRenderer.getWidth(entry.countText()));
         }

         return width;
      }
   }

   private record ItemEntry(ItemStack stack, String name, int count) {
      String countText() {
         return String.valueOf(this.count);
      }
   }
}
