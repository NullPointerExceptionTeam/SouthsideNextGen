package dev.southside.mixin.render;

import dev.southside.accessor.EntityRenderStateAccessor;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({class_10017.class})
public class MixinEntityRenderState implements EntityRenderStateAccessor {
   @Unique
   private class_1297 southside$entity;

   @Override
   public class_1297 southside$getEntity() {
      return this.southside$entity;
   }

   @Override
   public void southside$setEntity(class_1297 entity) {
      this.southside$entity = entity;
   }
}
