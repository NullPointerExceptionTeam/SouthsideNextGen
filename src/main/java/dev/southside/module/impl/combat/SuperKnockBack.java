package dev.southside.module.impl.combat;

import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.util.InputUtil;

public class SuperKnockBack extends Module {
   private int ticks = 0;

   public SuperKnockBack() {
      super(Category.Combat, "SuperKnockBack", "More KnockBack");
   }

   @Override
   public void onEnable() {
      this.ticks = 0;
   }

   @Override
   public void onDisable() {
      this.ticks = 0;
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.ticks = 0;
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (this.ticks == 2) {
         mc.options.forwardKey.setPressed(false);
         this.ticks = 1;
      } else if (this.ticks == 1) {
         mc.options.forwardKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()));
         this.ticks = 0;
      }
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      Entity entity = event.target;
      if (entity instanceof LivingEntity) {
         this.ticks = 2;
      }
   }
}
