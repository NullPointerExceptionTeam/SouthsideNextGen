package dev.southside.utils.world;

import dev.southside.MinecraftAccessor;
import net.minecraft.class_1297;
import net.minecraft.class_1541;
import net.minecraft.class_1665;
import net.minecraft.class_1667;
import net.minecraft.class_1670;
import net.minecraft.class_1674;
import net.minecraft.class_1675;
import net.minecraft.class_1680;
import net.minecraft.class_1681;
import net.minecraft.class_1682;
import net.minecraft.class_1683;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1687;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1823;
import net.minecraft.class_1835;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_3959;
import net.minecraft.class_4537;
import net.minecraft.class_9278;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class ProjectileEntitySimulator {
   private static final class_2339 blockPos = new class_2339();
   private static final Vector3d pos3d = new Vector3d(0.0, 0.0, 0.0);
   private static final Vector3d prevPos3d = new Vector3d(0.0, 0.0, 0.0);
   public final Vector3d pos = new Vector3d();
   public final Vector3d velocity = new Vector3d();
   private double gravity;
   private double airDrag;
   private double waterDrag;

   public boolean set(class_1297 user, class_1799 itemStack, double simulated, boolean accurate, boolean tnt, boolean stuck, float tickDelta) {
      class_1792 item = itemStack.method_7909();
      if (item instanceof class_1753) {
         double charge = class_1753.method_7722(MinecraftAccessor.mc.field_1724.method_6048());
         if (charge <= 0.0) {
            return false;
         }

         this.set(user, 0.0, charge * 3.0, simulated, 0.05F, 0.6, accurate, stuck, tickDelta);
      } else if (item instanceof class_1764) {
         if (!class_1764.method_7781(itemStack)) {
            return false;
         }

         if (class_1764.method_7781(itemStack)) {
            this.set(user, 0.0, class_1764.method_20309(class_9278.method_57439(itemStack)), simulated, 0.0, 0.6, accurate, stuck, tickDelta);
         } else {
            this.set(user, 0.0, class_1764.method_20309(class_9278.method_57439(itemStack)), simulated, 0.05F, 0.6, accurate, stuck, tickDelta);
         }
      } else if (item instanceof class_1787) {
         this.setFishingBobber(user, tickDelta);
      } else if (item instanceof class_1835) {
         this.set(user, 0.0, 2.5, simulated, 0.05F, 0.99, accurate, stuck, tickDelta);
      } else if (item instanceof class_1823 || item instanceof class_1771 || item instanceof class_1776) {
         this.set(user, 0.0, 1.5, simulated, 0.03, 0.8, accurate, stuck, tickDelta);
      } else if (item instanceof class_1779) {
         this.set(user, -20.0, 0.7, simulated, 0.07, 0.8, accurate, stuck, tickDelta);
      } else if (item instanceof class_4537) {
         this.set(user, -20.0, 0.5, simulated, 0.05, 0.8, accurate, stuck, tickDelta);
      } else {
         if (!tnt || !item.equals(class_1802.field_8626)) {
            return false;
         }

         this.set(user, 0.0, 1.5, simulated, 0.05, 0.8, accurate, stuck, tickDelta);
      }

      return true;
   }

   public void set(
      class_1297 user, double roll, double speed, double simulated, double gravity, double waterDrag, boolean accurate, boolean stuck, float tickDelta
   ) {
      this.pos.set(this.with(user.method_30950(tickDelta))).add(0.0, user.method_18381(user.method_18376()), 0.0);
      double yaw = class_3532.method_16439(tickDelta, user.field_5982, user.method_36454());
      double pitch = class_3532.method_16439(tickDelta, user.field_6004, user.method_36455());
      double x;
      double y;
      double z;
      if (simulated == 0.0) {
         x = -Math.sin(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
         y = -Math.sin((pitch + roll) * 0.017453292);
         z = Math.cos(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
      } else {
         class_243 vec3d = user.method_18864(1.0F);
         Quaterniond quaternion = new Quaterniond().setAngleAxis(simulated, vec3d.field_1352, vec3d.field_1351, vec3d.field_1350);
         class_243 vec3d2 = user.method_5828(1.0F);
         Vector3d vector3f = new Vector3d(vec3d2.field_1352, vec3d2.field_1351, vec3d2.field_1350);
         vector3f.rotate(quaternion);
         x = vector3f.x;
         y = vector3f.y;
         z = vector3f.z;
      }

      this.velocity.set(x, y, z).normalize().mul(speed);
      if (accurate) {
         class_243 vel = user.method_18798();
         this.velocity.add(vel.field_1352, !user.method_24828() && !stuck ? vel.field_1351 : 0.0, vel.field_1350);
      }

      this.gravity = gravity;
      this.airDrag = 0.99;
      this.waterDrag = waterDrag;
   }

   public boolean set(class_1297 entity, boolean accurate, boolean tnt, float tickDelta) {
      if (entity instanceof class_1665 projectile && projectile.method_65059()) {
         return false;
      } else {
         if (entity instanceof class_1667) {
            this.set(entity, 0.05F, 0.6, tickDelta);
         } else if (entity instanceof class_1684 || entity instanceof class_1680 || entity instanceof class_1681) {
            this.set(entity, 0.03, 0.8, tickDelta);
         } else if (entity instanceof class_1685) {
            this.set(entity, 0.05F, 0.99, tickDelta);
         } else if (entity instanceof class_1683) {
            this.set(entity, 0.07, 0.8, tickDelta);
         } else if (entity instanceof class_1682) {
            this.set(entity, 0.05, 0.8, tickDelta);
         } else if (!(entity instanceof class_1687) && !(entity instanceof class_1674) && !(entity instanceof class_1670)) {
            if (!tnt || !(entity instanceof class_1541)) {
               return false;
            }

            this.set(entity, 0.05F, 0.8, tickDelta);
         } else {
            this.set(entity, 0.0, 0.8, tickDelta);
         }

         if (entity.method_5740()) {
            this.gravity = 0.0;
         }

         return true;
      }
   }

   public void set(class_1297 entity, double gravity, double waterDrag, float tickDelta) {
      this.pos.set(entity.method_30950(tickDelta).method_46409());
      class_243 currentVelocity = entity.method_18798();
      this.velocity.set(currentVelocity.field_1352, currentVelocity.field_1351, currentVelocity.field_1350);
      this.gravity = gravity;
      this.airDrag = 0.99;
      this.waterDrag = waterDrag;
   }

   public void setFishingBobber(class_1297 user, float tickDelta) {
      double yaw = class_3532.method_16439(tickDelta, user.field_5982, user.method_36454());
      double pitch = class_3532.method_16439(tickDelta, user.field_6004, user.method_36455());
      double h = Math.cos(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      double i = Math.sin(-yaw * (float) (Math.PI / 180.0) - (float) Math.PI);
      double j = -Math.cos(-pitch * (float) (Math.PI / 180.0));
      double k = Math.sin(-pitch * (float) (Math.PI / 180.0));
      this.pos.set(this.with(user.method_30950(tickDelta))).sub(i * 0.3, 0.0, h * 0.3).add(0.0, user.method_18381(user.method_18376()), 0.0);
      this.velocity.set(-i, class_3532.method_15350(-(k / j), -5.0, 5.0), -h);
      double l = this.velocity.length();
      this.velocity.mul(0.6 / l + 0.5, 0.6 / l + 0.5, 0.6 / l + 0.5);
      this.gravity = 0.03;
      this.airDrag = 0.92;
      this.waterDrag = 0.0;
   }

   public class_239 tick() {
      prevPos3d.set(this.pos);
      this.pos.add(this.velocity);
      this.velocity.mul(this.isTouchingWater() ? this.waterDrag : this.airDrag);
      this.velocity.sub(0.0, this.gravity, 0.0);
      if (this.pos.y < MinecraftAccessor.mc.field_1687.method_31607()) {
         return new MissHitResult(this.with(this.pos));
      } else {
         int chunkX = (int)(this.pos.x / 16.0);
         int chunkZ = (int)(this.pos.z / 16.0);
         if (!MinecraftAccessor.mc.field_1687.method_2935().method_12123(chunkX, chunkZ)) {
            return new MissHitResult(this.with(this.pos));
         } else {
            pos3d.set(this.pos);
            class_239 hitResult = this.getCollision();
            return hitResult.method_17783() == class_240.field_1333 ? null : hitResult;
         }
      }
   }

   private boolean isTouchingWater() {
      blockPos.method_10102(this.pos.x, this.pos.y, this.pos.z);
      class_3610 fluidState = MinecraftAccessor.mc.field_1687.method_8316(blockPos);
      return fluidState.method_15772() != class_3612.field_15910 && fluidState.method_15772() != class_3612.field_15909
         ? false
         : this.pos.y - (int)this.pos.y <= fluidState.method_20785();
   }

   private class_239 getCollision() {
      class_243 vec3d3 = this.with(prevPos3d);
      class_239 hitResult = MinecraftAccessor.mc
         .field_1687
         .method_17742(
            new class_3959(
               vec3d3,
               this.with(pos3d),
               class_3960.field_17558,
               this.waterDrag == 0.0 ? class_242.field_1347 : class_242.field_1348,
               MinecraftAccessor.mc.field_1724
            )
         );
      if (hitResult.method_17783() != class_240.field_1333) {
         vec3d3 = hitResult.method_17784();
      }

      class_239 hitResult2 = class_1675.method_18077(
         MinecraftAccessor.mc.field_1687,
         MinecraftAccessor.mc.field_1724,
         vec3d3,
         this.with(pos3d),
         new class_238(this.pos.x, this.pos.y, this.pos.z, this.pos.x, this.pos.y, this.pos.z)
            .method_18804(MinecraftAccessor.mc.field_1724.method_18798())
            .method_1014(1.0),
         entity -> !entity.method_7325() && entity.method_5805() && entity.method_5863()
      );
      if (hitResult2 != null) {
         hitResult = hitResult2;
      }

      return hitResult;
   }

   public Vector3d with(class_243 vec) {
      return new Vector3d(vec.field_1352, vec.field_1351, vec.field_1350);
   }

   public class_243 with(Vector3d vec) {
      return new class_243(vec.x, vec.y, vec.z);
   }
}
