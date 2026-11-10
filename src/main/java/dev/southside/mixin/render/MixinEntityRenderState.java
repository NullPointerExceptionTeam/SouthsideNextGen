package dev.southside.mixin.render;

import dev.southside.accessor.EntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({EntityRenderState.class})
public class MixinEntityRenderState implements EntityRenderStateAccessor {
   @Unique
   private Entity southside$entity;

   @Override
   public Entity southside$getEntity() {
      return this.southside$entity;
   }

   @Override
   public void southside$setEntity(Entity entity) {
      this.southside$entity = entity;
   }
}
