package dev.southside.command.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.Southside;
import dev.southside.module.Module;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class ModuleArgumentType implements ArgumentType<Module> {
   private static final ModuleArgumentType INSTANCE = new ModuleArgumentType();
   private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(
      name -> class_2561.method_43470("Module with name " + name + " doesn't exist.")
   );
   private static final Collection<String> EXAMPLES = Southside.get()
      .getModuleManager()
      .getModules()
      .stream()
      .limit(3L)
      .map(Module::getReachableName)
      .collect(Collectors.toList());

   private ModuleArgumentType() {
   }

   public static ModuleArgumentType create() {
      return INSTANCE;
   }

   public static Module get(CommandContext<?> context) {
      return (Module)context.getArgument("module", Module.class);
   }

   public Module parse(StringReader reader) throws CommandSyntaxException {
      String argument = reader.readString();
      Module module = Southside.get().getModuleManager().get(argument.replaceAll("-", " "));
      if (module == null) {
         throw NO_SUCH_MODULE.create(argument);
      } else {
         return module;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return class_2172.method_9264(Southside.get().getModuleManager().getModules().stream().map(Module::getReachableName), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
