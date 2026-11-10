package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.player.BlockUtils;
import dev.southside.utils.player.FallingPlayer;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.timer.StopWatch;
import dev.southside.utils.types.Rotation;
import dev.southside.utils.world.ProjectileUtil;
import java.awt.Color;
import net.minecraft.util.Hand;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;

public class AntiVoid extends Module {
   public final Slider fallDistValue = new Slider("Fall Distance", 3, 1, 10, 1);
   public final Switch debugValue = new Switch("Debug", false);
   public final Slider attemptTime = new Slider("Max Attempt Times", 1, 2, 5);
   private AntiVoid.CalculateThread calculateThread;
   private int attempted;
   private boolean calculating;
   private boolean forcedStuck;
   private boolean waitingServerSync;
   private boolean scaffoldEnabledByAntiVoid;
   private boolean awaitScaffoldAfterSync;
   private boolean pearlThrownThisFall;

   public AntiVoid() {
      super(Category.Player, "AntiVoid", "Automatically throws ender pearls when falling into the void");
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
      if (this.calculating) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onRender(Render2DEvent event) {
      if (this.debugValue.getValue() && mc.player != null) {
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         String assessmentText = "assessment: "
            + new ProjectileUtil.EnderPearlPredictor(
                  mc.player.getX(),
                  mc.player.getY(),
                  mc.player.getZ(),
                  mc.player.getVelocity().y - 0.01,
                  mc.player.getVelocity().y + 0.02
               )
               .assessRotation(RotationUtils.getRotationSafely());
         String rotationText = "(" + mc.player.getYaw() + ", " + mc.player.getPitch() + ")";
         nanoVGHelper.setupAndDraw(true, vg -> {
            nanoVGHelper.drawText(vg, assessmentText, 20.0F, 20.0F, Color.WHITE.getRGB(), 12.0F, Fonts.REGULAR);
            nanoVGHelper.drawText(vg, rotationText, 20.0F, 35.0F, Color.WHITE.getRGB(), 12.0F, Fonts.REGULAR);
         });
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.player != null) {
         if (mc.player.isOnGround()) {
            this.attempted = 0;
            this.pearlThrownThisFall = false;
            this.waitingServerSync = false;
            this.awaitScaffoldAfterSync = false;
            this.stopActiveCalculation();
            this.disableScaffoldSupport();
            this.releaseStuckIfIdle();
         }

         if (event.getState() == Stage.POST) {
            if (this.calculateThread != null && this.calculateThread.completed) {
               Rotation solution = this.calculateThread.solution;
               BlockPos landingBlock = this.calculateThread.landingBlock;
               double score = this.calculateThread.solutionE;
               boolean requireScaffold = isLandingVoid(landingBlock);
               boolean hasSolution = solution != null && score >= 0.15;
               if (this.debugValue.getValue() && solution != null && score < 0.15) {
                  ChatUtils.warning(String.format("§a[AntiVoid] §c分数过低 %.3f，放弃投掷（避免浪费珍珠）", score));
               }

               if (hasSolution && !this.pearlThrownThisFall) {
                  this.waitingServerSync = true;
                  this.awaitScaffoldAfterSync = requireScaffold;
               }

               this.stopActiveCalculation();
               if (hasSolution && !this.pearlThrownThisFall) {
                  this.throwPearl(solution);
                  this.pearlThrownThisFall = true;
               } else if (this.debugValue.getValue() && solution == null) {
                  ChatUtils.warning("AntiVoid: 未能找到可用的珍珠角度");
               }
            }
         } else if (!this.calculating && !this.waitingServerSync && !this.pearlThrownThisFall) {
            boolean fallingIntoVoid = mc.player.getVelocity().y < 0.1
               && new FallingPlayer(mc.player).findCollision(60) == null
               && !BlockUtils.isSolidBlockUnder(mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()))
               && mc.player.fallDistance > this.fallDistValue.getValue().floatValue();
            if (fallingIntoVoid) {
               if (!(mc.player.getVelocity().y >= -1.0) && !mc.player.isOnGround()) {
                  if (this.attempted < this.attemptTime.getValue().intValue()) {
                     int pearlSlot = this.findPearlSlot();
                     if (pearlSlot == -1) {
                        if (this.debugValue.getValue()) {
                           ChatUtils.warning("AntiVoid: 热键栏中没有珍珠");
                        }
                     } else {
                        this.attempted++;
                        mc.player.getInventory().selectedSlot = pearlSlot;
                        this.startCalculation();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin() == TransferOrigin.RECEIVE) {
         if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.waitingServerSync = false;
            if (this.awaitScaffoldAfterSync && mc.player != null && !mc.player.isOnGround()) {
               this.enableScaffoldSupport();
            }

            this.awaitScaffoldAfterSync = false;
            this.releaseStuckIfIdle();
         }
      }
   }

   @Override
   public void onDisable() {
      this.waitingServerSync = false;
      this.pearlThrownThisFall = false;
      this.awaitScaffoldAfterSync = false;
      this.stopActiveCalculation();
      this.disableScaffoldSupport();
      if (this.forcedStuck) {
         Southside.INSTANCE.getModuleManager().get(Stuck.class).setEnable(false);
         this.forcedStuck = false;
      }

      super.onDisable();
   }

   private void startCalculation() {
      this.calculating = true;
      double velocityY = mc.player.getVelocity().y;
      this.calculateThread = new AntiVoid.CalculateThread(
         mc.player.getX(),
         mc.player.getY(),
         mc.player.getZ(),
         velocityY - 0.05,
         velocityY + 0.05,
         mc.player.getYaw(),
         mc.player.getPitch(),
         this.debugValue.getValue()
      );
      this.calculateThread.start();
      Stuck stuck = Southside.INSTANCE.getModuleManager().get(Stuck.class);
      if (!stuck.isEnabled()) {
         stuck.setEnable(true);
         this.forcedStuck = true;
      } else {
         this.forcedStuck = false;
      }
   }

   private void stopActiveCalculation() {
      if (this.calculateThread != null) {
         if (!this.calculateThread.completed) {
            this.calculateThread.requestStop();
         }

         this.calculateThread = null;
      }

      this.calculating = false;
      this.releaseStuckIfIdle();
   }

   private int findPearlSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).getItem() instanceof EnderPearlItem) {
            return i;
         }
      }

      return -1;
   }

