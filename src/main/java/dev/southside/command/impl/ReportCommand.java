package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.command.Command;
import dev.southside.utils.player.ReportUtils;
import net.minecraft.class_2172;

public class ReportCommand extends Command {
   public ReportCommand() {
      super("report", "Report a player");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.then(argument("player_name", StringArgumentType.word()).executes(context -> {
         String name = StringArgumentType.getString(context, "player_name");
         ReportUtils.report(name);
         return 1;
      }));
   }
}
