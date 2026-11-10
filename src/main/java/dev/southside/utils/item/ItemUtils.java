package dev.southside.utils.item;

import dev.southside.MinecraftAccessor;
import java.util.Objects;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.DataComponentTypes;

public class ItemUtils {
   public static boolean instanceofToolItem(Item item) {
      return item instanceof MiningToolItem || item instanceof SwordItem;
   }

   public static EquipmentSlot getEquippableSlotType(ItemStack item) {
      return ((EquippableComponent)Objects.requireNonNull((EquippableComponent)item.get(DataComponentTypes.EQUIPPABLE))).slot();
   }

   public static RegistryKey<EquipmentAsset> getArmorAsset(ItemStack item) {
      EquippableComponent component = (EquippableComponent)item.get(DataComponentTypes.EQUIPPABLE);
      return component != null ? (RegistryKey)component.assetId().orElse(null) : null;
   }

   public static double armorAttributeValueWithStack(ItemStack stack, RegistryEntry<EntityAttribute> attribute) {
      EquipmentSlot slot = getEquippableSlotType(stack);
      double base = MinecraftAccessor.mc.player.getAttributes().hasAttribute(attribute) ? MinecraftAccessor.mc.player.getAttributeBaseValue(attribute) : 0.0;
      AttributeModifiersComponent component = (AttributeModifiersComponent)stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      return component.applyOperations(base, slot);
   }

   public static double handAttributeValueWithStack(ItemStack stack, RegistryEntry<EntityAttribute> attribute, Hand hand) {
      EquipmentSlot slot = hand.equals(Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
      double base = MinecraftAccessor.mc.player.getAttributes().hasAttribute(attribute) ? MinecraftAccessor.mc.player.getAttributeBaseValue(attribute) : 0.0;
      AttributeModifiersComponent component = (AttributeModifiersComponent)stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      return component.applyOperations(base, slot);
   }

   public static boolean isConsumable(ItemStack stack) {
      UseAction action = stack.getUseAction();
      return !action.equals(UseAction.NONE);
   }
}
