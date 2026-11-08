package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.gui.animations.CubicBezier;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import dev.southside.mixin.hud.ChatHudAccessor;
import dev.southside.utils.render.animation.AnimationComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_7591;
import net.minecraft.class_303.class_7590;

public class ChatHud {
   private static final class_310 client = class_310.method_1551();
   private static final Map<Integer, AnimationComponent> messageAnimations = new HashMap<>();
   private static final Set<Integer> initializedMessageAnimations = new HashSet<>();
   private static final Map<Integer, Integer> messageFirstSeenTick = new HashMap<>();
   private static int lastPruneTick = -1;
   private static AnimationComponent overallBackgroundAnimation = null;
   private static AnimationComponent chatToggleAnimation = null;
   private static boolean wasChatOpen = false;
   private static final List<class_7590> currentMessages = new ArrayList<>();
   private static CubicBezier scrollPositionAnimation = null;
   private static int targetScrollLines = 0;
   private static final float SCROLL_SPEED = 3.0F;
   private static final float NEW_MESSAGE_FADE_IN_TICKS = 5.0F;
   private static final float SCISSOR_PADDING_X = 2.0F;
   private static final float SCISSOR_PADDING_TOP = 4.0F;
   private static final float SCISSOR_PADDING_BOTTOM = 3.0F;
   private static CubicBezier messageStackLiftAnimation = null;
   private static int lastNewestMessageKey = Integer.MIN_VALUE;
   private static float currentMessageStackLiftOffset = 0.0F;
   private static final float MESSAGE_STACK_LIFT_DURATION_MS = 180.0F;
   private static float lastScrollPosition = 0.0F;

   private static ChatHudAccessor getChatHudAccessor() {
      return (ChatHudAccessor)client.field_1705.method_1743();
   }

   public static void render(class_332 context, int currentTick, int mouseX, int mouseY) {
      ChatHudAccessor accessor = getChatHudAccessor();
      if (!accessor.invokeIsChatHidden()) {
         int visibleLineCount = accessor.invokeGetVisibleLineCount();
         int messageCount = accessor.getVisibleMessages().size();
         int scrolledLines = accessor.getScrolledLines();
         boolean isChatFocused = accessor.invokeIsChatFocused();
         boolean isChatOpen = isChatFocused || visibleLineCount > 0;
         if (wasChatOpen != isChatOpen) {
            if (chatToggleAnimation == null) {
               chatToggleAnimation = new AnimationComponent(200, 0.0F, 0.0F, wasChatOpen ? 1.0F : 0.0F, 0.0F, 0.0F);
            }

            chatToggleAnimation.updateTarget(200, 0.0F, 0.0F, isChatOpen ? 1.0F : 0.0F);
            wasChatOpen = isChatOpen;
         }

         if (targetScrollLines != scrolledLines) {
            targetScrollLines = scrolledLines;
            float[] easingPoints = new float[]{0.25F, 0.01F, 0.25F, 1.0F};
            scrollPositionAnimation = new CubicBezier(
               easingPoints,
               300.0F,
               scrollPositionAnimation != null && !scrollPositionAnimation.isFinished() ? scrollPositionAnimation.get() : lastScrollPosition,
               scrolledLines,
               false
            );
         }

         float currentScrollPosition = scrolledLines;
         if (scrollPositionAnimation != null && !scrollPositionAnimation.isFinished()) {
            currentScrollPosition = scrollPositionAnimation.get();
         }

         lastScrollPosition = currentScrollPosition;
         if (messageCount <= 0) {
            renderEmptyChat(context, accessor, chatToggleAnimation != null ? chatToggleAnimation.getAlpha() : 1.0F);
         } else {
            updateCurrentMessages(accessor, scrolledLines, visibleLineCount);
            pruneMessageAnimations(accessor, currentTick);
            updateMessageAnimations(currentTick);
            updateMessageStackLiftAnimation(accessor, scrolledLines);
            renderChatContent(
               context,
               accessor,
               currentTick,
               mouseX,
               mouseY,
               isChatFocused,
               isChatOpen,
               currentScrollPosition,
               scrolledLines,
               visibleLineCount,
               chatToggleAnimation != null ? chatToggleAnimation.getAlpha() : 1.0F
            );
         }
      }
   }

