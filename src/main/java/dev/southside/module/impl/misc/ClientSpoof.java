package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class ClientSpoof extends Module {
   public Dropdown spoofmode = new Dropdown("Spoof Mode", "Forge", "Vanilla", "Forge");

   public ClientSpoof() {
      super(Category.Misc, "ClientSpoof", "Spoof your client");
   }

   public String getClient() {
      String var1 = this.spoofmode.getMode();

      return switch (var1) {
         case "Vanilla" -> "vanilla";
         case "Forge" -> "forge";
         default -> "fabric";
      };
   }
}
