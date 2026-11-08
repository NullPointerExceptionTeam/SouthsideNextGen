package dev.southside.anticheat.player;

import dev.southside.Southside;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.utils.ChatUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Generated;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_243;

public class PlayerData {
   private class_1657 player;
   private float vl;
   private boolean firstDectected = true;
   public float lastTickYaw;
   private boolean lastOnGround = true;
   private class_243 lastPos;
   private class_243 lastVelocity;
   private int noSlowTicks;
   private int tellyTurnStreak;
   private int lastTellyTurnTick = -1000;
   private int lastTellyFlagTick = -1000;
   private float tellyLastSwingYaw;
   private boolean tellyLastSwinging;
   public boolean shouldEagle;
   public int eagleTicks;
   private int blinkFrozenTicks;
   private int blinkBurstCount;
   private int blinkLastFlagTick = -1000;
   private int velocityHitTick = -1000;
   private class_243 velocityPreHitPos;
   private boolean velocityWaitingCheck;
   private int velocityCheckTick;
   private int velocityFlagCount;
   private int velocityLastFlagTick = -1000;
   private int fastWebLastFlagTick = -1000;
   private int fastWebTicks;
   private final LinkedList<Float> scaffoldYawHistory = new LinkedList<>();
   private int scaffoldRotSignificantFails;
   private int scaffoldRotAngleSumFails;
   private int scaffoldRotVarianceFails;
   private int scaffoldRotSwitchFails;
   private int scaffoldJumpFails;
   private int scaffoldJumpLegitCount;
   private int scaffoldLastStairTick = -1000;
   private int scaffoldJumpLastFlagTick = -1000;
   private final List<Long> scaffoldPlaceTimes = new ArrayList<>();
   private final List<Boolean> scaffoldPlaceSneaking = new ArrayList<>();
   private class_2338 scaffoldLastPlacedBlock;
   private int scaffoldLastPlacedY = -1;
   private int scaffoldBatchLastFlagTick = -1000;

   public PlayerData(class_1657 player) {
      this.player = player;
      this.lastPos = player.method_19538();
   }

   public void addVl(float vl) {
      this.vl += vl;
   }

   public void addVl(float vl, String reason) {
      HackerDetector hackerDetector = Southside.get().getModuleManager().get(HackerDetector.class);
      this.vl += vl;
      if (hackerDetector.logValue.getValue()) {
         ChatUtils.infof("§7%s §cflagged §7%s §8(+%.1f, VL: %.1f)", this.player.method_5477().getString(), reason, vl, this.vl);
      }

      if (hackerDetector.NotificationValue.getValue()) {
         Notification.addNotificationKeepTime(this.player.method_5477().getString() + " → " + reason, "HackerDetector", Notification.NotificationType.INFO, 3.0);
      }
   }

   @Generated
   public class_1657 getPlayer() {
      return this.player;
   }

   @Generated
   public float getVl() {
      return this.vl;
   }

   @Generated
   public boolean isFirstDectected() {
      return this.firstDectected;
   }

   @Generated
   public float getLastTickYaw() {
      return this.lastTickYaw;
   }

   @Generated
   public boolean isLastOnGround() {
      return this.lastOnGround;
   }

   @Generated
   public class_243 getLastPos() {
      return this.lastPos;
   }

   @Generated
   public class_243 getLastVelocity() {
      return this.lastVelocity;
   }

   @Generated
   public int getNoSlowTicks() {
      return this.noSlowTicks;
   }

   @Generated
   public int getTellyTurnStreak() {
      return this.tellyTurnStreak;
   }

   @Generated
   public int getLastTellyTurnTick() {
      return this.lastTellyTurnTick;
   }

   @Generated
   public int getLastTellyFlagTick() {
      return this.lastTellyFlagTick;
   }

   @Generated
   public float getTellyLastSwingYaw() {
      return this.tellyLastSwingYaw;
   }

   @Generated
   public boolean isTellyLastSwinging() {
      return this.tellyLastSwinging;
   }

   @Generated
   public boolean isShouldEagle() {
      return this.shouldEagle;
   }

   @Generated
   public int getEagleTicks() {
      return this.eagleTicks;
   }

   @Generated
   public int getBlinkFrozenTicks() {
      return this.blinkFrozenTicks;
   }

