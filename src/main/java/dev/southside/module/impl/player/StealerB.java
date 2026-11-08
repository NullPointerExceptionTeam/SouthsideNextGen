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
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1766;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_476;
import net.minecraft.class_9463;
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
   private class_437 lastTickScreen;
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
         class_437 currentScreen = mc.field_1755;
         if (currentScreen instanceof class_476 container) {
            class_1707 menu = (class_1707)container.method_17577();
            if (currentScreen != this.lastTickScreen) {
               this.timer.reset();
               this.nextDelay = (long)this.startDelay.getValue().floatValue();
            } else {
               String chestTitle = container.method_25440().getString();
               String chest = class_2561.method_43471("container.chest").getString();
               String largeChest = class_2561.method_43471("container.chestDouble").getString();
               String enderChest = class_2561.method_43471("container.enderchest").getString();
               if ((
                     chestTitle.equals(chest)
                        || chestTitle.equals(largeChest)
                        || chestTitle.equals("Chest")
                        || this.pickEnderChest.getValue() && chestTitle.equals(enderChest)
                  )
                  && this.timer.hasPassed(this.nextDelay)) {
                  StealerB.PageView<class_1707> view = StealerB.PageView.from(menu);
                  view.buildView();
                  StealerB.StealView<class_1707> stealView = view.toStealerView();
                  List<StealerB.SlotMove> sort = stealView.solveAction();
                  if (!sort.isEmpty()) {
                     for (StealerB.SlotMove slotMove : sort) {
                        slotMove.apply();
                     }
                  } else if (this.timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                     manager.stolenAll();
                     mc.field_1724.method_7346();
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
      if (this.autoDisable.getValue() && mc.field_1687 != null) {
         if (!mc.field_1724.method_5805()) {
            this.setEnable(false);
            return true;
         }

         if (mc.field_1724.field_6012 <= 1) {
            this.setEnable(false);
         }
      }

      return false;
   }

   public record ItemStackKey(class_1799 stack) {
      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return o instanceof StealerB.ItemStackKey other ? class_1799.method_31577(this.stack, other.stack) : false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.stack.method_7909(), this.stack.method_57353());
      }
   }

   public record Node(int pos, int slot, class_1799 stack, List<StealerB.Node> sameStacks) {
      public boolean isFull() {
         if (this.stack.method_7947() >= this.stack.method_7914()) {
            return true;
         } else if (this.sameStacks.isEmpty()) {
            return true;
         } else {
            int non = 0;

            for (StealerB.Node sameStack : this.sameStacks) {
               if (!sameStack.equals(this)) {
                  class_1799 st = sameStack.stack;
                  if (st.method_7947() < st.method_7914()) {
                     non++;
                  }
               }
            }

            return non == 0;
         }
      }

      public boolean isEmpty() {
         return this.stack == null || this.stack.method_7960();
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

   public static class PageView<T extends class_1703> {
      public final Map<Integer, StealerB.Node> nodes = new LinkedHashMap<>();
      public final int columns;
      public final int rows;
      public final T handler;

      public static StealerB.PageView<class_1707> from(class_1707 container) {
         return new StealerB.PageView<>(9, container.method_17388(), container);
      }

      public void buildView() {
         for (class_1735 slot : this.handler.field_7761) {
            if (!slot.field_7871.equals(StealerB.mc.field_1724.method_31548())) {
               StealerB.Node node = new StealerB.Node(slot.method_34266(), slot.field_7874, slot.method_7677(), new LinkedList<>());
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

   public record SlotMove(int a, int b, int srcA, int srcB, int type, int action, class_1799 stack) {
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
         int handlerSlots = StealerB.mc.field_1724.field_7512.field_7761.size();
         return slot < 9 ? handlerSlots - 9 + slot : handlerSlots - 45 + slot;
      }

      public void apply() {
         int syncId = StealerB.mc.field_1724.field_7512.field_7763;
         switch (this.action) {
            case 0:
               StealerB.mc.field_1761.method_2906(syncId, this.a, 0, class_1713.field_7795, StealerB.mc.field_1724);
               break;
            case 1:
               StealerB.mc.field_1761.method_2906(syncId, this.b, 8, class_1713.field_7791, StealerB.mc.field_1724);
               StealerB.mc.field_1761.method_2906(syncId, this.a, 8, class_1713.field_7791, StealerB.mc.field_1724);
               break;
            case 2:
               StealerB.mc.field_1761.method_2906(syncId, this.a, 40, class_1713.field_7791, StealerB.mc.field_1724);
               break;
            case 3:
               StealerB.mc.field_1761.method_2906(syncId, this.b, 40, class_1713.field_7791, StealerB.mc.field_1724);
               break;
            case 4:
               StealerB.mc.field_1761.method_2906(syncId, this.a, 0, class_1713.field_7794, StealerB.mc.field_1724);
               break;
            case 5:
               StealerB.mc.field_1761.method_2906(syncId, this.a, this.b, class_1713.field_7791, StealerB.mc.field_1724);
               break;
            case 6:
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7790, StealerB.mc.field_1724);
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7793, StealerB.mc.field_1724);
               break;
            case 7:
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7790, StealerB.mc.field_1724);
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7793, StealerB.mc.field_1724);
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7790, StealerB.mc.field_1724);
               break;
            case 8:
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.b, 0, class_1713.field_7790, StealerB.mc.field_1724);
               break;
            case 9:
               StealerB.mc.field_1761.method_2906(StealerB.mc.field_1724.field_7512.field_7763, this.a, 0, class_1713.field_7790, StealerB.mc.field_1724);
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
      private class_1799 stack;

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

      public StealerB.SlotMoveBuilder stack(class_1799 stack) {
         this.stack = stack;
         return this;
      }

      public StealerB.SlotMove build() {
         return new StealerB.SlotMove(this.a, this.b, this.sourceA, this.sourceB, this.type, this.action, this.stack);
      }
   }

   public static class StealView<T extends class_1703> {
      private final StealerB.PageView<T> page;

      public List<StealerB.SlotMove> solveAction() {
         List<StealerB.Node> nodes = new ArrayList<>(this.page.nodes.values().stream().filter(n -> this.isNodeUseful(n) && this.isBestNode(n)).toList());
         List<StealerB.WeightableNode> weightableNodes = new ArrayList<>();

         for (StealerB.Node node : nodes) {
            float source = 0.0F;
            if (node.isEmpty()) {
               source -= 100.0F;
            }

            class_1799 stack = node.stack;
            class_1792 item = stack.method_7909();
            if (item instanceof class_1829) {
               source += 2.5F;
            } else if (item instanceof class_1766) {
               source += 0.5F;
               if (InventoryUtils.isGodAxe(stack)) {
                  source += 27.5F;
               }
            } else if (item.equals(class_1802.field_8705)) {
               source++;
            } else if (item.equals(class_1802.field_8463)) {
               source += 2.0F;
               if (StealerB.mc.field_1724.method_6032() < 10.0F) {
                  source++;
               }
            } else if (item.equals(class_1802.field_8367)) {
               source += 21.75F;
            } else if (item.equals(class_1802.field_8301)) {
               source += 11.75F;
            } else if (item instanceof class_1738) {
               source += 3.5F;
            } else if (item instanceof class_1747) {
               source++;
               if (node.isFull()) {
                  source += 0.99F;
               }
            } else if (item instanceof class_9463) {
               source++;
            } else if (item.equals(class_1802.field_8288)) {
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
         for (class_1735 slot : this.page.handler.field_7761) {
            if (slot.field_7871.equals(StealerB.mc.field_1724.method_31548()) && slot.method_7677().method_7960()) {
               return slot.field_7874;
            }
         }

         return -1;
      }

      private boolean isBestNode(StealerB.Node node) {
         class_1799 stack = node.stack;
         if (!InventoryUtils.isGodItem(stack) && !InventoryUtils.isSharpnessAxe(stack)) {
            for (StealerB.Node otherNode : this.page.nodes.values()) {
               if (!otherNode.equals(node)) {
                  class_1799 checkStack = otherNode.stack;
                  if (stack.method_7909() instanceof class_1738 && checkStack.method_7909() instanceof class_1738) {
                     float protection = InventoryUtils.getProtection(stack);
                     if (InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) >= protection) {
                        return false;
                     }

                     float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
                     return !(protection < bestArmor);
                  }

                  if (stack.method_7909() instanceof class_1829 && checkStack.method_7909() instanceof class_1829) {
                     if (InventoryUtils.getSwordDamage(checkStack) > InventoryUtils.getSwordDamage(stack)) {
                        return false;
                     }
                  } else if (stack.method_7909() instanceof class_1810 && checkStack.method_7909() instanceof class_1810) {
                     if (InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack)) {
                        return false;
                     }
                  } else if (stack.method_7909() instanceof class_1743 && checkStack.method_7909() instanceof class_1743) {
                     if (InventoryUtils.getToolScore(checkStack) > InventoryUtils.getToolScore(stack)) {
                        return false;
                     }
                  } else if (stack.method_7909() instanceof class_1821
                     && checkStack.method_7909() instanceof class_1821
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
         class_1799 stack = node.stack;
         if (stack.method_7960()) {
            return false;
         } else if (stack.method_7909().equals(class_1802.field_8529)
            || stack.method_7909().equals(class_1802.field_8536)
            || stack.method_7909().equals(class_1802.field_8674)) {
            return false;
         } else if (InventoryUtils.isGodItem(stack) || InventoryUtils.isSharpnessAxe(stack)) {
            return true;
         } else if (stack.method_7909() instanceof class_1738) {
            class_1738 item = (class_1738)stack.method_7909();
            float protection = InventoryUtils.getProtection(stack);
            float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
            return !(protection <= bestArmor);
         } else if (stack.method_7909() instanceof class_1829) {
            float damage = InventoryUtils.getSwordDamage(stack);
            float bestDamage = InventoryUtils.getBestSwordDamage();
            return !(damage <= bestDamage);
         } else if (stack.method_7909() instanceof class_1810) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestPickaxeScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() instanceof class_1743) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestAxeScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() instanceof class_1821) {
            float score = InventoryUtils.getToolScore(stack);
            float bestScore = InventoryUtils.getBestShovelScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() instanceof class_1764) {
            float score = InventoryUtils.getCrossbowScore(stack);
            float bestScore = InventoryUtils.getBestCrossbowScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() instanceof class_1753 && InventoryUtils.isPunchBow(stack)) {
            float score = InventoryUtils.getPunchBowScore(stack);
            float bestScore = InventoryUtils.getBestPunchBowScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() instanceof class_1753 && InventoryUtils.isPowerBow(stack)) {
            float score = InventoryUtils.getPowerBowScore(stack);
            float bestScore = InventoryUtils.getBestPowerBowScore();
            return !(score <= bestScore);
         } else if (stack.method_7909() == class_1802.field_8251) {
            return !InventoryUtils.hasItem(stack.method_7909());
         } else if (stack.method_7909() == class_1802.field_8705 && InventoryUtils.getItemCount(class_1802.field_8705) >= InvManagerN.getWaterBucketCount()) {
            return false;
         } else if (stack.method_7909() == class_1802.field_8187 && InventoryUtils.getItemCount(class_1802.field_8187) >= InvManagerN.getLavaBucketCount()) {
            return false;
         } else if (stack.method_7909() instanceof class_1747
            && !InventoryUtils.isInvalidBlock(stack)
            && InventoryUtils.getBlockCountInInventory() + stack.method_7947() >= InvManagerN.getMaxBlockSize()) {
            return false;
         } else if (stack.method_7909() == class_1802.field_8107
            && InventoryUtils.getItemCount(class_1802.field_8107) + stack.method_7947() >= InvManagerN.getMaxArrowSize()) {
            return false;
         } else if (stack.method_7909() instanceof class_1787 && InventoryUtils.getItemCount(class_1802.field_8378) >= 1) {
            return false;
         } else {
            return (stack.method_7909() == class_1802.field_8543 || stack.method_7909() == class_1802.field_8803)
                  && (
                     InventoryUtils.getItemCount(class_1802.field_8543) + InventoryUtils.getItemCount(class_1802.field_8803) + stack.method_7947()
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
