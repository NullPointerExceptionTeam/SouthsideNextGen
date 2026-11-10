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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

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
         if (mc.player != null && mc.world != null) {
            boolean gaming = BedwarsCheckUtils.INSTANCE.isBedwarsGaming();
            BlockPos bed = BedwarsCheckUtils.INSTANCE.getSelfBed();
            if (gaming && bed != null) {
               if (this.autoInc.getValue()) {
                  for (PlayerEntity player : mc.world.getPlayers()) {
                     if (player != mc.player && !Teams.isTeam(player)) {
                        String name = player.getGameProfile().getName();
                        if (!this.incAlerted.contains(name) && player.getBlockPos().isWithinDistance(bed, 16.0)) {
                           this.incAlerted.add(name);
                           mc.getNetworkHandler().sendChatMessage("inc! " + name + " 来我们家了!");
                           if (this.debug.getValue()) {
                              ChatUtils.info("[AutoINC] " + name + " entered bed range");
                           }
                        }
                     }
                  }

                  this.incAlerted.removeIf(namex -> {
                     PlayerEntity playerx = mc.world.getPlayers().stream().filter(p -> p.getGameProfile().getName().equals(namex)).findFirst().orElse(null);
                     return playerx == null || !playerx.getBlockPos().isWithinDistance(bed, 16.0);
                  });
               }

               if (this.antiBreakBed.getValue()) {
                  Set<BlockPos> protectionBlocks = this.getProtectionBlocks(bed);

                  for (PlayerEntity playerx : mc.world.getPlayers()) {
                     if (playerx != mc.player && !Teams.isTeam(playerx)) {
                        String name = playerx.getGameProfile().getName();
                        if (!this.reportedPlayers.contains(name) && playerx.handSwinging) {
                           BlockPos miningPos = this.getPlayerMiningBlock(playerx);
                           if (miningPos != null && protectionBlocks.contains(miningPos)) {
                              this.reportedPlayers.add(name);
                              if (this.debug.getValue()) {
                                 ChatUtils.info("[AntiBreakBed] " + name + " is breaking bed protection at " + miningPos.toShortString() + ", reporting...");
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

   private Set<BlockPos> getProtectionBlocks(BlockPos bedPos) {
      Set<BlockPos> result = new LinkedHashSet<>();
      BlockState bedState = mc.world.getBlockState(bedPos);
      if (!(bedState.getBlock() instanceof BedBlock)) {
         return result;
      } else {
         BlockPos headPos;
         BlockPos footPos;
         if (bedState.get(BedBlock.PART) == BedPart.HEAD) {
            headPos = bedPos;
            Direction facing = (Direction)bedState.get(BedBlock.FACING);
            footPos = bedPos.offset(facing.getOpposite());
         } else {
            footPos = bedPos;
            Direction facing = (Direction)bedState.get(BedBlock.FACING);
            headPos = bedPos.offset(facing);
         }

         Set<BlockPos> bedBlocks = new LinkedHashSet<>();
         bedBlocks.add(headPos.toImmutable());
         bedBlocks.add(footPos.toImmutable());

         for (BlockPos base : bedBlocks) {
            BlockPos above = base.up();
            if (!bedBlocks.contains(above)) {
               result.add(above.toImmutable());
            }

            for (int dx = -3; dx <= 3; dx++) {
               for (int dz = -3; dz <= 3; dz++) {
                  if ((dx != 0 || dz != 0) && Math.max(Math.abs(dx), Math.abs(dz)) <= 3) {
                     BlockPos target = base.add(dx, 0, dz);
                     if (!bedBlocks.contains(target)) {
                        result.add(target.toImmutable());
                     }
                  }
               }
            }
         }

         result.removeIf(pos -> mc.world.getBlockState(pos).isAir());
         return result;
      }
   }

   private BlockPos getPlayerMiningBlock(PlayerEntity player) {
      Vec3d eyePos = player.getEyePos();
      Vec3d lookVec = player.getRotationVec(1.0F);
      Vec3d endPos = eyePos.add(lookVec.multiply(4.5));
      BlockHitResult result = mc.world.raycast(new RaycastContext(eyePos, endPos, ShapeType.OUTLINE, FluidHandling.NONE, player));
      return result.getType() == Type.BLOCK ? result.getBlockPos() : null;
   }

   public static boolean isBedwarsGaming() {
      return BedwarsCheckUtils.INSTANCE.isBedwarsGaming();
   }

   public static BlockPos getSelfBed() {
      return BedwarsCheckUtils.INSTANCE.getSelfBed();
   }
}
