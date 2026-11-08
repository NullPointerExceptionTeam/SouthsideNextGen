package cc.polyfrost.oneconfig.gui.animations;

import cc.polyfrost.oneconfig.utils.color.ColorPalette;

public class ColorAnimation {
   private ColorPalette palette;
   private final int duration;
   private int prevState = 0;
   private Animation redAnimation;
   private Animation greenAnimation;
   private Animation blueAnimation;
   private Animation alphaAnimation;

   public ColorAnimation(ColorPalette palette, int duration) {
      this.palette = palette;
      this.duration = duration;
      this.redAnimation = new DummyAnimation(palette.getNormalColorf()[0]);
      this.greenAnimation = new DummyAnimation(palette.getNormalColorf()[1]);
      this.blueAnimation = new DummyAnimation(palette.getNormalColorf()[2]);
      this.alphaAnimation = new DummyAnimation(palette.getNormalColorf()[3]);
   }

   public ColorAnimation(ColorPalette palette) {
      this(palette, 100);
   }

   public int getColor(boolean hovered, boolean pressed) {
      int state = pressed ? 2 : (hovered ? 1 : 0);
      if (state != this.prevState) {
         float[] newColors = pressed ? this.palette.getPressedColorf() : (hovered ? this.palette.getHoveredColorf() : this.palette.getNormalColorf());
         this.redAnimation = new EaseInOutQuad(this.duration, this.redAnimation.get(), newColors[0], false);
         this.greenAnimation = new EaseInOutQuad(this.duration, this.greenAnimation.get(), newColors[1], false);
         this.blueAnimation = new EaseInOutQuad(this.duration, this.blueAnimation.get(), newColors[2], false);
         this.alphaAnimation = new EaseInOutQuad(this.duration, this.alphaAnimation.get(), newColors[3], false);
         this.prevState = state;
      }

      return (int)(this.alphaAnimation.get() * 255.0F) << 24
         | (int)(this.redAnimation.get() * 255.0F) << 16
         | (int)(this.greenAnimation.get() * 255.0F) << 8
         | (int)(this.blueAnimation.get() * 255.0F);
   }

   public float getAlpha() {
      return this.alphaAnimation.get(0.0F);
   }

   public ColorPalette getPalette() {
      return this.palette;
   }

   public void setPalette(ColorPalette palette) {
      if (!this.palette.equals(palette)) {
         this.palette = palette;
         this.prevState = 3;
      }
   }

   public void setColors(float[] colors) {
      this.redAnimation = new DummyAnimation(colors[0]);
      this.greenAnimation = new DummyAnimation(colors[1]);
      this.blueAnimation = new DummyAnimation(colors[2]);
      this.alphaAnimation = new DummyAnimation(colors[3]);
   }
}
