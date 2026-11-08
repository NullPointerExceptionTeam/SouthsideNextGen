package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.player.BedwarsCheckUtils;
import dev.southside.utils.player.ReportUtils;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.class_1657;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class BedwarsHelper extends Module {
   private final Switch debug = new Switch("Debug", false);
   private final Switch autoInc = new Switch("AutoINC", false);
   private final Switch antiBreakBed = new Switch("AntiBreakBed", false);
   private final Set<String> incAlerted = new HashSet<>();
   private final Set<String> reportedPlayers = new HashSet<>();
   private static final int HORIZONTAL_RANGE = 3;

   public BedwarsHelper() {
      super(Category.Misc, "BedwarsHelper", "Bedwars helper");
   }

   @Override
   public void onDisable() {
      this.incAlerted.clear();
      this.reportedPlayers.clear();
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            boolean gaming = BedwarsCheckUtils.INSTANCE.isBedwarsGaming();
            class_2338 bed = BedwarsCheckUtils.INSTANCE.getSelfBed();
            if (gaming && bed != null) {
               if (this.autoInc.getValue()) {
                  for (class_1657 player : mc.field_1687.method_18456()) {
                     if (player != mc.field_1724 && !Teams.isTeam(player)) {
                        String name = player.method_7334().getName();
                        if (!this.incAlerted.contains(name) && player.method_24515().method_19771(bed, 16.0)) {
                           this.incAlerted.add(name);
                           mc.method_1562().method_45729("inc! " + name + " 来我们家了!");
                           if (this.debug.getValue()) {
                              ChatUtils.info("[AutoINC] " + name + " entered bed range");
                           }
                        }
                     }
                  }

                  this.incAlerted.removeIf(namex -> {
                     class_1657 playerx = mc.field_1687.method_18456().stream().filter(p -> p.method_7334().getName().equals(namex)).findFirst().orElse(null);
                     return playerx == null || !playerx.method_24515().method_19771(bed, 16.0);
                  });
               }

               if (this.antiBreakBed.getValue()) {
                  Set<class_2338> protectionBlocks = this.getProtectionBlocks(bed);

                  for (class_1657 playerx : mc.field_1687.method_18456()) {
                     if (playerx != mc.field_1724 && !Teams.isTeam(playerx)) {
                        String name = playerx.method_7334().getName();
                        if (!this.reportedPlayers.contains(name) && playerx.field_6252) {
                           class_2338 miningPos = this.getPlayerMiningBlock(playerx);
                           if (miningPos != null && protectionBlocks.contains(miningPos)) {
                              this.reportedPlayers.add(name);
                              if (this.debug.getValue()) {
                                 ChatUtils.info("[AntiBreakBed] " + name + " is breaking bed protection at " + miningPos.method_23854() + ", reporting...");
                              }

                              ReportUtils.report(name);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   private void onWorld(WorldEvent event) {
      this.incAlerted.clear();
      this.reportedPlayers.clear();
   }

   private Set<class_2338> getProtectionBlocks(class_2338 bedPos) {
      Set<class_2338> result = new LinkedHashSet<>();
      class_2680 bedState = mc.field_1687.method_8320(bedPos);
      if (!(bedState.method_26204() instanceof class_2244)) {
         return result;
      } else {
         class_2338 headPos;
         class_2338 footPos;
         if (bedState.method_11654(class_2244.field_9967) == class_2742.field_12560) {
            headPos = bedPos;
            class_2350 facing = (class_2350)bedState.method_11654(class_2244.field_11177);
            footPos = bedPos.method_10093(facing.method_10153());
         } else {
            footPos = bedPos;
            class_2350 facing = (class_2350)bedState.method_11654(class_2244.field_11177);
            headPos = bedPos.method_10093(facing);
         }

         Set<class_2338> bedBlocks = new LinkedHashSet<>();
         bedBlocks.add(headPos.method_10062());
         bedBlocks.add(footPos.method_10062());

         for (class_2338 base : bedBlocks) {
            class_2338 above = base.method_10084();
            if (!bedBlocks.contains(above)) {
               result.add(above.method_10062());
            }

            for (int dx = -3; dx <= 3; dx++) {
               for (int dz = -3; dz <= 3; dz++) {
                  if ((dx != 0 || dz != 0) && Math.max(Math.abs(dx), Math.abs(dz)) <= 3) {
                     class_2338 target = base.method_10069(dx, 0, dz);
                     if (!bedBlocks.contains(target)) {
                        result.add(target.method_10062());
                     }
                  }
               }
            }
         }

         result.removeIf(pos -> mc.field_1687.method_8320(pos).method_26215());
         return result;
      }
   }

   private class_2338 getPlayerMiningBlock(class_1657 player) {
      class_243 eyePos = player.method_33571();
      class_243 lookVec = player.method_5828(1.0F);
      class_243 endPos = eyePos.method_1019(lookVec.method_1021(4.5));
      class_3965 result = mc.field_1687.method_17742(new class_3959(eyePos, endPos, class_3960.field_17559, class_242.field_1348, player));
      return result.method_17783() == class_240.field_1332 ? result.method_17777() : null;
   }

   public static boolean isBedwarsGaming() {
      return BedwarsCheckUtils.INSTANCE.isBedwarsGaming();
   }

   public static class_2338 getSelfBed() {
      return BedwarsCheckUtils.INSTANCE.getSelfBed();
   }
}