   @Generated
   public int getBlinkBurstCount() {
      return this.blinkBurstCount;
   }

   @Generated
   public int getBlinkLastFlagTick() {
      return this.blinkLastFlagTick;
   }

   @Generated
   public int getVelocityHitTick() {
      return this.velocityHitTick;
   }

   @Generated
   public class_243 getVelocityPreHitPos() {
      return this.velocityPreHitPos;
   }

   @Generated
   public boolean isVelocityWaitingCheck() {
      return this.velocityWaitingCheck;
   }

   @Generated
   public int getVelocityCheckTick() {
      return this.velocityCheckTick;
   }

   @Generated
   public int getVelocityFlagCount() {
      return this.velocityFlagCount;
   }

   @Generated
   public int getVelocityLastFlagTick() {
      return this.velocityLastFlagTick;
   }

   @Generated
   public int getFastWebLastFlagTick() {
      return this.fastWebLastFlagTick;
   }

   @Generated
   public int getFastWebTicks() {
      return this.fastWebTicks;
   }

   @Generated
   public LinkedList<Float> getScaffoldYawHistory() {
      return this.scaffoldYawHistory;
   }

   @Generated
   public int getScaffoldRotSignificantFails() {
      return this.scaffoldRotSignificantFails;
   }

   @Generated
   public int getScaffoldRotAngleSumFails() {
      return this.scaffoldRotAngleSumFails;
   }

   @Generated
   public int getScaffoldRotVarianceFails() {
      return this.scaffoldRotVarianceFails;
   }

   @Generated
   public int getScaffoldRotSwitchFails() {
      return this.scaffoldRotSwitchFails;
   }

   @Generated
   public int getScaffoldJumpFails() {
      return this.scaffoldJumpFails;
   }

   @Generated
   public int getScaffoldJumpLegitCount() {
      return this.scaffoldJumpLegitCount;
   }

   @Generated
   public int getScaffoldLastStairTick() {
      return this.scaffoldLastStairTick;
   }

   @Generated
   public int getScaffoldJumpLastFlagTick() {
      return this.scaffoldJumpLastFlagTick;
   }

   @Generated
   public List<Long> getScaffoldPlaceTimes() {
      return this.scaffoldPlaceTimes;
   }

   @Generated
   public List<Boolean> getScaffoldPlaceSneaking() {
      return this.scaffoldPlaceSneaking;
   }

   @Generated
   public class_2338 getScaffoldLastPlacedBlock() {
      return this.scaffoldLastPlacedBlock;
   }

   @Generated
   public int getScaffoldLastPlacedY() {
      return this.scaffoldLastPlacedY;
   }

   @Generated
   public int getScaffoldBatchLastFlagTick() {
      return this.scaffoldBatchLastFlagTick;
   }

   @Generated
   public void setPlayer(class_1657 player) {
      this.player = player;
   }

   @Generated
   public void setVl(float vl) {
      this.vl = vl;
   }

   @Generated
   public void setFirstDectected(boolean firstDectected) {
      this.firstDectected = firstDectected;
   }

   @Generated
   public void setLastTickYaw(float lastTickYaw) {
      this.lastTickYaw = lastTickYaw;
   }

   @Generated
   public void setLastOnGround(boolean lastOnGround) {
      this.lastOnGround = lastOnGround;
   }

   @Generated
   public void setLastPos(class_243 lastPos) {
      this.lastPos = lastPos;
   }

   @Generated
   public void setLastVelocity(class_243 lastVelocity) {
      this.lastVelocity = lastVelocity;
   }

   @Generated
   public void setNoSlowTicks(int noSlowTicks) {
      this.noSlowTicks = noSlowTicks;
   }

   @Generated
   public void setTellyTurnStreak(int tellyTurnStreak) {
      this.tellyTurnStreak = tellyTurnStreak;
   }

   @Generated
   public void setLastTellyTurnTick(int lastTellyTurnTick) {
      this.lastTellyTurnTick = lastTellyTurnTick;
   }

   @Generated
   public void setLastTellyFlagTick(int lastTellyFlagTick) {
      this.lastTellyFlagTick = lastTellyFlagTick;
   }

   @Generated
   public void setTellyLastSwingYaw(float tellyLastSwingYaw) {
      this.tellyLastSwingYaw = tellyLastSwingYaw;
   }

