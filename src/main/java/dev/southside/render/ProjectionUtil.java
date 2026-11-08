package dev.southside.render;

import dev.southside.MinecraftAccessor;
import dev.southside.compat.RenderProjection;
import dev.southside.module.impl.player.Blink;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class ProjectionUtil {
    private static final Vector4f vec4 = new Vector4f();
    private static final Vector4f mmMat4 = new Vector4f();
    private static final Vector4f pmMat4 = new Vector4f();
    private static final Vector3d camera = new Vector3d();
    private static final Vector3d cameraNegated = new Vector3d();
    private static Matrix4f world = new Matrix4f();
    private static Matrix4f model = new Matrix4f();
    private static Matrix4f projection = new Matrix4f();
    private static double windowScale;

    public static void updateProjection(Matrix4f world, Matrix4f model, Matrix4f projection) {
        ProjectionUtil.world.set((Matrix4fc)world);
        ProjectionUtil.model.set((Matrix4fc)model);
        ProjectionUtil.projection.set((Matrix4fc)projection);
        camera.set((Vector3fc)MinecraftAccessor.mc.field_1773.method_19418().method_19326().method_46409());
        cameraNegated.set((Vector3dc)camera);
        cameraNegated.negate();
        windowScale = MinecraftAccessor.mc.method_22683().method_4495();
    }

    public static class_241 entityScreenPos(class_1297 entity, float partialTicks) {
        class_243 pos = entity.method_30950(partialTicks);
        double width = ((double)entity.method_17681() + 0.2) / 2.0;
        double height = (double)entity.method_17682() + (entity.method_5715() ? -0.3 : 0.2) + 0.05;
        return ProjectionUtil.getVec2f(pos.field_1352, pos.field_1351, pos.field_1350, width, height);
    }

    public static class_241 worldScreenPos(class_243 pos) {
        double x = pos.field_1352;
        double y = pos.field_1351;
        double z = pos.field_1350;
        return ProjectionUtil.getVec2f(x, y, z, 0.0, 0.0);
    }

    private static class_241 getVec2f(double x, double y, double z, double width, double height) {
        class_238 aabb = new class_238(x - width, y, z - width, x + width, y + height, z + width);
        List<class_243> vectors = Arrays.asList(new class_243(aabb.field_1323, aabb.field_1322, aabb.field_1321), new class_243(aabb.field_1323, aabb.field_1325, aabb.field_1321), new class_243(aabb.field_1320, aabb.field_1322, aabb.field_1321), new class_243(aabb.field_1320, aabb.field_1325, aabb.field_1321), new class_243(aabb.field_1323, aabb.field_1322, aabb.field_1324), new class_243(aabb.field_1323, aabb.field_1325, aabb.field_1324), new class_243(aabb.field_1320, aabb.field_1322, aabb.field_1324), new class_243(aabb.field_1320, aabb.field_1325, aabb.field_1324));
        Vector4d position = null;
        for (class_243 vector : vectors) {
            vector = ProjectionUtil.project(vector);
            if (!(vector.field_1350 >= 0.0) || !(vector.field_1350 < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(vector.field_1352, vector.field_1351, vector.field_1352, vector.field_1351);
            }
            position = new Vector4d(Math.min(vector.field_1352, position.x()), Math.min(vector.field_1351, position.y()), Math.max(vector.field_1352, position.z()), Math.max(vector.field_1351, position.w()));
        }
        if (position == null) {
            return null;
        }
        return new class_241((float)(position.x() + (position.z() - position.x()) / 2.0), (float)(position.y() - 2.0));
    }

    public static class_243 project(class_243 pos) {
        class_4184 camera = MinecraftAccessor.mc.method_1561().field_4686;
        int displayHeight = MinecraftAccessor.mc.method_22683().method_4506();
        int[] viewport = new int[4];
        GL11.glGetIntegerv((int)2978, (int[])viewport);
        Vector3f target = new Vector3f();
        double deltaX = pos.field_1352 - camera.method_19326().field_1352;
        double deltaY = pos.field_1351 - camera.method_19326().field_1351;
        double deltaZ = pos.field_1350 - camera.method_19326().field_1350;
        Vector4f transformedCoordinates = new Vector4f((float)deltaX, (float)deltaY, (float)deltaZ, 1.0f).mul((Matrix4fc)world);
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)projection);
        Matrix4f matrixModel = new Matrix4f((Matrix4fc)model);
        RenderProjection.projectVisible(matrixProj.mul((Matrix4fc)matrixModel), transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new class_243((double)target.x / windowScale, (double)((float)displayHeight - target.y) / windowScale, (double)target.z);
    }

    public static boolean predictBox(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, double motionSlowdown, double size, double gravity, List<class_243> pos, float boxWidth, float boxHeight) {
        class_3965 landingPosition = null;
        boolean hasLanded = false;
        int ticks = 0;
        int ticksReleased = 0;
        int posCount = pos.size();
        while (!hasLanded && posY > -60.0 && ticks < pos.size()) {
            class_243 posBefore = new class_243(posX, posY, posZ);
            class_243 posAfter = new class_243(posX + motionX, posY + motionY, posZ + motionZ);
            class_3959 raycastContext = new class_3959(posBefore, posAfter, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)MinecraftAccessor.mc.field_1724);
            if (MinecraftAccessor.mc.field_1687 != null) {
                landingPosition = MinecraftAccessor.mc.field_1687.method_17742(raycastContext);
            }
            posBefore = new class_243(posX, posY, posZ);
            posAfter = new class_243(posX + motionX, posY + motionY, posZ + motionZ);
            if (landingPosition != null) {
                return false;
            }
            class_238 arrowBox = new class_238(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
            class_243 vec = pos.get(ticksReleased);
            if (arrowBox.method_994(new class_238(vec.field_1352 - (double)boxWidth, vec.field_1351 - (double)(boxHeight / 2.0f), vec.field_1350 - (double)boxWidth, vec.field_1352 + (double)boxWidth, vec.field_1351 + (double)boxHeight, vec.field_1350 + (double)boxWidth))) {
                return true;
            }
            class_2338 var35 = new class_2338((int)(posX += motionX), (int)(posY += motionY), (int)(posZ += motionZ));
            class_2248 var36 = MinecraftAccessor.mc.field_1687.method_8320(var35).method_26204();
            if (var36.method_9564().method_51176()) {
                motionX *= 0.6;
                motionY *= 0.6;
                motionZ *= 0.6;
            } else {
                motionX *= motionSlowdown;
                motionY *= motionSlowdown;
                motionZ *= motionSlowdown;
            }
            motionY -= gravity;
            ticksReleased += Blink.getReleaseSpeedModifier(++ticks, ++posCount - ticksReleased);
        }
        return false;
    }
}

