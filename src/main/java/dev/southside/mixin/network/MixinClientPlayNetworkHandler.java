package dev.southside.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.events.ChatEvent;
import dev.southside.event.events.CustomPayloadEvent;
import dev.southside.event.events.GameJoinedAddEntityEvent;
import dev.southside.event.events.GameJoinedEvent;
import dev.southside.event.events.GameLeftEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.mixininterfaces.IBoxUpdate;
import dev.southside.module.impl.misc.ChatBypass;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.rotation.RotationUtils;
import java.util.Set;
import net.minecraft.class_10182;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2678;
import net.minecraft.class_2708;
import net.minecraft.class_2709;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_745;
import net.minecraft.class_8588;
import net.minecraft.class_8710;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({class_634.class})
public class MixinClientPlayNetworkHandler {
   @Unique
   private float originalYaw;
   @Unique
   private float originalPitch;
   @Shadow
   private class_638 field_3699;
   @Unique
   private boolean worldNotNull;

   @ModifyVariable(
      method = {"method_45729(Ljava/lang/String;)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String handleSendMessage(String content) {
      return Southside.INSTANCE.getModuleManager().get(ChatBypass.class).isEnabled() ? ChatBypass.bypass(content) : content;
   }

   @Inject(
      method = {"method_11120(Lnet/minecraft/class_2678;)V"},
      at = {@At("HEAD")}
   )
   private void onGameJoinHead(class_2678 packet, CallbackInfo info) {
      this.worldNotNull = this.field_3699 != null;
   }

   @Inject(
      method = {"method_11120(Lnet/minecraft/class_2678;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_638;method_53875(Lnet/minecraft/class_1297;)V",
         shift = Shift.BEFORE
      )}
   )
   private void onGameJoinAddEntity(class_2678 packet, CallbackInfo info) {
      Southside.get().getEventBus().post(GameJoinedAddEntityEvent.INSTANCE);
   }

   @Inject(
      method = {"method_11120(Lnet/minecraft/class_2678;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_33689()V",
         shift = Shift.AFTER
      )}
   )
   private void onGameJoinTail(class_2678 packet, CallbackInfo info) {
      if (this.worldNotNull) {
         Southside.get().getEventBus().post(GameLeftEvent.INSTANCE);
      }

      Southside.get().getEventBus().post(GameJoinedEvent.INSTANCE);
   }

   @Inject(
      method = {"method_52798(Lnet/minecraft/class_8588;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_1255;)V",
         shift = Shift.AFTER
      )}
   )
   private void onEnterReconfiguration(class_8588 packet, CallbackInfo info) {
      Southside.get().getEventBus().post(GameLeftEvent.INSTANCE);
   }

   @WrapOperation(
      method = {"method_11152(Lnet/minecraft/class_8710;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_634;method_52801(Lnet/minecraft/class_8710;)V"
      )}
   )
   private void onCustomPayload(class_634 instance, class_8710 payload, Operation<Void> original) {
      CustomPayloadEvent event = new CustomPayloadEvent(payload);
      Southside.get().getEventBus().post(event);
      original.call(new Object[]{instance, payload});
   }

   @Inject(
      method = {"method_45729(Ljava/lang/String;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendChatMessage(String content, CallbackInfo info) {
      ChatEvent event = new ChatEvent(content);
      Southside.INSTANCE.getEventBus().call(event);
      String prefix = ".";
      if (content.startsWith(prefix)) {
         try {
            Southside.get().getCommandManager().dispatch(content.substring(prefix.length()));
         } catch (CommandSyntaxException var6) {
            ChatUtils.error(var6.getMessage());
         }

         MinecraftAccessor.mc.field_1705.method_1743().method_1803(content);
         info.cancel();
      }

      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(
      method = {"method_11157(Lnet/minecraft/class_2708;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_634;method_64897(Lnet/minecraft/class_10182;Ljava/util/Set;Lnet/minecraft/class_1297;Z)Z",
         shift = Shift.BEFORE
      )}
   )
   public void onSetPos(class_2708 packet, CallbackInfo ci) {
      if (class_310.method_1551().field_1724 != null) {
         this.originalYaw = class_310.method_1551().field_1724.method_36454();
         this.originalPitch = class_310.method_1551().field_1724.method_36455();
         class_310.method_1551()
            .field_1724
            .method_36456(Disabler.normalizeYawDiff(RotationUtils.getRotationSafely().yaw, RotationUtils.getServerRotation().yaw));
         class_310.method_1551().field_1724.method_36457(RotationUtils.getRotationSafely().getPitch());
      }
   }

   @Inject(
      method = {"method_11157(Lnet/minecraft/class_2708;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_634;method_64897(Lnet/minecraft/class_10182;Ljava/util/Set;Lnet/minecraft/class_1297;Z)Z",
         shift = Shift.AFTER
      )}
   )
   public void onResetPos(class_2708 packet, CallbackInfo ci) {
      if (class_310.method_1551().field_1724 != null) {
         class_310.method_1551().field_1724.method_36456(this.originalYaw);
         class_310.method_1551().field_1724.method_36457(this.originalPitch);
      }
   }

   @Inject(
      method = {"method_64897(Lnet/minecraft/class_10182;Ljava/util/Set;Lnet/minecraft/class_1297;Z)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void replaceSetPos(class_10182 pos, Set<class_2709> flags, class_1297 entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof class_745 otherClientPlayer) {
         IBoxUpdate player = (IBoxUpdate)otherClientPlayer;
         class_10182 playerPosition = class_10182.method_64621(entity);
         class_10182 playerPosition2 = class_10182.method_63639(playerPosition, pos, flags);
         boolean bl2 = playerPosition.comp_3148().method_1025(playerPosition2.comp_3148()) > 4096.0;
         if (bl && !bl2) {
            entity.method_5759(
               playerPosition2.comp_3148().method_10216(),
               playerPosition2.comp_3148().method_10214(),
               playerPosition2.comp_3148().method_10215(),
               playerPosition2.comp_3150(),
               playerPosition2.comp_3151(),
               3
            );
            entity.method_18799(playerPosition2.comp_3149());
            cir.setReturnValue(true);
         } else {
            if (ServerPacketManager.deSyncing) {
               synchronized (ServerPacketManager.packets) {
                  ServerPacketManager.packets.add((Runnable)() -> {
                     player.setBoxPos(playerPosition2.comp_3148());
                     player.setServerBoxPos(playerPosition2.comp_3148());
                     entity.method_33574(playerPosition2.comp_3148());
                  });
               }

               entity.method_23327(playerPosition2.comp_3148().field_1352, playerPosition2.comp_3148().field_1351, playerPosition2.comp_3148().field_1350);
            } else {
               player.setBoxPos(playerPosition2.comp_3148());
               player.setServerBoxPos(playerPosition2.comp_3148());
               entity.method_33574(playerPosition2.comp_3148());
            }

            entity.method_18799(playerPosition2.comp_3149());
            entity.method_36456(playerPosition2.comp_3150());
            entity.method_36457(playerPosition2.comp_3151());
            class_10182 playerPosition3 = new class_10182(entity.method_61411(), class_243.field_1353, entity.field_5982, entity.field_6004);
            class_10182 playerPosition4 = class_10182.method_63639(playerPosition3, pos, flags);
            entity.method_63615(playerPosition4.comp_3148(), playerPosition4.comp_3150(), playerPosition4.comp_3151());
            cir.setReturnValue(false);
         }
      }
   }
}
