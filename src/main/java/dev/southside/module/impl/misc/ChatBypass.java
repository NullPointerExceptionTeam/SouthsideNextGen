package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ChatBypass extends Module {
   public final Switch addComma = new Switch("Add Comma", true);

   public static boolean getAddComma() {
      return Southside.get().getModuleManager().get(ChatBypass.class).addComma.getValue();
   }

   public ChatBypass() {
      super(Category.Misc, "ChatBypass");
   }

   public static String bypass(String content) {
      if (!content.startsWith("/") && !content.startsWith(".")) {
         StringBuilder stringBuilder = new StringBuilder();

         for (char c : content.toCharArray()) {
            if (c == 'L') {
               stringBuilder.append('Ｌ');
            } else if (c == 'l') {
               stringBuilder.append('ｌ');
            } else {
               stringBuilder.append(c);
            }

            if (getAddComma()) {
               stringBuilder.append(',');
            }
         }

         return stringBuilder.toString();
      } else {
         return content;
      }
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
   }

   private static int[][] getGlyph(char character) {
      BufferedImage image = new BufferedImage(16, 16, 1);
      Graphics2D g = image.createGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, 16, 16);
      Font font = new Font("Courier", 0, 12);
      g.setFont(font);
      g.setColor(Color.BLACK);
      g.drawString(String.valueOf(character), 0, 12);
      int[][] res = new int[16][16];

      for (int i = 0; i < 16; i++) {
         for (int j = 0; j < 16; j++) {
            res[i][j] = image.getRGB(j, i) == Color.WHITE.getRGB() ? 0 : 1;
         }

         System.out.println();
      }

      g.dispose();
      return res;
   }

   private static String[] getCompressedGlyph(char character) {
      int[][] r = getGlyph(character);
      StringBuilder[] builder = new StringBuilder[4];

      for (int i = 0; i < 4; i++) {
         builder[i] = new StringBuilder();
      }

      String[] res = new String[4];

      for (int i = 0; i < 16; i += 4) {
         for (int j = 0; j < 16; j += 2) {
            int value = r[i][j]
               + r[i + 1][j] * 2
               + r[i + 2][j] * 4
               + r[i][j + 1] * 8
               + r[i + 1][j + 1] * 16
               + r[i + 2][j + 1] * 32
               + r[i + 3][j] * 64
               + r[i + 3][j + 1] * 128;
            builder[i / 4].append((char)(10240 + value));
         }
      }

      for (int i = 0; i < 4; i++) {
         res[i] = builder[i].toString();
      }

      return res;
   }

   public static String[] getGlyph(String s) {
      s = s.trim();
      if (s.isEmpty()) {
         return null;
      } else {
         StringBuilder[] builder = new StringBuilder[4];

         for (int i = 0; i < 4; i++) {
            builder[i] = new StringBuilder();
         }

         String[] res = new String[4];

         for (int i = 0; i < s.length(); i++) {
            String[] ch = getCompressedGlyph(s.charAt(i));

            for (int j = 0; j < 4; j++) {
               builder[j].append(ch[j]);
            }
         }

         for (int i = 0; i < 4; i++) {
            res[i] = builder[i].toString();
         }

         return res;
      }
   }
}
