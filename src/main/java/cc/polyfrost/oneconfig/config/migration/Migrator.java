package cc.polyfrost.oneconfig.config.migration;

import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Migrator {
   @Nullable
   Object getValue(Field var1, @NotNull String var2, String var3, String var4);
}
