package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class AddEntityEvent {
   public Entity entity;
   public World world;

   @Generated
   public AddEntityEvent(Entity entity, World world) {
      this.entity = entity;
      this.world = world;
   }
}
