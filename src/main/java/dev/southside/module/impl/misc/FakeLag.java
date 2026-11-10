package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.FriendManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.render.Render3D;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.misc.MSTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.OtherClientPlayerEntity;

public class FakeLag extends Module {
   public Slider reachRange = new Slider("StartRange", 6.0, 1.0, 8.0, 0.01);
   public Slider releaseRange = new Slider("ReleaseRange", 6.0, 1.0, 8.0, 0.01);
   public Slider pulseDelay = new Slider("Delay", 300, 50, 1000, 10);
   public final Switch renderPos = new Switch("RenderPos", true);
   private final Color boxColor = new Color("Box Color", new OneColor(64, 176, 255, 80));
   public final Switch antiBow = new Switch("AntiBow", false);
   public final Slider antiBowRange = new Slider("AntiBow Range", 30.0, 5.0, 50.0, 1.0);
   public final Switch hitLog = new Switch("HitLog", false);
   public final Switch progressBar = new Switch("ProgressBar", true);
   private final MSTimer pulseTimer = new MSTimer();
   private OtherClientPlayerEntity fakePlayer = null;
   private Entity target = null;
   private boolean isWorking = false;
   private boolean antiBowActive = false;
   private Vec3d antiBowBlinkPos = null;
   private float antiBowHealthBefore = -1.0F;
   private Entity antiBowShooter = null;
   private final MSTimer antiBowTimer = new MSTimer();
   private Vec3d renderPosFrom = null;
   private Vec3d renderPosTo = null;
   private long renderPosTimestamp = 0L;
   private ProgressBarOverlay.ProgressHandle progressHandle;

   public FakeLag() {
      super(Category.Misc, "FakeLag");
   }

   @Override
   public void onEnable() {
      this.resetState();
      this.antiBowActive = false;
      this.antiBowBlinkPos = null;
      this.renderPosFrom = null;
      this.renderPosTo = null;
      this.cancelProgressBar();
   }

   @Override
   public void onDisable() {
      this.stopWorking();
      this.stopAntiBow();
      this.cancelProgressBar();
   }

