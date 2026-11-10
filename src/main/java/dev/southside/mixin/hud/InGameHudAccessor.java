package dev.southside.mixin.hud;

import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({InGameHud.class})
public interface InGameHudAccessor {
   @Accessor("client")
   MinecraftClient getClient();

   @Accessor("SCOREBOARD_ENTRY_COMPARATOR")
   static Comparator<ScoreboardEntry> getScoreboardEntryComparator() {
      return null;
   }

   @Invoker("getTextRenderer")
   TextRenderer invokeGetTextRenderer();
}
