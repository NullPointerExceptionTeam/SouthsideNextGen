package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.manager.CPSManager;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AutoGApple;
import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.BoundBoxUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.Objects;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class Velocity extends Module {
   public final Switch debug = new Switch("Debug", false);
   public final Switch debugHud = new Switch("Delay HUD", false);
   public final Switch fireballCooldown = new Switch("Fireball Cooldown", true);
   public final Switch modernCombat = new Switch("1.9+", false);
   public final Switch testFull = new Switch("Test Mode", true);
   public final Switch delayTillGround = new Switch("Delay Till Ground", false);
   public final Slider targetMotion = new Slider("Target Motion", 0.15, 0.05, 0.5, 0.05);
   public final Switch multiTargetFix = new Switch("Multi Target", true);
   public final Slider releaseDistance = new Slider("Release Distance", 1.6, 0.1, 3.0, 0.1);
   public static boolean gotKB;
   public boolean jump;
   int coolDownTicks;
   static int attack;
   Entity target;
   Vec3d motion = Vec3d.ZERO;
   EntityVelocityUpdateS2CPacket kbPacket = null;
      private int aimSkipTicks;
   private ProgressBarOverlay.ProgressHandle progressHandle;

   public Velocity() {
      super(Category.Combat, "Velocity", "Reduce KnockBack");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
   }

   @Override
   public void onEnable() {
      this.reset();
   }

   @Override
   public void onDisable() {
      this.reset();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.target = null;
      this.coolDownTicks = 0;
      gotKB = false;
      attack = 0;
      this.jump = false;
      this.kbPacket = null;
      this.aimSkipTicks = 0;
   }

   @Override
   public void reset() {
      if (gotKB) {
         this.target = null;
         if (!NoSlowTest.shouldEnable) {
            ServerPacketManager.reset(false);
         }

         gotKB = false;
         attack = 0;
         this.jump = false;
         this.kbPacket = null;
         this.aimSkipTicks = 0;
         this.cancelProgressBar();
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (!event.getOrigin().equals(TransferOrigin.SEND)) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof EntityVelocityUpdateS2CPacket wrapper
               && wrapper.getEntityId() == mc.player.getId()
               && this.coolDownTicks <= 0
               && this.shouldReduce(wrapper)
               && !NoSlowTest.shouldEnable) {
               gotKB = true;
               this.kbPacket = wrapper;
               this.motion = new Vec3d(wrapper.getVelocityX(), wrapper.getVelocityY(), wrapper.getVelocityZ());
               if (!ServerPacketManager.deSyncing) {
                  ServerPacketManager.setup();
               }
            }

            if (packet instanceof PlayerPositionLookS2CPacket && gotKB) {
               if (this.debug.getValue()) {
                  ChatUtils.warning("Flag Detected");
               }

               this.coolDownTicks = 2;
            }
         }
      }
   }

   private boolean shouldReduce(EntityVelocityUpdateS2CPacket packet) {
      return Southside.INSTANCE.getModuleManager().isEnabled(KillAura.class)
         && !AutoGApple.eating
         && new Vec3d(packet.getVelocityX(), 0.0, packet.getVelocityZ()).length() >= this.targetMotion.getDouble();
   }

   @EventHandler(
      priority = -200
   )
   public void onRotation(RotationEvent event) {
      if (mc.player == null) {
         this.reset();
      } else if (this.coolDownTicks > 0) {
         this.coolDownTicks--;
         this.reset();
      } else {
         if (gotKB) {
            if (ServerPacketManager.deSyncing) {
               if (this.debugHud.getValue()) {
                  ProgressBarOverlay overlay = Southside.get().getProgressBarOverlay();
                  if (overlay != null) {
                     if (this.progressHandle == null || !this.progressHandle.isActive()) {
                        this.progressHandle = overlay.show(40, "KB Delay");
                     }

                     this.progressHandle.update(ServerPacketManager.deSyncTick);
                  }
               }

               if (ServerPacketManager.deSyncTick >= 40) {
                  if (this.debug.getValue()) {
                     ChatUtils.error("Time Out!");
                  }

                  this.reset();
               }

               if (KillAura.target == null) {
                  this.reset();
                  if (this.debug.getValue()) {
                     ChatUtils.error("Reset, reason: aura no target.");
                  }

                  return;
               }

               if ((this.ready() || this.multiTargetFix.getValue() && KillAura.targets.size() > 1) && this.checkAsyncTargets()) {
                  int tick = ServerPacketManager.deSyncTick;
                  if (this.testFull.getValue()) {
                     ServerPacketManager.releaseUntil(Objects::isNull, false);
                  } else {
                     ServerPacketManager.reset(false);
                  }

                  gotKB = false;
                  this.kbPacket = null;
                  this.cancelProgressBar();
                  double strength = new Vec3d(mc.player.getVelocity().getX(), 0.0, mc.player.getVelocity().getZ()).length();
                  attack = this.calcAttacks(strength) + 1;
                  if (this.debug.getValue()) {
                     ChatUtils.info("Sync, ticks used: " + tick + " calculated attacks: " + attack + ".");
                  }
               }
            } else {
               this.reset();
               if (this.debug.getValue()) {
                  ChatUtils.error("Reset, reason: not desyncing.");
               }
            }
         }

         if (gotKB && attack > 0) {
            mc.options.sprintKey.setPressed(true);
         }
      }
   }

   @EventHandler
   public void onInput(RotationAppliedEvent event) {
      if (this.coolDownTicks <= 0) {
         if (gotKB
            && ServerPacketManager.deSyncing
            && this.ready()
            && KillAura.target != null
            && Southside.INSTANCE.getModuleManager().get(KillAura.class).inRaycast(KillAura.target)
            && BoundBoxUtils.getMinDistanceVec3dByBox(mc.player.getEyePos(), KillAura.target.getBoundingBox()).distanceTo(mc.player.getEyePos())
               <= this.releaseDistance.getValue().doubleValue()) {
            int tick = ServerPacketManager.deSyncTick;
            if (this.testFull.getValue()) {
               ServerPacketManager.releaseUntil(packet -> packet.equals(this.kbPacket), false);
            } else {
               ServerPacketManager.reset(false);
            }

            gotKB = false;
            this.kbPacket = null;
            this.cancelProgressBar();
            double strength = new Vec3d(mc.player.getVelocity().getX(), 0.0, mc.player.getVelocity().getZ()).length();
            attack = this.calcAttacks(strength) + 1;
            if (this.debug.getValue()) {
               ChatUtils.info("Sync, ticks used: " + tick + " calculated attacks: " + attack + ".");
            }
         }

         if (attack > 0) {
            Entity aimTarget = this.target != null ? this.target : this.findTarget();
            this.target = aimTarget;

            if (aimTarget == null || !this.serverAimValid(aimTarget)) {
               if (++this.aimSkipTicks > 5) {
                  this.aimSkipTicks = 0;
                  attack--;
               }

               return;
            }

            this.aimSkipTicks = 0;
            attack--;
            if (this.modernCombat.getValue()) {
               this.runReduceModern();
            } else {
               this.runReduce(1);
               this.jump = true;
            }
         }
      }
   }

   private Entity findTarget() {
      return KillAura.target != null && Southside.INSTANCE.getModuleManager().get(KillAura.class).inRaycast(KillAura.target) ? KillAura.target : null;
   }

      private boolean serverAimValid(Entity entity) {
      if (entity == null) {
         return false;
      }

      if (ClientRayTraceUtil.eyePos == null) {
         return true;
      }

      Rotation rotation;
      if (RotationUtils.getRotation() != null) {
         rotation = RotationUtils.getRotationSafely();
      } else {
         rotation = RotationUtils.getServerRotation();
      }

      if (rotation == null) {
         return true;
      }

      double range = Math.max(3.0, this.releaseDistance.getDouble() + 1.0);
      return ClientRayTraceUtil.didHitEntity(rotation.yaw, rotation.pitch, range, entity);
   }

   @EventHandler(
      priority = -200
   )
   public void onItemUse(ItemUseEvent event) {
      if (this.fireballCooldown.getValue()
         && !event.isCancelled()
         && mc.player != null
         && (mc.player.getOffHandStack().getItem().equals(Items.FIRE_CHARGE) || mc.player.getMainHandStack().getItem().equals(Items.FIRE_CHARGE))
         )
       {
         this.coolDownTicks = 10;
      }
   }

   private boolean checkAsyncTargets() {
      if (KillAura.target != null) {
         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player) {
               Box box = entity.dimensions.getBoxAt(player.getPos());
               if (TargetUtils.isTarget(entity, true, true)
                  && BoundBoxUtils.getMinDistanceVec3dByBox(mc.player.getEyePos(), box).distanceTo(mc.player.getEyePos())
                     <= this.releaseDistance.getValue().doubleValue()) {
                  Rotation rotTo = RotationUtils.calculate(BoundBoxUtils.getMinDistanceVec3dByBox(ClientRayTraceUtil.eyePos, box));
                  if (ClientRayTraceUtil.overBox(rotTo.yaw, rotTo.pitch, 3.0, box)) {
                     KillAura.targets.add(entity);
                     KillAura.target = entity;
                     RotationUtils.setRotation(rotTo, 2);
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public void runReduce(int times) {
      if (attack == 0 && this.testFull.getValue() && !gotKB) {
         if (this.debug.getValue()) {
            ChatUtils.info("Hit complete.");
         }

         ServerPacketManager.reset(false);
      }

      if (times <= 0) {
         this.target = null;
      } else {
         if (this.target == null) {
            this.target = this.findTarget();
         }

         if (this.target != null) {
            if (mc.player.isSprinting()) {
               PacketUtils.sendPacket(PlayerInteractEntityC2SPacket.attack(this.target, mc.player.isSneaking()));
               mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
               mc.player.swingHand(Hand.MAIN_HAND);
               CPSManager.INSTANCE.click();
               KillAura.msTimer.reset();
               KillAura.lastAttackSprint = mc.player.lastSprinting;
               if (this.debug.getValue()) {
                  ChatUtils.info("Reduce.");
               }
            } else {
               ChatUtils.error("not sprinting.");
            }

            this.target = null;
         } else {
            Entity auraTarget = KillAura.target;
            double dist = auraTarget == null || ClientRayTraceUtil.eyePos == null
               ? -1.0
               : BoundBoxUtils.getMinDistanceVec3dByBox(ClientRayTraceUtil.eyePos, auraTarget.getBoundingBox()).distanceTo(ClientRayTraceUtil.eyePos);
            ChatUtils.error("target is null. dist: " + dist);
         }
      }
   }

   private void runReduceModern() {
      if (this.target == null) {
         this.target = this.findTarget();
      }

      if (this.target == null) {
         return;
      }

      if (mc.player.getAttackCooldownProgress(0.0F) < 1.0F) {
         attack = 1;
      } else {
         PacketUtils.sendPacket(PlayerInteractEntityC2SPacket.attack(this.target, false));
         PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
         mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
         CPSManager.INSTANCE.click();
         KillAura.msTimer.reset();
         if (this.debug.getValue()) {
            ChatUtils.info("1.9+ Reduced!");
         }

         this.target = null;
      }
   }

   @EventHandler
   public void onMove(MovementInputEvent event) {
      if (mc.currentScreen == null && (gotKB && ServerPacketManager.deSyncing || attack > 0)) {
         event.setForward(true);
         event.setRight(false);
         event.setLeft(false);
         event.setBack(false);
         event.setSprint(true);
      }

      if (attack > 0) {
         this.jump = false;
         event.setJump(false);
      }
   }

   private void cancelProgressBar() {
      if (this.progressHandle != null) {
         this.progressHandle.cancel();
         this.progressHandle = null;
      }
   }

   private int calcAttacks(double strength) {
      if (strength < this.targetMotion.getValue().doubleValue()) {
         return -1;
      } else {
         for (int i = 0; i < 8; i++) {
            strength *= 0.6;
            if (strength < this.targetMotion.getValue().doubleValue()) {
               return i;
            }
         }

         return 8;
      }
   }

   private boolean ready() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.isUsingItem() ? false : this.checkSprint() && (mc.player.isOnGround() || !this.delayTillGround.getValue());
      }
   }

   private boolean checkSprint() {
      return mc.player.isSprinting();
   }
}