   @EventHandler(
      priority = 100
   )
   public void onRotate(RotationEvent event) {
      if (!this.antiBowActive) {
         if (this.shouldPause()) {
            this.stopWorking();
         } else if (KillAura.target != null && mc.player.distanceTo(KillAura.target) <= this.releaseRange.getValue().floatValue()) {
            this.stopWorking();
         } else {
            if (this.hasValidTargets()) {
               if (!this.isWorking) {
                  this.startWorking();
               }

               this.pulse();
            } else {
               this.stopWorking();
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.updateProgressBar();
      if (this.antiBow.getValue() && mc.player != null && mc.world != null) {
         Blink blink = Southside.INSTANCE.getModuleManager().get(Blink.class);
         if (blink == null || !blink.isEnabled()) {
            if (Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
               if (this.antiBowActive) {
                  this.stopAntiBow();
               }
            } else if (KillAura.target != null) {
               if (this.antiBowActive) {
                  this.stopAntiBow();
               }
            } else {
               boolean hasEnemyArrow = false;
               ArrowEntity closestDangerousArrow = null;
               double closestDist = Double.MAX_VALUE;

               for (Entity entity : mc.world.getEntities()) {
                  if (entity instanceof ArrowEntity arrow && !arrow.isInGround() && arrow.getOwner() != mc.player) {
                     double dist = mc.player.distanceTo(arrow);
                     if (!(dist > this.antiBowRange.getValue().floatValue())) {
                        hasEnemyArrow = true;
                        if (dist < closestDist) {
                           closestDist = dist;
                           closestDangerousArrow = arrow;
                        }
                     }
                  }
               }

               if (hasEnemyArrow && !this.antiBowActive) {
                  if (this.isWorking) {
                     this.stopWorking();
                  }

                  this.startAntiBow();
               }

               if (this.antiBowActive) {
                  if (this.antiBowTimer.hasPassed(this.pulseDelay.getValue().longValue())) {
                     BlinkUtils.releasePacket();
                     if (this.hitLog.getValue()) {
                        this.printHitLog("§etimeout");
                     }

                     this.stopAntiBow();
                     return;
                  }

                  if (closestDangerousArrow != null) {
                     Entity owner = closestDangerousArrow.getOwner();
                     if (owner != null) {
                        this.antiBowShooter = owner;
                     }

                     boolean shouldRelease = false;
                     Vec3d arrowPos = closestDangerousArrow.getPos();
                     Vec3d arrowVel = closestDangerousArrow.getVelocity();
                     Vec3d toBlinkPos = this.antiBowBlinkPos.subtract(arrowPos);
                     double dot = arrowVel.dotProduct(toBlinkPos);
                     if (dot > 0.0) {
                        double speed = arrowVel.length();
                        if (speed > 0.01) {
                           double distToTarget = toBlinkPos.length();
                           double ticksToArrive = distToTarget / speed;
                           if (ticksToArrive <= 3.0) {
                              shouldRelease = true;
                           }
                        }

                        if (closestDist <= 3.0) {
                           shouldRelease = true;
                        }
                     }

                     if (shouldRelease) {
                        float hpBefore = mc.player.getHealth();
                        BlinkUtils.releasePacket();
                        this.antiBowBlinkPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                        this.updateRenderPos(this.antiBowBlinkPos);
                     }
                  }

                  if (!hasEnemyArrow) {
                     if (this.hitLog.getValue()) {
                        this.printHitLog(null);
                     }

                     this.stopAntiBow();
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (this.shouldRender()) {
         Vec3d interpolatedPos;
         if (this.renderPosFrom != null && this.renderPosTo != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - this.renderPosTimestamp;
            float duration = this.antiBowActive ? 50.0F : this.pulseDelay.getValue().floatValue();
            float progress = Math.min(1.0F, (float)elapsed / duration);
            double x = this.renderPosFrom.x + (this.renderPosTo.x - this.renderPosFrom.x) * progress;
            double y = this.renderPosFrom.y + (this.renderPosTo.y - this.renderPosFrom.y) * progress;
            double z = this.renderPosFrom.z + (this.renderPosTo.z - this.renderPosFrom.z) * progress;
            interpolatedPos = new Vec3d(x, y, z);
         } else {
            if (this.renderPosTo == null) {
               return;
            }

            interpolatedPos = this.renderPosTo;
         }

         float halfWidth = 0.3F;
         float height = 1.8F;
         Box box = new Box(
               interpolatedPos.x - halfWidth,
               interpolatedPos.y,
               interpolatedPos.z - halfWidth,
               interpolatedPos.x + halfWidth,
               interpolatedPos.y + height,
               interpolatedPos.z + halfWidth
            )
            .expand(0.2);
         Render3D.boxSides(box, this.boxColor.getValue().toJavaColor());
      }
   }

   public final boolean isSelected(Entity entity, boolean canAttackCheck) {
      if (entity != null && !(entity instanceof EndCrystalEntity)) {
         if (entity instanceof LivingEntity livingBase && entity != mc.player && livingBase.isAlive() && !livingBase.isInvisible()) {
            if (entity instanceof PlayerEntity) {
               return this.checkPlayerTarget((PlayerEntity)entity, canAttackCheck);
            }

            if (this.isMob(entity)) {
               return false;
            }

            if (this.isAnimal(entity)) {
               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean checkPlayerTarget(PlayerEntity player, boolean canAttackCheck) {
      if (!canAttackCheck) {
         return true;
      } else if (AntiBot.isBot(player)) {
         return false;
      } else if (!player.isSpectator() && !player.isSleeping()) {
         Teams teams = Southside.INSTANCE.getModuleManager().get(Teams.class);
         FriendManager friendManager = Southside.INSTANCE != null ? Southside.INSTANCE.getFriendManager() : null;
         if (teams != null && teams.isEnabled() && Teams.isTeam(player)) {
            return false;
         } else if (friendManager != null) {
            String playerName = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getName().getString();
            return playerName == null || !friendManager.isFriend(playerName);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public final boolean isAnimal(Entity entity) {
      return entity instanceof AnimalEntity
         || entity instanceof SquidEntity
         || entity instanceof GolemEntity
         || entity instanceof VillagerEntity
         || entity instanceof BatEntity;
   }

   public final boolean isMob(Entity entity) {
      return entity instanceof HostileEntity || entity instanceof SlimeEntity || entity instanceof GhastEntity || entity instanceof EnderDragonEntity;
   }

   private void startWorking() {
      if (BlinkUtils.blink()) {
         this.isWorking = true;
         Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
         this.renderPosFrom = pos;
         this.renderPosTo = pos;
         this.renderPosTimestamp = System.currentTimeMillis();
      }
   }

   private void stopWorking() {
      if (this.isWorking) {
         this.resetState();
         BlinkUtils.stopBlink();
      }
   }

   private void resetState() {
      this.fakePlayer = null;
      this.pulseTimer.reset();
      this.isWorking = false;
      this.target = null;
      this.renderPosFrom = null;
      this.renderPosTo = null;
      this.cancelProgressBar();
   }

   private void pulse() {
      if (this.pulseTimer.hasPassed(this.pulseDelay.getValue().intValue()) || mc.options.attackKey.isPressed() || mc.options.useKey.isPressed()) {
         this.updateRenderPos(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
         BlinkUtils.releasePacket();
         this.pulseTimer.reset();
      }
   }

   private void setPlayerRender() {
      this.fakePlayer = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
      this.fakePlayer.copyPositionAndRotation(mc.player);
      this.fakePlayer.headYaw = mc.player.headYaw;
   }

   private void updateRenderPos(Vec3d newPos) {
      this.renderPosFrom = this.renderPosTo != null ? this.renderPosTo : newPos;
      this.renderPosTo = newPos;
      this.renderPosTimestamp = System.currentTimeMillis();
   }

   private boolean shouldPause() {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(KillAura.class) || Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
         return true;
      } else if (this.fakePlayer != null && this.target != null) {
         return this.target.distanceTo(this.fakePlayer) < this.target.distanceTo(mc.player);
      } else {
         Blink blink = Southside.INSTANCE.getModuleManager().get(Blink.class);
         Scaffold scaffold = Southside.INSTANCE.getModuleManager().get(Scaffold.class);
         return blink != null && blink.isEnabled() || scaffold != null && scaffold.isEnabled();
      }
   }

   private boolean hasValidTargets() {
      for (Entity entity : mc.world.getEntities()) {
         if (this.isSelected(entity, true) && mc.player.distanceTo(entity) <= this.reachRange.getValue().floatValue()) {
            this.target = entity;
            return true;
         }
      }

      return false;
   }

   private boolean shouldRender() {
      return this.renderPos.getValue()
         && mc.player != null
         && (this.fakePlayer != null || this.renderPosTo != null)
         && this.isEnabled()
         && !Southside.INSTANCE.getModuleManager().get(Blink.class).isEnabled();
   }

   private void startAntiBow() {
      if (!this.antiBowActive && !BlinkUtils.blinking) {
         if (BlinkUtils.blink()) {
            this.antiBowActive = true;
            this.antiBowTimer.reset();
            this.antiBowBlinkPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            this.antiBowHealthBefore = mc.player.getHealth();
            this.renderPosFrom = this.antiBowBlinkPos;
            this.renderPosTo = this.antiBowBlinkPos;
            this.renderPosTimestamp = System.currentTimeMillis();
         }

         this.updateProgressBar();
      }
   }

   private void stopAntiBow() {
      if (this.antiBowActive) {
         this.antiBowActive = false;
         this.antiBowBlinkPos = null;
         this.antiBowHealthBefore = -1.0F;
         this.antiBowShooter = null;
         this.fakePlayer = null;
         this.renderPosFrom = null;
         this.renderPosTo = null;
         BlinkUtils.stopBlink();
         this.cancelProgressBar();
      }
   }

   private void updateProgressBar() {
      if (!this.progressBar.getValue()) {
         this.cancelProgressBar();
      } else if (!this.isWorking && !this.antiBowActive) {
         this.cancelProgressBar();
      } else {
         ProgressBarOverlay overlay = Southside.get().getProgressBarOverlay();
         if (overlay != null) {
            long delayMs = this.pulseDelay.getValue().longValue();
            if (delayMs <= 0L) {
               this.cancelProgressBar();
            } else {
               if (this.progressHandle == null || !this.progressHandle.isActive()) {
                  this.progressHandle = overlay.show((int)delayMs, "FakeLag");
               }

               long elapsed = this.antiBowActive ? this.antiBowTimer.getMS() : this.pulseTimer.getMS();
               int step = (int)Math.min(delayMs, Math.max(0L, elapsed));
               this.progressHandle.update(step);
            }
         }
      }
   }

   private void cancelProgressBar() {
      if (this.progressHandle != null) {
         this.progressHandle.cancel();
         this.progressHandle = null;
      }
   }

   private void printHitLog(String typeOverride) {
      if (mc.player != null) {
         String shooterName = "Unknown";
         if (this.antiBowShooter instanceof PlayerEntity player) {
            shooterName = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getName().getString();
         } else if (this.antiBowShooter != null) {
            shooterName = this.antiBowShooter.getName().getString();
         }

         float hpNow = mc.player.getHealth();
         float dmg = this.antiBowHealthBefore > 0.0F ? this.antiBowHealthBefore - hpNow : 0.0F;
         if (dmg < 0.0F) {
            dmg = 0.0F;
         }

         int ping = 0;
         PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
         if (entry != null) {
            ping = entry.getLatency();
         }

         String type;
         if (typeOverride != null) {
            type = typeOverride;
         } else {
            type = dmg <= 0.0F ? "§amiss" : "§cfailed";
         }

         ChatUtils.info(String.format("Target: §f%s §7| Dmg: §f%.1f §7| Ping: §f%dms §7| Type: %s", shooterName, dmg, ping, type));
      }
   }
}
