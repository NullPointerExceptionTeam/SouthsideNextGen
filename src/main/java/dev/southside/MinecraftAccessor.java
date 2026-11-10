package dev.southside;

import net.minecraft.client.MinecraftClient;

public interface MinecraftAccessor {
   MinecraftClient mc = MinecraftClient.getInstance();
}
