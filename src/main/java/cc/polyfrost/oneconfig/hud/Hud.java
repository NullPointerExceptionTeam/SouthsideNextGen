package cc.polyfrost.oneconfig.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.options.WrappedValue;
import cc.polyfrost.oneconfig.config.options.impl.Button;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.platform.Platform;
import dev.southside.MinecraftAccessor;
import net.minecraft.class_332;

public abstract class Hud implements MinecraftAccessor {
   public Button resetPosition = new Button("Reset Position", "Reset", "Reset HUD to default position", 1, this::resetPosition);
   public Switch locked = new Switch("Locked", "If the position is locked", 2, false);
   public WrappedValue<Position> position = new WrappedValue<>("HUD Position", new Position(null, 0.0F, 0.0F, 0.0F, 0.0F));
   public Slider scale = new Slider("Scale", 0.2F, 10.0F, 0, "The scale of the HUD", false, 1);
   private transient Config config;
   private int defaultAlignment;
   public float deltaTicks;
   private float defaultScale;
   private boolean loaded = false;
   private Position defaultPosition;
   public Switch showInChat = new Switch("Show in Chat", true);
   public Switch showInDebug = new Switch("Show in F3 (Debug)", false);
   public Switch showInGuis = new Switch("Show in GUIs", true);

   public Hud(float x, float y, int positionAlignment, float scale) {
      this.scale.setValue(scale);
      this.position.setValue(new Position(this, 0.0F, 0.0F, this.getWidth(scale, true), this.getHeight(scale, true)));
      this.position.getValue().anchor = Position.getAnchor(positionAlignment);
      if (!this.loaded) {
         this.defaultPosition = new Position(this, 0.0F, 0.0F, this.getWidth(scale, true), this.getHeight(scale, true));
         this.defaultPosition.setX(x);
         this.defaultPosition.setY(y);
         this.defaultPosition.anchor = Position.getAnchor(positionAlignment);
         this.defaultAlignment = positionAlignment;
         this.defaultScale = scale;
         this.loaded = true;
      }
   }

   public Hud(float x, float y, float scale) {
      this(x, y, 0, scale);
   }

   public Hud(float x, float y) {
      this(x, y, 1.0F);
   }

   public Hud() {
      this(0.0F, 0.0F, 1.0F);
   }

   protected abstract void draw(class_332 var1, UMatrixStack var2, float var3, float var4, float var5, boolean var6);

   protected abstract float getWidth(float var1, boolean var2);

   protected abstract float getHeight(float var1, boolean var2);

   protected void preRender(boolean example) {
   }

   protected void resetPosition() {
      Position pos = this.defaultPosition;
      float width = this.position.getValue().getWidth();
      float height = this.position.getValue().getHeight();
      this.scale.setValue(this.defaultScale);
      this.position.getValue().anchor = pos.anchor;
      float anchorX = this.position.getValue().anchor.x;
      float anchorY = this.position.getValue().anchor.y;
      if (anchorX == 0.0F) {
         this.position.getValue().setX(pos.getX());
      } else if (anchorX == 0.5F) {
         this.position.getValue().setX(pos.getCenterX() - width / 2.0F);
      } else {
         this.position.getValue().setX(pos.getRightX() - width);
      }

      if (anchorY == 0.0F) {
         this.position.getValue().setY(pos.getY());
      } else if (anchorY == 0.5F) {
         this.position.getValue().setY(pos.getCenterY() - height / 2.0F);
      } else {
         this.position.getValue().setY(pos.getBottomY() - height);
      }
   }

   public void drawAll(class_332 context, UMatrixStack matrices, boolean example) {
      if (example || this.shouldShow()) {
         this.preRender(example);
         this.position
            .getValue()
            .setSize(this.getWidth(this.scale.getValue().floatValue(), example), this.getHeight(this.scale.getValue().floatValue(), example));
         this.draw(context, matrices, this.position.getValue().getX(), this.position.getValue().getY(), this.scale.getValue().floatValue(), example);
      }
   }

   protected boolean shouldShow() {
      if (!this.showInGuis.getValue()
         && Platform.getGuiPlatform().getCurrentScreen() != null
         && !(Platform.getGuiPlatform().getCurrentScreen() instanceof OneConfigGui)) {
         return false;
      } else {
         return !this.showInChat.getValue() && Platform.getGuiPlatform().isInChat()
            ? false
            : this.showInDebug.getValue() || !Platform.getGuiPlatform().isInDebug();
      }
   }

   public boolean isEnabled() {
      return this.config == null || this.config.isEnabled();
   }

   public boolean isLocked() {
      return this.locked.getValue() && (this.config == null || this.config.isEnabled());
   }

   public void setConfig(Config config) {
      this.config = config;
   }

   public Config getConfig() {
      return this.config;
   }

   public float getScale() {
      return this.scale.getValue().floatValue();
   }

   public void setScale(float scale, boolean example) {
      this.scale.setValue(scale);
      this.position.getValue().updateSizePosition(this.getWidth(scale, example), this.getHeight(scale, example));
   }
}
