package dev.southside.utils.misc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2806;
import net.minecraft.class_3486;
import net.minecraft.class_3611;
import net.minecraft.class_3612;

public class Pathfinder {
   private static final int[][] DIRS = new int[][]{
      {1, 0, 0},
      {-1, 0, 0},
      {0, 0, 1},
      {0, 0, -1},
      {1, 0, 1},
      {1, 0, -1},
      {-1, 0, 1},
      {-1, 0, -1},
      {0, 1, 0},
      {1, 1, 0},
      {-1, 1, 0},
      {0, 1, 1},
      {0, 1, -1},
      {1, 1, 1},
      {1, 1, -1},
      {-1, 1, 1},
      {-1, 1, -1},
      {0, -1, 0},
      {1, -1, 0},
      {-1, -1, 0},
      {0, -1, 1},
      {0, -1, -1},
      {1, -1, 1},
      {1, -1, -1},
      {-1, -1, 1},
      {-1, -1, -1}
   };
   private static final double SQRT2 = Math.sqrt(2.0);
   private static final double SQRT3 = Math.sqrt(3.0);

   public static Pathfinder.PathResult findPathInternalForExternal(class_1937 world, class_2338 currentStart, class_2338 goal, Pathfinder.PathfinderConfig cfg) {
      if (cfg == null) {
         cfg = new Pathfinder.PathfinderConfig();
      }

      Pathfinder.ChunkCache cache = new Pathfinder.ChunkCache(world);
      AtomicBoolean cancelFlag = new AtomicBoolean(false);
      Pathfinder.PathResult primary = aStar(world, currentStart, goal, cfg, cache, cancelFlag);
      if (primary == null) {
         return new Pathfinder.PathResult(Collections.emptyList(), false, currentStart, 0);
      } else if (primary.complete && primary.reached.equals(goal)) {
         return primary;
      } else {
         class_2338 near = findNearestReachable(world, goal, cfg, cache, Math.max(6, cfg.chunkSearchRadius));
         if (near != null && !near.equals(goal)) {
            Pathfinder.PathResult alt = aStar(world, currentStart, near, cfg, cache, cancelFlag);
            if (alt != null && !alt.path.isEmpty()) {
               return new Pathfinder.PathResult(alt.path, false, alt.reached, alt.nodesExplored);
            }
         }

         return primary;
      }
   }

   private static class_2338 findReachableYColumn(
      class_1937 world, class_2338 goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, int verticalRange
   ) {
      if (cfg.dynamicChunkLoading && !cache.isLoaded(new class_1923(goal)) && !cfg.allowUnloadedTraversal
         || !hasSpaceForEntity(world, goal, cfg)
         || !canStandOn(world, goal)
         || cfg.avoidDanger && isDangerousBlock(world, goal)) {
         int d = 1;

         while (d <= verticalRange) {
            class_2338 up = goal.method_10086(d);
            class_1923 upChunk = new class_1923(up);
            if (cfg.dynamicChunkLoading && !cache.isLoaded(upChunk) && !cfg.allowUnloadedTraversal
               || !hasSpaceForEntity(world, up, cfg)
               || !canStandOn(world, up)
               || cfg.avoidDanger && isDangerousBlock(world, up)) {
               class_2338 down = goal.method_10087(d);
               class_1923 downChunk = new class_1923(down);
               if (cfg.dynamicChunkLoading && !cache.isLoaded(downChunk) && !cfg.allowUnloadedTraversal
                  || !hasSpaceForEntity(world, down, cfg)
                  || !canStandOn(world, down)
                  || cfg.avoidDanger && isDangerousBlock(world, down)) {
                  d++;
                  continue;
               }

               return down;
            }

            return up;
         }

         return null;
      } else {
         return goal;
      }
   }

