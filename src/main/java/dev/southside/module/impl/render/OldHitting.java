package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OldHitting extends Module {
   private int ticks;
   public Dropdown mode = new Dropdown(
      "Mode",
      "Vanilla",
      "Vanilla",
      "ModernVanilla",
      "OneSeven",
      "Pushdown",
      "Swang",
      "Swank",
      "Swing",
      "Swong",
      "SwAing",
      "Remix",
      "Test",
      "Test1",
      "Null",
      "Jello",
      "Punch",
      "Rotate",
      "Winter",
      "Circle",
      "Tap",
      "Tap2",
      "Slide",
      "Scale",
      "Dekael",
      "Leaked",
      "Ninja",
      "Down"
   );
   public Slider speed = new Slider("Speed", 1.0, -10, 10, 0.1);
   public Slider blockedX = new Slider("BlockedX", 0, -0.5, 0.5, 0.01);
   public Slider blockedY = new Slider("BlockedY", 0, -0.5, 0.5, 0.01);
   public Slider blockedZ = new Slider("BlockedZ", 0, -0.5, 0.5, 0.01);
   public Slider itemX = new Slider("X", 0, -0.5, 0.5, 0.01);
   public Slider itemY = new Slider("Y", 0, -0.5, 0.5, 0.01);
   public Slider itemZ = new Slider("Z", 0, -0.5, 0.5, 0.01);
   public Switch axe = new Switch("Axe", false);

   public OldHitting() {
      super(Category.Render, "OldHitting", "重生之我在1.8。");
   }

   @Override
   public void onEnable() {
      this.ticks = 0;
   }

   public void transform(MatrixStack matrices, float side, float equipProgress, float sp, Hand hand, Arm arm) {
      matrices.translate(this.blockedX.getDouble(), this.blockedY.getDouble(), this.blockedZ.getDouble());
      String var7 = this.mode.getMode();
      switch (var7) {
         case "Vanilla": {
            matrices.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            float f = MathHelper.sin(sp * sp * (float) Math.PI);
            float f1 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0F + f * -20.0F)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * f1 * -20.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0F));
            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.translate(-0.2F, 0.126F, 0.2F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 15.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * 80.0F));
            break;
         }
         case "ModernVanilla": {
            matrices.translate(side * 0.48F, -0.55F, -0.71999997F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = MathHelper.sin(sp * sp * (float) Math.PI);
            float var7x = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, var6 * -20.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, var7x * -20.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var7x * -69.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         }
         case "OneSeven":
            matrices.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            this.applySwingOffset(matrices, arm, sp);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
            break;
         case "Pushdown":
            matrices.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            float g = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * g * 10.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
            matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
            break;
         case "Swang": {
            matrices.translate(-0.1F, 0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            float sinF = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(-sinF, -0.0F, 9.0F, sinF * 30.0F / 2.0F));
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(1.0F, -sinF / 2.0F, -0.0F, sinF * 40.0F));
            this.doBlockTransformations(matrices);
            break;
         }
         case "Swank": {
            matrices.translate(-0.1F, 0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            float sinF = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(-sinF, -0.0F, 9.0F, sinF * 30.0F));
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(1.0F, -sinF, -0.0F, sinF * 40.0F));
            this.doBlockTransformations(matrices);
            break;
         }
         case "Swing":
            matrices.translate(-0.1F, 0.15F, 0.0);
            matrices.translate(0.1F, -0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            this.doBlockTransformations(matrices);
            break;
         case "Swong":
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, 0.0F);
            float var15 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(0.0F, 0.3F, -0.0F);
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(1.0F, 0.0F, 2.0F, -var15 * 30.0F));
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(1.5F, (float)(var15 / 1.2), 0.0F, -var15 * 26.0F));
            this.doBlockTransformations(matrices);
            break;
         case "SwAing":
            matrices.translate(-0.1F, 0.15F, 0.0);
            matrices.translate(0.1F, -0.2F, 0.0);
            this.avatar(matrices, equipProgress, sp);
            this.doBlockTransformations(matrices);
            break;
         case "Remix":
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.83F);
            this.doBlockTransformations(matrices);
            float f4 = MathHelper.sin(MathHelper.sqrt(sp) * 3.83F);
            this.rotate(matrices, -f4 * 0.0F, 0.0F, 0.0F, 0.0F);
            this.rotate(matrices, -f4 * 43.0F, 58.0F, 23.0F, 45.0F);
            break;
         case "Test":
            this.ticks++;
            matrices.translate(0.0F, -0.48F + equipProgress * -0.5F, -0.82F);
            float travel = MathHelper.sin(this.ticks * 0.12F) * 0.15F;
            matrices.translate(0.0F, 0.0F, travel);
            float swingPulse = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI) * 0.05F;
            matrices.translate(0.0F, swingPulse * -0.1F, swingPulse * 0.25F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-82.0F));
            matrices.scale(1.05F, 1.05F, 1.05F);
            break;
         case "Test1": {
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            float var8 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, -var8 * 70.0F / 2.0F, -2.0F, -0.0F, 2.0F);
            this.rotate(matrices, -var8 * 70.0F, 1.0F, -0.4F, -0.0F);
            break;
         }
         case "Null":
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            break;
         case "Jello": {
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            float var8 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(-0.05F, 0.3F, 0.0F);
            this.rotate(matrices, -var8 * 70.0F / 2.0F, -8.0F, -0.0F, 9.0F);
            this.rotate(matrices, -var8 * 70.0F, 1.0F, -0.4F, -0.0F);
            break;
         }
         case "Punch":
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            float var14 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(0.1F, 0.2F, 0.3F);
            this.rotate(matrices, -var14 * 30.0F, -5.0F, 0.0F, 9.0F);
            this.rotate(matrices, -var14 * 10.0F, 1.0F, -0.4F, -0.5F);
            break;
         case "Rotate":
            this.ticks++;
            matrices.translate(0.7, -0.4F, -0.8F);
            this.rotate(matrices, 50.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, 50.0F, 0.0F, 0.0F, -1.0F);
            this.rotate(matrices, (float)(this.ticks * 0.2F * this.speed.getDouble()), 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, -25.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Winter":
            matrices.translate(0.56F, -0.52F, -0.71999997F);
            matrices.translate(0.0F, equipProgress * -0.6F, 0.0F);
            this.rotate(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI) * -35.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Circle":
            this.ticks += 2;
            matrices.translate(0.7F, -0.4F, -0.8F);
            this.rotate(matrices, (float)(this.ticks * 0.2F * this.speed.getDouble()), 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, 40.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, 34.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Tap": {
            matrices.translate(0.0, -3.5, 0.0);
            matrices.translate(side * 0.56F, -0.52F, -0.72F);
            matrices.translate(side * 0.56F, -0.22F, -0.71999997F);
            this.rotate(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            float var6 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, 0.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var6 * -9.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -9.0F, 0.0F, 0.0F, 1.0F);
            matrices.translate(0.0, 3.2F, 0.0);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(2.7F, 2.7F, 2.7F);
            break;
         }
         case "Tap2": {
            matrices.translate(side * 0.648F, -0.55F, -0.71999997F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -var6 * 10.0F, 1.0F, -2.0F, 3.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Slide": {
            matrices.translate(side * 0.648F, -0.55F, -0.71999997F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -var6 * 20.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Scale": {
            matrices.translate(side * 0.48F, -0.55F, -0.71999997F);
            matrices.translate(0.0, equipProgress * -0.2F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            float scale = 1.2F - var6 * 0.3F;
            matrices.scale(scale, scale, scale);
            break;
         }
         case "Dekael": {
            float f1 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(side * 0.56F, -0.52F, -0.72F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotateVector(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotateVector(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotateVector(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotateVector(matrices, f1 * 10.0F, -4.0F, -2.0F, 5.0F);
            this.rotateVector(matrices, f1 * 30.0F, 1.0F, 0.0F, -1.0F);
            break;
         }
         case "Leaked": {
            float f1 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(side * 0.56F, -0.52F, -0.72F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotateVector(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotateVector(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotateVector(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotateVector(matrices, f1 * 10.0F, 4.0F, 2.0F, -5.0F);
            this.rotateVector(matrices, f1 * 40.0F, -1.0F, 0.0F, 1.0F);
            break;
         }
         case "Ninja": {
            matrices.translate(side * 0.48F, -0.39F, -0.71999997F);
            matrices.translate(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 100.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -50.0F, 0.0F, 0.0F, 1.0F);
            float var6 = MathHelper.sin(sp * (float) Math.PI);
            float var7xx = MathHelper.sin(sp * (float) Math.PI);
            this.rotate(matrices, var6 * -10.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, var7xx * -30.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var7xx * 109.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -90.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Down": {
            float var6 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
            matrices.translate(side * 0.48F, -0.55F, -0.71999997F);
            matrices.translate(0.0, var6 * -0.2F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
         }
      }
   }

   private void applySwingOffset(MatrixStack matrices, Arm arm, float sp) {
      int side = arm == Arm.RIGHT ? 1 : -1;
      float f = MathHelper.sin(sp * sp * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0F + f * -20.0F)));
      float g = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * g * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0F));
   }

   private void doBlockTransformations(MatrixStack matrices) {
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0F));
   }

   private void transformFirstPersonItem(MatrixStack matrices, float side, float equipProgress, float sp) {
      matrices.translate(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation(45.0F));
      float f = MathHelper.sin(sp * sp * (float) Math.PI);
      float f1 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45.0F + f * -20.0F)));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * f1 * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f1 * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45.0F));
      matrices.scale(0.9F, 0.9F, 0.9F);
   }

   private void avatar(MatrixStack matrices, float equipProgress, float sp) {
      matrices.translate(0.6F, -0.48F, -0.79999995F);
      matrices.translate(0.0F, 0.0F, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
      float f1 = MathHelper.sin(MathHelper.sqrt(sp) * (float) Math.PI);
      matrices.multiply(new Quaternionf().fromAxisAngleDeg(1.0F, -0.2F, 0.2F, f1 * -40.0F));
      matrices.scale(0.9F, 0.9F, 0.9F);
   }

   private void rotate(MatrixStack matrices, float angle, float x, float y, float z) {
      matrices.multiply(new Quaternionf().fromAxisAngleDeg(x, y, z, angle));
   }

   private void rotateVector(MatrixStack matrices, float angle, float x, float y, float z) {
      Quaternionf rotation = new Quaternionf().fromAxisAngleDeg(new Vector3f(x, y, z).normalize(), angle);
      matrices.multiply(rotation);
   }
}
