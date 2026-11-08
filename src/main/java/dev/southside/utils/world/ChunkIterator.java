package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import dev.southside.mixin.accessors.ClientChunkManagerAccessor;
import dev.southside.mixin.accessors.ClientChunkMapAccessor;
import java.util.Iterator;
import net.minecraft.class_2791;

public class ChunkIterator implements Iterator<class_2791> {
   private final ClientChunkMapAccessor map = (ClientChunkMapAccessor)(Object)((ClientChunkManagerAccessor)MinecraftAccessor.mc.field_1687.method_2935()).getChunks();
   private final boolean onlyWithLoadedNeighbours;
   private int i = 0;
   private class_2791 chunk;

   public ChunkIterator(boolean onlyWithLoadedNeighbours) {
      this.onlyWithLoadedNeighbours = onlyWithLoadedNeighbours;
      this.getNext();
   }

   private class_2791 getNext() {
      class_2791 prev = this.chunk;
      this.chunk = null;

      while (this.i < this.map.getChunks().length()) {
         this.chunk = (class_2791)this.map.getChunks().get(this.i++);
         if (this.chunk != null && (!this.onlyWithLoadedNeighbours || this.isInRadius(this.chunk))) {
            break;
         }
      }

      return prev;
   }

   private boolean isInRadius(class_2791 chunk) {
      int x = chunk.method_12004().field_9181;
      int z = chunk.method_12004().field_9180;
      return MinecraftAccessor.mc.field_1687.method_2935().method_12123(x + 1, z)
         && MinecraftAccessor.mc.field_1687.method_2935().method_12123(x - 1, z)
         && MinecraftAccessor.mc.field_1687.method_2935().method_12123(x, z + 1)
         && MinecraftAccessor.mc.field_1687.method_2935().method_12123(x, z - 1);
   }

   @Override
   public boolean hasNext() {
      return this.chunk != null;
   }

   public class_2791 next() {
      return this.getNext();
   }
}
