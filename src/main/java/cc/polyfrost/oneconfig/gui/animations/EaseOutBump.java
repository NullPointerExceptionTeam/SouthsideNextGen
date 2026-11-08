package cc.polyfrost.oneconfig.gui.animations;

public class EaseOutBump extends Animation {
   private static final double CONSTANT_1 = 1.7;
   private static final double CONSTANT_2 = 2.7;

   public EaseOutBump(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      return (float)(1.0 + 2.7 * Math.pow(x - 1.0F, 3.0) + 2.04 * Math.pow(x - 1.0F, 2.0));
   }
}
