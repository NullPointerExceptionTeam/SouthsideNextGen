package dev.southside.module;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.options.impl.KeyBind;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import com.google.gson.JsonObject;
import dev.southside.Southside;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.setting.Setting;
import dev.southside.sound.Sounds;
import dev.southside.utils.ChatAdapter;
import dev.southside.utils.json.ISerializable;
import dev.southside.utils.misc.ClassUtils;
import dev.southside.utils.misc.ClientUtils;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public abstract class Module extends Config implements ChatAdapter, ISerializable<Module>, Comparable<Module> {
   protected Runnable initPostRunnable;
   protected static final MinecraftClient mc = MinecraftClient.getInstance();
   protected boolean notifyOnToggle = true;
   private final Random random = Random.create();
   private final Category category;
   private final boolean autoEnable;
   private final String name;
   private final String desc;
   public KeyBind keyBind;
   private String suffix = "";
   private String displayName;
   private boolean visible = true;

   public Module(Category category, String name, String desc, int defaultKey, boolean autoEnable, boolean visible) {
      super(null);
      this.category = category;
      this.name = name;
      this.desc = desc;
      this.displayName = name;
      this.keyBind = new KeyBind("Bind", "", 1, new OneKeyBind(defaultKey));
      this.autoEnable = autoEnable;
      this.visible = visible;
      this.mod = new Mod(this.displayName, category);
   }

   public Module(Category category, String name, String desc, int defaultKey, boolean autoEnable) {
      this(category, name, desc, defaultKey, autoEnable, true);
   }

   public Module(Category category, String name, String desc, int defaultKey) {
      this(category, name, desc, defaultKey, false, true);
   }

   public Module(Category category, String name, String desc) {
      this(category, name, desc, -1, false, true);
   }

   public Module(Category category, String name) {
      this(category, name, "NONE", -1, false, true);
   }

   public Module(Category category, String name, String desc, boolean autoEnable) {
      this(category, name, desc, -1, autoEnable, true);
   }

   public Module(Category category, String name, boolean autoEnable) {
      this(category, name, "NONE", -1, autoEnable, true);
   }

   public Module(Category category, String name, int defaultKey, boolean autoEnable) {
      this(category, name, "NONE", defaultKey, autoEnable, true);
   }

   public Module(Category category, String name, int defaultKey) {
      this(category, name, "NONE", defaultKey, false, true);
   }

   @Override
   public void initialize() {
      super.initialize();
      this.registerKeyBind(this.keyBind.getValue(), () -> {
         if (mc.currentScreen == null) {
            this.toggle();
         }
      });
      this.keyBind.getDefaultValue().setRunnable(this.keyBind.getValue().getRunnable());
   }

   @Override
   public void setEnable(boolean enabled) {
      if (enabled != this.enabled.getValue()) {
         float v = ClientSettings.volumeValue.getValue().floatValue();
         if (enabled) {
            Notification.addNotification("Toggled " + this.displayName + " on.", "Module", Notification.NotificationType.ENABLED);
            if (!ClientUtils.nullCheck()) {
               String var3 = ClientSettings.style.getMode();
               switch (var3) {
                  case "Sigma":
                     mc.world.playSound(mc.player, mc.player.getBlockPos(), Sounds.SIGMA_ACTIVATE_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
                     break;
                  case "Piano":
                     mc.world
                        .playSound(mc.player, mc.player.getBlockPos(), Sounds.SOUTHSIDE_TOGGLE2_ON_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
                     break;
                  case "Default":
                     mc.world
                        .playSound(mc.player, mc.player.getBlockPos(), Sounds.SOUTHSIDE_TOGGLE_ON_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
               }
            }

            this.enabled.setValue(true);
            this.onEnable();
            Southside.INSTANCE.getEventBus().register(this);
         } else {
            Notification.addNotification("Toggled " + this.displayName + " off.", "Module", Notification.NotificationType.DISABLED);
            if (!ClientUtils.nullCheck()) {
               String var5 = ClientSettings.style.getMode();
               switch (var5) {
                  case "Sigma":
                     mc.world
                        .playSound(mc.player, mc.player.getBlockPos(), Sounds.SIGMA_DEACTIVATE_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
                     break;
                  case "Piano":
                     mc.world
                        .playSound(mc.player, mc.player.getBlockPos(), Sounds.SOUTHSIDE_TOGGLE2_OFF_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
                     break;
                  case "Default":
                     mc.world
                        .playSound(mc.player, mc.player.getBlockPos(), Sounds.SOUTHSIDE_TOGGLE_OFF_SOUNDEVENT, SoundCategory.BLOCKS, v, 1.0F);
               }
            }

            this.enabled.setValue(false);
            Southside.INSTANCE.getEventBus().unregister(this);
            this.onDisable();
         }
      }
   }

   public void toggle() {
      this.setEnable(!this.enabled.getValue());
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public List<Setting<?>> getSettings() {
      return ClassUtils.getValues(this.getClass(), this);
   }

   public String getReachableName() {
      return this.displayName.toLowerCase().replaceAll(" ", "-");
   }

   public int compareTo(@NotNull Module module) {
      return this.displayName.compareTo(module.displayName);
   }

   public void addRangedValueRestrict(Slider min, Slider max) {
      this.addListener(min.getLabel(), () -> {
         if (min.getValue().floatValue() > max.getValue().floatValue()) {
            min.setValue(max.getValue());
         }
      });
      this.addListener(max.getLabel(), () -> {
         if (max.getValue().floatValue() < min.getValue().floatValue()) {
            max.setValue(min.getValue());
         }
      });
   }

   @Override
   public JsonObject toTag() {
      return this.save();
   }

   public Module fromTag(JsonObject tag) {
      boolean has = tag.has(this.mod.name);
      if (has) {
         this.load(tag);
      }

      this.generateOptionList(this, this.mod.defaultPage, this.mod, !has);
      return this;
   }

   public void loadConfigFromJson(JsonObject config) {
      boolean has = config.has(this.mod.name);
      if (has) {
         this.load(config);
      }
   }

   public void initPostRunnable() {
      if (this.initPostRunnable != null) {
         this.initPostRunnable.run();
      }
   }

   public int rotationPriority() {
      return this.extraRotationPriority();
   }

   public int extraRotationPriority() {
      return 0;
   }

   @Generated
   public Runnable getInitPostRunnable() {
      return this.initPostRunnable;
   }

   @Generated
   public boolean isNotifyOnToggle() {
      return this.notifyOnToggle;
   }

   @Generated
   public Random getRandom() {
      return this.random;
   }

   @Generated
   public KeyBind getKeyBind() {
      return this.keyBind;
   }

   @Generated
   public String getSuffix() {
      return this.suffix;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   public boolean isVisible() {
      return this.visible;
   }

   @Generated
   public Category getCategory() {
      return this.category;
   }

   @Generated
   public boolean isAutoEnable() {
      return this.autoEnable;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDesc() {
      return this.desc;
   }

   @Generated
   public void setSuffix(String suffix) {
      this.suffix = suffix;
   }

   @Generated
   public void setVisible(boolean visible) {
      this.visible = visible;
   }
}
