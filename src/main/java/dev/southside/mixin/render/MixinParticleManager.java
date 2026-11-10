package dev.southside.mixin.render;

import dev.southside.Southside;
import dev.southside.module.impl.render.NoRender;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.FireworksSparkParticle.Flash;
import net.minecraft.client.particle.FireworksSparkParticle.FireworkParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ParticleManager.class})
public class MixinParticleManager {
   @Inject(
      at = {@At("HEAD")},
      method = {"addParticle(Lnet/minecraft/client/particle/Particle;)V"},
      cancellable = true
   )
   public void addParticleHook(Particle p, CallbackInfo e) {
      NoRender noRender = Southside.get().getModuleManager().get(NoRender.class);
      if (noRender.isEnabled()) {
         if (noRender.explosions.getValue() && p instanceof ExplosionLargeParticle) {
            e.cancel();
         }

         if (noRender.breakParticles.getValue() && p instanceof BlockDustParticle) {
            e.cancel();
         }

         if (noRender.fireworks.getValue() && (p instanceof FireworkParticle || p instanceof Flash)) {
            e.cancel();
         }
      }
   }
}
