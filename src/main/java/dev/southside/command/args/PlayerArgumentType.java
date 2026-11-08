package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.MinecraftAccessor;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1657;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class PlayerArgumentType implements ArgumentType<class_1657> {
   private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(
      name -> class_2561.method_43470("Player with name " + name + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = List.of("cao_jia_hao");

   public static PlayerArgumentType create() {
      return new PlayerArgumentType();
   }

   public static class_1657 get(CommandContext<?> context) {
      return (class_1657)context.getArgument("player", class_1657.class);
   }

   public class_1657 parse(StringReader reader) throws CommandSyntaxException {
      String argument = reader.readString();
      class_1657 playerEntity = null;

      for (class_1657 p : MinecraftAccessor.mc.field_1687.method_18456()) {
         if (p.method_5477().getString().equalsIgnoreCase(argument)) {
            playerEntity = p;
            break;
         }
      }

      if (playerEntity == null) {
         throw NO_SUCH_PLAYER.create(argument);
      } else {
         return playerEntity;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9264(MinecraftAccessor.mc.field_1687.method_18456().stream().map(p -> p.method_5477().getString()), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
