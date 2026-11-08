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
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1738;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1810;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_2266;
import net.minecraft.class_2269;
import net.minecraft.class_2292;
import net.minecraft.class_2349;
import net.minecraft.class_2354;
import net.minecraft.class_2356;
import net.minecraft.class_2399;
import net.minecraft.class_2440;
import net.minecraft.class_2482;
import net.minecraft.class_2510;
import net.minecraft.class_2533;
import net.minecraft.class_2577;
import net.minecraft.class_2815;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_7923;
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

   public static boolean isItemUseful(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      } else if (stack.method_7909().equals(class_1802.field_8529)
         || stack.method_7909().equals(class_1802.field_8536)
         || stack.method_7909().equals(class_1802.field_8674)) {
         return false;
      } else if (InventoryUtils.isGodItem(stack)) {
         return true;
      } else if (stack.method_7964().getString().contains("点击使用")) {
         return false;
      } else if (stack.method_7909() instanceof class_1738) {
         float protection = InventoryUtils.getProtection(stack);
         if (InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) >= protection) {
            return false;
         } else {
            float bestArmor = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack));
            return !(protection < bestArmor);
         }
      } else if (stack.method_7909() instanceof class_1829) {
         return InventoryUtils.getBestSword() == stack;
      } else if (stack.method_7909() instanceof class_1810) {
         return InventoryUtils.getBestPickaxe() == stack;
      } else if (stack.method_7909() instanceof class_1743 && !InventoryUtils.isSharpnessAxe(stack)) {
         return InventoryUtils.getBestAxe() == stack;
      } else if (stack.method_7909() instanceof class_1821) {
         return InventoryUtils.getBestShovel() == stack;
      } else if (stack.method_7909() instanceof class_1794) {
         return false;
      } else if (stack.method_7909() instanceof class_1764) {
         return InventoryUtils.getBestCrossbow() == stack;
      } else if (stack.method_7909() instanceof class_1753) {
         if (InventoryUtils.isPunchBow(stack) && InventoryUtils.getBestPunchBow() == stack) {
            return true;
         } else if (InventoryUtils.isPowerBow(stack) && InventoryUtils.getBestPowerBow() == stack) {
            return true;
         } else {
            return InventoryUtils.isFlameBow(stack) && InventoryUtils.getBestFlameBow() == stack
               ? true
               : InventoryUtils.getItemCount(class_1802.field_8102) <= 1;
         }
      } else if (stack.method_7909() == class_1802.field_8705 && InventoryUtils.getItemCount(class_1802.field_8705) > waterBucketCount.getValue().intValue()) {
         return false;
      } else if (stack.method_7909() == class_1802.field_8187 && InventoryUtils.getItemCount(class_1802.field_8187) > lavaBucketCount.getValue().intValue()) {
         return false;
      } else if (stack.method_7909() == class_1802.field_8786) {
         return InventoryUtils.getItemCount(class_1802.field_8786) <= 64;
      } else if (stack.method_7909() instanceof class_1787 && InventoryUtils.getItemCount(class_1802.field_8378) > 1) {
         return false;
      } else if ((stack.method_7909() == class_1802.field_8543 || stack.method_7909() == class_1802.field_8803) && !keepProjectile.getValue()) {
         return false;
      } else if (stack.method_7909() == class_1802.field_8626) {
         return true;
      } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2482) {
         return false;
      } else if (stack.method_7909() == class_1802.field_8662) {
         return false;
      } else if (stack.method_7909() != class_1802.field_8110
         && stack.method_7909() != class_1802.field_8145
         && stack.method_7909() != class_1802.field_8858
         && stack.method_7909() != class_1802.field_8200) {
         if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2356) {
            return false;
         } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2440) {
            return false;
         } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2510) {
            return false;
         } else if (stack.method_7909() != class_1802.field_8861
            && stack.method_7909() != class_1802.field_8179
            && stack.method_7909() != class_1802.field_8276) {
            if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2354) {
               return false;
            } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2349) {
               return false;
            } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2269) {
               return false;
            } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2577) {
               return false;
            } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2292) {
               return false;
            } else if (stack.method_7909() != class_1802.field_17535
               && stack.method_7909() != class_1802.field_17536
               && stack.method_7909() != class_1802.field_17537
               && stack.method_7909() != class_1802.field_17538
               && stack.method_7909() != class_1802.field_17539
               && stack.method_7909() != class_1802.field_17540
               && stack.method_7909() != class_1802.field_42688
               && stack.method_7909() != class_1802.field_37508) {
               if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2266) {
                  return false;
               } else if (stack.method_7909() instanceof class_1747 blockItem
                  && class_7923.field_41175.method_10221(blockItem.method_7711()).method_12832().contains("terracotta")) {
                  String path = class_7923.field_41175.method_10221(blockItem.method_7711()).method_12832();
                  return path.equals("blue_terracotta") || path.equals("red_terracotta");
               } else if (stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2533) {
                  return false;
               } else {
                  return stack.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2399
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
      if (e.getState() == Stage.PRE && this.autoDisable.getValue() && mc.field_1687 != null) {
         if (mc.field_1724 != null && !mc.field_1724.method_5805()) {
            this.setEnable(false);
            return;
         }

         if (mc.field_1724.field_6012 <= 1) {
            this.setEnable(false);
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent e) {
      if (!(mc.field_1755 instanceof OneConfigGui) && !this.checkConfig()) {
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

            if (mc.field_1724 != null && mc.field_1724.method_6115()) {
               this.usingItemTicks = 0;
               this.wasUsingItem = true;
            } else if (this.wasUsingItem) {
               this.usingItemTicks++;
               if (this.usingItemTicks >= 5) {
                  this.usingItemTicks = 0;
                  this.wasUsingItem = false;
               }
            }

            boolean isUsingItemWithDelay = mc.field_1724.method_6115()
               || this.wasUsingItem
               || KillAura.target != null && this.delay.getValue().doubleValue() != 0.0 && mc.field_1755 == null;
            if (!StealerN.isWorking()
               && !Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)
               && (this.inventoryOnly.getValue() ? mc.field_1755 instanceof class_490 : !isUsingItemWithDelay)) {
               if (!(mc.field_1755 instanceof class_465 container && container.method_17577().field_7763 != mc.field_1724.field_7498.field_7763)) {
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
                        for (int i = 0; i < mc.field_1724.method_31548().field_7548.size(); i++) {
                           class_1799 stack = (class_1799)mc.field_1724.method_31548().field_7548.get(i);
                           if (stack.method_7909() instanceof class_1738) {
                              class_1738 item = (class_1738)stack.method_7909();
                              if (!stack.method_7960()
                                 && InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) > InventoryUtils.getProtection(stack)) {
                                 this.clickSlot(mc.field_1724.field_7498.field_7763, 4 + (4 - i), 1, class_1713.field_7795, mc.field_1724);
                                 this.inventoryOpen = true;
                              }
                           }

                           this.mergePartialStacks();
                        }

                        for (int ix = 0; ix < mc.field_1724.method_31548().field_7547.size(); ix++) {
                           class_1799 stack = (class_1799)mc.field_1724.method_31548().field_7547.get(ix);
                           if (!stack.method_7960() && stack.method_7909() instanceof class_1738) {
                              class_1738 item = (class_1738)stack.method_7909();
                              float currentItemScore = InventoryUtils.getProtection(stack);
                              boolean isBestItem = InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) == currentItemScore;
                              boolean isBetterItem = InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) < currentItemScore;
                              if (isBestItem && isBetterItem) {
                                 if (ix < 9) {
                                    this.clickSlot(mc.field_1724.field_7498.field_7763, ix + 36, 0, class_1713.field_7794, mc.field_1724);
                                 } else {
                                    this.clickSlot(mc.field_1724.field_7498.field_7763, ix, 0, class_1713.field_7794, mc.field_1724);
                                 }

                                 this.inventoryOpen = true;
                              }
                           }
                        }
                     }

                     if (this.clickOffHand) {
                        this.clickSlot(mc.field_1724.field_7498.field_7763, 45, 0, class_1713.field_7790, mc.field_1724);
                        this.inventoryOpen = true;
                        this.clickOffHand = false;
                     }

                     if (this.offhandItems.isMode("Golden Apple")) {
                        class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
                        int slot = InventoryUtils.getItemSlot(class_1802.field_8463);
                        if (slot != -1) {
                           if (offHand.method_7909() == class_1802.field_8463) {
                              class_1799 goldenAppleStack = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
                              if (offHand.method_7947() + goldenAppleStack.method_7947() <= 64) {
                                 if (slot < 9) {
                                    this.clickSlot(mc.field_1724.field_7498.field_7763, slot + 36, 0, class_1713.field_7790, mc.field_1724);
                                 } else {
                                    this.clickSlot(mc.field_1724.field_7498.field_7763, slot, 0, class_1713.field_7790, mc.field_1724);
                                 }

                                 this.inventoryOpen = true;
                                 this.clickOffHand = true;
                              }
                           } else {
                              this.swapOffHand(slot);
                           }
                        }
                     } else if (this.offhandItems.isMode("Projectile")) {
                        class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
                        class_1799 bestProjectile = InventoryUtils.getBestProjectile();
                        if (bestProjectile != null) {
                           int slot = InventoryUtils.getItemStackSlot(bestProjectile);
                           boolean shouldSwap = false;
                           if (offHand.method_7909() != class_1802.field_8803 && offHand.method_7909() != class_1802.field_8543) {
                              shouldSwap = true;
                           } else if (offHand.method_7947() < bestProjectile.method_7947()) {
                              shouldSwap = true;
                           }

                           if (shouldSwap && slot != -1) {
                              this.swapOffHand(slot);
                           }
                        }
                     } else if (this.offhandItems.isMode("Fishing Rod")) {
                        class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
                        int slotx = InventoryUtils.getItemSlot(class_1802.field_8378);
                        if (slotx != -1 && offHand.method_7909() != class_1802.field_8378) {
                           this.swapOffHand(slotx);
                        }
                     } else if (this.offhandItems.isMode("Block")) {
                        class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
                        class_1799 bestBlock = InventoryUtils.getBestBlock();
                        if (bestBlock != null) {
                           int slotx = InventoryUtils.getItemStackSlot(bestBlock);
                           boolean shouldSwapx = false;
                           if (!InventoryUtils.isInvalidBlock(offHand)) {
                              if (offHand.method_7947() < bestBlock.method_7947()) {
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
                        this.swapItem((int)(this.goldenAppleSlot.getValue().floatValue() - 1.0F), class_1802.field_8463);
                     }

                     if (this.switchBlock.getValue()) {
                        int blockSlot = (int)(this.blockSlot.getValue().floatValue() - 1.0F);
                        class_1799 currentBlock = (class_1799)mc.field_1724.method_31548().field_7547.get(blockSlot);
                        class_1799 bestBlock = InventoryUtils.getBestBlock();
                        if (bestBlock != null
                           && (bestBlock.method_7947() > currentBlock.method_7947() || InventoryUtils.isInvalidBlock(currentBlock))
                           && !this.offhandItems.isMode("Block")) {
                           this.swapItem(blockSlot, bestBlock);
                        }

                        if (InventoryUtils.getBlockCountInInventory() > maxBlockSize.getValue().intValue()) {
                           class_1799 worstBlock = InventoryUtils.getWorstBlock();
                           this.throwItem(worstBlock);
                        }
                     }

                     if (this.switchSword.getValue()) {
                        int slotxx = (int)(this.swordSlot.getValue().floatValue() - 1.0F);
                        class_1799 currentSword = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxx);
                        class_1799 bestSword = InventoryUtils.getBestSword();
                        class_1799 bestShapeAxe = InventoryUtils.getBestShapeAxe();
                        if (InventoryUtils.getAxeDamage(bestShapeAxe) > InventoryUtils.getSwordDamage(bestSword)) {
                           bestSword = bestShapeAxe;
                        }

                        if (bestSword != null) {
                           float currentDamage = currentSword.method_7909() instanceof class_1829
                              ? InventoryUtils.getSwordDamage(currentSword)
                              : InventoryUtils.getAxeDamage(currentSword);
                           float bestWeaponDamage = bestSword.method_7909() instanceof class_1829
                              ? InventoryUtils.getSwordDamage(bestSword)
                              : InventoryUtils.getAxeDamage(bestSword);
                           if (bestWeaponDamage > currentDamage) {
                              this.swapItem(slotxx, bestSword);
                           }
                        }
                     }

                     if (this.switchPickaxe.getValue()) {
                        int slotxxx = (int)(this.pickaxeSlot.getValue().floatValue() - 1.0F);
                        class_1799 bestPickaxe = InventoryUtils.getBestPickaxe();
                        class_1799 currentPickaxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
                        if (bestPickaxe != null
                           && bestPickaxe.method_7909() instanceof class_1810
                           && (
                              InventoryUtils.getToolScore(bestPickaxe) > InventoryUtils.getToolScore(currentPickaxe)
                                 || !(currentPickaxe.method_7909() instanceof class_1810)
                           )) {
                           this.swapItem(slotxxx, bestPickaxe);
                        }
                     }

                     if (this.switchAxe.getValue()) {
                        int slotxxx = (int)(this.axeSlot.getValue().floatValue() - 1.0F);
                        class_1799 bestAxe = InventoryUtils.getBestAxe();
                        class_1799 currentAxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
                        if (bestAxe != null
                           && bestAxe.method_7909() instanceof class_1743
                           && (
                              InventoryUtils.getToolScore(bestAxe) > InventoryUtils.getToolScore(currentAxe)
                                 || !(currentAxe.method_7909() instanceof class_1743)
                           )) {
                           this.swapItem(slotxxx, bestAxe);
                        }
                     }

                     if (this.switchRod.getValue() && !this.offhandItems.isMode("Fishing Rod")) {
                        int slotxxx = (int)(this.rodSlot.getValue().floatValue() - 1.0F);
                        class_1799 bestRod = InventoryUtils.getFishingRod();
                        class_1799 currentRod = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
                        if (!(currentRod.method_7909() instanceof class_1787)) {
                           this.swapItem(slotxxx, bestRod);
                        }
                     }

                     if (this.switchBow.getValue()) {
                        int slotxxx = (int)(this.bowSlot.getValue().floatValue() - 1.0F);
                        class_1799 currentBow = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
                        class_1799 bestBow;
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

                        if (InventoryUtils.getItemCount(class_1802.field_8107) > maxArrowSize.getValue().intValue()) {
                           class_1799 worstArrow = InventoryUtils.getWorstArrow();
                           this.throwItem(worstArrow);
                        }
                     }

                     if (this.switchEnderPearl.getValue()) {
                        this.swapItem((int)(this.enderPearlSlot.getValue().floatValue() - 1.0F), class_1802.field_8634);
                     }

                     if (this.switchWaterBucket.getValue()) {
                        this.swapItem((int)(this.waterBucketSlot.getValue().floatValue() - 1.0F), class_1802.field_8705);
                     }

                     if (this.switchFireball.getValue()) {
                        this.swapItem((int)(this.fireballSlot.getValue().floatValue() - 1.0F), class_1802.field_8814);
                     }

                     if (keepProjectile.getValue()) {
                        if (InventoryUtils.getItemCount(class_1802.field_8803) + InventoryUtils.getItemCount(class_1802.field_8543)
                           > maxProjectileSize.getValue().intValue()) {
                           class_1799 worstProjectile = InventoryUtils.getWorstProjectile();
                           this.throwItem(worstProjectile);
                        }

                        if (this.switchProjectile.getValue() && !this.offhandItems.isMode("Projectile")) {
                           int projectileSlot = (int)(this.projectileSlot.getValue().floatValue() - 1.0F);
                           if (InventoryUtils.getItemCount(class_1802.field_8803) > 0) {
                              this.swapItem(projectileSlot, class_1802.field_8803);
                           } else if (InventoryUtils.getItemCount(class_1802.field_8543) > 0) {
                              this.swapItem(projectileSlot, class_1802.field_8543);
                           }
                        }
                     }

                     if (this.throwItems.getValue()) {
                        List<Integer> slots = IntStream.range(0, mc.field_1724.method_31548().field_7547.size()).boxed().collect(Collectors.toList());
                        Collections.shuffle(slots);

                        for (Integer slotxxxx : slots) {
                           class_1799 stack = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxxx);
                           if (!stack.method_7960() && !isItemUseful(stack)) {
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
         this.clickSlot(mc.field_1724.field_7498.field_7763, slot + 36, 40, class_1713.field_7791, mc.field_1724);
      } else {
         this.clickSlot(mc.field_1724.field_7498.field_7763, slot, 40, class_1713.field_7791, mc.field_1724);
      }

      this.inventoryOpen = true;
   }

   private void throwItem(class_1799 item) {
      if (InventoryUtils.isItemValid(item)) {
         int itemSlot = InventoryUtils.getItemStackSlot(item);
         if (itemSlot != -1) {
            if (itemSlot < 9) {
               this.clickSlot(mc.field_1724.field_7498.field_7763, itemSlot + 36, 1, class_1713.field_7795, mc.field_1724);
            } else {
               this.clickSlot(mc.field_1724.field_7498.field_7763, itemSlot, 1, class_1713.field_7795, mc.field_1724);
            }

            this.inventoryOpen = true;
         }
      }
   }

   private void swapItem(int targetSlot, class_1799 bestItem) {
      class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot) && bestItem != currentSlot) {
         int bestItemSlot = InventoryUtils.getItemStackSlot(bestItem);
         if (bestItemSlot != -1) {
            if (bestItemSlot < 9) {
               this.clickSlot(mc.field_1724.field_7498.field_7763, bestItemSlot + 36, targetSlot, class_1713.field_7791, mc.field_1724);
            } else {
               this.clickSlot(mc.field_1724.field_7498.field_7763, bestItemSlot, targetSlot, class_1713.field_7791, mc.field_1724);
            }

            this.inventoryOpen = true;
         }
      }
   }

   private void swapItem(int targetSlot, class_1792 item) {
      class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
      if (InventoryUtils.isItemValid(currentSlot)) {
         int bestItemSlot = InventoryUtils.getItemSlot(item);
         if (bestItemSlot != -1) {
            class_1799 bestItemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(bestItemSlot);
            if (currentSlot.method_7909() != item || currentSlot.method_7909() == item && currentSlot.method_7947() < bestItemStack.method_7947()) {
               if (bestItemSlot < 9) {
                  this.clickSlot(mc.field_1724.field_7498.field_7763, bestItemSlot + 36, targetSlot, class_1713.field_7791, mc.field_1724);
               } else {
                  this.clickSlot(mc.field_1724.field_7498.field_7763, bestItemSlot, targetSlot, class_1713.field_7791, mc.field_1724);
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
         if (mc.field_1724 != null && mc.field_1724.method_24828() && !mc.field_1724.method_5681()) {
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
         if (mc.field_1690.field_1903.method_1434() && mc.field_1724.method_24828()) {
            return;
         }

         mc.field_1690.field_1867.method_23481(false);
         mc.field_1724.method_5728(false);
         e.setSprint(false);
      }
   }

   private void clickSlot(int syncId, int slotId, int button, class_1713 actionType, class_1657 player) {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class) && !this.noMove.getValue()) {
         needStopSprint = true;
      }

      if (timer.hasPassed(this.delay.getValue().longValue()) || this.delay.getValue().doubleValue() == 0.0) {
         if (!this.inventoryOnly.getValue() && mc.field_1724.field_3919) {
            return;
         }

         mc.field_1761.method_2906(syncId, slotId, button, actionType, player);
         timer.reset();
         active = true;
         if (!this.inventoryOnly.getValue() && mc.field_1724.field_7512 == mc.field_1724.field_7498) {
            mc.field_1724.field_3944.method_52787(new class_2815(mc.field_1724.field_7498.field_7763));
         }
      }
   }

   private boolean hasMoveInput() {
      return mc.field_1690.field_1894.method_1434()
         || mc.field_1690.field_1881.method_1434()
         || mc.field_1690.field_1913.method_1434()
         || mc.field_1690.field_1849.method_1434()
         || mc.field_1690.field_1903.method_1434();
   }

   private boolean needsWork() {
      if (this.clickOffHand) {
         return true;
      } else {
         if (this.autoArmor.getValue()) {
            for (int i = 0; i < mc.field_1724.method_31548().field_7548.size(); i++) {
               class_1799 stack = (class_1799)mc.field_1724.method_31548().field_7548.get(i);
               if (!stack.method_7960()
                  && stack.method_7909() instanceof class_1738
                  && InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) > InventoryUtils.getProtection(stack)) {
                  return true;
               }
            }

            if (this.mergeSrc >= 0 && this.mergeDst >= 0) {
               return true;
            }

            List<class_1799> main = mc.field_1724.method_31548().field_7547;

            for (int ix = 0; ix < main.size(); ix++) {
               class_1799 stackA = main.get(ix);
               if (!stackA.method_7960() && stackA.method_7947() < stackA.method_7914() && stackA.method_7946()) {
                  for (int j = ix + 1; j < main.size(); j++) {
                     class_1799 stackB = main.get(j);
                     if (!stackB.method_7960() && stackB.method_7947() < stackB.method_7914() && class_1799.method_31577(stackA, stackB)) {
                        return true;
                     }
                  }
               }
            }
         }

         for (int ixx = 0; ixx < mc.field_1724.method_31548().field_7547.size(); ixx++) {
            class_1799 stack = (class_1799)mc.field_1724.method_31548().field_7547.get(ixx);
            if (!stack.method_7960() && stack.method_7909() instanceof class_1738) {
               float currentItemScore = InventoryUtils.getProtection(stack);
               if (InventoryUtils.getBestArmorScore(ItemUtils.getEquippableSlotType(stack)) == currentItemScore
                  && InventoryUtils.getCurrentArmorScore(ItemUtils.getEquippableSlotType(stack)) < currentItemScore) {
                  return true;
               }
            }
         }

         if (this.offhandItems.isMode("Golden Apple")) {
            class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
            int slot = InventoryUtils.getItemSlot(class_1802.field_8463);
            if (slot != -1) {
               if (offHand.method_7909() != class_1802.field_8463) {
                  return true;
               }

               class_1799 goldenAppleStack = (class_1799)mc.field_1724.method_31548().field_7547.get(slot);
               if (offHand.method_7947() + goldenAppleStack.method_7947() <= 64) {
                  return true;
               }
            }
         } else if (this.offhandItems.isMode("Projectile")) {
            class_1799 bestProjectile = InventoryUtils.getBestProjectile();
            if (bestProjectile != null) {
               int slot = InventoryUtils.getItemStackSlot(bestProjectile);
               class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
               boolean shouldSwap = offHand.method_7909() != class_1802.field_8803 && offHand.method_7909() != class_1802.field_8543
                  || offHand.method_7947() < bestProjectile.method_7947();
               if (shouldSwap && slot != -1) {
                  return true;
               }
            }
         } else if (this.offhandItems.isMode("Fishing Rod")) {
            int slotx = InventoryUtils.getItemSlot(class_1802.field_8378);
            if (slotx != -1 && ((class_1799)mc.field_1724.method_31548().field_7544.get(0)).method_7909() != class_1802.field_8378) {
               return true;
            }
         } else if (this.offhandItems.isMode("Block")) {
            class_1799 bestBlock = InventoryUtils.getBestBlock();
            if (bestBlock != null) {
               int slotx = InventoryUtils.getItemStackSlot(bestBlock);
               class_1799 offHand = (class_1799)mc.field_1724.method_31548().field_7544.get(0);
               boolean shouldSwapx = InventoryUtils.isInvalidBlock(offHand) || offHand.method_7947() < bestBlock.method_7947();
               if (shouldSwapx && slotx != -1) {
                  return true;
               }
            }
         }

         if (this.switchGoldenApple.getValue() && !this.offhandItems.isMode("Golden Apple")) {
            int targetSlot = (int)(this.goldenAppleSlot.getValue().floatValue() - 1.0F);
            class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(class_1802.field_8463);
               if (bestItemSlot != -1) {
                  class_1799 bestItemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(bestItemSlot);
                  if (currentSlot.method_7909() != class_1802.field_8463
                     || currentSlot.method_7909() == class_1802.field_8463 && currentSlot.method_7947() < bestItemStack.method_7947()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchBlock.getValue()) {
            int blockSlot = (int)(this.blockSlot.getValue().floatValue() - 1.0F);
            class_1799 currentBlock = (class_1799)mc.field_1724.method_31548().field_7547.get(blockSlot);
            class_1799 bestBlock = InventoryUtils.getBestBlock();
            if (bestBlock != null
               && (bestBlock.method_7947() > currentBlock.method_7947() || InventoryUtils.isInvalidBlock(currentBlock))
               && !this.offhandItems.isMode("Block")) {
               return true;
            }

            if (InventoryUtils.getBlockCountInInventory() > maxBlockSize.getValue().intValue()) {
               return true;
            }
         }

         if (this.switchSword.getValue()) {
            int slotxx = (int)(this.swordSlot.getValue().floatValue() - 1.0F);
            class_1799 currentSword = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxx);
            class_1799 bestSword = InventoryUtils.getBestSword();
            class_1799 bestShapeAxe = InventoryUtils.getBestShapeAxe();
            if (InventoryUtils.getAxeDamage(bestShapeAxe) > InventoryUtils.getSwordDamage(bestSword)) {
               bestSword = bestShapeAxe;
            }

            if (bestSword != null) {
               float currentDamage = currentSword.method_7909() instanceof class_1829
                  ? InventoryUtils.getSwordDamage(currentSword)
                  : InventoryUtils.getAxeDamage(currentSword);
               float bestWeaponDamage = bestSword.method_7909() instanceof class_1829
                  ? InventoryUtils.getSwordDamage(bestSword)
                  : InventoryUtils.getAxeDamage(bestSword);
               if (bestWeaponDamage > currentDamage) {
                  return true;
               }
            }
         }

         if (this.switchPickaxe.getValue()) {
            int slotxxx = (int)(this.pickaxeSlot.getValue().floatValue() - 1.0F);
            class_1799 bestPickaxe = InventoryUtils.getBestPickaxe();
            class_1799 currentPickaxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
            if (bestPickaxe != null
               && bestPickaxe.method_7909() instanceof class_1810
               && (
                  InventoryUtils.getToolScore(bestPickaxe) > InventoryUtils.getToolScore(currentPickaxe)
                     || !(currentPickaxe.method_7909() instanceof class_1810)
               )) {
               return true;
            }
         }

         if (this.switchAxe.getValue()) {
            int slotxxx = (int)(this.axeSlot.getValue().floatValue() - 1.0F);
            class_1799 bestAxe = InventoryUtils.getBestAxe();
            class_1799 currentAxe = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
            if (bestAxe != null
               && bestAxe.method_7909() instanceof class_1743
               && (InventoryUtils.getToolScore(bestAxe) > InventoryUtils.getToolScore(currentAxe) || !(currentAxe.method_7909() instanceof class_1743))) {
               return true;
            }
         }

         if (this.switchRod.getValue() && !this.offhandItems.isMode("Fishing Rod")) {
            int slotxxx = (int)(this.rodSlot.getValue().floatValue() - 1.0F);
            class_1799 currentRod = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
            if (!(currentRod.method_7909() instanceof class_1787)) {
               return true;
            }
         }

         if (this.switchBow.getValue()) {
            int slotxxx = (int)(this.bowSlot.getValue().floatValue() - 1.0F);
            class_1799 currentBow = (class_1799)mc.field_1724.method_31548().field_7547.get(slotxxx);
            class_1799 bestBow;
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

            if (InventoryUtils.getItemCount(class_1802.field_8107) > maxArrowSize.getValue().intValue()) {
               return true;
            }
         }

         if (this.switchEnderPearl.getValue()) {
            int targetSlot = (int)(this.enderPearlSlot.getValue().floatValue() - 1.0F);
            class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(class_1802.field_8634);
               if (bestItemSlot != -1) {
                  class_1799 bestItemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(bestItemSlot);
                  if (currentSlot.method_7909() != class_1802.field_8634
                     || currentSlot.method_7909() == class_1802.field_8634 && currentSlot.method_7947() < bestItemStack.method_7947()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchWaterBucket.getValue()) {
            int targetSlot = (int)(this.waterBucketSlot.getValue().floatValue() - 1.0F);
            class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(class_1802.field_8705);
               if (bestItemSlot != -1) {
                  class_1799 bestItemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(bestItemSlot);
                  if (currentSlot.method_7909() != class_1802.field_8705
                     || currentSlot.method_7909() == class_1802.field_8705 && currentSlot.method_7947() < bestItemStack.method_7947()) {
                     return true;
                  }
               }
            }
         }

         if (this.switchFireball.getValue()) {
            int targetSlot = (int)(this.fireballSlot.getValue().floatValue() - 1.0F);
            class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(targetSlot);
            if (InventoryUtils.isItemValid(currentSlot)) {
               int bestItemSlot = InventoryUtils.getItemSlot(class_1802.field_8814);
               if (bestItemSlot != -1) {
                  class_1799 bestItemStack = (class_1799)mc.field_1724.method_31548().field_7547.get(bestItemSlot);
                  if (currentSlot.method_7909() != class_1802.field_8814
                     || currentSlot.method_7909() == class_1802.field_8814 && currentSlot.method_7947() < bestItemStack.method_7947()) {
                     return true;
                  }
               }
            }
         }

         if (keepProjectile.getValue()) {
            if (InventoryUtils.getItemCount(class_1802.field_8803) + InventoryUtils.getItemCount(class_1802.field_8543)
               > maxProjectileSize.getValue().intValue()) {
               return true;
            }

            if (this.switchProjectile.getValue() && !this.offhandItems.isMode("Projectile")) {
               int projectileSlot = (int)(this.projectileSlot.getValue().floatValue() - 1.0F);
               class_1799 currentSlot = (class_1799)mc.field_1724.method_31548().field_7547.get(projectileSlot);
               if (InventoryUtils.getItemCount(class_1802.field_8803) > 0) {
                  if (currentSlot.method_7909() != class_1802.field_8803) {
                     return true;
                  }
               } else if (InventoryUtils.getItemCount(class_1802.field_8543) > 0 && currentSlot.method_7909() != class_1802.field_8543) {
                  return true;
               }
            }
         }

         if (this.throwItems.getValue()) {
            for (class_1799 stack : mc.field_1724.method_31548().field_7547) {
               if (!stack.method_7960() && !isItemUseful(stack)) {
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
            this.clickSlot(mc.field_1724.field_7498.field_7763, this.mergeDst + 36, 0, class_1713.field_7790, mc.field_1724);
         } else {
            this.clickSlot(mc.field_1724.field_7498.field_7763, this.mergeDst, 0, class_1713.field_7790, mc.field_1724);
         }

         this.mergeSrc = -1;
         this.mergeDst = -1;
      } else {
         List<class_1799> main = mc.field_1724.method_31548().field_7547;

         for (int i = 0; i < main.size(); i++) {
            class_1799 stackA = main.get(i);
            if (!stackA.method_7960() && stackA.method_7947() < stackA.method_7914() && stackA.method_7946()) {
               for (int j = i + 1; j < main.size(); j++) {
                  class_1799 stackB = main.get(j);
                  if (!stackB.method_7960() && stackB.method_7947() < stackB.method_7914() && class_1799.method_31577(stackA, stackB)) {
                     int smaller = stackA.method_7947() <= stackB.method_7947() ? i : j;
                     int larger = smaller == i ? j : i;
                     if (smaller < 9) {
                        this.clickSlot(mc.field_1724.field_7498.field_7763, smaller + 36, 0, class_1713.field_7790, mc.field_1724);
                     } else {
                        this.clickSlot(mc.field_1724.field_7498.field_7763, smaller, 0, class_1713.field_7790, mc.field_1724);
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
