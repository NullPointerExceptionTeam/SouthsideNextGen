package dev.southside.sound;

import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

public class Sounds {
   public static final class_2960 SIGMA_ACTIVATE_SOUND = class_2960.method_60654("southside:sactivate");
   public static final class_2960 SIGMA_DEACTIVATE_SOUND = class_2960.method_60654("southside:sdeactivate");
   public static final class_2960 SOUTHSIDE_TOGGLE_ON_SOUND = class_2960.method_60655("southside", "toggleon");
   public static final class_2960 SOUTHSIDE_TOGGLE_OFF_SOUND = class_2960.method_60655("southside", "toggleoff");
   public static final class_2960 SOUTHSIDE_TOGGLE2_ON_SOUND = class_2960.method_60655("southside", "toggleon2");
   public static final class_2960 SOUTHSIDE_TOGGLE2_OFF_SOUND = class_2960.method_60655("southside", "toggleoff2");
   public static final class_3414 SIGMA_ACTIVATE_SOUNDEVENT = class_3414.method_47908(SIGMA_ACTIVATE_SOUND);
   public static final class_3414 SIGMA_DEACTIVATE_SOUNDEVENT = class_3414.method_47908(SIGMA_DEACTIVATE_SOUND);
   public static final class_3414 SOUTHSIDE_TOGGLE_ON_SOUNDEVENT = class_3414.method_47908(SOUTHSIDE_TOGGLE_ON_SOUND);
   public static final class_3414 SOUTHSIDE_TOGGLE_OFF_SOUNDEVENT = class_3414.method_47908(SOUTHSIDE_TOGGLE_OFF_SOUND);
   public static final class_3414 SOUTHSIDE_TOGGLE2_ON_SOUNDEVENT = class_3414.method_47908(SOUTHSIDE_TOGGLE2_ON_SOUND);
   public static final class_3414 SOUTHSIDE_TOGGLE2_OFF_SOUNDEVENT = class_3414.method_47908(SOUTHSIDE_TOGGLE2_OFF_SOUND);

   public static void init() {
      class_2378.method_10230(class_7923.field_41172, SIGMA_ACTIVATE_SOUND, SIGMA_ACTIVATE_SOUNDEVENT);
      class_2378.method_10230(class_7923.field_41172, SIGMA_DEACTIVATE_SOUND, SIGMA_DEACTIVATE_SOUNDEVENT);
      class_2378.method_10230(class_7923.field_41172, SOUTHSIDE_TOGGLE_ON_SOUND, SOUTHSIDE_TOGGLE_ON_SOUNDEVENT);
      class_2378.method_10230(class_7923.field_41172, SOUTHSIDE_TOGGLE_OFF_SOUND, SOUTHSIDE_TOGGLE_OFF_SOUNDEVENT);
      class_2378.method_10230(class_7923.field_41172, SOUTHSIDE_TOGGLE2_ON_SOUND, SOUTHSIDE_TOGGLE2_ON_SOUNDEVENT);
      class_2378.method_10230(class_7923.field_41172, SOUTHSIDE_TOGGLE2_OFF_SOUND, SOUTHSIDE_TOGGLE2_OFF_SOUNDEVENT);
   }
}
