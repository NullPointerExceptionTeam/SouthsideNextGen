package dev.southside.utils.player;

import dev.southside.Southside;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;

public class TargetUtils {
   public static boolean isTarget(Entity entity, boolean targetCheck, boolean teamCheck) {
      if (entity instanceof LivingEntity livingEntity) {
         if (entity == MinecraftClient.getInstance().player) {
            return false;
         } else {
            boolean targetResult = !targetCheck || Targets.check(entity);
            if (!targetResult) {
               return false;
            } else if (entity instanceof PlayerEntity) {
               if (teamCheck && Teams.isTeam(livingEntity)) {
                  return false;
               } else {
                  return Southside.get().getFriendManager().isFriend(livingEntity.getName().getString()) ? false : !AntiBot.isBot(livingEntity);
               }
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public static List<Entity> getTargets(float range) {
      Iterable<Entity> entities = MinecraftClient.getInstance().world.getEntities();
      ArrayList<Entity> targets = new ArrayList<>();

      for (Entity entity : entities) {
         if (isTarget(entity, true, true) && entity.distanceTo(MinecraftClient.getInstance().player) <= range) {
            targets.add(entity);
         }
      }

      targets.sort(
         (entityx, entity2) -> (int)(entityx.distanceTo(MinecraftClient.getInstance().player) - entity2.distanceTo(MinecraftClient.getInstance().player))
      );
      return targets;
   }

   public static List<Entity> getTargets(float minranfg, float maxrange) {
      Iterable<Entity> entities = MinecraftClient.getInstance().world.getEntities();
      ArrayList<Entity> targets = new ArrayList<>();

      for (Entity entity : entities) {
         if (isTarget(entity, true, true)
            && entity.distanceTo(MinecraftClient.getInstance().player) >= minranfg
            && entity.distanceTo(MinecraftClient.getInstance().player) <= maxrange) {
            targets.add(entity);
         }
      }

      targets.sort(
         (entityx, entity2) -> (int)(entityx.distanceTo(MinecraftClient.getInstance().player) - entity2.distanceTo(MinecraftClient.getInstance().player))
      );
      return targets;
   }
}
