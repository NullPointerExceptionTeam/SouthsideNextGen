package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.disabler.aca.Aimstep;
import dev.southside.module.impl.movement.GrimFly;
import dev.southside.module.impl.player.NoFall;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.network.PacketUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.class_10182;
import net.minecraft.class_1713;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2708;
import net.minecraft.class_2735;
import net.minecraft.class_2813;
import net.minecraft.class_2815;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import net.minecraft.class_2885;
import net.minecraft.class_418;
import net.minecraft.class_434;
import net.minecraft.class_8875;
import net.minecraft.class_2848.class_2849;

public class Disabler extends Module {
   public static boolean lastSprinting = false;
   private boolean sprint = false;
   private int nowSlot;
   private int lastSlot;
   private final Switch badA = new Switch("BadPackets A", true);
   private final Switch badB = new Switch("BadPackets B", true);
   private final Switch badD = new Switch("BadPackets D", true);
   private final Switch badF = new Switch("BadPackets F", true);
   private final Switch badP = new Switch("BadPackets P", true);
   private final Switch badW = new Switch("BadPackets W", true);
   private final Switch badX = new Switch("BadPackets X", true);
   private final Switch badY = new Switch("BadPackets Y", true);
   private final Switch inv = new Switch("Inventory", false);
   private final Switch aimDuplicateLook = new Switch("AimDuplicateLook", true);
   private final Switch fastSwitch = new Switch("FastSwitch", false);
   private final Switch aimStep = new Switch("AimStep", true);
   private final Switch equalRotation = new Switch("equalRotation", true);
   public static final Switch grimPost = new Switch("GrimPost", true);
   public final Switch showLogMessage = new Switch("ShowLogging", true);
   public final Switch loggingLag = new Switch("LoggingLag", false);
   private final ConcurrentLinkedQueue<class_2596<?>> packetTick = new ConcurrentLinkedQueue<>();
   private long time = 0L;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;
   private double lastPlacedDotYaw;
   private float lastPlacedDeltaYaw;
   private float playerYaw;
   private float deltaYaw;
   private boolean rotated = false;
   private boolean lastOnGround = false;
   public static float prevYaw;
   private static boolean lastPostState = false;
   public static List<class_2596> storedPackets = new CopyOnWriteArrayList<>();
   public static boolean interacting = false;
   private boolean lagDisable = false;
   public Queue<class_2849> badpacketXQueue = new LinkedBlockingQueue<>();

   public Disabler() {
      super(Category.Misc, "Disabler", "ABCDEFG");
   }

   @Override
   public void onDisable() {
      this.badpacketXQueue.clear();
   }

   @EventHandler
   private void onWorld(WorldEvent event) {
      lastSprinting = false;
      this.lastSlot = -1;
   }

   @EventHandler
   private void onMotion(MotionEvent event) {
   }

