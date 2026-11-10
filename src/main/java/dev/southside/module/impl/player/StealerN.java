package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.item.ItemUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.ContainerUtils;
import dev.southside.utils.player.InvUtils;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;

public class StealerN extends Module {
   private static final MSTimer timer = new MSTimer();
   private static final MSTimer timer2 = new MSTimer();
   private static final MSTimer timer3 = new MSTimer();
   private final Switch swap = new Switch("Use Swap", false);
   public final Switch silently = new Switch("Silently Stealing", false);
   public final Switch instant = new Switch("Instant", false);
   private final Switch smart = new Switch("Smart", false);
   private final Switch doubleClickSmart = new Switch("DoubleClick", false);
   private final Switch halfDoubleClick = new Switch("DoubleClickHalf", false);
   private final Switch invManage = new Switch("InvManage", false);
   private final Switch invManagerBoost = new Switch("InvManager Boost", false);
   private final Slider startDelay = new Slider("Start Delay", 350, 0, 600, 1);
   private final Slider closeDelay = new Slider("Close Delay", 350, 0, 600, 1);
   private final Slider minDelay = new Slider("Min Delay", 80, 0, 200, 1);
   private final Slider maxDelay = new Slider("Max Delay", 100, 0, 200, 1);
   private final Slider delay1 = new Slider("Multi Stack Delay", 130, 0, 200, 1);
   private final Switch pickEnderChest = new Switch("Ender Chest", false);
   private final Switch autoDisable = new Switch("Auto Disable", false);
   private long nextDelay = 0L;
   private Screen lastTickScreen;
   private static final double MAX_REACH_DISTANCE = 4.5;
   private static final int ACTION_THROW = 0;
   private static final int ACTION_SWAP = 1;
   private static final int ACTION_OFFHAND = 2;
   private static final int ACTION_QUICK_MOVE = 3;
   private static final int ACTION_SWAP_TO_HOTBAR = 4;

   public StealerN() {
      super(Category.Player, "Stealer", "打不过zen");
   }

   public static boolean isWorking() {
      return !timer.hasPassed(150L);
   }

   @Override
   public void initPostRunnable() {
      this.addRangedValueRestrict(this.minDelay, this.maxDelay);
   }

   public static boolean screenValid(Screen screen) {
      if (!(screen instanceof GenericContainerScreen container)) {
         return false;
      } else {
         String chestTitle = container.getTitle().getString();
         String chest = Text.translatable("container.chest").getString();
         String largeChest = Text.translatable("container.chestDouble").getString();
         return chestTitle.equals(chest) || chestTitle.equals(largeChest) || chestTitle.equals("Chest");
      }
   }

