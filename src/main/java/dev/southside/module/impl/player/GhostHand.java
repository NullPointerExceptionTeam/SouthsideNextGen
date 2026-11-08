package dev.southside.module.impl.player;

import dev.southside.event.events.ItemUseEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import net.minecraft.class_1268;
import net.minecraft.class_2281;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2680;
import net.minecraft.class_2885;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class GhostHand extends Module {
   private static final double MAX_REACH_DISTANCE = 4.5;

   public GhostHand() {
      super(Category.Player, "GhostHand");
   }

   @EventHandler
   public void onItemUse(ItemUseEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1765 != null && mc.field_1765.method_17783() == class_240.field_1332) {
            class_3965 hitResult = (class_3965)mc.field_1765;
            if (this.isChestBlock(mc.field_1687.method_8320(hitResult.method_17777()))) {
               return;
            }
         }

         class_3965 hitResult = this.raycastForChest();
         if (hitResult != null) {
            PacketUtils.sendSequencedPacket(id -> new class_2885(class_1268.field_5808, hitResult, id));
            event.setCancelled(true);
         }
      }
   }

   private class_3965 raycastForChest() {
      float tickDelta = mc.method_61966().method_60637(false);
      class_243 start = mc.field_1724.method_5836(tickDelta);
      class_243 direction = mc.field_1724.method_5828(tickDelta);
      class_243 end = start.method_1019(direction.method_1021(4.5));
      double stepSize = 0.1;
      int steps = (int)(4.5 / stepSize);
      class_243 step = direction.method_1021(stepSize);
      class_243 current = start;

      for (int i = 0; i <= steps; i++) {
         class_2338 pos = new class_2338((int)Math.floor(current.field_1352), (int)Math.floor(current.field_1351), (int)Math.floor(current.field_1350));
         class_2680 state = mc.field_1687.method_8320(pos);
         if (this.isChestBlock(state) && this.hasChestCollision(pos, current)) {
            return ClientRayTraceUtil.getFacedContainerBlock(mc.field_1724.method_36454(), mc.field_1724.method_36455());
         }

         current = current.method_1019(step);
      }

      return null;
   }

   private boolean isChestBlock(class_2680 state) {
      return state.method_26204() instanceof class_2281 || state.method_26204() instanceof class_2336 || state.method_26204() instanceof class_2480;
   }

   private boolean hasChestCollision(class_2338 pos, class_243 rayPos) {
      try {
         double minX = pos.method_10263() + 0.1;
         double minY = pos.method_10264() + 0.1;
         double minZ = pos.method_10260() + 0.1;
         double maxX = pos.method_10263() + 0.9;
         double maxY = pos.method_10264() + 0.9;
         double maxZ = pos.method_10260() + 0.9;
         return rayPos.field_1352 >= minX
            && rayPos.field_1352 <= maxX
            && rayPos.field_1351 >= minY
            && rayPos.field_1351 <= maxY
            && rayPos.field_1350 >= minZ
            && rayPos.field_1350 <= maxZ;
      } catch (Exception var15) {
         return false;
      }
   }
}
