package dev.southside.event.events;

import lombok.Generated;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ChestOpenedEvent {
   private PlayerEntity opener;
   private BlockPos pos;

   @Generated
   public ChestOpenedEvent(PlayerEntity opener, BlockPos pos) {
      this.opener = opener;
      this.pos = pos;
   }

   @Generated
   public PlayerEntity getOpener() {
      return this.opener;
   }

   @Generated
   public BlockPos getPos() {
      return this.pos;
   }
}
