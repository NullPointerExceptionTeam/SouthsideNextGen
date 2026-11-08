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
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_7157;
import net.minecraft.class_7887;
import org.jetbrains.annotations.NotNull;

public abstract class Command implements ChatAdapter, Comparable<Command> {
   protected static final class_7157 REGISTRY_ACCESS = class_2170.method_46732(class_7887.method_46817());
   protected static final int SUCCESS = 1;
   public final String name;
   public final String description;
   public final List<String> aliases = new ArrayList<>();

   public Command(String name, String description, String... aliases) {
      this.name = name;
      this.description = description;
      Collections.addAll(this.aliases, aliases);
   }

   protected static <T> RequiredArgumentBuilder<class_2172, T> argument(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected static LiteralArgumentBuilder<class_2172> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   public final void registerTo(CommandDispatcher<class_2172> dispatcher) {
      this.register(dispatcher, this.name);

      for (String alias : this.aliases) {
         this.register(dispatcher, alias);
      }
   }

   public void register(CommandDispatcher<class_2172> dispatcher, String name) {
      LiteralArgumentBuilder<class_2172> builder = LiteralArgumentBuilder.literal(name);
      this.build(builder);
      dispatcher.register(builder);
   }

   public abstract void build(LiteralArgumentBuilder<class_2172> var1);

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
