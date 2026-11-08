package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.manager.ChestManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.player.Blink;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.InventoryUtil;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.SlotUtils;
import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2336;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2480;
import net.minecraft.class_2623;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2682;
import net.minecraft.class_3965;

public class ChestWreck extends Module {
   public static final List<class_2248> invalidBlocks = Arrays.asList(
      class_2246.field_10485,
      class_2246.field_10034,
      class_2246.field_10443,
      class_2246.field_10380,
      class_2246.field_10336,
      class_2246.field_10158,
      class_2246.field_40284,
      class_2246.field_10375,
      class_2246.field_10523
   );
   private final Slider range = new Slider("Range", 4.0, 1.0, 8.0, 0.1);
   private final Slider minDelay = new Slider("Min Delay", 50, 0, 1000, 10);
   private final Slider maxDelay = new Slider("Max Delay", 150, 0, 1000, 10);
   private final Switch blockAllOpened = new Switch("Block All Opened", false);
   private final MSTimer timer = new MSTimer();
   private ChestWreck.SlotData slot;
   private boolean prevSneak;
   private boolean shouldPlace = false;
   private class_2338 chestPos = null;
   private int lastSlot = -1;
   private long currentDelay = 0L;
   private final List<class_2338> openedChests = new ArrayList<>();

   public ChestWreck() {
      super(Category.World, "ChestWreck", "esu");
   }

   @Override
   public void onEnable() {
      this.shouldPlace = false;
      this.prevSneak = false;
      this.openedChests.clear();
      this.chestPos = null;
      this.lastSlot = -1;
      this.slot = null;
      this.generateRandomDelay();
   }

