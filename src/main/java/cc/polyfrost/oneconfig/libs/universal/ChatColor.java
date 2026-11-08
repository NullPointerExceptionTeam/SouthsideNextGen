package cc.polyfrost.oneconfig.libs.universal;

import java.awt.Color;

public class ChatColor {
   public static final ChatColor BLACK = new ChatColor("BLACK", 0, '0', new Color(0), false, 4);
   public static final ChatColor DARK_BLUE = new ChatColor("DARK_BLUE", 1, '1', new Color(170), false, 4);
   public static final ChatColor DARK_GREEN = new ChatColor("DARK_GREEN", 2, '2', new Color(43520), false, 4);
   public static final ChatColor DARK_AQUA = new ChatColor("DARK_AQUA", 3, '3', new Color(43690), false, 4);
   public static final ChatColor DARK_RED = new ChatColor("DARK_RED", 4, '4', new Color(11141120), false, 4);
   public static final ChatColor DARK_PURPLE = new ChatColor("DARK_PURPLE", 5, '5', new Color(11141290), false, 4);
   public static final ChatColor GOLD = new ChatColor("GOLD", 6, '6', new Color(16755200), false, 4);
   public static final ChatColor GRAY = new ChatColor("GRAY", 7, '7', new Color(11184810), false, 4);
   public static final ChatColor DARK_GRAY = new ChatColor("DARK_GRAY", 8, '8', new Color(5592405), false, 4);
   public static final ChatColor BLUE = new ChatColor("BLUE", 9, '9', new Color(5592575), false, 4);
   public static final ChatColor GREEN = new ChatColor("GREEN", 10, 'a', new Color(5635925), false, 4);
   public static final ChatColor AQUA = new ChatColor("AQUA", 11, 'b', new Color(5636095), false, 4);
   public static final ChatColor RED = new ChatColor("RED", 12, 'c', new Color(16733525), false, 4);
   public static final ChatColor LIGHT_PURPLE = new ChatColor("LIGHT_PURPLE", 13, 'd', new Color(16733695), false, 4);
   public static final ChatColor YELLOW = new ChatColor("YELLOW", 14, 'e', new Color(16777045), false, 4);
   public static final ChatColor WHITE = new ChatColor("WHITE", 15, 'f', new Color(16777215), false, 4);
   public static final ChatColor MAGIC = new ChatColor("MAGIC", 16, 'k', null, true, 2);
   public static final ChatColor BOLD = new ChatColor("BOLD", 17, 'l', null, true, 2);
   public static final ChatColor STRIKETHROUGH = new ChatColor("STRIKETHROUGH", 18, 'm', null, true, 2);
   public static final ChatColor UNDERLINE = new ChatColor("UNDERLINE", 19, 'n', null, true, 2);
   public static final ChatColor ITALIC = new ChatColor("ITALIC", 20, 'o', null, true, 2);
   public static final ChatColor RESET = new ChatColor("RESET", 21, 'r', null, false, 6);
   private final char char1;
   private final Color color;
   private final boolean isFormat;
   public static final char COLOR_CHAR = '§';

   public ChatColor(String string, int n, char c, Color color, boolean bl, int n2) {
      this(c, color, bl);
   }

   public ChatColor(char c, Color color, boolean isFormat) {
      this.char1 = c;
      this.color = color;
      this.isFormat = isFormat;
   }

   public String getChar() {
      return "§" + this.char1;
   }
}
