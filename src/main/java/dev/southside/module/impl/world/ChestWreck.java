package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.Blink;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.InventoryUtil;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.SlotUtils;
import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.util.hit.BlockHitResult;

public class ChestWreck extends Module {
   public static final List<Block> invalidBlocks = Arrays.asList(
      Blocks.ENCHANTING_TABLE,
      Blocks.CHEST,
      Blocks.ENDER_CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.TORCH,
      Blocks.STONE_PRESSURE_PLATE,
      Blocks.BAMBOO_PRESSURE_PLATE,
      Blocks.TNT,
      Blocks.REDSTONE_TORCH
   );
   private final Slider range = new Slider("Range", 4.0, 1.0, 8.0, 0.1);
   private final Slider minDelay = new Slider("Min Delay", 50, 0, 1000, 10);
   private final Slider maxDelay = new Slider("Max Delay", 150, 0, 1000, 10);
   private final Switch blockAllOpened = new Switch("Block All Opened", false);
   private final MSTimer timer = new MSTimer();
   private ChestWreck.SlotData slot;
   private boolean prevSneak;
   private boolean shouldPlace = false;
   private BlockPos chestPos = null;
   private int lastSlot = -1;
   private long currentDelay = 0L;
   private final List<BlockPos> openedChests = new ArrayList<>();

   public ChestWreck() {
      super(Category.World, "ChestWreck", "esu");
   }

   @Override
   public void onEnable() {
      this.shouldPlace = false;
      this.prevSneak = false;
      this.openedChests.clear();
      this.chestPos = null;
      this.lastSlot = -1;
      this.slot = null;
      this.generateRandomDelay();
   }

