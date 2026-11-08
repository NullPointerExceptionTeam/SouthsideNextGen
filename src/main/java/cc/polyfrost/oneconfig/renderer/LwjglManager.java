package cc.polyfrost.oneconfig.renderer;

import cc.polyfrost.oneconfig.internal.renderer.LwjglManagerImpl;
import cc.polyfrost.oneconfig.renderer.asset.AssetHelper;
import cc.polyfrost.oneconfig.renderer.font.FontHelper;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;

@Deprecated
public interface LwjglManager {
   LwjglManager INSTANCE = new LwjglManagerImpl();

   @Deprecated
   NanoVGHelper getNanoVGHelper();

   @Deprecated
   ScissorHelper getScissorHelper();

   @Deprecated
   AssetHelper getAssetHelper();

   @Deprecated
   FontHelper getFontHelper();

   TinyFD getTinyFD();
}
