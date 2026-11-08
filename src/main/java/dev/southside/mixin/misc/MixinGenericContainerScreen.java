package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.manager.ChestManager;
import dev.southside.module.impl.player.StealerN;
import dev.southside.render.ProjectionUtil;
import net.minecraft.class_1661;
import net.minecraft.class_1707;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3936;
import net.minecraft.class_465;
import net.minecraft.class_476;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_476.class})
public abstract class MixinGenericContainerScreen extends class_465<class_1707> implements class_3936<class_1707> {
   protected MixinGenericContainerScreen(class_1707 container, class_1661 playerInventory, class_2561 text) {
      super(container, playerInventory, text);
   }

   @Inject(
      method = {"method_25394(Lnet/minecraft/class_332;IIF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      StealerN stealer = Southside.INSTANCE.getModuleManager().get(StealerN.class);
      if (stealer.isEnabled() && stealer.silently.getValue()) {
         ci.cancel();
         this.drawContainerContents(context);
      }
   }

   @Unique
   private void drawContainerContents(class_332 context) {
      if (this.field_2797 != null && Southside.INSTANCE.getModuleManager() != null) {
         ChestManager manager = Southside.get().getChestManager();
         class_2338 stealing = manager.getStealingChest();
         if (stealing != null && ProjectionUtil.worldScreenPos(stealing.method_46558()) != null) {
            int containerX = (int)ProjectionUtil.worldScreenPos(stealing.method_46558()).field_1343;
            int containerY = (int)ProjectionUtil.worldScreenPos(stealing.method_46558()).field_1342;
            int slotSize = 18;
            int slotsPerRow = 9;
            int containerWidth = slotsPerRow * slotSize + 10;
            int containerHeight = (((class_1707)this.field_2797).field_7761.size() / slotsPerRow + 1) * slotSize + 10;
            context.method_25294(containerX, containerY, containerX + containerWidth, containerY + containerHeight, Integer.MIN_VALUE);
            String title = "Container Contents";
            context.method_25303(this.field_22793, title, containerX + 5, containerY + 5, 16777215);
            int separatorY = containerY + 20 + ((class_1707)this.field_2797).method_17388() * slotSize;
            context.method_25294(containerX + 5, separatorY, containerX + 5 + slotsPerRow * slotSize, separatorY + 1, -1);

            for (int i = 0; i < ((class_1707)this.field_2797).field_7761.size(); i++) {
               class_1735 slot = (class_1735)((class_1707)this.field_2797).field_7761.get(i);
               class_1799 stack = slot.method_7677();
               if (!stack.method_7960()) {
                  int row = i / slotsPerRow;
                  int col = i % slotsPerRow;
                  int x = containerX + 5 + col * slotSize;
                  int y = containerY + 20 + row * slotSize;
                  context.method_51427(stack, x, y);
                  context.method_51431(this.field_22793, stack, x, y);
               }
            }
         }
      }
   }
}
