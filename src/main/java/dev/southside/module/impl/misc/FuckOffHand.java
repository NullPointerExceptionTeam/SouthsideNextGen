package dev.southside.module.impl.misc;

import dev.southside.event.Stage;
import dev.southside.event.events.InteractItemEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2846.class_2847;

public class FuckOffHand extends Module {
   boolean swapBack = false;
   boolean mainHandUsed = false;

   public FuckOffHand() {
      super(Category.Misc, "FuckOffHand");
   }

   @EventHandler
   public void onInteractItem(InteractItemEvent event) {
      if (mc.field_1724 != null
         && mc.field_1761 != null
         && mc.field_1765 != null
         && mc.field_1765.method_17783().equals(class_240.field_1333)
         && this.isValidItem(mc.field_1724.method_6079())) {
         if (!event.getItem().method_7909().equals(class_1802.field_8814)) {
            if (event.getInteractHand().equals(class_1268.field_5808) && !this.isValidItem(event.getItem())) {
               class_1269 result = event.getItem().method_7972().method_7909().method_7836(mc.field_1687, mc.field_1724, class_1268.field_5808);
               if (!(result instanceof class_9860)) {
                  event.setCancelled(true);
               }
            } else {
               if (event.getInteractHand().equals(class_1268.field_5810) && this.isValidItem(event.getItem())) {
                  event.setCancelled(true);
                  PacketUtils.sendPacket(new class_2846(class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
                  class_1799 stack = mc.field_1724.method_6047().method_7972();
                  mc.field_1724.method_6122(class_1268.field_5808, mc.field_1724.method_6079());
                  mc.field_1724.method_6122(class_1268.field_5810, stack);
                  if (mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808) instanceof class_9860 success3) {
                     if (success3.comp_2909() == class_9861.field_52427) {
                        mc.field_1724.method_6104(class_1268.field_5808);
                     }

                     mc.field_1773.field_4012.method_3215(class_1268.field_5808);
                  }
               }

               this.swapBack = true;
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState().equals(Stage.PRE)) {
         if (this.swapBack) {
            this.swapBack = false;
            if (mc.field_1724 != null && !this.isValidItem(mc.field_1724.method_6079())) {
               PacketUtils.sendPacket(new class_2846(class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
            }
         }
      } else {
         this.mainHandUsed = false;
      }
   }

   public boolean isValidItem(class_1799 itemStack) {
      return itemStack == null ? false : itemStack.method_7909().equals(class_1802.field_8543) || itemStack.method_7909().equals(class_1802.field_8803);
   }
}
