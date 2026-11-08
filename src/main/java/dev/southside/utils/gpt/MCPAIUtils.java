package dev.southside.utils.gpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.utils.misc.AIWalkController;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class MCPAIUtils {
   private static final String API_URL = "https://api.openai.com/v1/chat/completions";
   private static String apiKey;
   private static String systemPrompt;
   private static CloseableHttpClient httpClient;
   private static List<MCPAIUtils.Message> conversationHistory = new ArrayList<>();
   private static final int Max_Length = 10;

   public static void initialize(String key, String sysPrompt) {
      apiKey = key;
      systemPrompt = sysPrompt;
      httpClient = HttpClients.createDefault();
      conversationHistory.clear();
   }

   public static void callAndExecute(String prompt) {
      String reply = callAPI(prompt);
      if (reply != null && !reply.isEmpty()) {
         try {
            JsonObject json = JsonParser.parseString(reply).getAsJsonObject();
            executeGameAction(json);
         } catch (Exception var3) {
            class_310.method_1551().execute(() -> class_310.method_1551().field_1724.method_7353(class_2561.method_30163("[AI] " + reply), false));
         }
      }
   }

   public static String callAPI(String prompt) {
      if (httpClient == null) {
         throw new IllegalStateException("GPT API not initialized. Call initialize() first.");
      } else {
         try {
            HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            JsonArray messages = new JsonArray();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
               messages.add(new MCPAIUtils.Message("system", systemPrompt).toJsonObject());
            }

            for (MCPAIUtils.Message message : conversationHistory) {
               messages.add(message.toJsonObject());
            }

            MCPAIUtils.Message userMessage = new MCPAIUtils.Message("user", prompt);
            messages.add(userMessage.toJsonObject());
            conversationHistory.add(userMessage);
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "deepseek-chat");
            requestBody.add("messages", messages);
            request.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));
            String response = EntityUtils.toString(httpClient.execute(request).getEntity(), StandardCharsets.UTF_8);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            if (jsonResponse.has("choices")) {
               JsonArray choices = jsonResponse.getAsJsonArray("choices");
               if (!choices.isEmpty()) {
                  JsonObject firstChoice = choices.get(0).getAsJsonObject();
                  if (firstChoice.has("message")) {
                     JsonObject messageObj = firstChoice.getAsJsonObject("message");
                     String assistantResponse = messageObj.get("content").getAsString();
                     MCPAIUtils.Message assistantMessage = new MCPAIUtils.Message("assistant", assistantResponse);
                     conversationHistory.add(assistantMessage);
                     if (conversationHistory.size() > 10) {
                        int startIndex = systemPrompt != null && !systemPrompt.isEmpty() ? 1 : 0;
                        conversationHistory = new ArrayList<>(
                           conversationHistory.subList(conversationHistory.size() - 10 + startIndex, conversationHistory.size())
                        );
                     }

                     return assistantResponse.trim();
                  }
               }
            }

            return jsonResponse.has("error")
               ? "API Error: " + jsonResponse.getAsJsonObject("error").get("message").getAsString()
               : "No valid response from API";
         } catch (Exception var13) {
            var13.printStackTrace();
            return "Error calling API: " + var13.getMessage();
         }
      }
   }

   private static void executeGameAction(JsonObject json) {
      String action = json.has("action") ? json.get("action").getAsString() : "";
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null) {
         switch (action) {
            case "chat":
               String msg = json.get("message").getAsString();
               mc.execute(() -> mc.method_1562().method_45729(msg));
               break;
            case "move":
               String direction = json.get("direction").getAsString();
               double distance = json.get("distance").getAsDouble();
               mc.execute(() -> movePlayer(player, direction, distance));
               break;
            default:
               mc.execute(() -> player.method_7353(class_2561.method_30163("[AI-Unknown Action] " + json.toString()), false));
         }
      }
   }

   private static void movePlayer(class_746 player, String direction, double distance) {
      double x = player.method_23317();
      double z = player.method_23321();
      double yaw = player.method_36454();
      String var10 = direction.toLowerCase();
      switch (var10) {
         case "north":
            z -= distance;
            break;
         case "south":
            z += distance;
            break;
         case "east":
            x += distance;
            break;
         case "west":
            x -= distance;
      }

      AIWalkController.walkTo(new class_2338((int)x, (int)player.method_23318(), (int)z));
      player.method_7353(class_2561.method_30163("[AI] Moved " + distance + " blocks " + direction), false);
   }

   public static class Message {
      private final String role;
      private final String content;

      public Message(String role, String content) {
         this.role = role;
         this.content = content;
      }

      public JsonObject toJsonObject() {
         JsonObject obj = new JsonObject();
         obj.addProperty("role", this.role);
         obj.addProperty("content", this.content);
         return obj;
      }
   }
}
