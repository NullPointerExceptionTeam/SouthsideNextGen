package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.southside.Southside;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1682;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_2846;
import net.minecraft.class_2886;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2846.class_2847;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class AutoRod extends Module {
   private final Slider delay = new Slider("Delay", 500, 100, 1000, 10);
   private final Slider minRange = new Slider("MinRange", 3.5, 3, 20, 0.1);
   private final Slider maxRange = new Slider("MaxRange", 12, 3, 20, 0.1);
   private final Slider fov = new Slider("Fov", 180.0F, 0.0F, 180.0F, 1.0F);
   private final Slider extraPredict = new Slider("Predict Ticks", 2, 0, 5, 1);
   private final Slider keepTime = new Slider("KeepTime", 500, 100, 2000, 10);
   private static final double FISHING_HOOK_SPEED = 1.5;
   private static final double GRAVITY = 0.03;
   private Rotation rotation;
   private final MSTimer timer = new MSTimer();
   public boolean isRodActive;
   private long reelTime;
   private int previousSlot = -1;
   private class_1268 rodHand;
   private class_1309 currentTarget;
   public int rodState = 0;
   private Map<class_1682, Long> thrownEntities = new HashMap<>();

   public AutoRod() {
      super(Category.Combat, "AutoRod", "Automatically uses fishing rods");
   }

   @Override
   public void initPostRunnable() {
      this.addRangedValueRestrict(this.minRange, this.maxRange);
   }

   @Override
   public void onEnable() {
      this.timer.reset();
      this.rotation = null;
      this.rodHand = null;
      this.currentTarget = null;
      this.rodState = 0;
      this.isRodActive = false;
      this.thrownEntities.clear();
   }

   @EventHandler
   public void onRotation(RotationEvent event) {
      if (!ClientUtils.nullCheck()) {
         if (mc.field_1755 == null) {
            if (!mc.field_1724.method_6115()) {
               if (!Southside.INSTANCE.getModuleManager().get(Scaffold.class).isEnabled()) {
                  long currentTime = System.currentTimeMillis();
                  if (this.isRodActive) {
                     if (this.currentTarget != null
                        && mc.field_1724.method_19538().method_1022(this.currentTarget.method_19538()) < this.minRange.getValue().floatValue()) {
                        mc.field_1761.method_2919(mc.field_1724, this.rodHand);
                        if (this.previousSlot != -1) {
                           mc.field_1724.method_31548().field_7545 = this.previousSlot;
                           this.previousSlot = -1;
                        }

                        this.isRodActive = false;
                        this.rodState = 0;
                        this.rotation = null;
                        this.rodHand = null;
                        this.currentTarget = null;
                     } else if (currentTime >= this.reelTime) {
                        mc.field_1761.method_2919(mc.field_1724, this.rodHand);
                        if (this.previousSlot != -1) {
                           mc.field_1724.method_31548().field_7545 = this.previousSlot;
                           this.previousSlot = -1;
                        }

                        this.isRodActive = false;
                        this.rodState = 0;
                        this.rotation = null;
                        this.rodHand = null;
                        this.currentTarget = null;
                     }
                  } else {
                     Map<class_1682, Long> newThrown = new HashMap<>();

                     for (class_1297 entity : mc.field_1687.method_18112()) {
                        if (entity instanceof class_1682 thrown) {
                           if (this.thrownEntities.containsKey(thrown)) {
                              newThrown.put(thrown, this.thrownEntities.get(thrown));
                           }

                           newThrown.put(thrown, System.currentTimeMillis());
                        }
                     }

                     this.thrownEntities = newThrown;
                     if (this.rodState == 1) {
                        int rodSlot = this.findRodSlot();
                        if (rodSlot == -1) {
                           this.rodState = 0;
                           return;
                        }

                        this.rodHand = rodSlot == -2 ? class_1268.field_5810 : class_1268.field_5808;
                        if (rodSlot != -2) {
                           this.previousSlot = mc.field_1724.method_31548().field_7545;
                           mc.field_1724.method_31548().field_7545 = rodSlot;
                        }

                        mc.field_1761.method_2919(mc.field_1724, rodSlot == -2 ? class_1268.field_5810 : class_1268.field_5808);
                        this.isRodActive = true;
                        this.rodState = 0;
                        this.rotation = null;
                     } else {
                        if (!this.timer.hasTimePassed(this.delay.getValue().intValue())) {
                           return;
                        }

                        List<class_1297> targets = (KillAura.target == null
                              ? TargetUtils.getTargets(this.minRange.getValue().floatValue(), this.maxRange.getValue().floatValue())
                              : KillAura.targets)
                           .stream()
                           .filter(entityx -> RotationUtils.inFoV(entityx, this.fov.getValue().floatValue()))
                           .toList();
                        if (targets.isEmpty()) {
                           return;
                        }

                        int rodSlotx = this.findRodSlot();
                        if (rodSlotx == -1) {
                           return;
                        }

                        class_1309 target = (class_1309)targets.get(0);
                        if (!(target instanceof class_1657)) {
                           return;
                        }

                        this.timer.reset();
                        double ticks = this.keepTime.getValue().doubleValue() / 50.0;
                        this.reelTime = currentTime + this.keepTime.getValue().intValue();
                        this.currentTarget = target;
                        class_243 predictedPos = this.predictPosition(target, ticks);
                        if (predictedPos == null) {
                           return;
                        }

                        if (this.isObstructed(predictedPos, ticks)) {
                           return;
                        }

                        this.rotation = RotationUtils.calculate(predictedPos);
                        RotationUtils.setRotation(this.rotation);
                        this.rodState = 1;
                     }
                  }
               }
            }
         }
      }
   }

   private int findRodSlot() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         class_1799 offhandStack = mc.field_1724.method_6079();
         if (!offhandStack.method_7960() && offhandStack.method_7909() instanceof class_1787) {
            return -2;
         } else {
            for (int i = 0; i < 9; i++) {
               class_1799 stack = mc.field_1724.method_31548().method_5438(i);
               if (!stack.method_7960() && stack.method_7909() instanceof class_1787) {
                  return i;
               }
            }

            return -1;
         }
      }
   }

   private double calculateThrowTime(class_1297 target) {
      class_243 targetPos = target.method_19538();
      class_243 playerPos = mc.field_1724.method_19538();
      double distance = targetPos.method_1022(playerPos);
      return Math.max(distance / 8.0, 1.0);
   }

   private class_243 predictPosition(class_1297 target, double ticks) {
      if (mc.field_1687 == null) {
         return null;
      } else {
         class_243 motion = target.method_19538().method_1023(target.field_6014, target.field_6036, target.field_5969);
         double predictedX = target.method_23317() + motion.field_1352 * ticks;
         double predictedY = target.method_23318() + motion.field_1351 * ticks;
         double predictedZ = target.method_23321() + motion.field_1350 * ticks;
         return new class_243(predictedX, predictedY + target.method_5751(), predictedZ);
      }
   }

   private boolean isObstructed(class_243 targetPos, double ticks) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23320(), mc.field_1724.method_23321());
         List<class_243> trajectory = this.calculateTrajectory(targetPos, ticks);

         for (int i = 0; i < trajectory.size() - 1; i++) {
            class_243 current = trajectory.get(i);
            class_243 next = trajectory.get(i + 1);
            class_3965 result = mc.field_1687.method_17742(new class_3959(current, next, class_3960.field_17558, class_242.field_1348, mc.field_1724));
            if (result.method_17783() == class_240.field_1332) {
               class_2338 blockPos = result.method_17777();
               if (!mc.field_1687.method_8320(blockPos).method_26215()
                  && !mc.field_1687.method_8320(blockPos).method_26220(mc.field_1687, blockPos).method_1110()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private List<class_243> calculateTrajectory(class_243 targetPos, double ticks) {
      if (mc.field_1724 == null) {
         return List.of();
      } else {
         List<class_243> points = new ArrayList<>();
         class_243 eyePos = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23320(), mc.field_1724.method_23321());
         class_243 direction = targetPos.method_1020(eyePos).method_1029();
         class_243 initialVel = direction.method_1021(1.5);
         class_243 pos = eyePos;
         class_243 vel = initialVel;
         points.add(eyePos);
         int maxTicks = (int)Math.ceil(ticks);

         for (int i = 0; i < maxTicks; i++) {
            pos = pos.method_1019(vel);
            vel = vel.method_1031(0.0, -0.03, 0.0);
            points.add(pos);
         }

         return points;
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent event) {
      if (event.getPacket() instanceof class_2824) {
         this.rodState = 0;
         this.rotation = null;
      } else if (event.getPacket() instanceof class_2886) {
         class_2886 packet = (class_2886)event.getPacket();
         if ((packet.method_12551() == class_1268.field_5808 ? mc.field_1724.method_6047() : mc.field_1724.method_6079()).method_7909() instanceof class_1787) {
            return;
         }

         this.rodState = 0;
         this.rotation = null;
      } else if (event.getPacket() instanceof class_2846 && ((class_2846)event.getPacket()).method_12363() == class_2847.field_12968) {
         this.rodState = 0;
         this.rotation = null;
      }
   }
}
