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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

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
         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
               Vec3d vec3d = BoundBoxUtils.getMinDistanceVec3dByBox(mc.player.getEyePos(), entity.getBoundingBox());
               if (mc.player.getEyePos().distanceTo(vec3d) <= this.range.getDouble()) {
                  Rotation rotation = AimMath.toRotation(AimMath.computeBestAimToEntity(mc.player, entity, 1.0F));
                  if (ClientRayTraceUtil.didHitEntity(rotation.yaw, rotation.pitch, 3.0, entity)) {
                     RotationUtils.setRotation(
                        RotationUtils.getRotationByVector(mc.player.getEyePos(), vec3d, false), this.rotationPriority(), MoveFixMode.Silent
                     );
                     PacketUtils.sendPacketNoEvent(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                     PacketUtils.sendPacketNoEvent(new HandSwingC2SPacket(Hand.MAIN_HAND));
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
