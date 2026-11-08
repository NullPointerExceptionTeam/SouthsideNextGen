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
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_2350.class_2352;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

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
   private class_1297 target;
   private class_2338 targetBlockPos;
   private int waitTicks;
   private int retrieveTicks;
   private int retrieveAttempts;
   private int placementAttempts;
   private int lavaReapplyCount;
   private int sourceWaitCount;
   private boolean lavaPlaced;
   private boolean retriedLavaPlace;
   private int previousSlot = -1;
   private class_2338 retrieveSourcePos;

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
         if (mc.field_1687 != null && mc.field_1724 != null && mc.field_1761 != null) {
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

            class_243 aimPoint = this.retrieveSourcePos != null ? this.retrieveSourcePos.method_46558() : this.currentPlacement.aim;
            rot = RotationUtils.calculate(mc.field_1724.method_33571(), aimPoint);
         } else {
            if (this.currentPlacement == null) {
               return;
            }

            rot = RotationUtils.calculate(mc.field_1724.method_33571(), this.currentPlacement.aim);
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
               class_238 box = new class_238(this.targetBlockPos);
               Render3D.boxSides(box, new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 64));
               Render3D.boxLines(box, new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 190));
            }

            if (this.currentPlacement != null) {
               this.renderFaceHighlight(this.currentPlacement.support, this.currentPlacement.face, new Color(255, 230, 0));
            }
         }
      }
   }

   private void renderFaceHighlight(class_2338 pos, class_2350 face, Color color) {
      class_238 faceBox = this.getFaceAabb(pos, face);
      Render3D.boxSides(faceBox, new Color(color.getRed(), color.getGreen(), color.getBlue(), 115));
      Render3D.boxLines(faceBox, new Color(color.getRed(), color.getGreen(), color.getBlue(), 242));
   }

   private class_238 getFaceAabb(class_2338 pos, class_2350 face) {
      double minX = pos.method_10263();
      double minY = pos.method_10264();
      double minZ = pos.method_10260();
      double t = 0.002;

      return switch (face) {
         case field_11036 -> new class_238(minX, minY + 1.0 - t, minZ, minX + 1.0, minY + 1.0 + t, minZ + 1.0);
         case field_11033 -> new class_238(minX, minY - t, minZ, minX + 1.0, minY + t, minZ + 1.0);
         case field_11034 -> new class_238(minX + 1.0 - t, minY, minZ, minX + 1.0 + t, minY + 1.0, minZ + 1.0);
         case field_11039 -> new class_238(minX - t, minY, minZ, minX + t, minY + 1.0, minZ + 1.0);
         case field_11035 -> new class_238(minX, minY, minZ + 1.0 - t, minX + 1.0, minY + 1.0, minZ + 1.0 + t);
         case field_11043 -> new class_238(minX, minY, minZ - t, minX + 1.0, minY + 1.0, minZ + t);
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
         this.previousSlot = mc.field_1724.method_31548().field_7545;
         mc.field_1724.method_31548().field_7545 = slot;
         boolean success;
         if (this.activePlacementType == AutoWeb.PlacementType.LAVA) {
            class_1269 result = mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            success = result.method_23665();
            if (success) {
               mc.field_1724.method_6104(class_1268.field_5808);
            }
         } else {
            class_3965 hit = new class_3965(this.currentPlacement.aim, this.currentPlacement.face, this.currentPlacement.support, false);
            success = mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit).method_23665();
            if (success) {
               mc.field_1724.method_6104(class_1268.field_5808);
            }
         }

         mc.field_1724.method_31548().field_7545 = this.previousSlot;
         return success;
      }
   }

   private Optional<AutoWeb.PlacementTarget> findPlacementTarget() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         float range = this.rangeSetting.getValue().floatValue();
         List<class_1297> candidates = StreamSupport.<class_1297>stream(mc.field_1687.method_18112().spliterator(), false)
            .filter(e -> e instanceof class_1657 && e != mc.field_1724)
            .filter(e -> e.method_5739(mc.field_1724) <= range)
            .filter(e -> TargetUtils.isTarget(e, true, true))
            .sorted(Comparator.comparingDouble(e -> e.method_5739(mc.field_1724)))
            .toList();
         if (this.killAuraSyncSetting.getValue()) {
            KillAura killAura = Southside.INSTANCE.getModuleManager().get(KillAura.class);
            class_1297 kaTarget = killAura.isEnabled() ? KillAura.target : null;
            if (kaTarget != null
               && kaTarget.method_5805()
               && !kaTarget.method_31481()
               && TargetUtils.isTarget(kaTarget, true, true)
               && kaTarget.method_5739(mc.field_1724) <= range) {
               candidates = List.of(kaTarget);
            }
         }

         int cobwebSlot = this.findCobwebSlot();
         int blockSlot = this.blockPlaceSetting.getValue() ? this.findBlockSlot() : -1;
         int lavaSlot = this.lavaSetting.getValue() ? this.findLavaBucketSlot() : -1;

         for (class_1297 entity : candidates) {
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
                  this.debugLog("Ground web " + entity.method_5477().getString());
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
                  this.debugLog("Lava " + entity.method_5477().getString());
                  return Optional.of(new AutoWeb.PlacementTarget(entity, lavaPlace.get(), AutoWeb.PlacementType.LAVA));
               }
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   private Optional<AutoWeb.PlacementTarget> findCobwebPlacement(class_1297 entity, int cobwebSlot, int blockSlot) {
      boolean canPlaceWeb = cobwebSlot != -1;
      boolean canPlaceBlock = blockSlot != -1;
      if (!canPlaceWeb && !canPlaceBlock) {
         return Optional.empty();
      } else {
         for (class_2338 placePos : this.getCobwebPositions(entity)) {
            if (mc.field_1687.method_8320(placePos).method_45474() && !this.intersectsSelf(placePos)) {
               boolean overlapsTarget = this.overlapsEntity(placePos, entity);
               if (!overlapsTarget && canPlaceBlock) {
                  Optional<AutoWeb.PlacementInfo> blockPlace = this.findPlacementAtAnyFace(entity, placePos);
                  if (blockPlace.isPresent()) {
                     this.debugLog("Block " + entity.method_5477().getString());
                     return Optional.of(new AutoWeb.PlacementTarget(entity, blockPlace.get(), AutoWeb.PlacementType.BLOCK));
                  }
               }

               if (overlapsTarget && canPlaceWeb) {
                  Optional<AutoWeb.PlacementInfo> webPlace = this.findPlacementAtAnyFace(entity, placePos);
                  if (webPlace.isPresent()) {
                     this.debugLog("Cobweb " + entity.method_5477().getString());
                     return Optional.of(new AutoWeb.PlacementTarget(entity, webPlace.get(), AutoWeb.PlacementType.COBWEB));
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   private Optional<AutoWeb.PlacementInfo> findPlacementAtAnyFace(class_1297 entity, class_2338 placePos) {
      class_2350 nearest = entity != null
         ? class_2350.method_10142(entity.method_23317() - mc.field_1724.method_23317(), 0.0, entity.method_23321() - mc.field_1724.method_23321())
         : class_2350.field_11043;
      List<class_2350> directions = new ArrayList<>();
      directions.add(nearest);

      for (class_2350 d : class_2353.field_11062) {
         if (d != nearest) {
            directions.add(d);
         }
      }

      directions.add(class_2350.field_11033);
      directions.add(class_2350.field_11036);

      for (class_2350 dir : directions) {
         class_2338 support = placePos.method_10093(dir);
         class_2350 face = dir.method_10153();
         Optional<class_243> hit = this.findFaceHitPoint(support, face);
         if (hit.isPresent()) {
            return Optional.of(new AutoWeb.PlacementInfo(support, face, hit.get(), placePos));
         }
      }

      return Optional.empty();
   }

   private boolean overlapsEntity(class_2338 pos, class_1297 entity) {
      class_238 posBox = new class_238(pos);
      if (entity.method_5829().method_994(posBox)) {
         return true;
      } else {
         class_243 movement = this.getEntityMovement(entity);

         for (int t = 1; t <= 3; t++) {
            class_238 predicted = entity.method_5829().method_989(movement.field_1352 * t, 0.0, movement.field_1350 * t);
            if (predicted.method_994(posBox)) {
               return true;
            }
         }

         return false;
      }
   }

   private List<class_2338> getCobwebPositions(class_1297 entity) {
      List<class_2338> list = new ArrayList<>();
      list.add(class_2338.method_49637(entity.method_23317(), entity.method_23318() + 1.0, entity.method_23321()));
      class_238 box = entity.method_5829();
      int minX = (int)Math.floor(box.field_1323 - 0.45);
      int maxX = (int)Math.floor(box.field_1320 + 0.45);
      int minZ = (int)Math.floor(box.field_1321 - 0.45);
      int maxZ = (int)Math.floor(box.field_1324 + 0.45);
      int headY = (int)Math.floor(entity.method_23318() + 1.0);
      int topY = (int)Math.floor(box.field_1325 - 0.05);

      for (int y = Math.min(headY, topY); y <= Math.max(headY, topY); y++) {
         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               class_2338 pos = new class_2338(x, y, z);
               if (!list.contains(pos)) {
                  list.add(pos);
               }
            }
         }
      }

      return list;
   }

   private Optional<AutoWeb.PlacementTarget> findWebExitPlacement(class_1297 entity, int cobwebSlot) {
      if (cobwebSlot == -1) {
         return Optional.empty();
      } else {
         class_243 movement = this.getEntityMovement(entity);
         if (movement.field_1352 * movement.field_1352 + movement.field_1350 * movement.field_1350 < 4.0E-4) {
            return Optional.empty();
         } else {
            for (int tickAhead = 2; tickAhead <= 3; tickAhead++) {
               class_238 predicted = entity.method_5829().method_989(movement.field_1352 * tickAhead, 0.0, movement.field_1350 * tickAhead);
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

   private Optional<AutoWeb.PlacementInfo> findWebExitPlacementForTick(class_1297 entity, AutoWeb.PredictionTick tick) {
      if (mc.field_1687.method_8320(tick.headPos).method_27852(class_2246.field_10343)) {
         return Optional.empty();
      } else if (!mc.field_1687.method_8320(tick.headPos).method_45474()) {
         return Optional.empty();
      } else if (this.intersectsSelf(tick.headPos)) {
         return Optional.empty();
      } else {
         class_2350 nearest = class_2350.method_10142(
            entity.method_23317() - mc.field_1724.method_23317(), 0.0, entity.method_23321() - mc.field_1724.method_23321()
         );
         List<class_2350> directions = new ArrayList<>();
         directions.add(nearest);

         for (class_2350 dir : class_2353.field_11062) {
            if (dir != nearest) {
               directions.add(dir);
            }
         }

         directions.add(class_2350.field_11033);
         directions.add(class_2350.field_11036);

         for (class_2350 dirx : directions) {
            class_2338 support = tick.headPos.method_10093(dirx);
            class_2350 face = dirx.method_10153();
            Optional<class_243> hit = this.findFaceHitPoint(support, face);
            if (hit.isPresent()) {
               return Optional.of(new AutoWeb.PlacementInfo(support, face, hit.get(), tick.headPos));
            }
         }

         return Optional.empty();
      }
   }

   private List<AutoWeb.PredictionTick> getPositionsForAabb(class_238 aabb, int tickAhead) {
      List<AutoWeb.PredictionTick> list = new ArrayList<>();
      int minX = (int)Math.floor(aabb.field_1323);
      int maxX = (int)Math.floor(aabb.field_1320);
      int minY = (int)Math.floor(aabb.field_1322);
      int maxY = (int)Math.floor(aabb.field_1325);
      int minZ = (int)Math.floor(aabb.field_1321);
      int maxZ = (int)Math.floor(aabb.field_1324);
      int footY = (int)Math.floor(aabb.field_1322 + 0.001);

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               class_2338 pos = new class_2338(x, y, z);
               if (aabb.method_994(new class_238(pos)) && !mc.field_1687.method_8320(pos).method_27852(class_2246.field_10343)) {
                  int chosenY = y <= footY ? footY : y - 1;
                  AutoWeb.PredictionTick tick = new AutoWeb.PredictionTick(tickAhead, new class_2338(x, chosenY, z), new class_2338(x, chosenY + 1, z));
                  if (list.stream().noneMatch(t -> t.footPos.equals(tick.footPos))) {
                     list.add(tick);
                  }
               }
            }
         }
      }

      list.sort(
         Comparator.comparingDouble(
            t -> t.footPos.method_46558().method_1028((aabb.field_1323 + aabb.field_1320) * 0.5, footY + 0.5, (aabb.field_1321 + aabb.field_1324) * 0.5)
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

   private boolean shouldPlaceLava(class_1297 entity) {
      if (!this.lavaSetting.getValue()) {
         return false;
      } else if (entity.method_5809()) {
         return false;
      } else {
         return !this.isEntityInCobweb(entity) && !this.isEntityGrounded(entity) ? false : this.isEntityInCobweb(entity) || !this.isEntityMoving(entity);
      }
   }

   private boolean isEntityMoving(class_1297 entity) {
      class_243 movement = this.getEntityMovement(entity);
      return movement.field_1352 * movement.field_1352 + movement.field_1350 * movement.field_1350 >= 0.01;
   }

   private class_243 getEntityMovement(class_1297 entity) {
      return new class_243(entity.method_23317() - entity.field_6014, 0.0, entity.method_23321() - entity.field_5969);
   }

   private boolean isEntityGrounded(class_1297 entity) {
      double yFrac = entity.method_23318() - Math.floor(entity.method_23318());
      double yDist = Math.min(yFrac, 1.0 - yFrac);
      return entity.method_24828() || yDist < 0.08 && Math.abs(entity.method_18798().field_1351) < 0.12;
   }

   private Optional<AutoWeb.PlacementInfo> findLavaSourcePlacement(class_1297 entity) {
      return this.findLavaGroundPlacement(entity.method_23317(), entity.method_23318(), entity.method_23321());
   }

   private Optional<AutoWeb.PlacementInfo> findLavaGroundPlacement(double x, double y, double z) {
      class_243 start = new class_243(x, y + 0.2, z);
      class_243 end = new class_243(x, y - 2.2, z);
      class_3965 hit = mc.field_1687.method_17742(new class_3959(start, end, class_3960.field_17559, class_242.field_1348, mc.field_1724));
      if (hit.method_17783() != class_240.field_1332) {
         return Optional.empty();
      } else if (hit.method_17780() != class_2350.field_11036) {
         return Optional.empty();
      } else {
         class_2338 supportPos = hit.method_17777();

         for (class_243 candidate : this.getAimCandidates(supportPos, x, z)) {
            if (!(mc.field_1724.method_33571().method_1025(candidate) > 25.0)) {
               class_243 offset = this.offsetTowardsFace(candidate, class_2350.field_11036);
               class_3965 verify = mc.field_1687
                  .method_17742(new class_3959(mc.field_1724.method_33571(), offset, class_3960.field_17559, class_242.field_1348, mc.field_1724));
               if (verify.method_17783() == class_240.field_1332 && verify.method_17780() == class_2350.field_11036 && verify.method_17777().equals(supportPos)
                  )
                {
                  class_2338 placePos = supportPos.method_10093(class_2350.field_11036);
                  if (!this.isWater(placePos)
                     && mc.field_1687.method_8320(placePos).method_45474()
                     && !this.intersectsSelf(placePos)
                     && mc.field_1687.method_8316(placePos).method_15769()
                     && this.isValidSupport(supportPos)) {
                     return Optional.of(new AutoWeb.PlacementInfo(supportPos, class_2350.field_11036, verify.method_17784(), placePos));
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   private List<class_243> getAimCandidates(class_2338 pos, double targetX, double targetZ) {
      double y = pos.method_10264() + 1.0;
      List<class_243> list = new ArrayList<>();
      list.add(new class_243(pos.method_10263() + 0.5, y, pos.method_10260() + 0.5));
      list.add(
         new class_243(
            clampValue(targetX, pos.method_10263() + 0.25, pos.method_10263() + 0.75),
            y,
            clampValue(targetZ, pos.method_10260() + 0.25, pos.method_10260() + 0.75)
         )
      );
      list.add(new class_243(pos.method_10263() + 0.35, y, pos.method_10260() + 0.35));
      list.add(new class_243(pos.method_10263() + 0.65, y, pos.method_10260() + 0.35));
      list.add(new class_243(pos.method_10263() + 0.35, y, pos.method_10260() + 0.65));
      list.add(new class_243(pos.method_10263() + 0.65, y, pos.method_10260() + 0.65));
      return list;
   }

   private static double clampValue(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private Optional<AutoWeb.PlacementInfo> findGroundWebPlacement(class_1297 entity) {
      if (!this.canPlaceGroundWeb(entity)) {
         return Optional.empty();
      } else {
         class_2338 foot = class_2338.method_49637(entity.method_23317(), entity.method_23318(), entity.method_23321());
         return !this.isBlockReplaceable(foot) ? Optional.empty() : this.findPlacementForPos(foot.method_10074(), foot);
      }
   }

   private boolean canPlaceGroundWeb(class_1297 entity) {
      return !this.isEntityGrounded(entity) ? false : !this.isEntityMoving(entity);
   }

   private Optional<AutoWeb.PlacementInfo> findPlacementForPos(class_2338 supportPos, class_2338 placePos) {
      if (!this.isValidSupport(supportPos)) {
         return Optional.empty();
      } else {
         for (class_243 candidate : this.getAimCandidates(supportPos, placePos.method_10263() + 0.5, placePos.method_10260() + 0.5)) {
            if (!(mc.field_1724.method_33571().method_1025(candidate) > 25.0)) {
               class_243 offset = this.offsetTowardsFace(candidate, class_2350.field_11036);
               class_3965 hit = mc.field_1687
                  .method_17742(new class_3959(mc.field_1724.method_33571(), offset, class_3960.field_17559, class_242.field_1348, mc.field_1724));
               if (hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(supportPos) && hit.method_17780() == class_2350.field_11036) {
                  return Optional.of(new AutoWeb.PlacementInfo(supportPos, class_2350.field_11036, hit.method_17784(), placePos));
               }
            }
         }

         return Optional.empty();
      }
   }

   private boolean isBlockReplaceable(class_2338 pos) {
      return mc.field_1687.method_8320(pos).method_45474() && mc.field_1687.method_8316(pos).method_15769() && !this.intersectsSelf(pos);
   }

   private Optional<class_243> findFaceHitPoint(class_2338 pos, class_2350 face) {
      for (class_243 point : this.getFacePoints(pos, face)) {
         if (!(mc.field_1724.method_33571().method_1025(point) > 25.0)) {
            class_3965 hit = mc.field_1687
               .method_17742(
                  new class_3959(mc.field_1724.method_33571(), this.offsetTowardsFace(point, face), class_3960.field_17559, class_242.field_1348, mc.field_1724)
               );
            if (hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(pos) && hit.method_17780() == face) {
               return Optional.of(hit.method_17784());
            }
         }
      }

      return Optional.empty();
   }

   private class_243 offsetTowardsFace(class_243 point, class_2350 face) {
      double step = 0.01;
      return point.method_1031(-face.method_10148() * step, -face.method_10164() * step, -face.method_10165() * step);
   }

   private List<class_243> getFacePoints(class_2338 pos, class_2350 face) {
      List<class_243> list = new ArrayList<>();
      class_238 faceBox = this.getFaceBoundingBox(pos, face);
      if (faceBox == null) {
         return list;
      } else {
         double[] fractions = new double[]{0.5, 0.25, 0.75};

         for (double u : fractions) {
            for (double v : fractions) {
               class_243 point = switch (face) {
                  case field_11036 -> new class_243(
                     pos.method_10263() + lerp(faceBox.field_1323, faceBox.field_1320, u),
                     pos.method_10264() + faceBox.field_1325,
                     pos.method_10260() + lerp(faceBox.field_1321, faceBox.field_1324, v)
                  );
                  case field_11033 -> new class_243(
                     pos.method_10263() + lerp(faceBox.field_1323, faceBox.field_1320, u),
                     pos.method_10264() + faceBox.field_1322,
                     pos.method_10260() + lerp(faceBox.field_1321, faceBox.field_1324, v)
                  );
                  case field_11034 -> new class_243(
                     pos.method_10263() + faceBox.field_1320,
                     pos.method_10264() + lerp(faceBox.field_1322, faceBox.field_1325, u),
                     pos.method_10260() + lerp(faceBox.field_1321, faceBox.field_1324, v)
                  );
                  case field_11039 -> new class_243(
                     pos.method_10263() + faceBox.field_1323,
                     pos.method_10264() + lerp(faceBox.field_1322, faceBox.field_1325, u),
                     pos.method_10260() + lerp(faceBox.field_1321, faceBox.field_1324, v)
                  );
                  case field_11035 -> new class_243(
                     pos.method_10263() + lerp(faceBox.field_1323, faceBox.field_1320, u),
                     pos.method_10264() + lerp(faceBox.field_1322, faceBox.field_1325, v),
                     pos.method_10260() + faceBox.field_1324
                  );
                  case field_11043 -> new class_243(
                     pos.method_10263() + lerp(faceBox.field_1323, faceBox.field_1320, u),
                     pos.method_10264() + lerp(faceBox.field_1322, faceBox.field_1325, v),
                     pos.method_10260() + faceBox.field_1321
                  );
                  default -> throw new MatchException(null, null);
               };
               if (list.stream().noneMatch(existing -> existing.method_1025(point) < 1.0E-6)) {
                  list.add(point);
               }
            }
         }

         return list;
      }
   }

   private class_238 getFaceBoundingBox(class_2338 pos, class_2350 face) {
      class_265 shape = this.getBlockShape(pos);
      if (shape.method_1110()) {
         return null;
      } else {
         class_238 best = null;
         double bestValue = face.method_10171() == class_2352.field_11056 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

         for (class_238 box : shape.method_1090()) {
            double faceCoord = switch (face) {
               case field_11036 -> box.field_1325;
               case field_11033 -> box.field_1322;
               case field_11034 -> box.field_1320;
               case field_11039 -> box.field_1323;
               case field_11035 -> box.field_1324;
               case field_11043 -> box.field_1321;
               default -> throw new MatchException(null, null);
            };
            boolean better = face.method_10171() == class_2352.field_11056 ? faceCoord > bestValue : faceCoord < bestValue;
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

   private class_265 getBlockShape(class_2338 pos) {
      class_2680 state = mc.field_1687.method_8320(pos);
      class_265 shape = state.method_26218(mc.field_1687, pos);
      return !shape.method_1110() ? shape : state.method_26220(mc.field_1687, pos);
   }

   private boolean isValidSupport(class_2338 pos) {
      class_2680 state = mc.field_1687.method_8320(pos);
      return !state.method_45474() && !this.getBlockShape(pos).method_1110();
   }

   private boolean isPlacementValid() {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
         return false;
      } else if (this.target == null || !this.target.method_5805() || this.target.method_31481()) {
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
         } else if (!mc.field_1687.method_8320(this.currentPlacement.place).method_45474()) {
            return false;
         } else {
            return !mc.field_1687.method_8316(this.currentPlacement.place).method_15769() ? false : this.isValidSupport(this.currentPlacement.support);
         }
      } else if (this.activePlacementType != AutoWeb.PlacementType.GROUND_WEB
         && this.activePlacementType != AutoWeb.PlacementType.WEB_EXIT_PREDICT
         && this.activePlacementType != AutoWeb.PlacementType.BLOCK
         && this.isEntityInCobweb(this.target)) {
         return false;
      } else if (this.intersectsSelf(this.currentPlacement.place)) {
         return false;
      } else if (!mc.field_1687.method_8320(this.currentPlacement.place).method_45474()) {
         return false;
      } else {
         return !mc.field_1687.method_8316(this.currentPlacement.place).method_15769() ? false : this.isValidSupport(this.currentPlacement.support);
      }
   }

   private boolean isRotationOnTarget(class_243 aimPoint) {
      if (RotationUtils.getRotation() != null && mc.field_1724 != null) {
         Rotation current = RotationUtils.getRotationSafely();
         Rotation wanted = RotationUtils.calculate(mc.field_1724.method_33571(), aimPoint);
         return Math.abs(RotationUtils.getAngleDifference(current.yaw, wanted.yaw)) < 12.0F
            && Math.abs(RotationUtils.getAngleDifference(current.pitch, wanted.pitch)) < 12.0F;
      } else {
         return false;
      }
   }

   private void doLavaRetrieve() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && this.currentPlacement != null) {
         this.retrieveTicks++;
         if (this.waitTicks > 0) {
            this.waitTicks--;
         } else {
            class_2338 expectedSource = this.currentPlacement.place;
            class_2338 actualSource = this.findNearbyLavaSource(expectedSource);
            class_2338 probe = actualSource != null ? actualSource : expectedSource;
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
               } else if (!this.isRotationOnTarget(actualSource.method_46558())) {
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
                     this.previousSlot = mc.field_1724.method_31548().field_7545;
                     mc.field_1724.method_31548().field_7545 = emptyBucketSlot;
                     class_1269 result = mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                     if (result.method_23665()) {
                        mc.field_1724.method_6104(class_1268.field_5808);
                     }

                     mc.field_1724.method_31548().field_7545 = this.previousSlot;
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

   private boolean isLavaSource(class_2338 pos) {
      return mc.field_1687.method_8320(pos).method_27852(class_2246.field_10164) && mc.field_1687.method_8316(pos).method_15771();
   }

   private boolean hasNearbyFlowingLava(class_2338 center) {
      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
               if (this.isFlowingLava(center.method_10069(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean tryReapplyLava() {
      if (this.target == null || this.target.method_31481() || !this.target.method_5805()) {
         return false;
      } else if (this.target.method_5809()) {
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
      return mc.field_1687 == null ? 9 : (mc.field_1687.method_8597().comp_644() ? 10 : 30);
   }

   private boolean isFlowingLava(class_2338 pos) {
      class_3610 state = mc.field_1687.method_8316(pos);
      class_3611 fluid = state.method_15772();
      return (fluid == class_3612.field_15908 || fluid == class_3612.field_15907) && !state.method_15771();
   }

   private boolean targetHasWaterBucket(class_1297 entity) {
      return !(entity instanceof class_1657 player)
         ? false
         : player.method_6047().method_31574(class_1802.field_8705) || player.method_6079().method_31574(class_1802.field_8705);
   }

   private boolean isWater(class_2338 pos) {
      class_3611 fluid = mc.field_1687.method_8316(pos).method_15772();
      return mc.field_1687.method_8320(pos).method_27852(class_2246.field_10382) || fluid == class_3612.field_15910 || fluid == class_3612.field_15909;
   }

   private boolean intersectsSelf(class_2338 pos) {
      return mc.field_1724 != null && mc.field_1724.method_5829().method_994(new class_238(pos));
   }

   private boolean isEntityInCobweb(class_1297 entity) {
      return entity != null && mc.field_1687 != null && this.hasAnyCobweb(entity.method_5829());
   }

   private boolean isAllCobweb(class_238 box) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         boolean any = false;

         for (int x = (int)Math.floor(box.field_1323); x <= (int)Math.floor(box.field_1320); x++) {
            for (int y = (int)Math.floor(box.field_1322); y <= (int)Math.floor(box.field_1325); y++) {
               for (int z = (int)Math.floor(box.field_1321); z <= (int)Math.floor(box.field_1324); z++) {
                  class_2338 pos = new class_2338(x, y, z);
                  if (box.method_994(new class_238(pos))) {
                     if (!mc.field_1687.method_8320(pos).method_27852(class_2246.field_10343)) {
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

   private boolean hasAnyCobweb(class_238 box) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         for (int x = (int)Math.floor(box.field_1323); x <= (int)Math.floor(box.field_1320); x++) {
            for (int y = (int)Math.floor(box.field_1322); y <= (int)Math.floor(box.field_1325); y++) {
               for (int z = (int)Math.floor(box.field_1321); z <= (int)Math.floor(box.field_1324); z++) {
                  class_2338 pos = new class_2338(x, y, z);
                  if (mc.field_1687.method_8320(pos).method_27852(class_2246.field_10343) && box.method_994(new class_238(pos))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private class_2338 findNearbyLavaSource(class_2338 center) {
      if (this.isLavaSource(center)) {
         return center;
      } else {
         class_2338 best = null;
         double bestDist = Double.POSITIVE_INFINITY;

         for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
               for (int z = -1; z <= 1; z++) {
                  class_2338 pos = center.method_10069(x, y, z);
                  if (this.isLavaSource(pos)) {
                     double dist = center.method_46558().method_1025(pos.method_46558());
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
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && stack.method_31574(class_1802.field_8786)) {
            return slot;
         }
      }

      return -1;
   }

   private int findBlockSlot() {
      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && stack.method_7909() instanceof class_1747 && !stack.method_31574(class_1802.field_8786)) {
            return slot;
         }
      }

      return -1;
   }

   private int findLavaBucketSlot() {
      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && stack.method_31574(class_1802.field_8187)) {
            return slot;
         }
      }

      return -1;
   }

   private int findEmptyBucketSlot() {
      for (int slot = 0; slot < 9; slot++) {
         class_1799 stack = mc.field_1724.method_31548().method_5438(slot);
         if (!stack.method_7960() && stack.method_31574(class_1802.field_8550)) {
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

   private record PlacementInfo(class_2338 support, class_2350 face, class_243 aim, class_2338 place) {
   }

   private record PlacementTarget(class_1297 target, AutoWeb.PlacementInfo placement, AutoWeb.PlacementType type) {
   }

   private static enum PlacementType {
      COBWEB,
      WEB_EXIT_PREDICT,
      GROUND_WEB,
      BLOCK,
      LAVA;
   }

   private record PredictionTick(int ticks, class_2338 footPos, class_2338 headPos) {
   }
}