   private static void renderEmptyChat(class_332 context, ChatHudAccessor accessor, float chatToggleAlpha) {
      if (overallBackgroundAnimation != null) {
         overallBackgroundAnimation.updateBackgroundTarget(200, 0.0F, 0.0F, 0.0F);
      }

      float chatScale = (float)accessor.invokeGetChatScale();
      int windowHeight = context.method_51443();
      int chatBottomPosY = class_3532.method_15375((windowHeight - 40) / chatScale);
      context.method_51448().method_22903();
      context.method_51448().method_22905(chatScale, chatScale, 1.0F);
      context.method_51448().method_46416(4.0F, 0.0F, 0.0F);
      double backgroundOpacity = (Double)accessor.getClient().field_1690.method_42550().method_41753();
      if (overallBackgroundAnimation != null) {
         float animBackgroundWidth = overallBackgroundAnimation.getBackgroundWidth();
         float animBackgroundHeight = overallBackgroundAnimation.getBackgroundHeight();
         float animBackgroundAlpha = overallBackgroundAnimation.getBackgroundAlpha() * chatToggleAlpha;
         if (animBackgroundAlpha > 0.0F) {
            int animatedBackgroundColorAlpha = (int)(255.0 * backgroundOpacity * animBackgroundAlpha);
            context.method_25294(
               -4, chatBottomPosY - (int)animBackgroundHeight, (int)animBackgroundWidth + 8, chatBottomPosY, animatedBackgroundColorAlpha << 24
            );
         }
      }

      context.method_51448().method_22909();
   }

   private static void updateCurrentMessages(ChatHudAccessor accessor, int scrolledLines, int visibleLineCount) {
      currentMessages.clear();
      int endLine = scrolledLines + visibleLineCount;

      for (int i = scrolledLines; i < Math.min(endLine, accessor.getVisibleMessages().size()); i++) {
         if (i >= 0) {
            class_7590 visibleLine = accessor.getVisibleMessages().get(i);
            if (visibleLine != null) {
               currentMessages.add(visibleLine);
            }
         }
      }
   }

