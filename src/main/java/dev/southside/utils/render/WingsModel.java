package dev.southside.utils.render;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.model.ModelPart;

public class WingsModel extends EntityModel<PlayerEntityRenderState> {
   private final ModelPart wing = this.root.getChild("wing");
   private final ModelPart wingTip = this.wing.getChild("wingtip");

   public WingsModel() {
      super(getTexturedModelData().createModel());
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData rootData = modelData.getRoot();
      ModelPartData wingData = rootData.addChild(
         "wing",
         ModelPartBuilder.create()
            .uv(0, 0)
            .cuboid(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F)
            .uv(-10, 8)
            .cuboid(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         ModelTransform.pivot(-2.0F, 0.0F, 0.0F)
      );
      wingData.addChild(
         "wingtip",
         ModelPartBuilder.create()
            .uv(0, 5)
            .cuboid(-10.0F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F)
            .uv(-10, 18)
            .cuboid(-10.0F, 0.0F, 0.5F, 10.0F, 0.0F, 10.0F),
         ModelTransform.pivot(-10.0F, 0.0F, 0.0F)
      );
      return TexturedModelData.of(modelData, 30, 30);
   }

   public void setAngles(float time) {
      this.wing.pitch = (float)Math.toRadians(-80.0) - (float)Math.cos(time) * 0.2F;
      this.wing.yaw = (float)Math.toRadians(20.0) + (float)Math.sin(time) * 0.4F;
      this.wing.roll = (float)Math.toRadians(20.0);
      this.wingTip.roll = -((float)(Math.sin(time + 2.0F) + 0.5)) * 0.75F;
   }

   public void setAngles(PlayerEntityRenderState state) {
   }
}
