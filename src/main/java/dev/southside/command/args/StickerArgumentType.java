package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2172;

public class StickerArgumentType implements ArgumentType<String> {
   private static final StickerArgumentType INSTANCE = new StickerArgumentType();

   private StickerArgumentType() {
   }

   public static StickerArgumentType create() {
      return INSTANCE;
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      return reader.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9265(Southside.get().getStickerManager().getAvailableStickers(), builder);
   }

   public Collection<String> getExamples() {
      return List.of("sh1t.gif", "example.png");
   }
}
