package dev.southside.mixin.hud;

import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.client.gui.hud.MessageIndicator.Icon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ChatHud.class})
public interface ChatHudAccessor {
   @Accessor("client")
   MinecraftClient getClient();

   @Accessor("messages")
   List<ChatHudLine> getMessages();

   @Accessor("visibleMessages")
   List<Visible> getVisibleMessages();

   @Accessor("scrolledLines")
   int getScrolledLines();

   @Accessor("scrolledLines")
   void setScrolledLines(int var1);

   @Accessor("hasUnreadNewMessages")
   boolean getHasUnreadNewMessages();

   @Accessor("hasUnreadNewMessages")
   void setHasUnreadNewMessages(boolean var1);

   @Invoker("toChatLineX")
   double invokeToChatLineX(double var1);

   @Invoker("toChatLineY")
   double invokeToChatLineY(double var1);

   @Invoker("isChatHidden")
   boolean invokeIsChatHidden();

   @Invoker("drawIndicatorIcon")
   void invokeDrawIndicatorIcon(DrawContext var1, int var2, int var3, Icon var4);

   @Invoker("getIndicatorX")
   int invokeGetIndicatorX(Visible var1);

   @Invoker("getMessageOpacityMultiplier")
   static double invokeGetMessageOpacityMultiplier(int age) {
      return 0.0;
   }

   @Invoker("isChatFocused")
   boolean invokeIsChatFocused();

   @Invoker("getLineHeight")
   int invokeGetLineHeight();

   @Invoker("getChatScale")
   double invokeGetChatScale();

   @Invoker("getWidth")
   int invokeGetWidth();

   @Invoker("getHeight")
   int invokeGetHeight();

   @Invoker("getVisibleLineCount")
   int invokeGetVisibleLineCount();

   @Invoker("addMessage")
   void invokeAddMessage(Text var1);

   @Invoker("getMessageIndex")
   int invokeGetMessageIndex(double var1, double var3);

   @Invoker("mouseClicked")
   boolean invokeMouseClicked(double var1, double var3);
}
