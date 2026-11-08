package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class ConfigsArgumentType implements ArgumentType<String> {
   private static final ConfigsArgumentType INSTANCE = new ConfigsArgumentType();
   private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(
      name -> class_2561.method_43470("Config with name " + name + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = Southside.get()
      .getConfigManager()
      .configs
      .stream()
      .limit(3L)
      .map(c -> c.name)
      .collect(Collectors.toList());

   private ConfigsArgumentType() {
   }

   public static ConfigsArgumentType create() {
      return INSTANCE;
   }

   public static String get(CommandContext<?> context) {
      return (String)context.getArgument("config", String.class);
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String argument = reader.readString();
      if (!Southside.get().getConfigManager().has(argument)) {
         throw NO_SUCH_MODULE.create(argument);
      } else {
         return argument;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9264(Southside.get().getConfigManager().configs.stream().map(c -> c.name), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