   private static double heuristic(class_2338 a, class_2338 b) {
      double dx = a.method_10263() - b.method_10263();
      double dy = a.method_10264() - b.method_10264();
      double dz = a.method_10260() - b.method_10260();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private static double moveCost(int[] dir) {
      int comps = Math.abs(dir[0]) + Math.abs(dir[1]) + Math.abs(dir[2]);
      if (comps == 1) {
         return 1.0;
      } else {
         return comps == 2 ? SQRT2 : SQRT3;
      }
   }

   private static boolean isLadder(class_1937 world, class_2338 pos) {
      class_2680 st = world.method_8320(pos);
      return st.method_27852(class_2246.field_9983) || st.method_27852(class_2246.field_10597) || st.method_27852(class_2246.field_16492);
   }

   private static boolean isDangerousBlock(class_1937 world, class_2338 pos) {
      class_2680 st = world.method_8320(pos);
      class_3611 f = st.method_26227().method_15772();
      if (f == class_3612.field_15908) {
         return true;
      } else if (st.method_27852(class_2246.field_10036) || st.method_27852(class_2246.field_22089)) {
         return true;
      } else if (st.method_27852(class_2246.field_10029)) {
         return true;
      } else {
         return st.method_27852(class_2246.field_16999) ? true : st.method_27852(class_2246.field_10092);
      }
   }

   private static int waterDepth(class_1937 world, class_2338 pos, int maxCheck) {
      int depth = 0;
      class_2338 p = pos;

      for (int i = 0; i < maxCheck; i++) {
         class_2680 s = world.method_8320(p);
         if (!s.method_26227().method_15767(class_3486.field_15517) && !s.method_26227().method_15771()) {
            break;
         }

         depth++;
         p = p.method_10074();
      }

      return depth;
   }

   private static boolean hasSpaceForEntity(class_1937 world, class_2338 pos, Pathfinder.PathfinderConfig cfg) {
      int height = (int)Math.ceil(cfg.entityHeight);

      for (int y = 0; y < height; y++) {
         class_2338 p = pos.method_10086(y);
         class_2680 st = world.method_8320(p);
         if (!st.method_26215() && !st.method_26227().method_15771() && !st.method_26220(world, p).method_1110()) {
            return false;
         }
      }

      return true;
   }

   private static boolean canStandOn(class_1937 world, class_2338 pos) {
      class_2680 below = world.method_8320(pos.method_10074());
      return below.method_26212(world, pos.method_10074()) || below.method_26227().method_15767(class_3486.field_15517) || isLadder(world, pos.method_10074());
   }

   private static Pathfinder.MovementType getMovementType(
      class_1937 world, class_2338 from, class_2338 to, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache
   ) {
      class_1923 cp = new class_1923(to);
      if (!cache.isLoaded(cp) && !cfg.allowUnloadedTraversal) {
         return Pathfinder.MovementType.INVALID;
      } else {
         boolean isUnloaded = !cache.isLoaded(cp);
         int dy = to.method_10264() - from.method_10264();
         if (!isUnloaded) {
            if (!hasSpaceForEntity(world, to, cfg)) {
               return Pathfinder.MovementType.INVALID;
            }

            if (cfg.avoidDanger && isDangerousBlock(world, to)) {
               return Pathfinder.MovementType.INVALID;
            }
         }

         class_2680 toState = isUnloaded ? class_2246.field_10124.method_9564() : world.method_8320(to);
         boolean inWater = toState.method_26227().method_15767(class_3486.field_15517) || toState.method_26227().method_15771();
         boolean onLadder = cfg.allowLadders && !isUnloaded && isLadder(world, to);
         if (inWater) {
            if (!cfg.allowSwimming) {
               return Pathfinder.MovementType.INVALID;
            } else {
               if (!isUnloaded) {
                  int depth = waterDepth(world, to, cfg.maxSwimDepth + 1);
                  if (depth > cfg.maxSwimDepth) {
                     return Pathfinder.MovementType.INVALID;
                  }
               }

               return Pathfinder.MovementType.SWIM;
            }
         } else {
            boolean hasGround = isUnloaded ? true : canStandOn(world, to);
            if (!hasGround && !onLadder) {
               return Pathfinder.MovementType.INVALID;
            } else if (dy > 0) {
               if (dy > cfg.maxJumpHeight) {
                  return onLadder ? Pathfinder.MovementType.LADDER : Pathfinder.MovementType.INVALID;
               } else {
                  return !isUnloaded && !hasSpaceForEntity(world, to, cfg) ? Pathfinder.MovementType.INVALID : Pathfinder.MovementType.JUMP;
               }
            } else if (dy < 0) {
               if (Math.abs(dy) > cfg.maxFallDistance) {
                  return Pathfinder.MovementType.INVALID;
               } else {
                  return !isUnloaded && !hasSpaceForEntity(world, to, cfg) ? Pathfinder.MovementType.INVALID : Pathfinder.MovementType.FALL;
               }
            } else {
               return Pathfinder.MovementType.WALK;
            }
         }
      }
   }

   private static boolean checkDiagonal(class_1937 world, class_2338 from, int[] dir, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache) {
      int dx = dir[0];
      int dy = dir[1];
      int dz = dir[2];
      if (Math.abs(dx) + Math.abs(dz) >= 2) {
         class_2338 a = from.method_10069(dx, 0, 0);
         class_2338 b = from.method_10069(0, 0, dz);
         class_1923 ap = new class_1923(a);
         class_1923 bp = new class_1923(b);
         if (!cfg.dynamicChunkLoading || cache.isLoaded(ap) && cache.isLoaded(bp)) {
            if (!hasSpaceForEntity(world, a, cfg) || !hasSpaceForEntity(world, b, cfg)) {
               return false;
            }

            if (dy == 0 && !canStandOn(world, a) && !canStandOn(world, b)) {
               return false;
            }
         } else if (!cfg.allowUnloadedTraversal) {
            return false;
         }
      }

      if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) >= 3) {
         class_2338 p1 = from.method_10069(dx, dy, 0);
         class_2338 p2 = from.method_10069(0, dy, dz);
         class_2338 p3 = from.method_10069(dx, 0, dz);
         if (cfg.dynamicChunkLoading
            && (!cache.isLoaded(new class_1923(p1)) || !cache.isLoaded(new class_1923(p2)) || !cache.isLoaded(new class_1923(p3)))
            && !cfg.allowUnloadedTraversal) {
            return false;
         }

         if (!hasSpaceForEntity(world, p1, cfg) || !hasSpaceForEntity(world, p2, cfg) || !hasSpaceForEntity(world, p3, cfg)) {
            return false;
         }
      }

