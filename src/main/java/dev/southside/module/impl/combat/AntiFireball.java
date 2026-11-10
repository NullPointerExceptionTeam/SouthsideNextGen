package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.orbit.EventHandler;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;

public class AntiFireball extends Module {
   private final Switch antikb = new Switch("Anti Velocity", false);
   private final Switch ignoreSelf = new Switch("Ignore Self", true);
   private boolean velocity = false;

   public AntiFireball() {
      super(Category.Combat, "AntiFireball", "Prevents fireballs from damaging you");
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.player != null && mc.world != null) {
         if (!Southside.INSTANCE.getModuleManager().get(LongJump.class).isEnabled() && event.getState() == Stage.PRE) {
            Stream<Entity> stream = StreamSupport.stream(mc.world.getEntities().spliterator(), true);
            Optional<FireballEntity> fireball = stream.filter(
                  entityx -> entityx instanceof FireballEntity
                     && mc.player.distanceTo(entityx) < 6.0F
                     && (!this.ignoreSelf.getValue() || ((FireballEntity)entityx).getOwner() != mc.player)
               )
               .map(entityx -> (FireballEntity)entityx)
               .findFirst();
            boolean isKb = Southside.INSTANCE.getModuleManager().get(Velocity.class).isEnabled();
            if (fireball.isEmpty()) {
               if (isKb && this.velocity) {
                  Southside.INSTANCE.getModuleManager().get(Velocity.class).setEnable(false);
                  this.velocity = false;
               }

               return;
            }

            if (this.antikb.getValue()) {
               if (!isKb) {
                  Southside.INSTANCE.getModuleManager().get(Velocity.class).setEnable(true);
                  this.velocity = true;
               }
            } else {
               FireballEntity entity = fireball.get();
               if (mc.interactionManager != null) {
                  mc.interactionManager.attackEntity(mc.player, entity);
                  mc.player.swingHand(Hand.MAIN_HAND);
               }
            }
         }
      }
   }
}
