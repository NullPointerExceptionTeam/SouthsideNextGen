package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.player.InvUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.FindItemResult;
import dev.southside.utils.types.Rotation;
import java.util.Comparator;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;

public class BowSpammer extends Module {
   private final Slider minRange = new Slider("Min Range", 10, 1, 100, 0.1F);
   private final Slider maxRange = new Slider("Max Range", 50, 1, 100, 0.1F);
   private final Switch predictMovement = new Switch("Predict Movement", true);
   private final Slider fov = new Slider("Fov", 360, 10, 360);
   private final Dropdown priorityMode = new Dropdown("Priority", "Distance", "Distance", "Health");
   private BowSpammer.SpamState state = BowSpammer.SpamState.IDLE;
   private BowSpammer.WeaponType currentWeapon = BowSpammer.WeaponType.NONE;
   private class_1309 target = null;
   private int drawTicks = 0;
   private int previousSlot = -1;
   public static boolean isWorking = false;
   private static final int AIM_TICKS_BEFORE_FIRE = 3;

   public BowSpammer() {
      super(Category.Combat, "BowSpammer", "Rapidly draws and fires bow/crossbow at targets");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addRangedValueRestrict(this.minRange, this.maxRange);
   }

   @Override
   public void onEnable() {
      this.state = BowSpammer.SpamState.IDLE;
      this.currentWeapon = BowSpammer.WeaponType.NONE;
      this.target = null;
      this.drawTicks = 0;
      this.previousSlot = -1;
      isWorking = false;
   }

