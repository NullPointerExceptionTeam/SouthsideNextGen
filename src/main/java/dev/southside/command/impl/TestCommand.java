package dev.southside.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.render.ProgressBarOverlay;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;

public class TestCommand extends Command {
   public TestCommand() {
      super("test", "Collection of small test helpers.");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         ((LiteralArgumentBuilder)literal("progressbar").executes(context -> this.startDemo(20, "Demo Task")))
            .then(
               ((RequiredArgumentBuilder)argument("steps", IntegerArgumentType.integer(1))
                     .executes(context -> this.startDemo(IntegerArgumentType.getInteger(context, "steps"), null)))
                  .then(
                     argument("title", StringArgumentType.greedyString())
                        .executes(context -> this.startDemo(IntegerArgumentType.getInteger(context, "steps"), StringArgumentType.getString(context, "title")))
                  )
            )
      );
   }

   private int startDemo(int steps, @Nullable String title) {
      ProgressBarOverlay overlay = Southside.get().getProgressBarOverlay();
      if (overlay == null) {
         this.error("Progress bar overlay is not ready yet.", new Object[0]);
         return 1;
      } else {
         ProgressBarOverlay.ProgressHandle handle = overlay.show(steps, title);
         this.info("Displaying demo progress bar (" + steps + " steps).", new Object[0]);
         CompletableFuture.runAsync(() -> {
            for (int i = 1; i <= steps; i++) {
               if (!handle.isActive()) {
                  return;
               }

               handle.update(i);

               try {
                  Thread.sleep(200L);
               } catch (InterruptedException var4x) {
                  Thread.currentThread().interrupt();
                  return;
               }
            }

            handle.finish();
         });
         return 1;
      }
   }
}
