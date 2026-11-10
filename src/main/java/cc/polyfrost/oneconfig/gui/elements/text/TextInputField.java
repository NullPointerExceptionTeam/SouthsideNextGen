package cc.polyfrost.oneconfig.gui.elements.text;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.gui.elements.BasicElement;
import cc.polyfrost.oneconfig.internal.assets.Colors;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.asset.SVG;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.MathUtils;
import cc.polyfrost.oneconfig.utils.TextUtils;
import dev.southside.event.Stage;
import dev.southside.event.events.GameRendererDrawEvent;
import dev.southside.orbit.EventHandler;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;

public class TextInputField extends BasicElement {
   protected final String defaultText;
   protected final boolean multiLine;
   private float radius;
   private float boarderThickness = 1.0F;
   private float textSize = 12.0F;
   protected String input;
   protected String selectedText;
   protected boolean password;
   protected int caretPos;
   protected float x;
   protected float y;
   protected float start;
   protected float end;
   protected int startLine;
   protected int endLine;
   protected long vg;
   protected int prevCaret = 0;
   protected boolean isDoubleClick = false;
   protected boolean onlyNums = false;
   protected boolean errored = false;
   protected boolean centered = false;
   protected SVG icon;
   protected ArrayList<String> wrappedText = null;
   private long clickTimeD1;
   private int lines = 1;
   private static boolean anyCheckInit = false;
   private static boolean isAnySelected = false;
   private long lastClickTime = 0L;
   private boolean awaitingDoubleClick = false;
   private InputHandler inputHandler;

   public TextInputField(int width, int height, String defaultText, boolean multiLine, boolean password, SVG icon, float radius) {
      super(width, height, false);
      if (!anyCheckInit) {
         anyCheckInit = true;
         EventManager.INSTANCE.register(new TextInputField.TextInputSelectionChecker());
      }

      this.multiLine = multiLine;
      this.defaultText = defaultText;
      this.password = password;
      this.input = "";
      this.icon = icon;
      this.radius = radius;
   }

   public TextInputField(int width, int height, String defaultText, boolean multiLine, boolean password, SVG icon) {
      this(width, height, defaultText, multiLine, password, null, 12.0F);
   }

   public TextInputField(int width, int height, String defaultText, boolean multiLine, boolean password) {
      this(width, height, defaultText, multiLine, password, null, 12.0F);
   }

   public TextInputField(int width, int height, boolean centered, String defaultText) {
      this(width, height, defaultText, false, false, null, 12.0F);
      this.centered = centered;
   }

   public TextInputField(int width, int height, boolean centered, float radius) {
      this(width, height, "", false, false, null, radius);
      this.setCentered(centered);
   }

   public static boolean isAllowedCharacter(char character) {
      return character != 167 && character >= ' ' && character != 127;
   }

   public void onlyAcceptNumbers(boolean state) {
      this.onlyNums = state;
   }

   public String getInput() {
      return this.input;
   }

   public void setInput(String input) {
      this.input = input;
   }

   public boolean getPassword() {
      return this.password;
   }

   public void setPassword(boolean password) {
      this.password = password;
   }

   public void setCentered(boolean centered) {
      this.centered = centered;
   }

   public boolean isErrored() {
      return this.errored;
   }

   public void setErrored(boolean errored) {
      this.errored = errored;
   }

