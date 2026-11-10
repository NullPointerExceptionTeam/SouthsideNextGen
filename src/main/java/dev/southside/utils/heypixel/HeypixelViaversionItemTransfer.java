package dev.southside.utils.heypixel;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.s2c.play.SetCursorItemS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.entity.data.DataTracker.SerializedEntry;

public final class HeypixelViaversionItemTransfer {
   private static final String MACE_MARKER = "1.21 Mace";
   private static final String WIND_CHARGE_MARKER = "1.21 Wind Charge";

   private HeypixelViaversionItemTransfer() {
   }

   public static Packet<?> transfer(Packet<?> packet) {
      if (packet == null) {
         return packet;
      } else if (packet instanceof InventoryS2CPacket inventoryPacket) {
         return transferInventoryPacket(inventoryPacket);
      } else {
         if (packet instanceof ScreenHandlerSlotUpdateS2CPacket slotPacket) {
            ItemStack stack = transferStack(slotPacket.getStack());
            if (stack != slotPacket.getStack()) {
               return new ScreenHandlerSlotUpdateS2CPacket(slotPacket.getSyncId(), slotPacket.getRevision(), slotPacket.getSlot(), stack);
            }
         }

         if (packet instanceof SetPlayerInventoryS2CPacket playerInventoryPacket) {
            ItemStack stack = transferStack(playerInventoryPacket.contents());
            if (stack != playerInventoryPacket.contents()) {
               return new SetPlayerInventoryS2CPacket(playerInventoryPacket.slot(), stack);
            }
         }

         if (packet instanceof SetCursorItemS2CPacket cursorPacket) {
            ItemStack stack = transferStack(cursorPacket.contents());
            if (stack != cursorPacket.contents()) {
               return new SetCursorItemS2CPacket(stack);
            }
         }

         if (packet instanceof EntityEquipmentUpdateS2CPacket equipmentPacket) {
            return transferEquipmentPacket(equipmentPacket);
         } else {
            return packet instanceof EntityTrackerUpdateS2CPacket trackerPacket ? transferTrackerPacket(trackerPacket) : packet;
         }
      }
   }

   private static Packet<?> transferInventoryPacket(InventoryS2CPacket packet) {
      boolean changed = false;
      List<ItemStack> contents = packet.getContents();
      DefaultedList<ItemStack> transferredContents = DefaultedList.ofSize(contents.size(), ItemStack.EMPTY);

      for (int i = 0; i < contents.size(); i++) {
         ItemStack oldStack = contents.get(i);
         ItemStack newStack = transferStack(oldStack);
         transferredContents.set(i, newStack);
         changed |= newStack != oldStack;
      }

      ItemStack oldCursor = packet.getCursorStack();
      ItemStack newCursor = transferStack(oldCursor);
      changed |= newCursor != oldCursor;
      return !changed ? packet : new InventoryS2CPacket(packet.getSyncId(), packet.getRevision(), transferredContents, newCursor);
   }

   private static Packet<?> transferEquipmentPacket(EntityEquipmentUpdateS2CPacket packet) {
      boolean changed = false;
      List<Pair<EquipmentSlot, ItemStack>> transferred = new ArrayList<>(packet.getEquipmentList().size());

      for (Pair<EquipmentSlot, ItemStack> pair : packet.getEquipmentList()) {
         ItemStack oldStack = (ItemStack)pair.getSecond();
         ItemStack newStack = transferStack(oldStack);
         transferred.add(Pair.of((EquipmentSlot)pair.getFirst(), newStack));
         changed |= newStack != oldStack;
      }

      return !changed ? packet : new EntityEquipmentUpdateS2CPacket(packet.getEntityId(), transferred);
   }

   private static Packet<?> transferTrackerPacket(EntityTrackerUpdateS2CPacket packet) {
      boolean changed = false;
      List<SerializedEntry<?>> transferred = new ArrayList<>(packet.trackedValues().size());

      for (SerializedEntry<?> entry : packet.trackedValues()) {
         if (entry.value() instanceof ItemStack oldStack) {
            ItemStack newStack = transferStack(oldStack);
            if (newStack != oldStack) {
               transferred.add(createSerializedEntry(entry, newStack));
               changed = true;
               continue;
            }
         }

         transferred.add(entry);
      }

      return !changed ? packet : new EntityTrackerUpdateS2CPacket(packet.id(), transferred);
   }

   private static SerializedEntry<ItemStack> createSerializedEntry(SerializedEntry<?> entry, ItemStack stack) {
      return new SerializedEntry(entry.id(), entry.handler(), stack);
   }

   private static ItemStack transferStack(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return stack;
      } else if (hasViaMarker(stack, "1.21 Mace")) {
         return rebuildStack(stack, Items.MACE);
      } else {
         return hasViaMarker(stack, "1.21 Wind Charge") ? rebuildStack(stack, Items.WIND_CHARGE) : stack;
      }
   }

   private static ItemStack rebuildStack(ItemStack stack, Item targetItem) {
      ItemStack transferred = new ItemStack(targetItem, stack.getCount());
      copySafeComponents(stack, transferred);
      return transferred;
   }

   private static void copySafeComponents(ItemStack from, ItemStack to) {
      copyComponent(from, to, DataComponentTypes.CUSTOM_DATA);
      copyComponent(from, to, DataComponentTypes.LORE);
      copyComponent(from, to, DataComponentTypes.ENCHANTMENTS);
      copyComponent(from, to, DataComponentTypes.UNBREAKABLE);
      copyComponent(from, to, DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
      copyComponent(from, to, DataComponentTypes.REPAIR_COST);
      copyCustomName(from, to);
   }

   private static <T> void copyComponent(ItemStack from, ItemStack to, ComponentType<T> type) {
      T value = (T)from.get(type);
      if (value != null) {
         to.set(type, value);
      }
   }

   private static void copyCustomName(ItemStack from, ItemStack to) {
      if (from.getCustomName() != null) {
         String customName = from.getCustomName().getString();
         if (!customName.contains("1.21 Mace") && !customName.contains("1.21 Wind Charge")) {
            to.set(DataComponentTypes.CUSTOM_NAME, from.getCustomName());
         }
      }
   }

   private static boolean hasViaMarker(ItemStack stack, String marker) {
      NbtComponent customData = (NbtComponent)stack.get(DataComponentTypes.CUSTOM_DATA);
      return customData != null && !customData.isEmpty() ? customData.copyNbt().toString().contains(marker) : false;
   }
}
