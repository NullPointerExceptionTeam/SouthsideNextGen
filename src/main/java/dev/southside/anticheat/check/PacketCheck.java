package dev.southside.anticheat.check;

import dev.southside.event.events.PacketEvent;

public interface PacketCheck extends Check {
   void onPacket(PacketEvent var1);
}
