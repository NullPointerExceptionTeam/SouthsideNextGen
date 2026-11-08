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
import net.minecraft.class_10055;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1511;
import net.minecraft.class_1675;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_2596;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2708;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_746;
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
        if (RotationUtils.mc.field_1724 == null && rotation != null) {
            return rotation;
        }
        if (RotationUtils.mc.field_1724 == null && rotation == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return rotation == null ? new Rotation(RotationUtils.mc.field_1724.method_36454(), RotationUtils.mc.field_1724.method_36455()) : rotation;
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

    public static class_243 getDirectionVector(float yaw, float pitch) {
        float yawRad = (float)Math.toRadians(-yaw - 180.0f);
        float pitchRad = (float)Math.toRadians(-pitch);
        float x = class_3532.method_15362((float)pitchRad) * class_3532.method_15374((float)yawRad);
        float y = class_3532.method_15374((float)pitchRad);
        float z = class_3532.method_15362((float)pitchRad) * class_3532.method_15362((float)yawRad);
        return new class_243((double)x, (double)y, (double)z).method_1029();
    }

    public static double calculateEntityDistance(class_1297 target) {
        class_238 targetBox = target.method_5829();
        if (target instanceof class_1511) {
            class_243 pos = target.method_19538();
            targetBox = new class_238(pos.field_1352 - 1.0, pos.field_1351, pos.field_1350 - 1.0, pos.field_1352 + 1.0, pos.field_1351 + 2.0, pos.field_1350 + 1.0);
        }
        targetBox = targetBox.method_1014(0.03);
        double maxReach = 3.0;
        double distanceRay = maxReach + 3.0;
        double minDistance = Double.MAX_VALUE;
        for (class_243 lookVec : List.of(RotationUtils.getRotationVector(RotationUtils.calculate((class_243)target.method_19538()).yaw, RotationUtils.calculate((class_243)target.method_19538()).pitch))) {
            Iterator<Float> iterator = List.of(Float.valueOf(RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376()))).iterator();
            while (iterator.hasNext()) {
                double dist;
                double eyeOffset = iterator.next().floatValue();
                class_243 eyePos = new class_243(RotationUtils.mc.field_1724.method_23317(), RotationUtils.mc.field_1724.method_23318() + eyeOffset, RotationUtils.mc.field_1724.method_23321());
                class_243 endPos = eyePos.method_1019(lookVec.method_1021(distanceRay));
                if (targetBox.method_1006(eyePos)) {
                    return 0.0;
                }
                Optional intercept = targetBox.method_992(eyePos, endPos);
                if (!intercept.isPresent() || !((dist = eyePos.method_1022((class_243)intercept.get())) < minDistance)) continue;
                minDistance = dist;
            }
        }
        return minDistance;
    }

    public static class_243 getRotationVector(float yaw, float pitch) {
        double yawRad = Math.toRadians(-yaw);
        double pitchRad = Math.toRadians(-pitch);
        double x = Math.cos(pitchRad) * Math.sin(yawRad);
        double y = Math.sin(pitchRad);
        double z = Math.cos(pitchRad) * Math.cos(yawRad);
        return new class_243(x, y, z);
    }

    private static class_238 getTargetBoundingBox(class_1297 entity) {
        return entity.method_5829();
    }

    private static boolean checkHitResult(class_243 eyePos, class_239 result, class_1297 target) {
        if (result.method_17783() == class_239.class_240.field_1331 && ((class_3966)result).method_17782() == target) {
            class_243 intercept = result.method_17784();
            return RotationUtils.isVecInside(RotationUtils.getTargetBoundingBox(target), eyePos) || intercept.method_1022(eyePos) <= 3.0;
        }
        return false;
    }

    public static boolean isVecInside(class_238 self, class_243 vec) {
        return vec.field_1352 > self.field_1323 && vec.field_1352 < self.field_1320 && vec.field_1351 > self.field_1322 && vec.field_1351 < self.field_1325 && vec.field_1350 > self.field_1321 && vec.field_1350 < self.field_1324;
    }

    public static float getDistanceToEntity(class_1297 target) {
        if (RotationUtils.mc.field_1724 == null) {
            return 0.0f;
        }
        class_243 eyes = RotationUtils.mc.field_1724.method_5836(1.0f);
        class_243 pos = RotationUtils.getNearestPointBB(eyes, target.method_5829());
        double xDist = Math.abs(pos.field_1352 - eyes.field_1352);
        double yDist = Math.abs(pos.field_1351 - eyes.field_1351);
        double zDist = Math.abs(pos.field_1350 - eyes.field_1350);
        return (float)Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0) + Math.pow(zDist, 2.0));
    }

    public static class_243 getNearestPointBB(class_243 eye, class_238 box) {
        double[] origin = new double[]{eye.field_1352, eye.field_1351, eye.field_1350};
        double[] destMinis = new double[]{box.field_1323, box.field_1322, box.field_1321};
        double[] destMaxis = new double[]{box.field_1320, box.field_1325, box.field_1324};
        for (int i = 0; i < 3; ++i) {
            if (origin[i] > destMaxis[i]) {
                origin[i] = destMaxis[i];
                continue;
            }
            if (!(origin[i] < destMinis[i])) continue;
            origin[i] = destMinis[i];
        }
        return new class_243(origin[0], origin[1], origin[2]);
    }

    private static class_239 rayTrace(Vector2f rotations) {
        class_238 box;
        double d0 = RotationUtils.mc.field_1724.method_55754();
        class_239 hitResult = RayCastUtils.rayCast(d0, 1.0f, false, rotations);
        class_243 vec3 = RotationUtils.mc.field_1724.method_5836(1.0f);
        boolean flag = false;
        double d1 = d0;
        if (d0 > 3.0) {
            flag = true;
        }
        d1 *= d1;
        if (hitResult != null) {
            d1 = hitResult.method_17784().method_1025(vec3);
        }
        class_243 vec31 = RotationUtils.getLook(rotations.x, rotations.y);
        class_243 vec32 = vec3.method_1031(vec31.field_1352 * d0, vec31.field_1351 * d0, vec31.field_1350 * d0);
        class_3966 entityhitresult = class_1675.method_18075((class_1297)RotationUtils.mc.field_1724, (class_243)vec3, (class_243)vec32, (class_238)(box = RotationUtils.mc.field_1724.method_5829().method_18804(vec31.method_1021(d0)).method_1014(1.0)), entity -> !entity.method_7325() && entity.method_30948(), (double)d1);
        if (entityhitresult != null) {
            class_243 vec33 = entityhitresult.method_17784();
            double d2 = vec3.method_1025(vec33);
            if (flag && d2 > 9.0) {
                hitResult = class_3965.method_17778((class_243)vec33, (class_2350)class_2350.method_10142((double)vec31.field_1352, (double)vec31.field_1351, (double)vec31.field_1350), (class_2338)class_2338.method_49638((class_2374)vec33));
            } else if (d2 < d1 || hitResult == null) {
                hitResult = entityhitresult;
            }
        }
        return hitResult;
    }

    public static class_243 getLook(float yaw, float pitch) {
        float f = class_3532.method_15362((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f1 = class_3532.method_15374((float)(-yaw * ((float)Math.PI / 180) - (float)Math.PI));
        float f2 = -class_3532.method_15362((float)(-pitch * ((float)Math.PI / 180)));
        float f3 = class_3532.method_15374((float)(-pitch * ((float)Math.PI / 180)));
        return new class_243((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public static Vector2f getRotations(class_243 eye, class_243 target) {
        double x = target.field_1352 - eye.field_1352;
        double y = target.field_1351 - eye.field_1351;
        double z = target.field_1350 - eye.field_1350;
        double diffXZ = Math.sqrt(x * x + z * z);
        float yaw = (float)Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(y, diffXZ)));
        return new Vector2f(class_3532.method_15393((float)yaw), class_3532.method_15393((float)pitch));
    }

    public static Rotation getBetterRotationCustom(class_1297 from, class_1297 target, double range) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || from == null || target == null) {
            return null;
        }
        class_243 eyePos = from.method_33571();
        class_238 box = target.method_5829();
        List<class_243> points = RotationUtils.getSamplePoints(box);
        Rotation bestRot = null;
        double bestDist = Double.MAX_VALUE;
        for (class_243 point : points) {
            double dist;
            if (eyePos.method_1022(point) > range || !RotationUtils.isUnobstructed(eyePos, point) || !((dist = eyePos.method_1022(point)) < bestDist)) continue;
            bestDist = dist;
            bestRot = Rotation.from(eyePos, point);
        }
        return bestRot;
    }

    public static Rotation getBetterRotation(class_1297 target, double range) {
        return RotationUtils.getBetterRotationCustom((class_1297)RotationUtils.mc.field_1724, target, range);
    }

    private static List<class_243> getSamplePoints(class_238 box) {
        ArrayList<class_243> list = new ArrayList<class_243>();
        list.add(box.method_1005());
        list.add(new class_243(box.method_1005().field_1352, box.field_1325, box.method_1005().field_1350));
        list.add(new class_243(box.method_1005().field_1352, box.field_1322, box.method_1005().field_1350));
        for (double x : new double[]{box.field_1323, box.field_1320}) {
            for (double y : new double[]{box.field_1322, box.field_1325}) {
                for (double z : new double[]{box.field_1321, box.field_1324}) {
                    list.add(new class_243(x, y, z));
                }
            }
        }
        for (int i = 0; i < 10; ++i) {
            double rx = box.field_1323 + Math.random() * (box.field_1320 - box.field_1323);
            double ry = box.field_1322 + Math.random() * (box.field_1325 - box.field_1322);
            double rz = box.field_1321 + Math.random() * (box.field_1324 - box.field_1321);
            list.add(new class_243(rx, ry, rz));
        }
        return list;
    }

    private static boolean isUnobstructed(class_243 from, class_243 to) {
        class_310 mc = class_310.method_1551();
        class_3965 result = mc.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
        return result.method_17783() == class_239.class_240.field_1333 || RotationUtils.squaredDistanceTo((class_239)result, from) >= from.method_1025(to) - 0.01;
    }

    public static double squaredDistanceTo(class_239 result, class_243 vec3d) {
        class_243 entity = result.method_17784();
        double d = vec3d.field_1352 - entity.method_10216();
        double e = vec3d.field_1351 - entity.method_10214();
        double f = vec3d.field_1350 - entity.method_10215();
        return d * d + e * e + f * f;
    }

    public static Rotation calculateRotation(class_243 cameraPos, class_243 aimPoint) {
        double dx = aimPoint.field_1352 - cameraPos.field_1352;
        double dy = aimPoint.field_1351 - cameraPos.field_1351;
        double dz = aimPoint.field_1350 - cameraPos.field_1350;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
        yaw = RotationUtils.wrapDegrees(yaw);
        pitch = class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f);
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
        class_310 mc = class_310.method_1551();
        float sensitivity = ((Double)mc.field_1690.method_42495().method_41753()).floatValue();
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
        if (RotationUtils.mc.field_1724 != null) {
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

    public static class_243 getClosestPoint(class_243 eyePos, class_238 box) {
        double closestX = class_3532.method_15350((double)eyePos.field_1352, (double)box.field_1323, (double)box.field_1320);
        double closestY = class_3532.method_15350((double)eyePos.field_1351, (double)box.field_1322, (double)box.field_1325);
        double closestZ = class_3532.method_15350((double)eyePos.field_1350, (double)box.field_1321, (double)box.field_1324);
        return new class_243(closestX, closestY, closestZ);
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
        return RotationUtils.getRawDirectionRotation(RotationUtils.mc.field_1724.method_36454(), RotationUtils.mc.field_1724.field_6212, RotationUtils.mc.field_1724.field_6250);
    }

    public static float[] getRotations(class_243 vec) {
        return RotationUtils.getRotations(vec.field_1352, vec.field_1351, vec.field_1350);
    }

    public static float[] getRotations(double x, double y, double z) {
        double deltaX = x - RotationUtils.mc.field_1724.method_23317();
        double deltaY = y - RotationUtils.mc.field_1724.method_23318() - (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376());
        double deltaZ = z - RotationUtils.mc.field_1724.method_23321();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)Math.toDegrees(-Math.atan2(deltaX, deltaZ));
        float pitch = (float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(class_1297 entity) {
        double pX = RotationUtils.mc.field_1724.method_23317();
        double pY = RotationUtils.mc.field_1724.method_23318() + (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376());
        double pZ = RotationUtils.mc.field_1724.method_23321();
        double eX = entity.method_23317();
        double eY = entity.method_23318() + (double)(entity.method_17682() / 2.0f);
        double eZ = entity.method_23321();
        double dX = pX - eX;
        double dY = pY - eY;
        double dZ = pZ - eZ;
        double dH = Math.sqrt(Math.pow(dX, 2.0) + Math.pow(dZ, 2.0));
        double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
        double pitch = Math.toDegrees(Math.atan2(dH, dY));
        return new float[]{(float)yaw, (float)(90.0 - pitch)};
    }

    public static Rotation toRotation(class_243 vec, float partialTicks) {
        class_243 eyesPos = new class_243(RotationUtils.mc.field_1724.method_23317(), RotationUtils.mc.field_1724.method_23318() + (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376()), RotationUtils.mc.field_1724.method_23321()).method_1019(RotationUtils.mc.field_1724.method_18798().method_1021((double)partialTicks));
        class_243 diff = vec.method_1020(eyesPos);
        double diffX = diff.field_1352;
        double diffZ = diff.field_1350;
        double horizontalDistance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diff.field_1351, horizontalDistance)));
        return new Rotation(yaw, pitch);
    }

    public static Rotation getAngles(class_1297 entity) {
        if (entity == null || RotationUtils.mc.field_1724 == null) {
            return null;
        }
        double diffX = entity.method_23317() - RotationUtils.mc.field_1724.method_23317();
        double diffZ = entity.method_23321() - RotationUtils.mc.field_1724.method_23321();
        double entityEyeHeight = entity.method_18381(entity.method_18376());
        double playerEyeHeight = RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376());
        double diffY = entity.method_23318() + entityEyeHeight * 0.9 - (RotationUtils.mc.field_1724.method_23318() + playerEyeHeight);
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
        return new Rotation(RotationUtils.mc.field_1724.method_36454() + class_3532.method_15393((float)(yaw - RotationUtils.mc.field_1724.method_36454())), RotationUtils.mc.field_1724.method_36455() + class_3532.method_15393((float)(pitch - RotationUtils.mc.field_1724.method_36455())));
    }

    public static Rotation getRotationToBlock(class_2338 blockPos, class_2350 direction) {
        double centerX = (double)blockPos.method_10263() + 0.5 + (double)direction.method_10148() * 0.5;
        double centerY = (double)blockPos.method_10264() + 0.5 + (double)direction.method_10164() * 0.5;
        double centerZ = (double)blockPos.method_10260() + 0.5 + (double)direction.method_10165() * 0.5;
        double playerX = RotationUtils.mc.field_1724.method_23317();
        double playerY = RotationUtils.mc.field_1724.method_23318() + (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376());
        double playerZ = RotationUtils.mc.field_1724.method_23321();
        double deltaX = centerX - playerX;
        double deltaY = centerY - playerY;
        double deltaZ = centerZ - playerZ;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, distanceXZ)));
        return new Rotation(yaw, pitch);
    }

    public static VecRotation faceBlock(class_2338 blockPos) {
        if (blockPos == null) {
            return null;
        }
        VecRotation vecRotation = null;
        for (double xSearch = 0.1; xSearch < 0.9; xSearch += 0.1) {
            for (double ySearch = 0.1; ySearch < 0.9; ySearch += 0.1) {
                for (double zSearch = 0.1; zSearch < 0.9; zSearch += 0.1) {
                    class_243 eyesPos = new class_243(RotationUtils.mc.field_1724.method_23317(), RotationUtils.mc.field_1724.method_5829().field_1322 + (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376()), RotationUtils.mc.field_1724.method_23321());
                    class_243 posVec = new class_243((double)blockPos.method_10263() + xSearch, (double)blockPos.method_10264() + ySearch, (double)blockPos.method_10260() + zSearch);
                    double dist = eyesPos.method_1022(posVec);
                    double diffX = posVec.field_1352 - eyesPos.field_1352;
                    double diffY = posVec.field_1351 - eyesPos.field_1351;
                    double diffZ = posVec.field_1350 - eyesPos.field_1350;
                    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                    Rotation rotation = new Rotation(class_3532.method_15393((float)((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f)), class_3532.method_15393((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))))));
                    class_243 rotationVector = RotationUtils.getRotationVec(rotation);
                    class_243 vector = eyesPos.method_1031(rotationVector.field_1352 * dist, rotationVector.field_1351 * dist, rotationVector.field_1350 * dist);
                    class_3965 obj = RotationUtils.mc.field_1687.method_17742(new class_3959(eyesPos, vector, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)RotationUtils.mc.field_1724));
                    if (obj.method_17783() != class_239.class_240.field_1332) continue;
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

    public static class_243 getRotationVec(Rotation rot) {
        return RotationUtils.getRotationVec(rot.yaw, rot.pitch);
    }

    public static class_243 getRotationVec(float yaw, float pitch) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = class_3532.method_15362((float)g);
        float i = class_3532.method_15374((float)g);
        float j = class_3532.method_15362((float)f);
        float k = class_3532.method_15374((float)f);
        return new class_243((double)(i * j), (double)(-k), (double)(h * j));
    }

    public static Rotation getRotationBlock(class_2338 pos, float predict) {
        return RotationUtils.getRotationByVector(RotationUtils.mc.field_1724.method_33571(), new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5), false);
    }

    public static Rotation getRotationBlock(class_2338 pos, class_2350 direction, float predict) {
        return RotationUtils.getRotationByVector(RotationUtils.mc.field_1724.method_33571(), new class_243((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 0.5, (double)pos.method_10260() + 0.5).method_1019(class_243.method_24954((class_2382)direction.method_62675()).method_1021(0.5)), false);
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
        class_2828 wrapper;
        class_2596<?> packet = event.packet;
        if (packet instanceof class_2828) {
            wrapper = (class_2828)packet;
            if (wrapper.field_12888) {
                serverRotation = new Rotation(wrapper.method_12271(RotationUtils.mc.field_1724.method_36454()), wrapper.method_12270(RotationUtils.mc.field_1724.method_36455()));
            }
        }
        if (packet instanceof class_2708) {
            class_2708 correction = (class_2708)packet;
            serverRotation = new Rotation(correction.comp_3228().comp_3150(), correction.comp_3228().comp_3151());
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

    public static void setHeadYaw(class_10055 state, float delta) {
        RotationUtils.setBodyYaw(state, delta);
    }

    public static void setBodyYaw(class_10055 state, float delta) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        float yaw = class_3532.method_17821((float)delta, (float)RotationUtils.lastRotation.yaw, (float)RotationUtils.rotation.yaw);
        state.field_53446 = RotationUtils.clampBodyYaw((class_1309)RotationUtils.mc.field_1724, yaw, delta);
        state.field_53447 = class_3532.method_15393((float)(yaw - state.field_53446));
    }

    public static void setPitch(class_10055 state, float delta) {
        if (rotation == null || lastRotation == null) {
            return;
        }
        state.field_53448 = class_3532.method_17821((float)delta, (float)RotationUtils.lastRotation.pitch, (float)RotationUtils.rotation.pitch);
    }

    private static float clampBodyYaw(class_1309 entity, float degrees, float tickDelta) {
        class_1297 vehicle = entity.method_5854();
        if (vehicle instanceof class_1309) {
            class_1309 living = (class_1309)vehicle;
            float lerped = class_3532.method_17821((float)tickDelta, (float)living.field_6220, (float)living.field_6283);
            float clamped = class_3532.method_15363((float)class_3532.method_15393((float)(degrees - lerped)), (float)-85.0f, (float)85.0f);
            lerped = degrees - clamped;
            if (Math.abs(clamped) > 50.0f) {
                lerped += clamped * 0.2f;
            }
            return lerped;
        }
        return class_3532.method_17821((float)tickDelta, (float)entity.field_6220, (float)entity.field_6283);
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

    public static Rotation getPlayerRotations(class_1297 entity) {
        double distanceToEnt = RotationUtils.mc.field_1724.method_5739(entity);
        double predictX = entity.method_23317() + (entity.method_23317() - entity.field_6038) * (distanceToEnt * 0.8);
        double predictZ = entity.method_23321() + (entity.method_23321() - entity.field_5989) * (distanceToEnt * 0.8);
        double x = predictX - RotationUtils.mc.field_1724.method_23317();
        double z = predictZ - RotationUtils.mc.field_1724.method_23321();
        double h = entity.method_23318() + 1.0 - (RotationUtils.mc.field_1724.method_23318() + (double)RotationUtils.mc.field_1724.method_18381(RotationUtils.mc.field_1724.method_18376()));
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

    public static Rotation calculate(class_1297 entity, double range, double wallRange, float predict, float predictPlayer) {
        if (RotationUtils.mc.field_1724 == null) {
            return null;
        }
        class_243 entityPos = entity.method_33571();
        Rotation normalRotations = RotationUtils.getRotationByVector(RotationUtils.mc.field_1724.method_33571(), new class_243(entityPos.field_1352, MathUtils.clamp(RotationUtils.mc.field_1724.method_33571().field_1351, entity.method_5829().field_1322, entity.method_5829().field_1325), entityPos.field_1350), false);
        class_3966 normalResult = RayCastUtils.raytraceEntity(range, normalRotations);
        if (normalResult != null && normalResult.method_17783() == class_239.class_240.field_1331) {
            return normalRotations;
        }
        return RotationUtils.calculateSimple(entity, range, wallRange, predict, predictPlayer);
    }

    public static Rotation calculate(class_243 from, class_243 to) {
        class_243 diff = to.method_1020(from);
        double horizontalDistance = class_3532.method_39241((double)diff.field_1352, (double)diff.field_1350);
        float yaw = (float)(class_3532.method_15349((double)diff.field_1350, (double)diff.field_1352) * 57.29577951308232) - 90.0f;
        float pitch = (float)(-(class_3532.method_15349((double)diff.field_1351, (double)horizontalDistance) * 57.29577951308232));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculate(class_243 to) {
        class_310 client = class_310.method_1551();
        class_746 player = client.field_1724;
        if (player == null) {
            return new Rotation(0.0f, 0.0f);
        }
        class_243 from = new class_243(player.method_23317(), player.method_23320(), player.method_23321());
        return RotationUtils.calculate(from, to);
    }

    public static Rotation calculate(class_243 position, class_2350 direction) {
        double x = position.field_1352 + 0.5;
        double y = position.field_1351 + 0.5;
        double z = position.field_1350 + 0.5;
        class_2382 offset = direction.method_62675();
        return RotationUtils.calculate(new class_243(x += (double)offset.method_10263() * 0.5, y += (double)offset.method_10264() * 0.5, z += (double)offset.method_10260() * 0.5));
    }

    public static float getAngleDifference(float a, float b) {
        return ((a - b) % 360.0f + 540.0f) % 360.0f - 180.0f;
    }

    public static Rotation calculateSimple(class_1297 entity, double range, double wallRange, float predict, float predictPlayer) {
        class_238 aabb = entity.method_5829();
        if (predict != 0.0f) {
            aabb = aabb.method_989((entity.method_23317() - entity.field_6038) * (double)predict, (entity.method_23318() - entity.field_5971) * (double)predict, (entity.method_23321() - entity.field_5989) * (double)predict);
        }
        class_243 eyePos = null;
        if (RotationUtils.mc.field_1724 != null) {
            eyePos = RotationUtils.mc.field_1724.method_33571();
        }
        class_243 nearest = null;
        if (eyePos != null) {
            nearest = new class_243(MathUtils.clamp(eyePos.field_1352, aabb.field_1323, aabb.field_1320), MathUtils.clamp(eyePos.field_1351, aabb.field_1322, aabb.field_1325), MathUtils.clamp(eyePos.field_1350, aabb.field_1321, aabb.field_1324));
        }
        Rotation rotation = null;
        if (nearest != null) {
            rotation = RotationUtils.getRotationByVector(RotationUtils.mc.field_1724.method_33571(), nearest, false);
        }
        if (nearest != null && nearest.method_1020(eyePos).method_1027() <= wallRange * wallRange) {
            return rotation;
        }
        class_3966 result = RayCastUtils.raytraceEntity(range, rotation);
        double maxRange = Math.max(wallRange, range);
        if (result != null && result.method_17782() == entity && result.method_17784().method_1020(eyePos).method_1027() <= maxRange * maxRange) {
            return rotation;
        }
        return null;
    }

    public static Rotation getRotationTo(class_243 start, class_243 end) {
        double diffX = end.method_10216() - start.field_1352;
        double diffY = end.method_10214() - start.field_1351;
        double diffZ = end.method_10215() - start.field_1350;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new Rotation(yaw, pitch);
    }

    public static Rotation getRotationByVector(class_243 eyePos, class_243 vec, boolean noise) {
        class_243 diff = vec.method_1020(eyePos);
        float yaw = class_3532.method_15393((float)((float)Math.toDegrees(Math.atan2(diff.field_1350, diff.field_1352)) - 90.0f));
        float pitch = class_3532.method_15393((float)((float)(-Math.toDegrees(Math.atan2(diff.field_1351, Math.sqrt(diff.field_1352 * diff.field_1352 + diff.field_1350 * diff.field_1350))))));
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

    public static double nearestRotation(class_238 bb) {
        class_243 eyes = RotationUtils.mc.field_1724.method_33571();
        class_243 vecRotation3d = null;
        for (double xSearch = 0.0; xSearch <= 1.0; xSearch += 0.05) {
            for (double ySearch = 0.0; ySearch < 1.0; ySearch += 0.05) {
                for (double zSearch = 0.0; zSearch <= 1.0; zSearch += 0.05) {
                    class_243 vec3 = new class_243(bb.field_1323 + (bb.field_1320 - bb.field_1323) * xSearch, bb.field_1322 + (bb.field_1325 - bb.field_1322) * ySearch, bb.field_1321 + (bb.field_1324 - bb.field_1321) * zSearch);
                    double vecDist = eyes.method_1025(vec3);
                    if (vecRotation3d != null && !(eyes.method_1025(vecRotation3d) > vecDist)) continue;
                    vecRotation3d = vec3;
                }
            }
        }
        return vecRotation3d.method_1022(eyes);
    }

    public static boolean inFoV(class_1297 entity, float fov) {
        return (Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f > 180.0f ? 360.0f - Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f : Math.abs(RotationUtils.getRotations(entity)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f) <= fov;
    }

    public static float getYawToEntity(class_1297 e) {
        return Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f > 180.0f ? 360.0f - Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f : Math.abs(RotationUtils.getRotations(e)[0] - RotationUtils.mc.field_1724.method_36454()) % 360.0f;
    }

    public static class_243 getVFR(Rotation rotation) {
        float yawCos = (float)Math.cos(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = (float)Math.sin(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = (float)(-Math.cos(-rotation.getPitch() * ((float)Math.PI / 180)));
        float pitchSin = (float)Math.sin(-rotation.getPitch() * ((float)Math.PI / 180));
        return new class_243((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
    }

    public static class_243 getVectorForRotation(Rotation rotation) {
        float yawCos = (float)Math.cos(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = (float)Math.sin(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = (float)(-Math.cos(-rotation.getPitch() * ((float)Math.PI / 180)));
        float pitchSin = (float)Math.sin(-rotation.getPitch() * ((float)Math.PI / 180));
        return new class_243((double)(yawSin * pitchCos), (double)pitchSin, (double)(yawCos * pitchCos));
    }

    public static Rotation calculate(Vector3d vector3d, class_2350 enumFacing) {
        return RotationUtils.calculate(new Vector3d(vector3d.getX() + 0.5 + (double)enumFacing.method_10148() * 0.5, vector3d.getY() + 0.5 + (double)enumFacing.method_10164() * 0.5, vector3d.getZ() + 0.5 + (double)enumFacing.method_10165() * 0.5));
    }

    public static Rotation calculate(Vector3d vector3d) {
        return RotationUtils.calculate(RotationUtils.mc.field_1724.method_19538().method_1031(0.0, (double)RotationUtils.getEyeHeight(), 0.0), vector3d);
    }

    public static Rotation calculate(class_243 vector3d, Vector3d vector3d2) {
        Vector3d subtract = vector3d2.subtract(new Vector3d(vector3d.field_1352, vector3d.field_1351, vector3d.field_1350));
        return new Rotation((float)(class_3532.method_15349((double)subtract.getZ(), (double)subtract.getX()) * 57.2957763671875) - 90.0f, (float)(-(class_3532.method_15349((double)subtract.getY(), (double)Math.hypot(subtract.getX(), subtract.getZ())) * 57.2957763671875)));
    }

    public static float getEyeHeight() {
        float f = 1.62f;
        if (RotationUtils.mc.field_1724.method_6113()) {
            f = 0.2f;
        }
        if (RotationUtils.mc.field_1724.method_5715()) {
            f -= 0.08f;
        }
        return f;
    }

    public static Rotation getRandomRotationTo(class_2338 blockPos, class_2350 facing) {
        class_746 player = RotationUtils.mc.field_1724;
        if (blockPos == null || facing == null || player == null) {
            return new Rotation(RotationUtils.mc.field_1724.method_36454(), RotationUtils.mc.field_1724.method_36455());
        }
        class_243 playerEyePos = player.method_33571();
        class_243 randomTargetPoint = RotationUtils.getRandomPointOnFace(blockPos, facing);
        return RotationUtils.calculateRotationFromPoints(playerEyePos, randomTargetPoint);
    }

    private static Rotation calculateRotationFromPoints(class_243 start, class_243 target) {
        class_243 d = target.method_1020(start);
        double dx = d.field_1352;
        double dy = d.field_1351;
        double dz = d.field_1350;
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float)Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new Rotation(yaw, pitch);
    }

    private static boolean nearly(double a, double b) {
        return Math.abs(a - b) <= 1.0E-7;
    }

    private static class_243 getRandomPointOnFace(class_2338 pos, class_2350 face) {
        double area;
        class_3726 ctx;
        class_1937 world = RotationUtils.mc.field_1724.method_37908();
        class_2680 state = world.method_8320(pos);
        class_265 shape = state.method_26172((class_1922)world, pos, ctx = class_3726.method_16195((class_1297)RotationUtils.mc.field_1724));
        if (shape.method_1110()) {
            shape = state.method_26194((class_1922)world, pos, ctx);
        }
        if (shape.method_1110()) {
            shape = class_259.method_1077();
        }
        ArrayList<class_238> boxes = new ArrayList<>(shape.method_1090());
        double sMinX = Double.POSITIVE_INFINITY;
        double sMaxX = Double.NEGATIVE_INFINITY;
        double sMinY = Double.POSITIVE_INFINITY;
        double sMaxY = Double.NEGATIVE_INFINITY;
        double sMinZ = Double.POSITIVE_INFINITY;
        double sMaxZ = Double.NEGATIVE_INFINITY;
        for (Object b : boxes) {
            if (((class_238)b).field_1323 < sMinX) {
                sMinX = ((class_238)b).field_1323;
            }
            if (((class_238)b).field_1320 > sMaxX) {
                sMaxX = ((class_238)b).field_1320;
            }
            if (((class_238)b).field_1322 < sMinY) {
                sMinY = ((class_238)b).field_1322;
            }
            if (((class_238)b).field_1325 > sMaxY) {
                sMaxY = ((class_238)b).field_1325;
            }
            if (((class_238)b).field_1321 < sMinZ) {
                sMinZ = ((class_238)b).field_1321;
            }
            if (!(((class_238)b).field_1324 > sMaxZ)) continue;
            sMaxZ = ((class_238)b).field_1324;
        }
        record FaceSlice(class_238 box, double area) {
        }
        ArrayList<FaceSlice> candidates = new ArrayList<FaceSlice>();
        block21: for (class_238 b : boxes) {
            switch (face) {
                case field_11034: {
                    if (!RotationUtils.nearly(b.field_1320, sMaxX)) break;
                    area = (b.field_1325 - b.field_1322) * (b.field_1324 - b.field_1321);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case field_11039: {
                    if (!RotationUtils.nearly(b.field_1323, sMinX)) break;
                    area = (b.field_1325 - b.field_1322) * (b.field_1324 - b.field_1321);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case field_11036: {
                    if (!RotationUtils.nearly(b.field_1325, sMaxY)) break;
                    area = (b.field_1320 - b.field_1323) * (b.field_1324 - b.field_1321);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case field_11033: {
                    if (!RotationUtils.nearly(b.field_1322, sMinY)) break;
                    area = (b.field_1320 - b.field_1323) * (b.field_1324 - b.field_1321);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case field_11035: {
                    if (!RotationUtils.nearly(b.field_1324, sMaxZ)) break;
                    area = (b.field_1320 - b.field_1323) * (b.field_1325 - b.field_1322);
                    if (!(area > 0.0)) continue block21;
                    candidates.add(new FaceSlice(b, area));
                    break;
                }
                case field_11043: {
                    if (!RotationUtils.nearly(b.field_1321, sMinZ) || !((area = (b.field_1320 - b.field_1323) * (b.field_1325 - b.field_1322)) > 0.0)) break;
                    candidates.add(new FaceSlice(b, area));
                }
            }
        }
        if (candidates.isEmpty()) {
            for (class_238 b : boxes) {
                switch (face.method_10166()) {
                    case field_11048: {
                        area = (b.field_1325 - b.field_1322) * (b.field_1324 - b.field_1321);
                        break;
                    }
                    case field_11052: {
                        area = (b.field_1320 - b.field_1323) * (b.field_1324 - b.field_1321);
                        break;
                    }
                    default: {
                        area = (b.field_1320 - b.field_1323) * (b.field_1325 - b.field_1322);
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
        class_238 chosen = null;
        for (FaceSlice fs : candidates) {
            if (!((pick -= fs.area) <= 0.0)) continue;
            chosen = fs.box;
            break;
        }
        if (chosen == null) {
            chosen = ((FaceSlice)candidates.get((int)(candidates.size() - 1))).box;
        }
        double x = pos.method_10263();
        double y = pos.method_10264();
        double z = pos.method_10260();
        switch (face) {
            case field_11034: {
                double ry = chosen.field_1322 + random.nextDouble() * (chosen.field_1325 - chosen.field_1322);
                double rz = chosen.field_1321 + random.nextDouble() * (chosen.field_1324 - chosen.field_1321);
                x += chosen.field_1320;
                y += ry;
                z += rz;
                break;
            }
            case field_11039: {
                double ry = chosen.field_1322 + random.nextDouble() * (chosen.field_1325 - chosen.field_1322);
                double rz = chosen.field_1321 + random.nextDouble() * (chosen.field_1324 - chosen.field_1321);
                x += chosen.field_1323;
                y += ry;
                z += rz;
                break;
            }
            case field_11036: {
                double rx = chosen.field_1323 + random.nextDouble() * (chosen.field_1320 - chosen.field_1323);
                double rz = chosen.field_1321 + random.nextDouble() * (chosen.field_1324 - chosen.field_1321);
                x += rx;
                y += chosen.field_1325;
                z += rz;
                break;
            }
            case field_11033: {
                double rx = chosen.field_1323 + random.nextDouble() * (chosen.field_1320 - chosen.field_1323);
                double rz = chosen.field_1321 + random.nextDouble() * (chosen.field_1324 - chosen.field_1321);
                x += rx;
                y += chosen.field_1322;
                z += rz;
                break;
            }
            case field_11035: {
                double rx = chosen.field_1323 + random.nextDouble() * (chosen.field_1320 - chosen.field_1323);
                double ry = chosen.field_1322 + random.nextDouble() * (chosen.field_1325 - chosen.field_1322);
                x += rx;
                y += ry;
                z += chosen.field_1324;
                break;
            }
            case field_11043: {
                double rx = chosen.field_1323 + random.nextDouble() * (chosen.field_1320 - chosen.field_1323);
                double ry = chosen.field_1322 + random.nextDouble() * (chosen.field_1325 - chosen.field_1322);
                x += rx;
                y += ry;
                z += chosen.field_1321;
            }
        }
        return new class_243(x, y, z);
    }

    public static Rotation getClosestToBlockFace(class_2338 pos, class_2350 face, float lastYaw, float lastPitch) {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            return new Rotation(lastYaw, 80.5f);
        }
        Rotation currentRot = new Rotation(lastYaw, lastPitch);
        if (ClientRayTraceUtil.didHitBlockFace(currentRot, pos, face, true)) {
            return currentRot;
        }
        class_2680 state = player.method_37908().method_8320(pos);
        class_265 shape = state.method_26218((class_1922)player.method_37908(), pos);
        if (shape.method_1110()) {
            shape = class_259.method_1077();
        }
        List<class_238> boxes = shape.method_1090();
        Rotation bestRot = null;
        Rotation closeRot = null;
        double minDiff = Double.MAX_VALUE;
        class_243 eyePos = ClientRayTraceUtil.eyePos;
        for (class_238 box : boxes) {
            class_238 worldBox = box.method_996(pos);
            class_243 optimalPoint = null;
            ArrayList<class_243> hitPoints = new ArrayList<class_243>();
            List<class_243> optimalPoints = RotationUtils.getOptimalPointOnFace(worldBox, face, eyePos, lastYaw);
            double minYawDiff = Double.MAX_VALUE;
            for (class_243 point : optimalPoints) {
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
            class_243 projectedPoint = RotationUtils.getProjectedPointOnFace(worldBox, face, eyePos, lastYaw, RandomUtils.generateRandomFloat(0.05f, 0.1f));
            closeRot = RotationUtils.calculate(eyePos, projectedPoint);
            if (RotationUtils.tryHit(projectedPoint, pos, face, eyePos)) {
                hitPoints.add(projectedPoint);
            }
            if (hitPoints.isEmpty()) continue;
            for (class_243 hitPoint : hitPoints) {
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

    private static boolean tryHit(class_243 target, class_2338 pos, class_2350 face, class_243 eyePos) {
        Rotation rot = RotationUtils.calculate(eyePos, target);
        return ClientRayTraceUtil.didHitBlockFace(rot, pos, face, true);
    }

    private static List<class_243> getOptimalPointOnFace(class_238 box, class_2350 face, class_243 eyePos, float lastYaw) {
        ArrayList<class_243> points = new ArrayList<class_243>();
        double edgeInset = RandomUtils.generateRandomFloat(0.1f, 0.15f);
        if (face.method_10166() == class_2350.class_2351.field_11052) {
            double planeY = face == class_2350.field_11036 ? box.field_1325 : box.field_1322;
            double clampedX = class_3532.method_15350((double)eyePos.field_1352, (double)(box.field_1323 + edgeInset), (double)(box.field_1320 - edgeInset));
            double clampedZ = class_3532.method_15350((double)eyePos.field_1350, (double)(box.field_1321 + edgeInset), (double)(box.field_1324 - edgeInset));
            points.add(new class_243(clampedX, planeY, clampedZ));
            double centerX = (box.field_1323 + box.field_1320) * 0.5;
            double centerZ = (box.field_1321 + box.field_1324) * 0.5;
            points.add(new class_243(centerX, planeY, centerZ));
        } else {
            double centerX = face.method_10166() == class_2350.class_2351.field_11048 ? (face == class_2350.field_11034 ? box.field_1320 : box.field_1323) : (box.field_1323 + box.field_1320) * 0.5;
            double centerY = (box.field_1322 + box.field_1325) * 0.5;
            double centerZ = face.method_10166() == class_2350.class_2351.field_11051 ? (face == class_2350.field_11035 ? box.field_1324 : box.field_1321) : (box.field_1321 + box.field_1324) * 0.5;
            points.add(new class_243(centerX, centerY, centerZ));
            double yawRad = Math.toRadians(lastYaw);
            double dirX = -Math.sin(yawRad);
            double dirZ = Math.cos(yawRad);
            double bestX = centerX;
            double bestZ = centerZ;
            if (face.method_10166() == class_2350.class_2351.field_11051) {
                double planeZ;
                double t;
                planeZ = face == class_2350.field_11035 ? box.field_1324 : box.field_1321;
                if (Math.abs(dirZ) > 1.0E-4 && (t = (planeZ - eyePos.field_1350) / dirZ) > 0.0) {
                    double intersectX = eyePos.field_1352 + t * dirX;
                    bestX = class_3532.method_15350((double)intersectX, (double)(box.field_1323 + edgeInset), (double)(box.field_1320 - edgeInset));
                    bestZ = planeZ;
                }
            } else if (face.method_10166() == class_2350.class_2351.field_11048) {
                double planeX;
                double t;
                planeX = face == class_2350.field_11034 ? box.field_1320 : box.field_1323;
                if (Math.abs(dirX) > 1.0E-4 && (t = (planeX - eyePos.field_1352) / dirX) > 0.0) {
                    double intersectZ = eyePos.field_1350 + t * dirZ;
                    bestZ = class_3532.method_15350((double)intersectZ, (double)(box.field_1321 + edgeInset), (double)(box.field_1324 - edgeInset));
                    bestX = planeX;
                }
            }
            for (double y = box.field_1322 + 0.1; y < box.field_1325; y += 0.4) {
                points.add(new class_243(bestX, y, bestZ));
            }
            points.add(new class_243(bestX, box.field_1325 - 0.1, bestZ));
            points.add(new class_243(bestX, box.field_1322 + 0.1, bestZ));
        }
        return points;
    }

    private static class_243 getProjectedPointOnFace(class_238 box, class_2350 face, class_243 eye, float lastYaw, double epsilon) {
        double t;
        class_243 u;
        double cx = face.method_10166() == class_2350.class_2351.field_11048 ? (face == class_2350.field_11034 ? box.field_1320 : box.field_1323) : (box.field_1323 + box.field_1320) * 0.5;
        double cy = face.method_10166() == class_2350.class_2351.field_11052 ? (face == class_2350.field_11036 ? box.field_1325 : box.field_1322) : (box.field_1322 + box.field_1325) * 0.5;
        double cz = face.method_10166() == class_2350.class_2351.field_11051 ? (face == class_2350.field_11035 ? box.field_1324 : box.field_1321) : (box.field_1321 + box.field_1324) * 0.5;
        class_243 center = new class_243(cx, cy, cz);
        class_243 v = switch (face.method_10166()) {
            case class_2350.class_2351.field_11048 -> {
                u = new class_243(0.0, 1.0, 0.0);
                yield new class_243(0.0, 0.0, 1.0);
            }
            case class_2350.class_2351.field_11052 -> {
                u = new class_243(1.0, 0.0, 0.0);
                yield new class_243(0.0, 0.0, 1.0);
            }
            default -> {
                u = new class_243(1.0, 0.0, 0.0);
                yield new class_243(0.0, 1.0, 0.0);
            }
        };
        double halfU = (u.field_1352 != 0.0 ? box.field_1320 - box.field_1323 : (u.field_1351 != 0.0 ? box.field_1325 - box.field_1322 : box.field_1324 - box.field_1321)) * 0.5;
        double halfV = (v.field_1352 != 0.0 ? box.field_1320 - box.field_1323 : (v.field_1351 != 0.0 ? box.field_1325 - box.field_1322 : box.field_1324 - box.field_1321)) * 0.5;
        double rad = Math.toRadians(lastYaw + 90.0f);
        class_243 dir = new class_243(Math.cos(rad), 0.0, Math.sin(rad));
        class_243 normal = new class_243((double)face.method_10148(), (double)face.method_10164(), (double)face.method_10165());
        double denom = normal.method_1026(dir);
        if (Math.abs(denom) > 1.0E-6 && (t = normal.method_1026(center.method_1020(eye)) / denom) > 0.0) {
            class_243 hit = eye.method_1019(dir.method_1021(t));
            double du = hit.method_1020(center).method_1026(u);
            double dv = hit.method_1020(center).method_1026(v);
            if (Math.abs(du) <= halfU && Math.abs(dv) <= halfV) {
                return RotationUtils.adjustInsideFace(center, u, v, du, dv, halfU, halfV, epsilon);
            }
        }
        return RotationUtils.projectToFaceBoundary(eye, dir, center, u, v, halfU, halfV, epsilon);
    }

    private static class_243 projectToFaceBoundary(class_243 eye, class_243 dir, class_243 center, class_243 u, class_243 v, double halfU, double halfV, double epsilon) {
        class_243 proj = eye.method_1019(dir.method_1021(100.0));
        double du = proj.method_1020(center).method_1026(u);
        double dv = proj.method_1020(center).method_1026(v);
        du = Math.max(-halfU, Math.min(halfU, du));
        dv = Math.max(-halfV, Math.min(halfV, dv));
        return RotationUtils.adjustInsideFace(center, u, v, du, dv, halfU, halfV, epsilon);
    }

    private static class_243 adjustInsideFace(class_243 center, class_243 u, class_243 v, double du, double dv, double halfU, double halfV, double epsilon) {
        if (Math.abs(du) >= halfU - 1.0E-4) {
            du = Math.copySign(halfU - epsilon, du);
        }
        if (Math.abs(dv) >= halfV - 1.0E-4) {
            dv = Math.copySign(halfV - epsilon, dv);
        }
        return center.method_1019(u.method_1021(du)).method_1019(v.method_1021(dv));
    }

    private static class_243 getCenterOfFace(class_238 box, class_2350 face) {
        double x = (box.field_1323 + box.field_1320) * 0.5;
        double y = (box.field_1322 + box.field_1325) * 0.5;
        double z = (box.field_1321 + box.field_1324) * 0.5;
        switch (face) {
            case field_11039: {
                x = box.field_1323;
                break;
            }
            case field_11034: {
                x = box.field_1320;
                break;
            }
            case field_11033: {
                y = box.field_1322;
                break;
            }
            case field_11036: {
                y = box.field_1325;
                break;
            }
            case field_11043: {
                z = box.field_1321;
                break;
            }
            case field_11035: {
                z = box.field_1324;
            }
        }
        return new class_243(x, y, z);
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
            if (mc != null && mc.field_1724 != null) {
                serverRotation = new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
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
