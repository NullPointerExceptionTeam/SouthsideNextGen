package dev.southside.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.southside.utils.ChatAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.BuiltinRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class Command implements ChatAdapter, Comparable<Command> {
   protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
   protected static final int SUCCESS = 1;
   public final String name;
   public final String description;
   public final List<String> aliases = new ArrayList<>();

   public Command(String name, String description, String... aliases) {
      this.name = name;
      this.description = description;
      Collections.addAll(this.aliases, aliases);
   }

   protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected static LiteralArgumentBuilder<CommandSource> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
      this.register(dispatcher, this.name);

      for (String alias : this.aliases) {
         this.register(dispatcher, alias);
      }
   }

   public void register(CommandDispatcher<CommandSource> dispatcher, String name) {
      LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
      this.build(builder);
      dispatcher.register(builder);
   }

   public abstract void build(LiteralArgumentBuilder<CommandSource> var1);

   public int compareTo(@NotNull Command o) {
      return this.name.compareTo(o.name);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Command module = (Command)o;
         return Objects.equals(this.name, module.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public List<String> getAliases() {
      return this.aliases;
   }
}
