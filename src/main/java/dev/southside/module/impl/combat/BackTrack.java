package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.Southside;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.awt.Color;
import net.minecraft.class_745;

public class BackTrack extends Module {
   private boolean lastTracking = false;

   public BackTrack() {
      super(Category.Combat, "BackTrack");
   }

   @EventHandler
   public void onRotation(RotationAppliedEvent event) {
      if (KillAura.target instanceof class_745 player
         && mc.field_1724 != null
         && mc.field_1724.method_19538().method_1022(player.method_19538()) <= 6.0
         && Southside.INSTANCE.getModuleManager().get(KillAura.class).inRaycast(KillAura.target)) {
         if (!this.lastTracking) {
            ServerPacketManager.setup();
            this.lastTracking = true;
         } else if (ServerPacketManager.deSyncTick > 20 && !Velocity.gotKB) {
            ServerPacketManager.releaseTick(true);
         }
      } else if (this.lastTracking) {
         this.lastTracking = false;
         this.releaseNonVelocity();
      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (this.lastTracking && ServerPacketManager.deSyncing) {
         NanoVGHelper instance = NanoVGHelper.INSTANCE;
         instance.setupAndDraw(true, vg -> {
            String text = "Tracking Ticks: " + ServerPacketManager.deSyncTick;
            float textWidth = instance.getTextWidth(vg, text, 8.0F, Fonts.REGULAR);
            float x = (mc.method_22683().method_4486() - textWidth) / 2.0F;
            float y = mc.method_22683().method_4502() / 2.0F + 50.0F;
            instance.drawTextWithFormatting(vg, text, x, y, Color.YELLOW.getRGB(), 8.0F, Fonts.REGULAR);
         });
      }
   }

   public void releaseNonVelocity() {
      if (!Velocity.gotKB) {
         ServerPacketManager.reset();
      }
   }
}
