package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class ClientPosArgumentType implements ArgumentType<Vec3d> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "~0.5 ~1 ~-5");
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public static ClientPosArgumentType pos() {
      return new ClientPosArgumentType();
   }

   public static Vec3d getPos(CommandContext<?> context, String name) {
      return (Vec3d)context.getArgument(name, Vec3d.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      if (context.getSource() instanceof CommandSource) {
         String string = builder.getRemaining();
         Object collection2 = ((CommandSource)context.getSource()).getBlockPositionSuggestions();
         return CommandSource.suggestPositions(string, (Collection)collection2, builder, CommandManager.getCommandValidator(this::parse));
      } else {
         return Suggestions.empty();
      }
   }

   public Vec3d parse(StringReader reader) throws CommandSyntaxException {
      int i = reader.getCursor();
      CoordinateArgument coordinateArgument = CoordinateArgument.parse(reader);
      if (reader.canRead() && reader.peek() == ' ') {
         reader.skip();
         CoordinateArgument coordinateArgument2 = CoordinateArgument.parse(reader);
         if (reader.canRead() && reader.peek() == ' ') {
            reader.skip();
            CoordinateArgument coordinateArgument3 = CoordinateArgument.parse(reader);
            double x = coordinateArgument.toAbsoluteCoordinate(mc.player.getX());
            double y = coordinateArgument2.toAbsoluteCoordinate(mc.player.getY());
            double z = coordinateArgument3.toAbsoluteCoordinate(mc.player.getZ());
            return new Vec3d(x, y, z);
         } else {
            reader.setCursor(i);
            throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
         }
      } else {
         reader.setCursor(i);
         throw Vec3ArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
      }
   }
}
