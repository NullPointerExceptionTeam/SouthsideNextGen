package dev.southside.mixin.accessors;

import net.minecraft.class_2960;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_757.class})
public interface IGameRenderer {
   @Invoker("method_62904")
   void invokeLoadPostProcessor(class_2960 var1);

   @Invoker("method_62905")
   void invokeClearPostProcessor();
}
