package dev.southside.sound;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;

public class Sounds {
   public static final Identifier SIGMA_ACTIVATE_SOUND = Identifier.of("southside:sactivate");
   public static final Identifier SIGMA_DEACTIVATE_SOUND = Identifier.of("southside:sdeactivate");
   public static final Identifier SOUTHSIDE_TOGGLE_ON_SOUND = Identifier.of("southside", "toggleon");
   public static final Identifier SOUTHSIDE_TOGGLE_OFF_SOUND = Identifier.of("southside", "toggleoff");
   public static final Identifier SOUTHSIDE_TOGGLE2_ON_SOUND = Identifier.of("southside", "toggleon2");
   public static final Identifier SOUTHSIDE_TOGGLE2_OFF_SOUND = Identifier.of("southside", "toggleoff2");
   public static final SoundEvent SIGMA_ACTIVATE_SOUNDEVENT = SoundEvent.of(SIGMA_ACTIVATE_SOUND);
   public static final SoundEvent SIGMA_DEACTIVATE_SOUNDEVENT = SoundEvent.of(SIGMA_DEACTIVATE_SOUND);
   public static final SoundEvent SOUTHSIDE_TOGGLE_ON_SOUNDEVENT = SoundEvent.of(SOUTHSIDE_TOGGLE_ON_SOUND);
   public static final SoundEvent SOUTHSIDE_TOGGLE_OFF_SOUNDEVENT = SoundEvent.of(SOUTHSIDE_TOGGLE_OFF_SOUND);
   public static final SoundEvent SOUTHSIDE_TOGGLE2_ON_SOUNDEVENT = SoundEvent.of(SOUTHSIDE_TOGGLE2_ON_SOUND);
   public static final SoundEvent SOUTHSIDE_TOGGLE2_OFF_SOUNDEVENT = SoundEvent.of(SOUTHSIDE_TOGGLE2_OFF_SOUND);

   public static void init() {
      Registry.register(Registries.SOUND_EVENT, SIGMA_ACTIVATE_SOUND, SIGMA_ACTIVATE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, SIGMA_DEACTIVATE_SOUND, SIGMA_DEACTIVATE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, SOUTHSIDE_TOGGLE_ON_SOUND, SOUTHSIDE_TOGGLE_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, SOUTHSIDE_TOGGLE_OFF_SOUND, SOUTHSIDE_TOGGLE_OFF_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, SOUTHSIDE_TOGGLE2_ON_SOUND, SOUTHSIDE_TOGGLE2_ON_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, SOUTHSIDE_TOGGLE2_OFF_SOUND, SOUTHSIDE_TOGGLE2_OFF_SOUNDEVENT);
   }
}
