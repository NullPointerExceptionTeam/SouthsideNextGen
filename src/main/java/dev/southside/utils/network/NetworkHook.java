package dev.southside.utils.network;

import com.mojang.authlib.properties.Property;
import dev.southside.Southside;
import dev.southside.module.impl.misc.ClientSpoof;

public class NetworkHook {
   public static String hookHandshakeHost(String original) {
      return original;
   }

   public static String hookBrand(String original) {
      return Southside.INSTANCE.getModuleManager().get(ClientSpoof.class).isEnabled()
         ? Southside.INSTANCE.getModuleManager().get(ClientSpoof.class).getClient()
         : original;
   }

   public static boolean hookValidate(Property property) {
      return false;
   }
}
