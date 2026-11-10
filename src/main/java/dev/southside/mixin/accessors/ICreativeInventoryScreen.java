package dev.southside.mixin.accessors;

import net.minecraft.item.ItemGroup;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({CreativeInventoryScreen.class})
public interface ICreativeInventoryScreen {
   @Accessor("selectedTab")
   static ItemGroup getSelectedTab() {
      return null;
   }
}
