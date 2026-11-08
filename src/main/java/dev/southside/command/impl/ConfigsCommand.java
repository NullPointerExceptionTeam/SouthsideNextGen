package dev.southside.command.impl;

import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.ConfigsArgumentType;
import dev.southside.config.ConfigInfo;
import net.minecraft.class_124;
import net.minecraft.class_2172;

public class ConfigsCommand extends Command {
   public ConfigsCommand() {
      super("config", "Config command", "cfg");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.then(literal("list").executes(context -> {
         Southside.INSTANCE.getConfigManager().refreshConfigs();
         this.info("------Configs------", new Object[0]);

         for (ConfigInfo file : Southside.get().getConfigManager().configs) {
            this.message(class_124.field_1061 + file.name, new Object[0]);
         }

         return 1;
      }));
      builder.then(literal("load").then(argument("name", ConfigsArgumentType.create()).executes(context -> {
         Southside.INSTANCE.getConfigManager().refreshConfigs();
         String config = (String)context.getArgument("name", String.class);

         try {
            ConfigCore.saveAll();
            Southside.INSTANCE.getConfigManager().loadConfig(config);
            this.success("成功加载参数 " + config + ".", new Object[0]);
         } catch (Exception var4) {
            this.error("Error: " + var4, new Object[0]);
         }

         return 1;
      })));
      builder.then(literal("remove").then(argument("name", ConfigsArgumentType.create()).executes(context -> {
         Southside.INSTANCE.getConfigManager().refreshConfigs();
         String config = (String)context.getArgument("name", String.class);

         try {
            Southside.INSTANCE.getConfigManager().removeConfig(config);
            this.success("成功删除参数 " + config + ".", new Object[0]);
         } catch (Exception var4) {
            this.error("Error: " + var4, new Object[0]);
         }

         return 1;
      })));
      builder.then(literal("save").then(argument("name", StringArgumentType.string()).executes(context -> {
         String name = (String)context.getArgument("name", String.class);

         try {
            Southside.INSTANCE.getConfigManager().saveConfig(name);
            this.success("成功保存参数 " + name + ".", new Object[0]);
         } catch (Exception var4) {
            this.error("Error: " + var4, new Object[0]);
         }

         return 1;
      })));
   }
}
