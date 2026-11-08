package dev.southside.utils.json;

import com.google.gson.JsonObject;

public interface ISerializable<T> {
   JsonObject toTag();

   T fromTag(JsonObject var1);
}
