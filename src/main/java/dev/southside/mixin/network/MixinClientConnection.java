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
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.OffThreadException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.entity.data.DataTracker.SerializedEntry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class MixinClientConnection {
   @Shadow
   @Nullable
   private volatile PacketListener packetListener;
   @Shadow
   @Nullable
   private volatile PacketListener prePlayStateListener;
   @Shadow
   @Final
   private static Logger LOGGER;
   @Shadow
   private Channel channel;
   @Shadow
   private int packetsReceivedCounter;

   @Shadow
   private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
   }

   @Shadow
   public void disconnect(Text disconnectReason) {
   }

   @Shadow
   public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
   }

   @Overwrite
   public void send(Packet<?> packet) {
      PacketEvent event = new PacketEvent(TransferOrigin.SEND, packet, (ClientConnection)(Object)this);
      PacketUtils.callPacket(event.getPacket());
      if (!Southside.INSTANCE.getEventBus().call(event).isCancelled()) {
         if (BlinkUtils.onPacket(event.getPacket())) {
            this.send(event.getPacket(), null);
            Southside.INSTANCE.getEventBus().call(new PacketSendPostEvent(event.getPacket()));
         }
      }
   }

   @Overwrite
   public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
      if (this.channel.isOpen()) {
         PacketListener packetListener = this.packetListener;
         if (packetListener == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
         }

         if (packet instanceof BundleS2CPacket bundleS2CPacket) {
            for (Packet packet1 : bundleS2CPacket.getPackets()) {
               packet1 = HeypixelViaversionItemTransfer.transfer(packet1);
               if (packet1 instanceof EntityTrackerUpdateS2CPacket var30) {
                  EntityTrackerUpdateS2CPacket var40 = var30;

                  int var41;

                  try {
                     var41 = var40.id();
                  } catch (Throwable var15) {
                     throw new MatchException(var15.toString(), var15);
                  }

                  int var34 = var41;
                  var40 = var30;

                  List<SerializedEntry<?>> var43;

                  try {
                     var43 = var40.trackedValues();
                  } catch (Throwable var14) {
                     throw new MatchException(var14.toString(), var14);
                  }

                  List<SerializedEntry<?>> var35 = var43;
                  if (MinecraftAccessor.mc.player != null && var34 == MinecraftAccessor.mc.player.getId()) {
                     for (SerializedEntry<?> entry : var35) {
                        if (entry.id() == 0 && entry.value() instanceof Byte flags) {
                           PlayerUtils.serverSprinting = (flags & 8) != 0;
                        }
                     }
                  }
               }

               if (!PingSpoofManager.onPacket(packet1)) {
                  if (Disabler.postState() && Disabler.postDelayPackets(packet1)) {
                     Packet<?> delayedPacket = packet1;
                     MinecraftClient.getInstance().execute(() -> Disabler.storedPackets.add(delayedPacket));
                  } else {
                     if (packet1 instanceof GameMessageS2CPacket messageS2CPacket && messageS2CPacket.content().getString().contains("Veta")) {
                        Southside.get().setVeta(true);
                     }

                     PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet1, (ClientConnection)(Object)this);
                     if (!Southside.INSTANCE.getEventBus().call(event).isCancelled() && !ServerPacketManager.onPacket(packet1)) {
                        MovementUtils.onPacketRec(packet1);

                        try {
                           handlePacket(packet1, packetListener);
                        } catch (OffThreadException var16) {
                        }
                     }
                  }
               }
            }

            return;
         }

         packet = HeypixelViaversionItemTransfer.transfer(packet);
         if (packet instanceof EntityTrackerUpdateS2CPacket event) {
            EntityTrackerUpdateS2CPacket var10000 = event;

            int var37;

            try {
               var37 = var10000.id();
            } catch (Throwable var21) {
               throw new MatchException(var21.toString(), var21);
            }

            int eventx = var37;
            var10000 = event;

            List<SerializedEntry<?>> var39;

            try {
               var39 = var10000.trackedValues();
            } catch (Throwable var20) {
               throw new MatchException(var20.toString(), var20);
            }

            List<SerializedEntry<?>> var28 = var39;
            if (MinecraftAccessor.mc.player != null && eventx == MinecraftAccessor.mc.player.getId()) {
               for (SerializedEntry<?> entryx : var28) {
                  if (entryx.id() == 0 && entryx.value() instanceof Byte flags) {
                     PlayerUtils.serverSprinting = (flags & 8) != 0;
                  }
               }
            }
         }

         if (PingSpoofManager.onPacket(packet)) {
            return;
         }

         if (Disabler.postState() && Disabler.postDelayPackets(packet)) {
            Packet<?> delayedPacket = packet;
            MinecraftClient.getInstance().execute(() -> Disabler.storedPackets.add(delayedPacket));
            return;
         }

         if (packet instanceof GameMessageS2CPacket messageS2CPacket && messageS2CPacket.content().getString().contains("Veta")) {
            Southside.get().setVeta(true);
         }

         PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet, (ClientConnection)(Object)this);
         if (Southside.INSTANCE.getEventBus().call(event).isCancelled()) {
            return;
         }

         if (ServerPacketManager.onPacket(packet)) {
            return;
         }

         MovementUtils.onPacketRec(packet);
         if (packetListener.accepts(packet)) {
            try {
               handlePacket(packet, packetListener);
            } catch (OffThreadException var17) {
            } catch (RejectedExecutionException var18) {
               this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
            } catch (ClassCastException var19) {
               LOGGER.error("Received {} that couldn't be processed", packet.getClass(), var19);
               this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
            }

            this.packetsReceivedCounter++;
         }
      }
   }

   @Inject(
      method = {"sendInternal(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHighLevelSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
      if (Southside.INSTANCE
         .getEventBus()
         .call(new HighLevelPacketEvent((ClientConnection)(Object)this, this.packetListener != null ? this.packetListener : this.prePlayStateListener, packet, TransferOrigin.SEND))
         .isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHighLevelReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
      if (Southside.INSTANCE
         .getEventBus()
         .call(new HighLevelPacketEvent((ClientConnection)(Object)this, this.packetListener != null ? this.packetListener : this.prePlayStateListener, packet, TransferOrigin.RECEIVE))
         .isCancelled()) {
         ci.cancel();
      }
   }

   private byte[] readByteArray(PacketByteBuf buf, int maxSize) {
      int i = buf.readVarInt() - 1;
      if (i > maxSize) {
         throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxSize);
      } else {
         byte[] abyte = new byte[i];
         buf.readBytes(abyte);
         return abyte;
      }
   }
}
