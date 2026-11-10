package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindingArgumentType implements ArgumentType<KeybindingArgumentType.Keybinding> {
   public static final DynamicCommandExceptionType INVALID_KEY = new DynamicCommandExceptionType(key -> Text.literal("Invalid key: " + key));
   private static final String[] KEYS = new String[]{
      "space",
      "apostrophe",
      "comma",
      "minus",
      "period",
      "slash",
      "0",
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "semicolon",
      "equal",
      "a",
      "b",
      "c",
      "d",
      "e",
      "f",
      "g",
      "h",
      "i",
      "j",
      "k",
      "l",
      "m",
      "n",
      "o",
      "p",
      "q",
      "r",
      "s",
      "t",
      "u",
      "v",
      "w",
      "x",
      "y",
      "z",
      "left_bracket",
      "backslash",
      "right_bracket",
      "grave_accent",
      "world_1",
      "world_2",
      "escape",
      "enter",
      "tab",
      "backspace",
      "insert",
      "delete",
      "right",
      "left",
      "down",
      "up",
      "page_up",
      "page_down",
      "home",
      "end",
      "caps_lock",
      "scroll_lock",
      "num_lock",
      "print_screen",
      "pause",
      "f1",
      "f2",
      "f3",
      "f4",
      "f5",
      "f6",
      "f7",
      "f8",
      "f9",
      "f10",
      "f11",
      "f12",
      "f13",
      "f14",
      "f15",
      "f16",
      "f17",
      "f18",
      "f19",
      "f20",
      "f21",
      "f22",
      "f23",
      "f24",
      "f25",
      "kp_0",
      "kp_1",
      "kp_2",
      "kp_3",
      "kp_4",
      "kp_5",
      "kp_6",
      "kp_7",
      "kp_8",
      "kp_9",
      "kp_decimal",
      "kp_divide",
      "kp_multiply",
      "kp_subtract",
      "kp_add",
      "kp_enter",
      "kp_equal",
      "left_shift",
      "left_control",
      "left_alt",
      "left_super",
      "right_shift",
      "right_control",
      "right_alt",
      "right_super",
      "menu",
      "last",
      "none"
   };
   private static final Collection<String> EXAMPLES = Arrays.asList("space", "right_shift", "a", "b", "1", "2");

   public static KeybindingArgumentType keybinding() {
      return new KeybindingArgumentType();
   }

   public KeybindingArgumentType.Keybinding parse(StringReader reader) throws CommandSyntaxException {
      String key = reader.canRead() ? reader.readString() : "";
      if (key.equalsIgnoreCase("none")) {
         return new KeybindingArgumentType.Keybinding(-1, "none");
      } else {
         try {
            MethodHandle handle = MethodHandles.publicLookup().findStaticGetter(GLFW.class, "GLFW_KEY_" + key.toUpperCase(Locale.ENGLISH), int.class);
            int keybinding = (int)handle.invoke();
            return new KeybindingArgumentType.Keybinding(keybinding, InputUtil.fromKeyCode(keybinding, 0).getLocalizedText().getString());
         } catch (Throwable var5) {
            throw INVALID_KEY.createWithContext(reader, key);
         }
      }
   }

   public static KeybindingArgumentType.Keybinding getKeybinding(CommandContext<CommandSource> context, String key) {
      return (KeybindingArgumentType.Keybinding)context.getArgument(key, KeybindingArgumentType.Keybinding.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(KEYS, builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public record Keybinding(int key, String name) {
   }
}
