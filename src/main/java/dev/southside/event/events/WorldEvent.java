package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.client.world.ClientWorld;

public class WorldEvent {
   private ClientWorld world;

   @Generated
   public ClientWorld getWorld() {
      return this.world;
   }

   @Generated
   public WorldEvent(ClientWorld world) {
      this.world = world;
   }
}
