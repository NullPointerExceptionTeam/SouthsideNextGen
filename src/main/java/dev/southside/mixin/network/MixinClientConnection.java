package dev.southside.mixin.network;

import dev.southside.MinecraftAccessor;
import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.HighLevelPacketEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.PacketSendPostEvent;
import dev.southside.manager.PingSpoofManager;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.utils.heypixel.HeypixelViaversionItemTransfer;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.player.PlayerUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.class_2535;
import net.minecraft.class_2540;
import net.minecraft.class_2547;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2739;
import net.minecraft.class_2987;
import net.minecraft.class_310;
import net.minecraft.class_7439;
import net.minecraft.class_7648;
import net.minecraft.class_8042;
import net.minecraft.class_2945.class_7834;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_2535.class})
public class MixinClientConnection {
   @Shadow
   @Nullable
   private volatile class_2547 field_11652;
   @Shadow
   @Nullable
   private volatile class_2547 field_45669;
   @Shadow
   @Final
   private static Logger field_11642;
   @Shadow
   private Channel field_11651;
   @Shadow
   private int field_11658;

   @Shadow
   private static <T extends class_2547> void method_10759(class_2596<T> packet, class_2547 listener) {
   }

   @Shadow
   public void method_10747(class_2561 disconnectReason) {
   }

   @Shadow
   public void method_10752(class_2596<?> packet, @Nullable class_7648 callbacks) {
   }

   @Overwrite
   public void method_10743(class_2596<?> packet) {
      PacketEvent event = new PacketEvent(TransferOrigin.SEND, packet, (class_2535)(Object)this);
      PacketUtils.callPacket(event.getPacket());
      if (!Southside.INSTANCE.getEventBus().call(event).isCancelled()) {
         if (BlinkUtils.onPacket(event.getPacket())) {
            this.method_10752(event.getPacket(), null);
            Southside.INSTANCE.getEventBus().call(new PacketSendPostEvent(event.getPacket()));
         }
      }
   }

   @Overwrite
   public void method_10770(ChannelHandlerContext channelHandlerContext, class_2596<?> packet) {
      if (this.field_11651.isOpen()) {
         class_2547 packetListener = this.field_11652;
         if (packetListener == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
         }

         if (packet instanceof class_8042 bundleS2CPacket) {
            for (class_2596 packet1 : bundleS2CPacket.method_48324()) {
               packet1 = HeypixelViaversionItemTransfer.transfer(packet1);
               if (packet1 instanceof class_2739 var30) {
                  class_2739 var40 = var30;

                  int var41;

                  try {
                     var41 = var40.comp_1127();
                  } catch (Throwable var15) {
                     throw new MatchException(var15.toString(), var15);
                  }

                  int var34 = var41;
                  var40 = var30;

                  List<class_7834<?>> var43;

                  try {
                     var43 = var40.comp_1128();
                  } catch (Throwable var14) {
                     throw new MatchException(var14.toString(), var14);
                  }

                  List<class_7834<?>> var35 = var43;
                  if (MinecraftAccessor.mc.field_1724 != null && var34 == MinecraftAccessor.mc.field_1724.method_5628()) {
                     for (class_7834<?> entry : var35) {
                        if (entry.comp_1115() == 0 && entry.comp_1117() instanceof Byte flags) {
                           PlayerUtils.serverSprinting = (flags & 8) != 0;
                        }
                     }
                  }
               }

               if (!PingSpoofManager.onPacket(packet1)) {
                  if (Disabler.postState() && Disabler.postDelayPackets(packet1)) {
                     class_2596<?> delayedPacket = packet1;
                     class_310.method_1551().execute(() -> Disabler.storedPackets.add(delayedPacket));
                  } else {
                     if (packet1 instanceof class_7439 messageS2CPacket && messageS2CPacket.comp_763().getString().contains("Veta")) {
                        Southside.get().setVeta(true);
                     }

                     PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet1, (class_2535)(Object)this);
                     if (!Southside.INSTANCE.getEventBus().call(event).isCancelled() && !ServerPacketManager.onPacket(packet1)) {
                        MovementUtils.onPacketRec(packet1);

                        try {
                           method_10759(packet1, packetListener);
                        } catch (class_2987 var16) {
                        }
                     }
                  }
               }
            }

            return;
         }

         packet = HeypixelViaversionItemTransfer.transfer(packet);
         if (packet instanceof class_2739 event) {
            class_2739 var10000 = event;

            int var37;

            try {
               var37 = var10000.comp_1127();
            } catch (Throwable var21) {
               throw new MatchException(var21.toString(), var21);
            }

            int eventx = var37;
            var10000 = event;

            List<class_7834<?>> var39;

            try {
               var39 = var10000.comp_1128();
            } catch (Throwable var20) {
               throw new MatchException(var20.toString(), var20);
            }

            List<class_7834<?>> var28 = var39;
            if (MinecraftAccessor.mc.field_1724 != null && eventx == MinecraftAccessor.mc.field_1724.method_5628()) {
               for (class_7834<?> entryx : var28) {
                  if (entryx.comp_1115() == 0 && entryx.comp_1117() instanceof Byte flags) {
                     PlayerUtils.serverSprinting = (flags & 8) != 0;
                  }
               }
            }
         }

         if (PingSpoofManager.onPacket(packet)) {
            return;
         }

         if (Disabler.postState() && Disabler.postDelayPackets(packet)) {
            class_2596<?> delayedPacket = packet;
            class_310.method_1551().execute(() -> Disabler.storedPackets.add(delayedPacket));
            return;
         }

         if (packet instanceof class_7439 messageS2CPacket && messageS2CPacket.comp_763().getString().contains("Veta")) {
            Southside.get().setVeta(true);
         }

         PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet, (class_2535)(Object)this);
         if (Southside.INSTANCE.getEventBus().call(event).isCancelled()) {
            return;
         }

         if (ServerPacketManager.onPacket(packet)) {
            return;
         }

         MovementUtils.onPacketRec(packet);
         if (packetListener.method_52413(packet)) {
            try {
               method_10759(packet, packetListener);
            } catch (class_2987 var17) {
            } catch (RejectedExecutionException var18) {
               this.method_10747(class_2561.method_43471("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException var19) {
               field_11642.error("Received {} that couldn't be processed", packet.getClass(), var19);
               this.method_10747(class_2561.method_43471("multiplayer.disconnect.invalid_packet"));
            }

            this.field_11658++;
         }
      }
   }

   @Inject(
      method = {"method_36942(Lnet/minecraft/class_2596;Lnet/minecraft/class_7648;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHighLevelSend(class_2596<?> packet, class_7648 callbacks, boolean flush, CallbackInfo ci) {
      if (Southside.INSTANCE
         .getEventBus()
         .call(new HighLevelPacketEvent((class_2535)(Object)this, this.field_11652 != null ? this.field_11652 : this.field_45669, packet, TransferOrigin.SEND))
         .isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"method_10770(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/class_2596;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHighLevelReceive(ChannelHandlerContext channelHandlerContext, class_2596<?> packet, CallbackInfo ci) {
      if (Southside.INSTANCE
         .getEventBus()
         .call(new HighLevelPacketEvent((class_2535)(Object)this, this.field_11652 != null ? this.field_11652 : this.field_45669, packet, TransferOrigin.RECEIVE))
         .isCancelled()) {
         ci.cancel();
      }
   }

   private byte[] readByteArray(class_2540 buf, int maxSize) {
      int i = buf.method_10816() - 1;
      if (i > maxSize) {
         throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxSize);
      } else {
         byte[] abyte = new byte[i];
         buf.method_52979(abyte);
         return abyte;
      }
   }
}
