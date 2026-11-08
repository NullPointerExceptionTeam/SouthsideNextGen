package cc.polyfrost.oneconfig.gui.elements.config;

import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.animations.Animation;
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation;
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad;
import cc.polyfrost.oneconfig.gui.elements.text.TextInputField;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigDropdown extends BasicOption {
   private final String[] originalOptions;
   private final List<String> options;
   private final ColorAnimation backgroundColor = new ColorAnimation(ColorPalette.SECONDARY);
   private final ColorAnimation atomColor = new ColorAnimation(new ColorPalette(Colors.PRIMARY_600, Colors.PRIMARY_500, Colors.PRIMARY_500));
   private final ColorAnimation colorAnimation = new ColorAnimation(new ColorPalette(Colors.GRAY_400_80, Colors.GRAY_400, Colors.GRAY_400), 200);
   private final TextInputField textInputField;
   private boolean opened = false;
   private Scissor inputScissor = null;
   private Animation scrollAnimation = null;
   private float scrollTarget = 0.0F;
   private long scrollTime = 0L;
   private boolean mouseWasDown = false;
   private boolean dragging = false;
   private float yStart = 0.0F;
   private float scroll = 0.0F;
   private InputHandler inputHandler;

   public ConfigDropdown(Field field, Object parent, String name, String description, String category, String subcategory, int size, String[] options) {
      super(field, parent, name, description, category, subcategory, size);
      this.originalOptions = options;
      this.options = new ArrayList<>(options.length);
      this.options.addAll(Arrays.asList(options));
      this.textInputField = new TextInputField(size == 1 ? 256 : 640, 32, "Search...", false, false, SVGs.SEARCH_SM, 12.0F);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler inputHandler) {
      this.inputHandler = inputHandler;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      if (!this.isEnabled()) {
         nanoVGHelper.setAlpha(vg, 0.5F);
      }

      nanoVGHelper.drawText(vg, this.name, x, y + 16, this.nameColor, 14.0F, Fonts.MEDIUM);
      boolean hovered;
      if (this.size == 1) {
         hovered = inputHandler.isAreaHovered(x + 224, y, 256.0F, 32.0F) && this.isEnabled();
      } else {
         hovered = inputHandler.isAreaHovered(x + 352, y, 640.0F, 32.0F) && this.isEnabled();
      }

      if (hovered && inputHandler.isClicked()
         || this.opened
            && inputHandler.isClicked(!this.dragging)
            && (
               this.size == 1 && !inputHandler.isAreaHovered(x + 224, y + 40, 256.0F, this.options.size() * 32, true)
                  || this.size == 2 && !inputHandler.isAreaHovered(x + 352, y + 40, 640.0F, this.options.size() * 32, true)
            )) {
         this.opened = !this.opened;
         if (!this.opened) {
            inputHandler.unblockDWheel();
            inputHandler.stopBlockingInput();
            if (this.inputScissor != null) {
               inputHandler.stopBlock(this.inputScissor);
            }
         } else {
            this.inputScissor = inputHandler.blockAllInput();
            this.textInputField.onClick();
            this.options.clear();

            for (String option : this.originalOptions) {
               if (option.toLowerCase(Locale.ENGLISH).contains(this.textInputField.getInput().toLowerCase(Locale.ENGLISH))) {
                  this.options.add(option);
               }
            }
         }

         this.backgroundColor.setPalette(this.opened ? ColorPalette.PRIMARY : ColorPalette.SECONDARY);
      }

      if (!this.opened) {
         int selected = 0;

         try {
            selected = (Integer)this.get();
         } catch (IllegalAccessException var12) {
         }

         if (hovered && Platform.getMousePlatform().isButtonDown(0)) {
            nanoVGHelper.setAlpha(vg, 0.8F);
         }

         if (this.size == 1) {
            nanoVGHelper.drawRoundedRect(
               vg, x + 224, y, 256.0F, 32.0F, this.backgroundColor.getColor(hovered, hovered && Platform.getMousePlatform().isButtonDown(0)), 12.0F
            );
            nanoVGHelper.drawText(vg, this.originalOptions[selected], x + 236, y + 16, Colors.WHITE_80, 14.0F, Fonts.MEDIUM);
            nanoVGHelper.drawRoundedRect(vg, x + 452, y + 4, 24.0F, 24.0F, this.atomColor.getColor(hovered, false), 8.0F);
            nanoVGHelper.drawSvg(vg, SVGs.DROPDOWN_LIST, (float)(x + 452), (float)(y + 4), 24.0F, 24.0F);
         } else {
            nanoVGHelper.drawRoundedRect(
               vg, x + 352, y, 640.0F, 32.0F, this.backgroundColor.getColor(hovered, hovered && Platform.getMousePlatform().isButtonDown(0)), 12.0F
            );
            nanoVGHelper.drawText(vg, this.originalOptions[selected], x + 364, y + 16, Colors.WHITE_80, 14.0F, Fonts.MEDIUM);
            nanoVGHelper.drawRoundedRect(vg, x + 964, y + 4, 24.0F, 24.0F, this.atomColor.getColor(hovered, false), 8.0F);
            nanoVGHelper.drawSvg(vg, SVGs.DROPDOWN_LIST, (float)(x + 964), (float)(y + 4), 24.0F, 24.0F);
         }

         nanoVGHelper.setAlpha(vg, 1.0F);
      }
   }

   @Override
   public void drawLast(long vg, int x, int y, InputHandler inputHandler) {
      super.drawLast(vg, x, y, inputHandler);
      if (!this.opened) {
         this.finishUpAndClose(false);
      } else {
         if (this.dragging) {
            inputHandler.blockAllInput();
         }

         NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
         boolean hovered;
         if (this.size == 1) {
            hovered = inputHandler.isAreaHovered(x + 224, y, 256.0F, 32.0F, true);
         } else {
            hovered = inputHandler.isAreaHovered(x + 352, y, 640.0F, 32.0F, true);
         }

         this.textInputField.setErrored(this.options.isEmpty());
         if (hovered && Platform.getMousePlatform().isButtonDown(0)) {
            nanoVGHelper.setAlpha(vg, 0.8F);
         }

         if (this.size == 1) {
            this.textInputField.draw(vg, x + 224, y, inputHandler);
         } else {
            this.textInputField.draw(vg, x + 352, y, inputHandler);
         }

         if (!this.options.isEmpty()) {
            if (this.size == 1) {
               nanoVGHelper.setAlpha(vg, 1.0F);
               nanoVGHelper.drawRoundedRect(vg, x + 224, y + 40, 256.0F, Math.min(this.options.size(), 10) * 32 + 8, Colors.GRAY_700, 12.0F);
               nanoVGHelper.drawHollowRoundRect(
                  vg, x + 223, y + 39, 258.0F, Math.min(this.options.size(), 10) * 32 + 10, new Color(204, 204, 204, 77).getRGB(), 12.0F, 1.0F
               );
               ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
               Scissor scissor = null;
               if (this.options.size() > 10) {
                  scissor = scissorHelper.scissor(vg, x + 224, y + 40, 256.0F, 328.0F);
               }

               float listHeight = Math.min(this.options.size(), 10) * 32 + 8;
               boolean listHovered = inputHandler.isAreaHovered(x + 224, y + 40, 256.0F, listHeight, true);
               if (this.opened && listHovered) {
                  inputHandler.blockDWheel();
               }

               this.scroll = this.scrollAnimation == null ? this.scrollTarget : this.scrollAnimation.get();
               float maxHeight = this.options.size() * 32 + 8;
               float scrollBarLength = listHeight / maxHeight * listHeight;
               float dWheel = (float)inputHandler.getDWheel(true);
               if (dWheel != 0.0F) {
                  this.scrollTarget += dWheel;
                  if (this.scrollTarget > 0.0F) {
                     this.scrollTarget = 0.0F;
                  } else if (this.scrollTarget < -maxHeight + listHeight) {
                     this.scrollTarget = -maxHeight + listHeight;
                  }

                  this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
                  this.scrollTime = System.currentTimeMillis();
               } else if (this.scrollAnimation != null && this.scrollAnimation.isFinished()) {
                  this.scrollAnimation = null;
               }

               float optionY = y + 44 + this.scroll;

               for (String option : this.options) {
                  int color = Colors.WHITE_80;
                  boolean optionHovered = inputHandler.isAreaHovered(x + 224, optionY, 252.0F, 32.0F, true);
                  if (optionHovered && Platform.getMousePlatform().isButtonDown(0)) {
                     nanoVGHelper.drawRoundedRect(vg, x + 228, optionY + 2.0F, 248.0F, 28.0F, Colors.PRIMARY_700_80, 8.0F);
                  } else if (optionHovered) {
                     nanoVGHelper.drawRoundedRect(vg, x + 228, optionY + 2.0F, 248.0F, 28.0F, Colors.PRIMARY_700, 8.0F);
                     color = Colors.WHITE;
                  }

                  if (optionHovered && inputHandler.isClicked(!this.dragging)) {
                     try {
                        this.set(Arrays.asList(this.originalOptions).indexOf(option));
                     } catch (IllegalAccessException var23) {
                     }

                     this.opened = false;
                     this.backgroundColor.setPalette(ColorPalette.SECONDARY);
                     inputHandler.unblockDWheel();
                     if (this.inputScissor != null && !this.dragging) {
                        inputHandler.stopBlock(this.inputScissor);
                     }
                  }

                  nanoVGHelper.drawText(vg, option, x + 240, optionY + 18.0F, color, 14.0F, Fonts.MEDIUM);
                  optionY += 32.0F;
               }

               if (scissor != null) {
                  scissorHelper.resetScissor(vg, scissor);
               }

               if (this.dragging && inputHandler.isClicked(true)) {
                  this.dragging = false;
                  inputHandler.stopBlockingInput();
               }

               if (!(scrollBarLength >= listHeight)) {
                  float scrollBarY = this.scroll / maxHeight * (listHeight - 8.0F) - 45.0F;
                  boolean isMouseDown = Platform.getMousePlatform().isButtonDown(0);
                  boolean scrollHover = inputHandler.isAreaHovered(x + 476, y - scrollBarY, 12.0F, (int)scrollBarLength - 5, true);
                  boolean scrollTimePeriod = System.currentTimeMillis() - this.scrollTime < 1000L;
                  if (scrollHover && isMouseDown && !this.mouseWasDown) {
                     this.yStart = inputHandler.mouseY();
                     this.dragging = true;
                  }

                  this.mouseWasDown = isMouseDown;
                  if (this.dragging) {
                     this.scrollTarget = -(inputHandler.mouseY() - this.yStart) * maxHeight / listHeight;
                     if (this.scrollTarget > 0.0F) {
                        this.scrollTarget = 0.0F;
                     } else if (this.scrollTarget < -maxHeight + listHeight) {
                        this.scrollTarget = -maxHeight + listHeight;
                     }

                     this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
                  }

                  NanoVGHelper.INSTANCE
                     .drawRoundedRect(
                        vg,
                        x + 476,
                        y - scrollBarY,
                        4.0F,
                        scrollBarLength - 5.0F,
                        this.colorAnimation.getColor(scrollHover || scrollTimePeriod, this.dragging),
                        4.0F
                     );
               }

               if (listHovered && this.opened) {
                  inputHandler.blockDWheel();
               }
            } else {
               nanoVGHelper.setAlpha(vg, 1.0F);
               nanoVGHelper.drawRoundedRect(vg, x + 352, y + 40, 640.0F, Math.min(this.options.size(), 10) * 32 + 8, Colors.GRAY_700, 12.0F);
               nanoVGHelper.drawHollowRoundRect(
                  vg, x + 351, y + 39, 642.0F, Math.min(this.options.size(), 10) * 32 + 10, new Color(204, 204, 204, 77).getRGB(), 12.0F, 1.0F
               );
               ScissorHelper scissorHelperx = ScissorHelper.INSTANCE;
               Scissor scissorx = null;
               if (this.options.size() > 10) {
                  scissorx = scissorHelperx.scissor(vg, x + 352, y + 40, 640.0F, 328.0F);
               }

               float listHeightx = Math.min(this.options.size(), 10) * 32 + 8;
               boolean listHoveredx = inputHandler.isAreaHovered(x + 352, y + 40, 640.0F, listHeightx, true);
               if (this.opened && listHoveredx) {
                  inputHandler.blockDWheel();
               }

               this.scroll = this.scrollAnimation == null ? this.scrollTarget : this.scrollAnimation.get();
               float maxHeightx = this.options.size() * 32 + 8;
               float scrollBarLengthx = listHeightx / maxHeightx * listHeightx;
               float dWheelx = (float)inputHandler.getDWheel(true);
               if (dWheelx != 0.0F) {
                  this.scrollTarget += dWheelx;
                  if (this.scrollTarget > 0.0F) {
                     this.scrollTarget = 0.0F;
                  } else if (this.scrollTarget < -maxHeightx + listHeightx) {
                     this.scrollTarget = -maxHeightx + listHeightx;
                  }

                  this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
                  this.scrollTime = System.currentTimeMillis();
               } else if (this.scrollAnimation != null && this.scrollAnimation.isFinished()) {
                  this.scrollAnimation = null;
               }

               float optionY = y + 44 + this.scroll;

               for (String option : this.options) {
                  int colorx = Colors.WHITE_80;
                  boolean optionHoveredx = inputHandler.isAreaHovered(x + 352, optionY, 640.0F, 36.0F, true);
                  if (optionHoveredx && Platform.getMousePlatform().isButtonDown(0)) {
                     nanoVGHelper.drawRoundedRect(vg, x + 356, optionY + 2.0F, 632.0F, 28.0F, Colors.PRIMARY_700_80, 8.0F);
                  } else if (optionHoveredx) {
                     nanoVGHelper.drawRoundedRect(vg, x + 356, optionY + 2.0F, 632.0F, 28.0F, Colors.PRIMARY_700, 8.0F);
                     colorx = Colors.WHITE;
                  }

                  nanoVGHelper.drawText(vg, option, x + 368, optionY + 18.0F, colorx, 14.0F, Fonts.MEDIUM);
                  if (optionHoveredx && inputHandler.isClicked(!this.dragging)) {
                     try {
                        this.set(Arrays.asList(this.originalOptions).indexOf(option));
                     } catch (IllegalAccessException var22) {
                     }

                     this.opened = false;
                     this.backgroundColor.setPalette(ColorPalette.SECONDARY);
                     inputHandler.unblockDWheel();
                     if (this.inputScissor != null && !this.dragging) {
                        inputHandler.stopBlock(this.inputScissor);
                     }
                  }

                  optionY += 32.0F;
               }

               if (scissorx != null) {
                  scissorHelperx.resetScissor(vg, scissorx);
               }

               if (this.dragging && inputHandler.isClicked(true)) {
                  this.dragging = false;
                  inputHandler.stopBlockingInput();
               }

               if (!(scrollBarLengthx >= listHeightx)) {
                  float scrollBarYx = this.scroll / maxHeightx * (listHeightx - 8.0F) - 45.0F;
                  boolean isMouseDownx = Platform.getMousePlatform().isButtonDown(0);
                  boolean scrollHoverx = inputHandler.isAreaHovered(x + 988, y - scrollBarYx, 12.0F, (int)scrollBarLengthx - 5, true);
                  boolean scrollTimePeriodx = System.currentTimeMillis() - this.scrollTime < 1000L;
                  if (scrollHoverx && isMouseDownx && !this.mouseWasDown) {
                     this.yStart = inputHandler.mouseY();
                     this.dragging = true;
                  }

                  this.mouseWasDown = isMouseDownx;
                  if (this.dragging) {
                     this.scrollTarget = -(inputHandler.mouseY() - this.yStart) * maxHeightx / listHeightx;
                     if (this.scrollTarget > 0.0F) {
                        this.scrollTarget = 0.0F;
                     } else if (this.scrollTarget < -maxHeightx + listHeightx) {
                        this.scrollTarget = -maxHeightx + listHeightx;
                     }

                     this.scrollAnimation = new EaseOutQuad(150, this.scroll, this.scrollTarget, false);
                  }

                  NanoVGHelper.INSTANCE
                     .drawRoundedRect(
                        vg,
                        x + 988,
                        y - scrollBarYx,
                        4.0F,
                        scrollBarLengthx - 5.0F,
                        this.colorAnimation.getColor(scrollHoverx || scrollTimePeriodx, this.dragging),
                        4.0F
                     );
               }

               if (listHoveredx && this.opened) {
                  inputHandler.blockDWheel();
               }
            }

            nanoVGHelper.setAlpha(vg, 1.0F);
         }
      }
   }

   @Override
   public void keyTyped(int keyCode, int scanCode, int modifiers) {
      super.keyTyped(keyCode, scanCode, modifiers);
      this.textInputField.keyTyped(keyCode, scanCode, modifiers);
      if (this.opened) {
         this.options.clear();

         for (String option : this.originalOptions) {
            if (option.toLowerCase(Locale.ENGLISH).contains(this.textInputField.getInput().toLowerCase(Locale.ENGLISH))) {
               this.options.add(option);
            }
         }
      }
   }

   @Override
   public void charTyped(char chr, int modifiers) {
      super.charTyped(chr, modifiers);
      this.textInputField.charTyped(chr, modifiers);
   }

   @Override
   public int getHeight() {
      return 32;
   }

   @Override
   protected boolean shouldDrawDescription() {
      return super.shouldDrawDescription() && !this.opened;
   }

   @Override
   public void finishUpAndClose() {
      super.finishUpAndClose();
      this.finishUpAndClose(true);
   }

   private void finishUpAndClose(boolean unblock) {
      super.finishUpAndClose();
      this.textInputField.setInput("");
      this.scroll = 0.0F;
      this.scrollTarget = 0.0F;
      this.scrollTime = 0L;
      this.scrollAnimation = null;
      if (unblock && this.inputHandler != null) {
         this.inputHandler.unblockDWheel();
         this.inputHandler.stopBlockingInput();
      }
   }
}
