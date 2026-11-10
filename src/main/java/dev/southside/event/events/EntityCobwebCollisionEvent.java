package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

public class EntityCobwebCollisionEvent extends CancellableEvent {
   public BlockState state;
   public World world;
   public BlockPos pos;
   public Entity entity;

   @Generated
   public BlockState getState() {
      return this.state;
   }

   @Generated
   public World getWorld() {
      return this.world;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public void setState(BlockState state) {
      this.state = state;
   }

   @Generated
   public void setWorld(World world) {
      this.world = world;
   }

   @Generated
   public void setPos(BlockPos pos) {
      this.pos = pos;
   }

   @Generated
   public void setEntity(Entity entity) {
      this.entity = entity;
   }

   @Generated
   public EntityCobwebCollisionEvent(BlockState state, World world, BlockPos pos, Entity entity) {
      this.state = state;
      this.world = world;
      this.pos = pos;
      this.entity = entity;
   }
}