   private static AnimationComponent getOrCreateMessageAnimation(int messageKey, int currentTick) {
      AnimationComponent existing = messageAnimations.get(messageKey);
      if (existing != null) {
         return existing;
      } else {
         initializedMessageAnimations.add(messageKey);
         messageFirstSeenTick.put(messageKey, currentTick);
         AnimationComponent created = new AnimationComponent(800, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
         created.updateTarget(800, 0.0F, 0.0F, 1.0F);
         messageAnimations.put(messageKey, created);
         return created;
      }
   }

   private static void pruneMessageAnimations(ChatHudAccessor accessor, int currentTick) {
      if (lastPruneTick != currentTick) {
         lastPruneTick = currentTick;
         List<class_7590> allMessages = accessor.getVisibleMessages();
         if (allMessages.isEmpty()) {
            messageAnimations.clear();
            initializedMessageAnimations.clear();
            messageFirstSeenTick.clear();
         } else {
            Set<Integer> aliveKeys = new HashSet<>(allMessages.size());

            for (class_7590 line : allMessages) {
               if (line != null) {
                  aliveKeys.add(Objects.hash(line.comp_895(), line.comp_896()));
               }
            }

            messageAnimations.keySet().removeIf(key -> !aliveKeys.contains(key));
            initializedMessageAnimations.removeIf(key -> !aliveKeys.contains(key));
            messageFirstSeenTick.keySet().removeIf(key -> !aliveKeys.contains(key));
         }
      }
   }

   private static void updateMessageAnimations(int currentTick) {
      for (class_7590 visibleLine : currentMessages) {
         int messageKey = Objects.hash(visibleLine.comp_895(), visibleLine.comp_896());
         if (!initializedMessageAnimations.contains(messageKey)) {
            getOrCreateMessageAnimation(messageKey, currentTick);
         }
      }
   }

   private static void updateMessageStackLiftAnimation(ChatHudAccessor accessor, int scrolledLines) {
      if (accessor.getVisibleMessages().isEmpty()) {
         currentMessageStackLiftOffset = 0.0F;
         lastNewestMessageKey = Integer.MIN_VALUE;
      } else {
         class_7590 newest = accessor.getVisibleMessages().get(0);
         int newestMessageKey = Objects.hash(newest.comp_895(), newest.comp_896());
         if (lastNewestMessageKey != Integer.MIN_VALUE && newestMessageKey != lastNewestMessageKey && scrolledLines == 0) {
            float[] easingPoints = new float[]{0.22F, 1.0F, 0.36F, 1.0F};
            float liftStart = accessor.invokeGetLineHeight();
            messageStackLiftAnimation = new CubicBezier(easingPoints, 180.0F, liftStart, 0.0F, false);
         }

         lastNewestMessageKey = newestMessageKey;
         if (messageStackLiftAnimation != null && !messageStackLiftAnimation.isFinished()) {
            currentMessageStackLiftOffset = messageStackLiftAnimation.get();
         } else {
            currentMessageStackLiftOffset = 0.0F;
         }
      }
   }

   private static void renderChatContent(
      class_332 context,
      ChatHudAccessor accessor,
      int currentTick,
      int mouseX,
      int mouseY,
      boolean isChatFocused,
      boolean isChatOpen,
      float currentScrollPosition,
      int scrolledLines,
      int visibleLineCount,
      float chatToggleAlpha
   ) {
      NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
      float chatScale = (float)accessor.invokeGetChatScale();
      int windowHeight = context.method_51443();
      context.method_51448().method_22903();
      context.method_51448().method_22905(chatScale, chatScale, 1.0F);
      context.method_51448().method_46416(4.0F, 0.0F, 0.0F);
      int chatBottomPosY = class_3532.method_15375((windowHeight - 40) / chatScale);
      int hoveredMessageIndex = accessor.invokeGetMessageIndex(accessor.invokeToChatLineX(mouseX), accessor.invokeToChatLineY(mouseY));
      double chatOpacity = (Double)accessor.getClient().field_1690.method_42542().method_41753() * 0.9 + 0.1;
      double backgroundOpacity = (Double)accessor.getClient().field_1690.method_42550().method_41753();
      double lineSpacing = (Double)accessor.getClient().field_1690.method_42546().method_41753();
      int lineHeight = accessor.invokeGetLineHeight();
      int lineOffset = (int)Math.round(-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing);
      int[] backgroundDimensions = calculateBackgroundDimensions(accessor, currentTick, isChatFocused, isChatOpen, chatOpacity);
      int currentBackgroundHeight = backgroundDimensions[0];
      int currentBackgroundWidth = backgroundDimensions[1];
      if (overallBackgroundAnimation == null) {
         overallBackgroundAnimation = new AnimationComponent(200, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
      }

      if (chatToggleAlpha > 0.0F) {
         overallBackgroundAnimation.updateBackgroundTarget(200, currentBackgroundWidth, currentBackgroundHeight, 1.0F);
      } else {
         overallBackgroundAnimation.updateBackgroundTarget(200, 0.0F, 0.0F, 0.0F);
      }

      float animBackgroundWidth = overallBackgroundAnimation.getBackgroundWidth();
      float animBackgroundHeight = overallBackgroundAnimation.getBackgroundHeight();
      float animBackgroundAlpha = overallBackgroundAnimation.getBackgroundAlpha() * chatToggleAlpha;
      if ((currentBackgroundHeight > 0 || animBackgroundHeight > 0.1F) && animBackgroundAlpha > 0.0F) {
         int animatedBackgroundColorAlpha = (int)(255.0 * backgroundOpacity * animBackgroundAlpha);
         nanoVGHelper.setupAndDraw(
            true,
            vg -> nanoVGHelper.drawRoundedRect(
               vg,
               -4.0F,
               chatBottomPosY - animBackgroundHeight,
               animBackgroundWidth + 10.0F,
               animBackgroundHeight + 2.0F,
               animatedBackgroundColorAlpha << 24,
               3.0F
            )
         );
      }

      float clipLeft = -4.0F;
      float clipTop = chatBottomPosY - animBackgroundHeight + 3.0F;
      float clipRight = animBackgroundWidth + 6.0F;
      float clipBottom = chatBottomPosY - 1.0F;
      int scissorX1 = class_3532.method_15375((clipLeft + 4.0F - 2.0F) * chatScale);
      int scissorY1 = class_3532.method_15375((clipTop - 4.0F) * chatScale);
      int scissorX2 = class_3532.method_15386((clipRight + 4.0F + 2.0F) * chatScale);
      int scissorY2 = class_3532.method_15386((clipBottom + 3.0F) * chatScale);
      context.method_44379(scissorX1, scissorY1, scissorX2, scissorY2);
      renderMessages(
         context,
         accessor,
         currentTick,
         isChatFocused,
         isChatOpen,
         chatOpacity,
         lineHeight,
         lineOffset,
         chatBottomPosY,
         hoveredMessageIndex,
         currentScrollPosition,
         scrolledLines,
         visibleLineCount,
         chatToggleAlpha,
         currentMessageStackLiftOffset
      );
      context.method_44380();
      context.method_51448().method_22909();
   }

   private static int[] calculateBackgroundDimensions(ChatHudAccessor accessor, int currentTick, boolean isChatFocused, boolean isChatOpen, double chatOpacity) {
      int height = 0;
      int width = 0;

      for (class_7590 visibleLine : currentMessages) {
         int messageAge = currentTick - visibleLine.comp_895();
         if (messageAge < 200 || isChatFocused || !isChatOpen) {
            height += accessor.invokeGetLineHeight();
            int lineWidth = accessor.getClient().field_1772.method_30880(visibleLine.comp_896());
            if (lineWidth > width) {
               width = lineWidth;
            }
         }
      }

      return new int[]{height, width};
   }

   private static void renderMessages(
      class_332 context,
      ChatHudAccessor accessor,
      int currentTick,
      boolean isChatFocused,
      boolean isChatOpen,
      double chatOpacity,
      int lineHeight,
      int lineOffset,
      int chatBottomPosY,
      int hoveredMessageIndex,
      float currentScrollPosition,
      int scrolledLines,
      int visibleLineCount,
      float chatToggleAlpha,
      float messageStackLiftOffset
   ) {
      int drawStartLine = Math.max(0, (int)Math.floor(currentScrollPosition) - 1);
      int drawEndLine = Math.min(accessor.getVisibleMessages().size(), (int)Math.ceil(currentScrollPosition) + visibleLineCount + 1);

      for (int i = drawStartLine; i < drawEndLine; i++) {
         class_7590 visibleLine = accessor.getVisibleMessages().get(i);
         if (visibleLine != null) {
            int messageAge = currentTick - visibleLine.comp_895();
            int messageKey = Objects.hash(visibleLine.comp_895(), visibleLine.comp_896());
            if (messageAge < 200 || isChatFocused || !isChatOpen) {
               double opacityMultiplier = isChatFocused ? 1.0 : ChatHudAccessor.invokeGetMessageOpacityMultiplier(messageAge);
               int textColorAlpha = (int)(255.0 * opacityMultiplier * chatOpacity);
               if (textColorAlpha > 3) {
                  AnimationComponent animationData = getOrCreateMessageAnimation(messageKey, currentTick);
                  if (animationData.getTargetAlpha() < 0.999F) {
                     animationData.updateTarget(800, 0.0F, 0.0F, 1.0F);
                  }

                  float animAlpha = animationData.getAlpha();
                  float spawnFadeAlpha = class_3532.method_15363((messageAge + 1.0F) / 5.0F, 0.0F, 1.0F);
                  float finalAlpha = animAlpha * chatToggleAlpha * spawnFadeAlpha;
                  int animatedTextColorAlpha = (int)(textColorAlpha * finalAlpha);
                  if (animatedTextColorAlpha > 3) {
                     float exactPosition = i - currentScrollPosition;
                     int posY = chatBottomPosY - (int)(exactPosition * lineHeight);
                     int shiftedPosY = posY + (int)messageStackLiftOffset;
                     int textPosY = shiftedPosY + lineOffset;
                     class_7591 messageIndicator = visibleLine.comp_897();
                     if (messageIndicator != null) {
                        int indicatorColor = messageIndicator.comp_899() & 16777215 | animatedTextColorAlpha << 24;
                        context.method_25294(-4, shiftedPosY - lineHeight, -2, shiftedPosY, indicatorColor);
                        if (i == hoveredMessageIndex && messageIndicator.comp_900() != null) {
                           int indicatorX = accessor.invokeGetIndicatorX(visibleLine);
                           int indicatorY = textPosY + 9;
                           accessor.invokeDrawIndicatorIcon(context, indicatorX, indicatorY, messageIndicator.comp_900());
                        }
                     }

                     context.method_35720(
                        accessor.getClient().field_1772, visibleLine.comp_896(), (int)animationData.getX(), textPosY, 16777215 + (animatedTextColorAlpha << 24)
                     );
                  }
               }
            }
         }
      }
   }
}
