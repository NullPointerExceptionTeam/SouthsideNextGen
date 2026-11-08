package dev.southside.utils.heypixel;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2371;
import net.minecraft.class_2596;
import net.minecraft.class_2649;
import net.minecraft.class_2653;
import net.minecraft.class_2739;
import net.minecraft.class_2744;
import net.minecraft.class_9279;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.minecraft.class_9834;
import net.minecraft.class_9835;
import net.minecraft.class_2945.class_7834;

public final class HeypixelViaversionItemTransfer {
   private static final String MACE_MARKER = "1.21 Mace";
   private static final String WIND_CHARGE_MARKER = "1.21 Wind Charge";

   private HeypixelViaversionItemTransfer() {
   }

   public static class_2596<?> transfer(class_2596<?> packet) {
      if (packet == null) {
         return packet;
      } else if (packet instanceof class_2649 inventoryPacket) {
         return transferInventoryPacket(inventoryPacket);
      } else {
         if (packet instanceof class_2653 slotPacket) {
            class_1799 stack = transferStack(slotPacket.method_11449());
            if (stack != slotPacket.method_11449()) {
               return new class_2653(slotPacket.method_11452(), slotPacket.method_37439(), slotPacket.method_11450(), stack);
            }
         }

         if (packet instanceof class_9835 playerInventoryPacket) {
            class_1799 stack = transferStack(playerInventoryPacket.comp_2892());
            if (stack != playerInventoryPacket.comp_2892()) {
               return new class_9835(playerInventoryPacket.comp_2891(), stack);
            }
         }

         if (packet instanceof class_9834 cursorPacket) {
            class_1799 stack = transferStack(cursorPacket.comp_2890());
            if (stack != cursorPacket.comp_2890()) {
               return new class_9834(stack);
            }
         }

         if (packet instanceof class_2744 equipmentPacket) {
            return transferEquipmentPacket(equipmentPacket);
         } else {
            return packet instanceof class_2739 trackerPacket ? transferTrackerPacket(trackerPacket) : packet;
         }
      }
   }

   private static class_2596<?> transferInventoryPacket(class_2649 packet) {
      boolean changed = false;
      List<class_1799> contents = packet.method_11441();
      class_2371<class_1799> transferredContents = class_2371.method_10213(contents.size(), class_1799.field_8037);

      for (int i = 0; i < contents.size(); i++) {
         class_1799 oldStack = contents.get(i);
         class_1799 newStack = transferStack(oldStack);
         transferredContents.set(i, newStack);
         changed |= newStack != oldStack;
      }

      class_1799 oldCursor = packet.method_37437();
      class_1799 newCursor = transferStack(oldCursor);
      changed |= newCursor != oldCursor;
      return !changed ? packet : new class_2649(packet.method_11440(), packet.method_37438(), transferredContents, newCursor);
   }

   private static class_2596<?> transferEquipmentPacket(class_2744 packet) {
      boolean changed = false;
      List<Pair<class_1304, class_1799>> transferred = new ArrayList<>(packet.method_30145().size());

      for (Pair<class_1304, class_1799> pair : packet.method_30145()) {
         class_1799 oldStack = (class_1799)pair.getSecond();
         class_1799 newStack = transferStack(oldStack);
         transferred.add(Pair.of((class_1304)pair.getFirst(), newStack));
         changed |= newStack != oldStack;
      }

      return !changed ? packet : new class_2744(packet.method_11820(), transferred);
   }

   private static class_2596<?> transferTrackerPacket(class_2739 packet) {
      boolean changed = false;
      List<class_7834<?>> transferred = new ArrayList<>(packet.comp_1128().size());

      for (class_7834<?> entry : packet.comp_1128()) {
         if (entry.comp_1117() instanceof class_1799 oldStack) {
            class_1799 newStack = transferStack(oldStack);
            if (newStack != oldStack) {
               transferred.add(createSerializedEntry(entry, newStack));
               changed = true;
               continue;
            }
         }

         transferred.add(entry);
      }

      return !changed ? packet : new class_2739(packet.comp_1127(), transferred);
   }

   private static class_7834<class_1799> createSerializedEntry(class_7834<?> entry, class_1799 stack) {
      return new class_7834(entry.comp_1115(), entry.comp_1116(), stack);
   }

   private static class_1799 transferStack(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return stack;
      } else if (hasViaMarker(stack, "1.21 Mace")) {
         return rebuildStack(stack, class_1802.field_49814);
      } else {
         return hasViaMarker(stack, "1.21 Wind Charge") ? rebuildStack(stack, class_1802.field_49098) : stack;
      }
   }

   private static class_1799 rebuildStack(class_1799 stack, class_1792 targetItem) {
      class_1799 transferred = new class_1799(targetItem, stack.method_7947());
      copySafeComponents(stack, transferred);
      return transferred;
   }

   private static void copySafeComponents(class_1799 from, class_1799 to) {
      copyComponent(from, to, class_9334.field_49628);
      copyComponent(from, to, class_9334.field_49632);
      copyComponent(from, to, class_9334.field_49633);
      copyComponent(from, to, class_9334.field_49630);
      copyComponent(from, to, class_9334.field_49641);
      copyComponent(from, to, class_9334.field_49639);
      copyCustomName(from, to);
   }

   private static <T> void copyComponent(class_1799 from, class_1799 to, class_9331<T> type) {
      T value = (T)from.method_57824(type);
      if (value != null) {
         to.method_57379(type, value);
      }
   }

   private static void copyCustomName(class_1799 from, class_1799 to) {
      if (from.method_65130() != null) {
         String customName = from.method_65130().getString();
         if (!customName.contains("1.21 Mace") && !customName.contains("1.21 Wind Charge")) {
            to.method_57379(class_9334.field_49631, from.method_65130());
         }
      }
   }

   private static boolean hasViaMarker(class_1799 stack, String marker) {
      class_9279 customData = (class_9279)stack.method_57824(class_9334.field_49628);
      return customData != null && !customData.method_57458() ? customData.method_57461().toString().contains(marker) : false;
   }
}
