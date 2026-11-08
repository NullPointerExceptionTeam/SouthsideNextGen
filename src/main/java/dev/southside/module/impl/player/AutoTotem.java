package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2846.class_2847;

public class AutoTotem extends Module {
   public Slider health = new Slider("Health", 3, 0, 20);
   private int oldSlot = -1;
   private boolean hasSwapped = false;

   public AutoTotem() {
      super(Category.Player, "AutoTotem");
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.isTotemInOffhand()) {
            if (this.oldSlot != -1) {
               mc.field_1724.method_31548().field_7545 = this.oldSlot;
               this.oldSlot = -1;
            }

            this.hasSwapped = false;
         } else {
            if (this.shouldSwap()) {
               int totemSlot = this.findTotemSlot();
               if (totemSlot == -1) {
                  this.resetState();
                  return;
               }

               this.handleTotemSwap(totemSlot);
            } else {
               this.resetState();
            }
         }
      }
   }

   private boolean isTotemInOffhand() {
      return mc.field_1724 == null ? false : mc.field_1724.method_6079().method_7909() == class_1802.field_8288;
   }

   private boolean isTotemInMainhand() {
      return mc.field_1724 == null ? false : mc.field_1724.method_6047().method_7909() == class_1802.field_8288;
   }

   private int findTotemSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else if (this.isTotemInMainhand()) {
         return mc.field_1724.method_31548().field_7545;
      } else {
         for (int i = 0; i < 9; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == class_1802.field_8288) {
               return i;
            }
         }

         return -1;
      }
   }

   private void handleTotemSwap(int totemSlot) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         if (this.oldSlot == -1) {
            this.oldSlot = mc.field_1724.method_31548().field_7545;
         }

         if (mc.field_1724.method_31548().field_7545 != totemSlot) {
            mc.field_1724.method_31548().field_7545 = totemSlot;
            mc.field_1761.method_2911();
         }

         if (!this.hasSwapped && mc.field_1724.method_31548().field_7545 == totemSlot) {
            this.hasSwapped = true;
            PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
         }
      }
   }

   private void resetState() {
      if (this.hasSwapped && !this.isTotemInOffhand()) {
         this.hasSwapped = false;
      }

      this.oldSlot = -1;
   }

   private boolean shouldSwap() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         float absorption = mc.field_1724.method_6059(class_1294.field_5898) ? mc.field_1724.method_6067() : 0.0F;
         return mc.field_1724.method_6032() + absorption <= this.health.getValue().doubleValue()
            ? true
            : mc.field_1724.field_6017 > 10.0F && !mc.field_1724.method_6128();
      }
   }
}
