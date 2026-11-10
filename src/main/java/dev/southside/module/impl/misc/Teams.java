package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.client.network.PlayerListEntry;
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

   public static boolean isTeam(Entity target) {
      if (!(target instanceof PlayerEntity)) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if (!Southside.INSTANCE.getModuleManager().isEnabled(Teams.class)) {
         return false;
      } else if (getMode() == Teams.Mode.COLOR) {
         int c1 = target.getTeamColorValue();
         int c2 = mc.player.getTeamColorValue();
         return c1 == c2;
      } else {
         String t1 = getTeam(target);
         String t2 = getTeam(mc.player);
         return Objects.equals(t1, t2);
      }
   }

   @Nullable
   public static String getTeam(Entity entity) {
      if (!(entity instanceof PlayerEntity)) {
         return null;
      } else if (mc.getNetworkHandler() == null) {
         return null;
      } else {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         if (entry == null) {
            return null;
         } else {
            Team team = entry.getScoreboardTeam();
            return team != null ? team.getName() : null;
         }
      }
   }

   public static enum Mode {
      SCOREBOARD,
      COLOR;
   }
}
