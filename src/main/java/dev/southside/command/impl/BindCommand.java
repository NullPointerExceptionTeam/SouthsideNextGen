package dev.southside.command.impl;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.KeybindingArgumentType;
import dev.southside.command.args.ModuleArgumentType;
import dev.southside.module.Module;
import java.util.ArrayList;
import net.minecraft.command.CommandSource;
import net.minecraft.client.util.InputUtil;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "Bound a module to a key", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(BindCommand.literal("list").executes(context -> {
            boolean hasBoundModules = false;
            for (Module module : Southside.get().getModuleManager().getModules()) {
                Integer bind;
                ArrayList<Integer> keyBinds = ((OneKeyBind)module.getKeyBind().getValue()).getKeyBinds();
                if (keyBinds.isEmpty() || (bind = keyBinds.getFirst()) == 0 || bind == -1) continue;
                this.info("- {}: {}", module.getDisplayName(), InputUtil.fromKeyCode((int)bind, (int)0).getLocalizedText().getString());
                hasBoundModules = true;
            }
            if (!hasBoundModules) {
                this.info("No modules are currently bound to any keys.", new Object[0]);
            }
            return 1;
        }));
        builder.then(BindCommand.argument("module", ModuleArgumentType.create()).then(BindCommand.argument("bind", KeybindingArgumentType.keybinding()).executes(context -> {
            Module module = (Module)context.getArgument("module", Module.class);
            KeybindingArgumentType.Keybinding bind = (KeybindingArgumentType.Keybinding)context.getArgument("bind", KeybindingArgumentType.Keybinding.class);
            ((OneKeyBind)module.keyBind.getValue()).clearKeys();
            ((OneKeyBind)module.keyBind.getValue()).addKey(bind.key());
            module.info("Bound module '" + module.getDisplayName() + "' to " + bind.name(), new Object[0]);
            return 1;
        })));
    }
}

