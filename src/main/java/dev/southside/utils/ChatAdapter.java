package dev.southside.utils;

public interface ChatAdapter {
   default void message(String text, Object... args) {
      ChatUtils.message(text, args);
   }

   default void info(String text, Object... args) {
      ChatUtils.info(text, args);
   }

   default void messagef(String text, Object... args) {
      ChatUtils.messagef(text, args);
   }

   default void infof(String text, Object... args) {
      ChatUtils.infof(text, args);
   }

   default void error(String text, Object... args) {
      ChatUtils.error(text, args);
   }

   default void errorf(String text, Object... args) {
      ChatUtils.errorf(text, args);
   }

   default void success(String text, Object... args) {
      ChatUtils.success(text, args);
   }

   default void successf(String text, Object... args) {
      ChatUtils.successf(text, args);
   }

   default void warning(String text, Object... args) {
      ChatUtils.warning(text, args);
   }

   default void warningf(String text, Object... args) {
      ChatUtils.warningf(text, args);
   }
}
