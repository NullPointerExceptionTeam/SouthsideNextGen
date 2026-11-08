package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_1297.class_5529;

public class RemoveEntityEvent {
   public class_1297 entity;
   public class_5529 reason;
   public class_1937 world;

   @Generated
   public RemoveEntityEvent(class_1297 entity, class_5529 reason, class_1937 world) {
      this.entity = entity;
      this.reason = reason;
      this.world = world;
   }
}
