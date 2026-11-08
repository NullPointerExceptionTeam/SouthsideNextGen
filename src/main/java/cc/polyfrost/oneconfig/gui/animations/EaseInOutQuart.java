package cc.polyfrost.oneconfig.gui.animations;

public class EaseInOutQuart extends Animation {
   public EaseInOutQuart(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      return x < 0.5 ? 8.0F * x * x * x * x : (float)(1.0 - Math.pow(-2.0F * x + 2.0F, 4.0) / 2.0);
   }
}
