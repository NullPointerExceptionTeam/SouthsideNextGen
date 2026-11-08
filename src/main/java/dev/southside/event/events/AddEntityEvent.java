package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_1937;

public class AddEntityEvent {
   public class_1297 entity;
   public class_1937 world;

   @Generated
   public AddEntityEvent(class_1297 entity, class_1937 world) {
      this.entity = entity;
      this.world = world;
   }
}
