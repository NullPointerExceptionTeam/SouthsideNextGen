package dev.southside.mixininterfaces;

public interface IMinecraftClient {
   boolean getCurrentIsInGame();

   void setCurrentIsInGame(boolean var1);

   void setAttackCooldown(int var1);

   void leftClick();

   boolean getIsLoaded();

   int getStartTick();

   void rightClick();
}
