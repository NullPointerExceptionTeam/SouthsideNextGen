package dev.southside.nel.open_nel;

import com.google.gson.annotations.SerializedName;
import lombok.Generated;

public class GameCharacter {
   @SerializedName("id")
   public String id;
   @SerializedName("name")
   public String name;

   @Generated
   public GameCharacter(String id, String name) {
      this.id = id;
      this.name = name;
   }
}