   @Override
   public void draw(long vg, float x, float y, InputHandler inputHandler) {
      ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      this.x = x;
      this.y = y;
      this.vg = vg;

      try {
         this.inputHandler = inputHandler;
         nanoVGHelper.drawRoundedRect(
            vg,
            x + this.boarderThickness,
            y + this.boarderThickness,
            this.width - this.boarderThickness * 2.0F,
            this.height - this.boarderThickness * 2.0F,
            Colors.GRAY_850,
            this.radius
         );
         int colorOutline = this.errored ? Colors.ERROR_700 : Colors.GRAY_700;
         if (!this.toggled) {
            nanoVGHelper.drawHollowRoundRect(vg, x, y, this.width - 0.5F, this.height - 0.5F, colorOutline, this.radius, this.boarderThickness);
         } else {
            nanoVGHelper.drawHollowRoundRect(
               vg, x, y, this.width - 0.5F, this.height - 0.5F, this.errored ? Colors.ERROR_600 : Colors.PRIMARY_600, this.radius, this.boarderThickness
            );
         }

         Scissor scissor = scissorHelper.scissor(vg, x, y, this.width, this.height);
         super.update(x, y, inputHandler);
         if (Platform.getMousePlatform().isButtonDown(0) && !this.hovered) {
            this.onClose();
            this.toggled = false;
         }

         int color = this.toggled ? Colors.WHITE : Colors.WHITE_60;
         if (!this.toggled) {
            this.caretPos = this.input.length();
         }

         if (this.caretPos > this.input.length()) {
            this.caretPos = this.input.length();
         }

         if (this.prevCaret > this.input.length()) {
            this.prevCaret = this.input.length();
         }

         if (this.caretPos < 0) {
            this.caretPos = 0;
         }

         if (this.prevCaret < 0) {
            this.prevCaret = 0;
         }

         if (this.icon != null) {
            nanoVGHelper.drawSvg(vg, this.icon, x + 10.0F, y + this.height / 2.0F - 10.0F, 20.0F, 20.0F, color);
            x += 36.0F;
            this.x = x;
         }

         StringBuilder s = new StringBuilder();
         float width;
         if (this.multiLine) {
            this.wrappedText = TextUtils.wrapText(vg, this.input, this.width - 24, this.textSize, Fonts.REGULAR);
            this.lines = this.wrappedText.size();
            if (!this.toggled) {
               this.caretPos = this.wrappedText.get(this.wrappedText.size() - 1).length();
            }

            int caretLine = (int)MathUtils.clamp(this.getCaretLine(this.caretPos), 0.0F, this.wrappedText.size() - 1);
            width = nanoVGHelper.getTextWidth(
               vg, this.wrappedText.get(caretLine).substring(0, this.getLineCaret(this.caretPos, caretLine)), this.textSize, Fonts.REGULAR
            );
         } else if (!this.password) {
            width = nanoVGHelper.getTextWidth(vg, this.input.substring(0, this.caretPos), this.textSize, Fonts.REGULAR);
         } else {
            for (int i = 0; i < this.input.length(); i++) {
               s.append("*");
            }

            width = nanoVGHelper.getTextWidth(vg, s.substring(0, this.caretPos), this.textSize, Fonts.REGULAR);
         }

         float halfTextWidth = this.getTextWidth(vg, this.input) / 2.0F;
         if (this.start != 0.0F && this.end != 0.0F && this.toggled) {
            if (!this.multiLine) {
               nanoVGHelper.drawRect(vg, this.start, y + this.height / 2.0F - 10.0F, this.end, 20.0F, Colors.GRAY_300);
            } else if (this.startLine == this.endLine) {
               nanoVGHelper.drawRect(vg, this.start, y + 10.0F + 24 * this.startLine, this.end, 20.0F, Colors.GRAY_300);
            } else {
               nanoVGHelper.drawRect(vg, this.start, y + 10.0F + 24 * this.startLine, (float)(this.width - 24), 20.0F, Colors.GRAY_300);

               for (int i = this.startLine + 1; i < this.endLine; i++) {
                  nanoVGHelper.drawRect(vg, x + 12.0F, y + 10.0F + 24 * i, (float)(this.width - 24), 20.0F, Colors.GRAY_300);
               }

               nanoVGHelper.drawRect(vg, x + 12.0F, y + 10.0F + 24 * this.endLine, this.end, 20.0F, Colors.GRAY_300);
            }
         }

         if (this.hovered && Platform.getMousePlatform().isButtonDown(0) && !this.isDoubleClick) {
            if (this.multiLine) {
               int caretLine = Math.max(0, Math.min(this.wrappedText.size() - 1, (int)Math.floor((inputHandler.mouseY() - y - 10.0F) / 24.0F)));
               this.caretPos = this.calculatePos(inputHandler.mouseX(), this.wrappedText.get(caretLine));

               for (int i = 0; i < caretLine; i++) {
                  this.caretPos = this.caretPos + this.wrappedText.get(i).length();
               }
            } else {
               this.caretPos = this.calculatePos(inputHandler.mouseX(), this.input);
            }

            if (this.caretPos > this.prevCaret) {
               if (!this.centered) {
                  this.start = x + 12.0F + this.getTextWidth(vg, this.input.substring(0, this.prevCaret));
               } else {
                  this.start = x + this.width / 2.0F - halfTextWidth + this.getTextWidth(vg, this.input.substring(0, this.prevCaret));
               }

               this.end = this.getTextWidth(vg, this.input.substring(this.prevCaret, this.caretPos));
               this.selectedText = this.input.substring(this.prevCaret, this.caretPos);
            } else {
               if (!this.centered) {
                  this.start = x + 12.0F + this.getTextWidth(vg, this.input.substring(0, this.prevCaret));
               } else {
                  this.start = x + this.width / 2.0F - halfTextWidth + this.getTextWidth(vg, this.input.substring(0, this.prevCaret));
               }

               this.end = -this.getTextWidth(vg, this.input.substring(this.caretPos, this.prevCaret));
               this.selectedText = this.input.substring(this.caretPos, this.prevCaret);
            }
         }

         float last = nanoVGHelper.lastAlpha();
         if (this.disabled) {
            nanoVGHelper.setAlpha(vg, 0.5F);
         }

         if (this.toggled) {
            if (this.multiLine) {
               float lineY = y + 20.0F + this.getCaretLine(this.caretPos) * 24;
               nanoVGHelper.drawLine(vg, x + width + 12.0F, lineY - 10.0F, x + width + 12.0F, lineY + 10.0F, 1.0F, Colors.WHITE);
            } else if (!this.centered) {
               nanoVGHelper.drawLine(
                  vg, x + width + 12.0F, y + this.height / 2.0F - 10.0F, x + width + 12.0F, y + this.height / 2.0F + 10.0F, 1.0F, Colors.WHITE
               );
            } else {
               nanoVGHelper.drawLine(
                  vg,
                  x + this.width / 2.0F - halfTextWidth + width,
                  y + this.height / 2.0F - 10.0F,
                  x + this.width / 2.0F - halfTextWidth + width,
                  y + this.height / 2.0F + 10.0F,
                  1.0F,
                  Colors.WHITE
               );
            }
         }

         if (this.input.equals("")) {
            if (this.multiLine) {
               nanoVGHelper.drawText(vg, this.defaultText, x + 12.0F, y + 16.0F, color, this.textSize, Fonts.REGULAR);
            } else if (!this.centered) {
               nanoVGHelper.drawText(vg, this.defaultText, x + 12.0F, y + this.height / 2.0F, color, this.textSize, Fonts.REGULAR);
            } else {
               nanoVGHelper.drawText(vg, this.defaultText, x + this.width / 2.0F - halfTextWidth, y + this.height / 2.0F, color, this.textSize, Fonts.REGULAR);
            }
         }

         if (!this.password) {
            if (this.multiLine) {
               float textY = y + 20.0F;

               for (String line : this.wrappedText) {
                  nanoVGHelper.drawText(vg, line, x + 12.0F, textY, color, this.textSize, Fonts.REGULAR);
                  textY += 24.0F;
               }
            } else if (!this.centered) {
               nanoVGHelper.drawText(vg, this.input, x + 12.0F, y + this.height / 2.0F, color, this.textSize, Fonts.REGULAR);
            } else {
               nanoVGHelper.drawText(vg, this.input, x + this.width / 2.0F - halfTextWidth, y + this.height / 2.0F, color, this.textSize, Fonts.REGULAR);
            }
         } else {
            nanoVGHelper.drawText(vg, s.toString(), x + 12.0F, y + this.height / 2.0F, color, this.textSize, Fonts.REGULAR);
         }

         nanoVGHelper.setAlpha(vg, last);
         scissorHelper.resetScissor(vg, scissor);
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      if (this.toggled) {
         isAnySelected = true;
      }
   }

   public void charTyped(char c, int modifiers) {
      try {
         if (this.toggled) {
            if (this.onlyNums && !Character.isDigit(c)) {
               return;
            }

            if (!Character.isDefined(c)) {
               return;
            }

            if (UKeyboard.isCtrlKeyDown()) {
               return;
            }

            if (isValidChar(c)) {
               if (this.selectedText != null) {
                  if (this.caretPos > this.prevCaret) {
                     this.caretPos = this.prevCaret;
                  }

                  if (this.selectedText.equals(this.input)) {
                     this.input = "";
                     this.caretPos = 0;
                  }

                  this.selectedText = null;
               }

               this.input = this.addCharAtPoint(this.caretPos, c);
               this.caretPos++;
            }

            if (this.start != 0.0F && this.end != 0.0F) {
               this.start = 0.0F;
               this.end = 0.0F;
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (this.hovered && mouseButton == 0) {
         long now = System.currentTimeMillis();
         if (now - this.lastClickTime < 300L) {
            this.onDoubleClick(this.inputHandler);
            this.isDoubleClick = true;
            this.awaitingDoubleClick = false;
         } else {
            this.awaitingDoubleClick = true;
         }

         this.lastClickTime = now;
      }
   }

   public void mouseReleased(double mouseX, double mouseY, int state) {
      if (this.hovered && state == 0) {
         if (this.awaitingDoubleClick && !this.isDoubleClick) {
            this.start = 0.0F;
            this.end = 0.0F;
            this.prevCaret = this.caretPos;
         }

         this.isDoubleClick = false;
         this.awaitingDoubleClick = false;
      }
   }

   public static boolean isValidChar(char chr) {
      return chr != 167 && chr >= ' ' && chr != 127;
   }

   public void keyTyped(int key, int scanCode, int modifiers) {
      try {
         if (this.toggled) {
            if (Screen.isCopy(key)) {
               if (this.selectedText != null && this.start != 0.0F && this.end != 0.0F) {
                  MinecraftClient.getInstance().keyboard.setClipboard(this.selectedText);
               }

               return;
            }

            if (Screen.isPaste(key)) {
               try {
                  String clip = MinecraftClient.getInstance().keyboard.getClipboard();
                  this.input = this.input.substring(0, this.caretPos) + clip + this.input.substring(this.caretPos);
                  this.caretPos = this.caretPos + Objects.requireNonNull(clip).length();
                  return;
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
            }

            if (key == 261) {
               this.input = "";
            }

            if (UKeyboard.isCtrlKeyDown()) {
               if (key == 259 && !UKeyboard.isKeyComboCtrlX(key)) {
                  try {
                     this.input = this.input.substring(0, this.input.lastIndexOf(" "));
                     this.caretPos = this.input.length();
                  } catch (Exception var5) {
                     this.input = "";
                     this.caretPos = 0;
                  }

                  return;
               }

               if (Screen.isSelectAll(key)) {
                  this.prevCaret = 0;
                  this.caretPos = this.input.length();
                  this.start = !this.centered ? this.x + 12.0F : this.x + this.width / 2.0F - this.getTextWidth(this.vg, this.input) / 2.0F;
                  this.selectedText = this.input;
                  System.out.println("ccc");
                  if (!this.multiLine) {
                     this.end = this.getTextWidth(this.vg, this.input);
                  }

                  if (this.multiLine) {
                     this.end = this.getTextWidth(this.vg, this.wrappedText.get(this.wrappedText.size() - 1));
                     this.startLine = 0;
                     this.endLine = this.wrappedText.size() - 1;
                  }

                  return;
               }

               if (Screen.isCut(key)) {
                  if (this.selectedText == null || this.start == 0.0F || this.end == 0.0F) {
                     return;
                  }

                  MinecraftClient.getInstance().keyboard.setClipboard(this.selectedText);
                  key = 259;
               }

               if (key == 263) {
                  this.caretPos = this.input.substring(0, this.caretPos).lastIndexOf(32) + 1;
               }

               if (key == 262) {
                  this.caretPos = this.input.indexOf(32, this.caretPos);
                  if (this.caretPos == -1) {
                     this.caretPos = this.input.length();
                  }
               }
            }

            if (key == 259) {
               if (!this.input.isEmpty()) {
                  if (this.start != 0.0F && this.end != 0.0F) {
                     this.start = 0.0F;
                     this.end = 0.0F;
                     if (this.caretPos > this.prevCaret) {
                        this.input = this.input.substring(0, this.prevCaret) + this.input.substring(this.caretPos);
                        this.caretPos = this.prevCaret;
                     }

                     if (this.caretPos < this.prevCaret) {
                        this.input = this.input.substring(0, this.caretPos) + this.input.substring(this.prevCaret);
                     }

                     return;
                  }

                  if (this.caretPos == this.input.length()) {
                     this.input = this.input.substring(0, this.input.length() - 1);
                  } else {
                     this.input = this.input.substring(0, this.caretPos - 1) + this.input.substring(this.caretPos);
                  }

                  this.caretPos--;
               }

               return;
            }

            if (key == 258) {
               if (this.onlyNums) {
                  return;
               }

               this.input = this.input + "    ";
               this.caretPos += 4;
               return;
            }

            if (key == 262) {
               this.caretPos++;
               if (this.caretPos > this.input.length()) {
                  this.caretPos = this.input.length();
               }

               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            if (key == 263) {
               this.caretPos--;
               if (this.caretPos < 0) {
                  this.caretPos = 0;
               }

               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            if (key != 265 && key != 201) {
               if (key != 264 && key != 209) {
                  if (key == 257) {
                     this.onClose();
                     this.toggled = false;
                     if (this.start != 0.0F && this.end != 0.0F) {
                        this.start = 0.0F;
                        this.end = 0.0F;
                     }
                  }

                  if (key == 269) {
                     this.onClose();
                     this.toggled = false;
                  }

                  if (key != 341
                     && key != 345
                     && key != 342
                     && key != 346
                     && key != 343
                     && key != 347
                     && key != 340
                     && key != 344
                     && key != 257
                     && key != 280
                     && key != 221
                     && key != 268) {
                     return;
                  }

                  return;
               }

               this.caretPos = this.input.length();
               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            this.caretPos = 0;
            if (this.start != 0.0F && this.end != 0.0F) {
               this.start = 0.0F;
               this.end = 0.0F;
            }

            return;
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   @NotNull
   private String addCharAtPoint(int index, char c) {
      return this.input.substring(0, index) + c + this.input.substring(index);
   }

   @Override
   public void onClick() {
      this.toggled = true;
   }

   private void onDoubleClick(InputHandler inputHandler) {
      this.prevCaret = this.input.substring(0, this.caretPos).lastIndexOf(32) + 1;
      this.caretPos = this.input.indexOf(32, this.caretPos);
      if (this.caretPos == -1) {
         this.caretPos = this.input.length();
      }

      this.selectedText = this.input.substring(this.prevCaret, this.caretPos);
      System.out.println("aa");
      if (this.multiLine) {
         int caretLine = Math.max(0, Math.min(this.wrappedText.size() - 1, (int)Math.floor((inputHandler.mouseY() - this.y - 10.0F) / 24.0F)));
         this.startLine = caretLine;
         this.endLine = caretLine;
         this.start = this.x
            + 12.0F
            + this.getTextWidth(this.vg, this.wrappedText.get(caretLine).substring(0, this.getLineCaret(this.prevCaret, this.startLine)));
         this.end = this.getTextWidth(
            this.vg,
            this.wrappedText.get(caretLine).substring(this.getLineCaret(this.prevCaret, this.startLine), this.getLineCaret(this.caretPos, this.startLine))
         );
      } else {
         this.start = this.x + 12.0F + this.getTextWidth(this.vg, this.input.substring(0, this.prevCaret));
         this.end = this.getTextWidth(this.vg, this.input.substring(this.prevCaret, this.caretPos));
      }
   }

   private int calculatePos(float pos, String string) {
      if (this.centered) {
         pos -= 12.0F;
      }

      String s1 = "";

      for (char c : string.toCharArray()) {
         if (pos - this.x - 12.0F < 0.0F) {
            return 0;
         }

         if (pos - this.x - 12.0F > this.getTextWidth(this.vg, string)) {
            return string.length();
         }

         s1 = s1 + c;
         int i = (int)this.getTextWidth(this.vg, s1);
         if (i >= pos - this.x - 16.0F) {
            return s1.length();
         }
      }

      return 0;
   }

   public void onClose() {
   }

   private float getTextWidth(long vg, String s) {
      if (this.password) {
         StringBuilder s1 = new StringBuilder();

         while (s1.length() < s.length()) {
            s1.append('*');
         }

         s = s1.toString();
      }

      return NanoVGHelper.INSTANCE.getTextWidth(vg, s, this.textSize, Fonts.REGULAR);
   }

   private int getCaretLine(int caret) {
      int pos = 0;

      for (int i = 0; i < this.wrappedText.size(); i++) {
         pos += this.wrappedText.get(i).length();
         if (pos >= caret - 1) {
            return i;
         }
      }

      return 0;
   }

   private float getCaretX(int caret) {
      int pos = 0;
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;

      for (String text : this.wrappedText) {
         float length = nanoVGHelper.getTextWidth(this.vg, text, this.textSize, Fonts.REGULAR);
         if (!(pos + length < caret)) {
            return nanoVGHelper.getTextWidth(this.vg, text.substring(0, caret - pos), this.textSize, Fonts.REGULAR);
         }

         pos = (int)(pos + length);
      }

      return 0.0F;
   }

   private int getLineCaret(int caret, int line) {
      int pos = 0;

      for (String text : this.wrappedText) {
         int length = text.length();
         if (pos + length >= caret - 1) {
            return caret - pos;
         }

         pos += length;
      }

      return 0;
   }

   public int getLines() {
      return this.lines;
   }

   public void setBoarderThickness(float thickness) {
      this.boarderThickness = thickness;
   }

   public void setFontSize(float textSize) {
      this.textSize = textSize;
   }

   public static boolean isAnySelected() {
      return isAnySelected;
   }

   private static class TextInputSelectionChecker {
      @EventHandler
      private void onRender(GameRendererDrawEvent event) {
         if (event.stage == Stage.PRE) {
            TextInputField.isAnySelected = false;
         }
      }
   }
}
