package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Targets extends Module {
   private static Targets INSTANCE;
   public Switch players = new Switch("Players", true);
   public Switch mobs = new Switch("Mobs", false);
   public Switch animals = new Switch("Animals", false);
   public Switch villagers = new Switch("Villagers", false);

   public Targets() {
      super(Category.Client, "Targets", "目标");
      INSTANCE = this;
   }

   public static Targets getInstance() {
      return INSTANCE;
   }

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   public static boolean check(LivingEntity entity) {
      if (entity != null && !(entity instanceof FakePlayerEntity)) {
         Targets t = getInstance();
         if (entity instanceof PlayerEntity) {
            return t.players.getValue();
         } else if (entity instanceof MobEntity && !(entity instanceof VillagerEntity) && !(entity instanceof AnimalEntity)) {
            return t.mobs.getValue();
         } else if (entity instanceof VillagerEntity) {
            return t.villagers.getValue();
         } else {
            return entity instanceof AnimalEntity ? t.animals.getValue() : false;
         }
      } else {
         return false;
      }
   }

   public static boolean check(Entity entity) {
      if (entity != null && !(entity instanceof FakePlayerEntity)) {
         Targets t = getInstance();
         if (entity instanceof PlayerEntity) {
            return t.players.getValue();
         } else if (entity instanceof MobEntity && !(entity instanceof VillagerEntity) && !(entity instanceof AnimalEntity)) {
            return t.mobs.getValue();
         } else if (entity instanceof VillagerEntity) {
            return t.villagers.getValue();
         } else {
            return entity instanceof AnimalEntity ? t.animals.getValue() : false;
         }
      } else {
         return false;
      }
   }
}
