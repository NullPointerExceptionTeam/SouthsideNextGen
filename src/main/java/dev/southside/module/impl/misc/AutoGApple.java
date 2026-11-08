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
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2886;
import net.minecraft.class_2846.class_2847;

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
      if (event.getState().equals(Stage.PRE) && mc.field_1724 != null && mc.field_1761 != null) {
         if (this.swapRequired) {
            this.swapRequired = false;
            PacketUtils.sendPacket(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
            ChatUtils.info("1swap");
            this.waitSwap = true;
         }

         if (this.waitSwap && mc.field_1724.method_6047().method_7909().equals(class_1802.field_8463)) {
            this.waitSwap = false;
            ServerPacketManager.setup();
            PacketUtils.sendPacket(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
            ChatUtils.info("2swap");
            eating = true;
            PacketUtils.sendSequencedPacket(
               sequence -> new class_2886(
                  class_1268.field_5810, sequence, RotationUtils.getRotationSafely().getYaw(), RotationUtils.getRotationSafely().getPitch()
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
