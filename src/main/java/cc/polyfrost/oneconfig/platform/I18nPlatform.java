package cc.polyfrost.oneconfig.platform;

public interface I18nPlatform {
   String format(String var1, Object... var2);

   String getKeyName(int var1, int var2);

   default String getKeyName(int key) {
      return this.getKeyName(key, 28);
   }
}
