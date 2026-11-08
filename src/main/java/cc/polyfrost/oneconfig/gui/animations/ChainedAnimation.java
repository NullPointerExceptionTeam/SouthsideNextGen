package cc.polyfrost.oneconfig.gui.animations;

public class ChainedAnimation extends Animation {
   private final Animation[] animations;
   private int currentAnimation = 0;
   private float value;

   public ChainedAnimation(Animation... animations) {
      super(1.0F, 0.0F, 0.0F, false);
      this.animations = animations;
   }

   @Override
   public float get(float deltaTime) {
      if (this.currentAnimation >= this.animations.length) {
         return this.value;
      } else {
         this.value = this.animations[this.currentAnimation].get(deltaTime);
         if (this.animations[this.currentAnimation].isFinished()) {
            this.currentAnimation++;
         }

         return this.value;
      }
   }

   @Override
   public boolean isFinished() {
      return this.currentAnimation >= this.animations.length;
   }

   @Override
   protected float animate(float x) {
      return 0.0F;
   }
}
