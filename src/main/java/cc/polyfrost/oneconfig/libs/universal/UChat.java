package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

public class UChat {
   public static void chat(String s) {
      MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(s));
   }

   public static void say(String s) {
      UMinecraft.getMinecraft().player.networkHandler.sendChatMessage(s);
   }
}
