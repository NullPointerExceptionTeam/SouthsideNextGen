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
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.CobwebBlock;
import net.minecraft.util.hit.BlockHitResult;

public class AutoTrap extends Module {
   public final List<Item> TRAPS = List.of(Items.COBWEB, Items.LAVA_BUCKET);
   public Slider range = new Slider("Range", 3.0, 3.0, 4.0, 0.01);
   public Slider reTicks = new Slider("LavaRetrieve", 30, 2, 30, 1);
   private final Map<BlockPos, Integer> placedLava = new HashMap<>();
   public static List<Entity> targets = new ArrayList<>();
   public static Entity target;

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
                  targets.sort((o1, o2) -> (int)(o1.distanceTo(mc.player) - o2.distanceTo(mc.player)));
                  target = targets.get(0);
                  int targetSlot = -1;
                  if (target != null
                     && target.isAlive()
                     && !target.isRemoved()
                     && mc.player.distanceTo(target) <= this.range.getValue().doubleValue()) {
                     for (int i = 0; i < 9; i++) {
                        if (this.TRAPS.contains(mc.player.getInventory().getStack(i).getItem())) {
                           targetSlot = i;
                           break;
                        }
                     }
                  }

                  if (targetSlot == -1) {
                     return;
                  }

                  int originalSlot = mc.player.getInventory().selectedSlot;
                  if (mc.player.getInventory().selectedSlot != targetSlot) {
                     mc.player.getInventory().selectedSlot = targetSlot;
                  }

                  BlockPos trapPos = target.getBlockPos().down();
                  Rotation rot = ScaffoldRotationHelper.getRotation(trapPos, Direction.UP);
                  if (mc.player.getInventory().getStack(targetSlot).getItem() == Items.COBWEB) {
                     RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(trapPos, Direction.UP), this.rotationPriority());
                     Vec3d hitVec = new Vec3d(trapPos.getX() + 0.5, trapPos.getY() + 1, trapPos.getZ() + 0.5);
                     BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, trapPos, false);
                     mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                     mc.player.swingHand(Hand.MAIN_HAND);
                  } else {
                     RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(trapPos, Direction.UP), this.rotationPriority());
                     mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                     BlockPos lavaPos = trapPos.up();
                     this.placedLava.put(lavaPos, 0);
                  }

                  mc.player.getInventory().selectedSlot = originalSlot;
               }
            }
         }
      }
   }

   private void handleLavaRetrieval() {
      if (!this.placedLava.isEmpty()) {
         List<BlockPos> toRetrieve = new ArrayList<>();
         this.placedLava.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            int ticks = entry.getValue();
            if (mc.world.getBlockState(pos).getBlock() != Blocks.LAVA || !mc.world.getFluidState(pos).isStill()) {
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
               if (mc.player.getInventory().getStack(i).getItem() == Items.BUCKET) {
                  emptyBucketSlot = i;
                  break;
               }
            }

            if (emptyBucketSlot != -1) {
               BlockPos lavaPos = toRetrieve.get(0);
               int originalSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = emptyBucketSlot;
               float[] rotArray = RotationUtils.getRotations(lavaPos.toCenterPos());
               RotationUtils.setRotation(ScaffoldRotationHelper.getRotation(lavaPos, Direction.UP), 0);
               ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               if (result.isAccepted()) {
                  mc.player.swingHand(Hand.MAIN_HAND);
               }

               mc.player.getInventory().selectedSlot = originalSlot;
            }
         }
      }
   }

   public void updateTargets() {
      target = null;
      targets.clear();

      for (Entity entity : mc.world.getEntities()) {
         if (TargetUtils.isTarget(entity, true, true)
            && BoundBoxUtils.getMinDistanceVec3dByBox(mc.player.getEyePos(), entity.getBoundingBox()).distanceTo(mc.player.getEyePos())
               <= this.range.getValue().doubleValue()
            && !entity.isInLava()
            && !(mc.world.getBlockState(entity.getBlockPos()).getBlock() instanceof CobwebBlock)
            && mc.world.getBlockState(entity.getBlockPos().down()).isSolid()) {
            targets.add(entity);
         }
      }
   }

   @Override
   public int rotationPriority() {
      return -2;
   }
}
