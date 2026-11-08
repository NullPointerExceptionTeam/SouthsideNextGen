package dev.southside.config;

import com.google.gson.JsonObject;
import dev.southside.Southside;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigManager {
   public final List<ConfigInfo> configs = new ArrayList<>();
   private static final File configDir = Southside.FOLDER.toPath().resolve("configs").toFile();

   public ConfigManager() {
      if (!configDir.exists()) {
         configDir.mkdirs();
      }
   }

   public void refreshConfigs() {
      this.configs.clear();

      try (Stream<Path> stream = Files.list(configDir.toPath())) {
         stream.forEach(path -> {
            File c = path.toFile();
            if (c.isFile() && c.getName().endsWith(".json")) {
               this.configs.add(new ConfigInfo(c.getName().replace(".json", ""), c));
            }
         });
      } catch (IOException var6) {
         var6.printStackTrace();
      }
   }

   public void saveConfig(String name) throws IOException {
      File file = new File(configDir, name + ".json");
      if (!file.exists() || !file.isFile()) {
         file.createNewFile();
      }

      JsonObject modulesConfig = new JsonObject();
      Southside.INSTANCE.getModuleManager().getModules().forEach(it -> modulesConfig.add(it.mod.name, it.toTag()));
      PrintWriter printWriter = new PrintWriter(new FileWriter(file));
      printWriter.println(Southside.PRETTY_GSON.toJson(modulesConfig));
      printWriter.close();
      this.configs.add(new ConfigInfo(name, file));
      this.refreshConfigs();
   }

   public boolean has(String name) {
      File configFile = new File(configDir, name + ".json");
      return configFile.exists() && configFile.isFile();
   }

   public void removeConfig(String name) {
      File configFile = new File(configDir, name + ".json");
      if (configFile.exists() && configFile.isFile() && configFile.delete()) {
         this.configs.removeIf(it -> it.path.getName().equals(name + ".json"));
      }
   }

   public void loadConfig(String name) throws IOException {
      for (ConfigInfo config : this.configs) {
         if (config.name.equals(name)) {
            config.apply();
            return;
         }
      }
   }
}
