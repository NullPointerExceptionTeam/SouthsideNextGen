package dev.southside.mixin.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({WorldRenderer.class})
public class MixinWorldRenderer {
   @Final
   @Shadow
   private MinecraftClient client;
   @Shadow
   private ClientWorld world;

   @Overwrite
   private ParticlesMode getRandomParticleSpawnChance(boolean canSpawnOnMinimal) {
      if (this.client != null && this.world != null) {
         ParticlesMode particlesMode = (ParticlesMode)this.client.options.getParticles().getValue();
         if (canSpawnOnMinimal && particlesMode == ParticlesMode.MINIMAL && this.world.random.nextInt(10) == 0) {
            particlesMode = ParticlesMode.DECREASED;
         }

         if (particlesMode == ParticlesMode.DECREASED && this.world.random.nextInt(3) == 0) {
            particlesMode = ParticlesMode.MINIMAL;
         }

         return particlesMode;
      } else {
         return ParticlesMode.MINIMAL;
      }
   }
}
