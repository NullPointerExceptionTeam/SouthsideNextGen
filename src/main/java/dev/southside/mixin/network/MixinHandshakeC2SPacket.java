package dev.southside.mixin.network;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({HandshakeC2SPacket.class})
public abstract class MixinHandshakeC2SPacket {
}
