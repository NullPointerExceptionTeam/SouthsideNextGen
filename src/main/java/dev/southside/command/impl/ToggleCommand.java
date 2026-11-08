package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.command.Command;
import dev.southside.command.args.ModuleArgumentType;
import dev.southside.module.Module;
import net.minecraft.class_2172;

public class ToggleCommand extends Command {
   public ToggleCommand() {
      super("toggle", "Toggle a module.", "t");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.then(argument("module", ModuleArgumentType.create()).executes(context -> {
         Module module = (Module)context.getArgument("module", Module.class);
         if (module == null) {
            this.info("Module '" + module.getDisplayName() + "' not found.", new Object[0]);
            return 1;
         } else {
            module.toggle();
            this.info(module.getName() + " is " + (module.isEnabled() ? "enabled" : "disabled"), new Object[0]);
            return 1;
         }
      }));
   }
}