      return true;
   }

   private static List<class_2338> reconstruct(Pathfinder.Node node) {
      List<class_2338> out = new ArrayList<>();

      while (node != null) {
         out.add(node.pos);
         node = node.parent;
      }

      Collections.reverse(out);
      return out;
   }

   private static Pathfinder.PathResult aStar(
      class_1937 world, class_2338 start, class_2338 goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, AtomicBoolean cancelFlag
   ) {
      PriorityQueue<Pathfinder.Node> open = new PriorityQueue<>(Comparator.comparingDouble(nx -> nx.f));
      Map<class_2338, Pathfinder.Node> all = new HashMap<>();
      Set<class_2338> closed = new HashSet<>();
      Pathfinder.Node s = new Pathfinder.Node(start);
      s.g = 0.0;
      s.h = heuristic(start, goal);
      s.f = s.h;
      open.add(s);
      all.put(start, s);
      Pathfinder.Node best = s;
      double bestDist = s.h;
      int iter = 0;

      while (!open.isEmpty() && iter++ < cfg.maxIterations) {
         if (cancelFlag.get()) {
            return new Pathfinder.PathResult(reconstruct(best), false, best.pos, iter);
         }

         Pathfinder.Node cur = open.poll();
         if (cur.h < bestDist) {
            bestDist = cur.h;
            best = cur;
         }

         if (cur.pos.equals(goal)) {
            return new Pathfinder.PathResult(reconstruct(cur), true, goal, iter);
         }

         closed.add(cur.pos);
         class_1923 curChunk = new class_1923(cur.pos);
         if (!cfg.dynamicChunkLoading || cache.isLoaded(curChunk) || cfg.allowUnloadedTraversal) {
            for (int[] dir : DIRS) {
               class_2338 nb = cur.pos.method_10069(dir[0], dir[1], dir[2]);
               if (!closed.contains(nb)) {
                  class_1923 nbChunk = new class_1923(nb);
                  if (!cfg.dynamicChunkLoading || cache.isLoaded(nbChunk) || cfg.allowUnloadedTraversal) {
                     Pathfinder.MovementType mt = getMovementType(world, cur.pos, nb, cfg, cache);
                     if (mt != Pathfinder.MovementType.INVALID && checkDiagonal(world, cur.pos, dir, cfg, cache)) {
                        double cost = moveCost(dir);
                        if (!cache.isLoaded(nbChunk) && cfg.allowUnloadedTraversal) {
                           cost += cfg.unloadedChunkPenalty;
                        }

                        if (mt == Pathfinder.MovementType.JUMP) {
                           cost *= 1.4;
                        } else if (mt == Pathfinder.MovementType.FALL) {
                           cost *= 1.2;
                        } else if (mt == Pathfinder.MovementType.SWIM) {
                           cost *= 1.8;
                        } else if (mt == Pathfinder.MovementType.LADDER) {
                           cost *= 1.5;
                        }

                        double g = cur.g + cost;
                        Pathfinder.Node n = all.get(nb);
                        if (n == null) {
                           n = new Pathfinder.Node(nb);
                           all.put(nb, n);
                        }

                        if (!(g >= n.g)) {
                           n.g = g;
                           n.h = heuristic(nb, goal);
                           n.f = n.g + n.h;
                           n.parent = cur;
                           if (!open.contains(n)) {
                              open.add(n);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return new Pathfinder.PathResult(reconstruct(best), false, best.pos, iter);
   }

   private static double getChunkDist(class_1923 a, class_1923 b) {
      int dx = a.field_9181 - b.field_9181;
      int dz = a.field_9180 - b.field_9180;
      return Math.sqrt(dx * dx + dz * dz);
   }

   private static class_2338 findNearestReachable(class_1937 world, class_2338 goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, int radius) {
      Queue<class_2338> q = new ArrayDeque<>();
      Set<class_2338> seen = new HashSet<>();
      q.add(goal);
      seen.add(goal);
      int steps = 0;

      while (!q.isEmpty() && steps++ < radius * radius * 20) {
         class_2338 p = q.poll();
         if ((!cfg.dynamicChunkLoading || cache.isLoaded(new class_1923(p)) || cfg.allowUnloadedTraversal)
            && hasSpaceForEntity(world, p, cfg)
            && canStandOn(world, p)
            && (!cfg.avoidDanger || !isDangerousBlock(world, p))) {
            return p;
         }

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dy != 0 || dz != 0) {
                     class_2338 np = p.method_10069(dx, dy, dz);
                     if (seen.add(np)) {
                        q.add(np);
                     }
                  }
               }
            }
         }
      }

      return goal;
   }

   public static List<class_2338> findPath(class_1937 world, class_2338 start, class_2338 goal, Pathfinder.PathfinderConfig cfg) {
      Pathfinder.PathResult r = findPathInternal(world, start, goal, cfg);
      return r.path;
   }

   private static Pathfinder.PathResult findPathInternal(class_1937 world, class_2338 start, class_2338 goal, Pathfinder.PathfinderConfig cfg) {
      if (cfg == null) {
         cfg = new Pathfinder.PathfinderConfig();
      }

      Pathfinder.ChunkCache cache = new Pathfinder.ChunkCache(world);
      AtomicBoolean cancelFlag = new AtomicBoolean(false);
      Pathfinder.PathResult result = aStar(world, start, goal, cfg, cache, cancelFlag);
      if (!result.complete) {
         class_2338 near = findNearestReachable(world, goal, cfg, cache, Math.max(6, cfg.chunkSearchRadius));
         if (!near.equals(goal)) {
            Pathfinder.PathResult alt = aStar(world, start, near, cfg, cache, cancelFlag);
            if (!alt.path.isEmpty()) {
               return new Pathfinder.PathResult(alt.path, false, alt.reached, alt.nodesExplored);
            }
         }
      }

      return result;
   }

   public static class AsyncPathfinder {
      private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
         Thread t = new Thread(r, "Pathfinder-Thread");
         t.setDaemon(true);
         return t;
      });
      private Future<?> future;
      private final AtomicBoolean cancelled = new AtomicBoolean(false);
      private Pathfinder.PathResult result;

      public void findPathAsync(class_1937 world, class_2338 start, class_2338 goal, Pathfinder.PathfinderConfig cfg, Pathfinder.PathCallback cb) {
         this.cancelled.set(false);
         this.future = this.exec.submit(() -> {
            try {
               Pathfinder.PathfinderConfig finalCfg = cfg == null ? new Pathfinder.PathfinderConfig() : cfg;
               Pathfinder.ChunkCache cache = new Pathfinder.ChunkCache(world);
               AtomicBoolean cancelFlag = this.cancelled;
               Pathfinder.PathResult r = Pathfinder.aStar(world, start, goal, finalCfg, cache, cancelFlag);
               if (!r.complete) {
                  class_2338 near = Pathfinder.findNearestReachable(world, goal, finalCfg, cache, Math.max(6, finalCfg.chunkSearchRadius));
                  if (!near.equals(goal)) {
                     Pathfinder.PathResult alt = Pathfinder.aStar(world, start, near, finalCfg, cache, cancelFlag);
                     if (!alt.path.isEmpty()) {
                        r = new Pathfinder.PathResult(alt.path, false, alt.reached, alt.nodesExplored);
                     }
                  }
               }

               this.result = r;
               if (!this.cancelled.get() && cb != null) {
                  cb.onPathFound(r);
               }
            } catch (Exception var12) {
               if (cb != null) {
                  cb.onError(var12);
               }
            }
         });
      }

      public void cancel() {
         this.cancelled.set(true);
         if (this.future != null) {
            this.future.cancel(true);
         }
      }

      public boolean isCancelled() {
         return this.cancelled.get();
      }

      public Pathfinder.PathResult getResult() {
         return this.result;
      }

      public void shutdown() {
         this.exec.shutdownNow();
      }
   }

   private static class ChunkCache {
      private final class_1937 world;
      private final Map<class_1923, Boolean> cached = new ConcurrentHashMap<>();

      public ChunkCache(class_1937 w) {
         this.world = w;
      }

      public boolean isLoaded(class_1923 p) {
         return this.cached.computeIfAbsent(p, cp -> {
            class_2791 c = this.world.method_8402(cp.field_9181, cp.field_9180, class_2806.field_12803, false);
            return c != null;
         });
      }
   }

   private static enum MovementType {
      WALK,
      JUMP,
      FALL,
      SWIM,
      LADDER,
      INVALID;
   }

   private static class Node {
      class_2338 pos;
      Pathfinder.Node parent;
      double g = Double.MAX_VALUE;
      double h;
      double f;

      Node(class_2338 pos) {
         this.pos = pos;
      }
   }

   public interface PathCallback {
      void onPathFound(Pathfinder.PathResult var1);

      void onError(Exception var1);
   }

   public static class PathResult {
      public final List<class_2338> path;
      public final boolean complete;
      public final class_2338 reached;
      public final int nodesExplored;

      public PathResult(List<class_2338> path, boolean complete, class_2338 reached, int nodesExplored) {
         this.path = path;
         this.complete = complete;
         this.reached = reached;
         this.nodesExplored = nodesExplored;
      }
   }

   public static class PathfinderConfig {
      public int maxFallDistance = 3;
      public int maxJumpHeight = 1;
      public boolean allowSwimming = true;
      public int maxSwimDepth = 3;
      public boolean allowLadders = true;
      public boolean avoidDanger = true;
      public double entityHeight = 1.8;
      public double entityWidth = 0.6;
      public int chunkSearchRadius = 8;
      public int maxIterations = 20000;
      public boolean dynamicChunkLoading = true;
      public boolean allowUnloadedTraversal = true;
      public double unloadedChunkPenalty = 8.0;
      public boolean preferWaterSurface = true;
      public boolean allowDigging = false;
      public boolean allowPlacement = false;
   }
}
