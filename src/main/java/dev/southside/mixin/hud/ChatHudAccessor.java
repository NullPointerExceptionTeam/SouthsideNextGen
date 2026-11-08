package dev.southside.mixin.hud;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_338;
import net.minecraft.class_303.class_7590;
import net.minecraft.class_7591.class_7592;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_338.class})
public interface ChatHudAccessor {
   @Accessor("field_2062")
   class_310 getClient();

   @Accessor("field_2061")
   List<class_303> getMessages();

   @Accessor("field_2064")
   List<class_7590> getVisibleMessages();

   @Accessor("field_2066")
   int getScrolledLines();

   @Accessor("field_2066")
   void setScrolledLines(int var1);

   @Accessor("field_2067")
   boolean getHasUnreadNewMessages();

   @Accessor("field_2067")
   void setHasUnreadNewMessages(boolean var1);

   @Invoker("method_44722")
   double invokeToChatLineX(double var1);

   @Invoker("method_44724")
   double invokeToChatLineY(double var1);

   @Invoker("method_23677")
   boolean invokeIsChatHidden();

   @Invoker("method_44719")
   void invokeDrawIndicatorIcon(class_332 var1, int var2, int var3, class_7592 var4);

   @Invoker("method_44720")
   int invokeGetIndicatorX(class_7590 var1);

   @Invoker("method_19348")
   static double invokeGetMessageOpacityMultiplier(int age) {
      return 0.0;
   }

   @Invoker("method_1819")
   boolean invokeIsChatFocused();

   @Invoker("method_44752")
   int invokeGetLineHeight();

   @Invoker("method_1814")
   double invokeGetChatScale();

   @Invoker("method_1811")
   int invokeGetWidth();

   @Invoker("method_1810")
   int invokeGetHeight();

   @Invoker("method_1813")
   int invokeGetVisibleLineCount();

   @Invoker("method_1812")
   void invokeAddMessage(class_2561 var1);

   @Invoker("method_45588")
   int invokeGetMessageIndex(double var1, double var3);

   @Invoker("method_27146")
   boolean invokeMouseClicked(double var1, double var3);
}
