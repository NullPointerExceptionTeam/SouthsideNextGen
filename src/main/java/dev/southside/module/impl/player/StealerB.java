package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.InventoryUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ProjectileItem;
import org.jetbrains.annotations.NotNull;

public class StealerB extends Module {
   private final Slider startDelay = new Slider("Start Delay", 350, 0, 600, 1);
   private final Slider closeDelay = new Slider("Close Delay", 350, 0, 600, 1);
   private final Slider minDelay = new Slider("Min Delay", 80, 0, 200, 1);
   private final Slider maxDelay = new Slider("Max Delay", 100, 0, 200, 1);
   private final Switch mergeSameStacks = new Switch("Merge Same Stacks", false);
   private final Switch pickEnderChest = new Switch("Ender Chest", false);
   private final Switch autoDisable = new Switch("Auto Disable", false);
   private final MSTimer timer3 = new MSTimer();
   private final MSTimer timer = new MSTimer();
   private long nextDelay = 0L;
   private Screen lastTickScreen;
   private static final float WEIGHT_TOOL = 0.1F;
   private static final float WEIGHT_PROJECTILE = 0.2F;
   private static final float WEIGHT_BLOCK = 0.3F;
   private static final float WEIGHT_BLOCK_FULL = 0.33F;
   private static final float WEIGHT_WATER_BUCKET = 0.35F;
   private static final float WEIGHT_GAPPLE = 0.4F;
   private static final float WEIGHT_SWORD = 0.5F;
   private static final float WEIGHT_GAPPLE_LOW_HEALTH = 0.6F;
   private static final float WEIGHT_ARMOR = 0.7F;
   private static final float WEIGHT_CRYSTAL = 2.35F;
   private static final float WEIGHT_EGAPPLE = 4.35F;
   private static final float WEIGHT_GOLD_AXE = 5.5F;
   private static final float WEIGHT_TOTEM = 55.5F;
   private static final int SOURCE_CONTAINER = 0;
   private static final int SOURCE_INVENTORY = 0;
   private static final int TYPE_NORMAL = 0;
   private static final int TYPE_INSTANT = 0;
   private static final int ACTION_THROW = 0;
   private static final int ACTION_SWAP = 1;
   private static final int ACTION_OFFHAND_A = 2;
   private static final int ACTION_OFFHAND_B = 3;
   private static final int ACTION_QUICK_MOVE = 4;
   private static final int ACTION_SWAP_TO_HOTBAR = 5;
   private static final int ACTION_MERGE_HALF = 6;
   private static final int ACTION_MERGE = 7;
   private static final int ACTION_PICKUP_B = 8;
   private static final int ACTION_PICKUP_A = 9;

   public StealerB() {
      super(Category.Player, "StealerB", "打不过zen");
   }

   @Override
   public void initPostRunnable() {
      this.addRangedValueRestrict(this.minDelay, this.maxDelay);
   }

   @Override
   public void onEnable() {
      this.timer.reset();
   }

   @EventHandler(
      priority = 1
   )
   public void onMotion(MotionEvent e) {
      if (e.getState() == Stage.PRE && !this.autoDisableCheck()) {
         ChestManager manager = Southside.get().getChestManager();
         Screen currentScreen = mc.currentScreen;
         if (currentScreen instanceof GenericContainerScreen container) {
            GenericContainerScreenHandler menu = (GenericContainerScreenHandler)container.getScreenHandler();
            if (currentScreen != this.lastTickScreen) {
               this.timer.reset();
               this.nextDelay = (long)this.startDelay.getValue().floatValue();
            } else {
               String chestTitle = container.getTitle().getString();
               String chest = Text.translatable("container.chest").getString();
               String largeChest = Text.translatable("container.chestDouble").getString();
               String enderChest = Text.translatable("container.enderchest").getString();
               if ((
                     chestTitle.equals(chest)
                        || chestTitle.equals(largeChest)
                        || chestTitle.equals("Chest")
                        || this.pickEnderChest.getValue() && chestTitle.equals(enderChest)
                  )
                  && this.timer.hasPassed(this.nextDelay)) {
                  StealerB.PageView<GenericContainerScreenHandler> view = StealerB.PageView.from(menu);
                  view.buildView();
                  StealerB.StealView<GenericContainerScreenHandler> stealView = view.toStealerView();
                  List<StealerB.SlotMove> sort = stealView.solveAction();
                  if (!sort.isEmpty()) {
                     for (StealerB.SlotMove slotMove : sort) {
                        slotMove.apply();
                     }
                  } else if (this.timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                     manager.stolenAll();
                     mc.player.closeHandledScreen();
                     this.timer3.reset();
                  }

                  this.timer.reset();
                  this.scheduleNextDelay();
               }
            }
         }

         this.lastTickScreen = currentScreen;
      }
   }

