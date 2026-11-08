package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.Southside;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.impl.world.sca.ScaffoldRotationHelper;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.player.BoundBoxUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2560;
import net.minecraft.class_3965;

public class AutoTrap extends Module {
   public final List<class_1792> TRAPS = List.of(class_1802.field_8786, class_1802.field_8187);
   public Slider range = new Slider("Range", 3.0, 3.0, 4.0, 0.01);
   public Slider reTicks = new Slider("LavaRetrieve", 30, 2, 30, 1);
   private final Map<class_2338, Integer> placedLava = new HashMap<>();
   public static List<class_1297> targets = new ArrayList<>();
   public static class_1297 target;

   public AutoTrap() {
      super(Category.Player, "AutoTrap", "我会trap别人。");
   }

   @EventHandler
   public void onTick(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (!Southside.get().getModuleManager().isEnabled(Scaffold.class)) {
            if (!Southside.get().getModuleManager().isEnabled(KillAura.class)) {
               this.handleLavaRetrieval();
               this.updateTargets();
               if (!targets.isEmpty()) {
                  targets.sort((o1, o2) -> (int)(o1.method_5739(mc.field_1724) - o2.method_5739(mc.field_1724)));
                  target = targets.get(0);
                  int targetSlot = -1;
                  if (target != null
                     && target.method_5805()
                     && !target.method_31481()
                     && mc.field_1724.method_5739(target) <= this.range.getValue().doubleValue()) {
                     for (int i = 0; i < 9; i++) {
                        if (this.TRAPS.contains(mc.field_1724.method_31548().method_5438(i).method_7909())) {
                           targetSlot = i;
                           break;
                        }
                     }
                  }

                  if (targetSlot == -1) {
                     return;
                  }

                  int originalSlot = mc.field_1724.method_31548().field_7545;
                  if (mc.field_1724.method_31548().field_7545 != targetSlot) {
                     mc.field_1724.method_31548().field_7545 = targetSlot;
                  }

                  class_2338 trapPos = target.method_24515().method_10074();
                  Rotation rot = ScaffoldRotationHelper.getRotation(trapPos, class_2350.field_11036);
                  if (mc.field_1724.method_31548().method_5438(targetSlot).method_7909() == class_1802.field_8786) {
                     RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(trapPos, class_2350.field_11036), this.rotationPriority());
                     class_243 hitVec = new class_243(trapPos.method_10263() + 0.5, trapPos.method_10264() + 1, trapPos.method_10260() + 0.5);
                     class_3965 hitResult = new class_3965(hitVec, class_2350.field_11036, trapPos, false);
                     mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hitResult);
                     mc.field_1724.method_6104(class_1268.field_5808);
                  } else {
                     RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(trapPos, class_2350.field_11036), this.rotationPriority());
                     mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
                     class_2338 lavaPos = trapPos.method_10084();
                     this.placedLava.put(lavaPos, 0);
                  }

                  mc.field_1724.method_31548().field_7545 = originalSlot;
               }
            }
         }
      }
   }

   private void handleLavaRetrieval() {
      if (!this.placedLava.isEmpty()) {
         List<class_2338> toRetrieve = new ArrayList<>();
         this.placedLava.entrySet().removeIf(entry -> {
            class_2338 pos = entry.getKey();
            int ticks = entry.getValue();
            if (mc.field_1687.method_8320(pos).method_26204() != class_2246.field_10164 || !mc.field_1687.method_8316(pos).method_15771()) {
               return true;
            } else if (ticks >= this.reTicks.getValue().intValue()) {
               toRetrieve.add(pos);
               return true;
            } else {
               entry.setValue(ticks + 1);
               return false;
            }
         });
         if (!toRetrieve.isEmpty()) {
            int emptyBucketSlot = -1;

            for (int i = 0; i < 9; i++) {
               if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8550) {
                  emptyBucketSlot = i;
                  break;
               }
            }

            if (emptyBucketSlot != -1) {
               class_2338 lavaPos = toRetrieve.get(0);
               int originalSlot = mc.field_1724.method_31548().field_7545;
               mc.field_1724.method_31548().field_7545 = emptyBucketSlot;
               float[] rotArray = RotationUtils.getRotations(lavaPos.method_46558());
               RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(lavaPos, class_2350.field_11036), 0);
               class_1269 result = mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               if (result.method_23665()) {
                  mc.field_1724.method_6104(class_1268.field_5808);
               }

               mc.field_1724.method_31548().field_7545 = originalSlot;
            }
         }
      }
   }

   public void updateTargets() {
      target = null;
      targets.clear();

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (TargetUtils.isTarget(entity, true, true)
            && BoundBoxUtils.getMinDistanceVec3dByBox(mc.field_1724.method_33571(), entity.method_5829()).method_1022(mc.field_1724.method_33571())
               <= this.range.getValue().doubleValue()
            && !entity.method_5771()
            && !(mc.field_1687.method_8320(entity.method_24515()).method_26204() instanceof class_2560)
            && mc.field_1687.method_8320(entity.method_24515().method_10074()).method_51367()) {
            targets.add(entity);
         }
      }
   }

   @Override
   public int rotationPriority() {
      return -2;
   }
}
