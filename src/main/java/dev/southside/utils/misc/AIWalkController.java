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
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class AIWalkController {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final LinkedList<BlockPos> path = new LinkedList<>();
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
   private volatile BlockPos originalGoal = null;
   private volatile Future<?> currentStitcherFuture = null;
   private Vec3d lastPos = null;
   private long lastMovedAt = 0L;
   private long lastReplanAt = 0L;
   private final AtomicBoolean mining = new AtomicBoolean(false);

   public static void walkTo(BlockPos target) {
      INSTANCE._walkTo(target);
   }

   private void _walkTo(BlockPos target) {
      if (mc.player != null && mc.world != null) {
         stopWalking();
         this.originalGoal = target;
         this.stitcherCancel.set(false);
         this.currentStitcherFuture = this.stitcherExec.submit(() -> {
            try {
               Pathfinder.PathfinderConfig cfg = new Pathfinder.PathfinderConfig();
               cfg.allowUnloadedTraversal = true;
               cfg.unloadedChunkPenalty = 6.0;
               List<BlockPos> fullPath = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), target, cfg);
               if (fullPath == null || fullPath.isEmpty()) {
                  mc.execute(() -> {
                     if (mc.player != null) {
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
                  if (mc.player != null) {
                     ChatUtils.error("[PathFinder] 寻路异常: " + var4.getMessage());
                  }
               });
            }
         });
      }
   }

   private List<BlockPos> buildFullPathByStitching(World world, BlockPos start, BlockPos goal, Pathfinder.PathfinderConfig cfg) {
      if (world != null && start != null && goal != null) {
         List<BlockPos> resultFull = new LinkedList<>();
         BlockPos currentStart = start;
         BlockPos lastReached = null;
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
      if (mc.player != null) {
         mc.options.forwardKey.setPressed(false);
         mc.options.jumpKey.setPressed(false);
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (walking && mc.player != null && mc.world != null) {
         if (mc.currentScreen == null) {
            Vec3d ppos = mc.player.getPos();
            BlockPos targetNode = null;
            double minDistSqr = Double.MAX_VALUE;
            synchronized (path) {
               if (path.isEmpty()) {
                  stopWalking();
                  return;
               }

               for (BlockPos node : path) {
                  double distSqr = ppos.squaredDistanceTo(node.toCenterPos());
                  if (distSqr < minDistSqr) {
                     minDistSqr = distSqr;
                     targetNode = node;
                  }
               }

               double followDistanceThresholdSqr = 0.2;

               while (!path.isEmpty() && ppos.squaredDistanceTo(path.peek().toCenterPos()) < followDistanceThresholdSqr) {
                  path.poll();
               }
            }

            if (targetNode != null) {
               double targetX = targetNode.getX() + 0.5;
               double targetY = targetNode.getY();
               double targetZ = targetNode.getZ() + 0.5;
               double dx = targetX - ppos.x;
               double dz = targetZ - ppos.z;
               double dy = targetY - ppos.y;
               double distXZ = Math.sqrt(dx * dx + dz * dz);
               if (this.lastPos == null) {
                  this.lastPos = ppos;
                  this.lastMovedAt = System.currentTimeMillis();
               } else if (ppos.squaredDistanceTo(this.lastPos) > 5.0E-4) {
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
                        mc.options.jumpKey.setPressed(false);
                        return;
                     }
                  }

                  boolean ok = this.finalValidateArrival(this.originalGoal);
                  if (ok) {
                     walking = false;
                     mc.options.forwardKey.setPressed(false);
                     mc.options.jumpKey.setPressed(false);
                     ChatUtils.success("[PathFinder] 到达目标");
                  } else {
                     this._attemptFinalStitchRetry();
                     mc.options.forwardKey.setPressed(false);
                  }
               } else {
                  float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
                  if (Southside.get().getModuleManager().isEnabled(StrafeFix.class)) {
                     RotationUtils.setRotation(new Rotation(yaw, mc.player.getPitch()), 0, MoveFixMode.Strict);
                  } else {
                     mc.player.setYaw(yaw);
                  }

                  if (this.isFrontBlocked(ppos, dx, dz)) {
                     if (allowDigging) {
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        double nx = dx / Math.max(1.0E-6, dist);
                        double nz = dz / Math.max(1.0E-6, dist);
                        int bx = (int)Math.floor(ppos.x + nx * 0.6);
                        int bz = (int)Math.floor(ppos.z + nz * 0.6);
                        int by = (int)Math.floor(ppos.y);
                        BlockPos toMine = new BlockPos(bx, by, bz);
                        mc.interactionManager.attackBlock(toMine, Direction.UP);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        mc.options.forwardKey.setPressed(false);
                     } else {
                        this.replanToCurrentGoal();
                     }
                  } else {
                     if (dy > 0.5 && mc.player.isOnGround()) {
                        mc.player.jump();
                     }

                     mc.options.forwardKey.setPressed(true);
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
               List<BlockPos> partial = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), this.originalGoal, cfg);
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
                     if (mc.player != null) {
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

   private boolean finalValidateArrival(BlockPos goal) {
      if (goal == null) {
         return true;
      } else {
         Vec3d p = mc.player.getPos();
         double dx = p.x - (goal.getX() + 0.5);
         double dz = p.z - (goal.getZ() + 0.5);
         double horiz = Math.sqrt(dx * dx + dz * dz);
         double dy = Math.abs(p.y - goal.getY());
         double finalXYTolerance = 0.7;
         double finalYTolerance = 0.9;
         return !requireExactY ? horiz <= finalXYTolerance : horiz <= finalXYTolerance && dy <= finalYTolerance;
      }
   }

   private boolean isFrontBlocked(Vec3d playerPos, double dx, double dz) {
      double dist = Math.sqrt(dx * dx + dz * dz);
      if (dist < 1.0E-6) {
         return false;
      } else {
         double nx = dx / dist;
         double nz = dz / dist;
         double checkX = playerPos.x + nx * 0.6;
         double checkZ = playerPos.z + nz * 0.6;
         int bx = (int)Math.floor(checkX);
         int by = (int)Math.floor(playerPos.y);
         int bz = (int)Math.floor(checkZ);
         BlockPos body = new BlockPos(bx, by, bz);
         BlockPos head = body.up(1);
         boolean bodyBlocked = !mc.world.getBlockState(body).getCollisionShape(mc.world, body).isEmpty();
         boolean headBlocked = !mc.world.getBlockState(head).getCollisionShape(mc.world, head).isEmpty();
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
                  List<BlockPos> full = this.buildFullPathByStitching(mc.world, mc.player.getBlockPos(), this.originalGoal, cfg);
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
                        if (mc.player != null) {
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
      List<BlockPos> local;
      synchronized (path) {
         local = new LinkedList<>(path);
      }

      if (!local.isEmpty()) {
         for (BlockPos p : local) {
            Box b1 = new Box(p);
            Render3D.boxLines(b1, new Color(0, 200, 0, 120));
            Render3D.boxSides(b1, new Color(0, 200, 0, 60));
         }

         BlockPos g = local.get(local.size() - 1);
         Box b1 = new Box(g);
         Render3D.boxLines(b1, new Color(255, 0, 0, 150));
         Render3D.boxSides(b1, new Color(255, 0, 0, 100));
      }
   }
}
