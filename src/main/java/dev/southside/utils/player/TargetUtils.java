package dev.southside.utils.player;

import dev.southside.Southside;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;

public class TargetUtils {
   public static boolean isTarget(class_1297 entity, boolean targetCheck, boolean teamCheck) {
      if (entity instanceof class_1309 livingEntity) {
         if (entity == class_310.method_1551().field_1724) {
            return false;
         } else {
            boolean targetResult = !targetCheck || Targets.check(entity);
            if (!targetResult) {
               return false;
            } else if (entity instanceof class_1657) {
               if (teamCheck && Teams.isTeam(livingEntity)) {
                  return false;
               } else {
                  return Southside.get().getFriendManager().isFriend(livingEntity.method_5477().getString()) ? false : !AntiBot.isBot(livingEntity);
               }
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static List<class_1297> getTargets(float range) {
      Iterable<class_1297> entities = class_310.method_1551().field_1687.method_18112();
      ArrayList<class_1297> targets = new ArrayList<>();

      for (class_1297 entity : entities) {
         if (isTarget(entity, true, true) && entity.method_5739(class_310.method_1551().field_1724) <= range) {
            targets.add(entity);
         }
      }

      targets.sort(
         (entityx, entity2) -> (int)(entityx.method_5739(class_310.method_1551().field_1724) - entity2.method_5739(class_310.method_1551().field_1724))
      );
      return targets;
   }

   public static List<class_1297> getTargets(float minranfg, float maxrange) {
      Iterable<class_1297> entities = class_310.method_1551().field_1687.method_18112();
      ArrayList<class_1297> targets = new ArrayList<>();

      for (class_1297 entity : entities) {
         if (isTarget(entity, true, true)
            && entity.method_5739(class_310.method_1551().field_1724) >= minranfg
            && entity.method_5739(class_310.method_1551().field_1724) <= maxrange) {
            targets.add(entity);
         }
      }

      targets.sort(
         (entityx, entity2) -> (int)(entityx.method_5739(class_310.method_1551().field_1724) - entity2.method_5739(class_310.method_1551().field_1724))
      );
      return targets;
   }
}
