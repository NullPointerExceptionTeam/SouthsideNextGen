package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.player.StealerN;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.InvUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.Random;
import net.minecraft.class_1268;
import net.minecraft.class_2281;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2397;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2680;
import net.minecraft.class_2885;
import net.minecraft.class_3965;

public class ChestAura extends Module {
   private final Slider range = new Slider("Range", 4.0, 1.0, 8.0, 0.1);
   private final Slider minDelay = new Slider("Min Delay", 50, 0, 1000, 10);
   private final Slider maxDelay = new Slider("Max Delay", 150, 0, 1000, 10);
   private final Switch openStolen = new Switch("OpenStolenChest", false);
   private final Switch raycast = new Switch("RayCast", true);
   private final Switch pickEnderChest = new Switch("Ender Chest", false);
   private final Switch pauseOnMoveInput = new Switch("NoMove", false);
   private final MSTimer timer = new MSTimer();
   private final Random random = new Random();
   private long currentDelay = 0L;
   private class_2338 currentPos;
   public int delayTime = 0;
   private boolean moveInputActive = false;
   private Rotation rotation = null;

   public ChestAura() {
      super(Category.World, "ChestAura");
   }

   @Override
   public void onEnable() {
      this.timer.reset();
      this.generateRandomDelay();
      this.moveInputActive = false;
   }

   @Override
   public void onDisable() {
      this.moveInputActive = false;
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      this.delayTime = 10;
   }

   @EventHandler
   public void onMoveInput(MovementInputEvent event) {
      if (!this.pauseOnMoveInput.getValue()) {
         this.moveInputActive = false;
      } else {
         this.moveInputActive = event.isForward() || event.isBack() || event.isLeft() || event.isRight() || event.isJump() || event.isSneak();
      }
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.delayTime > 0) {
            this.delayTime--;
         } else if (this.timer.hasTimePassed(this.currentDelay)) {
            if ((KillAura.target == null || !Southside.get().getModuleManager().get(KillAura.class).isEnabled())
               && !mc.field_1724.method_6115()
               && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
               && !Southside.get().getModuleManager().get(Blink.class).isEnabled()
               && mc.field_1724.field_6235 == 0) {
               if (!(mc.field_1724.field_6017 > 3.0F) && !NoSlowTest.shouldEnable) {
                  if (mc.field_1755 == null && !InvUtils.isInventoryFull(mc.field_1724.method_31548())) {
                     if (!this.pauseOnMoveInput.getValue() || !this.moveInputActive) {
                        class_2338 chestPos = this.findNextChest();
                        if (chestPos != null) {
                           if (!Southside.INSTANCE.getModuleManager().get(StealerN.class).isEnabled()
                              || Southside.INSTANCE.getModuleManager().get(Scaffold.class).isEnabled()) {
                              return;
                           }

                           this.lookAtChest(chestPos);
                           this.currentPos = chestPos;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onRotationApplied(RotationAppliedEvent event) {
      if (this.currentPos != null) {
         this.openChestWithPacket(this.currentPos);
         this.moveInputActive = false;
         this.timer.reset();
         this.generateRandomDelay();
         this.currentPos = null;
      }
   }

   private void generateRandomDelay() {
      int min = this.minDelay.getValue().intValue();
      int max = this.maxDelay.getValue().intValue();
      if (min > max) {
         this.currentDelay = min;
      } else {
         this.currentDelay = min + this.random.nextInt(max - min + 1);
      }
   }

   private class_2338 findNextChest() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 playerPos = mc.field_1724.method_24515();
         class_243 playerVec = mc.field_1724.method_19538();
         int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
         class_2338 nearestChest = null;
         double nearestDistance = Double.MAX_VALUE;

         for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
               for (int z = -rangeInt; z <= rangeInt; z++) {
                  class_2338 pos = playerPos.method_10069(x, y, z);
                  double distance = playerVec.method_1022(class_243.method_24953(pos));
                  ChestManager manager = Southside.get().getChestManager();
                  if (!manager.isStolen(pos) && this.isChestBlock(pos) && !this.isBlockedByNonLeaf(pos) && distance < nearestDistance) {
                     nearestChest = pos;
                     nearestDistance = distance;
                  }
               }
            }
         }

         return nearestChest;
      } else {
         return null;
      }
   }

   private boolean isChestBlock(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 state = mc.field_1687.method_8320(pos);
         return state.method_26204() instanceof class_2281
            || state.method_26204() instanceof class_2336 && this.pickEnderChest.getValue()
            || state.method_26204() instanceof class_2480;
      }
   }

   private boolean isChestBlock(class_2680 state) {
      return state.method_26204() instanceof class_2281 || state.method_26204() instanceof class_2336 || state.method_26204() instanceof class_2480;
   }

   private boolean isBlockedByNonLeaf(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 aboveState = mc.field_1687.method_8320(pos.method_10084());
         return aboveState.method_26204() instanceof class_2397 ? false : ChestWreck.isChestBlocked(pos);
      }
   }

   private void lookAtChest(class_2338 chestPos) {
      if (mc.field_1724 != null) {
         Rotation tmp = RotationUtils.getRandomRotationTo(chestPos, class_2350.field_11036);
         if (ClientRayTraceUtil.didHitBlockFace(
            mc.field_1724, tmp.yaw, tmp.pitch, chestPos, class_2350.field_11036, false, blockState -> !this.isChestBlock(blockState)
         )) {
            this.rotation = tmp;
            RotationUtils.setRotation(this.rotation, 1);
         }
      }
   }

   private void openChestWithPacket(class_2338 pos) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         if (Southside.INSTANCE.getModuleManager().get(StealerN.class).isEnabled() && !Southside.INSTANCE.getModuleManager().get(Scaffold.class).isEnabled()) {
            class_3965 hit = ClientRayTraceUtil.getFacedBlock(
               RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, blockState -> !this.isChestBlock(blockState)
            );
            boolean raycasted = hit != null
               && hit.method_17777().method_10263() == pos.method_10263()
               && hit.method_17777().method_10264() == pos.method_10264()
               && hit.method_17777().method_10260() == pos.method_10260();
            if (raycasted || !this.raycast.getValue()) {
               PacketUtils.sendSequencedPacket(id -> new class_2885(class_1268.field_5808, hit, id));
               ChestManager manager = Southside.get().getChestManager();
               manager.onSteal(pos);
            }
         }
      }
   }

   private class_2350 calculateHitSide(class_243 playerPos, class_243 chestCenter) {
      class_243 diff = playerPos.method_1020(chestCenter);
      double absX = Math.abs(diff.field_1352);
      double absY = Math.abs(diff.field_1351);
      double absZ = Math.abs(diff.field_1350);
      if (absX > absY && absX > absZ) {
         return diff.field_1352 > 0.0 ? class_2350.field_11034 : class_2350.field_11039;
      } else if (absY > absZ) {
         return diff.field_1351 > 0.0 ? class_2350.field_11036 : class_2350.field_11033;
      } else {
         return diff.field_1350 > 0.0 ? class_2350.field_11035 : class_2350.field_11043;
      }
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.moveInputActive = false;
   }
}
