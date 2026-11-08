package dev.southside.module.impl.misc;

import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PlayerTickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import net.minecraft.class_2338;
import net.minecraft.class_7438;
import net.minecraft.class_7439;

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
      if (e.getPacket() instanceof class_7438 packet) {
         String msg = packet.comp_1102().comp_1090();
         this.start(msg);
      } else if (e.getPacket() instanceof class_7439 chatPacket) {
         String msg = chatPacket.comp_763().getString();
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
         if (mc.field_1724 != null
            && mc.field_1724.field_3944 != null
            && this.isInGlassCage()
            && this.currentPlayers < this.maxPlayers
            && this.currentPlayers <= 7) {
            mc.field_1724.field_3944.method_45731("play swrsolo");
            ChatUtils.message("bypass");
         }

         this.waiting = false;
         this.currentPlayers = -1;
         this.maxPlayers = -1;
      }
   }

   private boolean isInGlassCage() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_2338 pos = mc.field_1724.method_24515();
         String blockBelow = mc.field_1687.method_8320(pos.method_10074()).method_26204().toString().toLowerCase();
         String blockAbove = mc.field_1687.method_8320(pos.method_10084()).method_26204().toString().toLowerCase();
         return blockBelow.contains("glass") || blockAbove.contains("glass");
      } else {
         return false;
      }
   }
}
