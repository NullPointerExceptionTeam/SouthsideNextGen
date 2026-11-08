package dev.southside.utils.freecookie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.Generated;

public class FreeCookieAPI {
   public static final String API = "https://freecookie.studio/api/v2/freecookie/heypassed";
   private static final HttpClient client = HttpClient.newHttpClient();
   private static final Gson gson = new GsonBuilder().create();

   public static void main(String[] args) throws IOException, InterruptedException {
      System.out.println(getAccount("testkey"));
   }

   public static FreeCookieAPI.Account getAccount(String secretKey) throws IOException, InterruptedException {
      String token = CryptoUtils.clientSignature(secretKey);
      System.out.println("token" + token);
      String url = "https://freecookie.studio/api/v2/freecookie/heypassed?token=" + token;
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      if (response.statusCode() != 200) {
         throw new IOException("请求失败: " + response.statusCode());
      } else {
         String json = response.body();
         System.out.println("JSON数据: " + json);
         FreeCookieAPI.FreeCookieResponse data = (FreeCookieAPI.FreeCookieResponse)gson.fromJson(json, FreeCookieAPI.FreeCookieResponse.class);
         if (data.getCode() != 0) {
            throw new IOException("API返回错误码: " + data.getCode());
         } else {
            return new FreeCookieAPI.Account(data.getAccount(), data.getPassword(), data.getCookie());
         }
      }
   }

   public static class Account {
      private final String accountName;
      private final String password;
      private final String cookie;

      public Account(String accountName, String password, String cookie) {
         this.accountName = accountName;
         this.password = password;
         this.cookie = cookie;
      }

      @Generated
      public String getAccountName() {
         return this.accountName;
      }

      @Generated
      public String getPassword() {
         return this.password;
      }

      @Generated
      public String getCookie() {
         return this.cookie;
      }
   }

   public static class FreeCookieResponse {
      @SerializedName("code")
      private int code;
      @SerializedName("account")
      private String account;
      @SerializedName("password")
      private String password;
      @SerializedName("cookie")
      private String cookie;

      @Generated
      public int getCode() {
         return this.code;
      }

      @Generated
      public String getAccount() {
         return this.account;
      }

      @Generated
      public String getPassword() {
         return this.password;
      }

      @Generated
      public String getCookie() {
         return this.cookie;
      }
   }
}
