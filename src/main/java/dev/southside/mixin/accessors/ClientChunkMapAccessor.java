package dev.southside.mixin.accessors;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.world.ClientChunkManager.ClientChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ClientChunkMap.class})
public interface ClientChunkMapAccessor {
   @Accessor("chunks")
   AtomicReferenceArray<WorldChunk> getChunks();
}