   private void releaseStuckIfIdle() {
      if (!this.calculating && !this.waitingServerSync && this.forcedStuck) {
         Southside.INSTANCE.getModuleManager().get(Stuck.class).setEnable(false);
         this.forcedStuck = false;
      }
   }

   private void enableScaffoldSupport() {
      Scaffold scaffold = Southside.INSTANCE.getModuleManager().get(Scaffold.class);
      if (mc.player != null && mc.world != null) {
         if (scaffold.getBlockCountInventory() > 0) {
            if (!scaffold.isEnabled()) {
               scaffold.setEnable(true);
               this.scaffoldEnabledByAntiVoid = true;
            } else {
               this.scaffoldEnabledByAntiVoid = false;
            }
         }
      }
   }

   private void disableScaffoldSupport() {
      if (this.scaffoldEnabledByAntiVoid) {
         Southside.INSTANCE.getModuleManager().get(Scaffold.class).setEnable(false);
         this.scaffoldEnabledByAntiVoid = false;
      }
   }

   private static boolean isLandingVoid(BlockPos landingBlock) {
      if (landingBlock != null && mc.world != null) {
         BlockPos below = landingBlock.down();
         return !mc.world.getBlockState(below).isSolidBlock(mc.world, below);
      } else {
         return false;
      }
   }

   private void throwPearl(Rotation rotation) {
      if (rotation != null && mc.player != null && mc.interactionManager != null) {
         rotation.apply();
         rotation.fixedSensitivity(((Double)mc.options.getMouseSensitivity().getValue()).floatValue());
         mc.player.networkHandler.sendPacket(new LookAndOnGround(rotation.yaw, rotation.pitch, mc.player.isOnGround(), mc.player.horizontalCollision));
         mc.player.swingHand(Hand.MAIN_HAND);
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         if (this.debugValue.getValue()) {
            ChatUtils.info(String.format("§a[AntiVoid] §f投掷珍珠 yaw=%.1f pitch=%.1f", rotation.yaw, rotation.pitch));
         }
      }
   }

   private static class CalculateThread extends Thread {
      private static final long MAX_CALCULATION_MS = 30L;
      private final ProjectileUtil.EnderPearlPredictor predictor;
      private final float baseYaw;
      private final float basePitch;
      private final boolean debug;
      private volatile boolean cancelled;
      private double solutionE;
      private Rotation solution;
      private BlockPos landingBlock;
      private boolean completed;
      private int totalEvaluations;

      private CalculateThread(
         double predictX, double predictY, double predictZ, double minMotionY, double maxMotionY, float baseYaw, float basePitch, boolean debug
      ) {
         this.predictor = new ProjectileUtil.EnderPearlPredictor(predictX, predictY, predictZ, minMotionY, maxMotionY);
         this.baseYaw = MathHelper.wrapDegrees(baseYaw);
         this.basePitch = clampPitch(basePitch);
         this.debug = debug;
         this.completed = false;
         this.cancelled = false;
         this.totalEvaluations = 0;
         this.setName("AntiVoid-Calculator");
      }

