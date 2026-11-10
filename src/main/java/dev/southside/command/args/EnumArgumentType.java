package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class EnumArgumentType<T extends Enum<?>> implements ArgumentType<T> {
   private static final DynamicCommandExceptionType NO_SUCH_TYPE = new DynamicCommandExceptionType(
      o -> Text.literal(o + " is not a valid argument.")
   );
   private T[] values;

   public EnumArgumentType(T defaultValue) {
      try {
         this.values = (T[])((Enum[])defaultValue.getClass().getMethod("values").invoke(null));
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException var3) {
         var3.printStackTrace();
      }
   }

   public static <T extends Enum<?>> EnumArgumentType<T> enumArgument(T defaultValue) {
      return new EnumArgumentType<>(defaultValue);
   }

   public static <T extends Enum<?>> T getEnum(CommandContext<?> context, String name, T defaultValue) {
      return (T)context.getArgument(name, defaultValue.getClass());
   }

   public T parse(StringReader reader) throws CommandSyntaxException {
      String argument = reader.readString();

      for (T t : this.values) {
         if (t.toString().equals(argument)) {
            return t;
         }
      }

      throw NO_SUCH_TYPE.create(argument);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Arrays.<Enum>stream(this.values).map(Enum::toString), builder);
   }
}
