package cc.polyfrost.oneconfig.gui.animations;

public class CubicBezier extends Animation {
   private static final float CubicErrorBound = 0.001F;
   protected final float P0;
   protected final float P1;
   protected final float P2;
   protected final float P3;

   public CubicBezier(float P0, float P1, float P2, float P3, float duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
      this.P0 = P0;
      this.P1 = P1;
      this.P2 = P2;
      this.P3 = P3;
   }

   public CubicBezier(float[] points, float duration, float start, float end, boolean reverse) {
      this(points[0], points[1], points[2], points[3], duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      float start = 0.0F;
      float end = 1.0F;

      while (true) {
         float midpoint = (start + end) / 2.0F;
         float estimate = this.evaluateCubic(this.P0, this.P2, midpoint);
         if (Math.abs(x - estimate) < 0.001F) {
            return this.evaluateCubic(this.P1, this.P3, midpoint);
         }

         if (estimate < x) {
            start = midpoint;
         } else {
            end = midpoint;
         }
      }
   }

   private float evaluateCubic(float a, float b, float m) {
      return 3.0F * a * (1.0F - m) * (1.0F - m) * m + 3.0F * b * (1.0F - m) * m * m + m * m * m;
   }
}
