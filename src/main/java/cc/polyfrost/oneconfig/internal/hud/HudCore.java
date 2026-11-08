package cc.polyfrost.oneconfig.internal.hud;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.options.impl.HUD;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.southside.event.events.Render2DEvent;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class HudCore {
   public static final ConcurrentHashMap<HUD, Hud> huds = new ConcurrentHashMap<>();
   public static final ArrayList<BasicOption> hudOptions = new ArrayList<>();
   public static boolean editing = false;

   @EventHandler
   public void onRender(Render2DEvent event) {
      UMatrixStack matrices = new UMatrixStack();
      if (!editing) {
         for (Hud hud : huds.values()) {
            if (hud.isEnabled()) {
               hud.deltaTicks = event.getRenderTick();
               hud.drawAll(event.getDrawContext(), matrices, false);
            }
         }
      }
   }

   public static void reInitHuds() {
      for (HUD option : huds.keySet()) {
         if (option != null && option.getValue() != null) {
            Hud oldHud = huds.get(option);
            Hud newHud = option.getValue();
            newHud.setConfig(oldHud.getConfig());

            for (BasicOption basicOption : hudOptions) {
               if (basicOption.getParent() != null && basicOption.getParent().equals(oldHud)) {
                  basicOption.setParent(newHud);
               }
            }

            huds.put(option, newHud);
         }
      }
   }
}