      @Override
      public void run() {
         StopWatch timer = new StopWatch();
         Rotation bestRotation = null;
         double bestScore = -1.0;

         try {
            float[] candidateYaws = new float[9];

            for (int i = 0; i < 8; i++) {
               candidateYaws[i] = MathHelper.wrapDegrees(i * 45.0F);
            }

            candidateYaws[8] = this.baseYaw;
            float[] candidatePitches = new float[]{-40.0F, -20.0F, 0.0F, 20.0F, 45.0F};
            float[] topYaw = new float[3];
            float[] topPitch = new float[3];
            double[] topScore = new double[]{-1.0, -1.0, -1.0};
            double motionYMid = (this.predictor.minMotionY + this.predictor.maxMotionY) * 0.5;

            for (float yaw : candidateYaws) {
               label394:
               for (float pitch : candidatePitches) {
                  if (!this.cancelled && timer.getElapsedTime() <= 30L) {
                     Rotation candidate = new Rotation(yaw, clampPitch(pitch));
                     double score = this.predictor.assessSingleRotation(candidate, motionYMid, this.debug);
                     this.totalEvaluations++;
                     int k = 0;

                     while (true) {
                        if (k < 3) {
                           if (!(score > topScore[k])) {
                              k++;
                              continue;
                           } else {
                              for (int m = 2; m > k; m--) {
                                 topYaw[m] = topYaw[m - 1];
                                 topPitch[m] = topPitch[m - 1];
                                 topScore[m] = topScore[m - 1];
                              }

                              topYaw[k] = yaw;
                              topPitch[k] = pitch;
                              topScore[k] = score;
                           }
                        }
                        continue label394;
                     }
                  }
               }
            }

            if (this.debug) {
               StringBuilder sb = new StringBuilder("§a[AntiVoid] §ePhase 0§f 候选筛选 (");
               sb.append(this.totalEvaluations).append(" evals, ").append(timer.getElapsedTime()).append("ms)");

               for (int k = 0; k < 3 && topScore[k] > 0.0; k++) {
                  sb.append(String.format("\n  §7#%d§f yaw=%.0f pitch=%.0f score=%.3f", k + 1, topYaw[k], topPitch[k], topScore[k]));
               }

               ChatUtils.info(sb.toString());
            }

            if (topScore[0] > bestScore) {
               bestScore = topScore[0];
               bestRotation = new Rotation(topYaw[0], topPitch[0]);
            }

            int phase1Evals = 0;

            for (int k = 0; k < 3 && topScore[k] > 0.0 && !this.cancelled && timer.getElapsedTime() <= 30L; k++) {
               float centerYaw = topYaw[k];
               float centerPitch = topPitch[k];

               for (float yaw = centerYaw - 15.0F; yaw <= centerYaw + 15.0F; yaw += 3.0F) {
                  for (float pitchx = centerPitch - 10.0F; pitchx <= centerPitch + 10.0F && !this.cancelled && timer.getElapsedTime() <= 30L; pitchx += 3.0F) {
                     Rotation candidate = new Rotation(MathHelper.wrapDegrees(yaw), clampPitch(pitchx));

                     double score;
                     try {
                        score = this.predictor.assessRotation(candidate);
                     } catch (Exception var34) {
                        continue;
                     }

                     this.totalEvaluations++;
                     phase1Evals++;
                     if (score > bestScore) {
                        bestScore = score;
                        bestRotation = candidate.copy();
                     }

                     if (bestScore >= 0.85) {
                        break;
                     }
                  }

                  if (bestScore >= 0.85) {
                     break;
                  }
               }

               if (bestScore >= 0.85) {
                  break;
               }
            }

            if (this.debug) {
               String bestInfo = bestRotation != null
                  ? String.format("yaw=%.1f pitch=%.1f score=%.3f", bestRotation.yaw, bestRotation.pitch, bestScore)
                  : "none";
               ChatUtils.info(String.format("§a[AntiVoid] §ePhase 1§f 局部细化 (%d evals, %dms) best: %s", phase1Evals, timer.getElapsedTime(), bestInfo));
            }

            if (bestRotation != null && bestScore < 0.9 && !this.cancelled && timer.getElapsedTime() < 30L) {
               int phase2Evals = 0;
               float fineYawCenter = bestRotation.yaw;
               float finePitchCenter = bestRotation.pitch;

               for (float yaw = fineYawCenter - 4.0F; yaw <= fineYawCenter + 4.0F; yaw++) {
                  for (float pitchx = finePitchCenter - 4.0F; pitchx <= finePitchCenter + 4.0F && !this.cancelled && timer.getElapsedTime() <= 30L; pitchx++) {
                     Rotation candidate = new Rotation(MathHelper.wrapDegrees(yaw), clampPitch(pitchx));

                     double scorex;
                     try {
                        scorex = this.predictor.assessRotation(candidate);
                     } catch (Exception var33) {
                        continue;
                     }

                     this.totalEvaluations++;
                     phase2Evals++;
                     if (scorex > bestScore) {
                        bestScore = scorex;
                        bestRotation = candidate.copy();
                     }

                     if (bestScore >= 0.95) {
                        break;
                     }
                  }

                  if (bestScore >= 0.95) {
                     break;
                  }
               }

               if (this.debug) {
                  String bestInfo = String.format("yaw=%.1f pitch=%.1f score=%.3f", bestRotation.yaw, bestRotation.pitch, bestScore);
                  ChatUtils.info(String.format("§a[AntiVoid] §ePhase 2§f 精细微调 (%d evals, %dms) best: %s", phase2Evals, timer.getElapsedTime(), bestInfo));
               }
            }
         } finally {
            this.solution = bestRotation;
            this.landingBlock = this.resolveLandingBlock(bestRotation);
            this.solutionE = bestScore;
            this.completed = true;
            if (this.debug && !this.cancelled) {
               long elapsed = timer.getElapsedTime();
               if (bestRotation != null) {
                  String landingInfo = this.landingBlock != null
                     ? String.format("(%d, %d, %d)", this.landingBlock.getX(), this.landingBlock.getY(), this.landingBlock.getZ())
                     : "unknown";
                  boolean needScaffold = AntiVoid.isLandingVoid(this.landingBlock);
                  ChatUtils.info(
                     String.format(
                        "§a[AntiVoid] §b完成§f yaw=%.1f pitch=%.1f score=%.3f | %d evals %dms | 落点=%s scaffold=%b",
                        bestRotation.yaw,
                        bestRotation.pitch,
                        bestScore,
                        this.totalEvaluations,
                        elapsed,
                        landingInfo,
                        needScaffold
                     )
                  );
               } else {
                  ChatUtils.warning(String.format("§a[AntiVoid] §c失败§f 未找到可用角度 | %d evals %dms", this.totalEvaluations, elapsed));
               }
            }
         }
      }

