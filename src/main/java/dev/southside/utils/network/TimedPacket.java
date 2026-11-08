package dev.southside.utils.network;

import lombok.Generated;
import net.minecraft.class_2596;

public class TimedPacket {
   private final class_2596 packet;
   private final long time;

   public TimedPacket(class_2596<?> packet) {
      this.packet = packet;
      this.time = System.currentTimeMillis();
   }

   @Generated
   public class_2596 getPacket() {
      return this.packet;
   }

   @Generated
   public long getTime() {
      return this.time;
   }
}
