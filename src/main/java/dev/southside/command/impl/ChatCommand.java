package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.command.Command;
import dev.southside.utils.gpt.GPTAPIUtils;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class ChatCommand extends Command {
   private final AtomicLong lastRequestTime = new AtomicLong(0L);

   public ChatCommand() {
      super("chat", "Ask questions to sexy GPT");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(context -> {
         this.sendClientMessage("Usage: .chat <question> - Ask GPT a question", Formatting.RED);
         return 1;
      });
      builder.then(argument("question", StringArgumentType.greedyString()).executes(context -> {
         long time = this.lastRequestTime.get() + 6000L - System.currentTimeMillis();
         if (time > 0L) {
            this.sendClientMessage("Please wait " + time / 1000L + " more seconds before asking another question.", Formatting.RED);
            return 1;
         } else {
            this.lastRequestTime.set(System.currentTimeMillis());
            String question = (String)context.getArgument("question", String.class);
            this.sendClientMessage("Asking ChatGPT: " + question, Formatting.WHITE);
            new Thread(() -> {
               String response = GPTAPIUtils.callAPI(question);
               MinecraftAccessor.mc.execute(() -> {
                  String text = "[Anthropic GPT]";
                  this.sendClientMessage(text, Formatting.GRAY);
                  this.sendClientMessage(response + Formatting.UNDERLINE, Formatting.WHITE);
               });
            }).start();
            return 1;
         }
      }));
   }

   private void sendClientMessage(String message, Formatting color) {
      MinecraftAccessor.mc.player.sendMessage(Text.literal(message).formatted(color), false);
   }
}
