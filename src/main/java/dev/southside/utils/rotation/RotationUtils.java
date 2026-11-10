package dev.southside.utils.rotation;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.HighLevelPacketEvent;
import dev.southside.event.events.JumpEvent;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.StrafeEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.math.Vector3d;
import dev.southside.utils.misc.Pair;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.raytrace.RayCastUtils;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.VecRotation;
import dev.southside.utils.types.Rotation;
import dev.southside.utils.vector.Vector2f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

public class RotationUtils
implements MinecraftAccessor {
    public static Rotation rotation;
    private static Rotation lastRotation;
    private static Rotation serverRotation;
    private static MoveFixMode strafeFixMode;
    private static boolean strafeFixSkipped;
    private static final List<Pair<Rotation, Integer>> rotationList;
        private static final List<MoveFixMode> rotationMoveFixList;
    private static final RotationEvent rotationEvent;
    public static ThreadLocalRandom random;
    private static final double EPS = 1.0E-7;

    public RotationUtils() {
        Southside.INSTANCE.getEventBus().register(this);
    }

    public static Rotation getRotationSafely() {
        if (RotationUtils.mc.player == null && rotation != null) {
            return rotation;
        }
        if (RotationUtils.mc.player == null && rotation == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return rotation == null ? new Rotation(RotationUtils.mc.player.getYaw(), RotationUtils.mc.player.getPitch()) : rotation;
    }

        public static MoveFixMode getEffectiveMoveFix() {
        return strafeFixMode == null ? MoveFixMode.Silent : strafeFixMode;
    }

    public static boolean isStrafeFix() {
        return !strafeFixSkipped && RotationUtils.getEffectiveMoveFix() == MoveFixMode.Silent && RotationUtils.getRotation() != null;
    }

    public static boolean isStrictFix() {
        return !strafeFixSkipped && RotationUtils.getEffectiveMoveFix() == MoveFixMode.Strict && RotationUtils.getRotation() != null;
    }

        public static boolean isSilentFixSkipped() {
        return strafeFixSkipped;
    }

    public static void setSilentFixSkipped(boolean skipped) {
        strafeFixSkipped = skipped;
    }

    public static Vec3d getDirectionVector(float yaw, float pitch) {
        float yawRad = (float)Math.toRadians(-yaw - 180.0f);
        float pitchRad = (float)Math.toRadians(-pitch);
        float x = MathHelper.cos((float)pitchRad) * MathHelper.sin((float)yawRad);
        float y = MathHelper.sin((float)pitchRad);
        float z = MathHelper.cos((float)pitchRad) * MathHelper.cos((float)yawRad);
        return new Vec3d((double)x, (double)y, (double)z).normalize();
    }

    public static double calculateEntityDistance(Entity target) {
        Box targetBox = target.getBoundingBox();
        if (target instanceof EndCrystalEntity) {
            Vec3d pos = target.getPos();
            targetBox = new Box(pos.x - 1.0, pos.y, pos.z - 1.0, pos.x + 1.0, pos.y + 2.0, pos.z + 1.0);
        }
        targetBox = targetBox.expand(0.03);
        double maxReach = 3.0;
        double distanceRay = maxReach + 3.0;
        double minDistance = Double.MAX_VALUE;
        for (Vec3d lookVec : List.of(RotationUtils.getRotationVector(RotationUtils.calculate((Vec3d)target.getPos()).yaw, RotationUtils.calculate((Vec3d)target.getPos()).pitch))) {
            Iterator<Float> iterator = List.of(Float.valueOf(RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose()))).iterator();
            while (iterator.hasNext()) {
                double dist;
                double eyeOffset = iterator.next().floatValue();
                Vec3d eyePos = new Vec3d(RotationUtils.mc.player.getX(), RotationUtils.mc.player.getY() + eyeOffset, RotationUtils.mc.player.getZ());
                Vec3d endPos = eyePos.add(lookVec.multiply(distanceRay));
                if (targetBox.contains(eyePos)) {
                    return 0.0;
                }
                Optional intercept = targetBox.raycast(eyePos, endPos);
                if (!intercept.isPresent() || !((dist = eyePos.distanceTo((Vec3d)intercept.get())) < minDistance)) continue;
                minDistance = dist;
            }
        }
        return minDistance;
    }

    public static Vec3d getRotationVector(float yaw, float pitch) {
        double yawRad = Math.toRadians(-yaw);
        double pitchRad = Math.toRadians(-pitch);
        double x = Math.cos(pitchRad) * Math.sin(yawRad);
        double y = Math.sin(pitchRad);
        double z = Math.cos(pitchRad) * Math.cos(yawRad);
        return new Vec3d(x, y, z);
    }

    private static Box getTargetBoundingBox(Entity entity) {
        return entity.getBoundingBox();
    }

    private static boolean checkHitResult(Vec3d eyePos, HitResult result, Entity target) {
        if (result.getType() == HitResult.Type.ENTITY && ((EntityHitResult)result).getEntity() == target) {
            Vec3d intercept = result.getPos();
            return RotationUtils.isVecInside(RotationUtils.getTargetBoundingBox(target), eyePos) || intercept.distanceTo(eyePos) <= 3.0;
        }
        return false;
    }

    public static boolean isVecInside(Box self, Vec3d vec) {
        return vec.x > self.minX && vec.x < self.maxX && vec.y > self.minY && vec.y < self.maxY && vec.z > self.minZ && vec.z < self.maxZ;
    }

    public static float getDistanceToEntity(Entity target) {
        if (RotationUtils.mc.player == null) {
            return 0.0f;
        }
        Vec3d eyes = RotationUtils.mc.player.getCameraPosVec(1.0f);
        Vec3d pos = RotationUtils.getNearestPointBB(eyes, target.getBoundingBox());
        double xDist = Math.abs(pos.x - eyes.x);
        double yDist = Math.abs(pos.y - eyes.y);
        double zDist = Math.abs(pos.z - eyes.z);
        return (float)Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0) + Math.pow(zDist, 2.0));
    }

    public static Vec3d getNearestPointBB(Vec3d eye, Box box) {
        double[] origin = new double[]{eye.x, eye.y, eye.z};
        double[] destMinis = new double[]{box.minX, box.minY, box.minZ};
        double[] destMaxis = new double[]{box.maxX, box.maxY, box.maxZ};
        for (int i = 0; i < 3; ++i) {
            if (origin[i] > destMaxis[i]) {
                origin[i] = destMaxis[i];
                continue;
            }
            if (!(origin[i] < destMinis[i])) continue;
            origin[i] = destMinis[i];
        }
        return new Vec3d(origin[0], origin[1], origin[2]);
    }

    private static HitResult rayTrace(Vector2f rotations) {
        Box box;
        double d0 = RotationUtils.mc.player.getBlockInteractionRange();
        HitResult hitResult = RayCastUtils.rayCast(d0, 1.0f, false, rotations);
        Vec3d vec3 = RotationUtils.mc.player.getCameraPosVec(1.0f);
        boolean flag = false;
        double d1 = d0;
        if (d0 > 3.0) {
            flag = true;
        }
        d1 *= d1;
        if (hitResult != null) {
            d1 = hitResult.getPos().squaredDistanceTo(vec3);
        }
        Vec3d vec31 = RotationUtils.getLook(rotations.x, rotations.y);
        Vec3d vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
        EntityHitResult entityhitresult = ProjectileUtil.raycast((Entity)RotationUtils.mc.player, (Vec3d)vec3, (Vec3d)vec32, (Box)(box = RotationUtils.mc.player.getBoundingBox().stretch(vec31.multiply(d0)).expand(1.0)), entity -> !entity.isSpectator() && entity.isCollidable(), (double)d1);
        if (entityhitresult != null) {
            Vec3d vec33 = entityhitresult.getPos();
            double d2 = vec3.squaredDistanceTo(vec33);
            if (flag && d2 > 9.0) {
                hitResult = BlockHitResult.createMissed((Vec3d)vec33, (Direction)Direction.getFacing((double)vec31.x, (double)vec31.y, (double)vec31.z), (BlockPos)BlockPos.ofFloored((Position)vec33));
            } else if (d2 < d1 || hitResult == null) {
                hitResult = entityhitresult;
            }
        }
        return hitResult;
    }

    public static Vec3d getLook(float yaw, float pitch) {
        float f = MathHelper.cos((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f1 = MathHelper.sin((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f2 = -MathHelper.cos((float)(-pitch * ((float)Math.PI / 180)));
        float f3 = MathHelper.sin((float)(-pitch * ((float)Math.PI / 180)));
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public static Vector2f getRotations(Vec3d eye, Vec3d target) {
        double x = target.x - eye.x;
        double y = target.y - eye.y;
        double z = target.z - eye.z;
        double diffXZ = Math.sqrt(x * x + z * z);
        float yaw = (float)Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(y, diffXZ)));
        return new Vector2f(MathHelper.wrapDegrees((float)yaw), MathHelper.wrapDegrees((float)pitch));
    }

    public static Rotation getBetterRotationCustom(Entity from, Entity target, double range) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || from == null || target == null) {
            return null;
        }
        Vec3d eyePos = from.getEyePos();
        Box box = target.getBoundingBox();
        List<Vec3d> points = RotationUtils.getSamplePoints(box);
        Rotation bestRot = null;
        double bestDist = Double.MAX_VALUE;
        for (Vec3d point : points) {
            double dist;
            if (eyePos.distanceTo(point) > range || !RotationUtils.isUnobstructed(eyePos, point) || !((dist = eyePos.distanceTo(point)) < bestDist)) continue;
            bestDist = dist;
            bestRot = Rotation.from(eyePos, point);
        }
        return bestRot;
    }

    public static Rotation getBetterRotation(Entity target, double range) {
        return RotationUtils.getBetterRotationCustom((Entity)RotationUtils.mc.player, target, range);
    }

    private static List<Vec3d> getSamplePoints(Box box) {
        ArrayList<Vec3d> list = new ArrayList<Vec3d>();
        list.add(box.getCenter());
        list.add(new Vec3d(box.getCenter().x, box.maxY, box.getCenter().z));
        list.add(new Vec3d(box.getCenter().x, box.minY, box.getCenter().z));
        for (double x : new double[]{box.minX, box.maxX}) {
            for (double y : new double[]{box.minY, box.maxY}) {
                for (double z : new double[]{box.minZ, box.maxZ}) {
                    list.add(new Vec3d(x, y, z));
                }
            }
        }
        for (int i = 0; i < 10; ++i) {
            double rx = box.minX + Math.random() * (box.maxX - box.minX);
            double ry = box.minY + Math.random() * (box.maxY - box.minY);
            double rz = box.minZ + Math.random() * (box.maxZ - box.minZ);
            list.add(new Vec3d(rx, ry, rz));
        }
        return list;
    }

    private static boolean isUnobstructed(Vec3d from, Vec3d to) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockHitResult result = mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)mc.player));
        return result.getType() == HitResult.Type.MISS || RotationUtils.squaredDistanceTo((HitResult)result, from) >= from.squaredDistanceTo(to) - 0.01;
    }

    public static double squaredDistanceTo(HitResult result, Vec3d vec3d) {
        Vec3d entity = result.getPos();
        double d = vec3d.x - entity.getX();
        double e = vec3d.y - entity.getY();
        double f = vec3d.z - entity.getZ();
        return d * d + e * e + f * f;
    }

    public static Rotation calculateRotation(Vec3d cameraPos, Vec3d aimPoint) {
        double dx = aimPoint.x - cameraPos.x;
        double dy = aimPoint.y - cameraPos.y;
        double dz = aimPoint.z - cameraPos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
        yaw = RotationUtils.wrapDegrees(yaw);
        pitch = MathHelper.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        return new Rotation(yaw, pitch);
    }

    private static float wrapDegrees(float angle) {
        if ((angle %= 360.0f) >= 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    public static float[] fixMouseSensitivity(float yaw1, float pitch2, float yaw, float pitch) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float sensitivity = ((Double)mc.options.getMouseSensitivity().getValue()).floatValue();
        float f = sensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        float deltaYaw = yaw1 - yaw;
        float deltaPitch = pitch2 - pitch;
        deltaYaw -= deltaYaw % gcd;
        deltaPitch -= deltaPitch % gcd;
        float fixedYaw = yaw + deltaYaw;
        float fixedPitch = pitch + deltaPitch;
        if (fixedPitch > 90.0f) {
            fixedPitch = 90.0f;
        }
        if (fixedPitch < -90.0f) {
            fixedPitch = -90.0f;
        }
        return new float[]{fixedYaw, fixedPitch};
    }

    public static void choose() {
        if (RotationUtils.mc.player != null) {
            rotationEvent.setDisableRotation(false);
            Southside.INSTANCE.getEventBus().call(rotationEvent);
            if (rotationList.isEmpty()) {
                RotationUtils.setRotation(null);
            }
            rotation = rotationEvent.isDisableRotation() ? null : RotationUtils.getRandomRotation(rotationList);

            RotationUtils.applyMoveFixModeOf(rotation);
            rotationList.clear();
            rotationMoveFixList.clear();
        }
    }

    private static void applyMoveFixModeOf(Rotation chosen) {
        for (int i = 0; i < rotationList.size(); i++) {
            if (rotationList.get(i).getKey() == chosen) {
                strafeFixMode = rotationMoveFixList.get(i);
                return;
            }
        }
    }

    public static boolean accepted(int priority) {
        int highestPriorityValue = rotationList.stream().mapToInt(Pair::getValue).max().orElse(Integer.MIN_VALUE);
        return priority >= highestPriorityValue;
    }

    public static void setRotationDirectly(Rotation rotation, MoveFixMode fixMode) {
        RotationUtils.rotation = rotation;
        strafeFixMode = fixMode;
    }

    public static void setRotation(Rotation rotation, int priority) {
        rotationList.add(new Pair<Rotation, Integer>(rotation, priority));
        rotationMoveFixList.add(null);
    }

    public static void setRotation(Rotation rotation, int priority, MoveFixMode mode) {
        rotationList.add(new Pair<Rotation, Integer>(rotation, priority));
        rotationMoveFixList.add(mode);
    }

    public static void setRotation(Rotation angles) {
        RotationUtils.setRotation(angles, 0, Southside.INSTANCE.getModuleManager().get(StrafeFix.class).getMode());
    }

    public static Vec3d getClosestPoint(Vec3d eyePos, Box box) {
        double closestX = MathHelper.clamp((double)eyePos.x, (double)box.minX, (double)box.maxX);
        double closestY = MathHelper.clamp((double)eyePos.y, (double)box.minY, (double)box.maxY);
        double closestZ = MathHelper.clamp((double)eyePos.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3d(closestX, closestY, closestZ);
    }

    public static float getRawDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;
        if (pForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (pForward < 0.0f) {
            forward = -0.5f;
        } else if (pForward > 0.0f) {
            forward = 0.5f;
        }
        if (pStrafe > 0.0f) {
            rotationYaw -= 90.0f * forward;
        }
        if (pStrafe < 0.0f) {
            rotationYaw += 90.0f * forward;
        }
        return rotationYaw;
    }

    public static float getRawDirection() {
        return RotationUtils.getRawDirectionRotation(RotationUtils.mc.player.getYaw(), RotationUtils.mc.player.sidewaysSpeed, RotationUtils.mc.player.forwardSpeed);
    }

    public static float[] getRotations(Vec3d vec) {
        return RotationUtils.getRotations(vec.x, vec.y, vec.z);
    }

    public static float[] getRotations(double x, double y, double z) {
        double deltaX = x - RotationUtils.mc.player.getX();
        double deltaY = y - RotationUtils.mc.player.getY() - (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose());
        double deltaZ = z - RotationUtils.mc.player.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)Math.toDegrees(-Math.atan2(deltaX, deltaZ));
        float pitch = (float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(Entity entity) {
        double pX = RotationUtils.mc.player.getX();
        double pY = RotationUtils.mc.player.getY() + (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose());
        double pZ = RotationUtils.mc.player.getZ();
        double eX = entity.getX();
        double eY = entity.getY() + (double)(entity.getHeight() / 2.0f);
        double eZ = entity.getZ();
        double dX = pX - eX;
        double dY = pY - eY;
        double dZ = pZ - eZ;
        double dH = Math.sqrt(Math.pow(dX, 2.0) + Math.pow(dZ, 2.0));
        double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
        double pitch = Math.toDegrees(Math.atan2(dH, dY));
        return new float[]{(float)yaw, (float)(90.0 - pitch)};
    }

    public static Rotation toRotation(Vec3d vec, float partialTicks) {
        Vec3d eyesPos = new Vec3d(RotationUtils.mc.player.getX(), RotationUtils.mc.player.getY() + (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose()), RotationUtils.mc.player.getZ()).add(RotationUtils.mc.player.getVelocity().multiply((double)partialTicks));
        Vec3d diff = vec.subtract(eyesPos);
        double diffX = diff.x;
        double diffZ = diff.z;
        double horizontalDistance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, horizontalDistance)));
        return new Rotation(yaw, pitch);
    }

    public static Rotation getAngles(Entity entity) {
        if (entity == null || RotationUtils.mc.player == null) {
            return null;
        }
        double diffX = entity.getX() - RotationUtils.mc.player.getX();
        double diffZ = entity.getZ() - RotationUtils.mc.player.getZ();
        double entityEyeHeight = entity.getEyeHeight(entity.getPose());
        double playerEyeHeight = RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose());
        double diffY = entity.getY() + entityEyeHeight * 0.9 - (RotationUtils.mc.player.getY() + playerEyeHeight);
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
        return new Rotation(RotationUtils.mc.player.getYaw() + MathHelper.wrapDegrees((float)(yaw - RotationUtils.mc.player.getYaw())), RotationUtils.mc.player.getPitch() + MathHelper.wrapDegrees((float)(pitch - RotationUtils.mc.player.getPitch())));
    }

    public static Rotation getRotationToBlock(BlockPos blockPos, Direction direction) {
        double centerX = (double)blockPos.getX() + 0.5 + (double)direction.getOffsetX() * 0.5;
        double centerY = (double)blockPos.getY() + 0.5 + (double)direction.getOffsetY() * 0.5;
        double centerZ = (double)blockPos.getZ() + 0.5 + (double)direction.getOffsetZ() * 0.5;
        double playerX = RotationUtils.mc.player.getX();
        double playerY = RotationUtils.mc.player.getY() + (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose());
        double playerZ = RotationUtils.mc.player.getZ();
        double deltaX = centerX - playerX;
        double deltaY = centerY - playerY;
        double deltaZ = centerZ - playerZ;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, distanceXZ)));
        return new Rotation(yaw, pitch);
    }

    public static VecRotation faceBlock(BlockPos blockPos) {
        if (blockPos == null) {
            return null;
        }
        VecRotation vecRotation = null;
        for (double xSearch = 0.1; xSearch < 0.9; xSearch += 0.1) {
            for (double ySearch = 0.1; ySearch < 0.9; ySearch += 0.1) {
                for (double zSearch = 0.1; zSearch < 0.9; zSearch += 0.1) {
                    Vec3d eyesPos = new Vec3d(RotationUtils.mc.player.getX(), RotationUtils.mc.player.getBoundingBox().minY + (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose()), RotationUtils.mc.player.getZ());
                    Vec3d posVec = new Vec3d((double)blockPos.getX() + xSearch, (double)blockPos.getY() + ySearch, (double)blockPos.getZ() + zSearch);
                    double dist = eyesPos.distanceTo(posVec);
                    double diffX = posVec.x - eyesPos.x;
                    double diffY = posVec.y - eyesPos.y;
                    double diffZ = posVec.z - eyesPos.z;
                    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                    Rotation rotation = new Rotation(MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f)), MathHelper.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))))));
                    Vec3d rotationVector = RotationUtils.getRotationVec(rotation);
                    Vec3d vector = eyesPos.add(rotationVector.x * dist, rotationVector.y * dist, rotationVector.z * dist);
                    BlockHitResult obj = RotationUtils.mc.world.raycast(new RaycastContext(eyesPos, vector, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)RotationUtils.mc.player));
                    if (obj.getType() != HitResult.Type.BLOCK) continue;
                    VecRotation currentVec = new VecRotation(posVec, rotation);
                    if (vecRotation != null && !(RotationUtils.getRotationDifference(currentVec.rotation()) < RotationUtils.getRotationDifference(vecRotation.rotation()))) continue;
                    vecRotation = currentVec;
                }
            }
        }
        return vecRotation;
    }

    public static double getRotationDifference(Rotation rotation) {
        return lastRotation == null ? 0.0 : RotationUtils.getRotationDifference(rotation, lastRotation);
    }

    public static double getRotationDifference(Rotation a, Rotation b) {
        return Math.hypot(RotationUtils.getAngleDifference(a.yaw, b.getPitch()), a.pitch - b.getYaw());
    }

    public static Vec3d getRotationVec(Rotation rot) {
        return RotationUtils.getRotationVec(rot.yaw, rot.pitch);
    }

    public static Vec3d getRotationVec(float yaw, float pitch) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos((float)g);
        float i = MathHelper.sin((float)g);
        float j = MathHelper.cos((float)f);
        float k = MathHelper.sin((float)f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    public static Rotation getRotationBlock(BlockPos pos, float predict) {
        return RotationUtils.getRotationByVector(RotationUtils.mc.player.getEyePos(), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), false);
    }

    public static Rotation getRotationBlock(BlockPos pos, Direction direction, float predict) {
        return RotationUtils.getRotationByVector(RotationUtils.mc.player.getEyePos(), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5).add(Vec3d.of((Vec3i)direction.getVector()).multiply(0.5)), false);
    }

    public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (moveForward < 0.0) {
            forward = -0.5f;
        } else if (moveForward > 0.0) {
            forward = 0.5f;
        }
        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0f * forward;
        }
        if (moveStrafing < 0.0) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double normalizeYawDiff(float yaw, float yaw2) {
        double diff = yaw - yaw2;
        return Math.abs(RotationUtils.normalizeYaw(diff));
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (event.getState().equals((Object)Stage.PRE) && rotation != null && !strafeFixSkipped) {
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());
        }
    }

    @EventHandler
    public void onTickPre(TickEvent event) {
        if (event.state.equals((Object)Stage.PRE) && rotation != null) {
            lastRotation = rotation.copy();
        }
    }

    @EventHandler(priority=-1145144)
    public void onHighLevelPacket(HighLevelPacketEvent event) {
        PlayerMoveC2SPacket wrapper;
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerMoveC2SPacket) {
            wrapper = (PlayerMoveC2SPacket)packet;
            if (wrapper.changeLook) {
                serverRotation = new Rotation(wrapper.getYaw(RotationUtils.mc.player.getYaw()), wrapper.getPitch(RotationUtils.mc.player.getPitch()));
            }
        }
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket correction = (PlayerPositionLookS2CPacket)packet;
            serverRotation = new Rotation(correction.change().yaw(), correction.change().pitch());
        }
    }

    @EventHandler
    public void onStrafe(StrafeEvent event) {
        if (!RotationUtils.isStrafeFix() && !RotationUtils.isStrictFix()) {
            return;
        }
        event.setYaw(rotation.getYaw());
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (!RotationUtils.isStrafeFix() && !RotationUtils.isStrictFix()) {
            return;
        }
        event.setYaw(rotation.getYaw());
    }

    public static void setHeadYaw(PlayerEntityRenderState state, float delta) {
        RotationUtils.setBodyYaw(state, delta);
    }

    public static void setBodyYaw(PlayerEntityRenderState state, float delta) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        float yaw = MathHelper.lerpAngleDegrees((float)delta, (float)RotationUtils.lastRotation.yaw, (float)RotationUtils.rotation.yaw);
        state.bodyYaw = RotationUtils.clampBodyYaw((LivingEntity)RotationUtils.mc.player, yaw, delta);
        state.yawDegrees = MathHelper.wrapDegrees((float)(yaw - state.bodyYaw));
    }

    public static void setPitch(PlayerEntityRenderState state, float delta) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        state.pitch = MathHelper.lerpAngleDegrees((float)delta, (float)RotationUtils.lastRotation.pitch, (float)RotationUtils.rotation.pitch);
    }

    private static float clampBodyYaw(LivingEntity entity, float degrees, float tickDelta) {
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)vehicle;
            float lerped = MathHelper.lerpAngleDegrees((float)tickDelta, (float)living.prevBodyYaw, (float)living.bodyYaw);
            float clamped = MathHelper.clamp((float)MathHelper.wrapDegrees((float)(degrees - lerped)), (float)-85.0f, (float)85.0f);
            lerped = degrees - clamped;
            if (Math.abs(clamped) > 50.0f) {
                lerped += clamped * 0.2f;
            }
            return lerped;
        }
        return MathHelper.lerpAngleDegrees((float)tickDelta, (float)entity.prevBodyYaw, (float)entity.bodyYaw);
    }

    public static void setHeadYaw(Args args) {
        RotationUtils.setBodyYaw(args);
    }

    public static void setBodyYaw(Args args) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        args.set(1, (Object)Float.valueOf(RotationUtils.lastRotation.yaw));
        args.set(2, (Object)Float.valueOf(RotationUtils.rotation.yaw));
    }

    public static void setPitch(Args args) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        args.set(1, (Object)Float.valueOf(RotationUtils.lastRotation.pitch));
        args.set(2, (Object)Float.valueOf(RotationUtils.rotation.pitch));
    }

    public static Rotation getPlayerRotations(Entity entity) {
        double distanceToEnt = RotationUtils.mc.player.distanceTo(entity);
        double predictX = entity.getX() + (entity.getX() - entity.lastRenderX) * (distanceToEnt * 0.8);
        double predictZ = entity.getZ() + (entity.getZ() - entity.lastRenderZ) * (distanceToEnt * 0.8);
        double x = predictX - RotationUtils.mc.player.getX();
        double z = predictZ - RotationUtils.mc.player.getZ();
        double h = entity.getY() + 1.0 - (RotationUtils.mc.player.getY() + (double)RotationUtils.mc.player.getEyeHeight(RotationUtils.mc.player.getPose()));
        double h1 = Math.sqrt(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = -RotationUtils.getTrajAngleSolutionLow((float)h1, (float)h, 1.0f);
        return new Rotation(yaw, pitch);
    }

    public static float getTrajAngleSolutionLow(float d3, float d1, float velocity) {
        float g = 0.006f;
        float sqrt = velocity * velocity * velocity * velocity - g * (g * (d3 * d3) + 2.0f * d1 * (velocity * velocity));
        return (float)Math.toDegrees(Math.atan(((double)(velocity * velocity) - Math.sqrt(sqrt)) / (double)(g * d3)));
    }

    public static Rotation calculate(Entity entity, double range, double wallRange, float predict, float predictPlayer) {
        if (RotationUtils.mc.player == null) {
            return null;
        }
        Vec3d entityPos = entity.getEyePos();
        Rotation normalRotations = RotationUtils.getRotationByVector(RotationUtils.mc.player.getEyePos(), new Vec3d(entityPos.x, MathUtils.clamp(RotationUtils.mc.player.getEyePos().y, entity.getBoundingBox().minY, entity.getBoundingBox().maxY), entityPos.z), false);
        EntityHitResult normalResult = RayCastUtils.raytraceEntity(range, normalRotations);
        if (normalResult != null && normalResult.getType() == HitResult.Type.ENTITY) {
            return normalRotations;
        }
        return RotationUtils.calculateSimple(entity, range, wallRange, predict, predictPlayer);
    }

    public static Rotation calculate(Vec3d from, Vec3d to) {
        Vec3d diff = to.subtract(from);
        double horizontalDistance = MathHelper.hypot((double)diff.x, (double)diff.z);
        float yaw = (float)(MathHelper.atan2((double)diff.z, (double)diff.x) * 57.29577951308232) - 90.0f;
        float pitch = (float)(-(MathHelper.atan2((double)diff.y, (double)horizontalDistance) * 57.29577951308232));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculate(Vec3d to) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return new Rotation(0.0f, 0.0f);
        }
        Vec3d from = new Vec3d(player.getX(), player.getEyeY(), player.getZ());
        return RotationUtils.calculate(from, to);
    }

    public static Rotation calculate(Vec3d position, Direction direction) {
        double x = position.x + 0.5;
        double y = position.y + 0.5;
        double z = position.z + 0.5;
        Vec3i offset = direction.getVector();
        return RotationUtils.calculate(new Vec3d(x += (double)offset.getX() * 0.5, y += (double)offset.getY() * 0.5, z += (double)offset.getZ() * 0.5));
    }

    public static float getAngleDifference(float a, float b) {
        return ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f;
    }

    public static Rotation calculateSimple(Entity entity, double range, double wallRange, float predict, float predictPlayer) {
        Box aabb = entity.getBoundingBox();
        if (predict != 0.0f) {
            aabb = aabb.offset((entity.getX() - entity.lastRenderX) * (double)predict, (entity.getY() - entity.lastRenderY) * (double)predict, (entity.getZ() - entity.lastRenderZ) * (double)predict);
        }
        Vec3d eyePos = null;
        if (RotationUtils.mc.player != null) {
            eyePos = RotationUtils.mc.player.getEyePos();
        }
        Vec3d nearest = null;
        if (eyePos != null) {
            nearest = new Vec3d(MathUtils.clamp(eyePos.x, aabb.minX, aabb.maxX), MathUtils.clamp(eyePos.y, aabb.minY, aabb.maxY), MathUtils.clamp(eyePos.z, aabb.minZ, aabb.maxZ));
        }
        Rotation rotation = null;
        if (nearest != null) {
            rotation = RotationUtils.getRotationByVector(RotationUtils.mc.player.getEyePos(), nearest, false);
        }
        if (nearest != null && nearest.subtract(eyePos).lengthSquared() <= wallRange * wallRange) {
            return rotation;
        }
        EntityHitResult result = RayCastUtils.raytraceEntity(range, rotation);
        double maxRange = Math.max(wallRange, range);
        if (result != null && result.getEntity() == entity && result.getPos().subtract(eyePos).lengthSquared() <= maxRange * maxRange) {
            return rotation;
        }
        return null;
    }

    public static Rotation getRotationTo(Vec3d start, Vec3d end) {
        double diffX = end.getX() - start.x;
        double diffY = end.getY() - start.y;
        double diffZ = end.getZ() - start.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new Rotation(yaw, pitch);
    }

    public static Rotation getRotationByVector(Vec3d eyePos, Vec3d vec, boolean noise) {
        Vec3d diff = vec.subtract(eyePos);
        float yaw = MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f));
        float pitch = MathHelper.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))))));
        float noiseYaw = yaw + (noise ? (float)ThreadLocalRandom.current().nextGaussian() * 5.0f : 0.0f);
        float noisePitch = pitch + (noise ? (float)ThreadLocalRandom.current().nextGaussian() * 2.0f : 0.0f);
        return new Rotation(noiseYaw, noisePitch);
    }

    private static Rotation getRandomRotation(List<Pair<Rotation, Integer>> list) {
        int highestPriorityValue = list.stream().mapToInt(Pair::getValue).max().orElse(-1);
        List<Pair<Rotation, Integer>> highestPriorityRotations = rotationList.stream().filter(pair -> (Integer)pair.getValue() == highestPriorityValue).toList();
        if (highestPriorityRotations.size() > 1) {
            Random random = new Random();
            return (Rotation)highestPriorityRotations.get(random.nextInt(highestPriorityRotations.size())).getKey();
        }
        if (highestPriorityRotations.size() == 1) {
            return (Rotation)highestPriorityRotations.getFirst().getKey();
        }
        return null;
    }

    public static double nearestRotation(Box bb) {
        Vec3d eyes = RotationUtils.mc.player.getEyePos();
        Vec3d vecRotation3d = null;
        for (double xSearch = 0.0; xSearch <= 1.0; xSearch += 0.05) {
            for (double ySearch = 0.0; ySearch < 1.0; ySearch += 0.05) {
                for (double zSearch = 0.0; zSearch <= 1.0; zSearch += 0.05) {
                    Vec3d vec3 = new Vec3d(bb.minX + (bb.maxX - bb.minX) * xSearch, bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch);
                    double vecDist = eyes.squaredDistanceTo(vec3);
                    if (vecRotation3d != null && !(eyes.squaredDistanceTo(vecRotation3d) > vecDist)) continue;
                    vecRotation3d = vec3;
                }
            }
        }
        return vecRotation3d.distanceTo(eyes);
    }

    public static boolean inFoV(Entity entity, float fov) {
        return (Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.player.getYaw()) % 360.0f > 180.0f ? 360.0f - Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.player.getYaw()) % 360.0f : Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.player.getYaw()) % 360.0f) <= fov;
    }

    public static float getYawToEntity(Entity e) {
        return Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.player.getYaw()) % 360.0f > 180.0f ? 360.0f - Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.player.getYaw()) % 360.0f : Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.player.getYaw()) % 360.0f;
    }

    public static Vec3d getVFR(Rotation rotation) {
        float yawCos = (float)Math.cos(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = (float)Math.sin(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = (float)(-Math.cos(-rotation.getPitch() * ((float)Math.PI / 180)));
        float pitchSin = (float)Math.sin(-rotation.getPitch() * ((float)Math.PI / 180));
        return new Vec3d((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
    }

    public static Vec3d getVectorForRotation(Rotation rotation) {
        float yawCos = (float)Math.cos(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = (float)Math.sin(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = (float)(-Math.cos(-rotation.getPitch() * ((float)Math.PI / 180)));
        float pitchSin = (float)Math.sin(-rotation.getPitch() * ((float)Math.PI / 180));
        return new Vec3d((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
    }

    public static Rotation calculate(Vector3d vector3d, Direction enumFacing) {
        return RotationUtils.calculate(new Vector3d(vector3d.getX() + 0.5 + (double)enumFacing.getOffsetX() * 0.5, vector3d.getY() + 0.5 + (double)enumFacing.getOffsetY() * 0.5, vector3d.getZ() + 0.5 + (double)enumFacing.getOffsetZ() * 0.5));
    }

    public static Rotation calculate(Vector3d vector3d) {
        return RotationUtils.calculate(RotationUtils.mc.player.getPos().add(0.0, (double)RotationUtils.getEyeHeight(), 0.0), vector3d);
    }

    public static Rotation calculate(Vec3d vector3d, Vector3d vector3d2) {
        Vector3d subtract = vector3d2.subtract(new Vector3d(vector3d.x, vector3d.y, vector3d.z));
        return new Rotation((float)(MathHelper.atan2((double)subtract.getZ(), (double)subtract.getX()) * 57.2957763671875) - 90.0f, (float)(-(MathHelper.atan2((double)subtract.getY(), (double)Math.hypot(subtract.getX(), subtract.getZ())) * 57.2957763671875)));
    }

    public static float getEyeHeight() {
        float f = 1.62f;
        if (RotationUtils.mc.player.isSleeping()) {
            f = 0.2f;
        }
        if (RotationUtils.mc.player.isSneaking()) {
            f -= 0.08f;
        }
        return f;
    }

    public static Rotation getRandomRotationTo(BlockPos blockPos, Direction facing) {
        ClientPlayerEntity player = RotationUtils.mc.player;
        if (blockPos == null || facing == null || player == null) {
            return new Rotation(RotationUtils.mc.player.getYaw(), RotationUtils.mc.player.getPitch());
        }
        Vec3d playerEyePos = player.getEyePos();
        Vec3d randomTargetPoint = RotationUtils.getRandomPointOnFace(blockPos, facing);
        return RotationUtils.calculateRotationFromPoints(playerEyePos, randomTargetPoint);
    }

    private static Rotation calculateRotationFromPoints(Vec3d start, Vec3d target) {
        Vec3d d = target.subtract(start);
        double dx = d.x;
        double dy = d.y;
        double dz = d.z;
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float)Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new Rotation(yaw, pitch);
    }

    private static boolean nearly(double a, double b) {
        return Math.abs(a - b) <= 1.0E-7;
    }

    private static Vec3d getRandomPointOnFace(BlockPos pos, Direction face) {
        double area;
        ShapeContext ctx;
        World world = RotationUtils.mc.player.getWorld();
        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getOutlineShape((BlockView)world, pos, ctx = ShapeContext.of((Entity)RotationUtils.mc.player));
        if (shape.isEmpty()) {
            shape = state.getCollisionShape((BlockView)world, pos, ctx);
        }
        if (shape.isEmpty()) {
            shape = VoxelShapes.fullCube();
        }
        ArrayList<Box> boxes = new ArrayList<>(shape.getBoundingBoxes());
        double sMinX = Double.POSITIVE_INFINITY;
        double sMaxX = Double.NEGATIVE_INFINITY;
        double sMinY = Double.POSITIVE_INFINITY;
        double sMaxY = Double.NEGATIVE_INFINITY;
        double sMinZ = Double.POSITIVE_INFINITY;
        double sMaxZ = Double.NEGATIVE_INFINITY;
        for (Object b : boxes) {
            if (((Box)b).minX < sMinX) {
                sMinX = ((Box)b).minX;
            }
            if (((Box)b).maxX > sMaxX) {
                sMaxX = ((Box)b).maxX;
            }
            if (((Box)b).minY < sMinY) {
                sMinY = ((Box)b).minY;
            }
            if (((Box)b).maxY > sMaxY) {
                sMaxY = ((Box)b).maxY;
            }
            if (((Box)b).minZ < sMinZ) {
                sMinZ = ((Box)b).minZ;
            }
            if (!(((Box)b).maxZ > sMaxZ)) continue;
            sMaxZ = ((Box)b).maxZ;
        }
        record FaceSlice(Box box, double area) {
        }
        ArrayList<FaceSlice> candidates = new ArrayList<FaceSlice>();
        block21: for (Box b : boxes) {
            switch (face) {
                case EAST: {
                    if (!RotationUtils.nearly(b.maxX, sMaxX)) break;
                    area = (b.maxY - b.minY) * (b.maxZ - b.minZ);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case WEST: {
                    if (!RotationUtils.nearly(b.minX, sMinX)) break;
                    area = (b.maxY - b.minY) * (b.maxZ - b.minZ);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case UP: {
                    if (!RotationUtils.nearly(b.maxY, sMaxY)) break;
                    area = (b.maxX - b.minX) * (b.maxZ - b.minZ);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case DOWN: {
                    if (!RotationUtils.nearly(b.minY, sMinY)) break;
                    area = (b.maxX - b.minX) * (b.maxZ - b.minZ);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case SOUTH: {
                    if (!RotationUtils.nearly(b.maxZ, sMaxZ)) break;
                    area = (b.maxX - b.minX) * (b.maxY - b.minY);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case NORTH: {
                    if (!RotationUtils.nearly(b.minZ, sMinZ) || !((area = (b.maxX - b.minX) * (b.maxY - b.minY)) > 0.0)) break;
                    candidates.add(new FaceSlice(b, area));
                }
            }
        }
        if (candidates.isEmpty()) {
            for (Box b : boxes) {
                switch (face.getAxis()) {
                    case X: {
                        area = (b.maxY - b.minY) * (b.maxZ - b.minZ);
                        break;
                    }
                    case Y: {
                        area = (b.maxX - b.minX) * (b.maxZ - b.minZ);
                        break;
                    }
                    default: {
                        area = (b.maxX - b.minX) * (b.maxY - b.minY);
                    }
                }
                if (!(area > 0.0)) continue;
                candidates.add(new FaceSlice(b, area));
            }
        }
        double total = 0.0;
        for (FaceSlice fs : candidates) {
            total += fs.area;
        }
        double pick = random.nextDouble() * total;
        Box chosen = null;
        for (FaceSlice fs : candidates) {
            if (!((pick -= fs.area) <= 0.0)) continue;
            chosen = fs.box;
            break;
        }
        if (chosen == null) {
            chosen = ((FaceSlice)candidates.get((int)(candidates.size() - 1))).box;
        }
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        switch (face) {
            case EAST: {
                double ry = chosen.minY + random.nextDouble() * (chosen.maxY - chosen.minY);
                double rz = chosen.minZ + random.nextDouble() * (chosen.maxZ - chosen.minZ);
                x += chosen.maxX;
                y += ry;
                z += rz;
                break;
            }
            case WEST: {
                double ry = chosen.minY + random.nextDouble() * (chosen.maxY - chosen.minY);
                double rz = chosen.minZ + random.nextDouble() * (chosen.maxZ - chosen.minZ);
                x += chosen.minX;
                y += ry;
                z += rz;
                break;
            }
            case UP: {
                double rx = chosen.minX + random.nextDouble() * (chosen.maxX - chosen.minX);
                double rz = chosen.minZ + random.nextDouble() * (chosen.maxZ - chosen.minZ);
                x += rx;
                y += chosen.maxY;
                z += rz;
                break;
            }
            case DOWN: {
                double rx = chosen.minX + random.nextDouble() * (chosen.maxX - chosen.minX);
                double rz = chosen.minZ + random.nextDouble() * (chosen.maxZ - chosen.minZ);
                x += rx;
                y += chosen.minY;
                z += rz;
                break;
            }
            case SOUTH: {
                double rx = chosen.minX + random.nextDouble() * (chosen.maxX - chosen.minX);
                double ry = chosen.minY + random.nextDouble() * (chosen.maxY - chosen.minY);
                x += rx;
                y += ry;
                z += chosen.maxZ;
                break;
            }
            case NORTH: {
                double rx = chosen.minX + random.nextDouble() * (chosen.maxX - chosen.minX);
                double ry = chosen.minY + random.nextDouble() * (chosen.maxY - chosen.minY);
                x += rx;
                y += ry;
                z += chosen.minZ;
            }
        }
        return new Vec3d(x, y, z);
    }

    public static Rotation getClosestToBlockFace(BlockPos pos, Direction face, float lastYaw, float lastPitch) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return new Rotation(lastYaw, 80.5f);
        }
        Rotation currentRot = new Rotation(lastYaw, lastPitch);
        if (ClientRayTraceUtil.didHitBlockFace(currentRot, pos, face, true)) {
            return currentRot;
        }
        BlockState state = player.getWorld().getBlockState(pos);
        VoxelShape shape = state.getOutlineShape((BlockView)player.getWorld(), pos);
        if (shape.isEmpty()) {
            shape = VoxelShapes.fullCube();
        }
        List<Box> boxes = shape.getBoundingBoxes();
        Rotation bestRot = null;
        Rotation closeRot = null;
        double minDiff = Double.MAX_VALUE;
        Vec3d eyePos = ClientRayTraceUtil.eyePos;
        for (Box box : boxes) {
            Box worldBox = box.offset(pos);
            Vec3d optimalPoint = null;
            ArrayList<Vec3d> hitPoints = new ArrayList<Vec3d>();
            List<Vec3d> optimalPoints = RotationUtils.getOptimalPointOnFace(worldBox, face, eyePos, lastYaw);
            double minYawDiff = Double.MAX_VALUE;
            for (Vec3d point : optimalPoints) {
                double diff;
                Rotation rot = RotationUtils.calculate(eyePos, point);
                if (Math.abs(rot.pitch) > 89.9f) {
                    rot.yaw = lastYaw;
                }
                if (!ClientRayTraceUtil.didHitBlockFace(rot, pos, face, true) || !((diff = Math.abs(RotationUtils.normalizeYaw(rot.yaw - lastYaw))) < minYawDiff)) continue;
                minYawDiff = diff;
                bestRot = rot;
                optimalPoint = point;
            }
            if (optimalPoint != null) {
                hitPoints.add(optimalPoint);
            }
            Vec3d projectedPoint = RotationUtils.getProjectedPointOnFace(worldBox, face, eyePos, lastYaw, RandomUtils.generateRandomFloat(0.05f, 0.1f));
            closeRot = RotationUtils.calculate(eyePos, projectedPoint);
            if (RotationUtils.tryHit(projectedPoint, pos, face, eyePos)) {
                hitPoints.add(projectedPoint);
            }
            if (hitPoints.isEmpty()) continue;
            for (Vec3d hitPoint : hitPoints) {
                double yawDiff;
                Rotation rot = RotationUtils.calculate(eyePos, hitPoint);
                if (Math.abs(rot.pitch) > 89.9f) {
                    rot.yaw = lastYaw;
                }
                if (!((yawDiff = Math.abs(RotationUtils.normalizeYaw(rot.yaw - lastYaw))) < minDiff)) continue;
                minDiff = yawDiff;
                bestRot = rot;
            }
        }
        return bestRot != null ? bestRot : closeRot;
    }

    private static boolean tryHit(Vec3d target, BlockPos pos, Direction face, Vec3d eyePos) {
        Rotation rot = RotationUtils.calculate(eyePos, target);
        return ClientRayTraceUtil.didHitBlockFace(rot, pos, face, true);
    }

    private static List<Vec3d> getOptimalPointOnFace(Box box, Direction face, Vec3d eyePos, float lastYaw) {
        ArrayList<Vec3d> points = new ArrayList<Vec3d>();
        double edgeInset = RandomUtils.generateRandomFloat(0.1f, 0.15f);
        if (face.getAxis() == Direction.Axis.Y) {
            double planeY = face == Direction.UP ? box.maxY : box.minY;
            double clampedX = MathHelper.clamp((double)eyePos.x, (double)(box.minX + edgeInset), (double)(box.maxX - edgeInset));
            double clampedZ = MathHelper.clamp((double)eyePos.z, (double)(box.minZ + edgeInset), (double)(box.maxZ - edgeInset));
            points.add(new Vec3d(clampedX, planeY, clampedZ));
            double centerX = (box.minX + box.maxX) * 0.5;
            double centerZ = (box.minZ + box.maxZ) * 0.5;
            points.add(new Vec3d(centerX, planeY, centerZ));
        } else {
            double centerX = face.getAxis() == Direction.Axis.X ? (face == Direction.EAST ? box.maxX : box.minX) : (box.minX + box.maxX) * 0.5;
            double centerY = (box.minY + box.maxY) * 0.5;
            double centerZ = face.getAxis() == Direction.Axis.Z ? (face == Direction.SOUTH ? box.maxZ : box.minZ) : (box.minZ + box.maxZ) * 0.5;
            points.add(new Vec3d(centerX, centerY, centerZ));
            double yawRad = Math.toRadians(lastYaw);
            double dirX = -Math.sin(yawRad);
            double dirZ = Math.cos(yawRad);
            double bestX = centerX;
            double bestZ = centerZ;
            if (face.getAxis() == Direction.Axis.Z) {
                double planeZ;
                double t;
                planeZ = face == Direction.SOUTH ? box.maxZ : box.minZ;
                if (Math.abs(dirZ) > 1.0E-4 && (t = (planeZ - eyePos.z) / dirZ) > 0.0) {
                    double intersectX = eyePos.x + t * dirX;
                    bestX = MathHelper.clamp((double)intersectX, (double)(box.minX + edgeInset), (double)(box.maxX - edgeInset));
                    bestZ = planeZ;
                }
            } else if (face.getAxis() == Direction.Axis.X) {
                double planeX;
                double t;
                planeX = face == Direction.EAST ? box.maxX : box.minX;
                if (Math.abs(dirX) > 1.0E-4 && (t = (planeX - eyePos.x) / dirX) > 0.0) {
                    double intersectZ = eyePos.z + t * dirZ;
                    bestZ = MathHelper.clamp((double)intersectZ, (double)(box.minZ + edgeInset), (double)(box.maxZ - edgeInset));
                    bestX = planeX;
                }
            }
            for (double y = box.minY + 0.1; y < box.maxY; y += 0.4) {
                points.add(new Vec3d(bestX, y, bestZ));
            }
            points.add(new Vec3d(bestX, box.maxY - 0.1, bestZ));
            points.add(new Vec3d(bestX, box.minY + 0.1, bestZ));
        }
        return points;
    }

    private static Vec3d getProjectedPointOnFace(Box box, Direction face, Vec3d eye, float lastYaw, double epsilon) {
        double t;
        Vec3d u;
        double cx = face.getAxis() == Direction.Axis.X ? (face == Direction.EAST ? box.maxX : box.minX) : (box.minX + box.maxX) * 0.5;
        double cy = face.getAxis() == Direction.Axis.Y ? (face == Direction.UP ? box.maxY : box.minY) : (box.minY + box.maxY) * 0.5;
        double cz = face.getAxis() == Direction.Axis.Z ? (face == Direction.SOUTH ? box.maxZ : box.minZ) : (box.minZ + box.maxZ) * 0.5;
        Vec3d center = new Vec3d(cx, cy, cz);
        Vec3d v = switch (face.getAxis()) {
            case Direction.Axis.X -> {
                u = new Vec3d(0.0, 1.0, 0.0);
                yield new Vec3d(0.0, 0.0, 1.0);
            }
            case Direction.Axis.Y -> {
                u = new Vec3d(1.0, 0.0, 0.0);
                yield new Vec3d(0.0, 0.0, 1.0);
            }
            default -> {
                u = new Vec3d(1.0, 0.0, 0.0);
                yield new Vec3d(0.0, 1.0, 0.0);
            }
        };
        double halfU = (u.x != 0.0 ? box.maxX - box.minX : (u.y != 0.0 ? box.maxY - box.minY : box.maxZ - box.minZ)) * 0.5;
        double halfV = (v.x != 0.0 ? box.maxX - box.minX : (v.y != 0.0 ? box.maxY - box.minY : box.maxZ - box.minZ)) * 0.5;
        double rad = Math.toRadians(lastYaw + 90.0f);
        Vec3d dir = new Vec3d(Math.cos(rad), 0.0, Math.sin(rad));
        Vec3d normal = new Vec3d((double)face.getOffsetX(), (double)face.getOffsetY(), (double)face.getOffsetZ());
        double denom = normal.dotProduct(dir);
        if (Math.abs(denom) > 1.0E-6 && (t = normal.dotProduct(center.subtract(eye)) / denom) > 0.0) {
            Vec3d hit = eye.add(dir.multiply(t));
            double du = hit.subtract(center).dotProduct(u);
            double dv = hit.subtract(center).dotProduct(v);
            if (Math.abs(du) <= halfU && Math.abs(dv) <= halfV) {
                return RotationUtils.adjustInsideFace(center, u, v, du, dv, halfU, halfV, epsilon);
            }
        }
        return RotationUtils.projectToFaceBoundary(eye, dir, center, u, v, halfU, halfV, epsilon);
    }

    private static Vec3d projectToFaceBoundary(Vec3d eye, Vec3d dir, Vec3d center, Vec3d u, Vec3d v, double halfU, double halfV, double epsilon) {
        Vec3d proj = eye.add(dir.multiply(100.0));
        double du = proj.subtract(center).dotProduct(u);
        double dv = proj.subtract(center).dotProduct(v);
        du = Math.max(-halfU, Math.min(halfU, du));
        dv = Math.max(-halfV, Math.min(halfV, dv));
        return RotationUtils.adjustInsideFace(center, u, v, du, dv, halfU, halfV, epsilon);
    }

    private static Vec3d adjustInsideFace(Vec3d center, Vec3d u, Vec3d v, double du, double dv, double halfU, double halfV, double epsilon) {
        if (Math.abs(du) >= halfU - 1.0E-4) {
            du = Math.copySign(halfU - epsilon, du);
        }
        if (Math.abs(dv) >= halfV - 1.0E-4) {
            dv = Math.copySign(halfV - epsilon, dv);
        }
        return center.add(u.multiply(du)).add(v.multiply(dv));
    }

    private static Vec3d getCenterOfFace(Box box, Direction face) {
        double x = (box.minX + box.maxX) * 0.5;
        double y = (box.minY + box.maxY) * 0.5;
        double z = (box.minZ + box.maxZ) * 0.5;
        switch (face) {
            case WEST: {
                x = box.minX;
                break;
            }
            case EAST: {
                x = box.maxX;
                break;
            }
            case DOWN: {
                y = box.minY;
                break;
            }
            case UP: {
                y = box.maxY;
                break;
            }
            case NORTH: {
                z = box.minZ;
                break;
            }
            case SOUTH: {
                z = box.maxZ;
            }
        }
        return new Vec3d(x, y, z);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double normalizeYaw(double yaw) {
        return ((yaw + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    public static double yawDiffDirectly(float yaw, float yaw2) {
        double diff = yaw - yaw2;
        return RotationUtils.normalizeYaw(diff);
    }

    public static float smooth(float diff, float value) {
        if (Math.abs(diff) < value) {
            return diff;
        }
        if (diff > 0.0f) {
            return value;
        }
        if (diff < 0.0f) {
            return -value;
        }
        return 0.0f;
    }

    @Generated
    public static Rotation getRotation() {
        return rotation;
    }

    @Generated
    public static Rotation getLastRotation() {
        return lastRotation;
    }

        public static Rotation getServerRotation() {
        if (serverRotation == null) {
            if (mc != null && mc.player != null) {
                serverRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
            } else {
                return new Rotation(0.0f, 0.0f);
            }
        }
        return serverRotation;
    }

    @Generated
    public static MoveFixMode getStrafeFixMode() {
        return strafeFixMode;
    }

    @Generated
    public static void setStrafeFixMode(MoveFixMode strafeFixMode) {
        RotationUtils.strafeFixMode = strafeFixMode;
    }

    @Generated
    public static List<Pair<Rotation, Integer>> getRotationList() {
        return rotationList;
    }

    static {
        rotationList = new ArrayList<Pair<Rotation, Integer>>();
        rotationMoveFixList = new ArrayList<MoveFixMode>();
        rotationEvent = new RotationEvent(false);
        random = ThreadLocalRandom.current();
    }
}
