package dev.southside.module.impl.misc;

import dev.southside.event.Stage;
import dev.southside.event.events.InteractItemEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class FuckOffHand extends Module {
   boolean swapBack = false;
   boolean mainHandUsed = false;

   public FuckOffHand() {
      super(Category.Misc, "FuckOffHand");
   }

   @EventHandler
   public void onInteractItem(InteractItemEvent event) {
      if (mc.player != null
         && mc.interactionManager != null
         && mc.crosshairTarget != null
         && mc.crosshairTarget.getType().equals(Type.MISS)
         && this.isValidItem(mc.player.getOffHandStack())) {
         if (!event.getItem().getItem().equals(Items.FIRE_CHARGE)) {
            if (event.getInteractHand().equals(Hand.MAIN_HAND) && !this.isValidItem(event.getItem())) {
               ActionResult result = event.getItem().copy().getItem().use(mc.world, mc.player, Hand.MAIN_HAND);
               if (!(result instanceof Success)) {
                  event.setCancelled(true);
               }
            } else {
               if (event.getInteractHand().equals(Hand.OFF_HAND) && this.isValidItem(event.getItem())) {
                  event.setCancelled(true);
                  PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                  ItemStack stack = mc.player.getMainHandStack().copy();
                  mc.player.setStackInHand(Hand.MAIN_HAND, mc.player.getOffHandStack());
                  mc.player.setStackInHand(Hand.OFF_HAND, stack);
                  if (mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND) instanceof Success success3) {
                     if (success3.swingSource() == SwingSource.CLIENT) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                     }

                     mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
                  }
               }

               this.swapBack = true;
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState().equals(Stage.PRE)) {
         if (this.swapBack) {
            this.swapBack = false;
            if (mc.player != null && !this.isValidItem(mc.player.getOffHandStack())) {
               PacketUtils.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            }
         }
      } else {
         this.mainHandUsed = false;
      }
   }

   public boolean isValidItem(ItemStack itemStack) {
      return itemStack == null ? false : itemStack.getItem().equals(Items.SNOWBALL) || itemStack.getItem().equals(Items.EGG);
   }
}
