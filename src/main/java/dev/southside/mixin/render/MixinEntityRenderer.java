package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.accessor.EntityRenderStateAccessor;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.render.LineBreakFormattor;
import dev.southside.module.impl.render.NameTag;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_897.class})
public abstract class MixinEntityRenderer<T extends class_1297, S extends class_10017> {
   @Shadow
   @Final
   protected class_898 field_4676;

   @Inject(
      method = {"method_62354(Lnet/minecraft/class_1297;Lnet/minecraft/class_10017;F)V"},
      at = {@At("TAIL")}
   )
   private void attachRenderStateEntity(T entity, S state, float tickDelta, CallbackInfo ci) {
      if (state instanceof EntityRenderStateAccessor accessor) {
         accessor.southside$setEntity(entity);
      }
   }

   @Inject(
      method = {"method_3926(Lnet/minecraft/class_10017;Lnet/minecraft/class_2561;Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderLabelIfPresent(S state, class_2561 text, class_4587 matrices, class_4597 vertexConsumers, int light, CallbackInfo ci) {
      ModuleManager moduleManager = Southside.get().getModuleManager();
      class_1297 entity = null;
      if (state instanceof EntityRenderStateAccessor accessor) {
         entity = accessor.southside$getEntity();
      }

      NoRender noRender = moduleManager.get(NoRender.class);
      if (entity instanceof class_1531 && noRender != null && noRender.isEnabled() && noRender.noArmorStands.getValue()) {
         ci.cancel();
      } else {
         NameTag nameTag = moduleManager.get(NameTag.class);
         if (nameTag != null && nameTag.isEnabled() && nameTag.hideVanilla.getValue() && entity instanceof class_1657) {
            ci.cancel();
         } else {
            LineBreakFormattor lineBreakFormattor = moduleManager.get(LineBreakFormattor.class);
            if (lineBreakFormattor != null && lineBreakFormattor.renderMultilineLabel(state, text, matrices, vertexConsumers, light)) {
               ci.cancel();
            }
         }
      }
   }
}
