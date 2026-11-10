package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.gui.HudGui;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.color.ChromaJS;
import dev.southside.Southside;
import dev.southside.gui.hud.jello.ActiveMods;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Watermark;
import dev.southside.utils.misc.BezierUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ArrayListHud extends Hud {
   public static final Dropdown mode = new Dropdown("Style", "Southside", "Southside", "SigmaJello", "大香肠");
   public ActiveMods jelloArraylist = null;
   public final Switch noRender = new Switch("No Render Modules", true);
   public final Switch onlyBind = new Switch("Only Bound Modules", false);
   public static final Switch removeShadow = new Switch("Remove Shadow", false);
   public static final Switch showSuffix = new Switch("Show Suffix", true);
   public static final Switch background = new Switch("Background", true);
   public static final Switch round = new Switch("Round Background", true);
   private static final String[] FONT_OPTIONS = new String[]{"Default", "Inter", "Jello-Light", "Vanilla"};
   private static final int FONT_VANILLA_INDEX = 3;
   public static final Dropdown font = new Dropdown("Font", FONT_OPTIONS, "Choose which font will show", FONT_OPTIONS.length, 0);
   public final Dropdown colorValue = new Dropdown(
      "Color", new String[]{"Custom", "Rainbow", "LightRainbow", "Astolfo", "Weird", "Valentine"}, "Choose which color will show in the Arraylist", 5, 1
   );
   public final Color customColor1 = new Color("Custom Color A", new OneColor(java.awt.Color.WHITE));
   public final Color customColor2 = new Color("Custom Color B", new OneColor(java.awt.Color.WHITE));
   private ChromaJS.Scale rainbow = null;
   public final Slider customSpeed = new Slider("Custom Speed", 10.0, 5.0, 20.0);
   private CopyOnWriteArrayList<ArrayListHud.ArrayListModule> enabledModules = new CopyOnWriteArrayList<>();
   private HashMap<Module, ArrayListHud.ArrayListModule> modules = new HashMap<>();
   private float lastScale = 1.0F;
   private float lastHeight = 50.0F;
   private float lastWidth = 50.0F;
   private int lastSize = -1;

   public ArrayListHud(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   public void reloadRainbow() {
      this.rainbow = new ChromaJS.Scale(this.customColor1.getValue().toJavaColor(), this.customColor2.getValue().toJavaColor(), 8);
   }

   public void syncModules() {
      int current = Southside.INSTANCE.getModuleManager().getModules().size();
      if (this.lastSize != current) {
         this.jelloArraylist = new ActiveMods();
         this.lastSize = current;
      }

      float fontSize = 6.0F * this.lastScale;
      if (this.modules.isEmpty()) {
         for (int i = 0; i < Southside.get().getModuleManager().getModules().size(); i++) {
            Module module = Southside.get().getModuleManager().getModules().get(i);
            this.modules.put(module, new ArrayListHud.ArrayListModule(this, module, -1));
         }
      }

      this.enabledModules = new CopyOnWriteArrayList<>();

      for (Module module : Southside.get().getModuleManager().getModules()) {
         if (this.shouldDisplay(module)) {
            this.enabledModules.add(this.modules.get(module));
         }
      }

      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      nanoVGHelper.setupAndDraw(
         true,
         vg -> this.enabledModules
            .sort(
               (o1, o2) -> {
                  String text2 = Southside.get().getModuleManager().formatRaw(o2.module);
                  String text1 = Southside.get().getModuleManager().formatRaw(o1.module);
                  float width2 = useVanillaFont()
                     ? mc.textRenderer.getWidth(text2) * this.lastScale
                     : nanoVGHelper.getTextWidth(vg, text2, fontSize, getSelectedFont());
                  float width1 = useVanillaFont()
                     ? mc.textRenderer.getWidth(text1) * this.lastScale
                     : nanoVGHelper.getTextWidth(vg, text1, fontSize, getSelectedFont());
                  return (int)(10000.0F * width2 - 10000.0F * width1);
               }
            )
      );
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      String var7 = mode.getMode();
      switch (var7) {
         case "Southside":
            if (this.rainbow == null) {
               this.reloadRainbow();
            }

            this.syncModules();
            this.lastScale = scale;
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            AtomicBoolean updatedSize = new AtomicBoolean(false);
            AtomicReference<Float> thisHeight = new AtomicReference<>(0.0F);
            AtomicReference<Float> thisWidth = new AtomicReference<>(0.0F);
            nanoVGHelper.setupAndDraw(true, vg -> {
               float fontSize = 6.0F * scale;
               float moduleY = 0.0F;

               int reverse = switch (this.position.getValue().anchor) {
                  case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> -1;
                  default -> 1;
               };

               int reverseX = switch (this.position.getValue().anchor) {
                  case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> -1;
                  default -> 1;
               };

               Scissor scissor = switch (this.position.getValue().anchor) {
                  case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> scissorHelper.scissor(vg, x, 0.0F, UResolution.getScaledWidth(), UResolution.getScaledHeight());
                  default -> scissorHelper.scissor(vg, 0.0F, 0.0F, x + this.getWidth(scale, example), UResolution.getScaledHeight());
               };

               for (ArrayListHud.ArrayListModule modulexx : this.enabledModules) {
                  modulexx.y.update(reverse * moduleY);
                  modulexx.y.freeze();
                  modulexx.x.update(0.0F);
                  float width = modulexx.draw(vg, x, y, background.getValue(), scale);
                  moduleY += modulexx.getLastHeight();
                  updatedSize.set(true);
                  thisWidth.set(Math.max(thisWidth.get(), width));
               }

               thisHeight.set(moduleY);

               for (ArrayListHud.ArrayListModule modulex : this.modules.values()) {
                  if (!this.shouldDisplay(modulex.module)) {
                     modulex.y.update(0.0F);
                     modulex.x.update(scale * reverseX * 110.0F * 0.3F);
                     modulex.draw(vg, x, y, background.getValue(), scale);
                  }
               }

               scissorHelper.resetScissor(vg, scissor);
            });
            if (useVanillaFont()) {
               for (ArrayListHud.ArrayListModule module : this.enabledModules) {
                  module.drawVanillaText(context);
               }
            }

            if (updatedSize.get()) {
               if (mc.currentScreen instanceof HudGui hudGui && hudGui.isScaling) {
                  return;
               }

               this.lastHeight = thisHeight.get() / scale;
               this.lastWidth = thisWidth.get() / scale;
            }
            break;
         case "SigmaJello":
            if (this.jelloArraylist == null) {
               this.jelloArraylist = new ActiveMods();
            }

            this.jelloArraylist.draw(getSelectedFont());
            break;
         default:
            AtomicInteger posY = Southside.get().getModuleManager().get(Watermark.class).isEnabled() ? new AtomicInteger(22) : new AtomicInteger(2);
            this.enabledModules.forEach(u -> {
               context.drawTextWithShadow(mc.textRenderer, u.module.getDisplayName(), 1, posY.get(), -1);
               posY.addAndGet(9);
            });
      }
   }

   private boolean shouldDisplay(Module module) {
      ArrayList<Integer> keyBinds = module.getKeyBind().getValue().getKeyBinds();
      boolean hasBind = !keyBinds.isEmpty() && keyBinds.get(0) != 0 && keyBinds.get(0) != -1;
      return module.isEnabled()
         && module.isVisible()
         && (module.getCategory() != Category.Render || !this.noRender.getValue())
         && (!this.onlyBind.getValue() || hasBind);
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      return mode != null && !mode.isMode("Southside") && this.jelloArraylist != null ? this.jelloArraylist.getWidth() : this.lastWidth * scale;
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      return mode != null && !mode.isMode("Southside") && this.jelloArraylist != null ? this.jelloArraylist.getHeight() : this.lastHeight * scale;
   }

   public int getRainbow(int speed, int offset, float s) {
      float hue = (float)((System.currentTimeMillis() / 10L * this.customSpeed.getValue().doubleValue() + offset) % speed);
      hue /= speed;
      return java.awt.Color.getHSBColor(hue, s, 1.0F).getRGB();
   }

   public java.awt.Color getGradientOffset(java.awt.Color color1, java.awt.Color color2, double offset) {
      if (offset > 1.0) {
         double left = offset % 1.0;
         long off = (long)offset;
         offset = off % 2L == 0L ? left : 1.0 - left;
      }

      double inverse_percent = 1.0 - offset;
      int redPart = (int)(color1.getRed() * inverse_percent + color2.getRed() * offset);
      int greenPart = (int)(color1.getGreen() * inverse_percent + color2.getGreen() * offset);
      int bluePart = (int)(color1.getBlue() * inverse_percent + color2.getBlue() * offset);
      return new java.awt.Color(redPart, greenPart, bluePart);
   }

   public int getCustomOffset(double offset) {
      if (offset > 1.0) {
         double left = offset % 1.0;
         long off = (long)offset;
         offset = off % 2L == 0L ? left : 1.0 - left;
      }

      return this.rainbow.getColorRGB(offset);
   }

   private static Font getSelectedFont() {
      return switch (font.getValue()) {
         case 0 -> Fonts.WQY;
         case 1 -> Fonts.REGULAR;
         case 2 -> Fonts.JELLOLIGHT;
         case 3 -> Fonts.WQY;
         default -> Fonts.WQY;
      };
   }

   private static boolean useVanillaFont() {
      return font.getValue() == 3;
   }

   public static class ArrayListModule {
      public ArrayListHud parent;
      public Module module;
      private final int color;
      public BezierUtil x = new BezierUtil(3.0F, 0.0F);
      public BezierUtil y = new BezierUtil(3.0F, 0.0F);
      private float lastHeight;
      private float vanillaTextX;
      private float vanillaTextY;
      private float vanillaScale;
      private int vanillaColor;
      private String vanillaText = "";
      private boolean vanillaNeedsDraw = false;

      public ArrayListModule(ArrayListHud parent, Module module, int color) {
         this.parent = parent;
         this.module = module;
         this.color = color;
      }

      public float draw(long vg, float targetX, float targetY, boolean background, float scale) {
         float fontSize = 6.0F * scale;

         int reverseX = switch (this.parent.position.getValue().anchor) {
            case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> -1;
            default -> 1;
         };
         String formatted = Southside.get().getModuleManager().format(this.module);
         String rawText = Southside.get().getModuleManager().formatRaw(this.module);
         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         boolean vanillaFontSelected = ArrayListHud.useVanillaFont();
         float moduleWidth = vanillaFontSelected
            ? ArrayListHud.mc.textRenderer.getWidth(rawText) * scale
            : nanoVGHelper.getTextWidth(vg, rawText, fontSize, ArrayListHud.getSelectedFont());
         float fontHeight = vanillaFontSelected ? 9.0F * scale : nanoVGHelper.getTextHeight(vg, fontSize, ArrayListHud.getSelectedFont());
         float height = fontHeight + 3.0F * scale;
         this.lastHeight = height;
         if (reverseX == 1 && this.x.get() >= 30.000002F * scale) {
            return 0.0F;
         } else if (reverseX == -1 && this.x.get() <= -30.000002F * scale) {
            return 0.0F;
         } else {
            float xPos = targetX + this.x.get() - moduleWidth;
            float yPos = targetY + this.y.get();
            switch (this.parent.position.getValue().anchor) {
               case TOP_LEFT:
               case MIDDLE_LEFT:
                  xPos += moduleWidth + 2.1000001F * scale;
                  break;
               case BOTTOM_LEFT:
                  xPos += moduleWidth + 2.1000001F * scale;
                  yPos += this.parent.getHeight(scale, false) - height;
                  break;
               case TOP_RIGHT:
               case MIDDLE_RIGHT:
               case TOP_CENTER:
               case MIDDLE_CENTER:
                  xPos += this.parent.getWidth(scale, false);
                  break;
               case BOTTOM_RIGHT:
               case BOTTOM_CENTER:
                  xPos += this.parent.getWidth(scale, false);
                  yPos += this.parent.getHeight(scale, false) - height;
            }

            if (background) {
               float rectX = xPos - 2.1000001F * scale;
               float rectWidth = moduleWidth + 2.1000001F * scale;
               int color = new java.awt.Color(32, 32, 32, 100).getRGB();
               int size = this.parent.enabledModules.size();
               float radiusTopLeft = 0.0F;
               float radiusTopRight = 0.0F;
               float radiusBottomRight = 0.0F;
               float radiusBottomLeft = 0.0F;
               if (size > 1) {
                  int index = this.parent.enabledModules.indexOf(this);
                  boolean isFirst = index == 0;
                  boolean isLast = index == size - 1;
                  float nextWidth = 0.0F;
                  if (index < size - 1) {
                     ArrayListHud.ArrayListModule nextModule = this.parent.enabledModules.get(index + 1);
                     nextWidth = nanoVGHelper.getTextWidth(
                           vg, Southside.get().getModuleManager().formatRaw(nextModule.module), fontSize, ArrayListHud.getSelectedFont()
                        )
                        + 2.1000001F * scale;
                  }

                  switch (this.parent.position.getValue().anchor) {
                     case TOP_LEFT:
                        if (isFirst) {
                           radiusTopRight = 3.0F;
                           radiusTopLeft = 3.0F;
                           radiusBottomRight = Math.min(3.0F, rectWidth - nextWidth);
                        }

                        if (isLast) {
                           radiusBottomRight = 3.0F;
                           radiusBottomLeft = 3.0F;
                        } else if (index < size - 1) {
                           radiusBottomRight = Math.min(3.0F, rectWidth - nextWidth);
                        }
                        break;
                     case MIDDLE_LEFT:
                     case MIDDLE_CENTER:
                     default:
                        radiusBottomRight = 0.0F;
                        radiusBottomLeft = 0.0F;
                        radiusTopRight = 0.0F;
                        radiusTopLeft = 0.0F;
                        break;
                     case BOTTOM_LEFT:
                        if (isFirst) {
                           radiusBottomRight = 3.0F;
                           radiusBottomLeft = 3.0F;
                           radiusTopRight = Math.min(3.0F, rectWidth - nextWidth);
                        }

                        if (isLast) {
                           radiusTopRight = 3.0F;
                           radiusTopLeft = 3.0F;
                        } else if (index < size - 1) {
                           radiusTopRight = Math.min(3.0F, rectWidth - nextWidth);
                        }
                        break;
                     case TOP_RIGHT:
                     case MIDDLE_RIGHT:
                     case TOP_CENTER:
                        if (isFirst) {
                           radiusTopRight = 3.0F;
                           radiusTopLeft = 3.0F;
                           radiusBottomLeft = Math.min(3.0F, rectWidth - nextWidth);
                        }

                        if (isLast) {
                           radiusBottomRight = 3.0F;
                           radiusBottomLeft = 3.0F;
                        } else if (index < size - 1) {
                           radiusBottomLeft = Math.min(3.0F, rectWidth - nextWidth);
                        }
                        break;
                     case BOTTOM_RIGHT:
                     case BOTTOM_CENTER:
                        if (isFirst) {
                           radiusBottomRight = 3.0F;
                           radiusBottomLeft = 3.0F;
                           radiusTopLeft = Math.min(3.0F, rectWidth - nextWidth);
                        }

                        if (isLast) {
                           radiusTopRight = 3.0F;
                           radiusTopLeft = 3.0F;
                        } else if (index < size - 1) {
                           radiusTopLeft = Math.min(3.0F, rectWidth - nextWidth);
                        }
                  }
               } else if (size == 1) {
                  radiusTopLeft = 3.0F;
                  radiusTopRight = 3.0F;
                  radiusBottomLeft = 3.0F;
                  radiusBottomRight = 3.0F;
               }

               if (ArrayListHud.round.getValue()) {
                  nanoVGHelper.drawRoundedRectVaried(
                     vg, rectX, yPos, rectWidth, height, color, radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft
                  );
               } else {
                  nanoVGHelper.drawRect(
                     vg, xPos - 2.1000001F * scale, yPos, moduleWidth + 2.1000001F * scale, height, new java.awt.Color(32, 32, 32, 100).getRGB()
                  );
               }
            }

            float maybeY = yPos + height / 2.0F + 0.3F * scale;

            int var32 = switch (this.parent.colorValue.getValue()) {
               case 0 -> this.parent
                  .getCustomOffset(Math.abs(System.currentTimeMillis() / 100.0 * this.parent.customSpeed.getValue().doubleValue()) / 100.0 + maybeY / 50.0F);
               case 1 -> this.parent.getRainbow(6000, (int)(maybeY * 30.0F), 0.85F);
               case 2 -> this.parent.getRainbow(6000, (int)(maybeY * 30.0F), 0.55F);
               case 3 -> this.parent
                  .getGradientOffset(
                     new java.awt.Color(255, 60, 234),
                     new java.awt.Color(27, 179, 255),
                     Math.abs(System.currentTimeMillis() / 100.0 * this.parent.customSpeed.getValue().doubleValue()) / 100.0 + maybeY / 50.0F
                  )
                  .getRGB();
               case 4 -> this.parent
                  .getGradientOffset(
                     new java.awt.Color(128, 171, 255),
                     new java.awt.Color(160, 72, 255),
                     Math.abs(System.currentTimeMillis() / 100.0 * this.parent.customSpeed.getValue().doubleValue()) / 100.0 + maybeY / 50.0F
                  )
                  .getRGB();
               case 5 -> this.parent
                  .getGradientOffset(
                     new java.awt.Color(255, 129, 202),
                     new java.awt.Color(255, 15, 0),
                     Math.abs(System.currentTimeMillis() / 100.0 * this.parent.customSpeed.getValue().doubleValue()) / 100.0 + maybeY / 50.0F
                  )
                  .getRGB();
               default -> -1;
            };
            if (ArrayListHud.removeShadow.getValue()) {
               if (vanillaFontSelected) {
                  this.cacheVanillaText(xPos - 0.6F * scale, yPos, height, var32, scale, formatted);
               } else {
                  nanoVGHelper.drawTextWithFormatting(vg, formatted, xPos - 0.6F * scale, maybeY, var32, fontSize, ArrayListHud.getSelectedFont());
               }
            } else if (vanillaFontSelected) {
               this.cacheVanillaText(xPos - 0.6F * scale, yPos, height, var32, scale, formatted);
            } else {
               nanoVGHelper.drawTextWithFormatting(vg, formatted, xPos - 0.6F * scale, maybeY, var32, fontSize, ArrayListHud.getSelectedFont());
            }

            return moduleWidth + 2.1000001F * scale;
         }
      }

      private void cacheVanillaText(float textX, float yPos, float height, int colour, float scale, String text) {
         float vanillaFontHeight = 9.0F * scale;
         float textTop = yPos + (height - vanillaFontHeight) / 2.0F;
         this.vanillaTextX = textX;
         this.vanillaTextY = textTop;
         this.vanillaScale = scale;
         this.vanillaColor = colour;
         this.vanillaText = text;
         this.vanillaNeedsDraw = true;
      }

      public void drawVanillaText(DrawContext context) {
         if (this.vanillaNeedsDraw) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(this.vanillaTextX, this.vanillaTextY, 0.0F);
            matrices.scale(this.vanillaScale, this.vanillaScale, 1.0F);
            context.drawText(ArrayListHud.mc.textRenderer, this.vanillaText, 0, 0, this.vanillaColor, !ArrayListHud.removeShadow.getValue());
            matrices.pop();
            this.vanillaNeedsDraw = false;
         }
      }

      public float getLastHeight() {
         return this.lastHeight;
      }

      @Generated
      public int getColor() {
         return this.color;
      }
   }
}
