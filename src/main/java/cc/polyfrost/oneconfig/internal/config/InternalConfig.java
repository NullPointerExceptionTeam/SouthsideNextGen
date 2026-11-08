package cc.polyfrost.oneconfig.internal.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.southside.Southside;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class InternalConfig extends Config {
   protected final transient Gson gson = this.addGsonOptions(new GsonBuilder()).create();
   private final File configFile;

   public InternalConfig(String title, String configFile) {
      super(new Mod(title, null));
      this.configFile = Southside.FOLDER.toPath().resolve(configFile).toFile();
   }

   @Override
   public void initialize() {
      if (this.configFile.exists()) {
         this.load(null);
      } else {
         this.save();
      }

      this.generateOptionList(this, this.mod.defaultPage, this.mod, false);
      this.mod.config = this;
   }

   @Override
   public JsonObject save() {
      try {
         if (!this.configFile.exists()) {
            this.configFile.createNewFile();
         }
      } catch (IOException var7) {
      }

      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(this.configFile.toPath()), StandardCharsets.UTF_8))) {
         JsonObject config = new JsonObject();
         writer.write(this.gson.toJson(saveConfig(config, this)));
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return new JsonObject();
   }

   @Override
   public void reset() {
   }

   @Override
   public void load(JsonObject base) {
      Path path = this.configFile.toPath();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
         JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
         parseConfig(config, this);
      } catch (Exception var8) {
         var8.printStackTrace();
         File file = path.toFile();
         file.renameTo(new File(file.getParentFile(), file.getName() + ".corrupted"));
      }
   }
}
