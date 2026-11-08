package dev.southside.compat;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class RenderProjection {
  private RenderProjection() {}
  public static Vector3f projectVisible(Matrix4f matrix, float x, float y, float z, int[] viewport, Vector3f dest) {
    float w = matrix.m03()*x + matrix.m13()*y + matrix.m23()*z + matrix.m33();
    float clipZ = matrix.m02()*x + matrix.m12()*y + matrix.m22()*z + matrix.m32();
    if (!Float.isFinite(w) || !Float.isFinite(clipZ) || w <= 0.00001f || clipZ < -w || clipZ >= w || viewport[2] <= 0 || viewport[3] <= 0)
      return dest.set(0, 0, -1);
    matrix.project(x, y, z, viewport, dest);
    if (!Float.isFinite(dest.x) || !Float.isFinite(dest.y) || !Float.isFinite(dest.z)) return dest.set(0, 0, -1);
    return dest;
  }
}
