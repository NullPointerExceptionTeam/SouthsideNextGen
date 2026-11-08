package dev.southside.utils.render;

import net.minecraft.class_10055;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_583;
import net.minecraft.class_630;

public class WingsModel extends class_583<class_10055> {
   private final class_630 wing = this.field_54014.method_32086("wing");
   private final class_630 wingTip = this.wing.method_32086("wingtip");

   public WingsModel() {
      super(getTexturedModelData().method_32109());
   }

   public static class_5607 getTexturedModelData() {
      class_5609 modelData = new class_5609();
      class_5610 rootData = modelData.method_32111();
      class_5610 wingData = rootData.method_32117(
         "wing",
         class_5606.method_32108()
            .method_32101(0, 0)
            .method_32097(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F)
            .method_32101(-10, 8)
            .method_32097(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         class_5603.method_32090(-2.0F, 0.0F, 0.0F)
      );
      wingData.method_32117(
         "wingtip",
         class_5606.method_32108()
            .method_32101(0, 5)
            .method_32097(-10.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F)
            .method_32101(-10, 18)
            .method_32097(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         class_5603.method_32090(-10.0F, 0.0F, 0.0F)
      );
      return class_5607.method_32110(modelData, 30, 30);
   }

   public void setAngles(float time) {
      this.wing.field_3654 = (float)Math.toRadians(-80.0) - (float)Math.cos(time) * 0.2F;
      this.wing.field_3675 = (float)Math.toRadians(20.0) + (float)Math.sin(time) * 0.4F;
      this.wing.field_3674 = (float)Math.toRadians(20.0);
      this.wingTip.field_3674 = -((float)(Math.sin(time + 2.0F) + 0.5)) * 0.75F;
   }

   public void setAngles(class_10055 state) {
   }
}
