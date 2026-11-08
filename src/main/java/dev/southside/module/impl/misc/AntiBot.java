package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3532;

public class AntiBot extends Module {
   public static final Switch checkShop = new Switch("Check Shop", true);
   public static final Switch checkInvisible = new Switch("Check Invisible", true);
   public static final Switch checkLatestBot = new Switch("Check LatestBot", true);
   private static final List<class_1297> exclude = new ArrayList<>();

   public AntiBot() {
      super(Category.Misc, "AntiBot");
   }

   @Override
   public void onDisable() {
      exclude.clear();
   }

   @EventHandler
   public void onWorld(WorldEvent e) {
      exclude.clear();
   }

   public static boolean isBot(class_1297 e) {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(AntiBot.class)) {
         return false;
      } else if (e instanceof class_1657 player) {
         if (player.method_7334().getName().isEmpty() && checkShop.getValue()) {
            return true;
         } else {
            if (checkLatestBot.getValue()) {
               if (exclude.contains(e)) {
                  return false;
               }

               boolean noArmor = true;

               for (class_1799 armorStack : player.method_31548().field_7548) {
                  if (!armorStack.method_7960()) {
                     noArmor = false;
                     break;
                  }
               }

               boolean emptyHands = player.method_6047().method_7960() && player.method_6079().method_7960();
               if (noArmor && emptyHands) {
                  float yaw = Math.abs(class_3532.method_15393(player.method_36454()));
                  boolean facingCardinal = yaw <= 0.0F || Math.abs(yaw - 90.0F) <= 0.0F || Math.abs(yaw - 180.0F) <= 0.0F;
                  if (facingCardinal) {
                     return true;
                  }

                  exclude.add(e);
               }
            }

            return !player.method_5805() || player.method_5767() || player.method_6032() == 0.0F;
         }
      } else {
         return true;
      }
   }
}
