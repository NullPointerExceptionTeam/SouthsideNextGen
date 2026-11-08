package cc.polyfrost.oneconfig.gui.animations;

public class EaseOutQuad extends Animation {
   public EaseOutQuad(int duration, float start, float end, boolean reverse) {
      super(duration, start, end, reverse);
   }

   @Override
   protected float animate(float x) {
      return 1.0F - (1.0F - x) * (1.0F - x);
   }
}