   @EventHandler(
      priority = 1
   )
   public void onMotion(MotionEvent e) {
      if (!e.getState().equals(Stage.POST)) {
         if (this.autoDisable.getValue() && mc.world != null) {
            if (!mc.player.isAlive()) {
               this.setEnable(false);
               return;
            }

            if (mc.player.age <= 1) {
               this.setEnable(false);
            }
         }

         ChestManager manager = Southside.get().getChestManager();
         Screen currentScreen = mc.currentScreen;
         if (currentScreen instanceof GenericContainerScreen container) {
            if (mc.player.currentScreenHandler != container.getScreenHandler()) {
               this.lastTickScreen = null;
               return;
            }

            GenericContainerScreenHandler menu = (GenericContainerScreenHandler)container.getScreenHandler();
            if (currentScreen != this.lastTickScreen) {
               timer.reset();
               this.nextDelay = (long)this.startDelay.getValue().floatValue();
            } else {
               String chestTitle = container.getTitle().getString();
               String chest = Text.translatable("container.chest").getString();
               String largeChest = Text.translatable("container.chestDouble").getString();
               String enderChest = Text.translatable("container.enderchest").getString();
               if (chestTitle.equals(chest)
                  || chestTitle.equals(largeChest)
                  || chestTitle.equals("Chest")
                  || this.pickEnderChest.getValue() && chestTitle.equals(enderChest)) {
                  if (!this.instant.getValue() && this.isChestEmpty(menu) && timer.hasPassed(this.nextDelay)) {
                     if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                        manager.stolenAll();
                        mc.player.closeHandledScreen();
                        timer3.reset();
                     }
                  } else if (this.instant.getValue() && timer.hasPassed(this.nextDelay)) {
                     boolean willDelay = true;
                     CopyOnWriteArrayList<StealerN.InstantSlotMove> moves = new CopyOnWriteArrayList<>();
                     this.reload(moves, menu);
                     if (moves.isEmpty()) {
                        if (timer.hasPassed(this.nextDelay)) {
                           if (this.instant.getValue() && InvUtils.isInventoryFull(mc.player.getInventory())) {
                              mc.player.closeHandledScreen();
                              this.warning("The inventory is full while instant stealing.", new Object[0]);
                              this.warning("Please clean your inventory and reopen the chest", new Object[0]);
                              return;
                           }

                           if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                              manager.stolenAll();
                              mc.player.closeHandledScreen();
                              timer3.reset();
                           }
                        }

                        return;
                     }

                     for (StealerN.InstantSlotMove move : moves) {
                        switch (move.action) {
                           case 0:
                              int empty = this.nextChestEmptySlot();
                              if (empty == -1) {
                                 continue;
                              }

                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.src, 40, SlotActionType.SWAP, mc.player);
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, empty, 40, SlotActionType.SWAP, mc.player);
                              break;
                           case 1:
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.src, 40, SlotActionType.SWAP, mc.player);
                              if (!((ItemStack)mc.player.getInventory().offHand.get(0)).isEmpty()) {
                                 mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.dst, 40, SlotActionType.SWAP, mc.player);
                                 if (this.doubleClickSmart.getValue() && move.stack != null) {
                                    boolean canDoubleClick = !move.stack.isEmpty() && move.stack.getCount() < move.stack.getMaxCount();
                                    if (canDoubleClick) {
                                       mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.dst, 0, SlotActionType.PICKUP, mc.player);
                                       mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.dst, 0, SlotActionType.PICKUP_ALL, mc.player);
                                       if (this.halfDoubleClick.getValue()) {
                                          mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.dst, 0, SlotActionType.PICKUP, mc.player);
                                       }
                                    }
                                 }
                              }
                              break;
                           case 2:
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.src, 40, SlotActionType.SWAP, mc.player);
                              break;
                           case 3:
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, move.src, 0, SlotActionType.QUICK_MOVE, mc.player);
                              if (this.smart.getValue()) {
                                 timer.reset();
                                 this.scheduleNextDelay();
                                 return;
                              }
                              break;
                           case 4:
                              this.performBoostSwap(mc.player.currentScreenHandler.syncId, move.src, move.dst);
                        }

                        this.reload(moves, menu);
                        if (moves.isEmpty()) {
                           if (timer.hasPassed(this.nextDelay)) {
                              if (this.instant.getValue() && InvUtils.isInventoryFull(mc.player.getInventory())) {
                                 mc.player.closeHandledScreen();
                                 this.warning("The inventory is full while instant stealing.", new Object[0]);
                                 this.warning("Please clean your inventory and reopen the chest", new Object[0]);
                                 break;
                              }

                              if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                                 manager.stolenAll();
                                 mc.player.closeHandledScreen();
                                 timer3.reset();
                              }
                           }

                           willDelay = false;
                           break;
                        }
                     }

                     if (willDelay) {
                        timer.reset();
                        this.scheduleNextDelay();
                     }
                  } else {
                     List<Integer> slots = IntStream.range(0, menu.getRows() * 9).boxed().collect(Collectors.toList());
                     Collections.shuffle(slots);

                     for (Integer pSlotId : slots) {
                        ItemStack stack = menu.getSlot(pSlotId).getStack();
                        if (isItemUseful(stack) && this.isBestItemInChest(menu, stack) && timer.hasPassed(this.nextDelay)) {
                           if (this.invManagerBoost.getValue() && this.tryBoostHotbarDirect(menu, pSlotId, stack)) {
                              timer.reset();
                              this.scheduleNextDelay();
                              break;
                           }

                           boolean willDelayx = !this.instant.getValue();
                           if (this.swap.getValue()) {
                              int slot = getFirstEmptySlot();
                              if (slot == -1 || slot + 18 >= 54) {
                                 mc.player.closeHandledScreen();
                              } else if (stack.getMaxCount() <= 1) {
                                 if (slot < 9) {
                                    mc.interactionManager.clickSlot(menu.syncId, pSlotId, slot, SlotActionType.SWAP, mc.player);
                                 } else {
                                    mc.interactionManager.clickSlot(menu.syncId, slot + 18, 8, SlotActionType.SWAP, mc.player);
                                    mc.interactionManager.clickSlot(menu.syncId, pSlotId, 8, SlotActionType.SWAP, mc.player);
                                 }
                              } else if (timer2.hasPassed(this.delay1.getValue().longValue())) {
                                 mc.interactionManager.clickSlot(menu.syncId, pSlotId, 0, SlotActionType.QUICK_MOVE, mc.player);
                                 timer2.reset();
                              }
                           } else {
                              mc.interactionManager.clickSlot(menu.syncId, pSlotId, 0, SlotActionType.QUICK_MOVE, mc.player);
                           }

                           if (willDelayx) {
                              timer.reset();
                              this.scheduleNextDelay();
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         this.lastTickScreen = currentScreen;
      }
   }

   private void reload(List<StealerN.InstantSlotMove> moves, GenericContainerScreenHandler menu) {
      moves.clear();
      List<Integer> slots = IntStream.range(0, menu.getRows() * 9).boxed().collect(Collectors.toList());
      Collections.shuffle(slots);
      boolean normal;
      if (this.invManage.getValue()) {
         normal = !this.runInvManage(moves);
         List<StealerN.InstantSlotMove> offhands = new ArrayList<>();
         if (!normal) {
            this.invManPostOffhand(offhands);
         }

         for (Integer pSlotId : slots) {
            StealerN.InstantSlotMove t1 = offhands.stream().filter(f -> f.src == pSlotId).findFirst().orElse(null);
            StealerN.InstantSlotMove t = moves.stream().filter(f -> f.src == pSlotId).findFirst().orElse(t1);
            if (t == null) {
               ItemStack stack = menu.getSlot(pSlotId).getStack();
               if (isItemUseful(stack)
                  && this.isBestItemInChest(menu, stack)
                  && (!this.invManagerBoost.getValue() || !this.tryBoostHotbarSlot(moves, pSlotId, stack))) {
                  for (Slot slot : mc.player.currentScreenHandler.slots) {
                     if (slot.inventory == mc.player.getInventory() && slot.getStack().isEmpty()) {
                        StealerN.InstantSlotMove target1 = offhands.stream().filter(f -> f.dst == slot.id).findFirst().orElse(null);
                        StealerN.InstantSlotMove target = moves.stream().filter(f -> f.dst == slot.id).findFirst().orElse(target1);
                        if (target == null) {
                           moves.add(new StealerN.InstantSlotMove(pSlotId, 0, 3, stack));
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (!normal) {
            this.invManPostOffhand(moves);
         }
      } else {
         normal = true;
      }

      if (normal) {
         for (Integer pSlotIdx : slots) {
            ItemStack stack = menu.getSlot(pSlotIdx).getStack();
            if (isItemUseful(stack)
               && this.isBestItemInChest(menu, stack)
               && (!this.invManagerBoost.getValue() || !this.tryBoostHotbarSlot(moves, pSlotIdx, stack))) {
               for (Slot slotx : mc.player.currentScreenHandler.slots) {
                  if (slotx.inventory == mc.player.getInventory() && slotx.getStack().isEmpty()) {
                     StealerN.InstantSlotMove target = moves.stream().filter(f -> f.dst == slotx.id).findFirst().orElse(null);
                     if (target == null) {
                        boolean canDoubleClick = !stack.isEmpty() && stack.getCount() < stack.getMaxCount();
                        if (canDoubleClick && this.smart.getValue()) {
                           moves.add(new StealerN.InstantSlotMove(pSlotIdx, -1, 3, stack));
                        } else {
                           moves.add(new StealerN.InstantSlotMove(pSlotIdx, 0, 3, stack));
                        }
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean runInvManage(List<StealerN.InstantSlotMove> actions) {
      if (this.invManage.getValue() && this.instant.getValue()) {
         if (mc.currentScreen instanceof GenericContainerScreen container) {
            GenericContainerScreenHandler menu = (GenericContainerScreenHandler)container.getScreenHandler();
            InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
            if (!manager.checkConfig()) {
               this.message("Inv 配置错误..", new Object[0]);
               return false;
            }

            if (manager.switchGoldenApple.getValue() && !manager.offhandItems.isMode("Golden Apple")) {
               this.invSwapItem(actions, (int)(manager.goldenAppleSlot.getValue().floatValue() - 1.0F), Items.GOLDEN_APPLE);
            }

            if (manager.switchBlock.getValue()) {
               int blockSlot = (int)(manager.blockSlot.getValue().floatValue() - 1.0F);
               ItemStack currentBlock = (ItemStack)mc.player.getInventory().main.get(blockSlot);
               ItemStack bestBlock = ContainerUtils.getBestBlock();
               if (bestBlock != null
                  && (bestBlock.getCount() > currentBlock.getCount() || InventoryUtils.isInvalidBlock(currentBlock))
                  && !manager.offhandItems.isMode("Block")) {
                  this.invSwapItem(actions, blockSlot, bestBlock);
               }
            }

            if (manager.switchSword.getValue()) {
               int slotxx = (int)(manager.swordSlot.getValue().floatValue() - 1.0F);
               ItemStack currentSword = (ItemStack)mc.player.getInventory().main.get(slotxx);
               ItemStack bestSword = ContainerUtils.getBestSword();
               ItemStack bestShapeAxe = ContainerUtils.getBestShapeAxe();
               if (ContainerUtils.getAxeDamage(bestShapeAxe) > ContainerUtils.getSwordDamage(bestSword)) {
                  bestSword = bestShapeAxe;
               }

               if (bestSword != null) {
                  float currentDamage = currentSword.getItem() instanceof SwordItem
                     ? ContainerUtils.getSwordDamage(currentSword)
                     : ContainerUtils.getAxeDamage(currentSword);
                  float bestWeaponDamage = bestSword.getItem() instanceof SwordItem
                     ? ContainerUtils.getSwordDamage(bestSword)
                     : ContainerUtils.getAxeDamage(bestSword);
                  if (bestWeaponDamage > currentDamage) {
                     this.invSwapItem(actions, slotxx, bestSword);
                  }
               }
            }

            if (manager.switchPickaxe.getValue()) {
               int slotxxx = (int)(manager.pickaxeSlot.getValue().floatValue() - 1.0F);
               ItemStack bestPickaxe = ContainerUtils.getBestPickaxe();
               ItemStack currentPickaxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
               if (bestPickaxe != null
                  && bestPickaxe.getItem() instanceof PickaxeItem
                  && (
                     ContainerUtils.getToolScore(bestPickaxe) > ContainerUtils.getToolScore(currentPickaxe)
                        || !(currentPickaxe.getItem() instanceof PickaxeItem)
                  )) {
                  this.invSwapItem(actions, slotxxx, bestPickaxe);
               }
            }

            if (manager.switchAxe.getValue()) {
               int slotxxx = (int)(manager.axeSlot.getValue().floatValue() - 1.0F);
               ItemStack bestAxe = ContainerUtils.getBestAxe();
               ItemStack currentAxe = (ItemStack)mc.player.getInventory().main.get(slotxxx);
               if (bestAxe != null
                  && bestAxe.getItem() instanceof AxeItem
                  && (ContainerUtils.getToolScore(bestAxe) > ContainerUtils.getToolScore(currentAxe) || !(currentAxe.getItem() instanceof AxeItem))) {
                  this.invSwapItem(actions, slotxxx, bestAxe);
               }
            }

            if (manager.switchRod.getValue() && !manager.offhandItems.isMode("Fishing Rod")) {
               int slotxxx = (int)(manager.rodSlot.getValue().floatValue() - 1.0F);
               ItemStack bestRod = ContainerUtils.getFishingRod();
               ItemStack currentRod = (ItemStack)mc.player.getInventory().main.get(slotxxx);
               if (!(currentRod.getItem() instanceof FishingRodItem)) {
                  this.invSwapItem(actions, slotxxx, bestRod);
               }
            }

            if (manager.switchEnderPearl.getValue()) {
               this.invSwapItem(actions, (int)(manager.enderPearlSlot.getValue().floatValue() - 1.0F), Items.ENDER_PEARL);
            }

            if (manager.switchWaterBucket.getValue()) {
               this.invSwapItem(actions, (int)(manager.waterBucketSlot.getValue().floatValue() - 1.0F), Items.WATER_BUCKET);
            }

            if (manager.switchFireball.getValue()) {
               this.invSwapItem(actions, (int)(manager.fireballSlot.getValue().floatValue() - 1.0F), Items.FIRE_CHARGE);
            }

            if (InvManagerN.keepProjectile.getValue() && manager.switchProjectile.getValue() && !manager.offhandItems.isMode("Projectile")) {
               int projectileSlot = (int)(manager.projectileSlot.getValue().floatValue() - 1.0F);
               if (ContainerUtils.getItemCount(Items.EGG) > 0) {
                  this.invSwapItem(actions, projectileSlot, Items.EGG);
               } else if (ContainerUtils.getItemCount(Items.SNOWBALL) > 0) {
                  this.invSwapItem(actions, projectileSlot, Items.SNOWBALL);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean invManPostOffhand(List<StealerN.InstantSlotMove> actions) {
      if (this.invManage.getValue() && this.instant.getValue()) {
         if (mc.currentScreen instanceof GenericContainerScreen container) {
            GenericContainerScreenHandler menu = (GenericContainerScreenHandler)container.getScreenHandler();
            InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
            Dropdown offhandItems = manager.offhandItems;
            ItemStack offhandStack = mc.player.getOffHandStack();
            if (offhandItems.isMode("Golden Apple")) {
               int slot = this.getItemSlot(Items.GOLDEN_APPLE);
               if (slot == -1) {
                  slot = this.invToChestPos(InventoryUtils.getItemSlot(Items.GOLDEN_APPLE));
               }

               if (offhandStack != null && offhandStack.getCount() >= menu.getSlot(slot).getStack().getCount()) {
                  return true;
               }

               if (slot != -1) {
                  actions.add(new StealerN.InstantSlotMove(slot, -1, 2, null));
               }
            } else if (offhandItems.isMode("Projectile")) {
               ItemStack bestProjectile = ContainerUtils.getBestProjectile();
               boolean isInv = false;
               if (bestProjectile == null) {
                  bestProjectile = InventoryUtils.getBestProjectile();
                  isInv = true;
               }

               if (bestProjectile != null) {
                  int slotx = isInv ? this.invToChestPos(InventoryUtils.getItemStackSlot(bestProjectile)) : this.getItemStackSlot(bestProjectile);
                  if (offhandStack != null
                     && (offhandStack.getItem() == Items.EGG || offhandStack.getItem() == Items.SNOWBALL)
                     && offhandStack.getCount() >= menu.getSlot(slotx).getStack().getCount()) {
                     return true;
                  }

                  if (slotx != -1) {
                     actions.add(new StealerN.InstantSlotMove(slotx, -1, 2, null));
                  }
               }
            } else if (offhandItems.isMode("Fishing Rod")) {
               int slotxx = this.getItemSlot(Items.FISHING_ROD);
               if (slotxx == -1) {
                  slotxx = this.invToChestPos(InventoryUtils.getItemSlot(Items.FISHING_ROD));
               }

               if (slotxx != -1) {
                  actions.add(new StealerN.InstantSlotMove(slotxx, -1, 2, null));
               }
            } else if (offhandItems.isMode("Block")) {
               ItemStack bestBlock = ContainerUtils.getBestBlock();
               boolean isInvx = false;
               if (bestBlock == null) {
                  bestBlock = InventoryUtils.getBestBlock();
                  isInvx = true;
               }

               if (bestBlock != null) {
                  int slotxxx = isInvx ? this.invToChestPos(InventoryUtils.getItemStackSlot(bestBlock)) : this.getItemStackSlot(bestBlock);
                  if (slotxxx != -1) {
                     actions.add(new StealerN.InstantSlotMove(slotxxx, -1, 2, null));
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void swapUsingOffhand(int slotId) {
      ItemStack stackInSlot = mc.player.currentScreenHandler.getSlot(slotId).getStack();
      mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 40, SlotActionType.SWAP, mc.player);
      if (!((ItemStack)mc.player.getInventory().offHand.get(0)).isEmpty()) {
         for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.inventory == mc.player.getInventory() && slot.getStack().isEmpty()) {
               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 40, SlotActionType.SWAP, mc.player);
               break;
            }
         }
      }
   }

   public boolean swapUsingOffhandSmart(int slotId) {
      ItemStack stackInSlot = mc.player.currentScreenHandler.getSlot(slotId).getStack();
      if (!stackInSlot.isEmpty() && stackInSlot.getCount() < stackInSlot.getMaxCount()) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, mc.player);
         return true;
      } else {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 40, SlotActionType.SWAP, mc.player);
         if (!((ItemStack)mc.player.getInventory().offHand.get(0)).isEmpty()) {
            for (Slot slot : mc.player.currentScreenHandler.slots) {
               if (slot.inventory == mc.player.getInventory() && slot.getStack().isEmpty()) {
                  mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 40, SlotActionType.SWAP, mc.player);
                  break;
               }
            }
         }

         return false;
      }
   }

   private boolean tryBoostHotbarSlot(List<StealerN.InstantSlotMove> actions, int chestSlot, ItemStack stack) {
      InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
      if (manager == null) {
         return false;
      } else {
         OptionalInt targetSlot = this.resolveBoostSlot(manager, stack);
         if (targetSlot.isEmpty()) {
            return false;
         } else {
            actions.add(new StealerN.InstantSlotMove(chestSlot, targetSlot.getAsInt(), 4, stack));
            return true;
         }
      }
   }

   private boolean tryBoostHotbarDirect(GenericContainerScreenHandler menu, int chestSlot, ItemStack stack) {
      InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
      if (manager == null) {
         return false;
      } else {
         OptionalInt targetSlot = this.resolveBoostSlot(manager, stack);
         return targetSlot.isPresent() && this.performBoostSwap(menu.syncId, chestSlot, targetSlot.getAsInt());
      }
   }

   private OptionalInt resolveBoostSlot(InvManagerN manager, ItemStack stack) {
      if (!this.invManagerBoost.getValue() || stack == null || stack.isEmpty() || mc.player == null) {
         return OptionalInt.empty();
      } else if (!manager.checkConfig()) {
         return OptionalInt.empty();
      } else {
         if (manager.switchGoldenApple.getValue() && !manager.offhandItems.isMode("Golden Apple") && stack.getItem() == Items.GOLDEN_APPLE) {
            int slot = this.sliderToHotbarSlot(manager.goldenAppleSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchBlock.getValue()
            && !manager.offhandItems.isMode("Block")
            && stack.getItem() instanceof BlockItem blockItem
            && !Scaffold.invalidBlocks.contains(blockItem.getBlock())) {
            int slot = this.sliderToHotbarSlot(manager.blockSlot);
            if (slot != -1 && this.shouldSwapBlock(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchSword.getValue()) {
            int slot = this.sliderToHotbarSlot(manager.swordSlot);
            if (slot != -1 && this.shouldSwapSword(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchPickaxe.getValue() && stack.getItem() instanceof PickaxeItem) {
            int slot = this.sliderToHotbarSlot(manager.pickaxeSlot);
            if (slot != -1 && this.shouldSwapPickaxe(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchAxe.getValue() && stack.getItem() instanceof AxeItem && !InventoryUtils.isSharpnessAxe(stack)) {
            int slot = this.sliderToHotbarSlot(manager.axeSlot);
            if (slot != -1 && this.shouldSwapAxe(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchRod.getValue() && !manager.offhandItems.isMode("Fishing Rod") && stack.getItem() instanceof FishingRodItem) {
            int slot = this.sliderToHotbarSlot(manager.rodSlot);
            if (slot != -1 && this.shouldSwapRod(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchBow.getValue() && (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem)) {
            int slot = this.sliderToHotbarSlot(manager.bowSlot);
            if (slot != -1 && this.shouldSwapBow(manager, slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchEnderPearl.getValue() && stack.getItem() == Items.ENDER_PEARL) {
            int slot = this.sliderToHotbarSlot(manager.enderPearlSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchWaterBucket.getValue() && stack.getItem() == Items.WATER_BUCKET) {
            int slot = this.sliderToHotbarSlot(manager.waterBucketSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchFireball.getValue() && stack.getItem() == Items.FIRE_CHARGE) {
            int slot = this.sliderToHotbarSlot(manager.fireballSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (InvManagerN.keepProjectile.getValue()
            && manager.switchProjectile.getValue()
            && !manager.offhandItems.isMode("Projectile")
            && (stack.getItem() == Items.EGG || stack.getItem() == Items.SNOWBALL)) {
            int slot = this.sliderToHotbarSlot(manager.projectileSlot);
            if (slot != -1 && this.shouldSwapProjectile(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         return OptionalInt.empty();
      }
   }

   private int sliderToHotbarSlot(Slider slider) {
      if (slider == null) {
         return -1;
      } else {
         int raw = Math.round(slider.getValue().floatValue());
         raw = Math.max(1, Math.min(9, raw));
         return raw - 1;
      }
   }

   private boolean shouldSwapSword(int slot, ItemStack candidate) {
      if (!this.isSwordCandidate(candidate)) {
         return false;
      } else {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         return this.getWeaponDamage(candidate) > this.getWeaponDamage(current);
      }
   }

   private boolean isSwordCandidate(ItemStack stack) {
      return stack.getItem() instanceof SwordItem || InventoryUtils.isSharpnessAxe(stack);
   }

   private float getWeaponDamage(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof SwordItem) {
         return InventoryUtils.getSwordDamage(stack);
      } else {
         return InventoryUtils.isSharpnessAxe(stack) ? InventoryUtils.getAxeDamage(stack) : 0.0F;
      }
   }

   private boolean shouldSwapBlock(int slot, ItemStack candidate) {
      if (candidate.getItem() instanceof BlockItem blockItem && !Scaffold.invalidBlocks.contains(blockItem.getBlock())) {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         return current.getItem() instanceof BlockItem currentBlock && !Scaffold.invalidBlocks.contains(currentBlock.getBlock())
            ? candidate.getCount() > current.getCount()
            : true;
      } else {
         return false;
      }
   }

   private boolean shouldSwapPickaxe(int slot, ItemStack candidate) {
      if (!(candidate.getItem() instanceof PickaxeItem)) {
         return false;
      } else {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         float candidateScore = InventoryUtils.getToolScore(candidate);
         float currentScore = current.getItem() instanceof PickaxeItem ? InventoryUtils.getToolScore(current) : 0.0F;
         return candidateScore > currentScore;
      }
   }

   private boolean shouldSwapAxe(int slot, ItemStack candidate) {
      if (candidate.getItem() instanceof AxeItem && !InventoryUtils.isSharpnessAxe(candidate)) {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         float candidateScore = InventoryUtils.getToolScore(candidate);
         float currentScore = current.getItem() instanceof AxeItem && !InventoryUtils.isSharpnessAxe(current)
            ? InventoryUtils.getToolScore(current)
            : 0.0F;
         return candidateScore > currentScore;
      } else {
         return false;
      }
   }

   private boolean shouldSwapRod(int slot, ItemStack candidate) {
      if (!(candidate.getItem() instanceof FishingRodItem)) {
         return false;
      } else {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         return !(current.getItem() instanceof FishingRodItem);
      }
   }

   private boolean shouldSwapBow(InvManagerN manager, int slot, ItemStack candidate) {
      float candidateScore = this.getBowScore(manager, candidate);
      if (candidateScore <= 0.0F) {
         return false;
      } else {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         float currentScore = this.getBowScore(manager, current);
         return candidateScore > currentScore;
      }
   }

   private float getBowScore(InvManagerN manager, ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return 0.0F;
      } else if (stack.getItem() instanceof CrossbowItem) {
         return InventoryUtils.getCrossbowScore(stack);
      } else if (!(stack.getItem() instanceof BowItem)) {
         return 0.0F;
      } else if (manager.preferBow.isMode("Power Bow")) {
         return InventoryUtils.getPowerBowScore(stack);
      } else {
         return manager.preferBow.isMode("Punch Bow")
            ? InventoryUtils.getPunchBowScore(stack)
            : Math.max(InventoryUtils.getPowerBowScore(stack), InventoryUtils.getPunchBowScore(stack));
      }
   }

   private boolean shouldSwapItemByCount(int slot, ItemStack candidate) {
      ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
      if (current.isEmpty()) {
         return true;
      } else {
         return current.getItem() != candidate.getItem() ? true : candidate.getCount() > current.getCount();
      }
   }

   private boolean shouldSwapProjectile(int slot, ItemStack candidate) {
      if (candidate.getItem() != Items.EGG && candidate.getItem() != Items.SNOWBALL) {
         return false;
      } else {
         ItemStack current = (ItemStack)mc.player.getInventory().main.get(slot);
         return !current.isEmpty() && (current.getItem() == Items.EGG || current.getItem() == Items.SNOWBALL)
            ? candidate.getCount() > current.getCount()
            : true;
      }
   }

   private boolean performBoostSwap(int syncId, int chestSlot, int hotbarIndex) {
      if (mc.interactionManager == null || mc.player == null) {
         return false;
      } else if (hotbarIndex >= 0 && hotbarIndex <= 8) {
         mc.interactionManager.clickSlot(syncId, chestSlot, hotbarIndex, SlotActionType.SWAP, mc.player);
         if (mc.player.currentScreenHandler == null) {
            return false;
         } else {
            Slot slot = mc.player.currentScreenHandler.getSlot(chestSlot);
            if (slot != null && !slot.getStack().isEmpty()) {
               mc.interactionManager.clickSlot(syncId, chestSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            }

            return true;
         }
      } else {
         return false;
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

   private boolean isBestItemInChest(GenericContainerScreenHandler menu, ItemStack stack) {
      if (!InventoryUtils.isGodItem(stack) && !InventoryUtils.isSharpnessAxe(stack)) {
         for (int i = 0; i < menu.getRows() * 9; i++) {
            ItemStack checkStack = menu.getSlot(i).getStack();
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

         return true;
      } else {
         return true;
      }
   }

   private boolean isChestEmpty(GenericContainerScreenHandler menu) {
      for (int i = 0; i < menu.getRows() * 9; i++) {
         ItemStack item = menu.getSlot(i).getStack();
         if (!item.isEmpty() && isItemUseful(item) && this.isBestItemInChest(menu, item)) {
            return false;
         }
      }

      return true;
   }

   public static int getFirstEmptySlot() {
      Inventory inventory = mc.player.getInventory();

      for (int i = 0; i < inventory.size(); i++) {
         if (i != 8 && inventory.getStack(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isItemUseful(ItemStack stack) {
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

   private BlockHitResult raycastForChest() {
      Rotation rotation = RotationUtils.getRotation();
      float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
      Vec3d start = mc.player.getCameraPosVec(tickDelta);
      Vec3d direction = mc.player.getRotationVec(tickDelta);
      if (rotation != null) {
         direction = rotation.getRotationVec();
      }

      Vec3d end = start.add(direction.multiply(4.5));
      double stepSize = 0.1;
      int steps = (int)(4.5 / stepSize);
      Vec3d step = direction.multiply(stepSize);
      Vec3d current = start;

      for (int i = 0; i <= steps; i++) {
         BlockPos pos = new BlockPos((int)Math.floor(current.x), (int)Math.floor(current.y), (int)Math.floor(current.z));
         BlockState state = mc.world.getBlockState(pos);
         if (this.isChestBlock(state) && this.hasChestCollision(pos, current)) {
            return ClientRayTraceUtil.getFacedContainerBlock(mc.player.getYaw(), mc.player.getPitch());
         }

         current = current.add(step);
      }

      return null;
   }

   private boolean isChestBlock(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }

   private boolean hasChestCollision(BlockPos pos, Vec3d rayPos) {
      try {
         double minX = pos.getX() + 0.1;
         double minY = pos.getY() + 0.1;
         double minZ = pos.getZ() + 0.1;
         double maxX = pos.getX() + 0.9;
         double maxY = pos.getY() + 0.9;
         double maxZ = pos.getZ() + 0.9;
         return rayPos.x >= minX
            && rayPos.x <= maxX
            && rayPos.y >= minY
            && rayPos.y <= maxY
            && rayPos.z >= minZ
            && rayPos.z <= maxZ;
      } catch (Exception var15) {
         return false;
      }
   }

   private int nextChestEmptySlot() {
      for (Slot slot : mc.player.currentScreenHandler.slots) {
         if (slot.inventory != mc.player.getInventory() && slot.getStack().isEmpty()) {
            return slot.id;
         }
      }

      return -1;
   }

   private int invToChestPos(int slot) {
      int handlerSlots = mc.player.currentScreenHandler.slots.size();
      return slot < 9 ? handlerSlots - 9 + slot : handlerSlots - 45 + slot;
   }

   private void invThrowItem(List<StealerN.InstantSlotMove> actions, ItemStack item) {
      if (InventoryUtils.isItemValid(item)) {
         int itemSlot = InventoryUtils.getItemStackSlot(item);
         if (itemSlot != -1) {
            actions.add(new StealerN.InstantSlotMove(this.invToChestPos(itemSlot), -1, 0, null));
         }
      }
   }

   private void invSwapItem(List<StealerN.InstantSlotMove> actions, int targetSlot, ItemStack bestItem) {
      ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot) && bestItem != currentSlot) {
         int bestItemSlot = this.getItemStackSlot(bestItem);
         if (bestItemSlot != -1) {
            actions.add(new StealerN.InstantSlotMove(bestItemSlot, this.invToChestPos(targetSlot), 1, bestItem));
         }
      }
   }

   private void invSwapItem(List<StealerN.InstantSlotMove> actions, int targetSlot, Item item) {
      ItemStack currentSlot = (ItemStack)mc.player.getInventory().main.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot)) {
         int bestItemSlot = this.getItemSlot(item);
         if (bestItemSlot != -1) {
            ItemStack bestItemStack = mc.player.currentScreenHandler.getSlot(bestItemSlot).getStack();
            if (currentSlot.getItem() != item || currentSlot.getCount() < bestItemStack.getCount()) {
               actions.add(new StealerN.InstantSlotMove(bestItemSlot, this.invToChestPos(targetSlot), 1, bestItemStack));
            }
         }
      }
   }

   private int getItemSlot(Item item) {
      if (item != null) {
         for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.inventory != mc.player.getInventory() && !slot.getStack().isEmpty() && slot.getStack().getItem().equals(item)) {
               return slot.id;
            }
         }
      }

      return -1;
   }

   private int getItemStackSlot(ItemStack stack) {
      if (stack != null) {
         for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.inventory != mc.player.getInventory() && !slot.getStack().isEmpty() && slot.getStack().equals(stack)) {
               return slot.id;
            }
         }
      }

      return -1;
   }

   record InstantSlotMove(int src, int dst, int action, ItemStack stack) {
   }
}
