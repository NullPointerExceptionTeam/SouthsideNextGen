package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.southside.MinecraftAccessor;
import dev.southside.command.Command;
import dev.southside.utils.player.FakePlayerEntity;
import net.minecraft.command.CommandSource;

public class FakePlayerCommand extends Command {
   private FakePlayerEntity fakePlayer;

   public FakePlayerCommand() {
      super("fakeplayer", "Spawn or remove a stationary fake player.");
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(context -> {
         if (this.fakePlayer != null) {
            if (MinecraftAccessor.mc.world != null) {
               this.fakePlayer.despawn();
            }

            this.fakePlayer = null;
            this.info("Fake player removed.", new Object[0]);
            return 1;
         } else if (MinecraftAccessor.mc.player != null && MinecraftAccessor.mc.world != null) {
            this.fakePlayer = new FakePlayerEntity(MinecraftAccessor.mc.player, "fakeplayer", 20.0F, true);
            this.fakePlayer.doNotPush = true;
            this.fakePlayer.hideWhenInsideCamera = true;
            this.fakePlayer.spawn();
            this.fakePlayer.setVelocity(0.0, 0.0, 0.0);
            this.info("Spawned fake player 'fakeplayer'.", new Object[0]);
            return 1;
         } else {
            this.error("You must be in a world to use this command.", new Object[0]);
            return 1;
         }
      });
   }
}
