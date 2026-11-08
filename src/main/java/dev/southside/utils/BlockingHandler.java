package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.render.OldHitting;

public class BlockingHandler {
   public static boolean isBlocking() {
      ModuleManager moduleManager = Southside.get().getModuleManager();
      return moduleManager.isEnabled(OldHitting.class) && moduleManager.isEnabled(KillAura.class) && KillAura.isBlocking();
   }
}
