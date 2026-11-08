package dev.southside.utils.network;

import com.google.gson.Gson;
import dev.southside.utils.json.GsonUtils;
import dev.southside.utils.json.JsonDateDeserializer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Http {
   private static final Gson GSON = GsonUtils.custom().registerTypeAdapter(Date.class, new JsonDateDeserializer()).create();

   public static Http.Request to(Http.Method method, String url) {
      return new Http.Request(method, url);
   }

   public static Http.Request get(String url) {
      return new Http.Request(Http.Method.GET, url);
   }

   public static Http.Request post(String url) {
      return new Http.Request(Http.Method.POST, url);
   }

   public static enum Method {
      GET,
      POST;
   }

   public static class Request {
      public final HttpClientContext context = HttpClientContext.create();
      public final CookieStore cookieStore = new BasicCookieStore();
      private final HttpRequestBase httpClient;
      private final Builder configBuilder = RequestConfig.custom();
      private boolean keepAlive = true;
      private String charset = "UTF-8";

      public Request(Http.Method method, String url) {
         this.httpClient = (HttpRequestBase)(method.equals(Http.Method.GET) ? new HttpGet(url) : new HttpPost(url));
         this.cookieSpec("default").context.setCookieStore(this.cookieStore);
         this.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
      }

      public Http.Request diKeepAlive() {
         this.keepAlive = false;
         return this;
      }

      public Http.Request userAgent(String agent) {
         this.httpClient.setHeader("User-Agent", agent);
         return this;
      }

      public Http.Request header(String header, Object value) {
         this.httpClient.setHeader(header, value.toString());
         return this;
      }

      public Http.Request cookieSpec(String spec) {
         this.configBuilder.setCookieSpec(spec);
         return this;
      }

      public Http.Request cookies(String data) {
         this.httpClient.setHeader("Cookie", data);
         return this;
      }

      public Http.Request bearer(String token) {
         return this.authorization("Bearer " + token);
      }

      public Http.Request authorization(String token) {
         this.httpClient.setHeader("Authorization", token);
         return this;
      }

      public Http.Request bodyJson(String data) {
         this.httpClient.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
         if (this.httpClient instanceof HttpPost post) {
            post.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON.withCharset(this.charset)));
         }

         return this;
      }

      public Http.Request bodyForm(String data) {
         this.httpClient.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
         if (this.httpClient instanceof HttpPost post) {
            post.setEntity(new StringEntity(data, ContentType.APPLICATION_FORM_URLENCODED.withCharset(this.charset)));
         }

         return this;
      }

      public Http.Request bodyForm(StringEntity data) {
         this.httpClient.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
         if (this.httpClient instanceof HttpPost post) {
            post.setEntity(data);
         }

         return this;
      }

      public Http.Request bodyCrypt(Function<String, byte[]> function, String data) {
         return this.bodyBytes(function.apply(data));
      }

      public Http.Request bodyCrypt(Function<String, byte[]> function, Object data) {
         return this.bodyBytes(function.apply(Http.GSON.toJson(data)));
      }

      public Http.Request bodyBytes(byte[] data) {
         this.httpClient.setHeader("Content-Type", ContentType.DEFAULT_BINARY.getMimeType());
         if (this.httpClient instanceof HttpPost post) {
            post.setEntity(new ByteArrayEntity(data));
         }

         return this;
      }

      public Http.Request bodyJson(Object object) {
         return this.bodyJson(Http.GSON.toJson(object));
      }

      public Http.Request proxy(String ip, int port) {
         this.configBuilder.setProxy(new HttpHost(ip, port));
         return this;
      }

      public Http.Request acceptJson() {
         return this.accept("application/json, text/json, text/x-json, text/javascript");
      }

      public Http.Request acceptAll() {
         return this.accept("*/*");
      }

      public Http.Request accept(String type) {
         this.httpClient.setHeader("Accept", type);
         return this;
      }

      private <T> T _send(Http.Request.HttpResponseHandler<T> responseHandler) {
         try {
            CloseableHttpClient client = HttpClientBuilder.create().build();

            Object var4;
            try {
               if (!this.keepAlive) {
                  this.httpClient.setHeader("Connection", "close");
               }

               this.httpClient.setConfig(this.configBuilder.build());
               CloseableHttpResponse response = client.execute(this.httpClient, this.context);
               var4 = responseHandler.handle(response.getEntity());
            } catch (Throwable var6) {
               if (client != null) {
                  try {
                     client.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (client != null) {
               client.close();
            }

            return (T)var4;
         } catch (IOException var7) {
            var7.printStackTrace();
            return null;
         }
      }

      public <T> T sendResponse(Http.Request.ResponseHandler<T> responseHandler) {
         try {
            CloseableHttpClient client = HttpClientBuilder.create().build();

            Object var4;
            try {
               if (!this.keepAlive) {
                  this.httpClient.setHeader("Connection", "close");
               }

               this.httpClient.setConfig(this.configBuilder.build());
               CloseableHttpResponse response = client.execute(this.httpClient);
               var4 = responseHandler.handle(response);
            } catch (Throwable var6) {
               if (client != null) {
                  try {
                     client.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (client != null) {
               client.close();
            }

            return (T)var4;
         } catch (IOException var7) {
            var7.printStackTrace();
            return null;
         }
      }

      public <T> T sendCtxResponse(Http.Request.CtxResponseHandler<T> responseHandler) {
         try {
            CloseableHttpClient client = HttpClientBuilder.create().build();

            Object var4;
            try {
               if (!this.keepAlive) {
                  this.httpClient.setHeader("Connection", "close");
               }

               this.httpClient.setConfig(this.configBuilder.build());
               CloseableHttpResponse response = client.execute(this.httpClient);
               var4 = responseHandler.handle(this.context, response);
            } catch (Throwable var6) {
               if (client != null) {
                  try {
                     client.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (client != null) {
               client.close();
            }

            return (T)var4;
         } catch (IOException var7) {
            var7.printStackTrace();
            return null;
         }
      }

      public void send() {
         this._send(response -> null);
      }

      public Http.Request setCoding(String charset) {
         this.charset = charset;
         return this;
      }

      public String sendString() {
         return new String(this.sendBytes(), Charset.forName(this.charset));
      }

      public HttpEntity sendHttpEntity() {
         return this._send(entity -> entity);
      }

      public Stream<String> sendLines() {
         return Stream.of(this.sendString().split("\\r?\\n"));
      }

      public <T> T sendJson(Type type) {
         String response = this.sendString();
         return (T)(response == null ? null : Http.GSON.fromJson(response, type));
      }

      public InputStream sendInputStream() {
         return this._send(entity -> {
            try {
               return entity.getContent();
            } catch (IOException var2) {
               return null;
            }
         });
      }

      public <T> T sendCryptJson(Function<byte[], String> function, Type type) {
         String response = function.apply(this.sendBytes());
         return (T)(response == null ? null : Http.GSON.fromJson(response, type));
      }

      public <T> T sendCrypt(Function<byte[], T> function) {
         return function.apply(this.sendBytes());
      }

      public <T> T sendCryptJson(BiFunction<byte[], String, String> function, String key, Type type) {
         String response = function.apply(this.sendBytes(), key);
         return (T)(response == null ? null : Http.GSON.fromJson(response, type));
      }

      public <T, R> T sendCrypt(BiFunction<byte[], R, T> function, R key) {
         return function.apply(this.sendBytes(), key);
      }

      public <T> T sendCryptJsonStr(Function<String, String> function, Type type) {
         String response = function.apply(this.sendString());
         return (T)(response == null ? null : Http.GSON.fromJson(response, type));
      }

      public <T> T sendCryptStr(Function<String, T> function) {
         return function.apply(this.sendString());
      }

      public <T> T sendCryptJsonStr(BiFunction<String, String, String> function, String key, Type type) {
         String response = function.apply(this.sendString(), key);
         return (T)(response == null ? null : Http.GSON.fromJson(response, type));
      }

      public <T, K> T sendCryptStr(BiFunction<String, K, T> function, K key) {
         return function.apply(this.sendString(), key);
      }

      public byte[] sendBytes() {
         return this._send(entity -> {
            try {
               byte[] var4;
               try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                  byte[] buffer = new byte[4096];

                  int read;
                  while ((read = entity.getContent().read(buffer)) > 0) {
                     out.write(buffer, 0, read);
                  }

                  var4 = out.toByteArray();
               }

               return var4;
            } catch (IOException var7) {
               return null;
            }
         });
      }

      public byte[] sendBytes(int per) {
         return this._send(entity -> {
            try {
               byte[] var5;
               try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                  byte[] buffer = new byte[per];

                  int read;
                  while ((read = entity.getContent().read(buffer)) > 0) {
                     out.write(buffer, 0, read);
                  }

                  var5 = out.toByteArray();
               }

               return var5;
            } catch (IOException var8) {
               return null;
            }
         });
      }

      public interface CtxResponseHandler<T> {
         T handle(HttpClientContext var1, HttpResponse var2);
      }

      public interface HttpResponseHandler<T> {
         T handle(HttpEntity var1);
      }

      public interface ResponseHandler<T> {
         T handle(HttpResponse var1);
      }
   }
}
