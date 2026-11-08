package cc.polyfrost.oneconfig.internal.gui;

import cc.polyfrost.oneconfig.internal.gui.impl.BlurHandlerImpl;

public interface BlurHandler {
   BlurHandler INSTANCE = new BlurHandlerImpl();

   void reloadBlur(Object var1);
}
