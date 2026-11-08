package cc.polyfrost.oneconfig.config.core;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.platform.Platform;
import java.util.ArrayList;

public class OneKeyBind {
   public final ArrayList<Integer> keyBinds = new ArrayList<>();
   protected transient Runnable runnable;
   protected transient boolean hasRun;

   public OneKeyBind(boolean mouse, int... keys) {
      for (int key : keys) {
         this.keyBinds.add(mouse ? key - 100 : key);
      }
   }

   public OneKeyBind(int... keys) {
      this(false, keys);
   }

   public OneKeyBind() {
   }

   public boolean isActive() {
      if (this.keyBinds.isEmpty()) {
         return false;
      } else {
         for (int keyBind : this.keyBinds) {
            if (!UKeyboard.isKeyDown(keyBind)) {
               this.hasRun = false;
               return false;
            }
         }

         return true;
      }
   }

   public void run() {
      if (this.runnable != null && !this.hasRun) {
         this.runnable.run();
         this.hasRun = true;
      }
   }

   public String getDisplay() {
      StringBuilder sb = new StringBuilder();

      for (int keyBind : this.keyBinds) {
         if (!sb.isEmpty()) {
            sb.append(" + ");
         }

         sb.append(Platform.getI18nPlatform().getKeyName(keyBind));
      }

      return sb.toString().trim();
   }

   public void addKey(int key) {
      this.addKey(key, false);
   }

   public void addKey(int key, boolean mouse) {
      if (!this.keyBinds.contains(key)) {
         this.keyBinds.add(mouse ? key - 100 : key);
      }
   }

   public void clearKeys() {
      this.keyBinds.clear();
   }

   public int getSize() {
      return this.keyBinds.size();
   }

   public void setRunnable(Runnable runnable) {
      this.runnable = runnable;
   }

   public Runnable getRunnable() {
      return this.runnable;
   }

   public ArrayList<Integer> getKeyBinds() {
      return this.keyBinds;
   }
}
