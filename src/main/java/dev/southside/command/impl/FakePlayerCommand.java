package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.command.Command;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.class_2172;

public class FakePlayerCommand extends Command {
   private FakePlayerEntity fakePlayer;

   public FakePlayerCommand() {
      super("fakeplayer", "Spawn or remove a stationary fake player.");
   }

   @Override
   public void build(LiteralArgumentBuilder<class_2172> builder) {
      builder.executes(context -> {
         if (this.fakePlayer != null) {
            if (MinecraftAccessor.mc.field_1687 != null) {
               this.fakePlayer.despawn();
            }

            this.fakePlayer = null;
            this.info("Fake player removed.", new Object[0]);
            return 1;
         } else if (MinecraftAccessor.mc.field_1724 != null && MinecraftAccessor.mc.field_1687 != null) {
            this.fakePlayer = new FakePlayerEntity(MinecraftAccessor.mc.field_1724, "fakeplayer", 20.0F, true);
            this.fakePlayer.doNotPush = true;
            this.fakePlayer.hideWhenInsideCamera = true;
            this.fakePlayer.spawn();
            this.fakePlayer.method_18800(0.0, 0.0, 0.0);
            this.info("Spawned fake player 'fakeplayer'.", new Object[0]);
            return 1;
         } else {
            this.error("You must be in a world to use this command.", new Object[0]);
            return 1;
         }
      });
   }
}
