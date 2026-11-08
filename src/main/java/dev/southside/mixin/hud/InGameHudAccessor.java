package dev.southside.mixin.hud;

import java.util.Comparator;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_9011;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({class_329.class})
public interface InGameHudAccessor {
   @Accessor("field_2035")
   class_310 getClient();

   @Accessor("field_47550")
   static Comparator<class_9011> getScoreboardEntryComparator() {
      return null;
   }

   @Invoker("method_1756")
   class_327 invokeGetTextRenderer();
}
