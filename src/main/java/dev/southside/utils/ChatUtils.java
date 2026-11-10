package dev.southside.utils;

import dev.southside.utils.misc.StringUtils;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;

public interface ChatUtils {
   Text CLIENT_MSG_PREFIX = Text.empty()
      .append(Text.literal("[").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
      .append(Text.literal("S").setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)))
      .append(Text.literal("] ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));

   static void message(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty().append(CLIENT_MSG_PREFIX).append(Text.literal(StringUtils.getReplaced(text, args)))
               );
         }
      }
   }

   static void message(Text text) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty().append(CLIENT_MSG_PREFIX).append(text));
         }
      }
   }

   static void info(String text, Object... args) {
      message(text, args);
   }

   static void info(Text text) {
      message(text);
   }

   static void messagef(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(Text.empty().append(CLIENT_MSG_PREFIX).append(Text.literal(String.format(text, args))));
         }
      }
   }

   static void infof(String text, Object... args) {
      messagef(text, args);
   }

   static void error(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(
                        Text.literal(StringUtils.getReplaced(text, args)).setStyle(Style.EMPTY.withColor(Formatting.RED))
                     )
               );
         }
      }
   }

   static void error(Text text) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(text)
                     .setStyle(Style.EMPTY.withColor(Formatting.RED))
               );
         }
      }
   }

   static void errorf(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(Text.literal(String.format(text, args)).setStyle(Style.EMPTY.withColor(Formatting.RED)))
               );
         }
      }
   }

   static void success(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(
                        Text.literal(StringUtils.getReplaced(text, args)).setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                     )
               );
         }
      }
   }

   static void success(Text text) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(text)
                     .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
               );
         }
      }
   }

   static void successf(String text, Object... args) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(Text.literal(String.format(text, args)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
               );
         }
      }
   }

   static void warning(String text, Object... replacement) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(
                        Text.literal(StringUtils.getReplaced(text, replacement))
                           .setStyle(Style.EMPTY.withColor(Formatting.RED))
                     )
               );
         }
      }
   }

   static void warning(Text text) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(text)
                     .setStyle(Style.EMPTY.withColor(Formatting.RED))
               );
         }
      }
   }

   static void warningf(String text, Object... replacement) {
      if (text != null) {
         if (MinecraftClient.getInstance().inGameHud != null) {
            MinecraftClient.getInstance()
               .inGameHud
               .getChatHud()
               .addMessage(
                  Text.empty()
                     .append(CLIENT_MSG_PREFIX)
                     .append(
                        Text.literal(String.format(text, replacement)).setStyle(Style.EMPTY.withColor(Formatting.RED))
                     )
               );
         }
      }
   }

   static void postChatMessage(String message) {
      if (MinecraftClient.getInstance().inGameHud != null) {
         MinecraftClient.getInstance()
            .inGameHud
            .getChatHud()
            .addMessage(
               Text.empty()
                  .append(CLIENT_MSG_PREFIX)
                  .append(
                     Text.literal(String.format(message, Formatting.WHITE))
                        .setStyle(Style.EMPTY.withColor(Formatting.RED))
                  )
            );
      }
   }

   static void ircMessage(String sender, String message, String client, String rank) {
      postChatMessage(
         Formatting.AQUA
            + "[IRC] "
            + Formatting.RESET
            + Formatting.GREEN
            + "("
            + client
            + ") "
            + Formatting.RESET
            + Formatting.BOLD
            + Formatting.GOLD
            + sender
            + " "
            + Formatting.RESET
            + getRankColor(rank)
            + Formatting.RESET
            + "["
            + rank
            + Formatting.RESET
            + "] "
            + Formatting.RESET
            + ": "
            + message
      );
   }

   private static String getRankColor(String rank) {
      String color = String.valueOf(Formatting.WHITE);
      switch (rank) {
         case "Admin":
            color = String.valueOf(Formatting.RED);
            break;
         case "Reseller":
            color = String.valueOf(Formatting.DARK_BLUE);
      }

      return color;
   }
}
