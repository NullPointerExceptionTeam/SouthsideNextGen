package dev.southside.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.command.Command;
import dev.southside.utils.gpt.GPTAPIUtils;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class ChatCommand extends Command {
   private final AtomicLong lastRequestTime = new AtomicLong(0L);

   public ChatCommand() {
      super("chat", "Ask questions to sexy GPT");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.executes(context -> {
         this.sendClientMessage("Usage: .chat <question> - Ask GPT a question", class_124.field_1061);
         return 1;
      });
      builder.then(argument("question", StringArgumentType.greedyString()).executes(context -> {
         long time = this.lastRequestTime.get() + 6000L - System.currentTimeMillis();
         if (time > 0L) {
            this.sendClientMessage("Please wait " + time / 1000L + " more seconds before asking another question.", class_124.field_1061);
            return 1;
         } else {
            this.lastRequestTime.set(System.currentTimeMillis());
            String question = (String)context.getArgument("question", String.class);
            this.sendClientMessage("Asking ChatGPT: " + question, class_124.field_1068);
            new Thread(() -> {
               String response = GPTAPIUtils.callAPI(question);
               MinecraftAccessor.mc.execute(() -> {
                  String text = "[Anthropic GPT]";
                  this.sendClientMessage(text, class_124.field_1080);
                  this.sendClientMessage(response + class_124.field_1073, class_124.field_1068);
               });
            }).start();
            return 1;
         }
      }));
   }

   private void sendClientMessage(String message, class_124 color) {
      MinecraftAccessor.mc.field_1724.method_7353(class_2561.method_43470(message).method_27692(color), false);
   }
}
