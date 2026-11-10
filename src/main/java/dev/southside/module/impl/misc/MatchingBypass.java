package dev.southside.module.impl.misc;

import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerTickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class MatchingBypass extends Module {
   private int currentPlayers = -1;
   private int maxPlayers = -1;
   private long lastCheckTime = -1L;
   private boolean waiting = false;

   public MatchingBypass() {
      super(Category.Misc, "MatchingBypass");
   }

   @EventHandler
   public void onPacket(PacketEvent e) {
      if (e.getPacket() instanceof ChatMessageS2CPacket packet) {
         String msg = packet.body().content();
         this.start(msg);
      } else if (e.getPacket() instanceof GameMessageS2CPacket chatPacket) {
         String msg = chatPacket.content().getString();
         this.start(msg);
      }
   }

   private void start(String msg) {
      if (msg.contains("加入了游戏") && msg.contains("/")) {
         String[] split = msg.split("\\(")[1].replace(")", "").split("/");
         this.currentPlayers = Integer.parseInt(split[0].trim());
         this.maxPlayers = Integer.parseInt(split[1].trim());
         this.lastCheckTime = System.currentTimeMillis();
         this.waiting = true;
      }
   }

   @EventHandler
   public void onTick(PlayerTickEvent e) {
      if (this.waiting && System.currentTimeMillis() - this.lastCheckTime >= 5000L) {
         if (mc.player != null
            && mc.player.networkHandler != null
            && this.isInGlassCage()
            && this.currentPlayers < this.maxPlayers
            && this.currentPlayers <= 7) {
            mc.player.networkHandler.sendCommand("play swrsolo");
            ChatUtils.message("bypass");
         }

         this.waiting = false;
         this.currentPlayers = -1;
         this.maxPlayers = -1;
      }
   }

   private boolean isInGlassCage() {
      if (mc.world != null && mc.player != null) {
         BlockPos pos = mc.player.getBlockPos();
         String blockBelow = mc.world.getBlockState(pos.down()).getBlock().toString().toLowerCase();
         String blockAbove = mc.world.getBlockState(pos.up()).getBlock().toString().toLowerCase();
         return blockBelow.contains("glass") || blockAbove.contains("glass");
      } else {
         return false;
      }
   }
}