   @Override
   public void onDisable() {
      if (mc.field_1724 != null) {
         if (this.state == BowSpammer.SpamState.DRAWING) {
            mc.field_1690.field_1904.method_23481(false);
         }

         if (this.previousSlot != -1) {
            mc.field_1724.method_31548().field_7545 = this.previousSlot;
            this.previousSlot = -1;
         }
      }

      this.state = BowSpammer.SpamState.IDLE;
      this.currentWeapon = BowSpammer.WeaponType.NONE;
      this.target = null;
      this.drawTicks = 0;
      isWorking = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (!ClientUtils.nullCheck()) {
            if (!Southside.INSTANCE.getModuleManager().isEnabled(NoSlowTest.class)) {
               if (this.state != BowSpammer.SpamState.IDLE) {
                  this.resetState();
               }

               this.setSuffix("NoSlow OFF");
            } else if (!this.hasArrows()) {
               if (this.state != BowSpammer.SpamState.IDLE) {
                  this.resetState();
               }

               this.setSuffix("No Arrows");
            } else {
               switch (this.state) {
                  case IDLE:
                     this.handleIdle();
                     break;
                  case SWAPPING:
                     this.handleSwapping();
                     break;
                  case DRAWING:
                     this.handleDrawing();
                     break;
                  case AIMING:
                     this.handleAiming();
                     break;
                  case FIRING:
                     this.handleFiring();
               }

               isWorking = this.state != BowSpammer.SpamState.IDLE;
            }
         }
      }
   }

   private void handleIdle() {
      this.target = this.findTarget();
      if (this.target == null) {
         this.setSuffix("No Target");
      } else {
         int weaponSlot = this.findBestWeaponSlot();
         if (weaponSlot == -1) {
            this.setSuffix("No Weapon");
         } else {
            class_1799 weaponStack = mc.field_1724.method_31548().method_5438(weaponSlot);
            if (weaponStack.method_7909() instanceof class_1753) {
               this.currentWeapon = BowSpammer.WeaponType.BOW;
            } else {
               if (!(weaponStack.method_7909() instanceof class_1764)) {
                  this.setSuffix("No Weapon");
                  return;
               }

               this.currentWeapon = BowSpammer.WeaponType.CROSSBOW;
               if (class_1764.method_7781(weaponStack)) {
                  if (mc.field_1724.method_31548().field_7545 != weaponSlot) {
                     this.previousSlot = mc.field_1724.method_31548().field_7545;
                     mc.field_1724.method_31548().field_7545 = weaponSlot;
                  }

                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
                  return;
               }
            }

            if (mc.field_1724.method_31548().field_7545 == weaponSlot) {
               this.state = BowSpammer.SpamState.DRAWING;
               this.drawTicks = 0;
               mc.field_1690.field_1904.method_23481(true);
               this.setSuffix("Drawing");
            } else {
               this.previousSlot = mc.field_1724.method_31548().field_7545;
               mc.field_1724.method_31548().field_7545 = weaponSlot;
               this.state = BowSpammer.SpamState.SWAPPING;
               this.setSuffix("Swapping");
            }
         }
      }
   }

   private void handleSwapping() {
      class_1799 heldItem = mc.field_1724.method_6047();
      if (!this.isWeapon(heldItem)) {
         this.resetState();
      } else {
         this.target = this.findTarget();
         if (this.target == null) {
            this.resetState();
         } else {
            this.state = BowSpammer.SpamState.DRAWING;
            this.drawTicks = 0;
            mc.field_1690.field_1904.method_23481(true);
            this.setSuffix("Drawing");
         }
      }
   }

   private void handleDrawing() {
      class_1799 heldItem = mc.field_1724.method_6047();
      if (!this.isWeapon(heldItem)) {
         this.resetState();
      } else if (mc.field_1724.method_6115() || this.drawTicks <= 1) {
         this.target = this.findTarget();
         if (this.target == null) {
            mc.field_1690.field_1904.method_23481(false);
            this.resetState();
         } else {
            this.drawTicks++;
            if (ServerPacketManager.deSyncing && ServerPacketManager.deSyncTick >= 30) {
               if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
               } else {
                  mc.field_1690.field_1904.method_23481(false);
                  this.resetState();
               }
            } else {
               if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
                  if (mc.field_1724.method_6048() >= 20) {
                     this.state = BowSpammer.SpamState.AIMING;
                     this.drawTicks = 0;
                     this.setSuffix("Aiming");
                  } else {
                     this.setSuffix("Bow " + mc.field_1724.method_6048() + "/20");
                  }
               } else if (class_1764.method_7781(heldItem)) {
                  mc.field_1690.field_1904.method_23481(false);
                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
               } else {
                  this.setSuffix("Loading...");
               }
            }
         }
      } else if (this.currentWeapon == BowSpammer.WeaponType.CROSSBOW && class_1764.method_7781(heldItem)) {
         mc.field_1690.field_1904.method_23481(false);
         this.state = BowSpammer.SpamState.AIMING;
         this.drawTicks = 0;
         this.setSuffix("Aiming");
      } else {
         this.resetState();
      }
   }

   private void handleAiming() {
      class_1799 heldItem = mc.field_1724.method_6047();
      if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
         if (!(heldItem.method_7909() instanceof class_1753) || !mc.field_1724.method_6115()) {
            this.resetState();
            return;
         }
      } else if (!(heldItem.method_7909() instanceof class_1764) || !class_1764.method_7781(heldItem)) {
         this.resetState();
         return;
      }

      this.target = this.findTarget();
      if (this.target == null) {
         if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
            mc.field_1690.field_1904.method_23481(false);
         }

         this.resetState();
      } else {
         this.drawTicks++;
         if (this.drawTicks >= 3) {
            this.state = BowSpammer.SpamState.FIRING;
            this.setSuffix("Firing");
         } else {
            this.setSuffix("Aim " + this.drawTicks + "/3");
         }
      }
   }

   private void handleFiring() {
      if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
         mc.field_1761.method_2897(mc.field_1724);
         mc.field_1690.field_1904.method_23481(false);
         if (this.previousSlot != -1) {
            mc.field_1724.method_31548().field_7545 = this.previousSlot;
            this.previousSlot = -1;
         }

         this.drawTicks = 0;
         this.state = BowSpammer.SpamState.IDLE;
         this.setSuffix("Fired");
      }
   }

   private void resetState() {
      mc.field_1690.field_1904.method_23481(false);
      if (this.previousSlot != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().field_7545 = this.previousSlot;
         this.previousSlot = -1;
      }

      this.state = BowSpammer.SpamState.IDLE;
      this.currentWeapon = BowSpammer.WeaponType.NONE;
      this.target = null;
      this.drawTicks = 0;
      isWorking = false;
   }

   @EventHandler(
      priority = -100
   )
   public void onRotation(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (this.state == BowSpammer.SpamState.AIMING || this.state == BowSpammer.SpamState.FIRING) {
            if (this.target != null && this.target.method_5805()) {
               Rotation aimRotation = this.getTrajectoryRotation(this.target);
               if (aimRotation != null) {
                  RotationUtils.setRotation(aimRotation, 2);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -200
   )
   public void onRotationApplied(RotationAppliedEvent event) {
      if (mc.field_1724 != null) {
         if (this.state == BowSpammer.SpamState.FIRING && this.currentWeapon == BowSpammer.WeaponType.CROSSBOW) {
            class_1799 heldItem = mc.field_1724.method_6047();
            if (heldItem.method_7909() instanceof class_1764 && class_1764.method_7781(heldItem)) {
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               if (this.previousSlot != -1) {
                  mc.field_1724.method_31548().field_7545 = this.previousSlot;
                  this.previousSlot = -1;
               }

               this.drawTicks = 0;
               this.state = BowSpammer.SpamState.IDLE;
               isWorking = false;
               this.setSuffix("Fired");
            }
         }
      }
   }

   private Rotation getTrajectoryRotation(class_1309 target) {
      class_243 pos = new class_243(target.method_53830(), target.method_53827(), target.method_53828());
      double distanceToEnt = mc.field_1724.method_5739(target);
      double predictX = pos.field_1352;
      double predictZ = pos.field_1350;
      if (this.predictMovement.getValue()) {
         class_243 velocity = new class_243(
            target.method_23317() - target.field_6038, target.method_23318() - target.field_5971, target.method_23321() - target.field_5989
         );
         predictX += velocity.field_1352 * (distanceToEnt * 0.8);
         predictZ += velocity.field_1350 * (distanceToEnt * 0.8);
      }

      double x = predictX - mc.field_1724.method_23317();
      double z = predictZ - mc.field_1724.method_23321();
      double h = pos.field_1351 + 1.0 - (mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376()));
      double h1 = Math.sqrt(x * x + z * z);
      float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)h1, (float)h, 1.0F);
      if (Float.isNaN(pitch)) {
         pitch = (float)(-Math.atan2(h, h1) * 180.0 / Math.PI);
      }

      return new Rotation(yaw, pitch);
   }

   private class_1309 findTarget() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         double min = this.minRange.getValue().doubleValue();
         double max = this.maxRange.getValue().doubleValue();
         return StreamSupport.<class_1297>stream(mc.field_1687.method_18112().spliterator(), false)
            .filter(e -> e instanceof class_1309)
            .map(e -> (class_1309)e)
            .filter(e -> TargetUtils.isTarget(e, true, true))
            .filter(e -> {
               double dist = mc.field_1724.method_5739(e);
               return dist >= min && dist <= max;
            })
            .filter(mc.field_1724::method_6057)
            .filter(e -> RotationUtils.inFoV(e, this.fov.getValue().floatValue()))
            .sorted(this.getTargetComparator())
            .findFirst()
            .orElse(null);
      } else {
         return null;
      }
   }

   private Comparator<class_1309> getTargetComparator() {
      return this.priorityMode.isMode("Health")
         ? Comparator.comparingDouble(class_1309::method_6032)
         : Comparator.comparingDouble(e -> e.method_5739(mc.field_1724));
   }

   private int findBestWeaponSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         int bestBowSlot = -1;
         double bestBowDamage = -1.0;
         int bestCrossbowSlot = -1;
         double bestCrossbowDamage = -1.0;
         int chargedCrossbowSlot = -1;
         double chargedCrossbowDamage = -1.0;

         for (int i = 0; i <= 8; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (!stack.method_7960()) {
               if (stack.method_7909() instanceof class_1753) {
                  double damage = InvUtils.getBowDamage(stack);
                  if (damage > bestBowDamage || bestBowSlot == -1) {
                     bestBowDamage = damage;
                     bestBowSlot = i;
                  }
               } else if (stack.method_7909() instanceof class_1764) {
                  double damage = InvUtils.getCrossbowDamage(stack);
                  if (class_1764.method_7781(stack)) {
                     if (damage > chargedCrossbowDamage || chargedCrossbowSlot == -1) {
                        chargedCrossbowDamage = damage;
                        chargedCrossbowSlot = i;
                     }
                  } else if (damage > bestCrossbowDamage || bestCrossbowSlot == -1) {
                     bestCrossbowDamage = damage;
                     bestCrossbowSlot = i;
                  }
               }
            }
         }

         if (chargedCrossbowSlot != -1) {
            return chargedCrossbowSlot;
         } else {
            return bestBowSlot != -1 ? bestBowSlot : bestCrossbowSlot;
         }
      }
   }

   private boolean isWeapon(class_1799 stack) {
      return stack != null && !stack.method_7960() ? stack.method_7909() instanceof class_1753 || stack.method_7909() instanceof class_1764 : false;
   }

   private boolean hasArrows() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         FindItemResult result = InvUtils.find(class_1802.field_8107, class_1802.field_8087, class_1802.field_8236);
         return result.found();
      }
   }

   private static enum SpamState {
      IDLE,
      SWAPPING,
      DRAWING,
      AIMING,
      FIRING;
   }

   private static enum WeaponType {
      NONE,
      BOW,
      CROSSBOW;
   }
}