   @EventHandler
   private void onPacket(PacketEvent event) {
      if (mc.field_1724 != null) {
         class_2596<?> packet = event.getPacket();
         if (event.getOrigin() != TransferOrigin.SEND) {
            if (event.getPacket() instanceof class_2708 posLookPacket) {
               this.lagDisable = true;
               if (this.loggingLag.getValue()) {
                  class_10182 pos = posLookPacket.comp_3228();
                  class_243 p = pos.comp_3148();
                  class_243 d = pos.comp_3149();
                  ChatUtils.infof(
                     "§fPos §7%.1f %.1f %.1f §7| §fVel §7%.2f %.2f %.2f §7| §fYaw §7%.1f §fPitch §7%.1f",
                     p.field_1352,
                     p.field_1351,
                     p.field_1350,
                     d.field_1352,
                     d.field_1351,
                     d.field_1350,
                     pos.comp_3150(),
                     pos.comp_3151()
                  );
               }
            }
         } else {
            if (packet instanceof class_2828 wrapper && !interacting) {
               if (!wrapper.method_36172()) {
                  return;
               }

               if (this.badD.getValue() && (wrapper.field_12885 > 90.0F || wrapper.field_12885 < -90.0F)) {
                  if (this.showLogMessage.getValue()) {
                     this.info("§7BadPacketD detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }

               if (this.aimStep.getValue()) {
                  double yawDelta = Aimstep.yawDelta(prevYaw, wrapper.field_12887);
                  double pitchDelta = Aimstep.pitchDelta(this.lastPitch, wrapper.field_12885);
                  if (Aimstep.isAimStep(yawDelta, pitchDelta)) {
                     wrapper.field_12887 += 1.0E-4F;
                     if (this.showLogMessage.getValue()) {
                        this.info("§7AimStep detected!", new Object[0]);
                     }
                  }

                  if (Aimstep.isAimStep(pitchDelta, yawDelta)) {
                     if (wrapper.field_12885 == 90.0F) {
                        wrapper.field_12885 -= 1.0E-4F;
                     } else {
                        wrapper.field_12885 += 1.0E-4F;
                     }
                  }
               }

               if (this.equalRotation.getValue() && this.lastYaw - wrapper.field_12887 == 0.0F && this.lastPitch - wrapper.field_12885 == 0.0F) {
                  wrapper.field_12887 += 0.001F;
                  if (wrapper.field_12885 == 90.0F) {
                     wrapper.field_12885 -= 1.0E-4F;
                  } else {
                     wrapper.field_12885 += 1.0E-4F;
                  }

                  if (this.showLogMessage.getValue()) {
                     this.info("§7equalRotation detected!", new Object[0]);
                  }
               }

               if (this.aimDuplicateLook.getValue()
                  && wrapper.field_12887 == this.lastYaw
                  && wrapper.field_12885 == this.lastPitch
                  && wrapper.field_12887 != 0.0F) {
                  wrapper.field_12887 = wrapper.field_12887 + RandomUtils.generateRandomFloat(1.0E-4F, 0.001F);
               }

               if (this.lagDisable) {
                  this.lagDisable = false;
               }

               if (wrapper.field_12888) {
                  this.lastPitch = wrapper.field_12885;
                  prevYaw = wrapper.field_12887;
               }

               this.lastYaw = wrapper.field_12887;
               this.lastOnGround = wrapper.field_29179;
            } else if (event.getPacket() instanceof class_2885 && this.rotated) {
               this.lastPlacedDeltaYaw = this.deltaYaw;
               this.rotated = false;
            }

            if (packet instanceof class_2813 wrapper && this.badP.getValue()) {
               class_1713 clickType = wrapper.method_12195();
               int button = wrapper.method_12193();

               boolean flag = switch (clickType) {
                  case field_7790, field_7794, field_7796 -> {
                     if (button <= 2 && button >= 0) {
                        yield false;
                     }

                     yield true;
                  }
                  case field_7791 -> (button > 8 || button < 0) && button != 40;
                  case field_7795 -> {
                     if (button != 0 && button != 1) {
                        yield true;
                     }

                     yield false;
                  }
                  case field_7789 -> {
                     if (button != 3 && button != 7 && button <= 10 && button >= 0) {
                        yield false;
                     }

                     yield true;
                  }
                  case field_7793 -> button != 0;
                  default -> false;
               };
               if (flag) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketP detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }
            }

            if (packet instanceof class_2824 wrapper) {
               int id = wrapper.field_12870;
               if (id == mc.field_1724.method_5628() && this.badB.getValue()) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketB detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }

               if (this.badW.getValue() && mc.field_1687 != null && mc.field_1687.method_8469(id) == null) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketsW detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }
            }

            if (packet instanceof class_8875 wrapper && this.badY.getValue() && (wrapper.comp_1998() < 0 || wrapper.comp_1998() > 8)) {
               if (this.showLogMessage.getValue()) {
                  this.warning("§7BadPacketsY detected!", new Object[0]);
               }

               event.setCancelled(true);
            }

            if (packet instanceof class_2868 wrapper && (this.badA.getValue() || this.fastSwitch.getValue())) {
               if (this.lastSlot == wrapper.method_12442() && !this.fastSwitch.getValue()) {
                  event.setCancelled(true);
               }

               this.lastSlot = wrapper.method_12442();
            }

            if (packet instanceof class_2735 var13) {
               class_2735 var30 = var13;

               int var31;
               try {
                  var31 = var30.comp_3325();
               } catch (Throwable var8) {
                  throw new MatchException(var8.toString(), var8);
               }

               int var26 = var31;
               if (this.badA.getValue() || this.fastSwitch.getValue()) {
                  this.lastSlot = var26;
               }
            }

            if (packet instanceof class_2868 wrapper && this.fastSwitch.getValue()) {
               this.lastSlot = this.nowSlot;
               this.nowSlot = wrapper.method_12442();
               if ((this.lastSlot != 0 || this.nowSlot != 8) && (this.lastSlot != 8 || this.nowSlot != 0)) {
                  int i = this.lastSlot;
                  if (this.lastSlot < this.nowSlot) {
                     event.cancel();

                     while (i != this.nowSlot - 1) {
                        PacketUtils.sendPacketNoEvent(new class_2868(++i));
                     }

                     PacketUtils.sendPacketNoEvent(packet);
                  }

                  if (this.lastSlot > this.nowSlot) {
                     event.cancel();

                     while (i != this.nowSlot + 1) {
                        PacketUtils.sendPacketNoEvent(new class_2868(--i));
                     }

                     PacketUtils.sendPacketNoEvent(packet);
                  }
               }
            }

            if (this.inv.getValue()
               && (
                  packet instanceof class_2813 slot && slot.method_12194() == mc.field_1724.field_7498.field_7763
                     || packet instanceof class_2815 close && close.method_36168() == mc.field_1724.field_7498.field_7763
               )) {
               event.setCancelled(true);
               if (mc.field_1724.field_3919) {
                  PacketUtils.sendPacket(new class_2848(mc.field_1724, class_2849.field_12985));
               }

               PacketUtils.sendPacketNoEvent(packet);
               if (mc.field_1724.field_3919 && mc.field_1724.method_5624()) {
                  PacketUtils.sendPacket(new class_2848(mc.field_1724, class_2849.field_12981));
               }
            }

            if (packet instanceof class_2848 wrapperx) {
               if (wrapperx.method_12365() == class_2849.field_12981) {
                  if (this.badF.getValue()) {
                     if (lastSprinting) {
                        event.setCancelled(true);
                        if (this.showLogMessage.getValue()) {
                           this.warning("§7BadPacketF(BadSprintingPacket) detected!", new Object[0]);
                        }

                        return;
                     }

                     lastSprinting = true;
                  }

                  if (this.badX.getValue()) {
                     if (this.sprint) {
                        if (this.showLogMessage.getValue()) {
                           this.warning("§7BadPacketX detected!", new Object[0]);
                        }

                        this.processBadpacketX(class_2849.field_12981);
                        event.setCancelled(true);
                     }

                     this.sprint = true;
                  }
               } else if (wrapperx.method_12365() == class_2849.field_12985) {
                  if (this.badF.getValue()) {
                     if (!lastSprinting) {
                        if (this.showLogMessage.getValue()) {
                           this.warning("§7BadPacketF(BadStopSprintingPacket) detected!", new Object[0]);
                        }

                        event.setCancelled(true);
                        return;
                     }

                     lastSprinting = false;
                  }

                  if (this.badX.getValue()) {
                     if (this.sprint) {
                        if (this.showLogMessage.getValue()) {
                           this.warning("§7BadPacketX detected!", new Object[0]);
                        }

                        this.processBadpacketX(class_2849.field_12985);
                        event.setCancelled(true);
                     }

                     this.sprint = true;
                  }
               }
            }

            if (this.inv.getValue()
               && (
                  packet instanceof class_2813 slot && slot.method_12194() == mc.field_1724.field_7498.field_7763
                     || packet instanceof class_2815 close && close.method_36168() == mc.field_1724.field_7498.field_7763
               )) {
               event.setCancelled(true);
               if (mc.field_1724.field_3919) {
                  PacketUtils.sendPacket(new class_2848(mc.field_1724, class_2849.field_12985));
               }

               PacketUtils.sendPacketNoEvent(packet);
               if (mc.field_1724.field_3919 && mc.field_1724.method_5624()) {
                  PacketUtils.sendPacket(new class_2848(mc.field_1724, class_2849.field_12981));
               }
            }
         }
      }
   }

