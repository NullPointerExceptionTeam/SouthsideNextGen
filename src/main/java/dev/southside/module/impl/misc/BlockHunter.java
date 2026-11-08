package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.ChatUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_2338;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2680;

public class BlockHunter extends Module {
   private final Set<class_2338> markedBlocks = new HashSet<>();
   private final Color sideColor = new Color("Side Color", true, new OneColor(255, 120, 40, 60));
   private final Color lineColor = new Color("Line Color", true, new OneColor(255, 120, 40, 160));

   public BlockHunter() {
      super(Category.Misc, "BlockHunter");
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getOrigin() == TransferOrigin.RECEIVE && mc.field_1687 != null) {
         if (event.getPacket() instanceof class_2626 packet) {
            this.markIfAir(packet.method_11309(), packet.method_11308());
         } else if (event.getPacket() instanceof class_2637 packet) {
            packet.method_30621(this::markIfAir);
         }
      }
   }

   @EventHandler
   public void onWorldChange(WorldEvent event) {
      this.markedBlocks.clear();
   }

   @EventHandler
   public void onRender(Render3DEvent event) {
      if (mc.field_1687 != null && !this.markedBlocks.isEmpty()) {
         for (class_2338 pos : this.markedBlocks) {
            Render3D.blockSides(pos, this.sideColor.getValue().toJavaColor());
            Render3D.blockLines(pos, this.lineColor.getValue().toJavaColor());
         }
      }
   }

   @Override
   public void onDisable() {
      this.markedBlocks.clear();
      super.onDisable();
   }

   private void markIfAir(class_2338 pos, class_2680 newState) {
      if (pos != null && newState != null && mc.field_1687 != null) {
         if (newState.method_26215()) {
            this.markedBlocks.remove(pos);
         } else {
            if (!newState.method_26215() && mc.field_1687.method_8320(pos).method_26215()) {
               class_2338 immutablePos = pos.method_10062();
               if (this.markedBlocks.add(immutablePos)) {
                  ChatUtils.info("Detected a hider! " + this.formatPos(immutablePos));
               }
            }
         }
      }
   }

   private String formatPos(class_2338 pos) {
      return "(" + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260() + ")";
   }
}
