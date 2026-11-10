package dev.southside.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Lifecycle;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.command.impl.BindCommand;
import dev.southside.command.impl.ChatCommand;
import dev.southside.command.impl.ConfigsCommand;
import dev.southside.command.impl.FakePlayerCommand;
import dev.southside.command.impl.FriendCommand;
import dev.southside.command.impl.GotoCommand;
import dev.southside.command.impl.ReportCommand;
import dev.southside.command.impl.StickersCommand;
import dev.southside.command.impl.TestCommand;
import dev.southside.command.impl.ToggleCommand;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.network.ClientCommandSource;
import org.jetbrains.annotations.NotNull;

public class CommandManager {
   private static CommandManager INSTANCE;
   public static final CommandManager.CommandRegistry REGISTRY = new CommandManager.CommandRegistry();
   public final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher();
   public final CommandSource COMMAND_SOURCE = new ClientCommandSource(null, MinecraftAccessor.mc);
   public final List<Command> COMMANDS = new ArrayList<>();

   public static CommandManager get() {
      return INSTANCE;
   }

   public CommandManager() {
      INSTANCE = this;
   }

   public void init() {
      Southside.get().getEventBus().subscribe(this);
      this.add(new ChatCommand());
      this.add(new BindCommand());
      this.add(new ToggleCommand());
      this.add(new ConfigsCommand());
      this.add(new GotoCommand());
      this.add(new FakePlayerCommand());
      this.add(new TestCommand());
      this.add(new ReportCommand());
      this.add(new StickersCommand());
      this.add(new FriendCommand());
      this.COMMANDS.sort(Command::compareTo);
   }

   public void add(Command command) {
      this.COMMANDS.removeIf(existing -> existing.name.equals(command.name));
      command.registerTo(this.DISPATCHER);
      this.COMMANDS.add(command);
   }

   public void dispatch(String message) throws CommandSyntaxException {
      this.DISPATCHER.execute(message, this.COMMAND_SOURCE);
   }

   public Command get(String name) {
      for (Command command : this.COMMANDS) {
         if (command.name.equals(name)) {
            return command;
         }
      }

      return null;
   }

   public static class CommandRegistry extends SimpleRegistry<Command> {
      public CommandRegistry() {
         super(RegistryKey.ofRegistry(Identifier.of("southside", "commands")), Lifecycle.stable());
      }

      public int size() {
         return CommandManager.get().COMMANDS.size();
      }

      @NotNull
      public Iterator<Command> iterator() {
         return new CommandManager.CommandRegistry.CommandIterator();
      }

      private static class CommandIterator implements Iterator<Command> {
         private final Iterator<Command> iterator = Southside.get().getCommandManager().COMMANDS.iterator();

         @Override
         public boolean hasNext() {
            return this.iterator.hasNext();
         }

         public Command next() {
            return this.iterator.next();
         }
      }
   }
}
