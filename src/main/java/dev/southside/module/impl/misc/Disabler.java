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
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

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
   private final ConcurrentLinkedQueue<Packet<?>> packetTick = new ConcurrentLinkedQueue<>();
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
   public static List<Packet> storedPackets = new CopyOnWriteArrayList<>();
   public static boolean interacting = false;
   private boolean lagDisable = false;
   public Queue<Mode> badpacketXQueue = new LinkedBlockingQueue<>();

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
      if (mc.player != null) {
         Packet<?> packet = event.getPacket();
         if (event.getOrigin() != TransferOrigin.SEND) {
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket posLookPacket) {
               this.lagDisable = true;
               if (this.loggingLag.getValue()) {
                  PlayerPosition pos = posLookPacket.change();
                  Vec3d p = pos.position();
                  Vec3d d = pos.deltaMovement();
                  ChatUtils.infof(
                     "§fPos §7%.1f %.1f %.1f §7| §fVel §7%.2f %.2f %.2f §7| §fYaw §7%.1f §fPitch §7%.1f",
                     p.x,
                     p.y,
                     p.z,
                     d.x,
                     d.y,
                     d.z,
                     pos.yaw(),
                     pos.pitch()
                  );
               }
            }
         } else {
            if (packet instanceof PlayerMoveC2SPacket wrapper && !interacting) {
               if (!wrapper.changesLook()) {
                  return;
               }

               if (this.badD.getValue() && (wrapper.pitch > 90.0F || wrapper.pitch < -90.0F)) {
                  if (this.showLogMessage.getValue()) {
                     this.info("§7BadPacketD detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }

               if (this.aimStep.getValue()) {
                  double yawDelta = Aimstep.yawDelta(prevYaw, wrapper.yaw);
                  double pitchDelta = Aimstep.pitchDelta(this.lastPitch, wrapper.pitch);
                  if (Aimstep.isAimStep(yawDelta, pitchDelta)) {
                     wrapper.yaw += 1.0E-4F;
                     if (this.showLogMessage.getValue()) {
                        this.info("§7AimStep detected!", new Object[0]);
                     }
                  }

                  if (Aimstep.isAimStep(pitchDelta, yawDelta)) {
                     if (wrapper.pitch == 90.0F) {
                        wrapper.pitch -= 1.0E-4F;
                     } else {
                        wrapper.pitch += 1.0E-4F;
                     }
                  }
               }

               if (this.equalRotation.getValue() && this.lastYaw - wrapper.yaw == 0.0F && this.lastPitch - wrapper.pitch == 0.0F) {
                  wrapper.yaw += 0.001F;
                  if (wrapper.pitch == 90.0F) {
                     wrapper.pitch -= 1.0E-4F;
                  } else {
                     wrapper.pitch += 1.0E-4F;
                  }

                  if (this.showLogMessage.getValue()) {
                     this.info("§7equalRotation detected!", new Object[0]);
                  }
               }

               if (this.aimDuplicateLook.getValue()
                  && wrapper.yaw == this.lastYaw
                  && wrapper.pitch == this.lastPitch
                  && wrapper.yaw != 0.0F) {
                  wrapper.yaw = wrapper.yaw + RandomUtils.generateRandomFloat(1.0E-4F, 0.001F);
               }

               if (this.lagDisable) {
                  this.lagDisable = false;
               }

               if (wrapper.changeLook) {
                  this.lastPitch = wrapper.pitch;
                  prevYaw = wrapper.yaw;
               }

               this.lastYaw = wrapper.yaw;
               this.lastOnGround = wrapper.onGround;
            } else if (event.getPacket() instanceof PlayerInteractBlockC2SPacket && this.rotated) {
               this.lastPlacedDeltaYaw = this.deltaYaw;
               this.rotated = false;
            }

            if (packet instanceof ClickSlotC2SPacket wrapper && this.badP.getValue()) {
               SlotActionType clickType = wrapper.getActionType();
               int button = wrapper.getButton();

               boolean flag = switch (clickType) {
                  case PICKUP, QUICK_MOVE, CLONE -> {
                     if (button <= 2 && button >= 0) {
                        yield false;
                     }

                     yield true;
                  }
                  case SWAP -> (button > 8 || button < 0) && button != 40;
                  case THROW -> {
                     if (button != 0 && button != 1) {
                        yield true;
                     }

                     yield false;
                  }
                  case QUICK_CRAFT -> {
                     if (button != 3 && button != 7 && button <= 10 && button >= 0) {
                        yield false;
                     }

                     yield true;
                  }
                  case PICKUP_ALL -> button != 0;
                  default -> false;
               };
               if (flag) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketP detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }
            }

            if (packet instanceof PlayerInteractEntityC2SPacket wrapper) {
               int id = wrapper.entityId;
               if (id == mc.player.getId() && this.badB.getValue()) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketB detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }

               if (this.badW.getValue() && mc.world != null && mc.world.getEntityById(id) == null) {
                  if (this.showLogMessage.getValue()) {
                     this.warning("§7BadPacketsW detected!", new Object[0]);
                  }

                  event.setCancelled(true);
               }
            }

            if (packet instanceof SlotChangedStateC2SPacket wrapper && this.badY.getValue() && (wrapper.slotId() < 0 || wrapper.slotId() > 8)) {
               if (this.showLogMessage.getValue()) {
                  this.warning("§7BadPacketsY detected!", new Object[0]);
               }

               event.setCancelled(true);
            }

            if (packet instanceof UpdateSelectedSlotC2SPacket wrapper && (this.badA.getValue() || this.fastSwitch.getValue())) {
               if (this.lastSlot == wrapper.getSelectedSlot() && !this.fastSwitch.getValue()) {
                  event.setCancelled(true);
               }

               this.lastSlot = wrapper.getSelectedSlot();
            }

            if (packet instanceof UpdateSelectedSlotS2CPacket var13) {
               UpdateSelectedSlotS2CPacket var30 = var13;

               int var31;
               try {
                  var31 = var30.slot();
               } catch (Throwable var8) {
                  throw new MatchException(var8.toString(), var8);
               }

               int var26 = var31;
               if (this.badA.getValue() || this.fastSwitch.getValue()) {
                  this.lastSlot = var26;
               }
            }

            if (packet instanceof UpdateSelectedSlotC2SPacket wrapper && this.fastSwitch.getValue()) {
               this.lastSlot = this.nowSlot;
               this.nowSlot = wrapper.getSelectedSlot();
               if ((this.lastSlot != 0 || this.nowSlot != 8) && (this.lastSlot != 8 || this.nowSlot != 0)) {
                  int i = this.lastSlot;
                  if (this.lastSlot < this.nowSlot) {
                     event.cancel();

                     while (i != this.nowSlot - 1) {
                        PacketUtils.sendPacketNoEvent(new UpdateSelectedSlotC2SPacket(++i));
                     }

                     PacketUtils.sendPacketNoEvent(packet);
                  }

                  if (this.lastSlot > this.nowSlot) {
                     event.cancel();

                     while (i != this.nowSlot + 1) {
                        PacketUtils.sendPacketNoEvent(new UpdateSelectedSlotC2SPacket(--i));
                     }

                     PacketUtils.sendPacketNoEvent(packet);
                  }
               }
            }

            if (this.inv.getValue()
               && (
                  packet instanceof ClickSlotC2SPacket slot && slot.getSyncId() == mc.player.playerScreenHandler.syncId
                     || packet instanceof CloseHandledScreenC2SPacket close && close.getSyncId() == mc.player.playerScreenHandler.syncId
               )) {
               event.setCancelled(true);
               if (mc.player.lastSprinting) {
                  PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
               }

               PacketUtils.sendPacketNoEvent(packet);
               if (mc.player.lastSprinting && mc.player.isSprinting()) {
                  PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
               }
            }

            if (packet instanceof ClientCommandC2SPacket wrapperx) {
               if (wrapperx.getMode() == Mode.START_SPRINTING) {
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

                        this.processBadpacketX(Mode.START_SPRINTING);
                        event.setCancelled(true);
                     }

                     this.sprint = true;
                  }
               } else if (wrapperx.getMode() == Mode.STOP_SPRINTING) {
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

                        this.processBadpacketX(Mode.STOP_SPRINTING);
                        event.setCancelled(true);
                     }

                     this.sprint = true;
                  }
               }
            }

            if (this.inv.getValue()
               && (
                  packet instanceof ClickSlotC2SPacket slot && slot.getSyncId() == mc.player.playerScreenHandler.syncId
                     || packet instanceof CloseHandledScreenC2SPacket close && close.getSyncId() == mc.player.playerScreenHandler.syncId
               )) {
               event.setCancelled(true);
               if (mc.player.lastSprinting) {
                  PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
               }

               PacketUtils.sendPacketNoEvent(packet);
               if (mc.player.lastSprinting && mc.player.isSprinting()) {
                  PacketUtils.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
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

   public static boolean postDelayPackets(Packet packet) {
      return mc.player == null ? false : !(mc.currentScreen instanceof DownloadingTerrainScreen) && !(mc.currentScreen instanceof DeathScreen);
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
         for (Packet packet : storedPackets) {
            if (mc.getNetworkHandler() != null && !ServerPacketManager.onPacket(packet)) {
               PacketEvent event = new PacketEvent(TransferOrigin.RECEIVE, packet, mc.getNetworkHandler().getConnection());
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

   public void processBadpacketX(Mode mode) {
      this.badpacketXQueue.add(mode);
   }
}
