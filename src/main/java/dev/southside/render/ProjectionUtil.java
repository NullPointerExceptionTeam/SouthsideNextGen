package dev.southside.render;

import dev.southside.MinecraftAccessor;
import dev.southside.compat.RenderProjection;
import dev.southside.module.impl.player.Blink;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
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
        camera.set((Vector3fc)MinecraftAccessor.mc.gameRenderer.getCamera().getPos().toVector3f());
        cameraNegated.set((Vector3dc)camera);
        cameraNegated.negate();
        windowScale = MinecraftAccessor.mc.getWindow().getScaleFactor();
    }

    public static Vec2f entityScreenPos(Entity entity, float partialTicks) {
        Vec3d pos = entity.getLerpedPos(partialTicks);
        double width = ((double)entity.getWidth() + 0.2) / 2.0;
        double height = (double)entity.getHeight() + (entity.isSneaking() ? -0.3 : 0.2) + 0.05;
        return ProjectionUtil.getVec2f(pos.x, pos.y, pos.z, width, height);
    }

    public static Vec2f worldScreenPos(Vec3d pos) {
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        return ProjectionUtil.getVec2f(x, y, z, 0.0, 0.0);
    }

    private static Vec2f getVec2f(double x, double y, double z, double width, double height) {
        Box aabb = new Box(x - width, y, z - width, x + width, y + height, z + width);
        List<Vec3d> vectors = Arrays.asList(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), new Vec3d(aabb.minX, aabb.maxY, aabb.minZ), new Vec3d(aabb.maxX, aabb.minY, aabb.minZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vec3d(aabb.minX, aabb.minY, aabb.maxZ), new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ));
        Vector4d position = null;
        for (Vec3d vector : vectors) {
            vector = ProjectionUtil.project(vector);
            if (!(vector.z >= 0.0) || !(vector.z < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(vector.x, vector.y, vector.x, vector.y);
            }
            position = new Vector4d(Math.min(vector.x, position.x()), Math.min(vector.y, position.y()), Math.max(vector.x, position.z()), Math.max(vector.y, position.w()));
        }
        if (position == null) {
            return null;
        }
        return new Vec2f((float)(position.x() + (position.z() - position.x()) / 2.0), (float)(position.y() - 2.0));
    }

    public static Vec3d project(Vec3d pos) {
        Camera camera = MinecraftAccessor.mc.getEntityRenderDispatcher().camera;
        int displayHeight = MinecraftAccessor.mc.getWindow().getFramebufferHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv((int)2978, (int[])viewport);
        Vector3f target = new Vector3f();
        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;
        Vector4f transformedCoordinates = new Vector4f((float)deltaX, (float)deltaY, (float)deltaZ, 1.0f).mul((Matrix4fc)world);
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)projection);
        Matrix4f matrixModel = new Matrix4f((Matrix4fc)model);
        RenderProjection.projectVisible(matrixProj.mul((Matrix4fc)matrixModel), transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d((double)target.x / windowScale, (double)((float)displayHeight - target.y) / windowScale, (double)target.z);
    }

    public static boolean predictBox(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, double motionSlowdown, double size, double gravity, List<Vec3d> pos, float boxWidth, float boxHeight) {
        BlockHitResult landingPosition = null;
        boolean hasLanded = false;
        int ticks = 0;
        int ticksReleased = 0;
        int posCount = pos.size();
        while (!hasLanded && posY > -60.0 && ticks < pos.size()) {
            Vec3d posBefore = new Vec3d(posX, posY, posZ);
            Vec3d posAfter = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
            RaycastContext raycastContext = new RaycastContext(posBefore, posAfter, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)MinecraftAccessor.mc.player);
            if (MinecraftAccessor.mc.world != null) {
                landingPosition = MinecraftAccessor.mc.world.raycast(raycastContext);
            }
            posBefore = new Vec3d(posX, posY, posZ);
            posAfter = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
            if (landingPosition != null) {
                return false;
            }
            Box arrowBox = new Box(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
            Vec3d vec = pos.get(ticksReleased);
            if (arrowBox.intersects(new Box(vec.x - (double)boxWidth, vec.y - (double)(boxHeight / 2.0f), vec.z - (double)boxWidth, vec.x + (double)boxWidth, vec.y + (double)boxHeight, vec.z + (double)boxWidth))) {
                return true;
            }
            BlockPos var35 = new BlockPos((int)(posX += motionX), (int)(posY += motionY), (int)(posZ += motionZ));
            Block var36 = MinecraftAccessor.mc.world.getBlockState(var35).getBlock();
            if (var36.getDefaultState().isLiquid()) {
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