   @Generated
   public void setTellyLastSwinging(boolean tellyLastSwinging) {
      this.tellyLastSwinging = tellyLastSwinging;
   }

   @Generated
   public void setShouldEagle(boolean shouldEagle) {
      this.shouldEagle = shouldEagle;
   }

   @Generated
   public void setEagleTicks(int eagleTicks) {
      this.eagleTicks = eagleTicks;
   }

   @Generated
   public void setBlinkFrozenTicks(int blinkFrozenTicks) {
      this.blinkFrozenTicks = blinkFrozenTicks;
   }

   @Generated
   public void setBlinkBurstCount(int blinkBurstCount) {
      this.blinkBurstCount = blinkBurstCount;
   }

   @Generated
   public void setBlinkLastFlagTick(int blinkLastFlagTick) {
      this.blinkLastFlagTick = blinkLastFlagTick;
   }

   @Generated
   public void setVelocityHitTick(int velocityHitTick) {
      this.velocityHitTick = velocityHitTick;
   }

   @Generated
   public void setVelocityPreHitPos(class_243 velocityPreHitPos) {
      this.velocityPreHitPos = velocityPreHitPos;
   }

   @Generated
   public void setVelocityWaitingCheck(boolean velocityWaitingCheck) {
      this.velocityWaitingCheck = velocityWaitingCheck;
   }

   @Generated
   public void setVelocityCheckTick(int velocityCheckTick) {
      this.velocityCheckTick = velocityCheckTick;
   }

   @Generated
   public void setVelocityFlagCount(int velocityFlagCount) {
      this.velocityFlagCount = velocityFlagCount;
   }

   @Generated
   public void setVelocityLastFlagTick(int velocityLastFlagTick) {
      this.velocityLastFlagTick = velocityLastFlagTick;
   }

   @Generated
   public void setFastWebLastFlagTick(int fastWebLastFlagTick) {
      this.fastWebLastFlagTick = fastWebLastFlagTick;
   }

   @Generated
   public void setFastWebTicks(int fastWebTicks) {
      this.fastWebTicks = fastWebTicks;
   }

   @Generated
   public void setScaffoldRotSignificantFails(int scaffoldRotSignificantFails) {
      this.scaffoldRotSignificantFails = scaffoldRotSignificantFails;
   }

   @Generated
   public void setScaffoldRotAngleSumFails(int scaffoldRotAngleSumFails) {
      this.scaffoldRotAngleSumFails = scaffoldRotAngleSumFails;
   }

   @Generated
   public void setScaffoldRotVarianceFails(int scaffoldRotVarianceFails) {
      this.scaffoldRotVarianceFails = scaffoldRotVarianceFails;
   }

   @Generated
   public void setScaffoldRotSwitchFails(int scaffoldRotSwitchFails) {
      this.scaffoldRotSwitchFails = scaffoldRotSwitchFails;
   }

   @Generated
   public void setScaffoldJumpFails(int scaffoldJumpFails) {
      this.scaffoldJumpFails = scaffoldJumpFails;
   }

   @Generated
   public void setScaffoldJumpLegitCount(int scaffoldJumpLegitCount) {
      this.scaffoldJumpLegitCount = scaffoldJumpLegitCount;
   }

   @Generated
   public void setScaffoldLastStairTick(int scaffoldLastStairTick) {
      this.scaffoldLastStairTick = scaffoldLastStairTick;
   }

   @Generated
   public void setScaffoldJumpLastFlagTick(int scaffoldJumpLastFlagTick) {
      this.scaffoldJumpLastFlagTick = scaffoldJumpLastFlagTick;
   }

   @Generated
   public void setScaffoldLastPlacedBlock(class_2338 scaffoldLastPlacedBlock) {
      this.scaffoldLastPlacedBlock = scaffoldLastPlacedBlock;
   }

   @Generated
   public void setScaffoldLastPlacedY(int scaffoldLastPlacedY) {
      this.scaffoldLastPlacedY = scaffoldLastPlacedY;
   }

   @Generated
   public void setScaffoldBatchLastFlagTick(int scaffoldBatchLastFlagTick) {
      this.scaffoldBatchLastFlagTick = scaffoldBatchLastFlagTick;
   }
}
