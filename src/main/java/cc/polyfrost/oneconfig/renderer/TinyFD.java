package cc.polyfrost.oneconfig.renderer;

import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TinyFD {
   String QUESTION_ICON = "question";
   String ERROR_ICON = "error";
   String WARNING_ICON = "warning";
   String INFO_ICON = "info";
   String OK_DIALOG = "ok";
   String OK_CANCEL_DIALOG = "okcancel";
   String YES_NO_DIALOG = "yesno";
   String YES_NO_CANCEL_DIALOG = "yesnocancel";
   TinyFD INSTANCE = LwjglManager.INSTANCE.getTinyFD();

   File openSaveSelector(@Nullable String var1, @Nullable String var2, @Nullable String[] var3, @Nullable String var4);

   @Nullable
   File openFileSelector(@Nullable String var1, @Nullable String var2, @Nullable String[] var3, @Nullable String var4);

   File[] openMultiFileSelector(@Nullable String var1, @Nullable String var2, @Nullable String[] var3, @Nullable String var4);

   File openFolderSelector(@Nullable String var1, @Nullable String var2);

   boolean showMessageBox(String var1, String var2, @NotNull String var3, String var4, boolean var5);

   int showNotification(String var1, String var2, String var3);
}
