package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.render.ColorUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;

public class BowAimAssist extends Module {
   private final Slider fov = new Slider("Fov", 180, 10, 360);
   private final Slider range = new Slider("Range", 100, 1, 200);
   private final Dropdown priority = new Dropdown("Priority", "Range", "Angle", "Range");
   private final Switch autoRelease = new Switch("AutoRelease", true);
   private final Switch rotationShot = new Switch("RotationShot", true);
   private final Switch prediction = new Switch("Prediction", true);
   private class_1309 target;
   private class_243 predictedPos;
   private int crossbowAimTicks = 0;
   private boolean crossbowFirePending = false;
   private static final int AIM_TICKS_BEFORE_RELEASE = 3;
   private static final double DRAG = 0.99;

   public BowAimAssist() {
      super(Category.Combat, "BowAimAssist");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.autoRelease.getLabel(), () -> false);
      this.hideIf(this.rotationShot.getLabel(), () -> false);
      this.hideIf(this.prediction.getLabel(), () -> false);
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE && mc.field_1724 != null) {
         class_1799 heldItem = mc.field_1724.method_6047();
         boolean holdingChargedCrossbow = heldItem != null && heldItem.method_7909() == class_1802.field_8399 && class_1764.method_7781(heldItem);
         if (this.rotationShot.getValue()
            && holdingChargedCrossbow
            && mc.field_1690.field_1904.method_1434()
            && this.target != null
            && !this.crossbowFirePending) {
            this.crossbowFirePending = true;
            this.crossbowAimTicks = 0;
            mc.field_1690.field_1904.method_23481(false);
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         class_1799 heldItem = mc.field_1724.method_6047();
         boolean isBow = heldItem != null && heldItem.method_7909() == class_1802.field_8102 && mc.field_1724.method_6115();
         boolean isCrossbow = heldItem != null && heldItem.method_7909() == class_1802.field_8399 && class_1764.method_7781(heldItem);
         if (!isBow && !isCrossbow) {
            this.target = null;
            this.predictedPos = null;
            this.crossbowAimTicks = 0;
            this.crossbowFirePending = false;
         } else {
            this.target = this.getTarget();
            if (this.target == null) {
               this.predictedPos = null;
               this.crossbowAimTicks = 0;
               this.crossbowFirePending = false;
            } else {
               if (isBow) {
                  Rotation rotation = this.getAimRotation(this.target);
                  this.predictedPos = null;
                  RotationUtils.setRotation(rotation);
                  if (this.autoRelease.getValue() && mc.field_1724.method_6048() >= 20) {
                     mc.field_1761.method_2897(mc.field_1724);
                  }
               } else {
                  Rotation rotation;
                  if (this.prediction.getValue()) {
                     this.predictedPos = this.getPredictedPos(this.target);
                     rotation = RotationUtils.getRotationTo(mc.field_1724.method_33571(), this.predictedPos);
                  } else {
                     this.predictedPos = null;
                     rotation = this.getAimRotation(this.target);
                  }

                  if (this.rotationShot.getValue()) {
                     if (this.crossbowFirePending) {
                        this.crossbowAimTicks++;
                        RotationUtils.setRotation(rotation);
                        mc.field_1690.field_1904.method_23481(false);
                     }
                  } else {
                     RotationUtils.setRotation(rotation);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = -200
   )
   public void onRotationApplied(RotationAppliedEvent event) {
      if (mc.field_1724 != null) {
         class_1799 heldItem = mc.field_1724.method_6047();
         if (heldItem != null) {
            if (this.crossbowFirePending && heldItem.method_7909() == class_1802.field_8399 && class_1764.method_7781(heldItem) && this.crossbowAimTicks >= 3) {
               this.crossbowFirePending = false;
               this.crossbowAimTicks = 0;
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (this.target != null) {
         Color healthColor = ColorUtils.getHealthColor(this.target.method_6032(), 20.0F);
         float tickDelta = event.getTickCounter();
         if (this.prediction.getValue() && this.predictedPos != null) {
            double halfW = this.target.method_17681() / 2.0;
            double h = this.target.method_17682();
            class_238 predictBox = new class_238(
               this.predictedPos.field_1352 - halfW,
               this.predictedPos.field_1351,
               this.predictedPos.field_1350 - halfW,
               this.predictedPos.field_1352 + halfW,
               this.predictedPos.field_1351 + h,
               this.predictedPos.field_1350 + halfW
            );
            Render3D.boxSides(predictBox, new Color(100, 200, 255, 70));
            Render3D.boxLines(predictBox, new Color(100, 200, 255, 150));
         }

         double moveX = this.target.method_23317() - this.target.field_6014;
         double moveY = this.target.method_23318() - this.target.field_6036;
         double moveZ = this.target.method_23321() - this.target.field_5969;
         class_238 box = this.target
            .method_5829()
            .method_1014(0.2)
            .method_989(-moveX + moveX * tickDelta, -moveY + moveY * tickDelta, -moveZ + moveZ * tickDelta);
         Render3D.boxSides(box, new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 100));
         Render3D.boxLines(box, new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 150));
      }
   }

   private class_1309 getTarget() {
      Stream<class_1309> stream = StreamSupport.<class_1297>stream(mc.field_1687.method_18112().spliterator(), false)
         .filter(e -> e instanceof class_1309)
         .map(e -> (class_1309)e)
         .filter(e -> TargetUtils.isTarget(e, true, true))
         .filter(e -> mc.field_1724.method_5739(e) <= this.range.getValue().doubleValue())
         .filter(mc.field_1724::method_6057)
         .filter(e -> RotationUtils.inFoV(e, this.fov.getValue().floatValue()));
      if (this.priority.isMode("Range")) {
         stream = stream.sorted(Comparator.comparingDouble(e -> e.method_5739(mc.field_1724)));
      } else if (this.priority.isMode("Angle")) {
         stream = stream.sorted(Comparator.comparingDouble(RotationUtils::getYawToEntity));
      }

      List<class_1309> list = stream.toList();
      return list.isEmpty() ? null : list.get(0);
   }

   private class_243 getTargetPos(class_1309 target) {
      return new class_243(target.method_53830(), target.method_53827(), target.method_53828());
   }

   private Rotation getAimRotation(class_1309 target) {
      class_243 pos = this.getTargetPos(target);
      double distanceToEnt = mc.field_1724.method_5739(target);
      class_243 velocity = new class_243(
         target.method_23317() - target.field_6038, target.method_23318() - target.field_5971, target.method_23321() - target.field_5989
      );
      double predictX = pos.field_1352 + velocity.field_1352 * (distanceToEnt * 0.8);
      double predictZ = pos.field_1350 + velocity.field_1350 * (distanceToEnt * 0.8);
      double x = predictX - mc.field_1724.method_23317();
      double z = predictZ - mc.field_1724.method_23321();
      double h = pos.field_1351 + 1.0 - (mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376()));
      double h1 = Math.sqrt(x * x + z * z);
      float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)h1, (float)h, 1.0F);
      return new Rotation(yaw, pitch);
   }

   private class_243 getPredictedPos(class_1309 target) {
      class_243 pos = this.getTargetPos(target);
      class_243 targetCenter = pos.method_1031(0.0, target.method_17682() / 2.0, 0.0);
      class_243 eyePos = mc.field_1724.method_33571();
      double distance = eyePos.method_1022(targetCenter);
      double arrowSpeed = 3.15;
      int flightTicks = this.estimateFlightTicks(distance, arrowSpeed);
      class_243 velocity = new class_243(
         target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969
      );
      double predX = pos.field_1352 + velocity.field_1352 * flightTicks;
      double predY = pos.field_1351 + velocity.field_1351 * flightTicks;
      double predZ = pos.field_1350 + velocity.field_1350 * flightTicks;
      return new class_243(predX, predY + target.method_17682() / 2.0, predZ);
   }

   private int estimateFlightTicks(double distance, double initialSpeed) {
      double traveled = 0.0;
      double speed = initialSpeed;

      int ticks;
      for (ticks = 0; traveled < distance && ticks < 100; ticks++) {
         traveled += speed;
         speed *= 0.99;
      }

      return ticks;
   }
}
