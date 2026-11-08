package dev.southside.nel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.southside.nel.open_nel.GameCharacter;
import dev.southside.nel.open_nel.InterceptorBody;
import dev.southside.nel.open_nel.NeteaseAccount;
import dev.southside.nel.open_nel.ResponsePack;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Generated;

public class OpenNELApi {
   private final ExecutorService executor;
   private final OpenNELApi.NelIO io;

   public CompletableFuture<String> handshake(String crcSalt, String version, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         String ping = "{\"ping\":\"ok\"}";
         String noop = "{\"type\":\"noop\", \"crcSalt\":\"" + crcSalt + "\",\"version\":\"" + version + "\"}";
         this.io.send("{\"ping\":\"ok\"}");
         String v1 = this.io.read(unit.toMillis(timedOut));
         if ("{\"ping\":\"ok\"}".equals(v1)) {
            this.io.send(noop);
            String v2 = this.io.read(unit.toMillis(timedOut));
            return noop.equals(v2) ? null : "type handshake failed";
         } else {
            return "ping handshake failed";
         }
      }, this.executor);
   }

   public CompletableFuture<String> loginCookie(String cookie, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject tag = new JsonObject();
         tag.addProperty("type", "login");
         tag.addProperty("cookie", cookie);
         this.io.send(tag);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return "Cookie login failed: null response";
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") && obj.get("type").isJsonPrimitive() ? obj.get("type").getAsString() : "";
                  if ("Success_login".equals(type)) {
                     return null;
                  }

                  if ("login_error".equals(type)) {
                     return ":" + obj.get("message").getAsString();
                  }
               }
            } catch (JsonSyntaxException var11) {
               return "response -> error json";
            }
         }

         return "no accounts";
      }, this.executor);
   }

   public CompletableFuture<String> login4399(String account, String password, String sessionId, String captcha, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject tag = new JsonObject();
         tag.addProperty("type", "login_4399");
         tag.addProperty("account", account);
         tag.addProperty("password", password);
         if (sessionId != null) {
            tag.addProperty("sessionId", sessionId);
         }

         if (captcha != null) {
            tag.addProperty("captcha", captcha);
         }

         this.io.send(tag);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return "4399 login failed: null response";
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") && obj.get("type").isJsonPrimitive() ? obj.get("type").getAsString() : "";
                  if ("Success_login".equals(type)) {
                     return null;
                  }

                  if ("captcha_required".equals(type)) {
                     String session = obj.has("sessionId") && obj.get("sessionId").isJsonPrimitive() ? obj.get("sessionId").getAsString() : "";
                     return "@" + session;
                  }

                  if ("login_4399_error".equals(type)) {
                     return ":" + obj.get("message").getAsString();
                  }
               }
            } catch (JsonSyntaxException var15) {
               return "response -> error json";
            }
         }

         return "no accounts";
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<List<GameCharacter>>> createRole(String serverId, String name, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject tag = new JsonObject();
         tag.addProperty("type", "create_role_named");
         tag.addProperty("serverId", serverId);
         tag.addProperty("name", name);
         this.io.send(tag);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return new ResponsePack<>("null response", null);
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") && obj.get("type").isJsonPrimitive() ? obj.get("type").getAsString() : "";
                  if ("server_roles".equals(type)) {
                     List<GameCharacter> characters = new ArrayList<>();
                     if (obj.has("items") && obj.get("items").isJsonArray()) {
                        for (JsonElement role : obj.get("items").getAsJsonArray()) {
                           if (role.isJsonObject()) {
                              JsonObject roleTag = role.getAsJsonObject();
                              String roleId = roleTag.has("id") ? roleTag.get("id").getAsString() : "未知ID";
                              String roleName = roleTag.has("name") ? roleTag.get("name").getAsString() : "新角色";
                              characters.add(new GameCharacter(roleId, roleName));
                           }
                        }
                     }

                     return new ResponsePack<>(null, characters);
                  }

                  if ("server_roles_error".equals(type)) {
                     return new ResponsePack<>(obj.get("message").getAsString(), null);
                  }
               }
            } catch (JsonSyntaxException var19) {
               return new ResponsePack<>("error json", null);
            }
         }

         return new ResponsePack<>("idk", null);
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<List<GameCharacter>>> openServerAndGetRoles(String serverId, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject tag = new JsonObject();
         tag.addProperty("type", "open_server");
         tag.addProperty("serverId", serverId);
         this.io.send(tag);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return new ResponsePack<>("null response", null);
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") && obj.get("type").isJsonPrimitive() ? obj.get("type").getAsString() : "";
                  if ("server_roles".equals(type)) {
                     List<GameCharacter> characters = new ArrayList<>();
                     if (obj.has("items") && obj.get("items").isJsonArray()) {
                        for (JsonElement role : obj.get("items").getAsJsonArray()) {
                           if (role.isJsonObject()) {
                              JsonObject roleTag = role.getAsJsonObject();
                              String roleId = roleTag.has("id") ? roleTag.get("id").getAsString() : "未知ID";
                              String roleName = roleTag.has("name") ? roleTag.get("name").getAsString() : "新角色";
                              characters.add(new GameCharacter(roleId, roleName));
                           }
                        }
                     }

                     return new ResponsePack<>(null, characters);
                  }

                  if ("server_roles_error".equals(type)) {
                     return new ResponsePack<>(obj.get("message").getAsString(), null);
                  }
               }
            } catch (JsonSyntaxException var18) {
               return new ResponsePack<>("error json", null);
            }
         }

         return new ResponsePack<>("idk", null);
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<List<NeteaseAccount>>> fetchAccounts(long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(
         () -> {
            JsonObject req = new JsonObject();
            req.addProperty("type", "list_accounts");
            this.io.send(req);

            for (int i = 0; i < 10; i++) {
               String response = this.io.read(unit.toMillis(timedOut));
               if (response == null) {
                  return new ResponsePack<>("null response", null);
               }

               try {
                  JsonElement element = JsonParser.parseString(response);
                  if (element.isJsonObject()) {
                     JsonObject obj = element.getAsJsonObject();
                     String type = obj.has("type") ? obj.get("type").getAsString() : "";
                     if ("accounts".equals(type)) {
                        List<NeteaseAccount> accounts = new ArrayList<>();
                        if (obj.has("items") && obj.get("items").isJsonArray()) {
                           for (JsonElement acc : obj.get("items").getAsJsonArray()) {
                              if (acc.isJsonObject()) {
                                 JsonObject accTag = acc.getAsJsonObject();
                                 accounts.add(
                                    new NeteaseAccount(
                                       accTag.get("entityId").getAsString(), accTag.get("channel").getAsString(), accTag.get("status").getAsString()
                                    )
                                 );
                              }
                           }
                        }

                        return new ResponsePack<>(null, accounts);
                     }
                  }
               } catch (JsonSyntaxException var15) {
                  return new ResponsePack<>("error json", null);
               }
            }

            return new ResponsePack<>("idk", null);
         },
         this.executor
      );
   }

   public CompletableFuture<String> selectAccount(String accountID, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject req = new JsonObject();
         req.addProperty("type", "select_account");
         req.addProperty("entityId", accountID);
         this.io.send(req);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") ? obj.get("type").getAsString() : "";
                  if ("notlogin".equals(type)) {
                     return "not login";
                  }

                  if ("selected_account".equals(type)) {
                     return null;
                  }
               }
            } catch (JsonSyntaxException var11) {
               return "error json";
            }
         }

         return null;
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<InterceptorBody>> joinGame(String serverId, String roleId, String serverName, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject joinMsg = new JsonObject();
         joinMsg.addProperty("type", "join_game");
         joinMsg.addProperty("serverId", serverId);
         joinMsg.addProperty("role", roleId);
         joinMsg.addProperty("serverName", serverName);
         this.io.send(joinMsg);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return new ResponsePack<>("null response", null);
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") ? obj.get("type").getAsString() : "";
                  if ("start_error".equals(type)) {
                     return new ResponsePack<>(":" + obj.get("message").getAsString(), null);
                  }

                  if ("channels_updated".equals(type)) {
                     String addr = obj.get("address").getAsString();
                     String id = obj.get("guid").getAsString();
                     return new ResponsePack<>(null, new InterceptorBody(addr, id));
                  }
               }
            } catch (JsonSyntaxException var15) {
               return new ResponsePack<>("error json", null);
            }
         }

         return new ResponsePack<>("timed out", null);
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<List<NeteaseAccount>>> deleteAccount(String entityId, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject req = new JsonObject();
         req.addProperty("type", "delete_account");
         req.addProperty("entityId", entityId);
         this.io.send(req);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return new ResponsePack<>("null response", null);
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") ? obj.get("type").getAsString() : "";
                  if ("delete_error".equals(type)) {
                     String msg = obj.has("message") ? obj.get("message").getAsString() : "unknown error";
                     return new ResponsePack<>(msg, null);
                  }

                  if ("accounts".equals(type)) {
                     List<NeteaseAccount> accounts = new ArrayList<>();
                     if (obj.has("items") && obj.get("items").isJsonArray()) {
                        for (JsonElement el : obj.get("items").getAsJsonArray()) {
                           if (el.isJsonObject()) {
                              JsonObject acc = el.getAsJsonObject();
                              String id = acc.has("entityId") ? acc.get("entityId").getAsString() : "";
                              String channel = acc.has("channel") ? acc.get("channel").getAsString() : "";
                              accounts.add(new NeteaseAccount(id, channel, ""));
                           }
                        }
                     }

                     return new ResponsePack<>(null, accounts);
                  }
               }
            } catch (JsonSyntaxException var17) {
               return new ResponsePack<>("error json", null);
            }
         }

         return new ResponsePack<>("timed out", null);
      }, this.executor);
   }

   public CompletableFuture<ResponsePack<List<String>>> shutdownGame(List<String> identifiers, long timedOut, TimeUnit unit) {
      return CompletableFuture.supplyAsync(() -> {
         JsonObject shutdownMsg = new JsonObject();
         shutdownMsg.addProperty("type", "shutdown_game");
         JsonArray arr = new JsonArray();

         for (String id : identifiers) {
            arr.add(id);
         }

         shutdownMsg.add("identifiers", arr);
         this.io.send(shutdownMsg);

         for (int i = 0; i < 10; i++) {
            String response = this.io.read(unit.toMillis(timedOut));
            if (response == null) {
               return new ResponsePack<>("null response", null);
            }

            try {
               JsonElement element = JsonParser.parseString(response);
               if (element.isJsonObject()) {
                  JsonObject obj = element.getAsJsonObject();
                  String type = obj.has("type") ? obj.get("type").getAsString() : "";
                  if ("shutdown_ack".equals(type)) {
                     List<String> closed = new ArrayList<>();
                     if (obj.has("identifiers") && obj.get("identifiers").isJsonArray()) {
                        for (JsonElement el : obj.get("identifiers").getAsJsonArray()) {
                           if (el.isJsonPrimitive()) {
                              closed.add(el.getAsString());
                           }
                        }
                     }

                     return new ResponsePack<>(null, closed);
                  }

                  if ("channels_updated".equals(type)) {
                     return new ResponsePack<>(null, List.of());
                  }

                  if ("start_error".equals(type)) {
                     return new ResponsePack<>(":" + obj.get("message").getAsString(), null);
                  }
               }
            } catch (JsonSyntaxException var15) {
               return new ResponsePack<>("error json", null);
            }
         }

         return new ResponsePack<>("timed out", null);
      }, this.executor);
   }

   @Generated
   public OpenNELApi(ExecutorService executor, OpenNELApi.NelIO io) {
      this.executor = executor;
      this.io = io;
   }

   public interface NelIO {
      void send(Object var1);

      String read(long var1);
   }
}