   public static float normalizeYawDiff(float currentYaw, float prevYaw) {
      float diff;
      for (diff = currentYaw - prevYaw; diff > 180.0F; diff = currentYaw - prevYaw) {
         currentYaw -= 360.0F;
      }

      while (diff < -180.0F) {
         currentYaw += 360.0F;
         diff = currentYaw - prevYaw;
      }

      return currentYaw;
   }

   public static boolean postDelayPackets(class_2596 packet) {
      return mc.field_1724 == null ? false : !(mc.field_1755 instanceof class_434) && !(mc.field_1755 instanceof class_418);
   }

   public static boolean postState() {
      NoFall noFall = Southside.get().getModuleManager().get(NoFall.class);
      GrimFly grimFly = Southside.get().getModuleManager().get(GrimFly.class);
      boolean currentState = Southside.INSTANCE.getModuleManager().get(Disabler.class).isEnabled()
         && grimPost.getValue()
         && (!grimFly.isEnabled() || grimFly.ticks % 2 != 0 || grimFly.ticks < 20 && grimFly.ticks % 4 != 0)
         && (!noFall.isEnabled() || !noFall.jump);
      if (lastPostState && !currentState) {
         lastPostState = false;
         mc.execute(Disabler::processPackets);
      }

      lastPostState = currentState;
      return currentState;
   }

   public static void processPackets() {
      if (!storedPackets.isEmpty()) {
         for (class_2596 packet : storedPackets) {
            if (mc.method_1562() != null && !ServerPacketManager.onPacket(packet)) {
               PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.method_1562().method_48296());
               Southside.INSTANCE.getEventBus().call(event);
               if (!event.isCancelled()) {
                  try {
                     PacketUtils.handlePacket(packet);
                  } catch (IllegalStateException var4) {
                     Southside.LOGGER.warn("Failed to handle packet (likely team-related): {}", var4.getMessage());
                  }
               }
            }
         }

         storedPackets.clear();
      }
   }

   public void processBadpacketX(class_2849 mode) {
      this.badpacketXQueue.add(mode);
   }
}
