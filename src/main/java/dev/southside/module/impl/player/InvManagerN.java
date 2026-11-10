package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.player.PlayerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.tuple.Pair;

public class InvManagerN extends Module {
   public static final MSTimer timer = new MSTimer();
   public final Slider delay = new Slider("Delay", 50.0F, 0.0F, 200.0F, 1.0F);
   public final Dropdown offhandItems = new Dropdown("Offhand Items", "None", "Golden Apple", "Projectile", "Fishing Rod", "Block", "None");
   public final Switch autoArmor = new Switch("AutoArmor", true);
   public final Switch inventoryOnly = new Switch("Inventory Only", true);
   public final Switch noMove = new Switch("No Move", false);
   public final Switch switchSword = new Switch("Switch Sword", true);
   public final Switch switchBlock = new Switch("Switch Block", true);
   public final Switch switchPickaxe = new Switch("Switch Pickaxe", true);
   public final Switch switchAxe = new Switch("Switch Axe", true);
   public final Switch switchBow = new Switch("Switch Bow or Crossbow", true);
   public final Dropdown preferBow = new Dropdown("Bow Priority", "Crossbow", "Power Bow", "Punch Bow", "Crossbow");
   public final Switch switchWaterBucket = new Switch("Switch Water Bucket", true);
   public final Switch switchEnderPearl = new Switch("Switch Ender Pearl", true);
   public final Switch switchFireball = new Switch("Switch Fireball", true);
   public final Switch switchGoldenApple = new Switch("Switch Golden Apple", true);
   public final Switch throwItems = new Switch("Throw Items", true);
   public static final Switch keepProjectile = new Switch("Keep Eggs & Snowballs", true);
   public final Switch switchProjectile = new Switch("Switch Eggs & Snowballs", false);
   public final Switch switchRod = new Switch("Switch Rod", false);
   public final Switch autoDisable = new Switch("Auto Disable", false);
   public final Slider swordSlot = new Slider("Sword Slot", 1.0F, 1.0F, 9.0F, 1.0F);
   public final Slider blockSlot = new Slider("Block Slot", 2.0F, 1.0F, 9.0F, 1.0F);
   public static final Slider maxBlockSize = new Slider("Max Block Size", 256.0F, 64.0F, 1024.0F, 64.0F);
   public final Slider pickaxeSlot = new Slider("Pickaxe Slot", 3.0F, 1.0F, 9.0F, 1.0F);
   public final Slider axeSlot = new Slider("Axe Slot", 4.0F, 1.0F, 9.0F, 1.0F);
   public final Slider bowSlot = new Slider("Bow Slot", 5.0F, 1.0F, 9.0F, 1.0F);
   public static final Slider maxArrowSize = new Slider("Max Arrow Size", 256.0F, 64.0F, 512.0F, 64.0F);
   public final Slider waterBucketSlot = new Slider("Water Bucket Slot", 6.0F, 1.0F, 9.0F, 1.0F);
   public final Slider enderPearlSlot = new Slider("Ender Pearl Slot", 7.0F, 1.0F, 9.0F, 1.0F);
   public final Slider fireballSlot = new Slider("Fireball Slot", 8.0F, 1.0F, 9.0F, 1.0F);
   public final Slider goldenAppleSlot = new Slider("Golden Apple Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   public static final Slider waterBucketCount = new Slider("Keep Water Buckets", 1.0F, 0.0F, 5.0F, 1.0F);
   public static final Slider lavaBucketCount = new Slider("Keep Lava Buckets", 1.0F, 0.0F, 5.0F, 1.0F);
   public static final Slider cobwebCount = new Slider("Keep Cobweb", 1.0F, 0.0F, 10.0F, 1.0F);
   public final Slider projectileSlot = new Slider("Eggs & Snowballs Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   public static final Slider maxProjectileSize = new Slider("Max Eggs & Snowballs Size", 64.0F, 16.0F, 256.0F, 16.0F);
   public final Slider rodSlot = new Slider("Rod Slot", 9.0F, 1.0F, 9.0F, 1.0F);
   int noMoveTicks = 0;
   private boolean clickOffHand = false;
   private boolean inventoryOpen = false;
   private int usingItemTicks = 0;
   private boolean wasUsingItem = false;
   private boolean microStopPending = false;
   private boolean prepZeroed = false;
   private int burstState = 0;
   private int burstAccum = 0;
   private int zeroInputTicks = 0;
   public static boolean active = false;
   private static boolean needStopSprint;
   private int mergeSrc = -1;
   private int mergeDst = -1;

   public InvManagerN() {
      super(Category.Player, "InvManager", "欣欣哥！");
   }

   public static boolean shouldKeepProjectile() {
      return keepProjectile.getValue();
   }

   public static int getMaxProjectileSize() {
      return maxProjectileSize.getValue().intValue();
   }

   public static int getMaxArrowSize() {
      return maxArrowSize.getValue().intValue();
   }

   public static int getMaxBlockSize() {
      return maxBlockSize.getValue().intValue();
   }

   public static int getLavaBucketCount() {
      return lavaBucketCount.getValue().intValue();
   }

   public static int getWaterBucketCount() {
      return waterBucketCount.getValue().intValue();
   }

   public static int getCobwebCount() {
      return cobwebCount.getValue().intValue();
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.noMove.getLabel(), () -> this.inventoryOnly.getValue());
      this.hideIf(this.switchBlock.getLabel(), () -> this.offhandItems.isMode("Block"));
      this.hideIf(this.blockSlot.getLabel(), () -> !this.switchBlock.getValue() || this.offhandItems.isMode("Block"));
      this.hideIf(maxBlockSize.getLabel(), () -> !this.switchBlock.getValue());
      this.hideIf(this.pickaxeSlot.getLabel(), () -> !this.switchPickaxe.getValue());
      this.hideIf(this.axeSlot.getLabel(), () -> !this.switchAxe.getValue());
      this.hideIf(this.bowSlot.getLabel(), () -> !this.switchBow.getValue());
      this.hideIf(this.preferBow.getLabel(), () -> !this.switchBow.getValue());
      this.hideIf(maxArrowSize.getLabel(), () -> !this.switchBow.getValue());
      this.hideIf(this.waterBucketSlot.getLabel(), () -> !this.switchWaterBucket.getValue());
      this.hideIf(this.enderPearlSlot.getLabel(), () -> !this.switchEnderPearl.getValue());
      this.hideIf(this.fireballSlot.getLabel(), () -> !this.switchFireball.getValue());
      this.hideIf(this.switchGoldenApple.getLabel(), () -> this.offhandItems.isMode("Golden Apple"));
      this.hideIf(this.goldenAppleSlot.getLabel(), () -> !this.switchGoldenApple.getValue() || this.offhandItems.isMode("Golden Apple"));
      this.hideIf(waterBucketCount.getLabel(), () -> !this.throwItems.getValue());
      this.hideIf(lavaBucketCount.getLabel(), () -> !this.throwItems.getValue());
      this.hideIf(cobwebCount.getLabel(), () -> !this.throwItems.getValue());
      this.hideIf(this.switchProjectile.getLabel(), () -> !keepProjectile.getValue() || this.offhandItems.isMode("Projectile"));
      this.hideIf(
         this.projectileSlot.getLabel(), () -> !this.switchProjectile.getValue() || !keepProjectile.getValue() || this.offhandItems.isMode("Projectile")
      );
      this.hideIf(maxProjectileSize.getLabel(), () -> !keepProjectile.getValue());
      this.hideIf(this.switchRod.getLabel(), () -> this.offhandItems.isMode("Fishing Rod"));
      this.hideIf(this.rodSlot.getLabel(), () -> !this.switchRod.getValue() || this.offhandItems.isMode("Fishing Rod"));
   }

   public static boolean isItemUseful(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else if (stack.getItem().equals(Items.BOOK)
         || stack.getItem().equals(Items.BOOKSHELF)
         || stack.getItem().equals(Items.WRITABLE_BOOK)) {
         return false;
      } else if (InventoryUtils.isGodItem(stack)) {
         return true;
      } else if (stack.getName().getString().contains("点击使用")) {
         return false;
      } else if (stack.getItem() instanceof ArmorItem) {
         float protection = InventoryUtils.getProtection(stack);
         if (InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) >= protection) {
            return false;
         } else {
            float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
            return !(protection < bestArmor);
         }
      } else if (stack.getItem() instanceof SwordItem) {
         return InventoryUtils.getBestSword() == stack;
      } else if (stack.getItem() instanceof PickaxeItem) {
         return InventoryUtils.getBestPickaxe() == stack;
      } else if (stack.getItem() instanceof AxeItem && !InventoryUtils.isSharpnessAxe(stack)) {
         return InventoryUtils.getBestAxe() == stack;
      } else if (stack.getItem() instanceof ShovelItem) {
         return InventoryUtils.getBestShovel() == stack;
      } else if (stack.getItem() instanceof HoeItem) {
         return false;
      } else if (stack.getItem() instanceof CrossbowItem) {
         return InventoryUtils.getBestCrossbow() == stack;
      } else if (stack.getItem() instanceof BowItem) {
         if (InventoryUtils.isPunchBow(stack) && InventoryUtils.getBestPunchBow() == stack) {
            return true;
         } else if (InventoryUtils.isPowerBow(stack) && InventoryUtils.getBestPowerBow() == stack) {
            return true;
         } else {
            return InventoryUtils.isFlameBow(stack) && InventoryUtils.getBestFlameBow() == stack
               ? true
               : InventoryUtils.getItemCount(Items.BOW) <= 1;
         }
      } else if (stack.getItem() == Items.WATER_BUCKET && InventoryUtils.getItemCount(Items.WATER_BUCKET) > waterBucketCount.getValue().intValue()) {
         return false;
      } else if (stack.getItem() == Items.LAVA_BUCKET && InventoryUtils.getItemCount(Items.LAVA_BUCKET) > lavaBucketCount.getValue().intValue()) {
         return false;
      } else if (stack.getItem() == Items.COBWEB) {
         return InventoryUtils.getItemCount(Items.COBWEB) <= 64;
      } else if (stack.getItem() instanceof FishingRodItem && InventoryUtils.getItemCount(Items.FISHING_ROD) > 1) {
         return false;
      } else if ((stack.getItem() == Items.SNOWBALL || stack.getItem() == Items.EGG) && !keepProjectile.getValue()) {
         return false;
      } else if (stack.getItem() == Items.TNT) {
         return true;
      } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock) {
         return false;
      } else if (stack.getItem() == Items.PRISMARINE_SHARD) {
         return false;
      } else if (stack.getItem() != Items.GRAVEL
         && stack.getItem() != Items.FLINT
         && stack.getItem() != Items.SAND
         && stack.getItem() != Items.RED_SAND) {
         if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FlowerBlock) {
            return false;
         } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PressurePlateBlock) {
            return false;
         } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof StairsBlock) {
            return false;
         } else if (stack.getItem() != Items.WHEAT
            && stack.getItem() != Items.CARROT
            && stack.getItem() != Items.STRING) {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FenceBlock) {
               return false;
            } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FenceGateBlock) {
               return false;
            } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ButtonBlock) {
               return false;
            } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CarpetBlock) {
               return false;
            } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ConcretePowderBlock) {
               return false;
            } else if (stack.getItem() != Items.OAK_SAPLING
               && stack.getItem() != Items.SPRUCE_SAPLING
               && stack.getItem() != Items.BIRCH_SAPLING
               && stack.getItem() != Items.JUNGLE_SAPLING
               && stack.getItem() != Items.ACACIA_SAPLING
               && stack.getItem() != Items.DARK_OAK_SAPLING
               && stack.getItem() != Items.CHERRY_SAPLING
               && stack.getItem() != Items.MANGROVE_PROPAGULE) {
               if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CactusBlock) {
                  return false;
               } else if (stack.getItem() instanceof BlockItem blockItem
                  && Registries.BLOCK.getId(blockItem.getBlock()).getPath().contains("terracotta")) {
                  String path = Registries.BLOCK.getId(blockItem.getBlock()).getPath();
                  return path.equals("blue_terracotta") || path.equals("red_terracotta");
               } else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TrapdoorBlock) {
                  return false;
               } else {
                  return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LadderBlock
                     ? false
                     : InventoryUtils.isCommonItemUseful(stack);
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState().equals(Stage.POST)) {
         active = false;
         needStopSprint = false;
      }
   }

   @EventHandler
   public void onPacket(PacketEvent e) {
      if (e.getOrigin() == TransferOrigin.SEND) {
      }
   }

   public boolean checkConfig() {
      List<Pair<Boolean, Float>> pairs = new ArrayList<>();
      if (!keepProjectile.getValue()) {
         this.switchProjectile.setValue(false);
      }

      pairs.add(Pair.of(this.switchSword.getValue(), this.swordSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchPickaxe.getValue(), this.pickaxeSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchAxe.getValue(), this.axeSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchBow.getValue(), this.bowSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchWaterBucket.getValue(), this.waterBucketSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchEnderPearl.getValue(), this.enderPearlSlot.getValue().floatValue()));
      pairs.add(Pair.of(this.switchFireball.getValue(), this.fireballSlot.getValue().floatValue()));
      if (!this.offhandItems.isMode("Golden Apple")) {
         pairs.add(Pair.of(this.switchGoldenApple.getValue(), this.goldenAppleSlot.getValue().floatValue()));
      }

      if (!this.offhandItems.isMode("Projectile")) {
         pairs.add(Pair.of(this.switchProjectile.getValue(), this.projectileSlot.getValue().floatValue()));
      }

      if (!this.offhandItems.isMode("Fishing Rod")) {
         pairs.add(Pair.of(this.switchRod.getValue(), this.rodSlot.getValue().floatValue()));
      }

      if (!this.offhandItems.isMode("Block")) {
         pairs.add(Pair.of(this.switchBlock.getValue(), this.blockSlot.getValue().floatValue()));
      }

      Set<Integer> usedSlot = new HashSet<>();

      for (Pair<Boolean, Float> pair : pairs) {
         if ((Boolean)pair.getKey()) {
            int targetSlot = (int)((Float)pair.getValue() - 1.0F);
            if (usedSlot.contains(targetSlot)) {
               return false;
            }

            usedSlot.add(targetSlot);
         }
      }

      return true;
   }

   @EventHandler(
      priority = 1
   )
   public void onMotion(MotionEvent e) {
      if (e.getState() == Stage.PRE && this.autoDisable.getValue() && mc.world != null) {
         if (mc.player != null && !mc.player.isAlive()) {
            this.setEnable(false);
            return;
         }

         if (mc.player.age <= 1) {
            this.setEnable(false);
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent e) {
      if (!(mc.currentScreen instanceof OneConfigGui) && !this.checkConfig()) {
         this.message("[Inv]配置错误请检查", new Object[0]);
         this.toggle();
      } else {
         boolean work = Southside.get().getModuleManager().get(AutoBucket.class).isBusy();
         if (!InventoryUtils.shouldDisableFeatures() && !work) {
            if (PlayerUtils.isMoving()) {
               this.noMoveTicks = 0;
            } else {
               this.noMoveTicks++;
            }

            if (mc.player != null && mc.player.isUsingItem()) {
               this.usingItemTicks = 0;
               this.wasUsingItem = true;
            } else if (this.wasUsingItem) {
               this.usingItemTicks++;
               if (this.usingItemTicks >= 5) {
                  this.usingItemTicks = 0;
                  this.wasUsingItem = false;
               }
            }

            boolean isUsingItemWithDelay = mc.player.isUsingItem()
               || this.wasUsingItem
               || KillAura.target != null && this.delay.getValue().doubleValue() != 0.0 && mc.currentScreen == null;
            if (!StealerN.isWorking()
               && !Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)
               && (this.inventoryOnly.getValue() ? mc.currentScreen instanceof InventoryScreen : !isUsingItemWithDelay)) {
               if (!(mc.currentScreen instanceof HandledScreen container && container.getScreenHandler().syncId != mc.player.playerScreenHandler.syncId)) {
                  boolean movingNow = this.hasMoveInput();
                  boolean walkingSort = !this.inventoryOnly.getValue()
                     && !Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)
                     && !this.noMove.getValue();
                  boolean shouldClickThisTick = true;
                  if (!movingNow || !walkingSort) {
                     this.burstState = 0;
                     this.burstAccum = 0;
                     this.microStopPending = false;
                     this.prepZeroed = false;
                     shouldClickThisTick = this.zeroInputTicks >= 2;
                  } else if (this.burstState == 0) {
                     this.burstAccum++;
                     if (this.burstAccum >= 3) {
                        if (this.needsWork()) {
                           needStopSprint = true;
                           this.burstState = 1;
                        } else {
                           this.burstAccum = 0;
                        }
                     }

                     shouldClickThisTick = false;
                  } else if (this.burstState == 1) {
                     needStopSprint = true;
                     this.microStopPending = true;
                     this.prepZeroed = false;
                     this.burstState = 2;
                     shouldClickThisTick = false;
                  } else {
                     needStopSprint = true;
                     if (this.prepZeroed) {
                        this.burstState = 0;
                        this.burstAccum = 0;
                        shouldClickThisTick = true;
                     } else {
                        this.burstState = 1;
                        shouldClickThisTick = false;
                     }
                  }

                  if (shouldClickThisTick) {
                     if (this.autoArmor.getValue()) {
                        for (int i = 0; i < mc.player.getInventory().armor.size(); i++) {
                           ItemStack stack = (ItemStack)mc.player.getInventory().armor.get(i);
                           if (stack.getItem() instanceof ArmorItem) {
                              ArmorItem item = (ArmorItem)stack.getItem();
                              if (!stack.isEmpty()
                                 && InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) > InventoryUtils.getProtection(stack)) {
                                 this.clickSlot(mc.player.playerScreenHandler.syncId, 4 + (4 - i), 1, SlotActionType.THROW, mc.player);
                                 this.inventoryOpen = true;
                              }
                           }

                           this.mergePartialStacks();
                        }

                        for (int ix = 0; ix < mc.player.getInventory().main.size(); ix++) {
                           ItemStack stack = (ItemStack)mc.player.getInventory().main.get(ix);
                           if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                              ArmorItem item = (ArmorItem)stack.getItem();
                              float currentItemScore = InventoryUtils.getProtection(stack);
                              boolean isBestItem = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) == currentItemScore;
                              boolean isBetterItem = InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) < currentItemScore;
                              if (isBestItem && isBetterItem) {
                                 if (ix < 9) {
                                    this.clickSlot(mc.player.playerScreenHandler.syncId, ix + 36, 0, SlotActionType.QUICK_MOVE, mc.player);
                                 } else {
                                    this.clickSlot(mc.player.playerScreenHandler.syncId, ix, 0, SlotActionType.QUICK_MOVE, mc.player);
                                 }

                                 this.inventoryOpen = true;
                              }
                           }
                        }
                     }

                     if (this.clickOffHand) {
                        this.clickSlot(mc.player.playerScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                        this.inventoryOpen = true;
                        this.clickOffHand = false;
                     }

                     if (this.offhandItems.isMode("Golden Apple")) {
                        ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
                        int slot = InventoryUtils.getItemSlot(Items.GOLDEN_APPLE);
                        if (slot != -1) {
                           if (offHand.getItem() == Items.GOLDEN_APPLE) {
                              ItemStack goldenAppleStack = (ItemStack)mc.player.getInventory().main.get(slot);
                              if (offHand.getCount() + goldenAppleStack.getCount() <= 64) {
                                 if (slot < 9) {
                                    this.clickSlot(mc.player.playerScreenHandler.syncId, slot + 36, 0, SlotActionType.PICKUP, mc.player);
                                 } else {
                                    this.clickSlot(mc.player.playerScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                                 }

                                 this.inventoryOpen = true;
                                 this.clickOffHand = true;
                              }
                           } else {
                              this.swapOffHand(slot);
                           }
                        }
                     } else if (this.offhandItems.isMode("Projectile")) {
                        ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
                        ItemStack bestProjectile = InventoryUtils.getBestProjectile();
                        if (bestProjectile != null) {
                           int slot = InventoryUtils.getItemStackSlot(bestProjectile);
                           boolean shouldSwap = false;
                           if (offHand.getItem() != Items.EGG && offHand.getItem() != Items.SNOWBALL) {
                              shouldSwap = true;
                           } else if (offHand.getCount() < bestProjectile.getCount()) {
                              shouldSwap = true;
                           }

                           if (shouldSwap && slot != -1) {
                              this.swapOffHand(slot);
                           }
                        }
                     } else if (this.offhandItems.isMode("Fishing Rod")) {
                        ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
                        int slotx = InventoryUtils.getItemSlot(Items.FISHING_ROD);
                        if (slotx != -1 && offHand.getItem() != Items.FISHING_ROD) {
                           this.swapOffHand(slotx);
                        }
                     } else if (this.offhandItems.isMode("Block")) {
                        ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
                        ItemStack bestBlock = InventoryUtils.getBestBlock();
                        if (bestBlock != null) {
                           int slotx = InventoryUtils.getItemStackSlot(bestBlock);
                           boolean shouldSwapx = false;
                           if (!InventoryUtils.isInvalidBlock(offHand)) {
                              if (offHand.getCount() < bestBlock.getCount()) {
                                 shouldSwapx = true;
                              }
                           } else {
                              shouldSwapx = true;
                           }

                           if (shouldSwapx && slotx != -1) {
                              this.swapOffHand(slotx);
                           }
                        }
                     }

                     if (this.switchGoldenApple.getValue() && !this.offhandItems.isMode("Golden Apple")) {
                        this.swapItem((int)(this.goldenAppleSlot.getValue().floatValue() - 1.0F), Items.GOLDEN_APPLE);
                     }

                     if (this.switchBlock.getValue()) {
                        int blockSlot = (int)(this.blockSlot.getValue().floatValue() - 1.0F);
                        ItemStack currentBlock = (ItemStack)mc.player.getInventory().main.get(blockSlot);
                        ItemStack bestBlock = InventoryUtils.getBestBlock();
                        if (bestBlock != null
                           && (bestBlock.getCount() > currentBlock.getCount() || InventoryUtils.isInvalidBlock(currentBlock))
                           && !this.offhandItems.isMode("Block")) {
                           this.swapItem(blockSlot, bestBlock);
                        }

                        if (InventoryUtils.getBlockCountInInventory() > maxBlockSize.getValue().intValue()) {
                           ItemStack worstBlock = InventoryUtils.getWorstBlock();
                           this.throwItem(worstBlock);
                        }
                     }

                     if (this.switchSword.getValue()) {
                        int slotxx = (int)(this.swordSlot.getValue().floatValue() - 1.0F);
                        ItemStack currentSword = (ItemStack)mc.player.getInventory().main.get(slotxx);
                        ItemStack bestSword = InventoryUtils.getBestSword();
                        ItemStack bestShapeAxe = InventoryUtils.getBestShapeAxe();
                        if (InventoryUtils.getAxeDamage(bestShapeAxe) > InventoryUtils.getSwordDamage(bestSword)) {
                           bestSword = bestShapeAxe;
                        }

                        if (bestSword != null) {
                           float currentDamage = currentSword.getItem() instanceof SwordItem
                              ? InventoryUtils.getSwordDamage(currentSword)
                              : InventoryUtils.getAxeDamage(currentSword);
                           float bestWeaponDamage = bestSword.getItem() instanceof SwordItem
                              ? InventoryUtils.getSwordDamage(bestSword)
                              : InventoryUtils.getAxeDamage(bestSword);
                           if (bestWeaponDamage > currentDamage) {
                              this.swapItem(slotxx, bestSword);
                           }
                        }
                     }

                     if (this.switchPickaxe.getValue()) {
                        int slotxxx = (int)(this.pickaxeSlot.getValue().floatValue() - 1.0F);
                        ItemStack bestPickaxe = InventoryUtils.getBestPickaxe();
                        ItemStack currentPickaxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
                        if (bestPickaxe != null
                           && bestPickaxe.getItem() instanceof PickaxeItem
                           && (
                              InventoryUtils.getToolScore(bestPickaxe) > InventoryUtils.getToolScore(currentPickaxe)
                                 || !(currentPickaxe.getItem() instanceof PickaxeItem)
                           )) {
                           this.swapItem(slotxxx, bestPickaxe);
                        }
                     }

                     if (this.switchAxe.getValue()) {
                        int slotxxx = (int)(this.axeSlot.getValue().floatValue() - 1.0F);
                        ItemStack bestAxe = InventoryUtils.getBestAxe();
                        ItemStack currentAxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
                        if (bestAxe != null
                           && bestAxe.getItem() instanceof AxeItem
                           && (
                              InventoryUtils.getToolScore(bestAxe) > InventoryUtils.getToolScore(currentAxe)
                                 || !(currentAxe.getItem() instanceof AxeItem)
                           )) {
                           this.swapItem(slotxxx, bestAxe);
                        }
                     }

                     if (this.switchRod.getValue() && !this.offhandItems.isMode("Fishing Rod")) {
                        int slotxxx = (int)(this.rodSlot.getValue().floatValue() - 1.0F);
                        ItemStack bestRod = InventoryUtils.getFishingRod();
                        ItemStack currentRod = (ItemStack)mc.player.getInventory().main.get(slotxxx);
                        if (!(currentRod.getItem() instanceof FishingRodItem)) {
                           this.swapItem(slotxxx, bestRod);
                        }
                     }

                     if (this.switchBow.getValue()) {
                        int slotxxx = (int)(this.bowSlot.getValue().floatValue() - 1.0F);
                        ItemStack currentBow = (ItemStack)mc.player.getInventory().main.get(slotxxx);
                        ItemStack bestBow;
                        float bestBowScore;
                        float currentBowScore;
                        if (this.preferBow.isMode("Crossbow")) {
                           bestBow = InventoryUtils.getBestCrossbow();
                           bestBowScore = InventoryUtils.getCrossbowScore(bestBow);
                           currentBowScore = InventoryUtils.getCrossbowScore(currentBow);
                        } else if (this.preferBow.isMode("Power Bow")) {
                           bestBow = InventoryUtils.getBestPowerBow();
                           bestBowScore = InventoryUtils.getPowerBowScore(bestBow);
                           currentBowScore = InventoryUtils.getPowerBowScore(currentBow);
                        } else {
                           bestBow = InventoryUtils.getBestPunchBow();
                           bestBowScore = InventoryUtils.getPunchBowScore(bestBow);
                           currentBowScore = InventoryUtils.getPunchBowScore(currentBow);
                        }

                        if (bestBow == null) {
                           bestBow = InventoryUtils.getBestCrossbow();
                           bestBowScore = InventoryUtils.getCrossbowScore(bestBow);
                           currentBowScore = InventoryUtils.getCrossbowScore(currentBow);
                        }

                        if (bestBow == null) {
                           bestBow = InventoryUtils.getBestPowerBow();
                           bestBowScore = InventoryUtils.getPowerBowScore(bestBow);
                           currentBowScore = InventoryUtils.getPowerBowScore(currentBow);
                        }

                        if (bestBow == null) {
                           bestBow = InventoryUtils.getBestPunchBow();
                           bestBowScore = InventoryUtils.getPunchBowScore(bestBow);
                           currentBowScore = InventoryUtils.getPunchBowScore(currentBow);
                        }

                        if (bestBow != null && bestBowScore > currentBowScore) {
                           this.swapItem(slotxxx, bestBow);
                        }

                        if (InventoryUtils.getItemCount(Items.ARROW) > maxArrowSize.getValue().intValue()) {
                           ItemStack worstArrow = InventoryUtils.getWorstArrow();
                           this.throwItem(worstArrow);
                        }
                     }

                     if (this.switchEnderPearl.getValue()) {
                        this.swapItem((int)(this.enderPearlSlot.getValue().floatValue() - 1.0F), Items.ENDER_PEARL);
                     }

                     if (this.switchWaterBucket.getValue()) {
                        this.swapItem((int)(this.waterBucketSlot.getValue().floatValue() - 1.0F), Items.WATER_BUCKET);
                     }

                     if (this.switchFireball.getValue()) {
                        this.swapItem((int)(this.fireballSlot.getValue().floatValue() - 1.0F), Items.FIRE_CHARGE);
                     }

                     if (keepProjectile.getValue()) {
                        if (InventoryUtils.getItemCount(Items.EGG) + InventoryUtils.getItemCount(Items.SNOWBALL)
                           > maxProjectileSize.getValue().intValue()) {
                           ItemStack worstProjectile = InventoryUtils.getWorstProjectile();
                           this.throwItem(worstProjectile);
                        }

                        if (this.switchProjectile.getValue() && !this.offhandItems.isMode("Projectile")) {
                           int projectileSlot = (int)(this.projectileSlot.getValue().floatValue() - 1.0F);
                           if (InventoryUtils.getItemCount(Items.EGG) > 0) {
                              this.swapItem(projectileSlot, Items.EGG);
                           } else if (InventoryUtils.getItemCount(Items.SNOWBALL) > 0) {
                              this.swapItem(projectileSlot, Items.SNOWBALL);
                           }
                        }
                     }

                     if (this.throwItems.getValue()) {
                        List<Integer> slots = IntStream.range(0, mc.player.getInventory().main.size()).boxed().collect(Collectors.toList());
                        Collections.shuffle(slots);

                        for (Integer slotxxxx : slots) {
                           ItemStack stack = (ItemStack)mc.player.getInventory().main.get(slotxxxx);
                           if (!stack.isEmpty() && !isItemUseful(stack)) {
                              this.throwItem(stack);
                           }
                        }
                     }
                  }
               }
            } else {
               this.clickOffHand = false;
            }
         }
      }
   }

   private void swapOffHand(int slot) {
      if (slot < 9) {
         this.clickSlot(mc.player.playerScreenHandler.syncId, slot + 36, 40, SlotActionType.SWAP, mc.player);
      } else {
         this.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
      }

      this.inventoryOpen = true;
   }

   private void throwItem(ItemStack item) {
      if (InventoryUtils.isItemValid(item)) {
         int itemSlot = InventoryUtils.getItemStackSlot(item);
         if (itemSlot != -1) {
            if (itemSlot < 9) {
               this.clickSlot(mc.player.playerScreenHandler.syncId, itemSlot + 36, 1, SlotActionType.THROW, mc.player);
            } else {
               this.clickSlot(mc.player.playerScreenHandler.syncId, itemSlot, 1, SlotActionType.THROW, mc.player);
            }

            this.inventoryOpen = true;
         }
      }
   }

   private void swapItem(int targetSlot, ItemStack bestItem) {
      ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot) && bestItem != currentSlot) {
         int bestItemSlot = InventoryUtils.getItemStackSlot(bestItem);
         if (bestItemSlot != -1) {
            if (bestItemSlot < 9) {
               this.clickSlot(mc.player.playerScreenHandler.syncId, bestItemSlot + 36, targetSlot, SlotActionType.SWAP, mc.player);
            } else {
               this.clickSlot(mc.player.playerScreenHandler.syncId, bestItemSlot, targetSlot, SlotActionType.SWAP, mc.player);
            }

            this.inventoryOpen = true;
         }
      }
   }

   private void swapItem(int targetSlot, Item item) {
      ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot)) {
         int bestItemSlot = InventoryUtils.getItemSlot(item);
         if (bestItemSlot != -1) {
            ItemStack bestItemStack = (ItemStack)mc.player.getInventory().main.get(bestItemSlot);
            if (currentSlot.getItem() != item || currentSlot.getItem() == item && currentSlot.getCount() < bestItemStack.getCount()) {
               if (bestItemSlot < 9) {
                  this.clickSlot(mc.player.playerScreenHandler.syncId, bestItemSlot + 36, targetSlot, SlotActionType.SWAP, mc.player);
               } else {
                  this.clickSlot(mc.player.playerScreenHandler.syncId, bestItemSlot, targetSlot, SlotActionType.SWAP, mc.player);
               }

               this.inventoryOpen = true;
            }
         }
      }
   }

   @EventHandler(
      priority = -200
   )
   public void onMoveInput(MovementInputEvent e) {
      boolean anyMoveKey = e.isForward() || e.isBack() || e.isLeft() || e.isRight() || e.isJump();
      if (anyMoveKey) {
         this.zeroInputTicks = 0;
      } else {
         this.zeroInputTicks++;
      }

      if (this.microStopPending) {
         if (mc.player != null && mc.player.isOnGround() && !mc.player.isSwimming()) {
            e.setForward(false);
            e.setBack(false);
            e.setLeft(false);
            e.setRight(false);
            e.setJump(false);
            e.setSprint(false);
            this.microStopPending = false;
            this.prepZeroed = true;
         } else {
            this.microStopPending = false;
            this.prepZeroed = false;
         }
      }

      if (needStopSprint) {
         if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            return;
         }

         mc.options.sprintKey.setPressed(false);
         mc.player.setSprinting(false);
         e.setSprint(false);
      }
   }

   private void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player) {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class) && !this.noMove.getValue()) {
         needStopSprint = true;
      }

      if (timer.hasPassed(this.delay.getValue().longValue()) || this.delay.getValue().doubleValue() == 0.0) {
         if (!this.inventoryOnly.getValue() && mc.player.lastSprinting) {
            return;
         }

         mc.interactionManager.clickSlot(syncId, slotId, button, actionType, player);
         timer.reset();
         active = true;
         if (!this.inventoryOnly.getValue() && mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
         }
      }
   }

   private boolean hasMoveInput() {
      return mc.options.forwardKey.isPressed()
         || mc.options.backKey.isPressed()
         || mc.options.leftKey.isPressed()
         || mc.options.rightKey.isPressed()
         || mc.options.jumpKey.isPressed();
   }

   private boolean needsWork() {
      if (this.clickOffHand) {
         return true;
      } else {
         if (this.autoArmor.getValue()) {
            for (int i = 0; i < mc.player.getInventory().armor.size(); i++) {
               ItemStack stack = (ItemStack)mc.player.getInventory().armor.get(i);
               if (!stack.isEmpty()
                  && stack.getItem() instanceof ArmorItem
                  && InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) > InventoryUtils.getProtection(stack)) {
                  return true;
               }
            }

            if (this.mergeSrc >= 0 && this.mergeDst >= 0) {
               return true;
            }

            List<ItemStack> main = mc.player.getInventory().main;

            for (int ix = 0; ix < main.size(); ix++) {
               ItemStack stackA = main.get(ix);
               if (!stackA.isEmpty() && stackA.getCount() < stackA.getMaxCount() && stackA.isStackable()) {
                  for (int j = ix + 1; j < main.size(); j++) {
                     ItemStack stackB = main.get(j);
                     if (!stackB.isEmpty() && stackB.getCount() < stackB.getMaxCount() && ItemStack.areItemsAndComponentsEqual(stackA, stackB)) {
                        return true;
                     }
                  }
               }
            }
         }

         for (int ixx = 0; ixx < mc.player.getInventory().main.size(); ixx++) {
            ItemStack stack = (ItemStack)mc.player.getInventory().main.get(ixx);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
               float currentItemScore = InventoryUtils.getProtection(stack);
               if (InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) == currentItemScore
                  && InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) < currentItemScore) {
                  return true;
               }
            }
         }

         if (this.offhandItems.isMode("Golden Apple")) {
            ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
            int slot = InventoryUtils.getItemSlot(Items.GOLDEN_APPLE);
            if (slot != -1) {
               if (offHand.getItem() != Items.GOLDEN_APPLE) {
                  return true;
               }

               ItemStack goldenAppleStack = (ItemStack)mc.player.getInventory().main.get(slot);
               if (offHand.getCount() + goldenAppleStack.getCount() <= 64) {
                  return true;
               }
            }
         } else if (this.offhandItems.isMode("Projectile")) {
            ItemStack bestProjectile = InventoryUtils.getBestProjectile();
            if (bestProjectile != null) {
               int slot = InventoryUtils.getItemStackSlot(bestProjectile);
               ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
               boolean shouldSwap = offHand.getItem() != Items.EGG && offHand.getItem() != Items.SNOWBALL
                  || offHand.getCount() < bestProjectile.getCount();
               if (shouldSwap && slot != -1) {
                  return true;
               }
            }
         } else if (this.offhandItems.isMode("Fishing Rod")) {
            int slotx = InventoryUtils.getItemSlot(Items.FISHING_ROD);
            if (slotx != -1 && ((ItemStack)mc.player.getInventory().offHand.get(0)).getItem() != Items.FISHING_ROD) {
               return true;
            }
         } else if (this.offhandItems.isMode("Block")) {
            ItemStack bestBlock = InventoryUtils.getBestBlock();
            if (bestBlock != null) {
               int slotx = InventoryUtils.getItemStackSlot(bestBlock);
               ItemStack offHand = (ItemStack)mc.player.getInventory().offHand.get(0);
               boolean shouldSwapx = InventoryUtils.isInvalidBlock(offHand) || offHand.getCount() < bestBlock.getCount();
               if (shouldSwapx && slotx != -1) {
                  return true;
               }
            }
         }

         if (this.switchGoldenApple.getValue() && !this.offhandItems.isMode("Golden Apple")) {
            int targetSlot = (int)(this.goldenAppleSlot.getValue().floatValue() - 1.0F);
            ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(Items.GOLDEN_APPLE);
               if (bestItemSlot != -1) {
                  ItemStack bestItemStack = (ItemStack)mc.player.getInventory().main.get(bestItemSlot);
                  if (currentSlot.getItem() != Items.GOLDEN_APPLE
                     || currentSlot.getItem() == Items.GOLDEN_APPLE && currentSlot.getCount() < bestItemStack.getCount()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchBlock.getValue()) {
            int blockSlot = (int)(this.blockSlot.getValue().floatValue() - 1.0F);
            ItemStack currentBlock = (ItemStack)mc.player.getInventory().main.get(blockSlot);
            ItemStack bestBlock = InventoryUtils.getBestBlock();
            if (bestBlock != null
               && (bestBlock.getCount() > currentBlock.getCount() || InventoryUtils.isInvalidBlock(currentBlock))
               && !this.offhandItems.isMode("Block")) {
               return true;
            }

            if (InventoryUtils.getBlockCountInInventory() > maxBlockSize.getValue().intValue()) {
               return true;
            }
         }

         if (this.switchSword.getValue()) {
            int slotxx = (int)(this.swordSlot.getValue().floatValue() - 1.0F);
            ItemStack currentSword = (ItemStack)mc.player.getInventory().main.get(slotxx);
            ItemStack bestSword = InventoryUtils.getBestSword();
            ItemStack bestShapeAxe = InventoryUtils.getBestShapeAxe();
            if (InventoryUtils.getAxeDamage(bestShapeAxe) > InventoryUtils.getSwordDamage(bestSword)) {
               bestSword = bestShapeAxe;
            }

            if (bestSword != null) {
               float currentDamage = currentSword.getItem() instanceof SwordItem
                  ? InventoryUtils.getSwordDamage(currentSword)
                  : InventoryUtils.getAxeDamage(currentSword);
               float bestWeaponDamage = bestSword.getItem() instanceof SwordItem
                  ? InventoryUtils.getSwordDamage(bestSword)
                  : InventoryUtils.getAxeDamage(bestSword);
               if (bestWeaponDamage > currentDamage) {
                  return true;
               }
            }
         }

         if (this.switchPickaxe.getValue()) {
            int slotxxx = (int)(this.pickaxeSlot.getValue().floatValue() - 1.0F);
            ItemStack bestPickaxe = InventoryUtils.getBestPickaxe();
            ItemStack currentPickaxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
            if (bestPickaxe != null
               && bestPickaxe.getItem() instanceof PickaxeItem
               && (
                  InventoryUtils.getToolScore(bestPickaxe) > InventoryUtils.getToolScore(currentPickaxe)
                     || !(currentPickaxe.getItem() instanceof PickaxeItem)
               )) {
               return true;
            }
         }

         if (this.switchAxe.getValue()) {
            int slotxxx = (int)(this.axeSlot.getValue().floatValue() - 1.0F);
            ItemStack bestAxe = InventoryUtils.getBestAxe();
            ItemStack currentAxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
            if (bestAxe != null
               && bestAxe.getItem() instanceof AxeItem
               && (InventoryUtils.getToolScore(bestAxe) > InventoryUtils.getToolScore(currentAxe) || !(currentAxe.getItem() instanceof AxeItem))) {
               return true;
            }
         }

         if (this.switchRod.getValue() && !this.offhandItems.isMode("Fishing Rod")) {
            int slotxxx = (int)(this.rodSlot.getValue().floatValue() - 1.0F);
            ItemStack currentRod = (ItemStack)mc.player.getInventory().main.get(slotxxx);
            if (!(currentRod.getItem() instanceof FishingRodItem)) {
               return true;
            }
         }

         if (this.switchBow.getValue()) {
            int slotxxx = (int)(this.bowSlot.getValue().floatValue() - 1.0F);
            ItemStack currentBow = (ItemStack)mc.player.getInventory().main.get(slotxxx);
            ItemStack bestBow;
            float bestBowScore;
            float currentBowScore;
            if (this.preferBow.isMode("Crossbow")) {
               bestBow = InventoryUtils.getBestCrossbow();
               bestBowScore = InventoryUtils.getCrossbowScore(bestBow);
               currentBowScore = InventoryUtils.getCrossbowScore(currentBow);
            } else if (this.preferBow.isMode("Power Bow")) {
               bestBow = InventoryUtils.getBestPowerBow();
               bestBowScore = InventoryUtils.getPowerBowScore(bestBow);
               currentBowScore = InventoryUtils.getPowerBowScore(currentBow);
            } else {
               bestBow = InventoryUtils.getBestPunchBow();
               bestBowScore = InventoryUtils.getPunchBowScore(bestBow);
               currentBowScore = InventoryUtils.getPunchBowScore(currentBow);
            }

            if (bestBow == null) {
               bestBow = InventoryUtils.getBestCrossbow();
               bestBowScore = InventoryUtils.getCrossbowScore(bestBow);
               currentBowScore = InventoryUtils.getCrossbowScore(currentBow);
            }

            if (bestBow == null) {
               bestBow = InventoryUtils.getBestPowerBow();
               bestBowScore = InventoryUtils.getPowerBowScore(bestBow);
               currentBowScore = InventoryUtils.getPowerBowScore(currentBow);
            }

            if (bestBow == null) {
               bestBow = InventoryUtils.getBestPunchBow();
               bestBowScore = InventoryUtils.getPunchBowScore(bestBow);
               currentBowScore = InventoryUtils.getPunchBowScore(currentBow);
            }

            if (bestBow != null && bestBowScore > currentBowScore) {
               return true;
            }

            if (InventoryUtils.getItemCount(Items.ARROW) > maxArrowSize.getValue().intValue()) {
               return true;
            }
         }

         if (this.switchEnderPearl.getValue()) {
            int targetSlot = (int)(this.enderPearlSlot.getValue().floatValue() - 1.0F);
            ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(Items.ENDER_PEARL);
               if (bestItemSlot != -1) {
                  ItemStack bestItemStack = (ItemStack)mc.player.getInventory().main.get(bestItemSlot);
                  if (currentSlot.getItem() != Items.ENDER_PEARL
                     || currentSlot.getItem() == Items.ENDER_PEARL && currentSlot.getCount() < bestItemStack.getCount()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchWaterBucket.getValue()) {
            int targetSlot = (int)(this.waterBucketSlot.getValue().floatValue() - 1.0F);
            ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(Items.WATER_BUCKET);
               if (bestItemSlot != -1) {
                  ItemStack bestItemStack = (ItemStack)mc.player.getInventory().main.get(bestItemSlot);
                  if (currentSlot.getItem() != Items.WATER_BUCKET
                     || currentSlot.getItem() == Items.WATER_BUCKET && currentSlot.getCount() < bestItemStack.getCount()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchFireball.getValue()) {
            int targetSlot = (int)(this.fireballSlot.getValue().floatValue() - 1.0F);
            ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(Items.FIRE_CHARGE);
               if (bestItemSlot != -1) {
                  ItemStack bestItemStack = (ItemStack)mc.player.getInventory().main.get(bestItemSlot);
                  if (currentSlot.getItem() != Items.FIRE_CHARGE
                     || currentSlot.getItem() == Items.FIRE_CHARGE && currentSlot.getCount() < bestItemStack.getCount()) {
                     return true;
                  }
               }
            }
         }

         if (keepProjectile.getValue()) {
            if (InventoryUtils.getItemCount(Items.EGG) + InventoryUtils.getItemCount(Items.SNOWBALL)
               > maxProjectileSize.getValue().intValue()) {
               return true;
            }

            if (this.switchProjectile.getValue() && !this.offhandItems.isMode("Projectile")) {
               int projectileSlot = (int)(this.projectileSlot.getValue().floatValue() - 1.0F);
               ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(projectileSlot);
               if (InventoryUtils.getItemCount(Items.EGG) > 0) {
                  if (currentSlot.getItem() != Items.EGG) {
                     return true;
                  }
               } else if (InventoryUtils.getItemCount(Items.SNOWBALL) > 0 && currentSlot.getItem() != Items.SNOWBALL) {
                  return true;
               }
            }
         }

         if (this.throwItems.getValue()) {
            for (ItemStack stack : mc.player.getInventory().main) {
               if (!stack.isEmpty() && !isItemUseful(stack)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private void mergePartialStacks() {
      if (this.mergeSrc >= 0 && this.mergeDst >= 0) {
         if (this.mergeDst < 9) {
            this.clickSlot(mc.player.playerScreenHandler.syncId, this.mergeDst + 36, 0, SlotActionType.PICKUP, mc.player);
         } else {
            this.clickSlot(mc.player.playerScreenHandler.syncId, this.mergeDst, 0, SlotActionType.PICKUP, mc.player);
         }

         this.mergeSrc = -1;
         this.mergeDst = -1;
      } else {
         List<ItemStack> main = mc.player.getInventory().main;

         for (int i = 0; i < main.size(); i++) {
            ItemStack stackA = main.get(i);
            if (!stackA.isEmpty() && stackA.getCount() < stackA.getMaxCount() && stackA.isStackable()) {
               for (int j = i + 1; j < main.size(); j++) {
                  ItemStack stackB = main.get(j);
                  if (!stackB.isEmpty() && stackB.getCount() < stackB.getMaxCount() && ItemStack.areItemsAndComponentsEqual(stackA, stackB)) {
                     int smaller = stackA.getCount() <= stackB.getCount() ? i : j;
                     int larger = smaller == i ? j : i;
                     if (smaller < 9) {
                        this.clickSlot(mc.player.playerScreenHandler.syncId, smaller + 36, 0, SlotActionType.PICKUP, mc.player);
                     } else {
                        this.clickSlot(mc.player.playerScreenHandler.syncId, smaller, 0, SlotActionType.PICKUP, mc.player);
                     }

                     this.mergeSrc = smaller;
                     this.mergeDst = larger;
                     return;
                  }
               }
            }
         }
      }
   }

   public static boolean isWorking() {
      return !timer.hasPassed(200L);
   }
}
