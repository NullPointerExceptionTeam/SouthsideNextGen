package dev.southside.nel.protocol;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ForwardServer {
   private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

   public static void start() throws Exception {
      HttpServer server = HttpServer.create(new InetSocketAddress(13337), 0);
      server.createContext("/third-party/heypixel/derive-key-v3", new ForwardServer.ProxyHandler());
      server.setExecutor(Executors.newFixedThreadPool(2));
      server.start();
   }

   static class ProxyHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
         String targetUrl = "http://dxg-on-top.hecker.lol/api/heypixel/derive-key";
         String params = exchange.getRequestURI().getQuery();
         String fullUrl = targetUrl + "?" + params;
         HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fullUrl)).GET().build();
         OutputStream stream = exchange.getResponseBody();

         try {
            HttpResponse<String> response = ForwardServer.HTTP_CLIENT.send(request, BodyHandlers.ofString());
            byte[] data = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(response.statusCode(), data.length);
            stream.write(data);
            exchange.close();
         } catch (InterruptedException var10) {
            byte[] datax = var10.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, datax.length);
            stream.write(datax);
            exchange.close();
         }
      }
   }
}
