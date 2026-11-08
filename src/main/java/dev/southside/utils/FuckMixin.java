package dev.southside.utils;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.events.InteractItemEvent;
import dev.southside.event.events.ItemUseEvent;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_1269.class_9857;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;
import net.minecraft.class_239.class_240;

public class FuckMixin implements MinecraftAccessor {
   private static boolean callItemUse() {
      ItemUseEvent event = new ItemUseEvent();
      Southside.INSTANCE.getEventBus().call(event);
      return event.isCancelled();
   }

   private static boolean callInteractItem(class_1268 hand, class_1799 itemStack) {
      InteractItemEvent interactItemEvent = new InteractItemEvent(hand, itemStack);
      Southside.INSTANCE.getEventBus().call(interactItemEvent);
      return interactItemEvent.isCancelled();
   }

   public static void doItemUse() {
      if (!callItemUse()) {
         if (!mc.field_1761.method_2923()) {
            mc.field_1752 = 4;
            if (!mc.field_1724.method_3144()) {
               for (class_1268 hand : class_1268.values()) {
                  class_1799 itemStack = mc.field_1724.method_5998(hand);
                  if (!itemStack.method_45435(mc.field_1687.method_45162())) {
                     return;
                  }

                  if (mc.field_1765 != null) {
                     if (mc.field_1765.method_17783().equals(class_240.field_1331)) {
                        class_3966 entityHitResult = (class_3966)mc.field_1765;
                        class_1297 entity = entityHitResult.method_17782();
                        if (!mc.field_1687.method_8621().method_11952(entity.method_24515())) {
                           return;
                        }

                        class_1269 actionResult = mc.field_1761.method_2917(mc.field_1724, entity, entityHitResult, hand);
                        if (!actionResult.method_23665()) {
                           actionResult = mc.field_1761.method_2905(mc.field_1724, entity, hand);
                        }

                        if (actionResult instanceof class_9860 success) {
                           if (success.comp_2909() == class_9861.field_52427) {
                              mc.field_1724.method_6104(hand);
                           }

                           return;
                        }
                     }

                     if (mc.field_1765.method_17783().equals(class_240.field_1332)) {
                        class_3965 blockHitResult = (class_3965)mc.field_1765;
                        int i = itemStack.method_7947();
                        class_1269 actionResult2 = mc.field_1761.method_2896(mc.field_1724, hand, blockHitResult);
                        if (actionResult2 instanceof class_9860 success2) {
                           if (success2.comp_2909() == class_9861.field_52427) {
                              mc.field_1724.method_6104(hand);
                              if (!itemStack.method_7960() && (itemStack.method_7947() != i || mc.field_1761.method_2914())) {
                                 mc.field_1773.field_4012.method_3215(hand);
                              }
                           }

                           return;
                        }

                        if (actionResult2 instanceof class_9857) {
                           return;
                        }
                     }
                  }

                  if (!itemStack.method_7960()
                     && !callInteractItem(hand, itemStack)
                     && mc.field_1761.method_2919(mc.field_1724, hand) instanceof class_9860 success3) {
                     if (success3.comp_2909() == class_9861.field_52427) {
                        mc.field_1724.method_6104(hand);
                     }

                     mc.field_1773.field_4012.method_3215(hand);
                     return;
                  }
               }
            }
         }
      }
   }
}
