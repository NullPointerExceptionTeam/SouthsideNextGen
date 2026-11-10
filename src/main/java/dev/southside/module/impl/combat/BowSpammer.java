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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BowSpammer extends Module {
   private final Slider minRange = new Slider("Min Range", 10, 1, 100, 0.1F);
   private final Slider maxRange = new Slider("Max Range", 50, 1, 100, 0.1F);
   private final Switch predictMovement = new Switch("Predict Movement", true);
   private final Slider fov = new Slider("Fov", 360, 10, 360);
   private final Dropdown priorityMode = new Dropdown("Priority", "Distance", "Distance", "Health");
   private BowSpammer.SpamState state = BowSpammer.SpamState.IDLE;
   private BowSpammer.WeaponType currentWeapon = BowSpammer.WeaponType.NONE;
   private LivingEntity target = null;
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
      if (mc.player != null) {
         if (this.state == BowSpammer.SpamState.DRAWING) {
            mc.options.useKey.setPressed(false);
         }

         if (this.previousSlot != -1) {
            mc.player.getInventory().selectedSlot = this.previousSlot;
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
            ItemStack weaponStack = mc.player.getInventory().getStack(weaponSlot);
            if (weaponStack.getItem() instanceof BowItem) {
               this.currentWeapon = BowSpammer.WeaponType.BOW;
            } else {
               if (!(weaponStack.getItem() instanceof CrossbowItem)) {
                  this.setSuffix("No Weapon");
                  return;
               }

               this.currentWeapon = BowSpammer.WeaponType.CROSSBOW;
               if (CrossbowItem.isCharged(weaponStack)) {
                  if (mc.player.getInventory().selectedSlot != weaponSlot) {
                     this.previousSlot = mc.player.getInventory().selectedSlot;
                     mc.player.getInventory().selectedSlot = weaponSlot;
                  }

                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
                  return;
               }
            }

            if (mc.player.getInventory().selectedSlot == weaponSlot) {
               this.state = BowSpammer.SpamState.DRAWING;
               this.drawTicks = 0;
               mc.options.useKey.setPressed(true);
               this.setSuffix("Drawing");
            } else {
               this.previousSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = weaponSlot;
               this.state = BowSpammer.SpamState.SWAPPING;
               this.setSuffix("Swapping");
            }
         }
      }
   }

   private void handleSwapping() {
      ItemStack heldItem = mc.player.getMainHandStack();
      if (!this.isWeapon(heldItem)) {
         this.resetState();
      } else {
         this.target = this.findTarget();
         if (this.target == null) {
            this.resetState();
         } else {
            this.state = BowSpammer.SpamState.DRAWING;
            this.drawTicks = 0;
            mc.options.useKey.setPressed(true);
            this.setSuffix("Drawing");
         }
      }
   }

   private void handleDrawing() {
      ItemStack heldItem = mc.player.getMainHandStack();
      if (!this.isWeapon(heldItem)) {
         this.resetState();
      } else if (mc.player.isUsingItem() || this.drawTicks <= 1) {
         this.target = this.findTarget();
         if (this.target == null) {
            mc.options.useKey.setPressed(false);
            this.resetState();
         } else {
            this.drawTicks++;
            if (ServerPacketManager.deSyncing && ServerPacketManager.deSyncTick >= 30) {
               if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
               } else {
                  mc.options.useKey.setPressed(false);
                  this.resetState();
               }
            } else {
               if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
                  if (mc.player.getItemUseTime() >= 20) {
                     this.state = BowSpammer.SpamState.AIMING;
                     this.drawTicks = 0;
                     this.setSuffix("Aiming");
                  } else {
                     this.setSuffix("Bow " + mc.player.getItemUseTime() + "/20");
                  }
               } else if (CrossbowItem.isCharged(heldItem)) {
                  mc.options.useKey.setPressed(false);
                  this.state = BowSpammer.SpamState.AIMING;
                  this.drawTicks = 0;
                  this.setSuffix("Aiming");
               } else {
                  this.setSuffix("Loading...");
               }
            }
         }
      } else if (this.currentWeapon == BowSpammer.WeaponType.CROSSBOW && CrossbowItem.isCharged(heldItem)) {
         mc.options.useKey.setPressed(false);
         this.state = BowSpammer.SpamState.AIMING;
         this.drawTicks = 0;
         this.setSuffix("Aiming");
      } else {
         this.resetState();
      }
   }

   private void handleAiming() {
      ItemStack heldItem = mc.player.getMainHandStack();
      if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
         if (!(heldItem.getItem() instanceof BowItem) || !mc.player.isUsingItem()) {
            this.resetState();
            return;
         }
      } else if (!(heldItem.getItem() instanceof CrossbowItem) || !CrossbowItem.isCharged(heldItem)) {
         this.resetState();
         return;
      }

      this.target = this.findTarget();
      if (this.target == null) {
         if (this.currentWeapon == BowSpammer.WeaponType.BOW) {
            mc.options.useKey.setPressed(false);
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
         mc.interactionManager.stopUsingItem(mc.player);
         mc.options.useKey.setPressed(false);
         if (this.previousSlot != -1) {
            mc.player.getInventory().selectedSlot = this.previousSlot;
            this.previousSlot = -1;
         }

         this.drawTicks = 0;
         this.state = BowSpammer.SpamState.IDLE;
         this.setSuffix("Fired");
      }
   }

   private void resetState() {
      mc.options.useKey.setPressed(false);
      if (this.previousSlot != -1 && mc.player != null) {
         mc.player.getInventory().selectedSlot = this.previousSlot;
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
            if (this.target != null && this.target.isAlive()) {
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
      if (mc.player != null) {
         if (this.state == BowSpammer.SpamState.FIRING && this.currentWeapon == BowSpammer.WeaponType.CROSSBOW) {
            ItemStack heldItem = mc.player.getMainHandStack();
            if (heldItem.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(heldItem)) {
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               if (this.previousSlot != -1) {
                  mc.player.getInventory().selectedSlot = this.previousSlot;
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

   private Rotation getTrajectoryRotation(LivingEntity target) {
      Vec3d pos = new Vec3d(target.getLerpTargetX(), target.getLerpTargetY(), target.getLerpTargetZ());
      double distanceToEnt = mc.player.distanceTo(target);
      double predictX = pos.x;
      double predictZ = pos.z;
      if (this.predictMovement.getValue()) {
         Vec3d velocity = new Vec3d(
            target.getX() - target.lastRenderX, target.getY() - target.lastRenderY, target.getZ() - target.lastRenderZ
         );
         predictX += velocity.x * (distanceToEnt * 0.8);
         predictZ += velocity.z * (distanceToEnt * 0.8);
      }

      double x = predictX - mc.player.getX();
      double z = predictZ - mc.player.getZ();
      double h = pos.y + 1.0 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
      double h1 = Math.sqrt(x * x + z * z);
      float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)h1, (float)h, 1.0F);
      if (Float.isNaN(pitch)) {
         pitch = (float)(-Math.atan2(h, h1) * 180.0 / Math.PI);
      }

      return new Rotation(yaw, pitch);
   }

   private LivingEntity findTarget() {
      if (mc.world != null && mc.player != null) {
         double min = this.minRange.getValue().doubleValue();
         double max = this.maxRange.getValue().doubleValue();
         return StreamSupport.<Entity>stream(mc.world.getEntities().spliterator(), false)
            .filter(e -> e instanceof LivingEntity)
            .map(e -> (LivingEntity)e)
            .filter(e -> TargetUtils.isTarget(e, true, true))
            .filter(e -> {
               double dist = mc.player.distanceTo(e);
               return dist >= min && dist <= max;
            })
            .filter(mc.player::canSee)
            .filter(e -> RotationUtils.inFoV(e, this.fov.getValue().floatValue()))
            .sorted(this.getTargetComparator())
            .findFirst()
            .orElse(null);
      } else {
         return null;
      }
   }

   private Comparator<LivingEntity> getTargetComparator() {
      return this.priorityMode.isMode("Health")
         ? Comparator.comparingDouble(LivingEntity::getHealth)
         : Comparator.comparingDouble(e -> e.distanceTo(mc.player));
   }

   private int findBestWeaponSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         int bestBowSlot = -1;
         double bestBowDamage = -1.0;
         int bestCrossbowSlot = -1;
         double bestCrossbowDamage = -1.0;
         int chargedCrossbowSlot = -1;
         double chargedCrossbowDamage = -1.0;

         for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
               if (stack.getItem() instanceof BowItem) {
                  double damage = InvUtils.getBowDamage(stack);
                  if (damage > bestBowDamage || bestBowSlot == -1) {
                     bestBowDamage = damage;
                     bestBowSlot = i;
                  }
               } else if (stack.getItem() instanceof CrossbowItem) {
                  double damage = InvUtils.getCrossbowDamage(stack);
                  if (CrossbowItem.isCharged(stack)) {
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

   private boolean isWeapon(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem : false;
   }

   private boolean hasArrows() {
      if (mc.player == null) {
         return false;
      } else {
         FindItemResult result = InvUtils.find(Items.ARROW, Items.TIPPED_ARROW, Items.SPECTRAL_ARROW);
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
