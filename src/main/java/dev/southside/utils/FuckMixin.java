package dev.southside.utils;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.events.InteractItemEvent;
import dev.southside.event.events.ItemUseEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.ActionResult.Fail;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.hit.HitResult.Type;

public class FuckMixin implements MinecraftAccessor {
   private static boolean callItemUse() {
      ItemUseEvent event = new ItemUseEvent();
      Southside.INSTANCE.getEventBus().call(event);
      return event.isCancelled();
   }

   private static boolean callInteractItem(Hand hand, ItemStack itemStack) {
      InteractItemEvent interactItemEvent = new InteractItemEvent(hand, itemStack);
      Southside.INSTANCE.getEventBus().call(interactItemEvent);
      return interactItemEvent.isCancelled();
   }

   public static void doItemUse() {
      if (!callItemUse()) {
         if (!mc.interactionManager.isBreakingBlock()) {
            mc.itemUseCooldown = 4;
            if (!mc.player.isRiding()) {
               for (Hand hand : Hand.values()) {
                  ItemStack itemStack = mc.player.getStackInHand(hand);
                  if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
                     return;
                  }

                  if (mc.crosshairTarget != null) {
                     if (mc.crosshairTarget.getType().equals(Type.ENTITY)) {
                        EntityHitResult entityHitResult = (EntityHitResult)mc.crosshairTarget;
                        Entity entity = entityHitResult.getEntity();
                        if (!mc.world.getWorldBorder().contains(entity.getBlockPos())) {
                           return;
                        }

                        ActionResult actionResult = mc.interactionManager.interactEntityAtLocation(mc.player, entity, entityHitResult, hand);
                        if (!actionResult.isAccepted()) {
                           actionResult = mc.interactionManager.interactEntity(mc.player, entity, hand);
                        }

                        if (actionResult instanceof Success success) {
                           if (success.swingSource() == SwingSource.CLIENT) {
                              mc.player.swingHand(hand);
                           }

                           return;
                        }
                     }

                     if (mc.crosshairTarget.getType().equals(Type.BLOCK)) {
                        BlockHitResult blockHitResult = (BlockHitResult)mc.crosshairTarget;
                        int i = itemStack.getCount();
                        ActionResult actionResult2 = mc.interactionManager.interactBlock(mc.player, hand, blockHitResult);
                        if (actionResult2 instanceof Success success2) {
                           if (success2.swingSource() == SwingSource.CLIENT) {
                              mc.player.swingHand(hand);
                              if (!itemStack.isEmpty() && (itemStack.getCount() != i || mc.interactionManager.hasCreativeInventory())) {
                                 mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                              }
                           }

                           return;
                        }

                        if (actionResult2 instanceof Fail) {
                           return;
                        }
                     }
                  }

                  if (!itemStack.isEmpty()
                     && !callInteractItem(hand, itemStack)
                     && mc.interactionManager.interactItem(mc.player, hand) instanceof Success success3) {
                     if (success3.swingSource() == SwingSource.CLIENT) {
                        mc.player.swingHand(hand);
                     }

                     mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                     return;
                  }
               }
            }
         }
      }
   }
}
