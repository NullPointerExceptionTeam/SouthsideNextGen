package dev.southside.mixin.hud;

import dev.southside.Southside;
import dev.southside.manager.FriendManager;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import java.util.UUID;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListHud.class})
public abstract class MixinPlayerListHud {
   @Inject(
      method = {"getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void southside$addStatusTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
      Text original = (Text)cir.getReturnValue();
      if (original != null) {
         Southside southside = Southside.get();
         MinecraftClient client = MinecraftClient.getInstance();
         if (southside != null && client != null) {
            FriendManager friendManager = southside.getFriendManager();
            UUID uuid = entry.getProfile() != null ? entry.getProfile().getId() : null;
            String name = entry.getProfile() != null ? entry.getProfile().getName() : null;
            PlayerEntity player = client.world != null && uuid != null ? client.world.getPlayerByUuid(uuid) : null;
            boolean isBot = player != null && AntiBot.isBot(player);
            boolean isFriend = friendManager != null && name != null && friendManager.isFriend(name);
            boolean isTeam = player != null && Teams.isTeam(player);
            if (isBot || isFriend || isTeam) {
               MutableText decorated = Text.empty();
               if (isBot) {
                  decorated.append(Text.literal("[Bot] ").formatted(Formatting.RED));
               }

               if (isFriend) {
                  decorated.append(Text.literal("[Friend] ").formatted(Formatting.GREEN));
               }

               if (isTeam) {
                  decorated.append(Text.literal("[Team] ").formatted(Formatting.GREEN));
               }

               cir.setReturnValue(decorated.append(original.copy()));
            }
         }
      }
   }
}
