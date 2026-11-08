package dev.southside.mixin.misc;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.southside.command.CommandManager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2172;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import net.minecraft.class_4717.class_464;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({class_4717.class})
public abstract class MixinChatInputSuggestor {
   @Shadow
   @Final
   class_342 field_21599;
   @Shadow
   boolean field_21614;
   @Shadow
   private ParseResults<class_2172> field_21610;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private class_464 field_21612;

   @Shadow
   protected abstract void method_23937();

   @Inject(
      method = {"method_23934()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
         remap = false
      )},
      cancellable = true,
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   public void onRefresh(CallbackInfo ci, String string, StringReader reader) {
      String prefix = ".";
      int length = prefix.length();
      if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
         reader.setCursor(reader.getCursor() + length);
         if (this.field_21610 == null) {
            this.field_21610 = CommandManager.get().DISPATCHER.parse(reader, CommandManager.get().COMMAND_SOURCE);
         }

         int cursor = this.field_21599.method_1881();
         if (cursor >= 1 && (this.field_21612 == null || !this.field_21614)) {
            this.field_21611 = CommandManager.get().DISPATCHER.getCompletionSuggestions(this.field_21610, cursor);
            this.field_21611.thenRun(() -> {
               if (this.field_21611.isDone()) {
                  this.method_23937();
               }
            });
         }

         ci.cancel();
      }
   }
}
