package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import net.minecraft.class_10039;
import net.minecraft.class_4587;
import net.minecraft.class_4597;

public class RenderItemEntityEvent extends CancellableEvent {
   private static final RenderItemEntityEvent INSTANCE = new RenderItemEntityEvent();
   public class_10039 renderState;
   public class_4587 matrixStack;
   public class_4597 vertexConsumerProvider;
   public int light;

   public static RenderItemEntityEvent get(class_10039 renderState, class_4587 matrixStack, class_4597 vertexConsumerProvider, int light) {
      INSTANCE.setCancelled(false);
      INSTANCE.renderState = renderState;
      INSTANCE.matrixStack = matrixStack;
      INSTANCE.vertexConsumerProvider = vertexConsumerProvider;
      INSTANCE.light = light;
      return INSTANCE;
   }
}
