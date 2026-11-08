package dev.southside.module.setting;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import lombok.Generated;

public abstract class Setting<T> {
   protected ArrayList<BooleanSupplier> hideSuppliers;
   private String name;
   private T value;

   protected Setting(String name) {
      this.name = name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void addSupplier(BooleanSupplier hide) {
      this.hideSuppliers.add(hide);
   }

   public void setValue(T value) {
      this.value = value;
   }

   public boolean isHidden() {
      for (BooleanSupplier supplier : this.hideSuppliers) {
         if (!supplier.getAsBoolean()) {
            return false;
         }
      }

      return true;
   }

   public void changeValue(T value) {
      this.value = value;
   }

   public abstract JsonElement toJson();

   public abstract void fromJson(JsonElement var1);

   @Generated
   public ArrayList<BooleanSupplier> getHideSuppliers() {
      return this.hideSuppliers;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public T getValue() {
      return this.value;
   }
}
