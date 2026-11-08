package dev.southside.utils;

import dev.southside.utils.misc.StringUtils;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;

public interface ChatUtils {
   class_2561 CLIENT_MSG_PREFIX = class_2561.method_43473()
      .method_10852(class_2561.method_43470("[").method_10862(class_2583.field_24360.method_10977(class_124.field_1080)))
      .method_10852(class_2561.method_43470("S").method_10862(class_2583.field_24360.method_10977(class_124.field_1062)))
      .method_10852(class_2561.method_43470("] ").method_10862(class_2583.field_24360.method_10977(class_124.field_1080)));

   static void message(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473().method_10852(CLIENT_MSG_PREFIX).method_10852(class_2561.method_43470(StringUtils.getReplaced(text, args)))
               );
         }
      }
   }

   static void message(class_2561 text) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551().field_1705.method_1743().method_1812(class_2561.method_43473().method_10852(CLIENT_MSG_PREFIX).method_10852(text));
         }
      }
   }

   static void info(String text, Object... args) {
      message(text, args);
   }

   static void info(class_2561 text) {
      message(text);
   }

   static void messagef(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(class_2561.method_43473().method_10852(CLIENT_MSG_PREFIX).method_10852(class_2561.method_43470(String.format(text, args))));
         }
      }
   }

   static void infof(String text, Object... args) {
      messagef(text, args);
   }

   static void error(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(
                        class_2561.method_43470(StringUtils.getReplaced(text, args)).method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
                     )
               );
         }
      }
   }

   static void error(class_2561 text) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(text)
                     .method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
               );
         }
      }
   }

   static void errorf(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(class_2561.method_43470(String.format(text, args)).method_10862(class_2583.field_24360.method_10977(class_124.field_1061)))
               );
         }
      }
   }

   static void success(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(
                        class_2561.method_43470(StringUtils.getReplaced(text, args)).method_10862(class_2583.field_24360.method_10977(class_124.field_1080))
                     )
               );
         }
      }
   }

   static void success(class_2561 text) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(text)
                     .method_10862(class_2583.field_24360.method_10977(class_124.field_1080))
               );
         }
      }
   }

   static void successf(String text, Object... args) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(class_2561.method_43470(String.format(text, args)).method_10862(class_2583.field_24360.method_10977(class_124.field_1080)))
               );
         }
      }
   }

   static void warning(String text, Object... replacement) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(
                        class_2561.method_43470(StringUtils.getReplaced(text, replacement))
                           .method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
                     )
               );
         }
      }
   }

   static void warning(class_2561 text) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(text)
                     .method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
               );
         }
      }
   }

   static void warningf(String text, Object... replacement) {
      if (text != null) {
         if (class_310.method_1551().field_1705 != null) {
            class_310.method_1551()
               .field_1705
               .method_1743()
               .method_1812(
                  class_2561.method_43473()
                     .method_10852(CLIENT_MSG_PREFIX)
                     .method_10852(
                        class_2561.method_43470(String.format(text, replacement)).method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
                     )
               );
         }
      }
   }

   static void postChatMessage(String message) {
      if (class_310.method_1551().field_1705 != null) {
         class_310.method_1551()
            .field_1705
            .method_1743()
            .method_1812(
               class_2561.method_43473()
                  .method_10852(CLIENT_MSG_PREFIX)
                  .method_10852(
                     class_2561.method_43470(String.format(message, class_124.field_1068))
                        .method_10862(class_2583.field_24360.method_10977(class_124.field_1061))
                  )
            );
      }
   }

   static void ircMessage(String sender, String message, String client, String rank) {
      postChatMessage(
         class_124.field_1075
            + "[IRC] "
            + class_124.field_1070
            + class_124.field_1060
            + "("
            + client
            + ") "
            + class_124.field_1070
            + class_124.field_1067
            + class_124.field_1065
            + sender
            + " "
            + class_124.field_1070
            + getRankColor(rank)
            + class_124.field_1070
            + "["
            + rank
            + class_124.field_1070
            + "] "
            + class_124.field_1070
            + ": "
            + message
      );
   }

   private static String getRankColor(String rank) {
      String color = String.valueOf(class_124.field_1068);
      switch (rank) {
         case "Admin":
            color = String.valueOf(class_124.field_1061);
            break;
         case "Reseller":
            color = String.valueOf(class_124.field_1058);
      }

      return color;
   }
}
