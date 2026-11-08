package dev.southside.module.impl.client;

import dev.southside.Southside;
import dev.southside.event.events.RawMouseEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.FriendManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.StringUtils;
import dev.southside.utils.render.animation.TimerUtil;
import net.minecraft.class_1657;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class MCF extends Module {
   private final TimerUtil timer = new TimerUtil();
   private boolean down;

   public MCF() {
      super(Category.Client, "MCF", "鼠标中键添加Friend");
   }

   @EventHandler
   public void onRawMouse(RawMouseEvent event) {
      if (event.getAction() == 0) {
         this.down = event.getButton() == 2;
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.method_1569() && this.down) {
         if (this.timer.hasReached(200.0)) {
            if ((!Southside.get().getModuleManager().isEnabled(KillAura.class) || KillAura.target == null)
               && mc.field_1765 != null
               && mc.field_1765.method_17783() == class_240.field_1331
               && mc.field_1765 instanceof class_3966 entityHitResult
               && entityHitResult.method_17782() instanceof class_1657 player) {
               String name = StringUtils.stripControlCodes(player.method_5477().getString());
               FriendManager friendManager = Southside.get().getFriendManager();
               if (friendManager.isFriend(name)) {
                  friendManager.remove(name);
                  this.info("Removed " + name + " from friends", new Object[0]);
               } else {
                  friendManager.add(name);
                  this.info("Added " + name + " to friends", new Object[0]);
               }
            }

            this.timer.reset();
         }

         this.down = false;
      }
   }
}
