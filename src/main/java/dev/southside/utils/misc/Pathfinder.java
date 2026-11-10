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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

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

   public static Pathfinder.PathResult findPathInternalForExternal(World world, BlockPos currentStart, BlockPos goal, Pathfinder.PathfinderConfig cfg) {
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
         BlockPos near = findNearestReachable(world, goal, cfg, cache, Math.max(6, cfg.chunkSearchRadius));
         if (near != null && !near.equals(goal)) {
            Pathfinder.PathResult alt = aStar(world, currentStart, near, cfg, cache, cancelFlag);
            if (alt != null && !alt.path.isEmpty()) {
               return new Pathfinder.PathResult(alt.path, false, alt.reached, alt.nodesExplored);
            }
         }

         return primary;
      }
   }

   private static BlockPos findReachableYColumn(
      World world, BlockPos goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, int verticalRange
   ) {
      if (cfg.dynamicChunkLoading && !cache.isLoaded(new ChunkPos(goal)) && !cfg.allowUnloadedTraversal
         || !hasSpaceForEntity(world, goal, cfg)
         || !canStandOn(world, goal)
         || cfg.avoidDanger && isDangerousBlock(world, goal)) {
         int d = 1;

         while (d <= verticalRange) {
            BlockPos up = goal.up(d);
            ChunkPos upChunk = new ChunkPos(up);
            if (cfg.dynamicChunkLoading && !cache.isLoaded(upChunk) && !cfg.allowUnloadedTraversal
               || !hasSpaceForEntity(world, up, cfg)
               || !canStandOn(world, up)
               || cfg.avoidDanger && isDangerousBlock(world, up)) {
               BlockPos down = goal.down(d);
               ChunkPos downChunk = new ChunkPos(down);
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

   private static double heuristic(BlockPos a, BlockPos b) {
      double dx = a.getX() - b.getX();
      double dy = a.getY() - b.getY();
      double dz = a.getZ() - b.getZ();
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

   private static boolean isLadder(World world, BlockPos pos) {
      BlockState st = world.getBlockState(pos);
      return st.isOf(Blocks.LADDER) || st.isOf(Blocks.VINE) || st.isOf(Blocks.SCAFFOLDING);
   }

   private static boolean isDangerousBlock(World world, BlockPos pos) {
      BlockState st = world.getBlockState(pos);
      Fluid f = st.getFluidState().getFluid();
      if (f == Fluids.LAVA) {
         return true;
      } else if (st.isOf(Blocks.FIRE) || st.isOf(Blocks.SOUL_FIRE)) {
         return true;
      } else if (st.isOf(Blocks.CACTUS)) {
         return true;
      } else {
         return st.isOf(Blocks.SWEET_BERRY_BUSH) ? true : st.isOf(Blocks.MAGMA_BLOCK);
      }
   }

   private static int waterDepth(World world, BlockPos pos, int maxCheck) {
      int depth = 0;
      BlockPos p = pos;

      for (int i = 0; i < maxCheck; i++) {
         BlockState s = world.getBlockState(p);
         if (!s.getFluidState().isIn(FluidTags.WATER) && !s.getFluidState().isStill()) {
            break;
         }

         depth++;
         p = p.down();
      }

      return depth;
   }

   private static boolean hasSpaceForEntity(World world, BlockPos pos, Pathfinder.PathfinderConfig cfg) {
      int height = (int)Math.ceil(cfg.entityHeight);

      for (int y = 0; y < height; y++) {
         BlockPos p = pos.up(y);
         BlockState st = world.getBlockState(p);
         if (!st.isAir() && !st.getFluidState().isStill() && !st.getCollisionShape(world, p).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private static boolean canStandOn(World world, BlockPos pos) {
      BlockState below = world.getBlockState(pos.down());
      return below.isSolidBlock(world, pos.down()) || below.getFluidState().isIn(FluidTags.WATER) || isLadder(world, pos.down());
   }

   private static Pathfinder.MovementType getMovementType(
      World world, BlockPos from, BlockPos to, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache
   ) {
      ChunkPos cp = new ChunkPos(to);
      if (!cache.isLoaded(cp) && !cfg.allowUnloadedTraversal) {
         return Pathfinder.MovementType.INVALID;
      } else {
         boolean isUnloaded = !cache.isLoaded(cp);
         int dy = to.getY() - from.getY();
         if (!isUnloaded) {
            if (!hasSpaceForEntity(world, to, cfg)) {
               return Pathfinder.MovementType.INVALID;
            }

            if (cfg.avoidDanger && isDangerousBlock(world, to)) {
               return Pathfinder.MovementType.INVALID;
            }
         }

         BlockState toState = isUnloaded ? Blocks.AIR.getDefaultState() : world.getBlockState(to);
         boolean inWater = toState.getFluidState().isIn(FluidTags.WATER) || toState.getFluidState().isStill();
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

   private static boolean checkDiagonal(World world, BlockPos from, int[] dir, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache) {
      int dx = dir[0];
      int dy = dir[1];
      int dz = dir[2];
      if (Math.abs(dx) + Math.abs(dz) >= 2) {
         BlockPos a = from.add(dx, 0, 0);
         BlockPos b = from.add(0, 0, dz);
         ChunkPos ap = new ChunkPos(a);
         ChunkPos bp = new ChunkPos(b);
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
         BlockPos p1 = from.add(dx, dy, 0);
         BlockPos p2 = from.add(0, dy, dz);
         BlockPos p3 = from.add(dx, 0, dz);
         if (cfg.dynamicChunkLoading
            && (!cache.isLoaded(new ChunkPos(p1)) || !cache.isLoaded(new ChunkPos(p2)) || !cache.isLoaded(new ChunkPos(p3)))
            && !cfg.allowUnloadedTraversal) {
            return false;
         }

         if (!hasSpaceForEntity(world, p1, cfg) || !hasSpaceForEntity(world, p2, cfg) || !hasSpaceForEntity(world, p3, cfg)) {
            return false;
         }
      }

      return true;
   }

   private static List<BlockPos> reconstruct(Pathfinder.Node node) {
      List<BlockPos> out = new ArrayList<>();

      while (node != null) {
         out.add(node.pos);
         node = node.parent;
      }

      Collections.reverse(out);
      return out;
   }

   private static Pathfinder.PathResult aStar(
      World world, BlockPos start, BlockPos goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, AtomicBoolean cancelFlag
   ) {
      PriorityQueue<Pathfinder.Node> open = new PriorityQueue<>(Comparator.comparingDouble(nx -> nx.f));
      Map<BlockPos, Pathfinder.Node> all = new HashMap<>();
      Set<BlockPos> closed = new HashSet<>();
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
         ChunkPos curChunk = new ChunkPos(cur.pos);
         if (!cfg.dynamicChunkLoading || cache.isLoaded(curChunk) || cfg.allowUnloadedTraversal) {
            for (int[] dir : DIRS) {
               BlockPos nb = cur.pos.add(dir[0], dir[1], dir[2]);
               if (!closed.contains(nb)) {
                  ChunkPos nbChunk = new ChunkPos(nb);
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

   private static double getChunkDist(ChunkPos a, ChunkPos b) {
      int dx = a.x - b.x;
      int dz = a.z - b.z;
      return Math.sqrt(dx * dx + dz * dz);
   }

   private static BlockPos findNearestReachable(World world, BlockPos goal, Pathfinder.PathfinderConfig cfg, Pathfinder.ChunkCache cache, int radius) {
      Queue<BlockPos> q = new ArrayDeque<>();
      Set<BlockPos> seen = new HashSet<>();
      q.add(goal);
      seen.add(goal);
      int steps = 0;

      while (!q.isEmpty() && steps++ < radius * radius * 20) {
         BlockPos p = q.poll();
         if ((!cfg.dynamicChunkLoading || cache.isLoaded(new ChunkPos(p)) || cfg.allowUnloadedTraversal)
            && hasSpaceForEntity(world, p, cfg)
            && canStandOn(world, p)
            && (!cfg.avoidDanger || !isDangerousBlock(world, p))) {
            return p;
         }

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dy != 0 || dz != 0) {
                     BlockPos np = p.add(dx, dy, dz);
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

   public static List<BlockPos> findPath(World world, BlockPos start, BlockPos goal, Pathfinder.PathfinderConfig cfg) {
      Pathfinder.PathResult r = findPathInternal(world, start, goal, cfg);
      return r.path;
   }

   private static Pathfinder.PathResult findPathInternal(World world, BlockPos start, BlockPos goal, Pathfinder.PathfinderConfig cfg) {
      if (cfg == null) {
         cfg = new Pathfinder.PathfinderConfig();
      }

      Pathfinder.ChunkCache cache = new Pathfinder.ChunkCache(world);
      AtomicBoolean cancelFlag = new AtomicBoolean(false);
      Pathfinder.PathResult result = aStar(world, start, goal, cfg, cache, cancelFlag);
      if (!result.complete) {
         BlockPos near = findNearestReachable(world, goal, cfg, cache, Math.max(6, cfg.chunkSearchRadius));
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

      public void findPathAsync(World world, BlockPos start, BlockPos goal, Pathfinder.PathfinderConfig cfg, Pathfinder.PathCallback cb) {
         this.cancelled.set(false);
         this.future = this.exec.submit(() -> {
            try {
               Pathfinder.PathfinderConfig finalCfg = cfg == null ? new Pathfinder.PathfinderConfig() : cfg;
               Pathfinder.ChunkCache cache = new Pathfinder.ChunkCache(world);
               AtomicBoolean cancelFlag = this.cancelled;
               Pathfinder.PathResult r = Pathfinder.aStar(world, start, goal, finalCfg, cache, cancelFlag);
               if (!r.complete) {
                  BlockPos near = Pathfinder.findNearestReachable(world, goal, finalCfg, cache, Math.max(6, finalCfg.chunkSearchRadius));
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
      private final World world;
      private final Map<ChunkPos, Boolean> cached = new ConcurrentHashMap<>();

      public ChunkCache(World w) {
         this.world = w;
      }

      public boolean isLoaded(ChunkPos p) {
         return this.cached.computeIfAbsent(p, cp -> {
            Chunk c = this.world.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
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
      BlockPos pos;
      Pathfinder.Node parent;
      double g = Double.MAX_VALUE;
      double h;
      double f;

      Node(BlockPos pos) {
         this.pos = pos;
      }
   }

   public interface PathCallback {
      void onPathFound(Pathfinder.PathResult var1);

      void onError(Exception var1);
   }

   public static class PathResult {
      public final List<BlockPos> path;
      public final boolean complete;
      public final BlockPos reached;
      public final int nodesExplored;

      public PathResult(List<BlockPos> path, boolean complete, BlockPos reached, int nodesExplored) {
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
