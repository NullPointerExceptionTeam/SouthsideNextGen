package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AntiBot extends Module {
   public static final Switch checkShop = new Switch("Check Shop", true);
   public static final Switch checkInvisible = new Switch("Check Invisible", true);
   public static final Switch checkLatestBot = new Switch("Check LatestBot", true);
   private static final List<Entity> exclude = new ArrayList<>();

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

   public static boolean isBot(Entity e) {
      if (!Southside.INSTANCE.getModuleManager().isEnabled(AntiBot.class)) {
         return false;
      } else if (e instanceof PlayerEntity player) {
         if (player.getGameProfile().getName().isEmpty() && checkShop.getValue()) {
            return true;
         } else {
            if (checkLatestBot.getValue()) {
               if (exclude.contains(e)) {
                  return false;
               }

               boolean noArmor = true;

               for (ItemStack armorStack : player.getInventory().armor) {
                  if (!armorStack.isEmpty()) {
                     noArmor = false;
                     break;
                  }
               }

               boolean emptyHands = player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty();
               if (noArmor && emptyHands) {
                  float yaw = Math.abs(MathHelper.wrapDegrees(player.getYaw()));
                  boolean facingCardinal = yaw <= 0.0F || Math.abs(yaw - 90.0F) <= 0.0F || Math.abs(yaw - 180.0F) <= 0.0F;
                  if (facingCardinal) {
                     return true;
                  }

                  exclude.add(e);
               }
            }

            return !player.isAlive() || player.isInvisible() || player.getHealth() == 0.0F;
         }
      } else {
         return true;
      }
   }
}
