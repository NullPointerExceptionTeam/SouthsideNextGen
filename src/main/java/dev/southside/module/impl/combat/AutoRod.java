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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

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
   private Hand rodHand;
   private LivingEntity currentTarget;
   public int rodState = 0;
   private Map<ThrownEntity, Long> thrownEntities = new HashMap<>();

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
         if (mc.currentScreen == null) {
            if (!mc.player.isUsingItem()) {
               if (!Southside.INSTANCE.getModuleManager().get(Scaffold.class).isEnabled()) {
                  long currentTime = System.currentTimeMillis();
                  if (this.isRodActive) {
                     if (this.currentTarget != null
                        && mc.player.getPos().distanceTo(this.currentTarget.getPos()) < this.minRange.getValue().floatValue()) {
                        mc.interactionManager.interactItem(mc.player, this.rodHand);
                        if (this.previousSlot != -1) {
                           mc.player.getInventory().selectedSlot = this.previousSlot;
                           this.previousSlot = -1;
                        }

                        this.isRodActive = false;
                        this.rodState = 0;
                        this.rotation = null;
                        this.rodHand = null;
                        this.currentTarget = null;
                     } else if (currentTime >= this.reelTime) {
                        mc.interactionManager.interactItem(mc.player, this.rodHand);
                        if (this.previousSlot != -1) {
                           mc.player.getInventory().selectedSlot = this.previousSlot;
                           this.previousSlot = -1;
                        }

                        this.isRodActive = false;
                        this.rodState = 0;
                        this.rotation = null;
                        this.rodHand = null;
                        this.currentTarget = null;
                     }
                  } else {
                     Map<ThrownEntity, Long> newThrown = new HashMap<>();

                     for (Entity entity : mc.world.getEntities()) {
                        if (entity instanceof ThrownEntity thrown) {
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

                        this.rodHand = rodSlot == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND;
                        if (rodSlot != -2) {
                           this.previousSlot = mc.player.getInventory().selectedSlot;
                           mc.player.getInventory().selectedSlot = rodSlot;
                        }

                        mc.interactionManager.interactItem(mc.player, rodSlot == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND);
                        this.isRodActive = true;
                        this.rodState = 0;
                        this.rotation = null;
                     } else {
                        if (!this.timer.hasTimePassed(this.delay.getValue().intValue())) {
                           return;
                        }

                        List<Entity> targets = (KillAura.target == null
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

                        LivingEntity target = (LivingEntity)targets.get(0);
                        if (!(target instanceof PlayerEntity)) {
                           return;
                        }

                        this.timer.reset();
                        double ticks = this.keepTime.getValue().doubleValue() / 50.0;
                        this.reelTime = currentTime + this.keepTime.getValue().intValue();
                        this.currentTarget = target;
                        Vec3d predictedPos = this.predictPosition(target, ticks);
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
      if (mc.player == null) {
         return -1;
      } else {
         ItemStack offhandStack = mc.player.getOffHandStack();
         if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof FishingRodItem) {
            return -2;
         } else {
            for (int i = 0; i < 9; i++) {
               ItemStack stack = mc.player.getInventory().getStack(i);
               if (!stack.isEmpty() && stack.getItem() instanceof FishingRodItem) {
                  return i;
               }
            }

            return -1;
         }
      }
   }

   private double calculateThrowTime(Entity target) {
      Vec3d targetPos = target.getPos();
      Vec3d playerPos = mc.player.getPos();
      double distance = targetPos.distanceTo(playerPos);
      return Math.max(distance / 8.0, 1.0);
   }

   private Vec3d predictPosition(Entity target, double ticks) {
      if (mc.world == null) {
         return null;
      } else {
         Vec3d motion = target.getPos().subtract(target.prevX, target.prevY, target.prevZ);
         double predictedX = target.getX() + motion.x * ticks;
         double predictedY = target.getY() + motion.y * ticks;
         double predictedZ = target.getZ() + motion.z * ticks;
         return new Vec3d(predictedX, predictedY + target.getStandingEyeHeight(), predictedZ);
      }
   }

   private boolean isObstructed(Vec3d targetPos, double ticks) {
      if (mc.player != null && mc.world != null) {
         new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
         List<Vec3d> trajectory = this.calculateTrajectory(targetPos, ticks);

         for (int i = 0; i < trajectory.size() - 1; i++) {
            Vec3d current = trajectory.get(i);
            Vec3d next = trajectory.get(i + 1);
            BlockHitResult result = mc.world.raycast(new RaycastContext(current, next, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
            if (result.getType() == Type.BLOCK) {
               BlockPos blockPos = result.getBlockPos();
               if (!mc.world.getBlockState(blockPos).isAir()
                  && !mc.world.getBlockState(blockPos).getCollisionShape(mc.world, blockPos).isEmpty()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private List<Vec3d> calculateTrajectory(Vec3d targetPos, double ticks) {
      if (mc.player == null) {
         return List.of();
      } else {
         List<Vec3d> points = new ArrayList<>();
         Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
         Vec3d direction = targetPos.subtract(eyePos).normalize();
         Vec3d initialVel = direction.multiply(1.5);
         Vec3d pos = eyePos;
         Vec3d vel = initialVel;
         points.add(eyePos);
         int maxTicks = (int)Math.ceil(ticks);

         for (int i = 0; i < maxTicks; i++) {
            pos = pos.add(vel);
            vel = vel.add(0.0, -0.03, 0.0);
            points.add(pos);
         }

         return points;
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent event) {
      if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
         this.rodState = 0;
         this.rotation = null;
      } else if (event.getPacket() instanceof PlayerInteractItemC2SPacket) {
         PlayerInteractItemC2SPacket packet = (PlayerInteractItemC2SPacket)event.getPacket();
         if ((packet.getHand() == Hand.MAIN_HAND ? mc.player.getMainHandStack() : mc.player.getOffHandStack()).getItem() instanceof FishingRodItem) {
            return;
         }

         this.rodState = 0;
         this.rotation = null;
      } else if (event.getPacket() instanceof PlayerActionC2SPacket && ((PlayerActionC2SPacket)event.getPacket()).getAction() == Action.START_DESTROY_BLOCK) {
         this.rodState = 0;
         this.rotation = null;
      }
   }
}
