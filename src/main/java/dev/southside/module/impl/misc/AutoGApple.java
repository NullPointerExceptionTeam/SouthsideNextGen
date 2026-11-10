package dev.southside.module.impl.misc;

import dev.southside.event.Stage;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.InvManagerN;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.rotation.RotationUtils;
import net.minecraft.util.Hand;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class AutoGApple extends Module {
   public static boolean eating = false;
   boolean waitSwap = false;
   boolean swapRequired = false;

   public AutoGApple() {
      super(Category.Misc, "AutoGApple", "Retard");
   }

   @Override
   public void onEnable() {
      this.swapRequired = true;
      eating = false;
      this.waitSwap = false;
   }

   @Override
   public void onDisable() {
      eating = false;
      this.waitSwap = false;
      this.swapRequired = false;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState().equals(Stage.PRE) && mc.player != null && mc.interactionManager != null) {
         if (this.swapRequired) {
            this.swapRequired = false;
            PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            ChatUtils.info("1swap");
            this.waitSwap = true;
         }

         if (this.waitSwap && mc.player.getMainHandStack().getItem().equals(Items.GOLDEN_APPLE)) {
            this.waitSwap = false;
            ServerPacketManager.setup();
            PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            ChatUtils.info("2swap");
            eating = true;
            PacketUtils.sendSequencedPacket(
               sequence -> new PlayerInteractItemC2SPacket(
                  Hand.OFF_HAND, sequence, RotationUtils.getRotationSafely().getYaw(), RotationUtils.getRotationSafely().getPitch()
               )
            );
            KillAura.msTimer.reset();
         }

         if (eating) {
            InvManagerN.timer.reset();
            if (ServerPacketManager.deSyncing && ServerPacketManager.deSyncTick >= 34) {
               ServerPacketManager.reset();
               ChatUtils.info("3reset");
               this.setEnable(false);
            }
         }
      }
   }
}
