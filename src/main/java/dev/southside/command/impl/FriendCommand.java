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
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;

public class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "管理好友列表", "f");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> builder) {
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

      if (MinecraftAccessor.mc.getNetworkHandler() != null) {
         List<String> onlinePlayers = MinecraftAccessor.mc
            .getNetworkHandler()
            .getPlayerList()
            .stream()
            .<GameProfile>map(PlayerListEntry::getProfile)
            .map(profile -> profile.getName())
            .filter(name -> name != null && !name.isBlank())
            .filter(name -> MinecraftAccessor.mc.player == null || !name.equalsIgnoreCase(MinecraftAccessor.mc.player.getName().getString()))
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

   private MutableText friendLine(String name) {
      return Text.empty()
         .append(Text.literal("- ").formatted(Formatting.GRAY))
         .append(Text.literal(name).formatted(Formatting.GREEN))
         .append(Text.literal(" "))
         .append(this.actionButton("删除", Formatting.RED, ".friend del " + name, "点击删除好友 " + name));
   }

   private MutableText addLine(String name) {
      return Text.empty()
         .append(Text.literal("- ").formatted(Formatting.GRAY))
         .append(Text.literal(name).formatted(Formatting.WHITE))
         .append(Text.literal(" "))
         .append(this.actionButton("添加", Formatting.GREEN, ".friend add " + name, "点击添加好友 " + name));
   }

   private MutableText actionButton(String label, Formatting color, String command, String hoverText) {
      return Text.literal("[" + label + "]")
         .setStyle(
            Style.EMPTY
               .withColor(color)
               .withClickEvent(new ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, command))
               .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal(hoverText)))
         );
   }

   private CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
      if (MinecraftAccessor.mc.getNetworkHandler() == null) {
         return Suggestions.empty();
      } else {
         FriendManager friendManager = Southside.get().getFriendManager();
         Stream<String> names = MinecraftAccessor.mc
            .getNetworkHandler()
            .getPlayerList()
            .stream()
            .<GameProfile>map(PlayerListEntry::getProfile)
            .map(profile -> profile.getName())
            .filter(name -> name != null && !name.isBlank())
            .filter(name -> MinecraftAccessor.mc.player == null || !name.equalsIgnoreCase(MinecraftAccessor.mc.player.getName().getString()))
            .filter(name -> !friendManager.isFriend(name))
            .sorted(String.CASE_INSENSITIVE_ORDER);
         return CommandSource.suggestMatching(names, builder);
      }
   }

   private CompletableFuture<Suggestions> suggestFriends(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
      Stream<String> names = Southside.get().getFriendManager().getFriends().stream().sorted(String.CASE_INSENSITIVE_ORDER);
      return CommandSource.suggestMatching(names, builder);
   }
}
