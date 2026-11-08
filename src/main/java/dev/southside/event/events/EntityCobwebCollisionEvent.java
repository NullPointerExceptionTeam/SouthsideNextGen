package dev.southside.event.events;

import dev.southside.event.CancellableEvent;
import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

public class EntityCobwebCollisionEvent extends CancellableEvent {
   public class_2680 state;
   public class_1937 world;
   public class_2338 pos;
   public class_1297 entity;

   @Generated
   public class_2680 getState() {
      return this.state;
   }

   @Generated
   public class_1937 getWorld() {
      return this.world;
   }

   @Generated
   public class_2338 getPos() {
      return this.pos;
   }

   @Generated
   public class_1297 getEntity() {
      return this.entity;
   }

   @Generated
   public void setState(class_2680 state) {
      this.state = state;
   }

   @Generated
   public void setWorld(class_1937 world) {
      this.world = world;
   }

   @Generated
   public void setPos(class_2338 pos) {
      this.pos = pos;
   }

   @Generated
   public void setEntity(class_1297 entity) {
      this.entity = entity;
   }

   @Generated
   public EntityCobwebCollisionEvent(class_2680 state, class_1937 world, class_2338 pos, class_1297 entity) {
      this.state = state;
      this.world = world;
      this.pos = pos;
      this.entity = entity;
   }
}
