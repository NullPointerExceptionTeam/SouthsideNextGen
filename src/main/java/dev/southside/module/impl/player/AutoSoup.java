package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.InventoryUtils;
import dev.southside.utils.timer.StopWatch;
import net.minecraft.util.Hand;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class AutoSoup extends Module {
   public Slider health = new Slider("Health", 15, 0, 20, 1);
   public Slider minDelay = new Slider("Min Delay", 300, 0, 1000, 1);
   public Slider maxDelay = new Slider("Max Delay", 500, 0, 1000, 1);
   public Switch autoRefill = new Switch("Auto Refill", false);
   private StopWatch timer = new StopWatch();
   private StopWatch refillTimer = new StopWatch();
   private int soup = -1;
   private int oldSlot;

   public AutoSoup() {
      super(Category.Player, "AutoSoup", "自动喝汤");
   }

   @Override
   public void onDisable() {
      this.soup = -1;
   }

   @EventHandler
   private void onRotationApplied(RotationAppliedEvent event) {
      if (this.autoRefill.getValue() && !mc.player.lastSprinting && this.refillTimer.finished(50L)) {
         this.refillSoup();
         this.refillTimer.reset();
      }

      if (this.timer.finished(this.getRandomDelay()) && mc.player.age > 10 && mc.player.getHealth() < this.health.getValue().floatValue()) {
         this.soup = this.findSoup();
         if (this.soup != -1) {
            this.oldSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = this.soup;
            if (mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND) instanceof Success success3
               && success3.swingSource() == SwingSource.CLIENT) {
               PacketUtils.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }

            ItemStack afterUse = mc.player.getInventory().getStack(this.soup);
            if (afterUse.getItem() == Items.BOWL || afterUse.getItem() == Items.MUSHROOM_STEW) {
               mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
               mc.player.getInventory().setStack(this.soup, ItemStack.EMPTY);
            }

            mc.player.getInventory().selectedSlot = this.oldSlot;
            mc.interactionManager.syncSelectedSlot();
         }

         this.timer.reset();
      }
   }

   private int findSoup() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).getItem() == Items.MUSHROOM_STEW) {
            return i;
         }
      }

      return -1;
   }

   private long getRandomDelay() {
      double min = this.minDelay.getValue().doubleValue();
      double max = this.maxDelay.getValue().doubleValue();
      return (long)(min + Math.random() * (max - min));
   }

   private void refillSoup() {
      int soupSlot = InventoryUtils.findItem(9, 36, Items.MUSHROOM_STEW);
      if (soupSlot != -1) {
         int emptySlot = this.findEmptyHotbarSlot();
         if (emptySlot != -1) {
            mc.interactionManager.clickSlot(0, soupSlot, emptySlot, SlotActionType.SWAP, mc.player);
         }
      }
   }

   private int findEmptyHotbarSlot() {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getStack(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }
}
