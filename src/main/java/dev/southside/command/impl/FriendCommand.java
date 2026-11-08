package dev.southside.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.manager.FriendManager;
import dev.southside.utils.ChatUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "管理好友列表", "f");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.executes(context -> this.listFriends());
      builder.then(literal("list").executes(context -> this.listFriends()));
      builder.then(
         literal("add")
            .then(
               argument("name", StringArgumentType.word())
                  .suggests(this::suggestOnlinePlayers)
                  .executes(context -> this.addFriend(StringArgumentType.getString(context, "name")))
            )
      );
      builder.then(
         literal("del")
            .then(
               argument("name", StringArgumentType.word())
                  .suggests(this::suggestFriends)
                  .executes(context -> this.removeFriend(StringArgumentType.getString(context, "name")))
            )
      );
   }

   private int listFriends() {
      FriendManager friendManager = Southside.get().getFriendManager();
      if (friendManager.getFriends().isEmpty()) {
         ChatUtils.info("好友列表为空。");
      } else {
         ChatUtils.info("好友列表 (" + friendManager.getFriends().size() + "):");
         friendManager.getFriends().stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(name -> ChatUtils.info(this.friendLine(name)));
      }

      if (MinecraftAccessor.mc.method_1562() != null) {
         List<String> onlinePlayers = MinecraftAccessor.mc
            .method_1562()
            .method_2880()
            .stream()
            .<GameProfile>map(class_640::method_2966)
            .map(profile -> profile.getName())
            .filter(name -> name != null && !name.isBlank())
            .filter(name -> MinecraftAccessor.mc.field_1724 == null || !name.equalsIgnoreCase(MinecraftAccessor.mc.field_1724.method_5477().getString()))
            .filter(name -> !friendManager.isFriend(name))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
         if (!onlinePlayers.isEmpty()) {
            ChatUtils.info("在线玩家，可点击添加:");
            onlinePlayers.forEach(name -> ChatUtils.info(this.addLine(name)));
         }
      }

      return 1;
   }

   private int addFriend(String name) {
      FriendManager friendManager = Southside.get().getFriendManager();
      if (friendManager.isFriend(name)) {
         ChatUtils.warning(name + " 已经在好友列表中。");
         return 1;
      } else {
         friendManager.add(name);
         ChatUtils.success("已添加好友: " + name);
         return 1;
      }
   }

   private int removeFriend(String name) {
      FriendManager friendManager = Southside.get().getFriendManager();
      if (!friendManager.isFriend(name)) {
         ChatUtils.warning("好友不存在: " + name);
         return 1;
      } else {
         friendManager.remove(name);
         ChatUtils.success("已删除好友: " + name);
         return 1;
      }
   }

   private class_5250 friendLine(String name) {
      return class_2561.method_43473()
         .method_10852(class_2561.method_43470("- ").method_27692(class_124.field_1080))
         .method_10852(class_2561.method_43470(name).method_27692(class_124.field_1060))
         .method_10852(class_2561.method_43470(" "))
         .method_10852(this.actionButton("删除", class_124.field_1061, ".friend del " + name, "点击删除好友 " + name));
   }

   private class_5250 addLine(String name) {
      return class_2561.method_43473()
         .method_10852(class_2561.method_43470("- ").method_27692(class_124.field_1080))
         .method_10852(class_2561.method_43470(name).method_27692(class_124.field_1068))
         .method_10852(class_2561.method_43470(" "))
         .method_10852(this.actionButton("添加", class_124.field_1060, ".friend add " + name, "点击添加好友 " + name));
   }

   private class_5250 actionButton(String label, class_124 color, String command, String hoverText) {
      return class_2561.method_43470("[" + label + "]")
         .method_10862(
            class_2583.field_24360
               .method_10977(color)
               .method_10958(new class_2558(class_2559.field_11750, command))
               .method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470(hoverText)))
         );
   }

   private CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<class_2172> context, SuggestionsBuilder builder) {
      if (MinecraftAccessor.mc.method_1562() == null) {
         return Suggestions.empty();
      } else {
         FriendManager friendManager = Southside.get().getFriendManager();
         Stream<String> names = MinecraftAccessor.mc
            .method_1562()
            .method_2880()
            .stream()
            .<GameProfile>map(class_640::method_2966)
            .map(profile -> profile.getName())
            .filter(name -> name != null && !name.isBlank())
            .filter(name -> MinecraftAccessor.mc.field_1724 == null || !name.equalsIgnoreCase(MinecraftAccessor.mc.field_1724.method_5477().getString()))
            .filter(name -> !friendManager.isFriend(name))
            .sorted(String.CASE_INSENSITIVE_ORDER);
         return class_2172.method_9264(names, builder);
      }
   }

   private CompletableFuture<Suggestions> suggestFriends(CommandContext<class_2172> context, SuggestionsBuilder builder) {
      Stream<String> names = Southside.get().getFriendManager().getFriends().stream().sorted(String.CASE_INSENSITIVE_ORDER);
      return class_2172.method_9264(names, builder);
   }
}
