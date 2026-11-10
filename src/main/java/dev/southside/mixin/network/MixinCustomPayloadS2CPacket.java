package dev.southside.mixin.network;

import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({CustomPayloadS2CPacket.class})
public class MixinCustomPayloadS2CPacket {
}
