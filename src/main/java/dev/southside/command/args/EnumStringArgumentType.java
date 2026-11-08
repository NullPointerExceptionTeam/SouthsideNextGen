package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class EnumStringArgumentType implements ArgumentType<String> {
   private static final DynamicCommandExceptionType INVALID_OPTION = new DynamicCommandExceptionType(
      name -> class_2561.method_43470(name + " is not a valid option.")
   );
   private final Collection<String> EXAMPLES;

   public EnumStringArgumentType(Collection<String> examples) {
      this.EXAMPLES = examples;
   }

   public static String getString(CommandContext<?> context, String name) {
      return (String)context.getArgument(name, String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String arg = reader.readUnquotedString();
      if (!this.EXAMPLES.contains(arg)) {
         throw INVALID_OPTION.create(arg);
      } else {
         return arg;
      }
   }

   public Collection<String> getExamples() {
      return this.EXAMPLES;
   }

   @Override
   public String toString() {
      return "string()";
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9265(this.EXAMPLES, builder);
   }
}
