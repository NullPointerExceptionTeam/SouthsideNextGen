package dev.southside.utils.item;

import dev.southside.MinecraftAccessor;
import java.util.Objects;
import net.minecraft.class_10192;
import net.minecraft.class_10394;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1320;
import net.minecraft.class_1766;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1829;
import net.minecraft.class_1839;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_9285;
import net.minecraft.class_9334;

public class ItemUtils {
   public static boolean instanceofToolItem(class_1792 item) {
      return item instanceof class_1766 || item instanceof class_1829;
   }

   public static class_1304 getEquippableSlotType(class_1799 item) {
      return ((class_10192)Objects.requireNonNull((class_10192)item.method_57824(class_9334.field_54196))).comp_3174();
   }

   public static class_5321<class_10394> getArmorAsset(class_1799 item) {
      class_10192 component = (class_10192)item.method_57824(class_9334.field_54196);
      return component != null ? (class_5321)component.comp_3176().orElse(null) : null;
   }

   public static double armorAttributeValueWithStack(class_1799 stack, class_6880<class_1320> attribute) {
      class_1304 slot = getEquippableSlotType(stack);
      double base = MinecraftAccessor.mc.field_1724.method_6127().method_45331(attribute) ? MinecraftAccessor.mc.field_1724.method_45326(attribute) : 0.0;
      class_9285 component = (class_9285)stack.method_57825(class_9334.field_49636, class_9285.field_49326);
      return component.method_57481(base, slot);
   }

   public static double handAttributeValueWithStack(class_1799 stack, class_6880<class_1320> attribute, class_1268 hand) {
      class_1304 slot = hand.equals(class_1268.field_5808) ? class_1304.field_6173 : class_1304.field_6171;
      double base = MinecraftAccessor.mc.field_1724.method_6127().method_45331(attribute) ? MinecraftAccessor.mc.field_1724.method_45326(attribute) : 0.0;
      class_9285 component = (class_9285)stack.method_57825(class_9334.field_49636, class_9285.field_49326);
      return component.method_57481(base, slot);
   }

   public static boolean isConsumable(class_1799 stack) {
      class_1839 action = stack.method_7976();
      return !action.equals(class_1839.field_8952);
   }
}
