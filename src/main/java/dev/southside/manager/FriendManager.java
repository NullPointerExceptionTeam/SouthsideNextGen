package dev.southside.manager;

import dev.southside.module.impl.client.Notification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendManager {
   private final ArrayList<String> friends = new ArrayList<>();

   public void add(String name) {
      if (!this.friends.contains(name)) {
         this.friends.add(name);
         Notification.addNotification("Added friend: " + name, "Friend Manager", Notification.NotificationType.ENABLED);
      } else {
         Notification.addNotification(name + " is already your friend!", "Friend Manager", Notification.NotificationType.WARN);
      }
   }

   public void remove(String name) {
      if (this.friends.contains(name)) {
         this.friends.remove(name);
         Notification.addNotification("Removed friend: " + name, "Friend Manager", Notification.NotificationType.ENABLED);
      } else {
         Notification.addNotification("Friend not found: " + name, "Friend Manager", Notification.NotificationType.ENABLED);
      }
   }

   public boolean isFriend(String name) {
      return this.friends.contains(name);
   }

   public List<String> getFriends() {
      return Collections.unmodifiableList(this.friends);
   }
}
