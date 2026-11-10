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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

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
      if (mc.isWindowFocused() && this.down) {
         if (this.timer.hasReached(200.0)) {
            if ((!Southside.get().getModuleManager().isEnabled(KillAura.class) || KillAura.target == null)
               && mc.crosshairTarget != null
               && mc.crosshairTarget.getType() == Type.ENTITY
               && mc.crosshairTarget instanceof EntityHitResult entityHitResult
               && entityHitResult.getEntity() instanceof PlayerEntity player) {
               String name = StringUtils.stripControlCodes(player.getName().getString());
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
