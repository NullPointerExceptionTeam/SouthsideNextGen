package dev.southside.nel.open_nel;

import com.google.gson.annotations.SerializedName;
import lombok.Generated;

public class NeteaseAccount {
   @SerializedName("entityId")
   public String entityId;
   @SerializedName("channel")
   public String channel;
   @SerializedName("status")
   public String status;

   @Generated
   public NeteaseAccount(String entityId, String channel, String status) {
      this.entityId = entityId;
      this.channel = channel;
      this.status = status;
   }
}
