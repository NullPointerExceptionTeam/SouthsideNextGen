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
import net.minecraft.class_1263;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_2281;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_437;
import net.minecraft.class_476;

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
   private class_437 lastTickScreen;
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

   public static boolean screenValid(class_437 screen) {
      if (!(screen instanceof class_476 container)) {
         return false;
      } else {
         String chestTitle = container.method_25440().getString();
         String chest = class_2561.method_43471("container.chest").getString();
         String largeChest = class_2561.method_43471("container.chestDouble").getString();
         return chestTitle.equals(chest) || chestTitle.equals(largeChest) || chestTitle.equals("Chest");
      }
   }

   @EventHandler(
      priority = 1
   )
   public void onMotion(MotionEvent e) {
      if (!e.getState().equals(Stage.POST)) {
         if (this.autoDisable.getValue() && mc.field_1687 != null) {
            if (!mc.field_1724.method_5805()) {
               this.setEnable(false);
               return;
            }

            if (mc.field_1724.field_6012 <= 1) {
               this.setEnable(false);
            }
         }

         ChestManager manager = Southside.get().getChestManager();
         class_437 currentScreen = mc.field_1755;
         if (currentScreen instanceof class_476 container) {
            if (mc.field_1724.field_7512 != container.method_17577()) {
               this.lastTickScreen = null;
               return;
            }

            class_1707 menu = (class_1707)container.method_17577();
            if (currentScreen != this.lastTickScreen) {
               timer.reset();
               this.nextDelay = (long)this.startDelay.getValue().floatValue();
            } else {
               String chestTitle = container.method_25440().getString();
               String chest = class_2561.method_43471("container.chest").getString();
               String largeChest = class_2561.method_43471("container.chestDouble").getString();
               String enderChest = class_2561.method_43471("container.enderchest").getString();
               if (chestTitle.equals(chest)
                  || chestTitle.equals(largeChest)
                  || chestTitle.equals("Chest")
                  || this.pickEnderChest.getValue() && chestTitle.equals(enderChest)) {
                  if (!this.instant.getValue() && this.isChestEmpty(menu) && timer.hasPassed(this.nextDelay)) {
                     if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                        manager.stolenAll();
                        mc.field_1724.method_7346();
                        timer3.reset();
                     }
                  } else if (this.instant.getValue() && timer.hasPassed(this.nextDelay)) {
                     boolean willDelay = true;
                     CopyOnWriteArrayList<StealerN.InstantSlotMove> moves = new CopyOnWriteArrayList<>();
                     this.reload(moves, menu);
                     if (moves.isEmpty()) {
                        if (timer.hasPassed(this.nextDelay)) {
                           if (this.instant.getValue() && InvUtils.isInventoryFull(mc.field_1724.method_31548())) {
                              mc.field_1724.method_7346();
                              this.warning("The inventory is full while instant stealing.", new Object[0]);
                              this.warning("Please clean your inventory and reopen the chest", new Object[0]);
                              return;
                           }

                           if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                              manager.stolenAll();
                              mc.field_1724.method_7346();
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

                              mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.src, 40, class_1713.field_7791, mc.field_1724);
                              mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, empty, 40, class_1713.field_7791, mc.field_1724);
                              break;
                           case 1:
                              mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.src, 40, class_1713.field_7791, mc.field_1724);
                              if (!((class_1799)mc.field_1724.method_31548().field_7544.get(0)).method_7960()) {
                                 mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.dst, 40, class_1713.field_7791, mc.field_1724);
                                 if (this.doubleClickSmart.getValue() && move.stack != null) {
                                    boolean canDoubleClick = !move.stack.method_7960() && move.stack.method_7947() < move.stack.method_7914();
                                    if (canDoubleClick) {
                                       mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.dst, 0, class_1713.field_7790, mc.field_1724);
                                       mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.dst, 0, class_1713.field_7793, mc.field_1724);
                                       if (this.halfDoubleClick.getValue()) {
                                          mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.dst, 0, class_1713.field_7790, mc.field_1724);
                                       }
                                    }
                                 }
                              }
                              break;
                           case 2:
                              mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.src, 40, class_1713.field_7791, mc.field_1724);
                              break;
                           case 3:
                              mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, move.src, 0, class_1713.field_7794, mc.field_1724);
                              if (this.smart.getValue()) {
                                 timer.reset();
                                 this.scheduleNextDelay();
                                 return;
                              }
                              break;
                           case 4:
                              this.performBoostSwap(mc.field_1724.field_7512.field_7763, move.src, move.dst);
                        }

                        this.reload(moves, menu);
                        if (moves.isEmpty()) {
                           if (timer.hasPassed(this.nextDelay)) {
                              if (this.instant.getValue() && InvUtils.isInventoryFull(mc.field_1724.method_31548())) {
                                 mc.field_1724.method_7346();
                                 this.warning("The inventory is full while instant stealing.", new Object[0]);
                                 this.warning("Please clean your inventory and reopen the chest", new Object[0]);
                                 break;
                              }

                              if (timer3.hasPassed(this.closeDelay.getValue().intValue())) {
                                 manager.stolenAll();
                                 mc.field_1724.method_7346();
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
                     List<Integer> slots = IntStream.range(0, menu.method_17388() * 9).boxed().collect(Collectors.toList());
                     Collections.shuffle(slots);

                     for (Integer pSlotId : slots) {
                        class_1799 stack = menu.method_7611(pSlotId).method_7677();
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
                                 mc.field_1724.method_7346();
                              } else if (stack.method_7914() <= 1) {
                                 if (slot < 9) {
                                    mc.field_1761.method_2906(menu.field_7763, pSlotId, slot, class_1713.field_7791, mc.field_1724);
                                 } else {
                                    mc.field_1761.method_2906(menu.field_7763, slot + 18, 8, class_1713.field_7791, mc.field_1724);
                                    mc.field_1761.method_2906(menu.field_7763, pSlotId, 8, class_1713.field_7791, mc.field_1724);
                                 }
                              } else if (timer2.hasPassed(this.delay1.getValue().longValue())) {
                                 mc.field_1761.method_2906(menu.field_7763, pSlotId, 0, class_1713.field_7794, mc.field_1724);
                                 timer2.reset();
                              }
                           } else {
                              mc.field_1761.method_2906(menu.field_7763, pSlotId, 0, class_1713.field_7794, mc.field_1724);
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

   private void reload(List<StealerN.InstantSlotMove> moves, class_1707 menu) {
      moves.clear();
      List<Integer> slots = IntStream.range(0, menu.method_17388() * 9).boxed().collect(Collectors.toList());
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
               class_1799 stack = menu.method_7611(pSlotId).method_7677();
               if (isItemUseful(stack)
                  && this.isBestItemInChest(menu, stack)
                  && (!this.invManagerBoost.getValue() || !this.tryBoostHotbarSlot(moves, pSlotId, stack))) {
                  for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
                     if (slot.field_7871 == mc.field_1724.method_31548() && slot.method_7677().method_7960()) {
                        StealerN.InstantSlotMove target1 = offhands.stream().filter(f -> f.dst == slot.field_7874).findFirst().orElse(null);
                        StealerN.InstantSlotMove target = moves.stream().filter(f -> f.dst == slot.field_7874).findFirst().orElse(target1);
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
            class_1799 stack = menu.method_7611(pSlotIdx).method_7677();
            if (isItemUseful(stack)
               && this.isBestItemInChest(menu, stack)
               && (!this.invManagerBoost.getValue() || !this.tryBoostHotbarSlot(moves, pSlotIdx, stack))) {
               for (class_1735 slotx : mc.field_1724.field_7512.field_7761) {
                  if (slotx.field_7871 == mc.field_1724.method_31548() && slotx.method_7677().method_7960()) {
                     StealerN.InstantSlotMove target = moves.stream().filter(f -> f.dst == slotx.field_7874).findFirst().orElse(null);
                     if (target == null) {
                        boolean canDoubleClick = !stack.method_7960() && stack.method_7947() < stack.method_7914();
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
         if (mc.field_1755 instanceof class_476 container) {
            class_1707 menu = (class_1707)container.method_17577();
            InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
            if (!manager.checkConfig()) {
               this.message("Inv 配置错误..", new Object[0]);
               return false;
            }

            if (manager.switchGoldenApple.getValue() && !manager.offhandItems.isMode("Golden Apple")) {
               this.invSwapItem(actions, (int)(manager.goldenAppleSlot.getValue().floatValue() - 1.0F), class_1802.field_8463);
            }

            if (manager.switchBlock.getValue()) {
               int blockSlot = (int)(manager.blockSlot.getValue().floatValue() - 1.0F);
               class_1799 currentBlock = (class_1799)mc.field_1724.method_31548().field_7547.get(blockSlot);
               class_1799 bestBlock = ContainerUtils.getBestBlock();
               if (bestBlock != null
                  && (bestBlock.method_7947() > currentBlock.method_7947() || InventoryUtils.isInvalidBlock(currentBlock))
                  && !manager.offhandItems.isMode("Block")) {
                  this.invSwapItem(actions, blockSlot, bestBlock);
               }
            }

            if (manager.switchSword.getValue()) {
               int slotxx = (int)(manager.swordSlot.getValue().floatValue() - 1.0F);
               class_1799 currentSword = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxx);
               class_1799 bestSword = ContainerUtils.getBestSword();
               class_1799 bestShapeAxe = ContainerUtils.getBestShapeAxe();
               if (ContainerUtils.getAxeDamage(bestShapeAxe) > ContainerUtils.getSwordDamage(bestSword)) {
                  bestSword = bestShapeAxe;
               }

               if (bestSword != null) {
                  float currentDamage = currentSword.method_7909() instanceof class_1829
                     ? ContainerUtils.getSwordDamage(currentSword)
                     : ContainerUtils.getAxeDamage(currentSword);
                  float bestWeaponDamage = bestSword.method_7909() instanceof class_1829
                     ? ContainerUtils.getSwordDamage(bestSword)
                     : ContainerUtils.getAxeDamage(bestSword);
                  if (bestWeaponDamage > currentDamage) {
                     this.invSwapItem(actions, slotxx, bestSword);
                  }
               }
            }

            if (manager.switchPickaxe.getValue()) {
               int slotxxx = (int)(manager.pickaxeSlot.getValue().floatValue() - 1.0F);
               class_1799 bestPickaxe = ContainerUtils.getBestPickaxe();
               class_1799 currentPickaxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
               if (bestPickaxe != null
                  && bestPickaxe.method_7909() instanceof class_1810
                  && (
                     ContainerUtils.getToolScore(bestPickaxe) > ContainerUtils.getToolScore(currentPickaxe)
                        || !(currentPickaxe.method_7909() instanceof class_1810)
                  )) {
                  this.invSwapItem(actions, slotxxx, bestPickaxe);
               }
            }

            if (manager.switchAxe.getValue()) {
               int slotxxx = (int)(manager.axeSlot.getValue().floatValue() - 1.0F);
               class_1799 bestAxe = ContainerUtils.getBestAxe();
               class_1799 currentAxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
               if (bestAxe != null
                  && bestAxe.method_7909() instanceof class_1743
                  && (ContainerUtils.getToolScore(bestAxe) > ContainerUtils.getToolScore(currentAxe) || !(currentAxe.method_7909() instanceof class_1743))) {
                  this.invSwapItem(actions, slotxxx, bestAxe);
               }
            }

            if (manager.switchRod.getValue() && !manager.offhandItems.isMode("Fishing Rod")) {
               int slotxxx = (int)(manager.rodSlot.getValue().floatValue() - 1.0F);
               class_1799 bestRod = ContainerUtils.getFishingRod();
               class_1799 currentRod = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
               if (!(currentRod.method_7909() instanceof class_1787)) {
                  this.invSwapItem(actions, slotxxx, bestRod);
               }
            }

            if (manager.switchEnderPearl.getValue()) {
               this.invSwapItem(actions, (int)(manager.enderPearlSlot.getValue().floatValue() - 1.0F), class_1802.field_8634);
            }

            if (manager.switchWaterBucket.getValue()) {
               this.invSwapItem(actions, (int)(manager.waterBucketSlot.getValue().floatValue() - 1.0F), class_1802.field_8705);
            }

            if (manager.switchFireball.getValue()) {
               this.invSwapItem(actions, (int)(manager.fireballSlot.getValue().floatValue() - 1.0F), class_1802.field_8814);
            }

            if (InvManagerN.keepProjectile.getValue() && manager.switchProjectile.getValue() && !manager.offhandItems.isMode("Projectile")) {
               int projectileSlot = (int)(manager.projectileSlot.getValue().floatValue() - 1.0F);
               if (ContainerUtils.getItemCount(class_1802.field_8803) > 0) {
                  this.invSwapItem(actions, projectileSlot, class_1802.field_8803);
               } else if (ContainerUtils.getItemCount(class_1802.field_8543) > 0) {
                  this.invSwapItem(actions, projectileSlot, class_1802.field_8543);
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
         if (mc.field_1755 instanceof class_476 container) {
            class_1707 menu = (class_1707)container.method_17577();
            InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
            Dropdown offhandItems = manager.offhandItems;
            class_1799 offhandStack = mc.field_1724.method_6079();
            if (offhandItems.isMode("Golden Apple")) {
               int slot = this.getItemSlot(class_1802.field_8463);
               if (slot == -1) {
                  slot = this.invToChestPos(InventoryUtils.getItemSlot(class_1802.field_8463));
               }

               if (offhandStack != null && offhandStack.method_7947() >= menu.method_7611(slot).method_7677().method_7947()) {
                  return true;
               }

               if (slot != -1) {
                  actions.add(new StealerN.InstantSlotMove(slot, -1, 2, null));
               }
            } else if (offhandItems.isMode("Projectile")) {
               class_1799 bestProjectile = ContainerUtils.getBestProjectile();
               boolean isInv = false;
               if (bestProjectile == null) {
                  bestProjectile = InventoryUtils.getBestProjectile();
                  isInv = true;
               }

               if (bestProjectile != null) {
                  int slotx = isInv ? this.invToChestPos(InventoryUtils.getItemStackSlot(bestProjectile)) : this.getItemStackSlot(bestProjectile);
                  if (offhandStack != null
                     && (offhandStack.method_7909() == class_1802.field_8803 || offhandStack.method_7909() == class_1802.field_8543)
                     && offhandStack.method_7947() >= menu.method_7611(slotx).method_7677().method_7947()) {
                     return true;
                  }

                  if (slotx != -1) {
                     actions.add(new StealerN.InstantSlotMove(slotx, -1, 2, null));
                  }
               }
            } else if (offhandItems.isMode("Fishing Rod")) {
               int slotxx = this.getItemSlot(class_1802.field_8378);
               if (slotxx == -1) {
                  slotxx = this.invToChestPos(InventoryUtils.getItemSlot(class_1802.field_8378));
               }

               if (slotxx != -1) {
                  actions.add(new StealerN.InstantSlotMove(slotxx, -1, 2, null));
               }
            } else if (offhandItems.isMode("Block")) {
               class_1799 bestBlock = ContainerUtils.getBestBlock();
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
      class_1799 stackInSlot = mc.field_1724.field_7512.method_7611(slotId).method_7677();
      mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, slotId, 40, class_1713.field_7791, mc.field_1724);
      if (!((class_1799)mc.field_1724.method_31548().field_7544.get(0)).method_7960()) {
         for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
            if (slot.field_7871 == mc.field_1724.method_31548() && slot.method_7677().method_7960()) {
               mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, slot.field_7874, 40, class_1713.field_7791, mc.field_1724);
               break;
            }
         }
      }
   }

   public boolean swapUsingOffhandSmart(int slotId) {
      class_1799 stackInSlot = mc.field_1724.field_7512.method_7611(slotId).method_7677();
      if (!stackInSlot.method_7960() && stackInSlot.method_7947() < stackInSlot.method_7914()) {
         mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, slotId, 0, class_1713.field_7794, mc.field_1724);
         return true;
      } else {
         mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, slotId, 40, class_1713.field_7791, mc.field_1724);
         if (!((class_1799)mc.field_1724.method_31548().field_7544.get(0)).method_7960()) {
            for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
               if (slot.field_7871 == mc.field_1724.method_31548() && slot.method_7677().method_7960()) {
                  mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, slot.field_7874, 40, class_1713.field_7791, mc.field_1724);
                  break;
               }
            }
         }

         return false;
      }
   }

   private boolean tryBoostHotbarSlot(List<StealerN.InstantSlotMove> actions, int chestSlot, class_1799 stack) {
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

   private boolean tryBoostHotbarDirect(class_1707 menu, int chestSlot, class_1799 stack) {
      InvManagerN manager = Southside.get().getModuleManager().get(InvManagerN.class);
      if (manager == null) {
         return false;
      } else {
         OptionalInt targetSlot = this.resolveBoostSlot(manager, stack);
         return targetSlot.isPresent() && this.performBoostSwap(menu.field_7763, chestSlot, targetSlot.getAsInt());
      }
   }

   private OptionalInt resolveBoostSlot(InvManagerN manager, class_1799 stack) {
      if (!this.invManagerBoost.getValue() || stack == null || stack.method_7960() || mc.field_1724 == null) {
         return OptionalInt.empty();
      } else if (!manager.checkConfig()) {
         return OptionalInt.empty();
      } else {
         if (manager.switchGoldenApple.getValue() && !manager.offhandItems.isMode("Golden Apple") && stack.method_7909() == class_1802.field_8463) {
            int slot = this.sliderToHotbarSlot(manager.goldenAppleSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchBlock.getValue()
            && !manager.offhandItems.isMode("Block")
            && stack.method_7909() instanceof class_1747 blockItem
            && !Scaffold.invalidBlocks.contains(blockItem.method_7711())) {
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

         if (manager.switchPickaxe.getValue() && stack.method_7909() instanceof class_1810) {
            int slot = this.sliderToHotbarSlot(manager.pickaxeSlot);
            if (slot != -1 && this.shouldSwapPickaxe(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchAxe.getValue() && stack.method_7909() instanceof class_1743 && !InventoryUtils.isSharpnessAxe(stack)) {
            int slot = this.sliderToHotbarSlot(manager.axeSlot);
            if (slot != -1 && this.shouldSwapAxe(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchRod.getValue() && !manager.offhandItems.isMode("Fishing Rod") && stack.method_7909() instanceof class_1787) {
            int slot = this.sliderToHotbarSlot(manager.rodSlot);
            if (slot != -1 && this.shouldSwapRod(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchBow.getValue() && (stack.method_7909() instanceof class_1753 || stack.method_7909() instanceof class_1764)) {
            int slot = this.sliderToHotbarSlot(manager.bowSlot);
            if (slot != -1 && this.shouldSwapBow(manager, slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchEnderPearl.getValue() && stack.method_7909() == class_1802.field_8634) {
            int slot = this.sliderToHotbarSlot(manager.enderPearlSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchWaterBucket.getValue() && stack.method_7909() == class_1802.field_8705) {
            int slot = this.sliderToHotbarSlot(manager.waterBucketSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (manager.switchFireball.getValue() && stack.method_7909() == class_1802.field_8814) {
            int slot = this.sliderToHotbarSlot(manager.fireballSlot);
            if (slot != -1 && this.shouldSwapItemByCount(slot, stack)) {
               return OptionalInt.of(slot);
            }
         }

         if (InvManagerN.keepProjectile.getValue()
            && manager.switchProjectile.getValue()
            && !manager.offhandItems.isMode("Projectile")
            && (stack.method_7909() == class_1802.field_8803 || stack.method_7909() == class_1802.field_8543)) {
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

   private boolean shouldSwapSword(int slot, class_1799 candidate) {
      if (!this.isSwordCandidate(candidate)) {
         return false;
      } else {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         return this.getWeaponDamage(candidate) > this.getWeaponDamage(current);
      }
   }

   private boolean isSwordCandidate(class_1799 stack) {
      return stack.method_7909() instanceof class_1829 || InventoryUtils.isSharpnessAxe(stack);
   }

   private float getWeaponDamage(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return 0.0F;
      } else if (stack.method_7909() instanceof class_1829) {
         return InventoryUtils.getSwordDamage(stack);
      } else {
         return InventoryUtils.isSharpnessAxe(stack) ? InventoryUtils.getAxeDamage(stack) : 0.0F;
      }
   }

   private boolean shouldSwapBlock(int slot, class_1799 candidate) {
      if (candidate.method_7909() instanceof class_1747 blockItem && !Scaffold.invalidBlocks.contains(blockItem.method_7711())) {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         return current.method_7909() instanceof class_1747 currentBlock && !Scaffold.invalidBlocks.contains(currentBlock.method_7711())
            ? candidate.method_7947() > current.method_7947()
            : true;
      } else {
         return false;
      }
   }

   private boolean shouldSwapPickaxe(int slot, class_1799 candidate) {
      if (!(candidate.method_7909() instanceof class_1810)) {
         return false;
      } else {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         float candidateScore = InventoryUtils.getToolScore(candidate);
         float currentScore = current.method_7909() instanceof class_1810 ? InventoryUtils.getToolScore(current) : 0.0F;
         return candidateScore > currentScore;
      }
   }

   private boolean shouldSwapAxe(int slot, class_1799 candidate) {
      if (candidate.method_7909() instanceof class_1743 && !InventoryUtils.isSharpnessAxe(candidate)) {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         float candidateScore = InventoryUtils.getToolScore(candidate);
         float currentScore = current.method_7909() instanceof class_1743 && !InventoryUtils.isSharpnessAxe(current)
            ? InventoryUtils.getToolScore(current)
            : 0.0F;
         return candidateScore > currentScore;
      } else {
         return false;
      }
   }

   private boolean shouldSwapRod(int slot, class_1799 candidate) {
      if (!(candidate.method_7909() instanceof class_1787)) {
         return false;
      } else {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         return !(current.method_7909() instanceof class_1787);
      }
   }

   private boolean shouldSwapBow(InvManagerN manager, int slot, class_1799 candidate) {
      float candidateScore = this.getBowScore(manager, candidate);
      if (candidateScore <= 0.0F) {
         return false;
      } else {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         float currentScore = this.getBowScore(manager, current);
         return candidateScore > currentScore;
      }
   }

   private float getBowScore(InvManagerN manager, class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return 0.0F;
      } else if (stack.method_7909() instanceof class_1764) {
         return InventoryUtils.getCrossbowScore(stack);
      } else if (!(stack.method_7909() instanceof class_1753)) {
         return 0.0F;
      } else if (manager.preferBow.isMode("Power Bow")) {
         return InventoryUtils.getPowerBowScore(stack);
      } else {
         return manager.preferBow.isMode("Punch Bow")
            ? InventoryUtils.getPunchBowScore(stack)
            : Math.max(InventoryUtils.getPowerBowScore(stack), InventoryUtils.getPunchBowScore(stack));
      }
   }

   private boolean shouldSwapItemByCount(int slot, class_1799 candidate) {
      class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
      if (current.method_7960()) {
         return true;
      } else {
         return current.method_7909() != candidate.method_7909() ? true : candidate.method_7947() > current.method_7947();
      }
   }

   private boolean shouldSwapProjectile(int slot, class_1799 candidate) {
      if (candidate.method_7909() != class_1802.field_8803 && candidate.method_7909() != class_1802.field_8543) {
         return false;
      } else {
         class_1799 current = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
         return !current.method_7960() && (current.method_7909() == class_1802.field_8803 || current.method_7909() == class_1802.field_8543)
            ? candidate.method_7947() > current.method_7947()
            : true;
      }
   }

   private boolean performBoostSwap(int syncId, int chestSlot, int hotbarIndex) {
      if (mc.field_1761 == null || mc.field_1724 == null) {
         return false;
      } else if (hotbarIndex >= 0 && hotbarIndex <= 8) {
         mc.field_1761.method_2906(syncId, chestSlot, hotbarIndex, class_1713.field_7791, mc.field_1724);
         if (mc.field_1724.field_7512 == null) {
            return false;
         } else {
            class_1735 slot = mc.field_1724.field_7512.method_7611(chestSlot);
            if (slot != null && !slot.method_7677().method_7960()) {
               mc.field_1761.method_2906(syncId, chestSlot, 0, class_1713.field_7794, mc.field_1724);
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

   private boolean isBestItemInChest(class_1707 menu, class_1799 stack) {
      if (!InventoryUtils.isGodItem(stack) && !InventoryUtils.isSharpnessAxe(stack)) {
         for (int i = 0; i < menu.method_17388() * 9; i++) {
            class_1799 checkStack = menu.method_7611(i).method_7677();
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

         return true;
      } else {
         return true;
      }
   }

   private boolean isChestEmpty(class_1707 menu) {
      for (int i = 0; i < menu.method_17388() * 9; i++) {
         class_1799 item = menu.method_7611(i).method_7677();
         if (!item.method_7960() && isItemUseful(item) && this.isBestItemInChest(menu, item)) {
            return false;
         }
      }

      return true;
   }

   public static int getFirstEmptySlot() {
      class_1263 inventory = mc.field_1724.method_31548();

      for (int i = 0; i < inventory.method_5439(); i++) {
         if (i != 8 && inventory.method_5438(i).method_7960()) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isItemUseful(class_1799 stack) {
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

   private class_3965 raycastForChest() {
      Rotation rotation = RotationUtils.getRotation();
      float tickDelta = mc.method_61966().method_60637(false);
      class_243 start = mc.field_1724.method_5836(tickDelta);
      class_243 direction = mc.field_1724.method_5828(tickDelta);
      if (rotation != null) {
         direction = rotation.getRotationVec();
      }

      class_243 end = start.method_1019(direction.method_1021(4.5));
      double stepSize = 0.1;
      int steps = (int)(4.5 / stepSize);
      class_243 step = direction.method_1021(stepSize);
      class_243 current = start;

      for (int i = 0; i <= steps; i++) {
         class_2338 pos = new class_2338((int)Math.floor(current.field_1352), (int)Math.floor(current.field_1351), (int)Math.floor(current.field_1350));
         class_2680 state = mc.field_1687.method_8320(pos);
         if (this.isChestBlock(state) && this.hasChestCollision(pos, current)) {
            return ClientRayTraceUtil.getFacedContainerBlock(mc.field_1724.method_36454(), mc.field_1724.method_36455());
         }

         current = current.method_1019(step);
      }

      return null;
   }

   private boolean isChestBlock(class_2680 state) {
      return state.method_26204() instanceof class_2281 || state.method_26204() instanceof class_2336 || state.method_26204() instanceof class_2480;
   }

   private boolean hasChestCollision(class_2338 pos, class_243 rayPos) {
      try {
         double minX = pos.method_10263() + 0.1;
         double minY = pos.method_10264() + 0.1;
         double minZ = pos.method_10260() + 0.1;
         double maxX = pos.method_10263() + 0.9;
         double maxY = pos.method_10264() + 0.9;
         double maxZ = pos.method_10260() + 0.9;
         return rayPos.field_1352 >= minX
            && rayPos.field_1352 <= maxX
            && rayPos.field_1351 >= minY
            && rayPos.field_1351 <= maxY
            && rayPos.field_1350 >= minZ
            && rayPos.field_1350 <= maxZ;
      } catch (Exception var15) {
         return false;
      }
   }

   private int nextChestEmptySlot() {
      for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
         if (slot.field_7871 != mc.field_1724.method_31548() && slot.method_7677().method_7960()) {
            return slot.field_7874;
         }
      }

      return -1;
   }

   private int invToChestPos(int slot) {
      int handlerSlots = mc.field_1724.field_7512.field_7761.size();
      return slot < 9 ? handlerSlots - 9 + slot : handlerSlots - 45 + slot;
   }

   private void invThrowItem(List<StealerN.InstantSlotMove> actions, class_1799 item) {
      if (InventoryUtils.isItemValid(item)) {
         int itemSlot = InventoryUtils.getItemStackSlot(item);
         if (itemSlot != -1) {
            actions.add(new StealerN.InstantSlotMove(this.invToChestPos(itemSlot), -1, 0, null));
         }
      }
   }

   private void invSwapItem(List<StealerN.InstantSlotMove> actions, int targetSlot, class_1799 bestItem) {
      class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot) && bestItem != currentSlot) {
         int bestItemSlot = this.getItemStackSlot(bestItem);
         if (bestItemSlot != -1) {
            actions.add(new StealerN.InstantSlotMove(bestItemSlot, this.invToChestPos(targetSlot), 1, bestItem));
         }
      }
   }

   private void invSwapItem(List<StealerN.InstantSlotMove> actions, int targetSlot, class_1792 item) {
      class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot)) {
         int bestItemSlot = this.getItemSlot(item);
         if (bestItemSlot != -1) {
            class_1799 bestItemStack = mc.field_1724.field_7512.method_7611(bestItemSlot).method_7677();
            if (currentSlot.method_7909() != item || currentSlot.method_7947() < bestItemStack.method_7947()) {
               actions.add(new StealerN.InstantSlotMove(bestItemSlot, this.invToChestPos(targetSlot), 1, bestItemStack));
            }
         }
      }
   }

   private int getItemSlot(class_1792 item) {
      if (item != null) {
         for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
            if (slot.field_7871 != mc.field_1724.method_31548() && !slot.method_7677().method_7960() && slot.method_7677().method_7909().equals(item)) {
               return slot.field_7874;
            }
         }
      }

      return -1;
   }

   private int getItemStackSlot(class_1799 stack) {
      if (stack != null) {
         for (class_1735 slot : mc.field_1724.field_7512.field_7761) {
            if (slot.field_7871 != mc.field_1724.method_31548() && !slot.method_7677().method_7960() && slot.method_7677().equals(stack)) {
               return slot.field_7874;
            }
         }
      }

      return -1;
   }

   record InstantSlotMove(int src, int dst, int action, class_1799 stack) {
   }
}
