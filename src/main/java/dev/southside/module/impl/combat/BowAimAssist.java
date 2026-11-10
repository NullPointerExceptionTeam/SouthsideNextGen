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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BowAimAssist extends Module {
   private final Slider fov = new Slider("Fov", 180, 10, 360);
   private final Slider range = new Slider("Range", 100, 1, 200);
   private final Dropdown priority = new Dropdown("Priority", "Range", "Angle", "Range");
   private final Switch autoRelease = new Switch("AutoRelease", true);
   private final Switch rotationShot = new Switch("RotationShot", true);
   private final Switch prediction = new Switch("Prediction", true);
   private LivingEntity target;
   private Vec3d predictedPos;
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
      if (event.getState() == Stage.PRE && mc.player != null) {
         ItemStack heldItem = mc.player.getMainHandStack();
         boolean holdingChargedCrossbow = heldItem != null && heldItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(heldItem);
         if (this.rotationShot.getValue()
            && holdingChargedCrossbow
            && mc.options.useKey.isPressed()
            && this.target != null
            && !this.crossbowFirePending) {
            this.crossbowFirePending = true;
            this.crossbowAimTicks = 0;
            mc.options.useKey.setPressed(false);
         }
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         ItemStack heldItem = mc.player.getMainHandStack();
         boolean isBow = heldItem != null && heldItem.getItem() == Items.BOW && mc.player.isUsingItem();
         boolean isCrossbow = heldItem != null && heldItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(heldItem);
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
                  if (this.autoRelease.getValue() && mc.player.getItemUseTime() >= 20) {
                     mc.interactionManager.stopUsingItem(mc.player);
                  }
               } else {
                  Rotation rotation;
                  if (this.prediction.getValue()) {
                     this.predictedPos = this.getPredictedPos(this.target);
                     rotation = RotationUtils.getRotationTo(mc.player.getEyePos(), this.predictedPos);
                  } else {
                     this.predictedPos = null;
                     rotation = this.getAimRotation(this.target);
                  }

                  if (this.rotationShot.getValue()) {
                     if (this.crossbowFirePending) {
                        this.crossbowAimTicks++;
                        RotationUtils.setRotation(rotation);
                        mc.options.useKey.setPressed(false);
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
      if (mc.player != null) {
         ItemStack heldItem = mc.player.getMainHandStack();
         if (heldItem != null) {
            if (this.crossbowFirePending && heldItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(heldItem) && this.crossbowAimTicks >= 3) {
               this.crossbowFirePending = false;
               this.crossbowAimTicks = 0;
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (this.target != null) {
         Color healthColor = ColorUtils.getHealthColor(this.target.getHealth(), 20.0F);
         float tickDelta = event.getTickCounter();
         if (this.prediction.getValue() && this.predictedPos != null) {
            double halfW = this.target.getWidth() / 2.0;
            double h = this.target.getHeight();
            Box predictBox = new Box(
               this.predictedPos.x - halfW,
               this.predictedPos.y,
               this.predictedPos.z - halfW,
               this.predictedPos.x + halfW,
               this.predictedPos.y + h,
               this.predictedPos.z + halfW
            );
            Render3D.boxSides(predictBox, new Color(100, 200, 255, 70));
            Render3D.boxLines(predictBox, new Color(100, 200, 255, 150));
         }

         double moveX = this.target.getX() - this.target.prevX;
         double moveY = this.target.getY() - this.target.prevY;
         double moveZ = this.target.getZ() - this.target.prevZ;
         Box box = this.target
            .getBoundingBox()
            .expand(0.2)
            .offset(-moveX + moveX * tickDelta, -moveY + moveY * tickDelta, -moveZ + moveZ * tickDelta);
         Render3D.boxSides(box, new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 100));
         Render3D.boxLines(box, new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 150));
      }
   }

   private LivingEntity getTarget() {
      Stream<LivingEntity> stream = StreamSupport.<Entity>stream(mc.world.getEntities().spliterator(), false)
         .filter(e -> e instanceof LivingEntity)
         .map(e -> (LivingEntity)e)
         .filter(e -> TargetUtils.isTarget(e, true, true))
         .filter(e -> mc.player.distanceTo(e) <= this.range.getValue().doubleValue())
         .filter(mc.player::canSee)
         .filter(e -> RotationUtils.inFoV(e, this.fov.getValue().floatValue()));
      if (this.priority.isMode("Range")) {
         stream = stream.sorted(Comparator.comparingDouble(e -> e.distanceTo(mc.player)));
      } else if (this.priority.isMode("Angle")) {
         stream = stream.sorted(Comparator.comparingDouble(RotationUtils::getYawToEntity));
      }

      List<LivingEntity> list = stream.toList();
      return list.isEmpty() ? null : list.get(0);
   }

   private Vec3d getTargetPos(LivingEntity target) {
      return new Vec3d(target.getLerpTargetX(), target.getLerpTargetY(), target.getLerpTargetZ());
   }

   private Rotation getAimRotation(LivingEntity target) {
      Vec3d pos = this.getTargetPos(target);
      double distanceToEnt = mc.player.distanceTo(target);
      Vec3d velocity = new Vec3d(
         target.getX() - target.lastRenderX, target.getY() - target.lastRenderY, target.getZ() - target.lastRenderZ
      );
      double predictX = pos.x + velocity.x * (distanceToEnt * 0.8);
      double predictZ = pos.z + velocity.z * (distanceToEnt * 0.8);
      double x = predictX - mc.player.getX();
      double z = predictZ - mc.player.getZ();
      double h = pos.y + 1.0 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
      double h1 = Math.sqrt(x * x + z * z);
      float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
      float pitch = -RotationUtils.getTrajAngleSolutionLow((float)h1, (float)h, 1.0F);
      return new Rotation(yaw, pitch);
   }

   private Vec3d getPredictedPos(LivingEntity target) {
      Vec3d pos = this.getTargetPos(target);
      Vec3d targetCenter = pos.add(0.0, target.getHeight() / 2.0, 0.0);
      Vec3d eyePos = mc.player.getEyePos();
      double distance = eyePos.distanceTo(targetCenter);
      double arrowSpeed = 3.15;
      int flightTicks = this.estimateFlightTicks(distance, arrowSpeed);
      Vec3d velocity = new Vec3d(
         target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ
      );
      double predX = pos.x + velocity.x * flightTicks;
      double predY = pos.y + velocity.y * flightTicks;
      double predZ = pos.z + velocity.z * flightTicks;
      return new Vec3d(predX, predY + target.getHeight() / 2.0, predZ);
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
