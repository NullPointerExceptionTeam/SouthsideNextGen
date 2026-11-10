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
import net.minecraft.util.Hand;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.hit.BlockHitResult;

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
   private BlockPos currentPos;
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
      if (mc.player != null && mc.world != null) {
         if (this.delayTime > 0) {
            this.delayTime--;
         } else if (this.timer.hasTimePassed(this.currentDelay)) {
            if ((KillAura.target == null || !Southside.get().getModuleManager().get(KillAura.class).isEnabled())
               && !mc.player.isUsingItem()
               && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
               && !Southside.get().getModuleManager().get(Blink.class).isEnabled()
               && mc.player.hurtTime == 0) {
               if (!(mc.player.fallDistance > 3.0F) && !NoSlowTest.shouldEnable) {
                  if (mc.currentScreen == null && !InvUtils.isInventoryFull(mc.player.getInventory())) {
                     if (!this.pauseOnMoveInput.getValue() || !this.moveInputActive) {
                        BlockPos chestPos = this.findNextChest();
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

   private BlockPos findNextChest() {
      if (mc.player != null && mc.world != null) {
         BlockPos playerPos = mc.player.getBlockPos();
         Vec3d playerVec = mc.player.getPos();
         int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
         BlockPos nearestChest = null;
         double nearestDistance = Double.MAX_VALUE;

         for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
               for (int z = -rangeInt; z <= rangeInt; z++) {
                  BlockPos pos = playerPos.add(x, y, z);
                  double distance = playerVec.distanceTo(Vec3d.ofCenter(pos));
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

   private boolean isChestBlock(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState state = mc.world.getBlockState(pos);
         return state.getBlock() instanceof ChestBlock
            || state.getBlock() instanceof EnderChestBlock && this.pickEnderChest.getValue()
            || state.getBlock() instanceof ShulkerBoxBlock;
      }
   }

   private boolean isChestBlock(BlockState state) {
      return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
   }

   private boolean isBlockedByNonLeaf(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState aboveState = mc.world.getBlockState(pos.up());
         return aboveState.getBlock() instanceof LeavesBlock ? false : ChestWreck.isChestBlocked(pos);
      }
   }

   private void lookAtChest(BlockPos chestPos) {
      if (mc.player != null) {
         Rotation tmp = RotationUtils.getRandomRotationTo(chestPos, Direction.UP);
         if (ClientRayTraceUtil.didHitBlockFace(
            mc.player, tmp.yaw, tmp.pitch, chestPos, Direction.UP, false, blockState -> !this.isChestBlock(blockState)
         )) {
            this.rotation = tmp;
            RotationUtils.setRotation(this.rotation, 1);
         }
      }
   }

   private void openChestWithPacket(BlockPos pos) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         if (Southside.INSTANCE.getModuleManager().get(StealerN.class).isEnabled() && !Southside.INSTANCE.getModuleManager().get(Scaffold.class).isEnabled()) {
            BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(
               RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, blockState -> !this.isChestBlock(blockState)
            );
            boolean raycasted = hit != null
               && hit.getBlockPos().getX() == pos.getX()
               && hit.getBlockPos().getY() == pos.getY()
               && hit.getBlockPos().getZ() == pos.getZ();
            if (raycasted || !this.raycast.getValue()) {
               PacketUtils.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, id));
               ChestManager manager = Southside.get().getChestManager();
               manager.onSteal(pos);
            }
         }
      }
   }

   private Direction calculateHitSide(Vec3d playerPos, Vec3d chestCenter) {
      Vec3d diff = playerPos.subtract(chestCenter);
      double absX = Math.abs(diff.x);
      double absY = Math.abs(diff.y);
      double absZ = Math.abs(diff.z);
      if (absX > absY && absX > absZ) {
         return diff.x > 0.0 ? Direction.EAST : Direction.WEST;
      } else if (absY > absZ) {
         return diff.y > 0.0 ? Direction.UP : Direction.DOWN;
      } else {
         return diff.z > 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.moveInputActive = false;
   }
}
