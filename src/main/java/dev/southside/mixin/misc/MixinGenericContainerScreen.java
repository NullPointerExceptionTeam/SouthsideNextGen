package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.manager.ChestManager;
import dev.southside.module.impl.player.StealerN;
import dev.southside.render.ProjectionUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GenericContainerScreen.class})
public abstract class MixinGenericContainerScreen extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
   protected MixinGenericContainerScreen(GenericContainerScreenHandler container, PlayerInventory playerInventory, Text text) {
      super(container, playerInventory, text);
   }

   @Inject(
      method = {"render(Lnet/minecraft/client/gui/DrawContext;IIF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      StealerN stealer = Southside.INSTANCE.getModuleManager().get(StealerN.class);
      if (stealer.isEnabled() && stealer.silently.getValue()) {
         ci.cancel();
         this.drawContainerContents(context);
      }
   }

   @Unique
   private void drawContainerContents(DrawContext context) {
      if (this.handler != null && Southside.INSTANCE.getModuleManager() != null) {
         ChestManager manager = Southside.get().getChestManager();
         BlockPos stealing = manager.getStealingChest();
         if (stealing != null && ProjectionUtil.worldScreenPos(stealing.toCenterPos()) != null) {
            int containerX = (int)ProjectionUtil.worldScreenPos(stealing.toCenterPos()).x;
            int containerY = (int)ProjectionUtil.worldScreenPos(stealing.toCenterPos()).y;
            int slotSize = 18;
            int slotsPerRow = 9;
            int containerWidth = slotsPerRow * slotSize + 10;
            int containerHeight = (((GenericContainerScreenHandler)this.handler).slots.size() / slotsPerRow + 1) * slotSize + 10;
            context.fill(containerX, containerY, containerX + containerWidth, containerY + containerHeight, Integer.MIN_VALUE);
            String title = "Container Contents";
            context.drawTextWithShadow(this.textRenderer, title, containerX + 5, containerY + 5, 16777215);
            int separatorY = containerY + 20 + ((GenericContainerScreenHandler)this.handler).getRows() * slotSize;
            context.fill(containerX + 5, separatorY, containerX + 5 + slotsPerRow * slotSize, separatorY + 1, -1);

            for (int i = 0; i < ((GenericContainerScreenHandler)this.handler).slots.size(); i++) {
               Slot slot = (Slot)((GenericContainerScreenHandler)this.handler).slots.get(i);
               ItemStack stack = slot.getStack();
               if (!stack.isEmpty()) {
                  int row = i / slotsPerRow;
                  int col = i % slotsPerRow;
                  int x = containerX + 5 + col * slotSize;
                  int y = containerY + 20 + row * slotSize;
                  context.drawItem(stack, x, y);
                  context.drawStackOverlay(this.textRenderer, stack, x, y);
               }
            }
         }
      }
   }
}
