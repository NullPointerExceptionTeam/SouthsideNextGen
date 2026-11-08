package cc.polyfrost.oneconfig.internal.config.profiles;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.core.ConfigCore;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiles {
   private static final Logger LOGGER = LogManager.getLogger("OneConfig Profiles");
   public static final File profileDir = new File("Southside/configs");
   private static ArrayList<String> profiles;

   public static String getCurrentProfile() {
      if (OneConfigConfig.getInstance() == null) {
         OneConfigConfig.getInstance();
      }

      if (!profileDir.exists() && !profileDir.mkdir()) {
         LOGGER.fatal("Could not create profiles folder");
         return null;
      } else {
         if (profiles == null) {
            String[] profilesArray = profileDir.list((file, s) -> file.isDirectory());
            if (profilesArray != null) {
               profiles = new ArrayList<>(Arrays.asList(profilesArray));
            }
         }

         if (!getProfileDir(OneConfigConfig.currentProfile.getValue()).exists()) {
            createProfile(OneConfigConfig.currentProfile.getValue());
         }

         return OneConfigConfig.currentProfile.getValue();
      }
   }

   public static void createProfile(String name) {
      File folder = new File(profileDir, name);
      if (!folder.exists() && !folder.mkdir()) {
         LOGGER.fatal("Could not create profile folder");
      } else {
         profiles.add(name);
      }
   }

   public static File getProfileDir() {
      return getProfileDir(getCurrentProfile());
   }

   public static File getProfileDir(String profile) {
      return new File(profileDir, profile);
   }

   public static File getProfileFile(String file) {
      return new File(getProfileDir(), file);
   }

   public static List<String> getProfiles() {
      return new ArrayList<>(profiles);
   }

   public static boolean doesProfileExist(String profile) {
      return profiles.contains(profile);
   }

   public static void loadProfile(String profile) {
      ConfigCore.saveAll();
      OneConfigConfig.currentProfile.setValue(profile);
      OneConfigConfig.getInstance().save();
      ConfigCore.reInitAll();
   }

   public static void renameProfile(String name, String newName) {
      try {
         File newFile = new File(profileDir, newName);
         FileUtils.moveDirectory(getProfileDir(name), newFile);
         if (OneConfigConfig.currentProfile.getValue().equals(name)) {
            OneConfigConfig.currentProfile.setValue(newName);
         }

         profiles.remove(name);
         profiles.add(newName);
      } catch (IOException var3) {
         LOGGER.error("Failed to rename profile");
      }
   }

   public static void deleteProfile(String name) {
      if (name.equals(getCurrentProfile())) {
         if (profiles.size() == 1) {
            LOGGER.error("Cannot delete only profile!");
            return;
         }

         loadProfile(profiles.stream().filter(entry -> !entry.equals(name)).findFirst().get());
      }

      try {
         FileUtils.deleteDirectory(getProfileDir(name));
         profiles.remove(name);
      } catch (IOException var2) {
         LOGGER.error("Failed to delete profile");
      }
   }
}
