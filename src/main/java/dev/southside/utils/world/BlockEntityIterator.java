package dev.southside.utils.world;

import dev.southside.mixin.accessors.ChunkAccessor;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2791;

public class BlockEntityIterator implements Iterator<class_2586> {
   private final Iterator<class_2791> chunks = new ChunkIterator(false);
   private Iterator<class_2586> blockEntities;

   public BlockEntityIterator() {
      this.nextChunk();
   }

   private void nextChunk() {
      while (this.chunks.hasNext()) {
         Map<class_2338, class_2586> blockEntityMap = ((ChunkAccessor)this.chunks.next()).getBlockEntities();
         if (!blockEntityMap.isEmpty()) {
            this.blockEntities = blockEntityMap.values().iterator();
            break;
         }
      }
   }

   @Override
   public boolean hasNext() {
      if (this.blockEntities == null) {
         return false;
      } else if (this.blockEntities.hasNext()) {
         return true;
      } else {
         this.nextChunk();
         return this.blockEntities.hasNext();
      }
   }

   public class_2586 next() {
      return this.blockEntities.next();
   }
}