   @EventHandler
   private void onRot(RotationEvent e) {
      if (mc.field_1755 != null) {
         this.timer.reset();
      } else if (this.timer.hasTimePassed(this.currentDelay)) {
         if ((KillAura.target == null || !Southside.get().getModuleManager().get(KillAura.class).isEnabled())
            && !mc.field_1724.method_6115()
            && !Southside.get().getModuleManager().get(Scaffold.class).isEnabled()
            && !Southside.get().getModuleManager().get(Blink.class).isEnabled()
            && mc.field_1724.field_6235 == 0) {
            if (this.shouldPlace && mc.field_1724.field_3936) {
               if (!(this.chestPos.method_10264() + 1 > mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376()))) {
                  class_1269 result = mc.field_1761
                     .method_2896(
                        mc.field_1724,
                        this.slot.hand,
                        new class_3965(Scaffold.getVec3(this.chestPos, class_2350.field_11036), class_2350.field_11036, this.chestPos, false)
                     );
                  if (result == class_1269.field_5812) {
                     mc.field_1724.method_6104(this.slot.hand());
                  }

                  if (!this.slot.hand.equals(class_1268.field_5810)) {
                     mc.field_1724.method_31548().field_7545 = this.lastSlot;
                  }
               }

               this.timer.reset();
               this.generateRandomDelay();
               mc.field_1690.field_1832.method_23481(this.prevSneak);
               mc.field_1724.method_5660(this.prevSneak);
               this.shouldPlace = false;
            } else {
               class_2338 nearest = this.findNextChest();
               if (nearest != null) {
                  this.lastSlot = mc.field_1724.method_31548().field_7545;
                  this.slot = null;
                  if (InventoryUtil.isFullBlock(mc.field_1724.method_6079()) && isValid(mc.field_1724.method_6079().method_7909())) {
                     this.slot = new ChestWreck.SlotData(45, class_1268.field_5810);
                  }

                  if (this.slot == null && InventoryUtil.isFullBlock(mc.field_1724.method_6047()) && isValid(mc.field_1724.method_6047().method_7909())) {
                     this.slot = new ChestWreck.SlotData(mc.field_1724.method_31548().field_7545, class_1268.field_5808);
                  }

                  if (this.slot == null) {
                     for (int i = 0; i <= 8; i++) {
                        class_1799 is = mc.field_1724.method_31548().method_5438(i);
                        if (InventoryUtil.isFullBlock(is) && isValid(is.method_7909())) {
                           this.slot = new ChestWreck.SlotData(i, class_1268.field_5808);
                        }
                     }
                  }

                  if (this.slot == null || !this.slot.check()) {
                     return;
                  }

                  if (nearest.method_10264() + 1 < mc.field_1724.method_23318() + mc.field_1724.method_18381(mc.field_1724.method_18376())) {
                     class_243 minPos = null;
                     double lastDis = Double.MAX_VALUE;

                     for (double x = 0.1; x <= 0.9; x += 0.1) {
                        for (double z = 0.1; z <= 0.9; z += 0.1) {
                           class_243 pos = new class_243(nearest.method_10263() + x, nearest.method_10264() + 1, nearest.method_10260() + z);
                           double dis = mc.field_1724.method_5836(1.0F).method_1025(pos);
                           if (dis < lastDis) {
                              minPos = pos;
                              lastDis = dis;
                           }
                        }
                     }

                     if (minPos != null) {
                        Rotation rot = RotationUtils.calculate(minPos);
                        class_3965 raycast = RayCastUtils.raycast(4.5, rot, false, 1.0F);
                        if (raycast != null && raycast.method_17777().equals(nearest)) {
                           RotationUtils.setRotation(rot);
                           if (!this.slot.hand.equals(class_1268.field_5810)) {
                              mc.field_1724.method_31548().field_7545 = this.slot.slot;
                           }

                           this.chestPos = nearest;
                           this.shouldPlace = true;
                           this.prevSneak = mc.field_1690.field_1832.method_1434();
                           mc.field_1690.field_1832.method_23481(true);
                           mc.field_1724.method_5660(true);
                        }
                     }
                  }
               } else {
                  this.timer.reset();
               }
            }
         } else {
            this.timer.reset();
         }
      }
   }

   private class_2338 findNextChest() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 playerPos = mc.field_1724.method_24515();
         class_243 playerVec = mc.field_1724.method_19538();
         int rangeInt = (int)Math.ceil(this.range.getValue().floatValue());
         class_2338 nearestChest = null;
         double nearestDistance = Double.MAX_VALUE;
         ChestManager manager = Southside.get().getChestManager();

         for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
               for (int z = -rangeInt; z <= rangeInt; z++) {
                  class_2338 pos = playerPos.method_10069(x, y, z);
                  double distance = playerVec.method_1022(class_243.method_24953(pos));
                  if (!(distance > this.range.getValue().floatValue())
                     && (this.blockAllOpened.getValue() ? manager.isStolen(pos) : manager.isSelfStolen(pos))
                     && this.openedChests.contains(pos)
                     && this.isChestBlock(pos)
                     && !isChestBlocked(pos)
                     && this.isSpaceEmpty(pos.method_10084())
                     && distance < nearestDistance) {
                     nearestChest = pos;
                     nearestDistance = distance;
                  }
               }
            }
         }

         return nearestChest;
      } else {
         return null;
      }
   }

   public static boolean isChestBlocked(class_2338 pos) {
      return mc.field_1687 == null ? false : isValidBlock(mc.field_1687.method_8320(pos.method_10084()).method_26204());
   }

   private boolean isSpaceEmpty(class_2338 pos) {
      return mc.field_1687 == null ? false : mc.field_1687.method_8587(mc.field_1724, new class_238(pos));
   }

   private boolean isChestBlock(class_2338 pos) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         class_2680 state = mc.field_1687.method_8320(pos);
         return state.method_26204() instanceof class_2281 || state.method_26204() instanceof class_2336 || state.method_26204() instanceof class_2480;
      }
   }

   private static boolean isValid(class_1792 item) {
      return item instanceof class_1747 && !invalidBlocks.contains(((class_1747)item).method_7711()) && SlotUtils.isGoodForBridging(item);
   }

   private static boolean isValidBlock(class_2248 b) {
      return !invalidBlocks.contains(b) && SlotUtils.isGoodForBridging(b);
   }

   private void generateRandomDelay() {
      int min = this.minDelay.getValue().intValue();
      int max = this.maxDelay.getValue().intValue();
      if (min > max) {
         this.currentDelay = min;
      } else {
         this.currentDelay = min + RandomUtils.nextInt(0, max - min + 1);
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1687 != null) {
         if (event.getPacket() instanceof class_2623 sb) {
            class_2680 state = mc.field_1687.method_8320(sb.method_11298());
            if (state.method_26204() instanceof class_2281 && sb.method_11294() == 1 && sb.method_11296() == 1) {
               this.openedChests.add(sb.method_11298());
            }
         }
      }
   }

   record SlotData(int slot, class_1268 hand) {
      public boolean check() {
         if (this.hand.equals(class_1268.field_5810)) {
            class_1799 stack = ChestWreck.mc.field_1724.method_6079();
            boolean success = !stack.method_7960() && stack.method_7909() instanceof class_1747;
            if (success) {
               return this.checkBlocked(class_2248.method_9503(stack.method_7909()));
            }
         }

         class_1799 stack = ChestWreck.mc.field_1724.method_31548().method_5438(this.slot);
         boolean success = !stack.method_7960() && stack.method_7909() instanceof class_1747;
         return success ? this.checkBlocked(class_2248.method_9503(stack.method_7909())) : false;
      }

      public boolean checkBlocked(class_2248 block) {
         class_2680 state = block.method_9564();
         class_265 shape = state.method_26218(class_2682.field_12294, class_2338.field_10980);
         return class_2248.method_9501(shape, class_2350.field_11033);
      }
   }
}
