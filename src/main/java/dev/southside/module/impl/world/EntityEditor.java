package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1657;

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

   public static boolean shouldReplace(class_1297 entity) {
      if (mc.field_1724 == null) {
         return false;
      } else if (entity == mc.field_1724) {
         return false;
      } else {
         EntityEditor module = getInstance();
         if (module == null || !module.isEnabled()) {
            return false;
         } else if (module.allEntities.getValue()) {
            return entity instanceof class_1309;
         } else {
            return module.players.getValue() && entity instanceof class_1657 ? true : module.mobs.getValue() && entity instanceof class_1308;
         }
      }
   }

   public static class_1299<?> getTargetEntityType() {
      EntityEditor module = getInstance();
      if (module == null) {
         return class_1299.field_6093;
      } else {
         return switch (module.model.getValue()) {
            case 1 -> class_1299.field_6115;
            case 2 -> class_1299.field_6055;
            case 3 -> class_1299.field_40116;
            case 4 -> class_1299.field_6067;
            case 5 -> class_1299.field_6046;
            case 6 -> class_1299.field_6137;
            case 7 -> class_1299.field_6051;
            case 8 -> class_1299.field_6099;
            case 9 -> class_1299.field_6147;
            default -> class_1299.field_6093;
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
