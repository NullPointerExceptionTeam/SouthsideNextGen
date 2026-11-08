package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.math.AimMath;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.BoundBoxUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_2879;

public class AutoCrystal extends Module {
   public Slider range = new Slider("Range", 3.0, 1.0, 8.0, 0.01);

   public AutoCrystal() {
      super(Category.Combat, "AutoCrystal", "自动打水晶(无搜索/放置)");
   }

   @EventHandler(
      priority = 200
   )
   public void onRotation(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (entity instanceof class_1511) {
               class_243 vec3d = BoundBoxUtils.getMinDistanceVec3dByBox(mc.field_1724.method_33571(), entity.method_5829());
               if (mc.field_1724.method_33571().method_1022(vec3d) <= this.range.getDouble()) {
                  Rotation rotation = AimMath.toRotation(AimMath.computeBestAimToEntity(mc.field_1724, entity, 1.0F));
                  if (ClientRayTraceUtil.didHitEntity(rotation.yaw, rotation.pitch, 3.0, entity)) {
                     RotationUtils.setRotation(
                        RotationUtils.getRotationByVector(mc.field_1724.method_33571(), vec3d, false), this.rotationPriority(), MoveFixMode.Silent
                     );
                     PacketUtils.sendPacketNoEvent(class_2824.method_34206(entity, mc.field_1724.method_5715()));
                     PacketUtils.sendPacketNoEvent(new class_2879(class_1268.field_5808));
                  }
                  break;
               }
            }
         }
      }
   }

   @Override
   public int rotationPriority() {
      return 2 + this.extraRotationPriority();
   }

   @Override
   public int extraRotationPriority() {
      return 500;
   }
}
