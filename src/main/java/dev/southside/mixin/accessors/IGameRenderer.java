package dev.southside.mixin.accessors;

import net.minecraft.util.Identifier;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({GameRenderer.class})
public interface IGameRenderer {
   @Invoker("setPostProcessor")
   void invokeLoadPostProcessor(Identifier var1);

   @Invoker("clearPostProcessor")
   void invokeClearPostProcessor();
}
