package cc.polyfrost.oneconfig.config.elements;

import cc.polyfrost.oneconfig.config.options.Option;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.DummyAnimation;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.utils.DescriptionRenderer;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import cc.polyfrost.oneconfig.utils.color.ColorUtil;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;

public abstract class BasicOption {
   public final int size;
   protected final Field field;
   protected Object parent;
   public final String name;
   public final String description;
   public String category;
   public String subcategory;
   private final ColorAnimation nameColorAnimation = new ColorAnimation(new ColorPalette(Colors.WHITE_90, Colors.WHITE, Colors.WHITE_90));
   protected int nameColor = Colors.WHITE_90;
   private final Map<String, Supplier<Boolean>> dependencies = new HashMap<>();
   private final List<Runnable> listeners = new ArrayList<>();
   private final List<Supplier<Boolean>> hideConditions = new ArrayList<>();
   private Animation descriptionAnimation = new DummyAnimation(0.0F);
   private float hoverTime = 0.0F;

   public BasicOption(Field field, Object parent, String name, String description, String category, String subcategory, int size) {
      this.field = field;
      this.parent = parent;
      this.name = name;
      this.description = ColorUtil.stripColor(description);
      this.category = category;
      this.subcategory = subcategory;
      this.size = size;
      if (field != null) {
         field.setAccessible(true);
      }

      if (parent instanceof Option) {
         ((Option)parent).basicOption = this;
      }
   }

   protected void set(Object object) throws IllegalAccessException {
      if (this.field != null) {
         if (!object.equals(this.field.get(this.parent))) {
            this.field.set(this.parent, object);
            this.triggerListeners();
         }
      }
   }

   public void triggerListeners() {
      for (Runnable listener : this.listeners) {
         listener.run();
      }
   }

   public Object get() throws IllegalAccessException {
      return this.field == null ? null : this.field.get(this.parent);
   }

   public abstract int getHeight();

   public abstract void draw(long var1, int var3, int var4, InputHandler var5);

   public void drawLast(long vg, int x, int y, InputHandler inputHandler) {
      this.drawDescription(vg, x, y, inputHandler);
   }

   public void keyTyped(int keyCode, int scanCode, int modifiers) {
   }

   public void charTyped(char chr, int modifiers) {
   }

   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
   }

   public void mouseReleased(double mouseX, double mouseY, int state) {
   }

   public void drawDescription(long vg, int x, int y, InputHandler inputHandler) {
      if (!this.description.trim().isEmpty() || !this.dependencies.isEmpty()) {
         boolean hovered = inputHandler.isAreaHovered(this.getNameX(x), y, NanoVGHelper.INSTANCE.getTextWidth(vg, this.name, 14.0F, Fonts.MEDIUM), 32.0F);
         this.nameColor = this.nameColorAnimation.getColor(hovered, false);
         if (hovered) {
            this.hoverTime = this.hoverTime + GuiUtils.getDeltaTime();
         } else {
            this.hoverTime = 0.0F;
         }

         String warningDescription = null;
         int others = 0;
         List<String> options = new ArrayList<>();
         if (!this.dependencies.isEmpty()) {
            for (Entry<String, Supplier<Boolean>> dependency : this.dependencies.entrySet()) {
               String name = dependency.getKey();
               Supplier<Boolean> supplier = dependency.getValue();
               if (name.startsWith("unknown-")) {
                  others++;
               } else if (!supplier.get()) {
                  options.add(name);
               }
            }
         }

         if (!options.isEmpty() || others != 0) {
            boolean knownOptions = options.isEmpty();
            StringBuilder builder = new StringBuilder("Option disabled by ");

            for (String mod : options) {
               builder.append("\"").append(mod).append("\", ");
            }

            builder = new StringBuilder(builder.substring(0, builder.length() - 2));
            if (others != 0) {
               if (knownOptions) {
                  builder.append(" and ");
               }

               builder.append(others).append(" other option").append(others == 1 ? "" : "s");
            }

            builder.append(".");
            warningDescription = builder.toString();
         }

         if (!this.description.trim().isEmpty()) {
            DescriptionRenderer.drawDescription(
               vg,
               x,
               y,
               this.description,
               warningDescription,
               () -> this.descriptionAnimation,
               a -> this.descriptionAnimation = a,
               null,
               this.shouldDrawDescription(),
               UResolution.getWindowWidth() / 2.0F < inputHandler.mouseX()
                  ? DescriptionRenderer.DescriptionPosition.RIGHT
                  : DescriptionRenderer.DescriptionPosition.LEFT,
               inputHandler
            );
         }
      }
   }

   protected boolean shouldDrawDescription() {
      return this.hoverTime > 350.0F;
   }

   protected float getNameX(int x) {
      return x;
   }

   public boolean isEnabled() {
      for (Supplier<Boolean> dependency : this.dependencies.values()) {
         if (!dependency.get()) {
            return false;
         }
      }

      return true;
   }

   public boolean isHidden() {
      for (Supplier<Boolean> condition : this.hideConditions) {
         if (condition.get()) {
            return true;
         }
      }

      return false;
   }

   public void addDependency(String optionName, Supplier<Boolean> supplier) {
      this.dependencies.put(optionName, supplier);
   }

   @Deprecated
   public void addDependency(Supplier<Boolean> supplier) {
      this.dependencies.put("unknown-" + UUID.randomUUID(), supplier);
   }

   public void addListener(Runnable runnable) {
      this.listeners.add(runnable);
   }

   public void addHideCondition(Supplier<Boolean> supplier) {
      this.hideConditions.add(supplier);
   }

   public Field getField() {
      return this.field;
   }

   public Object getParent() {
      return this.parent;
   }

   public void setParent(Object parent) {
      this.parent = parent;
   }

   public void finishUpAndClose() {
   }
}
