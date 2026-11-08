package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1646;
import net.minecraft.class_1657;

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

   public static boolean check(class_1309 entity) {
      if (entity != null && !(entity instanceof FakePlayerEntity)) {
         Targets t = getInstance();
         if (entity instanceof class_1657) {
            return t.players.getValue();
         } else if (entity instanceof class_1308 && !(entity instanceof class_1646) && !(entity instanceof class_1429)) {
            return t.mobs.getValue();
         } else if (entity instanceof class_1646) {
            return t.villagers.getValue();
         } else {
            return entity instanceof class_1429 ? t.animals.getValue() : false;
         }
      } else {
         return false;
      }
   }

   public static boolean check(class_1297 entity) {
      if (entity != null && !(entity instanceof FakePlayerEntity)) {
         Targets t = getInstance();
         if (entity instanceof class_1657) {
            return t.players.getValue();
         } else if (entity instanceof class_1308 && !(entity instanceof class_1646) && !(entity instanceof class_1429)) {
            return t.mobs.getValue();
         } else if (entity instanceof class_1646) {
            return t.villagers.getValue();
         } else {
            return entity instanceof class_1429 ? t.animals.getValue() : false;
         }
      } else {
         return false;
      }
   }
}
