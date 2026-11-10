package dev.southside.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.command.Command;
import dev.southside.utils.misc.AIWalkController;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

public class GotoCommand extends Command {
   public GotoCommand() {
      super("goto", "走路到指定坐标");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(context -> {
         this.sendClientMessage("Usage: .goto <x> <y> <z>", Formatting.RED);
         return 1;
      });
      builder.then(
         argument("x", IntegerArgumentType.integer())
            .then(argument("y", IntegerArgumentType.integer()).then(argument("z", IntegerArgumentType.integer()).executes(context -> {
               int x = IntegerArgumentType.getInteger(context, "x");
               int y = IntegerArgumentType.getInteger(context, "y");
               int z = IntegerArgumentType.getInteger(context, "z");
               MinecraftAccessor.mc.execute(() -> AIWalkController.walkTo(new BlockPos(x, y, z)));
               return 1;
            })))
      );
      builder.then(literal("stop").executes(context -> {
         AIWalkController.stopWalking();
         return 1;
      }));
   }

   private void sendClientMessage(String message, Formatting color) {
      this.info(message, new Object[0]);
   }
}
