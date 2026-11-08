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
import net.minecraft.class_2172;
import net.minecraft.class_2561;
import net.minecraft.class_640;

public class PlayerListEntryArgumentType implements ArgumentType<class_640> {
   private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(
      name -> class_2561.method_43470("Player list entry with name " + name + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = List.of("cao_jia_hao");

   public static PlayerListEntryArgumentType create() {
      return new PlayerListEntryArgumentType();
   }

   public static class_640 get(CommandContext<?> context) {
      return (class_640)context.getArgument("player", class_640.class);
   }

   public class_640 parse(StringReader reader) throws CommandSyntaxException {
      String argument = reader.readString();
      class_640 playerListEntry = null;

      for (class_640 p : MinecraftAccessor.mc.method_1562().method_2880()) {
         if (p.method_2966().getName().equalsIgnoreCase(argument)) {
            playerListEntry = p;
            break;
         }
      }

      if (playerListEntry == null) {
         throw NO_SUCH_PLAYER.create(argument);
      } else {
         return playerListEntry;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9264(
         MinecraftAccessor.mc.method_1562().method_2880().stream().map(playerListEntry -> playerListEntry.method_2966().getName()), builder
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
