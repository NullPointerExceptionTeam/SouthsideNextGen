package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AutoWeb extends Module {
   public final Slider rangeSetting = new Slider("Range", 4.0, 3.0, 5.0, 0.1);
   public final Slider delaySetting = new Slider("Delay (tick)", 4.0, 1.0, 20.0, 1.0);
   public final Switch renderSetting = new Switch("Render", true);
   public final Switch groundWebSetting = new Switch("Ground Web", false);
   public final Switch killAuraSyncSetting = new Switch("Kill Aura Sync", true);
   public final Switch blockPlaceSetting = new Switch("Block Place", true);
   public final Switch lavaSetting = new Switch("Lava", true);
   public final Switch lavaWaitWaterBucketSetting = new Switch("Lava Wait Water Bucket", true);
   public final Switch debugSetting = new Switch("Debug", true);
   private final MSTimer placementTimer = new MSTimer();
   private AutoWeb.Phase placementPhase = AutoWeb.Phase.IDLE;
   private AutoWeb.PlacementInfo currentPlacement;
   private AutoWeb.PlacementType activePlacementType;
   private Entity target;
   private BlockPos targetBlockPos;
   private int waitTicks;
   private int retrieveTicks;
   private int retrieveAttempts;
   private int placementAttempts;
   private int lavaReapplyCount;
   private int sourceWaitCount;
   private boolean lavaPlaced;
   private boolean retriedLavaPlace;
   private int previousSlot = -1;
   private BlockPos retrieveSourcePos;

   public AutoWeb() {
      super(Category.Player, "AutoWeb");
   }

   @Override
   public void onEnable() {
      this.resetState();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetState();
      super.onDisable();
   }

   private void resetState() {
      this.placementPhase = AutoWeb.Phase.IDLE;
      this.currentPlacement = null;
      this.activePlacementType = null;
      this.target = null;
      this.targetBlockPos = null;
      this.waitTicks = 0;
      this.retrieveTicks = 0;
      this.retrieveAttempts = 0;
      this.placementAttempts = 0;
      this.lavaReapplyCount = 0;
      this.sourceWaitCount = 0;
      this.lavaPlaced = false;
      this.retriedLavaPlace = false;
      this.retrieveSourcePos = null;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.world != null && mc.player != null && mc.interactionManager != null) {
            switch (this.placementPhase) {
               case IDLE:
                  this.tickSearch();
               case PLACE:
               default:
                  break;
               case LAVA_RETRIEVE:
                  this.doLavaRetrieve();
            }
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (this.placementPhase != AutoWeb.Phase.IDLE) {
         Rotation rot;
         if (this.placementPhase == AutoWeb.Phase.LAVA_RETRIEVE) {
            if (this.currentPlacement == null) {
               return;
            }

            if (this.waitTicks > 0) {
               return;
            }

            Vec3d aimPoint = this.retrieveSourcePos != null ? this.retrieveSourcePos.toCenterPos() : this.currentPlacement.aim;
            rot = RotationUtils.calculate(mc.player.getEyePos(), aimPoint);
         } else {
            if (this.currentPlacement == null) {
               return;
            }

            rot = RotationUtils.calculate(mc.player.getEyePos(), this.currentPlacement.aim);
         }

         rot.pitch = rot.pitch - RandomUtils.generateRandomFloat(0.001F, 0.003F);
         rot.yaw = rot.yaw - RandomUtils.generateRandomFloat(1.0E-4F, 3.0E-4F);
         if (rot.pitch > 90.0F) {
            rot.pitch = 90.0F;
         }

         if (rot.pitch < -90.0F) {
            rot.pitch = -90.0F;
         }

         RotationUtils.setRotation(rot, this.rotationPriority(), MoveFixMode.Silent);
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.placementPhase == AutoWeb.Phase.PLACE && this.currentPlacement != null) {
         if (RotationUtils.accepted(this.rotationPriority())) {
            if (!this.isRotationOnTarget(this.currentPlacement.aim)) {
               if (++this.placementAttempts >= 8) {
                  this.resetState();
               }
            } else {
               this.executePlacement();
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (this.renderSetting.getValue()) {
         if (this.targetBlockPos != null || this.currentPlacement != null) {
            Color tint = this.activePlacementType == AutoWeb.PlacementType.LAVA ? new Color(255, 100, 0) : new Color(255, 255, 255);
            if (this.targetBlockPos != null) {
               Box box = new Box(this.targetBlockPos);
               Render3D.boxSides(box, new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 64));
               Render3D.boxLines(box, new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 190));
            }

            if (this.currentPlacement != null) {
               this.renderFaceHighlight(this.currentPlacement.support, this.currentPlacement.face, new Color(255, 230, 0));
            }
         }
      }
   }

   private void renderFaceHighlight(BlockPos pos, Direction face, Color color) {
      Box faceBox = this.getFaceAabb(pos, face);
      Render3D.boxSides(faceBox, new Color(color.getRed(), color.getGreen(), color.getBlue(), 115));
      Render3D.boxLines(faceBox, new Color(color.getRed(), color.getGreen(), color.getBlue(), 242));
   }

   private Box getFaceAabb(BlockPos pos, Direction face) {
      double minX = pos.getX();
      double minY = pos.getY();
      double minZ = pos.getZ();
      double t = 0.002;

      return switch (face) {
         case UP -> new Box(minX, minY + 1.0 - t, minZ, minX + 1.0, minY + 1.0 + t, minZ + 1.0);
         case DOWN -> new Box(minX, minY - t, minZ, minX + 1.0, minY + t, minZ + 1.0);
         case EAST -> new Box(minX + 1.0 - t, minY, minZ, minX + 1.0 + t, minY + 1.0, minZ + 1.0);
         case WEST -> new Box(minX - t, minY, minZ, minX + t, minY + 1.0, minZ + 1.0);
         case SOUTH -> new Box(minX, minY, minZ + 1.0 - t, minX + 1.0, minY + 1.0, minZ + 1.0 + t);
         case NORTH -> new Box(minX, minY, minZ - t, minX + 1.0, minY + 1.0, minZ + t);
         default -> throw new MatchException(null, null);
      };
   }

   private void tickSearch() {
      if (this.findCobwebSlot() != -1 || this.lavaSetting.getValue() && this.findLavaBucketSlot() != -1) {
         if (this.placementTimer.hasPassed(this.delaySetting.getValue().longValue() * 50L)) {
            Optional<AutoWeb.PlacementTarget> targetOpt = this.findPlacementTarget();
            if (!targetOpt.isEmpty()) {
               AutoWeb.PlacementTarget pt = targetOpt.get();
               this.target = pt.target;
               this.currentPlacement = pt.placement;
               this.activePlacementType = pt.type;
               this.targetBlockPos = pt.placement.place;
               this.placementAttempts = 0;
               this.lavaReapplyCount = 0;
               this.placementPhase = AutoWeb.Phase.PLACE;
            }
         }
      }
   }

   private void executePlacement() {
      if (!this.isPlacementValid()) {
         this.resetState();
      } else if (!this.doPlacement()) {
         this.resetState();
      } else if (this.isGroundWebMode()
         || this.activePlacementType == AutoWeb.PlacementType.WEB_EXIT_PREDICT
         || this.activePlacementType == AutoWeb.PlacementType.BLOCK) {
         this.placementTimer.reset();
         this.resetState();
      } else if (this.activePlacementType == AutoWeb.PlacementType.LAVA) {
         this.placementTimer.reset();
         this.waitTicks = 1;
         this.retrieveTicks = 0;
         this.retrieveAttempts = 0;
         this.placementAttempts = 0;
         this.sourceWaitCount = 0;
         this.lavaPlaced = false;
         this.retriedLavaPlace = false;
         this.retrieveSourcePos = null;
         this.placementPhase = AutoWeb.Phase.LAVA_RETRIEVE;
      } else {
         if (!this.tryChainLava()) {
            this.placementTimer.reset();
            this.resetState();
         } else {
            this.placementPhase = AutoWeb.Phase.PLACE;
         }
      }
   }

   private boolean doPlacement() {
      int slot;
      if (this.activePlacementType == AutoWeb.PlacementType.LAVA) {
         slot = this.findLavaBucketSlot();
      } else if (this.activePlacementType == AutoWeb.PlacementType.BLOCK) {
         slot = this.findBlockSlot();
      } else {
         slot = this.findCobwebSlot();
      }

      if (slot == -1) {
         return false;
      } else {
         this.previousSlot = mc.player.getInventory().selectedSlot;
         mc.player.getInventory().selectedSlot = slot;
         boolean success;
         if (this.activePlacementType == AutoWeb.PlacementType.LAVA) {
            ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            success = result.isAccepted();
            if (success) {
               mc.player.swingHand(Hand.MAIN_HAND);
            }
         } else {
            BlockHitResult hit = new BlockHitResult(this.currentPlacement.aim, this.currentPlacement.face, this.currentPlacement.support, false);
            success = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit).isAccepted();
            if (success) {
               mc.player.swingHand(Hand.MAIN_HAND);
            }
         }

         mc.player.getInventory().selectedSlot = this.previousSlot;
         return success;
      }
   }

   private Optional<AutoWeb.PlacementTarget> findPlacementTarget() {
      if (mc.world != null && mc.player != null) {
         float range = this.rangeSetting.getValue().floatValue();
         List<Entity> candidates = StreamSupport.<Entity>stream(mc.world.getEntities().spliterator(), false)
            .filter(e -> e instanceof PlayerEntity && e != mc.player)
            .filter(e -> e.distanceTo(mc.player) <= range)
            .filter(e -> TargetUtils.isTarget(e, true, true))
            .sorted(Comparator.comparingDouble(e -> e.distanceTo(mc.player)))
            .toList();
         if (this.killAuraSyncSetting.getValue()) {
            KillAura killAura = Southside.INSTANCE.getModuleManager().get(KillAura.class);
            Entity kaTarget = killAura.isEnabled() ? KillAura.target : null;
            if (kaTarget != null
               && kaTarget.isAlive()
               && !kaTarget.isRemoved()
               && TargetUtils.isTarget(kaTarget, true, true)
               && kaTarget.distanceTo(mc.player) <= range) {
               candidates = List.of(kaTarget);
            }
         }

         int cobwebSlot = this.findCobwebSlot();
         int blockSlot = this.blockPlaceSetting.getValue() ? this.findBlockSlot() : -1;
         int lavaSlot = this.lavaSetting.getValue() ? this.findLavaBucketSlot() : -1;

         for (Entity entity : candidates) {
            boolean inCobweb = this.isEntityInCobweb(entity);
            if (inCobweb) {
               Optional<AutoWeb.PlacementTarget> webExit = this.findWebExitPlacement(entity, cobwebSlot);
               if (webExit.isPresent()) {
                  return webExit;
               }
            }

            if (!inCobweb && this.groundWebSetting.getValue() && cobwebSlot != -1) {
               Optional<AutoWeb.PlacementInfo> groundWeb = this.findGroundWebPlacement(entity);
               if (groundWeb.isPresent()) {
                  this.debugLog("Ground web " + entity.getName().getString());
                  return Optional.of(new AutoWeb.PlacementTarget(entity, groundWeb.get(), AutoWeb.PlacementType.GROUND_WEB));
               }
            }

            if (!inCobweb) {
               Optional<AutoWeb.PlacementTarget> placement = this.findCobwebPlacement(entity, cobwebSlot, blockSlot);
               if (placement.isPresent()) {
                  return placement;
               }
            }

            if (lavaSlot != -1 && this.shouldPlaceLava(entity)) {
               Optional<AutoWeb.PlacementInfo> lavaPlace = this.findLavaSourcePlacement(entity);
               if (lavaPlace.isPresent()) {
                  this.debugLog("Lava " + entity.getName().getString());
                  return Optional.of(new AutoWeb.PlacementTarget(entity, lavaPlace.get(), AutoWeb.PlacementType.LAVA));
               }
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   private Optional<AutoWeb.PlacementTarget> findCobwebPlacement(Entity entity, int cobwebSlot, int blockSlot) {
      boolean canPlaceWeb = cobwebSlot != -1;
      boolean canPlaceBlock = blockSlot != -1;
      if (!canPlaceWeb && !canPlaceBlock) {
         return Optional.empty();
      } else {
         for (BlockPos placePos : this.getCobwebPositions(entity)) {
            if (mc.world.getBlockState(placePos).isReplaceable() && !this.intersectsSelf(placePos)) {
               boolean overlapsTarget = this.overlapsEntity(placePos, entity);
               if (!overlapsTarget && canPlaceBlock) {
                  Optional<AutoWeb.PlacementInfo> blockPlace = this.findPlacementAtAnyFace(entity, placePos);
                  if (blockPlace.isPresent()) {
                     this.debugLog("Block " + entity.getName().getString());
                     return Optional.of(new AutoWeb.PlacementTarget(entity, blockPlace.get(), AutoWeb.PlacementType.BLOCK));
                  }
               }

               if (overlapsTarget && canPlaceWeb) {
                  Optional<AutoWeb.PlacementInfo> webPlace = this.findPlacementAtAnyFace(entity, placePos);
                  if (webPlace.isPresent()) {
                     this.debugLog("Cobweb " + entity.getName().getString());
                     return Optional.of(new AutoWeb.PlacementTarget(entity, webPlace.get(), AutoWeb.PlacementType.COBWEB));
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   private Optional<AutoWeb.PlacementInfo> findPlacementAtAnyFace(Entity entity, BlockPos placePos) {
      Direction nearest = entity != null
         ? Direction.getFacing(entity.getX() - mc.player.getX(), 0.0, entity.getZ() - mc.player.getZ())
         : Direction.NORTH;
      List<Direction> directions = new ArrayList<>();
      directions.add(nearest);

      for (Direction d : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
         if (d != nearest) {
            directions.add(d);
         }
      }

      directions.add(Direction.DOWN);
      directions.add(Direction.UP);

      for (Direction dir : directions) {
         BlockPos support = placePos.offset(dir);
         Direction face = dir.getOpposite();
         Optional<Vec3d> hit = this.findFaceHitPoint(support, face);
         if (hit.isPresent()) {
            return Optional.of(new AutoWeb.PlacementInfo(support, face, hit.get(), placePos));
         }
      }

      return Optional.empty();
   }

   private boolean overlapsEntity(BlockPos pos, Entity entity) {
      Box posBox = new Box(pos);
      if (entity.getBoundingBox().intersects(posBox)) {
         return true;
      } else {
         Vec3d movement = this.getEntityMovement(entity);

         for (int t = 1; t <= 3; t++) {
            Box predicted = entity.getBoundingBox().offset(movement.x * t, 0.0, movement.z * t);
            if (predicted.intersects(posBox)) {
               return true;
            }
         }

         return false;
      }
   }

   private List<BlockPos> getCobwebPositions(Entity entity) {
      List<BlockPos> list = new ArrayList<>();
      list.add(BlockPos.ofFloored(entity.getX(), entity.getY() + 1.0, entity.getZ()));
      Box box = entity.getBoundingBox();
      int minX = (int)Math.floor(box.minX - 0.45);
      int maxX = (int)Math.floor(box.maxX + 0.45);
      int minZ = (int)Math.floor(box.minZ - 0.45);
      int maxZ = (int)Math.floor(box.maxZ + 0.45);
      int headY = (int)Math.floor(entity.getY() + 1.0);
      int topY = (int)Math.floor(box.maxY - 0.05);

      for (int y = Math.min(headY, topY); y <= Math.max(headY, topY); y++) {
         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (!list.contains(pos)) {
                  list.add(pos);
               }
            }
         }
      }

      return list;
   }

   private Optional<AutoWeb.PlacementTarget> findWebExitPlacement(Entity entity, int cobwebSlot) {
      if (cobwebSlot == -1) {
         return Optional.empty();
      } else {
         Vec3d movement = this.getEntityMovement(entity);
         if (movement.x * movement.x + movement.z * movement.z < 4.0E-4) {
            return Optional.empty();
         } else {
            for (int tickAhead = 2; tickAhead <= 3; tickAhead++) {
               Box predicted = entity.getBoundingBox().offset(movement.x * tickAhead, 0.0, movement.z * tickAhead);
               if (!this.isAllCobweb(predicted)) {
                  for (AutoWeb.PredictionTick tick : this.getPositionsForAabb(predicted, tickAhead)) {
                     Optional<AutoWeb.PlacementInfo> place = this.findWebExitPlacementForTick(entity, tick);
                     if (place.isPresent()) {
                        this.debugLog("Web exit predict ticks=" + tick.ticks);
                        return Optional.of(new AutoWeb.PlacementTarget(entity, place.get(), AutoWeb.PlacementType.WEB_EXIT_PREDICT));
                     }
                  }
               }
            }

            return Optional.empty();
         }
      }
   }

   private Optional<AutoWeb.PlacementInfo> findWebExitPlacementForTick(Entity entity, AutoWeb.PredictionTick tick) {
      if (mc.world.getBlockState(tick.headPos).isOf(Blocks.COBWEB)) {
         return Optional.empty();
      } else if (!mc.world.getBlockState(tick.headPos).isReplaceable()) {
         return Optional.empty();
      } else if (this.intersectsSelf(tick.headPos)) {
         return Optional.empty();
      } else {
         Direction nearest = Direction.getFacing(
            entity.getX() - mc.player.getX(), 0.0, entity.getZ() - mc.player.getZ()
         );
         List<Direction> directions = new ArrayList<>();
         directions.add(nearest);

         for (Direction dir : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
            if (dir != nearest) {
               directions.add(dir);
            }
         }

         directions.add(Direction.DOWN);
         directions.add(Direction.UP);

         for (Direction dirx : directions) {
            BlockPos support = tick.headPos.offset(dirx);
            Direction face = dirx.getOpposite();
            Optional<Vec3d> hit = this.findFaceHitPoint(support, face);
            if (hit.isPresent()) {
               return Optional.of(new AutoWeb.PlacementInfo(support, face, hit.get(), tick.headPos));
            }
         }

         return Optional.empty();
      }
   }

   private List<AutoWeb.PredictionTick> getPositionsForAabb(Box aabb, int tickAhead) {
      List<AutoWeb.PredictionTick> list = new ArrayList<>();
      int minX = (int)Math.floor(aabb.minX);
      int maxX = (int)Math.floor(aabb.maxX);
      int minY = (int)Math.floor(aabb.minY);
      int maxY = (int)Math.floor(aabb.maxY);
      int minZ = (int)Math.floor(aabb.minZ);
      int maxZ = (int)Math.floor(aabb.maxZ);
      int footY = (int)Math.floor(aabb.minY + 0.001);

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (aabb.intersects(new Box(pos)) && !mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
                  int chosenY = y <= footY ? footY : y - 1;
                  AutoWeb.PredictionTick tick = new AutoWeb.PredictionTick(tickAhead, new BlockPos(x, chosenY, z), new BlockPos(x, chosenY + 1, z));
                  if (list.stream().noneMatch(t -> t.footPos.equals(tick.footPos))) {
                     list.add(tick);
                  }
               }
            }
         }
      }

      list.sort(
         Comparator.comparingDouble(
            t -> t.footPos.toCenterPos().squaredDistanceTo((aabb.minX + aabb.maxX) * 0.5, footY + 0.5, (aabb.minZ + aabb.maxZ) * 0.5)
         )
      );
      return list;
   }

   private boolean tryChainLava() {
      if (this.target == null || !this.lavaSetting.getValue() || this.findLavaBucketSlot() == -1) {
         return false;
      } else if (!this.shouldPlaceLava(this.target)) {
         return false;
      } else {
         Optional<AutoWeb.PlacementInfo> placement = this.findLavaSourcePlacement(this.target);
         if (placement.isEmpty()) {
            return false;
         } else {
            this.lavaReapplyCount = 0;
            this.currentPlacement = placement.get();
            this.activePlacementType = AutoWeb.PlacementType.LAVA;
            this.targetBlockPos = placement.get().place;
            this.retrieveSourcePos = null;
            return true;
         }
      }
   }

   private boolean isGroundWebMode() {
      return this.activePlacementType == AutoWeb.PlacementType.GROUND_WEB;
   }

   private boolean shouldPlaceLava(Entity entity) {
      if (!this.lavaSetting.getValue()) {
         return false;
      } else if (entity.isOnFire()) {
         return false;
      } else {
         return !this.isEntityInCobweb(entity) && !this.isEntityGrounded(entity) ? false : this.isEntityInCobweb(entity) || !this.isEntityMoving(entity);
      }
   }

   private boolean isEntityMoving(Entity entity) {
      Vec3d movement = this.getEntityMovement(entity);
      return movement.x * movement.x + movement.z * movement.z >= 0.01;
   }

   private Vec3d getEntityMovement(Entity entity) {
      return new Vec3d(entity.getX() - entity.prevX, 0.0, entity.getZ() - entity.prevZ);
   }

   private boolean isEntityGrounded(Entity entity) {
      double yFrac = entity.getY() - Math.floor(entity.getY());
      double yDist = Math.min(yFrac, 1.0 - yFrac);
      return entity.isOnGround() || yDist < 0.08 && Math.abs(entity.getVelocity().y) < 0.12;
   }

   private Optional<AutoWeb.PlacementInfo> findLavaSourcePlacement(Entity entity) {
      return this.findLavaGroundPlacement(entity.getX(), entity.getY(), entity.getZ());
   }

   private Optional<AutoWeb.PlacementInfo> findLavaGroundPlacement(double x, double y, double z) {
      Vec3d start = new Vec3d(x, y + 0.2, z);
      Vec3d end = new Vec3d(x, y - 2.2, z);
      BlockHitResult hit = mc.world.raycast(new RaycastContext(start, end, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
      if (hit.getType() != net.minecraft.util.hit.HitResult.Type.BLOCK) {
         return Optional.empty();
      } else if (hit.getSide() != Direction.UP) {
         return Optional.empty();
      } else {
         BlockPos supportPos = hit.getBlockPos();

         for (Vec3d candidate : this.getAimCandidates(supportPos, x, z)) {
            if (!(mc.player.getEyePos().squaredDistanceTo(candidate) > 25.0)) {
               Vec3d offset = this.offsetTowardsFace(candidate, Direction.UP);
               BlockHitResult verify = mc.world
                  .raycast(new RaycastContext(mc.player.getEyePos(), offset, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
               if (verify.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK && verify.getSide() == Direction.UP && verify.getBlockPos().equals(supportPos)
                  )
                {
                  BlockPos placePos = supportPos.offset(Direction.UP);
                  if (!this.isWater(placePos)
                     && mc.world.getBlockState(placePos).isReplaceable()
                     && !this.intersectsSelf(placePos)
                     && mc.world.getFluidState(placePos).isEmpty()
                     && this.isValidSupport(supportPos)) {
                     return Optional.of(new AutoWeb.PlacementInfo(supportPos, Direction.UP, verify.getPos(), placePos));
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   private List<Vec3d> getAimCandidates(BlockPos pos, double targetX, double targetZ) {
      double y = pos.getY() + 1.0;
      List<Vec3d> list = new ArrayList<>();
      list.add(new Vec3d(pos.getX() + 0.5, y, pos.getZ() + 0.5));
      list.add(
         new Vec3d(
            clampValue(targetX, pos.getX() + 0.25, pos.getX() + 0.75),
            y,
            clampValue(targetZ, pos.getZ() + 0.25, pos.getZ() + 0.75)
         )
      );
      list.add(new Vec3d(pos.getX() + 0.35, y, pos.getZ() + 0.35));
      list.add(new Vec3d(pos.getX() + 0.65, y, pos.getZ() + 0.35));
      list.add(new Vec3d(pos.getX() + 0.35, y, pos.getZ() + 0.65));
      list.add(new Vec3d(pos.getX() + 0.65, y, pos.getZ() + 0.65));
      return list;
   }

   private static double clampValue(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private Optional<AutoWeb.PlacementInfo> findGroundWebPlacement(Entity entity) {
      if (!this.canPlaceGroundWeb(entity)) {
         return Optional.empty();
      } else {
         BlockPos foot = BlockPos.ofFloored(entity.getX(), entity.getY(), entity.getZ());
         return !this.isBlockReplaceable(foot) ? Optional.empty() : this.findPlacementForPos(foot.down(), foot);
      }
   }

   private boolean canPlaceGroundWeb(Entity entity) {
      return !this.isEntityGrounded(entity) ? false : !this.isEntityMoving(entity);
   }

   private Optional<AutoWeb.PlacementInfo> findPlacementForPos(BlockPos supportPos, BlockPos placePos) {
      if (!this.isValidSupport(supportPos)) {
         return Optional.empty();
      } else {
         for (Vec3d candidate : this.getAimCandidates(supportPos, placePos.getX() + 0.5, placePos.getZ() + 0.5)) {
            if (!(mc.player.getEyePos().squaredDistanceTo(candidate) > 25.0)) {
               Vec3d offset = this.offsetTowardsFace(candidate, Direction.UP);
               BlockHitResult hit = mc.world
                  .raycast(new RaycastContext(mc.player.getEyePos(), offset, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
               if (hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK && hit.getBlockPos().equals(supportPos) && hit.getSide() == Direction.UP) {
                  return Optional.of(new AutoWeb.PlacementInfo(supportPos, Direction.UP, hit.getPos(), placePos));
               }
            }
         }

         return Optional.empty();
      }
   }

   private boolean isBlockReplaceable(BlockPos pos) {
      return mc.world.getBlockState(pos).isReplaceable() && mc.world.getFluidState(pos).isEmpty() && !this.intersectsSelf(pos);
   }

   private Optional<Vec3d> findFaceHitPoint(BlockPos pos, Direction face) {
      for (Vec3d point : this.getFacePoints(pos, face)) {
         if (!(mc.player.getEyePos().squaredDistanceTo(point) > 25.0)) {
            BlockHitResult hit = mc.world
               .raycast(
                  new RaycastContext(mc.player.getEyePos(), this.offsetTowardsFace(point, face), ShapeType.OUTLINE, FluidHandling.NONE, mc.player)
               );
            if (hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK && hit.getBlockPos().equals(pos) && hit.getSide() == face) {
               return Optional.of(hit.getPos());
            }
         }
      }

      return Optional.empty();
   }

   private Vec3d offsetTowardsFace(Vec3d point, Direction face) {
      double step = 0.01;
      return point.add(-face.getOffsetX() * step, -face.getOffsetY() * step, -face.getOffsetZ() * step);
   }

   private List<Vec3d> getFacePoints(BlockPos pos, Direction face) {
      List<Vec3d> list = new ArrayList<>();
      Box faceBox = this.getFaceBoundingBox(pos, face);
      if (faceBox == null) {
         return list;
      } else {
         double[] fractions = new double[]{0.5, 0.25, 0.75};

         for (double u : fractions) {
            for (double v : fractions) {
               Vec3d point = switch (face) {
                  case UP -> new Vec3d(
                     pos.getX() + lerp(faceBox.minX, faceBox.maxX, u),
                     pos.getY() + faceBox.maxY,
                     pos.getZ() + lerp(faceBox.minZ, faceBox.maxZ, v)
                  );
                  case DOWN -> new Vec3d(
                     pos.getX() + lerp(faceBox.minX, faceBox.maxX, u),
                     pos.getY() + faceBox.minY,
                     pos.getZ() + lerp(faceBox.minZ, faceBox.maxZ, v)
                  );
                  case EAST -> new Vec3d(
                     pos.getX() + faceBox.maxX,
                     pos.getY() + lerp(faceBox.minY, faceBox.maxY, u),
                     pos.getZ() + lerp(faceBox.minZ, faceBox.maxZ, v)
                  );
                  case WEST -> new Vec3d(
                     pos.getX() + faceBox.minX,
                     pos.getY() + lerp(faceBox.minY, faceBox.maxY, u),
                     pos.getZ() + lerp(faceBox.minZ, faceBox.maxZ, v)
                  );
                  case SOUTH -> new Vec3d(
                     pos.getX() + lerp(faceBox.minX, faceBox.maxX, u),
                     pos.getY() + lerp(faceBox.minY, faceBox.maxY, v),
                     pos.getZ() + faceBox.maxZ
                  );
                  case NORTH -> new Vec3d(
                     pos.getX() + lerp(faceBox.minX, faceBox.maxX, u),
                     pos.getY() + lerp(faceBox.minY, faceBox.maxY, v),
                     pos.getZ() + faceBox.minZ
                  );
                  default -> throw new MatchException(null, null);
               };
               if (list.stream().noneMatch(existing -> existing.squaredDistanceTo(point) < 1.0E-6)) {
                  list.add(point);
               }
            }
         }

         return list;
      }
   }

   private Box getFaceBoundingBox(BlockPos pos, Direction face) {
      VoxelShape shape = this.getBlockShape(pos);
      if (shape.isEmpty()) {
         return null;
      } else {
         Box best = null;
         double bestValue = face.getDirection() == AxisDirection.POSITIVE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

         for (Box box : shape.getBoundingBoxes()) {
            double faceCoord = switch (face) {
               case UP -> box.maxY;
               case DOWN -> box.minY;
               case EAST -> box.maxX;
               case WEST -> box.minX;
               case SOUTH -> box.maxZ;
               case NORTH -> box.minZ;
               default -> throw new MatchException(null, null);
            };
            boolean better = face.getDirection() == AxisDirection.POSITIVE ? faceCoord > bestValue : faceCoord < bestValue;
            if (better) {
               bestValue = faceCoord;
               best = box;
            }
         }

         return best;
      }
   }

   private static double lerp(double a, double b, double t) {
      return a + (b - a) * t;
   }

   private VoxelShape getBlockShape(BlockPos pos) {
      BlockState state = mc.world.getBlockState(pos);
      VoxelShape shape = state.getOutlineShape(mc.world, pos);
      return !shape.isEmpty() ? shape : state.getCollisionShape(mc.world, pos);
   }

   private boolean isValidSupport(BlockPos pos) {
      BlockState state = mc.world.getBlockState(pos);
      return !state.isReplaceable() && !this.getBlockShape(pos).isEmpty();
   }

   private boolean isPlacementValid() {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         return false;
      } else if (this.target == null || !this.target.isAlive() || this.target.isRemoved()) {
         return false;
      } else if (this.currentPlacement == null) {
         return false;
      } else if (this.activePlacementType == AutoWeb.PlacementType.LAVA) {
         if (!this.shouldPlaceLava(this.target)) {
            return false;
         } else if (this.intersectsSelf(this.currentPlacement.place)) {
            return false;
         } else if (this.isWater(this.currentPlacement.place)) {
            return false;
         } else if (!mc.world.getBlockState(this.currentPlacement.place).isReplaceable()) {
            return false;
         } else {
            return !mc.world.getFluidState(this.currentPlacement.place).isEmpty() ? false : this.isValidSupport(this.currentPlacement.support);
         }
      } else if (this.activePlacementType != AutoWeb.PlacementType.GROUND_WEB
         && this.activePlacementType != AutoWeb.PlacementType.WEB_EXIT_PREDICT
         && this.activePlacementType != AutoWeb.PlacementType.BLOCK
         && this.isEntityInCobweb(this.target)) {
         return false;
      } else if (this.intersectsSelf(this.currentPlacement.place)) {
         return false;
      } else if (!mc.world.getBlockState(this.currentPlacement.place).isReplaceable()) {
         return false;
      } else {
         return !mc.world.getFluidState(this.currentPlacement.place).isEmpty() ? false : this.isValidSupport(this.currentPlacement.support);
      }
   }

   private boolean isRotationOnTarget(Vec3d aimPoint) {
      if (RotationUtils.getRotation() != null && mc.player != null) {
         Rotation current = RotationUtils.getRotationSafely();
         Rotation wanted = RotationUtils.calculate(mc.player.getEyePos(), aimPoint);
         return Math.abs(RotationUtils.getAngleDifference(current.yaw, wanted.yaw)) < 12.0F
            && Math.abs(RotationUtils.getAngleDifference(current.pitch, wanted.pitch)) < 12.0F;
      } else {
         return false;
      }
   }

   private void doLavaRetrieve() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && this.currentPlacement != null) {
         this.retrieveTicks++;
         if (this.waitTicks > 0) {
            this.waitTicks--;
         } else {
            BlockPos expectedSource = this.currentPlacement.place;
            BlockPos actualSource = this.findNearbyLavaSource(expectedSource);
            BlockPos probe = actualSource != null ? actualSource : expectedSource;
            boolean flowingLava = this.hasNearbyFlowingLava(probe);
            boolean targetHasWaterBucket = this.targetHasWaterBucket(this.target);
            boolean timedOut = this.retrieveTicks >= this.getLavaTickDelay();
            boolean readyToRetrieve = targetHasWaterBucket || flowingLava || timedOut;
            boolean shouldAct = !this.lavaWaitWaterBucketSetting.getValue() || readyToRetrieve;
            if (actualSource != null) {
               this.lavaPlaced = true;
               this.sourceWaitCount = 0;
               this.targetBlockPos = actualSource;
               this.retrieveSourcePos = actualSource;
               if (!shouldAct) {
                  this.waitTicks = 1;
               } else if (!this.isRotationOnTarget(actualSource.toCenterPos())) {
                  this.waitTicks = 1;
               } else {
                  int emptyBucketSlot = this.findEmptyBucketSlot();
                  if (emptyBucketSlot == -1) {
                     if (++this.retrieveAttempts >= 8) {
                        this.resetState();
                     } else {
                        this.waitTicks = 1;
                     }
                  } else {
                     this.previousSlot = mc.player.getInventory().selectedSlot;
                     mc.player.getInventory().selectedSlot = emptyBucketSlot;
                     ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                     if (result.isAccepted()) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                     }

                     mc.player.getInventory().selectedSlot = this.previousSlot;
                     if (++this.retrieveAttempts >= 8) {
                        this.resetState();
                     } else {
                        this.waitTicks = 2;
                     }
                  }
               }
            } else if (this.lavaPlaced) {
               if (!this.tryReapplyLava()) {
                  this.resetState();
               }
            } else if (++this.sourceWaitCount >= 8) {
               this.resetState();
            } else {
               if (!this.retriedLavaPlace && this.sourceWaitCount >= 2 && this.findLavaBucketSlot() != -1 && this.isPlacementValid()) {
                  this.retriedLavaPlace = true;
                  if (!this.isRotationOnTarget(this.currentPlacement.aim)) {
                     this.waitTicks = 1;
                     return;
                  }

                  this.doPlacement();
               }

               this.waitTicks = 1;
            }
         }
      } else {
         this.resetState();
      }
   }

   private boolean isLavaSource(BlockPos pos) {
      return mc.world.getBlockState(pos).isOf(Blocks.LAVA) && mc.world.getFluidState(pos).isStill();
   }

   private boolean hasNearbyFlowingLava(BlockPos center) {
      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
               if (this.isFlowingLava(center.add(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean tryReapplyLava() {
      if (this.target == null || this.target.isRemoved() || !this.target.isAlive()) {
         return false;
      } else if (this.target.isOnFire()) {
         return false;
      } else if (this.lavaReapplyCount >= 1) {
         return false;
      } else if (this.findLavaBucketSlot() == -1) {
         return false;
      } else if (!this.shouldPlaceLava(this.target)) {
         return false;
      } else {
         Optional<AutoWeb.PlacementInfo> placement = this.findLavaSourcePlacement(this.target);
         if (placement.isEmpty()) {
            return false;
         } else {
            this.lavaReapplyCount++;
            this.currentPlacement = placement.get();
            this.activePlacementType = AutoWeb.PlacementType.LAVA;
            this.targetBlockPos = placement.get().place;
            this.retrieveSourcePos = null;
            this.placementPhase = AutoWeb.Phase.PLACE;
            return true;
         }
      }
   }

   private int getLavaTickDelay() {
      return mc.world == null ? 9 : (mc.world.getDimension().ultrawarm() ? 10 : 30);
   }

   private boolean isFlowingLava(BlockPos pos) {
      FluidState state = mc.world.getFluidState(pos);
      Fluid fluid = state.getFluid();
      return (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) && !state.isStill();
   }

   private boolean targetHasWaterBucket(Entity entity) {
      return !(entity instanceof PlayerEntity player)
         ? false
         : player.getMainHandStack().isOf(Items.WATER_BUCKET) || player.getOffHandStack().isOf(Items.WATER_BUCKET);
   }

   private boolean isWater(BlockPos pos) {
      Fluid fluid = mc.world.getFluidState(pos).getFluid();
      return mc.world.getBlockState(pos).isOf(Blocks.WATER) || fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
   }

   private boolean intersectsSelf(BlockPos pos) {
      return mc.player != null && mc.player.getBoundingBox().intersects(new Box(pos));
   }

   private boolean isEntityInCobweb(Entity entity) {
      return entity != null && mc.world != null && this.hasAnyCobweb(entity.getBoundingBox());
   }

   private boolean isAllCobweb(Box box) {
      if (mc.world == null) {
         return false;
      } else {
         boolean any = false;

         for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); x++) {
            for (int y = (int)Math.floor(box.minY); y <= (int)Math.floor(box.maxY); y++) {
               for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); z++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (box.intersects(new Box(pos))) {
                     if (!mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
                        return false;
                     }

                     any = true;
                  }
               }
            }
         }

         return any;
      }
   }

   private boolean hasAnyCobweb(Box box) {
      if (mc.world == null) {
         return false;
      } else {
         for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); x++) {
            for (int y = (int)Math.floor(box.minY); y <= (int)Math.floor(box.maxY); y++) {
               for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); z++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (mc.world.getBlockState(pos).isOf(Blocks.COBWEB) && box.intersects(new Box(pos))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private BlockPos findNearbyLavaSource(BlockPos center) {
      if (this.isLavaSource(center)) {
         return center;
      } else {
         BlockPos best = null;
         double bestDist = Double.POSITIVE_INFINITY;

         for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
               for (int z = -1; z <= 1; z++) {
                  BlockPos pos = center.add(x, y, z);
                  if (this.isLavaSource(pos)) {
                     double dist = center.toCenterPos().squaredDistanceTo(pos.toCenterPos());
                     if (dist < bestDist) {
                        best = pos;
                        bestDist = dist;
                     }
                  }
               }
            }
         }

         return best;
      }
   }

   private void debugLog(String message) {
      if (this.debugSetting.getValue()) {
         ChatUtils.info("[AutoWeb] " + message);
      }
   }

   private int findCobwebSlot() {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && stack.isOf(Items.COBWEB)) {
            return slot;
         }
      }

      return -1;
   }

   private int findBlockSlot() {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && !stack.isOf(Items.COBWEB)) {
            return slot;
         }
      }

      return -1;
   }

   private int findLavaBucketSlot() {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && stack.isOf(Items.LAVA_BUCKET)) {
            return slot;
         }
      }

      return -1;
   }

   private int findEmptyBucketSlot() {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = mc.player.getInventory().getStack(slot);
         if (!stack.isEmpty() && stack.isOf(Items.BUCKET)) {
            return slot;
         }
      }

      return -1;
   }

   @Override
   public int rotationPriority() {
      return 4;
   }

   private static enum Phase {
      IDLE,
      PLACE,
      LAVA_RETRIEVE;
   }

   private record PlacementInfo(BlockPos support, Direction face, Vec3d aim, BlockPos place) {
   }

   private record PlacementTarget(Entity target, AutoWeb.PlacementInfo placement, AutoWeb.PlacementType type) {
   }

   private static enum PlacementType {
      COBWEB,
      WEB_EXIT_PREDICT,
      GROUND_WEB,
      BLOCK,
      LAVA;
   }

   private record PredictionTick(int ticks, BlockPos footPos, BlockPos headPos) {
   }
}
