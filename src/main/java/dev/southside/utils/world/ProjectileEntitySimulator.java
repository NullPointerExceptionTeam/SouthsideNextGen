package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.world.RaycastContext;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class ProjectileEntitySimulator {
   private static final Mutable blockPos = new Mutable();
   private static final Vector3d pos3d = new Vector3d(0.0, 0.0, 0.0);
   private static final Vector3d prevPos3d = new Vector3d(0.0, 0.0, 0.0);
   public final Vector3d pos = new Vector3d();
   public final Vector3d velocity = new Vector3d();
   private double gravity;
   private double airDrag;
   private double waterDrag;

   public boolean set(Entity user, ItemStack itemStack, double simulated, boolean accurate, boolean tnt, boolean stuck, float tickDelta) {
      Item item = itemStack.getItem();
      if (item instanceof BowItem) {
         double charge = BowItem.getPullProgress(MinecraftAccessor.mc.player.getItemUseTime());
         if (charge <= 0.0) {
            return false;
         }

         this.set(user, 0.0, charge * 3.0, simulated, 0.05F, 0.6, accurate, stuck, tickDelta);
      } else if (item instanceof CrossbowItem) {
         if (!CrossbowItem.isCharged(itemStack)) {
            return false;
         }

         if (CrossbowItem.isCharged(itemStack)) {
            this.set(user, 0.0, CrossbowItem.getSpeed(ChargedProjectilesComponent.of(itemStack)), simulated, 0.0, 0.6, accurate, stuck, tickDelta);
         } else {
            this.set(user, 0.0, CrossbowItem.getSpeed(ChargedProjectilesComponent.of(itemStack)), simulated, 0.05F, 0.6, accurate, stuck, tickDelta);
         }
      } else if (item instanceof FishingRodItem) {
         this.setFishingBobber(user, tickDelta);
      } else if (item instanceof TridentItem) {
         this.set(user, 0.0, 2.5, simulated, 0.05F, 0.99, accurate, stuck, tickDelta);
      } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
         this.set(user, 0.0, 1.5, simulated, 0.03, 0.8, accurate, stuck, tickDelta);
      } else if (item instanceof ExperienceBottleItem) {
         this.set(user, -20.0, 0.7, simulated, 0.07, 0.8, accurate, stuck, tickDelta);
      } else if (item instanceof ThrowablePotionItem) {
         this.set(user, -20.0, 0.5, simulated, 0.05, 0.8, accurate, stuck, tickDelta);
      } else {
         if (!tnt || !item.equals(Items.TNT)) {
            return false;
         }

         this.set(user, 0.0, 1.5, simulated, 0.05, 0.8, accurate, stuck, tickDelta);
      }

      return true;
   }

   public void set(
      Entity user, double roll, double speed, double simulated, double gravity, double waterDrag, boolean accurate, boolean stuck, float tickDelta
   ) {
      this.pos.set(this.with(user.getLerpedPos(tickDelta))).add(0.0, user.getEyeHeight(user.getPose()), 0.0);
      double yaw = MathHelper.lerp(tickDelta, user.prevYaw, user.getYaw());
      double pitch = MathHelper.lerp(tickDelta, user.prevPitch, user.getPitch());
      double x;
      double y;
      double z;
      if (simulated == 0.0) {
         x = -Math.sin(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
         y = -Math.sin((pitch + roll) * 0.017453292);
         z = Math.cos(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
      } else {
         Vec3d vec3d = user.getOppositeRotationVector(1.0F);
         Quaterniond quaternion = new Quaterniond().setAngleAxis(simulated, vec3d.x, vec3d.y, vec3d.z);
         Vec3d vec3d2 = user.getRotationVec(1.0F);
         Vector3d vector3f = new Vector3d(vec3d2.x, vec3d2.y, vec3d2.z);
         vector3f.rotate(quaternion);
         x = vector3f.x;
         y = vector3f.y;
         z = vector3f.z;
      }

      this.velocity.set(x, y, z).normalize().mul(speed);
      if (accurate) {
         Vec3d vel = user.getVelocity();
         this.velocity.add(vel.x, !user.isOnGround() && !stuck ? vel.y : 0.0, vel.z);
      }

      this.gravity = gravity;
      this.airDrag = 0.99;
      this.waterDrag = waterDrag;
   }

   public boolean set(Entity entity, boolean accurate, boolean tnt, float tickDelta) {
      if (entity instanceof PersistentProjectileEntity projectile && projectile.isInGround()) {
         return false;
      } else {
         if (entity instanceof ArrowEntity) {
            this.set(entity, 0.05F, 0.6, tickDelta);
         } else if (entity instanceof EnderPearlEntity || entity instanceof SnowballEntity || entity instanceof EggEntity) {
            this.set(entity, 0.03, 0.8, tickDelta);
         } else if (entity instanceof TridentEntity) {
            this.set(entity, 0.05F, 0.99, tickDelta);
         } else if (entity instanceof ExperienceBottleEntity) {
            this.set(entity, 0.07, 0.8, tickDelta);
         } else if (entity instanceof ThrownEntity) {
            this.set(entity, 0.05, 0.8, tickDelta);
         } else if (!(entity instanceof WitherSkullEntity) && !(entity instanceof FireballEntity) && !(entity instanceof DragonFireballEntity)) {
            if (!tnt || !(entity instanceof TntEntity)) {
               return false;
            }

            this.set(entity, 0.05F, 0.8, tickDelta);
         } else {
            this.set(entity, 0.0, 0.8, tickDelta);
         }

         if (entity.hasNoGravity()) {
            this.gravity = 0.0;
         }

         return true;
      }
   }

   public void set(Entity entity, double gravity, double waterDrag, float tickDelta) {
      this.pos.set(entity.getLerpedPos(tickDelta).toVector3f());
      Vec3d currentVelocity = entity.getVelocity();
      this.velocity.set(currentVelocity.x, currentVelocity.y, currentVelocity.z);
      this.gravity = gravity;
      this.airDrag = 0.99;
      this.waterDrag = waterDrag;
   }

   public void setFishingBobber(Entity user, float tickDelta) {
      double yaw = MathHelper.lerp(tickDelta, user.prevYaw, user.getYaw());
      double pitch = MathHelper.lerp(tickDelta, user.prevPitch, user.getPitch());
      double h = Math.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      double i = Math.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      double j = -Math.cos(-pitch * (float) (Math.PI / 180.0));
      double k = Math.sin(-pitch * (float) (Math.PI / 180.0));
      this.pos.set(this.with(user.getLerpedPos(tickDelta))).sub(i * 0.3, 0.0, h * 0.3).add(0.0, user.getEyeHeight(user.getPose()), 0.0);
      this.velocity.set(-i, MathHelper.clamp(-(k / j), -5.0, 5.0), -h);
      double l = this.velocity.length();
      this.velocity.mul(0.6 / l + 0.5, 0.6 / l + 0.5, 0.6 / l + 0.5);
      this.gravity = 0.03;
      this.airDrag = 0.92;
      this.waterDrag = 0.0;
   }

   public HitResult tick() {
      prevPos3d.set(this.pos);
      this.pos.add(this.velocity);
      this.velocity.mul(this.isTouchingWater() ? this.waterDrag : this.airDrag);
      this.velocity.sub(0.0, this.gravity, 0.0);
      if (this.pos.y < MinecraftAccessor.mc.world.getBottomY()) {
         return new MissHitResult(this.with(this.pos));
      } else {
         int chunkX = (int)(this.pos.x / 16.0);
         int chunkZ = (int)(this.pos.z / 16.0);
         if (!MinecraftAccessor.mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
            return new MissHitResult(this.with(this.pos));
         } else {
            pos3d.set(this.pos);
            HitResult hitResult = this.getCollision();
            return hitResult.getType() == Type.MISS ? null : hitResult;
         }
      }
   }

   private boolean isTouchingWater() {
      blockPos.set(this.pos.x, this.pos.y, this.pos.z);
      FluidState fluidState = MinecraftAccessor.mc.world.getFluidState(blockPos);
      return fluidState.getFluid() != Fluids.WATER && fluidState.getFluid() != Fluids.FLOWING_WATER
         ? false
         : this.pos.y - (int)this.pos.y <= fluidState.getHeight();
   }

   private HitResult getCollision() {
      Vec3d vec3d3 = this.with(prevPos3d);
      HitResult hitResult = MinecraftAccessor.mc
         .world
         .raycast(
            new RaycastContext(
               vec3d3,
               this.with(pos3d),
               ShapeType.COLLIDER,
               this.waterDrag == 0.0 ? FluidHandling.ANY : FluidHandling.NONE,
               MinecraftAccessor.mc.player
            )
         );
      if (hitResult.getType() != Type.MISS) {
         vec3d3 = hitResult.getPos();
      }

      HitResult hitResult2 = ProjectileUtil.getEntityCollision(
         MinecraftAccessor.mc.world,
         MinecraftAccessor.mc.player,
         vec3d3,
         this.with(pos3d),
         new Box(this.pos.x, this.pos.y, this.pos.z, this.pos.x, this.pos.y, this.pos.z)
            .stretch(MinecraftAccessor.mc.player.getVelocity())
            .expand(1.0),
         entity -> !entity.isSpectator() && entity.isAlive() && entity.canHit()
      );
      if (hitResult2 != null) {
         hitResult = hitResult2;
      }

      return hitResult;
   }

   public Vector3d with(Vec3d vec) {
      return new Vector3d(vec.x, vec.y, vec.z);
   }

   public Vec3d with(Vector3d vec) {
      return new Vec3d(vec.x, vec.y, vec.z);
   }
}
