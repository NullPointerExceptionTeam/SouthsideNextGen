package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.entity.Entity.RemovalReason;

public class RemoveEntityEvent {
   public Entity entity;
   public RemovalReason reason;
   public World world;

   @Generated
   public RemoveEntityEvent(Entity entity, RemovalReason reason, World world) {
      this.entity = entity;
      this.reason = reason;
      this.world = world;
   }
}
