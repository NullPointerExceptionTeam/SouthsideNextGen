package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityEditor extends Module {
   public final Dropdown model = new Dropdown("Model", "Pig", "Pig", "Sheep", "Wolf", "Camel", "Donkey", "Creeper", "Skeleton", "Zombie", "Blaze", "Iron Golem");
   public final Switch allEntities = new Switch("All Entities", false);
   public final Switch players = new Switch("Players", true);
   public final Switch mobs = new Switch("Mobs", false);

   public EntityEditor() {
      super(Category.World, "EntityEditor", "Replace entity models");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addListener(this.model.getLabel(), () -> this.setSuffix(this.model.getMode()));
      this.hideIf(this.players.getLabel(), () -> this.allEntities.getValue());
      this.hideIf(this.mobs.getLabel(), () -> this.allEntities.getValue());
      this.setSuffix(this.model.getMode());
   }

   public static boolean shouldReplace(Entity entity) {
      if (mc.player == null) {
         return false;
      } else if (entity == mc.player) {
         return false;
      } else {
         EntityEditor module = getInstance();
         if (module == null || !module.isEnabled()) {
            return false;
         } else if (module.allEntities.getValue()) {
            return entity instanceof LivingEntity;
         } else {
            return module.players.getValue() && entity instanceof PlayerEntity ? true : module.mobs.getValue() && entity instanceof MobEntity;
         }
      }
   }

   public static EntityType<?> getTargetEntityType() {
      EntityEditor module = getInstance();
      if (module == null) {
         return EntityType.PIG;
      } else {
         return switch (module.model.getValue()) {
            case 1 -> EntityType.SHEEP;
            case 2 -> EntityType.WOLF;
            case 3 -> EntityType.CAMEL;
            case 4 -> EntityType.DONKEY;
            case 5 -> EntityType.CREEPER;
            case 6 -> EntityType.SKELETON;
            case 7 -> EntityType.ZOMBIE;
            case 8 -> EntityType.BLAZE;
            case 9 -> EntityType.IRON_GOLEM;
            default -> EntityType.PIG;
         };
      }
   }

   public static int getModelIndex() {
      EntityEditor module = getInstance();
      return module == null ? 0 : module.model.getValue();
   }

   private static EntityEditor getInstance() {
      try {
         return Southside.get().getModuleManager().get(EntityEditor.class);
      } catch (Exception var1) {
         return null;
      }
   }
}
