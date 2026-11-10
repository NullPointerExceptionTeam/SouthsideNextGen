package dev.southside.mixin.gui;

import dev.southside.gui.netease.NeteaseScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class})
public abstract class MixinTitleScreen extends Screen {
   @Unique
   private static final int SOUTH_SIDE_OPTIONS_OFFSET = 24;
   @Unique
   private static final List<Text> SHIFTS = List.of();

   protected MixinTitleScreen(Text title) {
      super(title);
   }

   @Inject(
      method = {"init()V"},
      at = {@At("TAIL")}
   )
   private void modifyAll(CallbackInfo ci) {
      ButtonWidget multiplayer = null;
      List<ButtonWidget> toRemove = new ArrayList<>();

      for (Drawable drawable : this.drawables) {
         if (drawable instanceof ButtonWidget b) {
            Text message = b.getMessage();
            if (SHIFTS.contains(message)) {
               b.setPosition(b.getX(), b.getY() + 12);
            } else if (message.equals(Text.translatable("menu.online"))) {
               toRemove.add(b);
            } else if (message.equals(Text.translatable("menu.multiplayer"))) {
               multiplayer = b;
            }
         }
      }

      for (ButtonWidget drawablex : toRemove) {
         this.remove(drawablex);
      }

      if (multiplayer != null) {
         int y = multiplayer.getY();
         int spacingY = 24;
         this.addDrawableChild(
            ButtonWidget.builder(Text.literal("NEL"), button -> this.client.setScreen(NeteaseScreen.create()))
               .dimensions(this.width / 2 - 100, y + spacingY, 200, 20)
               .build()
         );
      }
   }
}
