package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
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

   public void transform(class_4587 matrices, float side, float equipProgress, float sp, class_1268 hand, class_1306 arm) {
      matrices.method_22904(this.blockedX.getDouble(), this.blockedY.getDouble(), this.blockedZ.getDouble());
      String var7 = this.mode.getMode();
      switch (var7) {
         case "Vanilla": {
            matrices.method_46416(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            float f = class_3532.method_15374(sp * sp * (float) Math.PI);
            float f1 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(side * (45.0F + f * -20.0F)));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(side * f1 * -20.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(f1 * -80.0F));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(side * -45.0F));
            matrices.method_22905(0.9F, 0.9F, 0.9F);
            matrices.method_46416(-0.2F, 0.126F, 0.2F);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-102.25F));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(side * 15.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(side * 80.0F));
            break;
         }
         case "ModernVanilla": {
            matrices.method_46416(side * 0.48F, -0.55F, -0.71999997F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = class_3532.method_15374(sp * sp * (float) Math.PI);
            float var7x = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            this.rotate(matrices, var6 * -20.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, var7x * -20.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var7x * -69.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(1.2F, 1.2F, 1.2F);
            break;
         }
         case "OneSeven":
            matrices.method_46416(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            this.applySwingOffset(matrices, arm, sp);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-102.25F));
            matrices.method_22907((arm == class_1306.field_6183 ? class_7833.field_40716 : class_7833.field_40715).rotationDegrees(13.365F));
            matrices.method_22907((arm == class_1306.field_6183 ? class_7833.field_40718 : class_7833.field_40717).rotationDegrees(78.05F));
            break;
         case "Pushdown":
            matrices.method_46416(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
            float g = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_22907(class_7833.field_40718.rotationDegrees((arm == class_1306.field_6183 ? 1 : -1) * g * 10.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(g * -35.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-102.25F));
            matrices.method_22907((arm == class_1306.field_6183 ? class_7833.field_40716 : class_7833.field_40715).rotationDegrees(13.365F));
            matrices.method_22907((arm == class_1306.field_6183 ? class_7833.field_40718 : class_7833.field_40717).rotationDegrees(78.05F));
            break;
         case "Swang": {
            matrices.method_22904(-0.1F, 0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            float sinF = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(-sinF, -0.0F, 9.0F, sinF * 30.0F / 2.0F));
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(1.0F, -sinF / 2.0F, -0.0F, sinF * 40.0F));
            this.doBlockTransformations(matrices);
            break;
         }
         case "Swank": {
            matrices.method_22904(-0.1F, 0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            float sinF = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(-sinF, -0.0F, 9.0F, sinF * 30.0F));
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(1.0F, -sinF, -0.0F, sinF * 40.0F));
            this.doBlockTransformations(matrices);
            break;
         }
         case "Swing":
            matrices.method_22904(-0.1F, 0.15F, 0.0);
            matrices.method_22904(0.1F, -0.15F, 0.0);
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, sp);
            this.doBlockTransformations(matrices);
            break;
         case "Swong":
            this.transformFirstPersonItem(matrices, side, equipProgress / 2.0F, 0.0F);
            float var15 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(0.0F, 0.3F, -0.0F);
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(1.0F, 0.0F, 2.0F, -var15 * 30.0F));
            matrices.method_22907(new Quaternionf().fromAxisAngleDeg(1.5F, (float)(var15 / 1.2), 0.0F, -var15 * 26.0F));
            this.doBlockTransformations(matrices);
            break;
         case "SwAing":
            matrices.method_22904(-0.1F, 0.15F, 0.0);
            matrices.method_22904(0.1F, -0.2F, 0.0);
            this.avatar(matrices, equipProgress, sp);
            this.doBlockTransformations(matrices);
            break;
         case "Remix":
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.83F);
            this.doBlockTransformations(matrices);
            float f4 = class_3532.method_15374(class_3532.method_15355(sp) * 3.83F);
            this.rotate(matrices, -f4 * 0.0F, 0.0F, 0.0F, 0.0F);
            this.rotate(matrices, -f4 * 43.0F, 58.0F, 23.0F, 45.0F);
            break;
         case "Test":
            this.ticks++;
            matrices.method_46416(0.0F, -0.48F + equipProgress * -0.5F, -0.82F);
            float travel = class_3532.method_15374(this.ticks * 0.12F) * 0.15F;
            matrices.method_46416(0.0F, 0.0F, travel);
            float swingPulse = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI) * 0.05F;
            matrices.method_46416(0.0F, swingPulse * -0.1F, swingPulse * 0.25F);
            matrices.method_22907(class_7833.field_40718.rotationDegrees(90.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-82.0F));
            matrices.method_22905(1.05F, 1.05F, 1.05F);
            break;
         case "Test1": {
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            float var8 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
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
            float var8 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(-0.05F, 0.3F, 0.0F);
            this.rotate(matrices, -var8 * 70.0F / 2.0F, -8.0F, -0.0F, 9.0F);
            this.rotate(matrices, -var8 * 70.0F, 1.0F, -0.4F, -0.0F);
            break;
         }
         case "Punch":
            this.transformFirstPersonItem(matrices, side, equipProgress, 0.0F);
            this.doBlockTransformations(matrices);
            float var14 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(0.1F, 0.2F, 0.3F);
            this.rotate(matrices, -var14 * 30.0F, -5.0F, 0.0F, 9.0F);
            this.rotate(matrices, -var14 * 10.0F, 1.0F, -0.4F, -0.5F);
            break;
         case "Rotate":
            this.ticks++;
            matrices.method_22904(0.7, -0.4F, -0.8F);
            this.rotate(matrices, 50.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, 50.0F, 0.0F, 0.0F, -1.0F);
            this.rotate(matrices, (float)(this.ticks * 0.2F * this.speed.getDouble()), 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, -25.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Winter":
            matrices.method_46416(0.56F, -0.52F, -0.71999997F);
            matrices.method_46416(0.0F, equipProgress * -0.6F, 0.0F);
            this.rotate(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI) * -35.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Circle":
            this.ticks += 2;
            matrices.method_46416(0.7F, -0.4F, -0.8F);
            this.rotate(matrices, (float)(this.ticks * 0.2F * this.speed.getDouble()), 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, 40.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, 34.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Tap": {
            matrices.method_22904(0.0, -3.5, 0.0);
            matrices.method_46416(side * 0.56F, -0.52F, -0.72F);
            matrices.method_46416(side * 0.56F, -0.22F, -0.71999997F);
            this.rotate(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            float var6 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            this.rotate(matrices, 0.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var6 * -9.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -9.0F, 0.0F, 0.0F, 1.0F);
            matrices.method_22904(0.0, 3.2F, 0.0);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(2.7F, 2.7F, 2.7F);
            break;
         }
         case "Tap2": {
            matrices.method_46416(side * 0.648F, -0.55F, -0.71999997F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -var6 * 10.0F, 1.0F, -2.0F, 3.0F);
            matrices.method_22905(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Slide": {
            matrices.method_46416(side * 0.648F, -0.55F, -0.71999997F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -var6 * 20.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Scale": {
            matrices.method_46416(side * 0.48F, -0.55F, -0.71999997F);
            matrices.method_22904(0.0, equipProgress * -0.2F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            float var6 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            float scale = 1.2F - var6 * 0.3F;
            matrices.method_22905(scale, scale, scale);
            break;
         }
         case "Dekael": {
            float f1 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(side * 0.56F, -0.52F, -0.72F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotateVector(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotateVector(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotateVector(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotateVector(matrices, f1 * 10.0F, -4.0F, -2.0F, 5.0F);
            this.rotateVector(matrices, f1 * 30.0F, 1.0F, 0.0F, -1.0F);
            break;
         }
         case "Leaked": {
            float f1 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(side * 0.56F, -0.52F, -0.72F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotateVector(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotateVector(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotateVector(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            this.rotateVector(matrices, f1 * 10.0F, 4.0F, 2.0F, -5.0F);
            this.rotateVector(matrices, f1 * 40.0F, -1.0F, 0.0F, 1.0F);
            break;
         }
         case "Ninja": {
            matrices.method_46416(side * 0.48F, -0.39F, -0.71999997F);
            matrices.method_22904(0.0, equipProgress * -0.6F, 0.0);
            this.rotate(matrices, 100.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -50.0F, 0.0F, 0.0F, 1.0F);
            float var6 = class_3532.method_15374(sp * (float) Math.PI);
            float var7xx = class_3532.method_15374(sp * (float) Math.PI);
            this.rotate(matrices, var6 * -10.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, var7xx * -30.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, var7xx * 109.0F, 1.0F, 0.0F, 0.0F);
            this.rotate(matrices, -90.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(1.2F, 1.2F, 1.2F);
            break;
         }
         case "Down": {
            float var6 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
            matrices.method_46416(side * 0.48F, -0.55F, -0.71999997F);
            matrices.method_22904(0.0, var6 * -0.2F, 0.0);
            this.rotate(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.rotate(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.rotate(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.method_22905(1.2F, 1.2F, 1.2F);
         }
      }
   }

   private void applySwingOffset(class_4587 matrices, class_1306 arm, float sp) {
      int side = arm == class_1306.field_6183 ? 1 : -1;
      float f = class_3532.method_15374(sp * sp * (float) Math.PI);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * (45.0F + f * -20.0F)));
      float g = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * g * -20.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(g * -80.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * -45.0F));
   }

   private void doBlockTransformations(class_4587 matrices) {
      matrices.method_22907(class_7833.field_40716.rotationDegrees(30.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(60.0F));
   }

   private void transformFirstPersonItem(class_4587 matrices, float side, float equipProgress, float sp) {
      matrices.method_46416(side * 0.56F, -0.52F + equipProgress * -0.6F, -0.71999997F);
      matrices.method_22907(class_7833.field_40716.rotation(45.0F));
      float f = class_3532.method_15374(sp * sp * (float) Math.PI);
      float f1 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * (45.0F + f * -20.0F)));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(side * f1 * -20.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(f1 * -80.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(side * -45.0F));
      matrices.method_22905(0.9F, 0.9F, 0.9F);
   }

   private void avatar(class_4587 matrices, float equipProgress, float sp) {
      matrices.method_46416(0.6F, -0.48F, -0.79999995F);
      matrices.method_46416(0.0F, 0.0F, 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(45.0F));
      float f1 = class_3532.method_15374(class_3532.method_15355(sp) * (float) Math.PI);
      matrices.method_22907(new Quaternionf().fromAxisAngleDeg(1.0F, -0.2F, 0.2F, f1 * -40.0F));
      matrices.method_22905(0.9F, 0.9F, 0.9F);
   }

   private void rotate(class_4587 matrices, float angle, float x, float y, float z) {
      matrices.method_22907(new Quaternionf().fromAxisAngleDeg(x, y, z, angle));
   }

   private void rotateVector(class_4587 matrices, float angle, float x, float y, float z) {
      Quaternionf rotation = new Quaternionf().fromAxisAngleDeg(new Vector3f(x, y, z).normalize(), angle);
      matrices.method_22907(rotation);
   }
}