   private void scheduleNextDelay() {
      long min = Math.min(this.minDelay.getValue().longValue(), this.maxDelay.getValue().longValue());
      long max = Math.max(this.minDelay.getValue().longValue(), this.maxDelay.getValue().longValue());
      if (max < 0L) {
         max = 0L;
      }

      if (min < 0L) {
         min = 0L;
      }

      if (max < min) {
         long t = min;
         min = max;
         max = t;
      }

      this.nextDelay = ThreadLocalRandom.current().nextLong(min, max + 1L);
   }

   public static StealerB.SlotMoveBuilder move() {
      return new StealerB.SlotMoveBuilder();
   }

   private boolean autoDisableCheck() {
      if (this.autoDisable.getValue() && mc.world != null) {
         if (!mc.player.isAlive()) {
            this.setEnable(false);
            return true;
         }

         if (mc.player.age <= 1) {
            this.setEnable(false);
         }
      }

      return false;
   }

   public record ItemStackKey(ItemStack stack) {
      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return o instanceof StealerB.ItemStackKey other ? ItemStack.areItemsAndComponentsEqual(this.stack, other.stack) : false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.stack.getItem(), this.stack.getComponents());
      }
   }

   public record Node(int pos, int slot, ItemStack stack, List<StealerB.Node> sameStacks) {
      public boolean isFull() {
         if (this.stack.getCount() >= this.stack.getMaxCount()) {
            return true;
         } else if (this.sameStacks.isEmpty()) {
            return true;
         } else {
            int non = 0;

            for (StealerB.Node sameStack : this.sameStacks) {
               if (!sameStack.equals(this)) {
                  ItemStack st = sameStack.stack;
                  if (st.getCount() < st.getMaxCount()) {
                     non++;
                  }
               }
            }

            return non == 0;
         }
      }

      public boolean isEmpty() {
         return this.stack == null || this.stack.isEmpty();
      }

      @NotNull
      @Override
      public String toString() {
         return "Node(" + this.pos + ", " + this.slot + ", " + this.stack + ", canMerge = " + !this.isFull() + ")";
      }

      @Override
      public boolean equals(Object o) {
         if (o != null && this.getClass() == o.getClass()) {
            StealerB.Node node = (StealerB.Node)o;
            return this.pos == node.pos && this.slot == node.slot;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.pos, this.slot);
      }
   }

   public static class PageView<T extends ScreenHandler> {
      public final Map<Integer, StealerB.Node> nodes = new LinkedHashMap<>();
      public final int columns;
      public final int rows;
      public final T handler;

      public static StealerB.PageView<GenericContainerScreenHandler> from(GenericContainerScreenHandler container) {
         return new StealerB.PageView<>(9, container.getRows(), container);
      }

      public void buildView() {
         for (Slot slot : this.handler.slots) {
            if (!slot.inventory.equals(StealerB.mc.player.getInventory())) {
               StealerB.Node node = new StealerB.Node(slot.getIndex(), slot.id, slot.getStack(), new LinkedList<>());
               this.nodes.put(node.pos, node);
            }
         }

         Map<StealerB.ItemStackKey, List<StealerB.Node>> groups = new HashMap<>();

         for (StealerB.Node node : this.nodes.values()) {
            StealerB.ItemStackKey key = new StealerB.ItemStackKey(node.stack);
            groups.computeIfAbsent(key, k -> new LinkedList<>()).add(node);
         }

         for (List<StealerB.Node> group : groups.values()) {
            if (group.size() > 1) {
               for (StealerB.Node node : group) {
                  node.sameStacks.addAll(group.stream().filter(n -> n != node).toList());
               }
            }
         }
      }

      public StealerB.StealView<T> toStealerView() {
         return new StealerB.StealView<>(this);
      }

      @Generated
      public PageView(int columns, int rows, T handler) {
         this.columns = columns;
         this.rows = rows;
         this.handler = handler;
      }
   }

   public record SlotMove(int a, int b, int srcA, int srcB, int type, int action, ItemStack stack) {
      public int aToChest() {
         return switch (this.srcA) {
            case 0 -> this.invToChestPos(this.a);
            default -> this.a;
         };
      }

      public int bToChest() {
         return switch (this.srcB) {
            case 0 -> this.invToChestPos(this.a);
            default -> this.a;
         };
      }

      private int invToChestPos(int slot) {
         int handlerSlots = StealerB.mc.player.currentScreenHandler.slots.size();
         return slot < 9 ? handlerSlots - 9 + slot : handlerSlots - 45 + slot;
      }

      public void apply() {
         int syncId = StealerB.mc.player.currentScreenHandler.syncId;
         switch (this.action) {
            case 0:
               StealerB.mc.interactionManager.clickSlot(syncId, this.a, 0, SlotActionType.THROW, StealerB.mc.player);
               break;
            case 1:
               StealerB.mc.interactionManager.clickSlot(syncId, this.b, 8, SlotActionType.SWAP, StealerB.mc.player);
               StealerB.mc.interactionManager.clickSlot(syncId, this.a, 8, SlotActionType.SWAP, StealerB.mc.player);
               break;
            case 2:
               StealerB.mc.interactionManager.clickSlot(syncId, this.a, 40, SlotActionType.SWAP, StealerB.mc.player);
               break;
            case 3:
               StealerB.mc.interactionManager.clickSlot(syncId, this.b, 40, SlotActionType.SWAP, StealerB.mc.player);
               break;
            case 4:
               StealerB.mc.interactionManager.clickSlot(syncId, this.a, 0, SlotActionType.QUICK_MOVE, StealerB.mc.player);
               break;
            case 5:
               StealerB.mc.interactionManager.clickSlot(syncId, this.a, this.b, SlotActionType.SWAP, StealerB.mc.player);
               break;
            case 6:
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP, StealerB.mc.player);
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP_ALL, StealerB.mc.player);
               break;
            case 7:
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP, StealerB.mc.player);
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP_ALL, StealerB.mc.player);
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP, StealerB.mc.player);
               break;
            case 8:
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.b, 0, SlotActionType.PICKUP, StealerB.mc.player);
               break;
            case 9:
               StealerB.mc.interactionManager.clickSlot(StealerB.mc.player.currentScreenHandler.syncId, this.a, 0, SlotActionType.PICKUP, StealerB.mc.player);
         }
      }
   }

   public static class SlotMoveBuilder {
      private int a;
      private int b;
      private int sourceA;
      private int sourceB;
      private int type;
      private int action;
      private ItemStack stack;

      public StealerB.SlotMoveBuilder a(int a) {
         this.a = a;
         return this;
      }

      public StealerB.SlotMoveBuilder b(int b) {
         this.b = b;
         return this;
      }

      public StealerB.SlotMoveBuilder srcA(int sourceA) {
         this.sourceA = sourceA;
         return this;
      }

      public StealerB.SlotMoveBuilder srcB(int sourceB) {
         this.sourceB = sourceB;
         return this;
      }

      public StealerB.SlotMoveBuilder type(int type) {
         this.type = type;
         return this;
      }

      public StealerB.SlotMoveBuilder action(int action) {
         this.action = action;
         return this;
      }

      public StealerB.SlotMoveBuilder stack(ItemStack stack) {
         this.stack = stack;
         return this;
      }

      public StealerB.SlotMove build() {
         return new StealerB.SlotMove(this.a, this.b, this.sourceA, this.sourceB, this.type, this.action, this.stack);
      }
   }

   public static class StealView<T extends ScreenHandler> {
      private final StealerB.PageView<T> page;

      public List<StealerB.SlotMove> solveAction() {
         List<StealerB.Node> nodes = new ArrayList<>(this.page.nodes.values().stream().filter(n -> this.isNodeUseful(n) && this.isBestNode(n)).toList());
         List<StealerB.WeightableNode> weightableNodes = new ArrayList<>();

         for (StealerB.Node node : nodes) {
            float source = 0.0F;
            if (node.isEmpty()) {
               source -= 100.0F;
            }

            ItemStack stack = node.stack;
            Item item = stack.getItem();
            if (item instanceof SwordItem) {
               source += 2.5F;
            } else if (item instanceof MiningToolItem) {
               source += 0.5F;
               if (InventoryUtils.isGodAxe(stack)) {
                  source += 27.5F;
               }
            } else if (item.equals(Items.WATER_BUCKET)) {
               source++;
            } else if (item.equals(Items.GOLDEN_APPLE)) {
               source += 2.0F;
               if (StealerB.mc.player.getHealth() < 10.0F) {
                  source++;
               }
            } else if (item.equals(Items.ENCHANTED_GOLDEN_APPLE)) {
               source += 21.75F;
            } else if (item.equals(Items.END_CRYSTAL)) {
               source += 11.75F;
            } else if (item instanceof ArmorItem) {
               source += 3.5F;
            } else if (item instanceof BlockItem) {
               source++;
               if (node.isFull()) {
                  source += 0.99F;
               }
            } else if (item instanceof ProjectileItem) {
               source++;
            } else if (item.equals(Items.TOTEM_OF_UNDYING)) {
               source += 277.5F;
            }

            weightableNodes.add(new StealerB.WeightableNode(node, source));
         }

         weightableNodes.sort(Comparator.comparingDouble(n -> n.source));
         Collections.reverse(weightableNodes);
         if (weightableNodes.isEmpty()) {
            return List.of();
         } else {
            StealerB.WeightableNode first = (StealerB.WeightableNode)weightableNodes.getFirst();
            int slot = this.getFirstEmptySlot();
            if (slot != -1 && slot + 18 < 54) {
               List<StealerB.SlotMove> moves = new ArrayList<>();
               StealerB.Node node = first.node;
               if (!node.isFull()) {
                  moves.add(StealerB.move().a(node.slot).b(slot).srcA(0).srcB(0).type(0).action(6).stack(node.stack).build());
                  moves.add(StealerB.move().a(node.slot).b(slot).srcA(0).srcB(0).type(0).action(8).stack(node.stack).build());
               } else {
                  moves.add(StealerB.move().a(node.slot).b(slot).srcA(0).srcB(0).type(0).action(4).stack(node.stack).build());
               }

               return moves;
            } else {
               return List.of();
            }
         }
      }

      public int getFirstEmptySlot() {
         for (Slot slot : this.page.handler.slots) {
            if (slot.inventory.equals(StealerB.mc.player.getInventory()) && slot.getStack().isEmpty()) {
               return slot.id;
            }
         }

         return -1;
      }

      private boolean isBestNode(StealerB.Node node) {
         ItemStack stack = node.stack;
         if (!InventoryUtils.isGodItem(stack) && !InventoryUtils.isSharpnessAxe(stack)) {
            for (StealerB.Node otherNode : this.page.nodes.values()) {
               if (!otherNode.equals(node)) {
                  ItemStack checkStack = otherNode.stack;
                  if (stack.getItem() instanceof ArmorItem && checkStack.getItem() instanceof ArmorItem) {
                     float protection = InventoryUtils.getProtection(stack);
                     if (InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) >= protection) {
                        return false;
                     }

                     float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
                     return !(protection < bestArmor);
                  }

                  if (stack.getItem() instanceof SwordItem && checkStack.getItem() instanceof SwordItem) {
                     if (InventoryUtils.getSwordDamage(checkStack) > InventoryUtils.getSwordDamage(stack)) {
                        return false;
                     }
                  } else if (stack.getItem() instanceof PickaxeItem && checkStack.getItem() instanceof PickaxeItem) {
                     if (InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack)) {
                        return false;
                     }
                  } else if (stack.getItem() instanceof AxeItem && checkStack.getItem() instanceof AxeItem) {
                     if (InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack)) {
                        return false;
                     }
                  } else if (stack.getItem() instanceof ShovelItem
                     && checkStack.getItem() instanceof ShovelItem
                     && InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack)) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      }

      public boolean isNodeUseful(StealerB.Node node) {
         ItemStack stack = node.stack;
         if (stack.isEmpty()) {
            return false;
         } else if (stack.getItem().equals(Items.BOOK)
            || stack.getItem().equals(Items.BOOKSHELF)
            || stack.getItem().equals(Items.WRITABLE_BOOK)) {
            return false;
         } else if (InventoryUtils.isGodItem(stack) || InventoryUtils.isSharpnessAxe(stack)) {
            return true;
         } else if (stack.getItem() instanceof ArmorItem) {
            ArmorItem item = (ArmorItem)stack.getItem();
            float protection = InventoryUtils.getProtection(stack);
            float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
            return !(protection <= bestArmor);
         } else if (stack.getItem() instanceof SwordItem) {
            float damage = InventoryUtils.getSwordDamage(stack);
            float bestDamage = InventoryUtils.getBestSwordDamage();
            return !(damage <= bestDamage);
         } else if (stack.getItem() instanceof PickaxeItem) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestPickaxeScore();
            return !(score <= bestScore);
         } else if (stack.getItem() instanceof AxeItem) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestAxeScore();
            return !(score <= bestScore);
         } else if (stack.getItem() instanceof ShovelItem) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestShovelScore();
            return !(score <= bestScore);
         } else if (stack.getItem() instanceof CrossbowItem) {
            float score = InventoryUtils.getCrossbowScore(stack);
            float bestScore = InventoryUtils.getBestCrossbowScore();
            return !(score <= bestScore);
         } else if (stack.getItem() instanceof BowItem && InventoryUtils.isPunchBow(stack)) {
            float score = InventoryUtils.getPunchBowScore(stack);
            float bestScore = InventoryUtils.getBestPunchBowScore();
            return !(score <= bestScore);
         } else if (stack.getItem() instanceof BowItem && InventoryUtils.isPowerBow(stack)) {
            float score = InventoryUtils.getPowerBowScore(stack);
            float bestScore = InventoryUtils.getBestPowerBowScore();
            return !(score <= bestScore);
         } else if (stack.getItem() == Items.COMPASS) {
            return !InventoryUtils.hasItem(stack.getItem());
         } else if (stack.getItem() == Items.WATER_BUCKET && InventoryUtils.getItemCount(Items.WATER_BUCKET) >= InvManagerN.getWaterBucketCount()) {
            return false;
         } else if (stack.getItem() == Items.LAVA_BUCKET && InventoryUtils.getItemCount(Items.LAVA_BUCKET) >= InvManagerN.getLavaBucketCount()) {
            return false;
         } else if (stack.getItem() instanceof BlockItem
            && !InventoryUtils.isInvalidBlock(stack)
            && InventoryUtils.getBlockCountInInventory() + stack.getCount() >= InvManagerN.getMaxBlockSize()) {
            return false;
         } else if (stack.getItem() == Items.ARROW
            && InventoryUtils.getItemCount(Items.ARROW) + stack.getCount() >= InvManagerN.getMaxArrowSize()) {
            return false;
         } else if (stack.getItem() instanceof FishingRodItem && InventoryUtils.getItemCount(Items.FISHING_ROD) >= 1) {
            return false;
         } else {
            return (stack.getItem() == Items.SNOWBALL || stack.getItem() == Items.EGG)
                  && (
                     InventoryUtils.getItemCount(Items.SNOWBALL) + InventoryUtils.getItemCount(Items.EGG) + stack.getCount()
                           >= InvManagerN.getMaxProjectileSize()
                        || !InvManagerN.shouldKeepProjectile()
                  )
               ? false
               : InventoryUtils.isCommonItemUseful(stack);
         }
      }

      @Generated
      public StealView(StealerB.PageView<T> page) {
         this.page = page;
      }
   }

   record WeightableNode(StealerB.Node node, float source) {
   }
}
