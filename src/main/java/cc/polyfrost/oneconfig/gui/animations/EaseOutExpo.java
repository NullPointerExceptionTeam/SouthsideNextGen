package cc.polyfrost.oneconfig.gui.animations;

public class EaseOutExpo extends Animation {
   public EaseOutExpo(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      return x == 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0F * x);
   }
}
