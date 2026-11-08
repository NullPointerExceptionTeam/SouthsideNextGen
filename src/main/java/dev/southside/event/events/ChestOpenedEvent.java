package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.class_1657;
import net.minecraft.class_2338;

public class ChestOpenedEvent {
   private class_1657 opener;
   private class_2338 pos;

   @Generated
   public ChestOpenedEvent(class_1657 opener, class_2338 pos) {
      this.opener = opener;
      this.pos = pos;
   }

   @Generated
   public class_1657 getOpener() {
      return this.opener;
   }

   @Generated
   public class_2338 getPos() {
      return this.pos;
   }
}
