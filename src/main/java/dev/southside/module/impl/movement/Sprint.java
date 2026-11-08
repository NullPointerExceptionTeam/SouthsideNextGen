package dev.southside.module.impl.movement;

import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;

public class Sprint extends Module {
   public Sprint() {
      super(Category.Movement, "Sprint", "自动疾跑");
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (!event.getState().equals(Stage.POST)) {
         mc.field_1690.field_1867.method_23481(true);
         mc.field_1690.field_21333.method_41748(false);
      }
   }
}
