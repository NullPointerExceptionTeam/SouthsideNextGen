package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;

public class RenderItemEntityEvent extends CancellableEvent {
   private static final RenderItemEntityEvent INSTANCE = new RenderItemEntityEvent();
   public ItemEntityRenderState renderState;
   public MatrixStack matrixStack;
   public VertexConsumerProvider vertexConsumerProvider;
   public int light;

   public static RenderItemEntityEvent get(ItemEntityRenderState renderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
      INSTANCE.setCancelled(false);
      INSTANCE.renderState = renderState;
      INSTANCE.matrixStack = matrixStack;
      INSTANCE.vertexConsumerProvider = vertexConsumerProvider;
      INSTANCE.light = light;
      return INSTANCE;
   }
}