   @EventHandler
   private void onRot(RotationEvent e) {
      if (mc.currentScreen != null) {
         this.timer.reset();
      } else if (this.timer.hasTimePassed(this.currentDelay)) {
         if ((KillAura.target == null || !Southside.get().getModuleManager().get(KillAura.class).isEnabled())
            && !mc.player.isUsingItem()
            && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
            && !Southside.get().getModuleManager().get(Blink.class).isEnabled()
            && mc.player.hurtTime == 0) {
            if (this.shouldPlace && mc.player.lastSneaking) {
               if (!(this.chestPos.getY() + 1 > mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()))) {
                  ActionResult result = mc.interactionManager
                     .interactBlock(
                        mc.player,
                        this.slot.hand,
                        new BlockHitResult(Scaffold.getVec3(this.chestPos, Direction.UP), Direction.UP, this.chestPos, false)
                     );
                  if (result == ActionResult.SUCCESS) {
                     mc.player.swingHand(this.slot.hand());
                  }

                  if (!this.slot.hand.equals(Hand.OFF_HAND)) {
                     mc.player.getInventory().selectedSlot = this.lastSlot;
                  }
               }

               this.timer.reset();
               this.generateRandomDelay();
               mc.options.sneakKey.setPressed(this.prevSneak);
               mc.player.setSneaking(this.prevSneak);
               this.shouldPlace = false;
            } else {
               BlockPos nearest = this.findNextChest();
               if (nearest != null) {
                  this.lastSlot = mc.player.getInventory().selectedSlot;
                  this.slot = null;
                  if (InventoryUtil.isFullBlock(mc.player.getOffHandStack()) && isValid(mc.player.getOffHandStack().getItem())) {
                     this.slot = new ChestWreck.SlotData(45, Hand.OFF_HAND);
                  }

                  if (this.slot == null && InventoryUtil.isFullBlock(mc.player.getMainHandStack()) && isValid(mc.player.getMainHandStack().getItem())) {
                     this.slot = new ChestWreck.SlotData(mc.player.getInventory().selectedSlot, Hand.MAIN_HAND);
                  }

                  if (this.slot == null) {
                     for (int i = 0; i <= 8; i++) {
                        ItemStack is = mc.player.getInventory().getStack(i);
                        if (InventoryUtil.isFullBlock(is) && isValid(is.getItem())) {
                           this.slot = new ChestWreck.SlotData(i, Hand.MAIN_HAND);
                        }
                     }
                  }

                  if (this.slot == null || !this.slot.check()) {
                     return;
                  }

                  if (nearest.getY() + 1 < mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())) {
                     Vec3d minPos = null;
                     double lastDis = Double.MAX_VALUE;

                     for (double x = 0.1; x <= 0.9; x += 0.1) {
                        for (double z = 0.1; z <= 0.9; z += 0.1) {
                           Vec3d pos = new Vec3d(nearest.getX() + x, nearest.getY() + 1, nearest.getZ() + z);
                           double dis = mc.player.getCameraPosVec(1.0F).squaredDistanceTo(pos);
                           if (dis < lastDis) {
                              minPos = pos;
                              lastDis = dis;
                           }
                        }
                     }

                     if (minPos != null) {
                        Rotation rot = RotationUtils.calculate(minPos);
                        BlockHitResult raycast = RayCastUtils.raycast(4.5, rot, false, 1.0F);
                        if (raycast != null && raycast.getBlockPos().equals(nearest)) {
                           RotationUtils.setRotation(rot);
                           if (!this.slot.hand.equals(Hand.OFF_HAND)) {
                              mc.player.getInventory().selectedSlot = this.slot.slot;
                           }

                           this.chestPos = nearest;
                           this.shouldPlace = true;
                           this.prevSneak = mc.options.sneakKey.isPressed();
                           mc.options.sneakKey.setPressed(true);
                           mc.player.setSneaking(true);
                        }
                     }
                  }
               } else {
                  this.timer.reset();
               }
            }
         } else {
            this.timer.reset();
         }
      }
   }

   private BlockPos findNextChest() {
      if (mc.player != null && mc.world != null) {
         BlockPos playerPos = mc.player.getBlockPos();
         Vec3d playerVec = mc.player.getPos();
         int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
         BlockPos nearestChest = null;
         double nearestDistance = Double.MAX_VALUE;
         ChestManager manager = Southside.get().getChestManager();

         for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
               for (int z = -rangeInt; z <= rangeInt; z++) {
                  BlockPos pos = playerPos.add(x, y, z);
                  double distance = playerVec.distanceTo(Vec3d.ofCenter(pos));
                  if (!(distance > this.range.getValue().floatValue())
                     && (this.blockAllOpened.getValue() ? manager.isStolen(pos) : manager.isSelfStolen(pos))
                     && this.openedChests.contains(pos)
                     && this.isChestBlock(pos)
                     && !isChestBlocked(pos)
                     && this.isSpaceEmpty(pos.up())
                     && distance < nearestDistance) {
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

   public static boolean isChestBlocked(BlockPos pos) {
      return mc.world == null ? false : isValidBlock(mc.world.getBlockState(pos.up()).getBlock());
   }

   private boolean isSpaceEmpty(BlockPos pos) {
      return mc.world == null ? false : mc.world.isSpaceEmpty(mc.player, new Box(pos));
   }

   private boolean isChestBlock(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState state = mc.world.getBlockState(pos);
         return state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock || state.getBlock() instanceof ShulkerBoxBlock;
      }
   }

   private static boolean isValid(Item item) {
      return item instanceof BlockItem && !invalidBlocks.contains(((BlockItem)item).getBlock()) && SlotUtils.isGoodForBridging(item);
   }

   private static boolean isValidBlock(Block b) {
      return !invalidBlocks.contains(b) && SlotUtils.isGoodForBridging(b);
   }

   private void generateRandomDelay() {
      int min = this.minDelay.getValue().intValue();
      int max = this.maxDelay.getValue().intValue();
      if (min > max) {
         this.currentDelay = min;
      } else {
         this.currentDelay = min + RandomUtils.nextInt(0, max - min + 1);
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.world != null) {
         if (event.getPacket() instanceof BlockEventS2CPacket sb) {
            BlockState state = mc.world.getBlockState(sb.getPos());
            if (state.getBlock() instanceof ChestBlock && sb.getType() == 1 && sb.getData() == 1) {
               this.openedChests.add(sb.getPos());
            }
         }
      }
   }

   record SlotData(int slot, Hand hand) {
      public boolean check() {
         if (this.hand.equals(Hand.OFF_HAND)) {
            ItemStack stack = ChestWreck.mc.player.getOffHandStack();
            boolean success = !stack.isEmpty() && stack.getItem() instanceof BlockItem;
            if (success) {
               return this.checkBlocked(Block.getBlockFromItem(stack.getItem()));
            }
         }

         ItemStack stack = ChestWreck.mc.player.getInventory().getStack(this.slot);
         boolean success = !stack.isEmpty() && stack.getItem() instanceof BlockItem;
         return success ? this.checkBlocked(Block.getBlockFromItem(stack.getItem())) : false;
      }

      public boolean checkBlocked(Block block) {
         BlockState state = block.getDefaultState();
         VoxelShape shape = state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
         return Block.isFaceFullSquare(shape, Direction.DOWN);
      }
   }
}
