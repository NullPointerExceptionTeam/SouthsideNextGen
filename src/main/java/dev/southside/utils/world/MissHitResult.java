package dev.southside.utils.world;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult.Type;

public class MissHitResult extends HitResult {
   protected MissHitResult(Vec3d pos) {
      super(pos);
   }

   public Type getType() {
      return Type.MISS;
   }
}
