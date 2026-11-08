package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.StickerArgumentType;
import dev.southside.manager.StickerManager;
import java.util.List;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

public class StickersCommand extends Command {
   public StickersCommand() {
      super("stickers", "Manage HUD stickers", "sticker");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.executes(context -> {
         this.msg("§eUsage:");
         this.msg("§7  .stickers list §8- List available stickers");
         this.msg("§7  .stickers use <name> §8- Show a sticker");
         this.msg("§7  .stickers off §8- Hide current sticker");
         this.msg("§7  .stickers reload §8- Reload stickers folder");
         return 1;
      });
      builder.then(literal("list").executes(context -> {
         StickerManager manager = Southside.get().getStickerManager();
         List<String> stickers = manager.getAvailableStickers();
         if (stickers.isEmpty()) {
            this.msg("§cNo stickers found. Place .png/.gif files in Southside-nextgen/stickers/");
         } else {
            this.msg("§eAvailable stickers (" + stickers.size() + "):");
            String active = manager.getActiveStickerName();

            for (String name : stickers) {
               if (name.equals(active)) {
                  this.msg("§a  ▶ " + name + " §7(active)");
               } else {
                  this.msg("§7  • " + name);
               }
            }
         }

         return 1;
      }));
      builder.then(literal("use").then(argument("name", StickerArgumentType.create()).executes(context -> {
         String name = ((String)context.getArgument("name", String.class)).trim();
         StickerManager manager = Southside.get().getStickerManager();
         List<String> available = manager.getAvailableStickers();
         if (available.contains(name)) {
            manager.use(name);
            this.msg("§aSticker set to: §f" + name);
         } else {
            this.msg("§cSticker not found: §f" + name);
            this.msg("§7Use .stickers list to see available stickers");
         }

         return 1;
      })));
      builder.then(literal("off").executes(context -> {
         Southside.get().getStickerManager().off();
         this.msg("§7Sticker disabled");
         return 1;
      }));
      builder.then(literal("reload").executes(context -> {
         Southside.get().getStickerManager().reload();
         List<String> stickers = Southside.get().getStickerManager().getAvailableStickers();
         this.msg("§aReloaded! Found " + stickers.size() + " sticker(s)");
         return 1;
      }));
   }

   private void msg(String text) {
      if (MinecraftAccessor.mc.field_1724 != null) {
         MinecraftAccessor.mc.field_1724.method_7353(class_2561.method_43470(text), false);
      }
   }
}
