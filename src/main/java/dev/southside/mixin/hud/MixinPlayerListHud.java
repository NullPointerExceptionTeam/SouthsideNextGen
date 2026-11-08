package dev.southside.mixin.hud;

import dev.southside.Southside;
import dev.southside.manager.FriendManager;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_355.class})
public abstract class MixinPlayerListHud {
   @Inject(
      method = {"method_1918(Lnet/minecraft/class_640;)Lnet/minecraft/class_2561;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$addStatusTag(class_640 entry, CallbackInfoReturnable<class_2561> cir) {
      class_2561 original = (class_2561)cir.getReturnValue();
      if (original != null) {
         Southside southside = Southside.get();
         class_310 client = class_310.method_1551();
         if (southside != null && client != null) {
            FriendManager friendManager = southside.getFriendManager();
            UUID uuid = entry.method_2966() != null ? entry.method_2966().getId() : null;
            String name = entry.method_2966() != null ? entry.method_2966().getName() : null;
            class_1657 player = client.field_1687 != null && uuid != null ? client.field_1687.method_18470(uuid) : null;
            boolean isBot = player != null && AntiBot.isBot(player);
            boolean isFriend = friendManager != null && name != null && friendManager.isFriend(name);
            boolean isTeam = player != null && Teams.isTeam(player);
            if (isBot || isFriend || isTeam) {
               class_5250 decorated = class_2561.method_43473();
               if (isBot) {
                  decorated.method_10852(class_2561.method_43470("[Bot] ").method_27692(class_124.field_1061));
               }

               if (isFriend) {
                  decorated.method_10852(class_2561.method_43470("[Friend] ").method_27692(class_124.field_1060));
               }

               if (isTeam) {
                  decorated.method_10852(class_2561.method_43470("[Team] ").method_27692(class_124.field_1060));
               }

               cir.setReturnValue(decorated.method_10852(original.method_27661()));
            }
         }
      }
   }
}
