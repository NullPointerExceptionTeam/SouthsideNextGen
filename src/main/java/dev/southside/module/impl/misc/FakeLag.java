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
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1420;
import net.minecraft.class_1427;
import net.minecraft.class_1429;
import net.minecraft.class_1477;
import net.minecraft.class_1510;
import net.minecraft.class_1511;
import net.minecraft.class_1571;
import net.minecraft.class_1588;
import net.minecraft.class_1621;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_1667;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_640;
import net.minecraft.class_745;

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
   private class_745 fakePlayer = null;
   private class_1297 target = null;
   private boolean isWorking = false;
   private boolean antiBowActive = false;
   private class_243 antiBowBlinkPos = null;
   private float antiBowHealthBefore = -1.0F;
   private class_1297 antiBowShooter = null;
   private final MSTimer antiBowTimer = new MSTimer();
   private class_243 renderPosFrom = null;
   private class_243 renderPosTo = null;
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
         } else if (KillAura.target != null && mc.field_1724.method_5739(KillAura.target) <= this.releaseRange.getValue().floatValue()) {
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
      if (this.antiBow.getValue() && mc.field_1724 != null && mc.field_1687 != null) {
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
               class_1667 closestDangerousArrow = null;
               double closestDist = Double.MAX_VALUE;

               for (class_1297 entity : mc.field_1687.method_18112()) {
                  if (entity instanceof class_1667 arrow && !arrow.method_65059() && arrow.method_24921() != mc.field_1724) {
                     double dist = mc.field_1724.method_5739(arrow);
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
                     class_1297 owner = closestDangerousArrow.method_24921();
                     if (owner != null) {
                        this.antiBowShooter = owner;
                     }

                     boolean shouldRelease = false;
                     class_243 arrowPos = closestDangerousArrow.method_19538();
                     class_243 arrowVel = closestDangerousArrow.method_18798();
                     class_243 toBlinkPos = this.antiBowBlinkPos.method_1020(arrowPos);
                     double dot = arrowVel.method_1026(toBlinkPos);
                     if (dot > 0.0) {
                        double speed = arrowVel.method_1033();
                        if (speed > 0.01) {
                           double distToTarget = toBlinkPos.method_1033();
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
                        float hpBefore = mc.field_1724.method_6032();
                        BlinkUtils.releasePacket();
                        this.antiBowBlinkPos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
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
         class_243 interpolatedPos;
         if (this.renderPosFrom != null && this.renderPosTo != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - this.renderPosTimestamp;
            float duration = this.antiBowActive ? 50.0F : this.pulseDelay.getValue().floatValue();
            float progress = Math.min(1.0F, (float)elapsed / duration);
            double x = this.renderPosFrom.field_1352 + (this.renderPosTo.field_1352 - this.renderPosFrom.field_1352) * progress;
            double y = this.renderPosFrom.field_1351 + (this.renderPosTo.field_1351 - this.renderPosFrom.field_1351) * progress;
            double z = this.renderPosFrom.field_1350 + (this.renderPosTo.field_1350 - this.renderPosFrom.field_1350) * progress;
            interpolatedPos = new class_243(x, y, z);
         } else {
            if (this.renderPosTo == null) {
               return;
            }

            interpolatedPos = this.renderPosTo;
         }

         float halfWidth = 0.3F;
         float height = 1.8F;
         class_238 box = new class_238(
               interpolatedPos.field_1352 - halfWidth,
               interpolatedPos.field_1351,
               interpolatedPos.field_1350 - halfWidth,
               interpolatedPos.field_1352 + halfWidth,
               interpolatedPos.field_1351 + height,
               interpolatedPos.field_1350 + halfWidth
            )
            .method_1014(0.2);
         Render3D.boxSides(box, this.boxColor.getValue().toJavaColor());
      }
   }

   public final boolean isSelected(class_1297 entity, boolean canAttackCheck) {
      if (entity != null && !(entity instanceof class_1511)) {
         if (entity instanceof class_1309 livingBase && entity != mc.field_1724 && livingBase.method_5805() && !livingBase.method_5767()) {
            if (entity instanceof class_1657) {
               return this.checkPlayerTarget((class_1657)entity, canAttackCheck);
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

   private boolean checkPlayerTarget(class_1657 player, boolean canAttackCheck) {
      if (!canAttackCheck) {
         return true;
      } else if (AntiBot.isBot(player)) {
         return false;
      } else if (!player.method_7325() && !player.method_6113()) {
         Teams teams = Southside.INSTANCE.getModuleManager().get(Teams.class);
         FriendManager friendManager = Southside.INSTANCE != null ? Southside.INSTANCE.getFriendManager() : null;
         if (teams != null && teams.isEnabled() && Teams.isTeam(player)) {
            return false;
         } else if (friendManager != null) {
            String playerName = player.method_7334() != null ? player.method_7334().getName() : player.method_5477().getString();
            return playerName == null || !friendManager.isFriend(playerName);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public final boolean isAnimal(class_1297 entity) {
      return entity instanceof class_1429
         || entity instanceof class_1477
         || entity instanceof class_1427
         || entity instanceof class_1646
         || entity instanceof class_1420;
   }

   public final boolean isMob(class_1297 entity) {
      return entity instanceof class_1588 || entity instanceof class_1621 || entity instanceof class_1571 || entity instanceof class_1510;
   }

   private void startWorking() {
      if (BlinkUtils.blink()) {
         this.isWorking = true;
         class_243 pos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
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
      if (this.pulseTimer.hasPassed(this.pulseDelay.getValue().intValue()) || mc.field_1690.field_1886.method_1434() || mc.field_1690.field_1904.method_1434()) {
         this.updateRenderPos(new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()));
         BlinkUtils.releasePacket();
         this.pulseTimer.reset();
      }
   }

   private void setPlayerRender() {
      this.fakePlayer = new class_745(mc.field_1687, mc.field_1724.method_7334());
      this.fakePlayer.method_5719(mc.field_1724);
      this.fakePlayer.field_6241 = mc.field_1724.field_6241;
   }

   private void updateRenderPos(class_243 newPos) {
      this.renderPosFrom = this.renderPosTo != null ? this.renderPosTo : newPos;
      this.renderPosTo = newPos;
      this.renderPosTimestamp = System.currentTimeMillis();
   }

   private boolean shouldPause() {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(KillAura.class) || Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class)) {
         return true;
      } else if (this.fakePlayer != null && this.target != null) {
         return this.target.method_5739(this.fakePlayer) < this.target.method_5739(mc.field_1724);
      } else {
         Blink blink = Southside.INSTANCE.getModuleManager().get(Blink.class);
         Scaffold scaffold = Southside.INSTANCE.getModuleManager().get(Scaffold.class);
         return blink != null && blink.isEnabled() || scaffold != null && scaffold.isEnabled();
      }
   }

   private boolean hasValidTargets() {
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (this.isSelected(entity, true) && mc.field_1724.method_5739(entity) <= this.reachRange.getValue().floatValue()) {
            this.target = entity;
            return true;
         }
      }

      return false;
   }

   private boolean shouldRender() {
      return this.renderPos.getValue()
         && mc.field_1724 != null
         && (this.fakePlayer != null || this.renderPosTo != null)
         && this.isEnabled()
         && !Southside.INSTANCE.getModuleManager().get(Blink.class).isEnabled();
   }

   private void startAntiBow() {
      if (!this.antiBowActive && !BlinkUtils.blinking) {
         if (BlinkUtils.blink()) {
            this.antiBowActive = true;
            this.antiBowTimer.reset();
            this.antiBowBlinkPos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
            this.antiBowHealthBefore = mc.field_1724.method_6032();
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
      if (mc.field_1724 != null) {
         String shooterName = "Unknown";
         if (this.antiBowShooter instanceof class_1657 player) {
            shooterName = player.method_7334() != null ? player.method_7334().getName() : player.method_5477().getString();
         } else if (this.antiBowShooter != null) {
            shooterName = this.antiBowShooter.method_5477().getString();
         }

         float hpNow = mc.field_1724.method_6032();
         float dmg = this.antiBowHealthBefore > 0.0F ? this.antiBowHealthBefore - hpNow : 0.0F;
         if (dmg < 0.0F) {
            dmg = 0.0F;
         }

         int ping = 0;
         class_640 entry = mc.field_1724.field_3944.method_2871(mc.field_1724.method_5667());
         if (entry != null) {
            ping = entry.method_2959();
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
