package cc.polyfrost.oneconfig.platform;

public interface ServerPlatform {
   boolean inMultiplayer();

   String getServerBrand();

   boolean doesPlayerExist();
}
