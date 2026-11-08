package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.Button;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.Southside;
import dev.southside.event.events.GameJoinedEvent;
import dev.southside.event.events.SetScreenEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.Http;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.function.Consumer;
import net.minecraft.class_310;
import net.minecraft.class_419;
import org.jetbrains.annotations.Nullable;

public class Account extends Module {
   private static final String SECRET_KEY = "69eup3qrvihqwryjbgoxfnow4pxdp7nfdfq9zws92kb5ub9dxtk14rjm2tt89ecq2jqnxdvsldyuw7mutebmt9x4s6m5p2c9h8huoygvqf5u2468f1mycdwysho8k1o1";
   private static final String API_ENDPOINT = "https://freecookies.fisproxy.org/api/v2/freecookie/heypassed";
   public Switch debugMode = new Switch("debug", false);
   public Switch autogetMode = new Switch("AutoGetWhenDisconnected", false);
   public Button getAccountButton = new Button("豁免账号", "获取", "不保证账号质量。", 2, this.getAccount());
   private boolean autoFetchedOnLastDisconnect = false;

   public Account() {
      super(Category.Client, "Account", "妈妈我不要哈维哈");
   }

   @Override
   public void initialize() {
      super.initialize();
      Southside.INSTANCE.getEventBus().register(this);
   }

   @Override
   public void onEnable() {
      this.setEnable(false);
   }

   public Runnable getAccount() {
      return () -> {
         Notification.addNotification("正在获取...", "FreeCookie", Notification.NotificationType.INFO);
         this.debug("正在发送请求...");
         this.fetchAccount(credentials -> {
            String account = credentials.account();
            String password = credentials.password();
            String webUrl = "https://freecookies.fisproxy.org/heypassed.html#" + account + ":" + password;
            this.message("浏览器打开此链接查看您获取的账号: " + webUrl, new Object[0]);
            boolean opened = this.openUrlWithSystemCommand(webUrl);
            if (opened) {
               Notification.addNotification("获取成功！请检查浏览器。", "FreeCookie", Notification.NotificationType.INFO);
            } else {
               Notification.addNotification("复制账号失败", "FreeCookie", Notification.NotificationType.INFO);
            }
         }, error -> Notification.addNotification(error, "FreeCookie", Notification.NotificationType.INFO));
      };
   }

