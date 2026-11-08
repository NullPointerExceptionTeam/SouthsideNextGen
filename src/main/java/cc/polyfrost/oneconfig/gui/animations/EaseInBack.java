package cc.polyfrost.oneconfig.gui.animations;

public class EaseInBack extends Animation {
   public EaseInBack(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return c3 * x * x * x - c1 * x * x;
   }
}
