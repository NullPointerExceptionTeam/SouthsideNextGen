package dev.southside.utils.misc;

import dev.southside.Southside;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1268;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;

public class AIWalkController {
   private static final class_310 mc = class_310.method_1551();
   private static final LinkedList<class_2338> path = new LinkedList<>();
   private static volatile boolean walking = false;
   public static final AIWalkController INSTANCE = new AIWalkController();
   public static boolean allowDigging = false;
   public static boolean allowPlacement = false;
   public static double replanDeviationThreshold = 2.0;
   public static long replanCooldownMs = 1500L;
   public static long stuckTimeoutMs = 1000L;
   public static boolean requireExactY = true;
   public static int stitchMaxSegments = 8;
   public static long stitchSleepMs = 120L;
   public static int stitchMaxRetries = 3;
   private final ExecutorService stitcherExec = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "AIWalk-Stitcher");
      t.setDaemon(true);
      return t;
   });
   private final AtomicBoolean stitcherCancel = new AtomicBoolean(false);
   private volatile class_2338 originalGoal = null;
   private volatile Future<?> currentStitcherFuture = null;
   private class_243 lastPos = null;
   private long lastMovedAt = 0L;
   private long lastReplanAt = 0L;
   private final AtomicBoolean mining = new AtomicBoolean(false);

   public static void walkTo(class_2338 target) {
      INSTANCE._walkTo(target);
   }

   private void _walkTo(class_2338 target) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         stopWalking();
         this.originalGoal = target;
         this.stitcherCancel.set(false);
         this.currentStitcherFuture = this.stitcherExec.submit(() -> {
            try {
               Pathfinder.PathfinderConfig cfg = new Pathfinder.PathfinderConfig();
               cfg.allowUnloadedTraversal = true;
               cfg.unloadedChunkPenalty = 6.0;
               List<class_2338> fullPath = this.buildFullPathByStitching(mc.field_1687, mc.field_1724.method_24515(), target, cfg);
               if (fullPath == null || fullPath.isEmpty()) {
                  mc.execute(() -> {
                     if (mc.field_1724 != null) {
                        ChatUtils.warning("[PathFinder] 无法找到到达目标的路径");
                     }
                  });
                  return;
               }

               mc.execute(() -> {
                  synchronized (path) {
                     path.clear();
                     path.addAll(fullPath);
                     walking = true;
                  }
               });
            } catch (Exception var4) {
               var4.printStackTrace();
               mc.execute(() -> {
                  if (mc.field_1724 != null) {
                     ChatUtils.error("[PathFinder] 寻路异常: " + var4.getMessage());
                  }
               });
            }
         });
      }
   }

   private List<class_2338> buildFullPathByStitching(class_1937 world, class_2338 start, class_2338 goal, Pathfinder.PathfinderConfig cfg) {
      if (world != null && start != null && goal != null) {
         List<class_2338> resultFull = new LinkedList<>();
         class_2338 currentStart = start;
         class_2338 lastReached = null;
         int segments = 0;
         int retries = 0;

         while (segments < stitchMaxSegments && !this.stitcherCancel.get()) {
            Pathfinder.PathResult pr = Pathfinder.findPathInternalForExternal(world, currentStart, goal, cfg);
            if (pr != null && pr.path != null && !pr.path.isEmpty()) {
               if (resultFull.isEmpty()) {
                  resultFull.addAll(pr.path);
               } else if (!pr.path.isEmpty()) {
                  if (resultFull.get(resultFull.size() - 1).equals(pr.path.get(0))) {
                     for (int i = 1; i < pr.path.size(); i++) {
                        resultFull.add(pr.path.get(i));
                     }
                  } else {
                     resultFull.addAll(pr.path);
                  }
               }

               lastReached = pr.reached;
               segments++;
               if (pr.complete && pr.reached.equals(goal)) {
                  return resultFull;
               }

               if (pr.reached.equals(currentStart)) {
                  if (++retries > stitchMaxRetries) {
                     break;
                  }

                  cfg.allowUnloadedTraversal = true;
                  cfg.unloadedChunkPenalty = Math.max(1.0, cfg.unloadedChunkPenalty / 2.0);

                  try {
                     Thread.sleep(stitchSleepMs);
                  } catch (InterruptedException var13) {
                  }
               } else {
                  currentStart = pr.reached;

                  try {
                     Thread.sleep(stitchSleepMs);
                  } catch (InterruptedException var12) {
                  }
               }
            } else {
               if (++retries > stitchMaxRetries) {
                  return resultFull.isEmpty() ? null : resultFull;
               }

               try {
                  Thread.sleep(stitchSleepMs);
               } catch (InterruptedException var14) {
               }
            }
         }

         if (!resultFull.isEmpty() && resultFull.get(resultFull.size() - 1).equals(goal)) {
            return resultFull;
         } else {
            return resultFull.isEmpty() ? null : resultFull;
         }
      } else {
         return null;
      }
   }

   public static void stopWalking() {
      INSTANCE._stopWalking();
   }

   private void _stopWalking() {
      this.stitcherCancel.set(true);
      if (this.currentStitcherFuture != null) {
         this.currentStitcherFuture.cancel(true);
      }

      synchronized (path) {
         path.clear();
      }

      walking = false;
      if (mc.field_1724 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (walking && mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 == null) {
            class_243 ppos = mc.field_1724.method_19538();
            class_2338 targetNode = null;
            double minDistSqr = Double.MAX_VALUE;
            synchronized (path) {
               if (path.isEmpty()) {
                  stopWalking();
                  return;
               }

               for (class_2338 node : path) {
                  double distSqr = ppos.method_1025(node.method_46558());
                  if (distSqr < minDistSqr) {
                     minDistSqr = distSqr;
                     targetNode = node;
                  }
               }

               double followDistanceThresholdSqr = 0.2;

               while (!path.isEmpty() && ppos.method_1025(path.peek().method_46558()) < followDistanceThresholdSqr) {
                  path.poll();
               }
            }

            if (targetNode != null) {
               double targetX = targetNode.method_10263() + 0.5;
               double targetY = targetNode.method_10264();
               double targetZ = targetNode.method_10260() + 0.5;
               double dx = targetX - ppos.field_1352;
               double dz = targetZ - ppos.field_1350;
               double dy = targetY - ppos.field_1351;
               double distXZ = Math.sqrt(dx * dx + dz * dz);
               if (this.lastPos == null) {
                  this.lastPos = ppos;
                  this.lastMovedAt = System.currentTimeMillis();
               } else if (ppos.method_1025(this.lastPos) > 5.0E-4) {
                  this.lastPos = ppos;
                  this.lastMovedAt = System.currentTimeMillis();
               } else if (System.currentTimeMillis() - this.lastMovedAt > stuckTimeoutMs) {
                  this.replanToCurrentGoal();
                  return;
               }

               double deviation = Math.sqrt(dx * dx + dz * dz);
               if (deviation > replanDeviationThreshold && System.currentTimeMillis() - this.lastReplanAt > replanCooldownMs) {
                  this.lastReplanAt = System.currentTimeMillis();
                  this.replanToCurrentGoal();
               } else if (distXZ < 0.35 && Math.abs(dy) < 1.2) {
                  synchronized (path) {
                     path.poll();
                     if (!path.isEmpty()) {
                        mc.field_1690.field_1903.method_23481(false);
                        return;
                     }
                  }

                  boolean ok = this.finalValidateArrival(this.originalGoal);
                  if (ok) {
                     walking = false;
                     mc.field_1690.field_1894.method_23481(false);
                     mc.field_1690.field_1903.method_23481(false);
                     ChatUtils.success("[PathFinder] 到达目标");
                  } else {
                     this._attemptFinalStitchRetry();
                     mc.field_1690.field_1894.method_23481(false);
                  }
               } else {
                  float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
                  if (Southside.get().getModuleManager().isEnabled(StrafeFix.class)) {
                     RotationUtils.setRotation(new Rotation(yaw, mc.field_1724.method_36455()), 0, MoveFixMode.Strict);
                  } else {
                     mc.field_1724.method_36456(yaw);
                  }

                  if (this.isFrontBlocked(ppos, dx, dz)) {
                     if (allowDigging) {
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        double nx = dx / Math.max(1.0E-6, dist);
                        double nz = dz / Math.max(1.0E-6, dist);
                        int bx = (int)Math.floor(ppos.field_1352 + nx * 0.6);
                        int bz = (int)Math.floor(ppos.field_1350 + nz * 0.6);
                        int by = (int)Math.floor(ppos.field_1351);
                        class_2338 toMine = new class_2338(bx, by, bz);
                        mc.field_1761.method_2910(toMine, class_2350.field_11036);
                        mc.field_1724.method_6104(class_1268.field_5808);
                        mc.field_1690.field_1894.method_23481(false);
                     } else {
                        this.replanToCurrentGoal();
                     }
                  } else {
                     if (dy > 0.5 && mc.field_1724.method_24828()) {
                        mc.field_1724.method_6043();
                     }

                     mc.field_1690.field_1894.method_23481(true);
                  }
               }
            }
         }
      }
   }

   private void _attemptFinalStitchRetry() {
      if (this.currentStitcherFuture == null || this.currentStitcherFuture.isDone()) {
         this.currentStitcherFuture = this.stitcherExec.submit(() -> {
            try {
               Pathfinder.PathfinderConfig cfg = new Pathfinder.PathfinderConfig();
               cfg.allowUnloadedTraversal = true;
               cfg.unloadedChunkPenalty = 4.0;
               cfg.chunkSearchRadius = Math.max(cfg.chunkSearchRadius, 12);
               List<class_2338> partial = this.buildFullPathByStitching(mc.field_1687, mc.field_1724.method_24515(), this.originalGoal, cfg);
               if (partial != null && !partial.isEmpty()) {
                  mc.execute(() -> {
                     synchronized (path) {
                        path.clear();
                        path.addAll(partial);
                        walking = true;
                     }
                  });
               } else {
                  mc.execute(() -> {
                     if (mc.field_1724 != null) {
                        ChatUtils.warning("[PathFinder] 最终拼接失败，尝试重新开始寻路");
                     }

                     walkTo(this.originalGoal);
                  });
               }
            } catch (Exception var3) {
               var3.printStackTrace();
            }
         });
      }
   }

   private boolean finalValidateArrival(class_2338 goal) {
      if (goal == null) {
         return true;
      } else {
         class_243 p = mc.field_1724.method_19538();
         double dx = p.field_1352 - (goal.method_10263() + 0.5);
         double dz = p.field_1350 - (goal.method_10260() + 0.5);
         double horiz = Math.sqrt(dx * dx + dz * dz);
         double dy = Math.abs(p.field_1351 - goal.method_10264());
         double finalXYTolerance = 0.7;
         double finalYTolerance = 0.9;
         return !requireExactY ? horiz <= finalXYTolerance : horiz <= finalXYTolerance && dy <= finalYTolerance;
      }
   }

   private boolean isFrontBlocked(class_243 playerPos, double dx, double dz) {
      double dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < 1.0E-6) {
         return false;
      } else {
         double nx = dx / dist;
         double nz = dz / dist;
         double checkX = playerPos.field_1352 + nx * 0.6;
         double checkZ = playerPos.field_1350 + nz * 0.6;
         int bx = (int)Math.floor(checkX);
         int by = (int)Math.floor(playerPos.field_1351);
         int bz = (int)Math.floor(checkZ);
         class_2338 body = new class_2338(bx, by, bz);
         class_2338 head = body.method_10086(1);
         boolean bodyBlocked = !mc.field_1687.method_8320(body).method_26220(mc.field_1687, body).method_1110();
         boolean headBlocked = !mc.field_1687.method_8320(head).method_26220(mc.field_1687, head).method_1110();
         return bodyBlocked || headBlocked;
      }
   }

   private void replanToCurrentGoal() {
      if (System.currentTimeMillis() - this.lastReplanAt >= replanCooldownMs) {
         this.lastReplanAt = System.currentTimeMillis();
         if (this.originalGoal != null) {
            this._stopWalking();
            this.stitcherCancel.set(false);
            this.currentStitcherFuture = this.stitcherExec.submit(() -> {
               try {
                  Pathfinder.PathfinderConfig cfg = new Pathfinder.PathfinderConfig();
                  cfg.allowUnloadedTraversal = true;
                  cfg.unloadedChunkPenalty = 6.0;
                  cfg.chunkSearchRadius = Math.max(cfg.chunkSearchRadius, 12);
                  List<class_2338> full = this.buildFullPathByStitching(mc.field_1687, mc.field_1724.method_24515(), this.originalGoal, cfg);
                  if (full != null && !full.isEmpty()) {
                     mc.execute(() -> {
                        synchronized (path) {
                           path.clear();
                           path.addAll(full);
                           walking = true;
                        }
                     });
                  } else {
                     mc.execute(() -> {
                        if (mc.field_1724 != null) {
                           ChatUtils.warning("[PathFinder] 重新规划未找到路径");
                        }
                     });
                  }
               } catch (Exception var3) {
                  var3.printStackTrace();
               }
            });
         }
      }
   }

   @EventHandler
   public void onRenderWorld(Render3DEvent event) {
      List<class_2338> local;
      synchronized (path) {
         local = new LinkedList<>(path);
      }

      if (!local.isEmpty()) {
         for (class_2338 p : local) {
            class_238 b1 = new class_238(p);
            Render3D.boxLines(b1, new Color(0, 200, 0, 120));
            Render3D.boxSides(b1, new Color(0, 200, 0, 60));
         }

         class_2338 g = local.get(local.size() - 1);
         class_238 b1 = new class_238(g);
         Render3D.boxLines(b1, new Color(255, 0, 0, 150));
         Render3D.boxSides(b1, new Color(255, 0, 0, 100));
      }
   }
}
