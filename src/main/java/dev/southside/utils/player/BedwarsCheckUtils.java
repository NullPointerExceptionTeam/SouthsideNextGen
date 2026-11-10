package dev.southside.utils.player;

import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;

public class BedwarsCheckUtils {
   public static final BedwarsCheckUtils INSTANCE = new BedwarsCheckUtils();
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final int SCAN_RADIUS = 20;
   private BlockPos selfBed = null;

   private BedwarsCheckUtils() {
   }

   @EventHandler
   private void onTick(TickEvent event) {
      if (event.getState() == Stage.PRE) {
         if (mc.player != null && mc.world != null) {
            if (this.selfBed == null) {
               boolean hasWoodenSword = false;

               for (int i = 0; i < 9; i++) {
                  if (mc.player.getInventory().getStack(i).getItem() == Items.WOODEN_SWORD) {
                     hasWoodenSword = true;
                     break;
                  }
               }

               if (hasWoodenSword) {
                  BlockPos playerPos = mc.player.getBlockPos();

                  for (int x = -20; x <= 20; x++) {
                     for (int y = -20; y <= 20; y++) {
                        for (int z = -20; z <= 20; z++) {
                           BlockPos pos = playerPos.add(x, y, z);
                           if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                              this.selfBed = pos;
                              return;
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
      this.selfBed = null;
   }

   public boolean isBedwarsGaming() {
      return this.selfBed != null;
   }

   public BlockPos getSelfBed() {
      return this.selfBed;
   }
}