   public void fetchAccount(@Nullable Consumer<Account.Credentials> onSuccess, @Nullable Consumer<String> onFailure) {
      new Thread(
            () -> {
               try {
                  String token = this.generateToken(
                     "69eup3qrvihqwryjbgoxfnow4pxdp7nfdfq9zws92kb5ub9dxtk14rjm2tt89ecq2jqnxdvsldyuw7mutebmt9x4s6m5p2c9h8huoygvqf5u2468f1mycdwysho8k1o1"
                  );
                  this.debug("生成的Token: " + token);
                  String apiUrl = "https://freecookies.fisproxy.org/api/v2/freecookie/heypassed?token=" + token;
                  this.debug("请求URL: " + apiUrl);
                  Http.get(apiUrl).userAgent("Minecraft-Southside-Client").sendResponse(c -> {
                     try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(c.getEntity().getContent()));
                        StringBuilder response = new StringBuilder();

                        String line;
                        while ((line = reader.readLine()) != null) {
                           response.append(line);
                        }

                        reader.close();
                        int responseCode = c.getStatusLine().getStatusCode();
                        this.debug("HTTP响应: " + responseCode);
                        this.debug("原始响应: " + response);

                        JsonObject jsonResponse;
                        try {
                           jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                        } catch (Exception var12) {
                           this.debug("JSON解析失败: " + var12.getMessage());
                           this.dispatchFailure(onFailure, "服务器错误: " + responseCode);
                           return null;
                        }

                        if (!jsonResponse.has("code")) {
                           this.message("响应缺少code字段", new Object[0]);
                           this.debug("完整响应: " + jsonResponse);
                           this.dispatchFailure(onFailure, "响应缺少code字段");
                           return null;
                        }

                        int code = jsonResponse.get("code").getAsInt();
                        this.debug("响应code: " + code);
                        if (code == 0) {
                           if (!jsonResponse.has("account") || !jsonResponse.has("password")) {
                              this.message("响应缺少账号或密码字段", new Object[0]);
                              this.debug("完整响应: " + jsonResponse);
                              this.dispatchFailure(onFailure, "响应缺少账号或密码字段");
                              return null;
                           }

                           String account = jsonResponse.get("account").getAsString();
                           String password = jsonResponse.get("password").getAsString();
                           this.dispatchSuccess(onSuccess, new Account.Credentials(account, password));
                        } else {
                           String errorMsg = jsonResponse.has("msg") ? jsonResponse.get("msg").getAsString() : "未知错误 (code=" + code + ")";
                           this.dispatchFailure(onFailure, errorMsg);
                        }
                     } catch (Exception var13) {
                        this.message("未知错误", new Object[0]);
                        this.debug("异常信息: " + var13.getMessage());
                        var13.printStackTrace();
                        this.dispatchFailure(onFailure, "未知错误");
                     }

                     return null;
                  });
               } catch (Exception var5) {
                  this.message("未知错误", new Object[0]);
                  this.debug("异常信息: " + var5.getMessage());
                  var5.printStackTrace();
                  this.dispatchFailure(onFailure, "未知错误");
               }
            },
            "Account-Fetch-Thread"
         )
         .start();
   }

   private void dispatchSuccess(@Nullable Consumer<Account.Credentials> consumer, Account.Credentials credentials) {
      if (consumer != null) {
         class_310 client = class_310.method_1551();
         Runnable callback = () -> consumer.accept(credentials);
         if (client != null) {
            client.execute(callback);
         } else {
            callback.run();
         }
      }
   }

   private void dispatchFailure(@Nullable Consumer<String> consumer, String message) {
      if (consumer != null) {
         class_310 client = class_310.method_1551();
         Runnable callback = () -> consumer.accept(message);
         if (client != null) {
            client.execute(callback);
         } else {
            callback.run();
         }
      }
   }

   @EventHandler
   private void onSetScreen(SetScreenEvent event) {
      try {
         if (!this.autogetMode.getValue()) {
            return;
         }

         if (event.screen instanceof class_419 && !this.autoFetchedOnLastDisconnect) {
            this.autoFetchedOnLastDisconnect = true;
            Notification.addNotification("检测到断开，正在自动获取账号...", "Powered BY FreeCookie", Notification.NotificationType.INFO);
            this.getAccount().run();
         }
      } catch (Exception var3) {
      }
   }

   @EventHandler
   private void onGameJoin(GameJoinedEvent event) {
      this.autoFetchedOnLastDisconnect = false;
   }

   private boolean openUrlWithSystemCommand(String url) {
      try {
         String os = System.getProperty("os.name").toLowerCase();
         if (os.contains("win")) {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
         } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"open", url});
         } else if (!os.contains("nix") && !os.contains("nux")) {
            Desktop.getDesktop().browse(new URI(url));
         } else {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
         }

         return true;
      } catch (Exception var5) {
         this.debug("系统命令打开URL失败: " + var5.getMessage());

         try {
            Desktop.getDesktop().browse(new URI(url));
            return true;
         } catch (Exception var4) {
            this.debug("Desktop打开URL也失败: " + var4.getMessage());
            return false;
         }
      }
   }

   private void debug(String message) {
      if (this.debugMode.getValue() && mc.field_1724 != null) {
         this.message(message, new Object[0]);
      }
   }

   private String generateToken(String secretKey) {
      try {
         long timestamp = System.currentTimeMillis() / 1000L - 10L;
         String nonce = this.generateRandomString(32);
         String deviceId = this.getDeviceId();
         String tsStr = String.valueOf(timestamp);
         int checkDigit = this.computeCheckDigit(tsStr, nonce, deviceId);
         String modifiedTs = tsStr.substring(0, tsStr.length() - 1) + checkDigit;
         String dataToSign = secretKey + modifiedTs + nonce + deviceId;
         String signature = this.sha256(dataToSign);
         String combined = modifiedTs + ":" + nonce + ":" + deviceId + ":" + signature;
         return Base64.getEncoder().encodeToString(combined.getBytes());
      } catch (Exception var12) {
         this.debug("生成Token时发生异常: " + var12.getMessage());
         return "";
      }
   }

   private int computeCheckDigit(String originalTs, String nonce, String deviceId) {
      String hash = this.sha256(originalTs + nonce + deviceId);
      return (int)(Long.parseLong(hash.substring(0, 8), 16) % 10L);
   }

   private String getDeviceId() {
      try {
         StringBuilder raw = new StringBuilder();

         for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            byte[] mac = ni.getHardwareAddress();
            if (mac != null && mac.length > 0) {
               StringBuilder macStr = new StringBuilder();

               for (byte b : mac) {
                  macStr.append(String.format("%02x", b));
               }

               raw.append((CharSequence)macStr);
            }
         }

         raw.append(System.getProperty("os.name", ""));
         raw.append(System.getProperty("os.arch", ""));
         raw.append(System.getProperty("user.name", ""));
         raw.append(System.getProperty("user.home", ""));
         return this.sha256(raw.toString());
      } catch (Exception var11) {
         this.debug("获取设备ID失败: " + var11.getMessage());
         String fallback = System.getProperty("os.name", "")
            + System.getProperty("os.arch", "")
            + System.getProperty("user.name", "")
            + System.getProperty("user.home", "");
         return this.sha256(fallback);
      }
   }

   private String generateRandomString(int length) {
      String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      StringBuilder result = new StringBuilder();
      SecureRandom random = new SecureRandom();

      for (int i = 0; i < length; i++) {
         result.append(chars.charAt(random.nextInt(chars.length())));
      }

      return result.toString();
   }

   private String sha256(String input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(input.getBytes("UTF-8"));
         StringBuilder hexString = new StringBuilder();

         for (byte b : hash) {
            String hex = Integer.toHexString(255 & b);
            if (hex.length() == 1) {
               hexString.append('0');
            }

            hexString.append(hex);
         }

         return hexString.toString();
      } catch (Exception var10) {
         this.debug("SHA256计算失败: " + var10.getMessage());
         return "";
      }
   }

   public record Credentials(String account, String password) {
   }
}
