package dev.southside.mixin.gui;

import dev.southside.gui.netease.NeteaseScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_442.class})
public abstract class MixinTitleScreen extends class_437 {
   @Unique
   private static final int SOUTH_SIDE_OPTIONS_OFFSET = 24;
   @Unique
   private static final List<class_2561> SHIFTS = List.of();

   protected MixinTitleScreen(class_2561 title) {
      super(title);
   }

   @Inject(
      method = {"method_25426()V"},
      at = {@At("TAIL")}
   )
   private void modifyAll(CallbackInfo ci) {
      class_4185 multiplayer = null;
      List<class_4185> toRemove = new ArrayList<>();

      for (class_4068 drawable : this.field_33816) {
         if (drawable instanceof class_4185 b) {
            class_2561 message = b.method_25369();
            if (SHIFTS.contains(message)) {
               b.method_48229(b.method_46426(), b.method_46427() + 12);
            } else if (message.equals(class_2561.method_43471("menu.online"))) {
               toRemove.add(b);
            } else if (message.equals(class_2561.method_43471("menu.multiplayer"))) {
               multiplayer = b;
            }
         }
      }

      for (class_4185 drawablex : toRemove) {
         this.method_37066(drawablex);
      }

      if (multiplayer != null) {
         int y = multiplayer.method_46427();
         int spacingY = 24;
         this.method_37063(
            class_4185.method_46430(class_2561.method_43470("NEL"), button -> this.field_22787.method_1507(NeteaseScreen.create()))
               .method_46434(this.field_22789 / 2 - 100, y + spacingY, 200, 20)
               .method_46431()
         );
      }
   }
}
