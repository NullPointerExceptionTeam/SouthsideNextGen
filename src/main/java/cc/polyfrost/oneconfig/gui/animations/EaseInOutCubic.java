package cc.polyfrost.oneconfig.gui.animations;

public class EaseInOutCubic extends Animation {
   public EaseInOutCubic(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      return x < 0.5 ? 4.0F * x * x * x : (float)(1.0 - Math.pow(-2.0F * x + 2.0F, 3.0) / 2.0);
   }
}
