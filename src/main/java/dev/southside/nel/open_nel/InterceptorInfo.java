package dev.southside.nel.open_nel;

import java.util.Objects;

public record InterceptorInfo(String serverId, String roleId, String address, String uuid) {
   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         InterceptorInfo that = (InterceptorInfo)o;
         return Objects.equals(this.roleId, that.roleId) && Objects.equals(this.serverId, that.serverId);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.serverId, this.roleId);
   }
}
