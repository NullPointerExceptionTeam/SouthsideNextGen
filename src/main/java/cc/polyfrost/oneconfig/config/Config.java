package cc.polyfrost.oneconfig.config;

import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.config.elements.OptionSubcategory;
import cc.polyfrost.oneconfig.config.options.Option;
import cc.polyfrost.oneconfig.config.options.WrappedValue;
import cc.polyfrost.oneconfig.config.options.impl.HUD;
import cc.polyfrost.oneconfig.config.options.impl.Page;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigHeader;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigKeyBind;
import cc.polyfrost.oneconfig.gui.elements.config.ConfigPageButton;
import cc.polyfrost.oneconfig.gui.pages.ModConfigPage;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.hud.Position;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import cc.polyfrost.oneconfig.internal.config.core.KeyBindHandler;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.southside.Southside;
import dev.southside.module.Module;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_437;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class Config {
   public final transient HashMap<String, BasicOption> optionNames = new HashMap<>();
   protected final transient Gson gson = this.addGsonOptions(new GsonBuilder()).create();
   protected final transient List<Consumer<Boolean>> toggleListener = new ArrayList<>();
   public transient Mod mod;
   public WrappedValue<Boolean> enabled = new WrappedValue<Boolean>("Enabled", false) {
      public void setValue(Boolean value) {
         super.setValue(value);
         Config.this.toggleListener.forEach(e -> e.accept(value));
      }
   };
   public final boolean canToggle;
   private final transient Logger logger;

   public Config(Mod modData, boolean enabled, boolean canToggle) {
      this.mod = modData;
      this.enabled.setValue(enabled);
      this.canToggle = canToggle;
      this.logger = LogManager.getLogger(this.getClass());
   }

   public Config(Mod modData, boolean enabled) {
      this(modData, enabled, true);
   }

   public Config(Mod modData) {
      this(modData, false);
   }

   public void preInitialize() {
      this.mod.config = this;
   }

   public void initialize() {
      ConfigCore.mods.add(this.mod);
   }

   public void setEnable(boolean enabled) {
      this.enabled.setValue(enabled);
   }

   public boolean isEnabled() {
      return this.enabled.getValue();
   }

   public void registerToggleListener(Consumer<Boolean> l) {
      this.toggleListener.add(l);
   }

   public void reInitialize() {
      this.logger.trace("Reinitializing config for {}...", this.mod.name);
      if (this instanceof Module module) {
         try {
            Southside.get().getModuleManager().reInit(List.of(module));
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      } else {
         this.initialize();
      }
   }

   public void reset() {
      try {
         parseConfig(null, this);
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public JsonObject save() {
      this.logger.trace("Saving config for {}...", this.mod.name);
      JsonObject config = new JsonObject();

      try {
         saveConfig(config, this);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return config;
   }

   public void load(JsonObject base) {
      this.logger.trace("Loading config for {}...", this.mod.name);

      try {
         JsonObject config = base.getAsJsonObject(this.mod.name);
         parseConfig(config, this);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public static void parseConfig(JsonObject config, Object object) throws Exception {
      ArrayList<Field> fields = new ArrayList<>(Arrays.stream(object.getClass().getFields()).toList());
      fields.addAll(Arrays.stream(object.getClass().getDeclaredFields()).toList());

      for (Field field : fields) {
         boolean darkMagic = false;
         Object obj;
         if (!(object instanceof WrappedValue) && !(object instanceof Option)) {
            boolean staticFlag = Modifier.isStatic(field.getModifiers());
            if (!field.canAccess(staticFlag ? null : object) || Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);
            }

            obj = field.get(staticFlag ? null : object);
         } else {
            obj = object;
            darkMagic = true;
         }

         if (obj instanceof WrappedValue<?> wrappedValue) {
            String label = wrappedValue.getLabel();
            Class<?> targetClass = wrappedValue.getTargetClass();
            if (config == null) {
               if (label.equals("Enabled") && object instanceof Module module) {
                  module.setEnable((Boolean)wrappedValue.getDefaultValue());
                  continue;
               }

               wrappedValue.reset();
               continue;
            }

            JsonElement value = config.get(label);
            if (value != null) {
               if (targetClass.isAssignableFrom(Boolean.class)) {
                  if (label.equals("Enabled") && object instanceof Module module) {
                     module.setEnable(value.getAsBoolean());
                  } else {
                     ((WrappedValue<Boolean>)wrappedValue).setValue(value.getAsBoolean());
                  }
               } else if (targetClass.isAssignableFrom(Character.class)) {
                  ((WrappedValue<Character>)wrappedValue).setValue(value.getAsCharacter());
               } else if (targetClass.isAssignableFrom(Short.class)) {
                  ((WrappedValue<Short>)wrappedValue).setValue(value.getAsShort());
               } else if (targetClass.isAssignableFrom(Byte.class)) {
                  ((WrappedValue<Byte>)wrappedValue).setValue(value.getAsByte());
               } else if (targetClass.isAssignableFrom(Integer.class)) {
                  ((WrappedValue<Integer>)wrappedValue).setValue(value.getAsInt());
               } else if (targetClass.isAssignableFrom(Float.class)) {
                  ((WrappedValue<Float>)wrappedValue).setValue(value.getAsFloat());
               } else if (targetClass.isAssignableFrom(Long.class)) {
                  ((WrappedValue<Long>)wrappedValue).setValue(value.getAsLong());
               } else if (targetClass.isAssignableFrom(Double.class)) {
                  ((WrappedValue<Double>)wrappedValue).setValue(value.getAsDouble());
               } else if (targetClass.isAssignableFrom(String.class)) {
                  ((WrappedValue<String>)wrappedValue).setValue(value.getAsString());
               } else if (targetClass.isAssignableFrom(Position.class)) {
                  JsonObject positionObject = value.getAsJsonObject();
                  Position position = (Position)wrappedValue.getValue();
                  float x = positionObject.get("x").getAsFloat();
                  float y = positionObject.get("y").getAsFloat();
                  position = new Position(position.getHud(), x, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
                  int anchor = Integer.parseInt(positionObject.get("anchor").getAsString());
                  position.anchor = Position.AnchorPosition.values()[anchor];
                  ((WrappedValue<Position>)wrappedValue).setValue(position);
               }
            }
         } else if (obj instanceof Option<?> option) {
            String labelx = option.getLabel();
            if (config == null) {
               if (obj instanceof HUD) {
                  parseConfig(null, option.getValue());
               } else if (obj instanceof Page) {
                  Object pageInstance = option.getValue();
                  if (!(pageInstance instanceof cc.polyfrost.oneconfig.gui.pages.Page)) {
                     parseConfig(null, option.getValue());
                  }
               } else {
                  option.reset();
               }
               continue;
            }

            JsonElement value = config.get(labelx);
            if (value != null) {
               if (obj instanceof HUD) {
                  parseConfig(value.getAsJsonObject(), option.getValue());
               } else if (obj instanceof Page) {
                  Object pageInstance = option.getValue();
                  if (!(pageInstance instanceof cc.polyfrost.oneconfig.gui.pages.Page)) {
                     parseConfig(value.getAsJsonObject(), option.getValue());
                  }
               } else {
                  switch (option.type()) {
                     case SWITCH:
                     case CHECKBOX:
                     case DUAL_OPTION:
                        ((Option<Boolean>)option).setValue(value.getAsBoolean());
                        break;
                     case TEXT:
                        ((Option<String>)option).setValue(value.getAsString());
                        break;
                     case SLIDER:
                     case NUMBER:
                        ((Option<Number>)option).setValue(value.getAsNumber());
                        break;
                     case COLOR: {
                        JsonObject jsonObject = value.getAsJsonObject();
                        JsonArray hsba = jsonObject.get("hsba").getAsJsonArray();
                        JsonPrimitive dataBit = jsonObject.get("dataBit").getAsJsonPrimitive();
                        OneColor oneColor = new OneColor(
                           hsba.get(0).getAsNumber().intValue(),
                           hsba.get(1).getAsNumber().intValue(),
                           hsba.get(2).getAsNumber().intValue(),
                           hsba.get(3).getAsNumber().intValue(),
                           dataBit.getAsInt()
                        );
                        ((Option<OneColor>)option).setValue(oneColor);
                        break;
                     }
                     case DROPDOWN:
                        ((Option<Integer>)option).setValue(value.getAsNumber().intValue());
                        break;
                     case KEYBIND: {
                        Option<OneKeyBind> casted = (Option<OneKeyBind>)option;
                        ((OneKeyBind)option.value).keyBinds.clear();
                        JsonObject jsonObject = value.getAsJsonObject();
                        JsonArray keyBinds = jsonObject.get("keyBinds").getAsJsonArray();
                        int[] keyBindsArray = new int[keyBinds.size()];

                        for (int i = 0; i < keyBinds.size(); i++) {
                           keyBindsArray[i] = keyBinds.get(i).getAsNumber().intValue();
                        }

                        for (int key : keyBindsArray) {
                           casted.value.keyBinds.add(key);
                        }

                        if (casted.basicOption != null) {
                           casted.basicOption.triggerListeners();
                        }
                     }
                  }
               }
            }
         }

         if (darkMagic) {
            break;
         }
      }
   }

   public static JsonObject saveConfig(JsonObject config, Object object) throws Exception {
      ArrayList<Field> fields = new ArrayList<>(Arrays.stream(object.getClass().getFields()).toList());
      fields.addAll(Arrays.stream(object.getClass().getDeclaredFields()).toList());

      for (Field field : fields) {
         boolean darkMagic = false;
         Object obj;
         if (!(object instanceof WrappedValue) && !(object instanceof Option)) {
            boolean staticFlag = Modifier.isStatic(field.getModifiers());
            if (Modifier.isTransient(field.getModifiers())) {
               continue;
            }

            if (!field.canAccess(staticFlag ? null : object)) {
               field.setAccessible(true);
            }

            obj = field.get(staticFlag ? null : object);
         } else {
            obj = object;
            darkMagic = true;
         }

         if (obj instanceof WrappedValue<?> wrappedValue) {
            String label = wrappedValue.getLabel();
            Class<?> targetClass = wrappedValue.getTargetClass();
            Object value = wrappedValue.getValue();
            if (value != null) {
               if (targetClass.isAssignableFrom(Boolean.class)) {
                  config.addProperty(label, (Boolean)value);
               } else if (targetClass.isAssignableFrom(Character.class)) {
                  config.addProperty(label, (Character)value);
               } else if (targetClass.isAssignableFrom(Short.class)) {
                  config.addProperty(label, (Short)value);
               } else if (targetClass.isAssignableFrom(Byte.class)) {
                  config.addProperty(label, (Byte)value);
               } else if (targetClass.isAssignableFrom(Integer.class)) {
                  config.addProperty(label, (Integer)value);
               } else if (targetClass.isAssignableFrom(Float.class)) {
                  config.addProperty(label, (Float)value);
               } else if (targetClass.isAssignableFrom(Long.class)) {
                  config.addProperty(label, (Long)value);
               } else if (targetClass.isAssignableFrom(Double.class)) {
                  config.addProperty(label, (Double)value);
               } else if (targetClass.isAssignableFrom(String.class)) {
                  config.addProperty(label, (String)value);
               } else if (targetClass.isAssignableFrom(Position.class)) {
                  Position position = (Position)value;
                  JsonObject positionObject = new JsonObject();
                  positionObject.addProperty("anchor", String.valueOf(position.anchor.ordinal()));
                  positionObject.addProperty("x", position.x);
                  positionObject.addProperty("y", position.y);
                  config.add(label, positionObject);
               }
            }
         } else if (obj instanceof Option<?> option) {
            String label = option.getLabel();
            Object value = option.getValue();
            if (value != null) {
               if (obj instanceof HUD) {
                  config.add(label, saveConfig(new JsonObject(), value));
               } else if (obj instanceof Page) {
                  Object pageInstance = option.getValue();
                  if (!(pageInstance instanceof cc.polyfrost.oneconfig.gui.pages.Page)) {
                     config.add(label, saveConfig(new JsonObject(), value));
                  }
               } else {
                  switch (option.type()) {
                     case SWITCH:
                     case CHECKBOX:
                     case DUAL_OPTION:
                        config.addProperty(label, (Boolean)value);
                        break;
                     case TEXT:
                        config.addProperty(label, (String)value);
                        break;
                     case SLIDER:
                     case NUMBER:
                        config.addProperty(label, (Number)value);
                        break;
                     case COLOR:
                        OneColor oneColor = (OneColor)value;
                        JsonObject oneColorObject = new JsonObject();
                        JsonArray hsba = new JsonArray();

                        for (short i : oneColor.hsba) {
                           hsba.add(i);
                        }

                        oneColorObject.add("hsba", hsba);
                        oneColorObject.addProperty("dataBit", oneColor.dataBit);
                        config.add(label, oneColorObject);
                        break;
                     case DROPDOWN:
                        config.addProperty(label, (Integer)value);
                        break;
                     case KEYBIND:
                        OneKeyBind oneKeyBind = (OneKeyBind)value;
                        JsonObject oneKeyBindObject = new JsonObject();
                        JsonArray keyBinds = new JsonArray();

                        for (Integer keyBind : oneKeyBind.keyBinds) {
                           keyBinds.add(keyBind);
                        }

                        oneKeyBindObject.add("keyBinds", keyBinds);
                        config.add(label, oneKeyBindObject);
                  }
               }
            }
         }

         if (darkMagic) {
            break;
         }
      }

      return config;
   }

   protected final ArrayList<BasicOption> generateOptionList(Object instance, OptionPage page, Mod mod, boolean migrate) {
      return this.generateOptionList(new ArrayList<>(), instance, instance.getClass(), page, mod, migrate);
   }

   protected final ArrayList<BasicOption> generateOptionList(
      ArrayList<BasicOption> des, Object instance, Class<?> targetClass, OptionPage page, Mod mod, boolean migrate, Object... context
   ) {
      Objects.requireNonNull(des);
      this.optionNames.clear();
      this.logger.trace("Generating option list for {}... (targetting={})", mod.name, targetClass.getName());
      Class<?> superclass = targetClass.getSuperclass();
      if (superclass != Object.class) {
         des.addAll(this.generateOptionList(new ArrayList<>(), instance, superclass, page, mod, migrate, context));
      }

      for (Field field : targetClass.getDeclaredFields()) {
         boolean staticFlag = Modifier.isStatic(field.getModifiers());
         if (!field.canAccess(staticFlag ? null : instance)) {
            field.setAccessible(true);
         }

         Object obj = null;

         try {
            obj = field.get(staticFlag ? null : instance);
         } catch (IllegalAccessException var23) {
            var23.printStackTrace();
            continue;
         }

         if (obj instanceof Option<?> option) {
            if (obj instanceof HUD hudOption) {
               hudOption.getValue().position.getValue().setHud(hudOption.getValue());
               hudOption.getValue().setConfig(this);
               HudCore.huds.put(hudOption, hudOption.getValue());
               ArrayList<BasicOption> basicOptions = this.generateOptionList(new ArrayList<BasicOption>() {
                  {
                     this.add(new ConfigHeader(null, null, hudOption.getLabel(), hudOption.category(), hudOption.subcategory(), 2));
                  }
               }, hudOption.getValue(), hudOption.getValue().getClass(), page, mod, migrate, hudOption.category(), hudOption.subcategory());
               HudCore.hudOptions.addAll(basicOptions);
               if (hudOption.getValue() instanceof BasicHud hud) {
                  try {
                     boolean changed = false;
                     if (hud.paddingX.getValue().floatValue() > 10.0F) {
                        hud.paddingX.setValue(5.0F);
                     }

                     if (hud.paddingY.getValue().floatValue() > 10.0F) {
                        hud.paddingY.setValue(5.0F);
                     }

                     if (changed) {
                        this.save();
                     }
                  } catch (Exception var22) {
                     var22.printStackTrace();
                  }
               }

               ConfigUtils.getSubCategory(page, option.category(), option.subcategory()).options.addAll(basicOptions);
            } else if (obj instanceof Page pageOption) {
               OptionSubcategory subcategory = ConfigUtils.getSubCategory(page, pageOption.category(), pageOption.subcategory());
               Object pageInstance = pageOption.getValue();
               if (pageInstance != null) {
                  ConfigPageButton button;
                  if (pageInstance instanceof cc.polyfrost.oneconfig.gui.pages.Page) {
                     button = new ConfigPageButton(
                        field,
                        instance,
                        pageOption.getLabel(),
                        pageOption.getDescription(),
                        pageOption.category(),
                        pageOption.subcategory(),
                        (cc.polyfrost.oneconfig.gui.pages.Page)pageInstance
                     );
                  } else {
                     OptionPage newPage = new OptionPage(pageOption.getLabel(), mod);
                     this.generateOptionList(pageInstance, newPage, mod, migrate);
                     button = new ConfigPageButton(
                        field, instance, pageOption.getLabel(), pageOption.getDescription(), pageOption.category(), pageOption.subcategory(), newPage
                     );
                  }

                  if (pageOption.getLocation() == PageLocation.TOP) {
                     subcategory.topButtons.add(button);
                  } else {
                     subcategory.bottomButtons.add(button);
                  }
               }
            } else {
               BasicOption configOption;
               if (context.length == 2) {
                  configOption = ConfigUtils.getOption2(option, (String)context[0], (String)context[1]);
                  this.optionNames.put(option.getLabel(), configOption);
               } else {
                  configOption = ConfigUtils.addOptionToPage2(page, option);
                  this.optionNames.put(option.getLabel(), configOption);
               }

               des.add(configOption);
            }
         }
      }

      this.logger.trace("Finished generating option list for {} (targetting={})", mod.name, targetClass.getName());
      return des;
   }

   protected GsonBuilder addGsonOptions(GsonBuilder builder) {
      return builder.excludeFieldsWithModifiers(new int[]{128}).setPrettyPrinting();
   }

   public void openGui() {
      if (this.mod != null) {
         GuiUtils.displayScreen((class_437)(new OneConfigGui(new ModConfigPage(this.mod.defaultPage))));
      }
   }

   protected final void addDependency(String option, String conditionName, Supplier<Boolean> condition) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addDependency(conditionName, condition);
      }
   }

   protected final void addDependency(String option, Supplier<Boolean> condition) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addDependency(condition);
      }
   }

   protected final void addDependency(String option, String dependentOption) {
      if (this.optionNames.containsKey(option) && this.optionNames.containsKey(dependentOption)) {
         BasicOption optionObj = this.optionNames.get(dependentOption);
         this.optionNames.get(option).addDependency(optionObj.name, () -> {
            try {
               return (Boolean)optionObj.get();
            } catch (IllegalAccessException var2) {
               return true;
            }
         });
      }
   }

   @Deprecated
   protected final void addDependency(String option, boolean value) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addDependency(() -> value);
      }
   }

   protected final void hideIf(String option, Supplier<Boolean> condition) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addHideCondition(condition);
      }
   }

   protected final void hideIf(String option, String dependentOption) {
      if (this.optionNames.containsKey(option) && this.optionNames.containsKey(dependentOption)) {
         this.optionNames.get(option).addHideCondition(() -> {
            try {
               return (Boolean)this.optionNames.get(dependentOption).get();
            } catch (IllegalAccessException var3) {
               return true;
            }
         });
      }
   }

   @Deprecated
   protected final void hideIf(String option, boolean value) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addHideCondition(() -> value);
      }
   }

   protected final void addListener(String option, Runnable runnable) {
      if (this.optionNames.containsKey(option)) {
         this.optionNames.get(option).addListener(runnable);
      }
   }

   protected final void registerKeyBind(OneKeyBind keyBind, Runnable runnable) {
      Field field = null;
      Object instance = null;

      for (BasicOption option : this.optionNames.values()) {
         if (option instanceof ConfigKeyBind) {
            try {
               Field f = option.getField();
               OneKeyBind keyBind1 = (OneKeyBind)option.get();
               if (keyBind1 == keyBind) {
                  field = f;
                  instance = option.getParent();
                  break;
               }
            } catch (IllegalAccessException var9) {
            }
         }
      }

      keyBind.setRunnable(runnable);
      KeyBindHandler.INSTANCE.addKeyBind(field, instance, keyBind);
   }

   public boolean supportsProfiles() {
      return true;
   }

   @Experimental
   @Contract("null -> null")
   public static Mod register(Mod mod) {
      if (mod == null) {
         return null;
      } else if (ConfigCore.mods.contains(mod)) {
         return mod;
      } else {
         ConfigCore.mods.add(mod);
         ConfigCore.sortMods();
         return null;
      }
   }

   public final void preload() {
   }
}
