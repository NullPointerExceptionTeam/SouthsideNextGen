package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.EnchantmentUtils;
import dev.southside.utils.player.MovementUtils;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1893;
import net.minecraft.class_2868;

public class Criticals extends Module {
   public static final Dropdown mode = new Dropdown("Mode", "Switch", "Switch", "Stuck", "Packet");
   private int lastSlot;
   private int attacks;
   private boolean release;
   private boolean spamAttack;

   public Criticals() {
      super(Category.Combat, "Criticals", "idk");
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (mode.isMode("Stuck") && event.stage == Stage.PRE) {
         this.attacks++;
         if (this.attacks % 2 == 0) {
            this.release = true;
         }
      }

      if (mode.isMode("Switch")) {
         if (event.stage == Stage.PRE) {
            this.lastSlot = -1;
            int bestSword = this.getBestSword(mc.field_1724.field_7498);
            if (bestSword == -1) {
               return;
            }

            if (mc.field_1724.method_31548().field_7545 != bestSword) {
               this.lastSlot = mc.field_1724.method_31548().field_7545;
               mc.field_1724.method_31548().field_7545 = bestSword;
            }
         } else if (this.lastSlot != -1) {
            mc.field_1724.method_31548().field_7545 = this.lastSlot;
            int i = mc.field_1724.method_31548().field_7545;
            if (i != mc.field_1761.field_3721) {
               mc.field_1761.field_3721 = i;
               mc.field_1761.field_3720.method_52787(new class_2868(mc.field_1761.field_3721));
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (mode.isMode("Stuck")) {
         if (KillAura.target == null || !Southside.INSTANCE.getModuleManager().get(KillAura.class).inRaycast(KillAura.target)) {
            this.attacks = 0;
         } else if (mc.field_1724.field_6017 > 0.0F && !this.release && mc.field_1724.method_18798().field_1351 < 0.05) {
            MovementUtils.cancelMove();
         } else if (this.release) {
            this.release = false;
         }
      }
   }

   private int getBestSword(class_1703 c) {
      double damage = 0.0;
      int slot = -1;

      for (int i = 36; i < 45; i++) {
         class_1799 stack = c.method_7611(i).method_7677();
         if (!stack.method_7960() && mc.field_1724.method_31548().field_7545 != i - 36) {
            double newDamage = this.getItemDamage(stack);
            if (newDamage > damage) {
               damage = newDamage;
               slot = i - 36;
            }
         }
      }

      return slot;
   }

   private double getItemDamage(class_1799 stack) {
      double damage = 0.0;
      if (stack.method_7942()) {
         damage += EnchantmentUtils.getLevel(class_1893.field_9118, stack) * 0.5;
      }

      return damage;
   }
}
