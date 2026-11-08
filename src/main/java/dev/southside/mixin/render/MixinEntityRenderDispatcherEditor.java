package dev.southside.mixin.render;

import dev.southside.module.impl.world.EntityEditor;
import java.util.Map;
import net.minecraft.class_10007;
import net.minecraft.class_10010;
import net.minecraft.class_10013;
import net.minecraft.class_10037;
import net.minecraft.class_10042;
import net.minecraft.class_10053;
import net.minecraft.class_10063;
import net.minecraft.class_10066;
import net.minecraft.class_10085;
import net.minecraft.class_10086;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.minecraft.class_9064;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_898.class})
public abstract class MixinEntityRenderDispatcherEditor {
   @Shadow
   private Map<class_1299<?>, class_897<?, ?>> field_4696;
   @Shadow
   public class_4184 field_4686;

   @Shadow
   public abstract <T extends class_1297> class_897<? super T, ?> method_3953(T var1);

   @Inject(
      method = {"method_62424(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private <E extends class_1297> void onRenderEntity(
      E entity, double x, double y, double z, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light, CallbackInfo ci
   ) {
      if (EntityEditor.shouldReplace(entity)) {
         class_1299<?> targetType = EntityEditor.getTargetEntityType();
         class_897<?, ?> animalRenderer = this.field_4696.get(targetType);
         if (animalRenderer != null) {
            try {
               this.southside$renderAsAnimal(entity, x, y, z, tickDelta, matrices, vertexConsumers, light, animalRenderer);
               ci.cancel();
            } catch (Exception var16) {
            }
         }
      }
   }

   @Unique
   private void southside$renderAsAnimal(
      class_1297 entity, double x, double y, double z, float tickDelta, class_4587 matrices, class_4597 vertexConsumers, int light, class_897 animalRenderer
   ) {
      class_10042 state = this.southside$createAnimalState();
      state.field_53325 = class_3532.method_16436(tickDelta, entity.field_6038, entity.method_23317());
      state.field_53326 = class_3532.method_16436(tickDelta, entity.field_5971, entity.method_23318());
      state.field_53327 = class_3532.method_16436(tickDelta, entity.field_5989, entity.method_23321());
      state.field_53333 = entity.method_5767();
      state.field_53328 = entity.field_6012 + tickDelta;
      state.field_53329 = entity.method_17681();
      state.field_53330 = entity.method_17682();
      state.field_53331 = entity.method_5751();
      state.field_53332 = entity.method_5707(class_310.method_1551().field_1773.method_19418().method_19326());
      state.field_53334 = entity.method_21751();
      state.field_53335 = entity.method_5862();
      if (entity instanceof class_1309 living) {
         state.field_53446 = class_3532.method_17821(tickDelta, living.field_6220, living.field_6283);
         state.field_53447 = class_3532.method_17821(tickDelta, living.field_5982, living.method_36454());
         state.field_53448 = class_3532.method_16439(tickDelta, living.field_6004, living.method_36455());
         state.field_53453 = living.method_55693();
         state.field_53454 = 1.0F;
         state.field_53457 = living.method_6109();
         state.field_53460 = living.field_6235 > 0;
         state.field_53449 = living.field_6213;
         state.field_53450 = living.field_42108.method_48570(tickDelta);
         state.field_53451 = living.field_42108.method_48572(tickDelta);
         state.field_53458 = living.method_5799();
      } else {
         state.field_53446 = class_3532.method_17821(tickDelta, entity.field_5982, entity.method_36454());
         state.field_53447 = state.field_53446;
         state.field_53448 = class_3532.method_16439(tickDelta, entity.field_6004, entity.method_36455());
         state.field_53453 = 1.0F;
         state.field_53454 = 1.0F;
      }

      if (entity instanceof class_1657 player) {
         String name = player.method_7334() != null ? player.method_7334().getName() : player.method_5476().getString();
         state.field_53337 = class_2561.method_43470(name);
      } else {
         state.field_53337 = entity.method_5864().method_5897();
      }

      class_243 namePos = entity.method_56072().method_55675(class_9064.field_47745, 0, entity.method_61415(tickDelta));
      state.field_53338 = namePos;
      matrices.method_22903();

      try {
         class_243 posOffset = animalRenderer.method_23169(state);
         double dx = x + posOffset.method_10216();
         double dy = y + posOffset.method_10214();
         double dz = z + posOffset.method_10215();
         matrices.method_22904(dx, dy, dz);
         animalRenderer.method_3936(state, matrices, vertexConsumers, light);
         matrices.method_22904(-posOffset.method_10216(), -posOffset.method_10214(), -posOffset.method_10215());
      } finally {
         matrices.method_22909();
      }
   }

   @Unique
   private class_10042 southside$createAnimalState() {
      int modelIndex = EntityEditor.getModelIndex();

      return (class_10042)(switch (modelIndex) {
         case 1 -> new class_10063();
         case 2 -> new class_10085();
         case 3 -> new class_10007();
         case 4 -> new class_10013();
         case 5 -> new class_10010();
         case 6 -> new class_10066();
         case 7 -> new class_10086();
         case 8 -> new class_10042();
         case 9 -> new class_10037();
         default -> new class_10053();
      });
   }
}
