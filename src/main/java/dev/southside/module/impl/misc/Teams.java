package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_268;
import net.minecraft.class_640;
import org.jetbrains.annotations.Nullable;

public class Teams extends Module {
   public static Dropdown mode = new Dropdown("Mode", "Color", "Color", "Scoreboard");

   public Teams() {
      super(Category.Misc, "Teams", "队伍");
   }

   public static Teams.Mode getMode() {
      String var0 = mode.getMode();
      byte var1 = -1;
      switch (var0.hashCode()) {
         case 65290051:
            if (var0.equals("Color")) {
               var1 = 0;
            }
         default:
            return switch (var1) {
               case 0 -> Teams.Mode.COLOR;
               default -> Teams.Mode.SCOREBOARD;
            };
      }
   }

   public static boolean isTeam(class_1297 target) {
      if (!(target instanceof class_1657)) {
         return false;
      } else if (mc.field_1724 == null) {
         return false;
      } else if (!Southside.INSTANCE.getModuleManager().isEnabled(Teams.class)) {
         return false;
      } else if (getMode() == Teams.Mode.COLOR) {
         int c1 = target.method_22861();
         int c2 = mc.field_1724.method_22861();
         return c1 == c2;
      } else {
         String t1 = getTeam(target);
         String t2 = getTeam(mc.field_1724);
         return Objects.equals(t1, t2);
      }
   }

   @Nullable
   public static String getTeam(class_1297 entity) {
      if (!(entity instanceof class_1657)) {
         return null;
      } else if (mc.method_1562() == null) {
         return null;
      } else {
         class_640 entry = mc.method_1562().method_2871(entity.method_5667());
         if (entry == null) {
            return null;
         } else {
            class_268 team = entry.method_2955();
            return team != null ? team.method_1197() : null;
         }
      }
   }

   public static enum Mode {
      SCOREBOARD,
      COLOR;
   }
}
