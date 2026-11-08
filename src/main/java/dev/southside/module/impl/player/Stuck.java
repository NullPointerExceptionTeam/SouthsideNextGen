package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.MovementUtils;
import net.minecraft.class_2848;
import net.minecraft.class_2848.class_2849;

public class Stuck extends Module {
   public final Switch offScaffold = new Switch("Disable Scaffold", false);
   public final Switch infnite = new Switch("Infnite", false);

   public Stuck() {
      super(Category.Player, "Stuck", "你会被定住");
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.offScaffold.getValue() && Southside.INSTANCE != null) {
         Scaffold scaffold = Southside.INSTANCE.getModuleManager().get(Scaffold.class);
         if (scaffold.isEnabled()) {
            scaffold.setEnable(false);
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (PacketUtils.noMovePackets < 19 && !this.infnite.getValue()) {
            MovementUtils.cancelMove();
         }

         if (this.infnite.getValue()) {
            if (PacketUtils.noMovePackets % 60 == 0) {
               mc.method_1562().method_52787(new class_2848(mc.field_1724, class_2849.field_12982));
            }

            MovementUtils.cancelMove();
         }
      }
   }
}
