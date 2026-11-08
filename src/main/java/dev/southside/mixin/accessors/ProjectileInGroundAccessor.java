package dev.southside.mixin.accessors;

import net.minecraft.class_1665;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_1665.class})
public interface ProjectileInGroundAccessor {
   @Invoker("method_65059")
   boolean invokeIsInGround();
}
