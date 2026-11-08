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
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1674;

public class AntiFireball extends Module {
   private final Switch antikb = new Switch("Anti Velocity", false);
   private final Switch ignoreSelf = new Switch("Ignore Self", true);
   private boolean velocity = false;

   public AntiFireball() {
      super(Category.Combat, "AntiFireball", "Prevents fireballs from damaging you");
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!Southside.INSTANCE.getModuleManager().get(LongJump.class).isEnabled() && event.getState() == Stage.PRE) {
            Stream<class_1297> stream = StreamSupport.stream(mc.field_1687.method_18112().spliterator(), true);
            Optional<class_1674> fireball = stream.filter(
                  entityx -> entityx instanceof class_1674
                     && mc.field_1724.method_5739(entityx) < 6.0F
                     && (!this.ignoreSelf.getValue() || ((class_1674)entityx).method_24921() != mc.field_1724)
               )
               .map(entityx -> (class_1674)entityx)
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
               class_1674 entity = fireball.get();
               if (mc.field_1761 != null) {
                  mc.field_1761.method_2918(mc.field_1724, entity);
                  mc.field_1724.method_6104(class_1268.field_5808);
               }
            }
         }
      }
   }
}