      public void requestStop() {
         this.cancelled = true;
      }

      private static float clampPitch(float pitch) {
         return MathHelper.clamp(pitch, -89.9F, 89.9F);
      }

      private ProjectileUtil.ProjectileHit simulateLanding(Rotation rotation) {
         if (rotation != null && AntiVoid.mc.player != null) {
            Rotation adjusted = rotation.copy();
            adjusted.fixPitch();
            float motionFactor = 1.5F;
            float gravity = 0.03F;
            float size = 0.25F;
            float motionSlowdown = 0.99F;
            double posX = this.predictor.predictX - MathHelper.cos(adjusted.yaw / 180.0F * (float) Math.PI) * 0.16F;
            double posY = this.predictor.predictY + AntiVoid.mc.player.getEyeHeight(AntiVoid.mc.player.getPose()) - 0.1F;
            double posZ = this.predictor.predictZ - MathHelper.sin(adjusted.yaw / 180.0F * (float) Math.PI) * 0.16F;
            double motionX = -MathHelper.sin(adjusted.yaw / 180.0F * (float) Math.PI)
                  * MathHelper.cos(adjusted.pitch / 180.0F * (float) Math.PI)
               * 0.4;
            double motionY = -MathHelper.sin(adjusted.pitch / 180.0F * (float) Math.PI) * 0.4;
            double motionZ = MathHelper.cos(adjusted.yaw / 180.0F * (float) Math.PI)
                  * MathHelper.cos(adjusted.pitch / 180.0F * (float) Math.PI)
               * 0.4;
            float distance = MathHelper.sqrt((float)(motionX * motionX + motionY * motionY + motionZ * motionZ));
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
            motionX *= 1.5;
            motionY *= 1.5;
            motionZ *= 1.5;
            double motionYOffset = (this.predictor.minMotionY + this.predictor.maxMotionY) * 0.5;
            motionY += motionYOffset;
            return ProjectileUtil.predict(posX, posY, posZ, motionX, motionY, motionZ, 0.99F, 0.25, 0.03F, false);
         } else {
            return null;
         }
      }

      private BlockPos resolveLandingBlock(Rotation rotation) {
         ProjectileUtil.ProjectileHit hit = this.simulateLanding(rotation);
         return hit != null && hit.landingPosition() != null ? hit.landingPosition().getBlockPos() : null;
      }
   }
}
