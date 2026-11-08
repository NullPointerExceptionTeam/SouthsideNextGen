package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.FakePlayerEntity;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_1541;
import net.minecraft.class_1657;
import net.minecraft.class_1667;
import net.minecraft.class_1676;
import net.minecraft.class_1680;
import net.minecraft.class_1681;
import net.minecraft.class_1934;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2827;
import net.minecraft.class_2848;
import net.minecraft.class_2879;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2848.class_2849;

public class Blink extends Module {
   private LinkedList<List<class_2596<?>>> packets = new LinkedList<>();
   public FakePlayerEntity fakePlayer;
   public final Slider maxPacketsValue = new Slider("Max Packets", 1, 1, 200, 1);
   public static final Switch slowReleaseValue = new Switch("Slow Release", false);
   public final Switch ignoreKeepAlive = new Switch("Ignore KeepAlive", false);
   public final Switch antiAimValue = new Switch("Anti Aim", true);
   public final Switch antiAimArrowValue = new Switch("Arrow", true);
   public final Switch antiAimProjectileValue = new Switch("Projectile", true);
   public final Switch antiAimTNTValue = new Switch("TNT", true);
   public final Switch antiAimPlayerValue = new Switch("Player", true);
   private int ticks;
   private List<class_243> realPos = new ArrayList<>();

   public Blink() {
      super(Category.Player, "Blink", "666开瞬移了。。");
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.packets.clear();
         this.packets.add(new ArrayList<>());
         mc.field_1724.method_31549().field_7476 = true;
         mc.field_1724.method_51889(class_1934.field_9215);
         this.fakePlayer = new FakePlayerEntity(mc.field_1724, mc.field_1724.method_7334().getName(), 20.0F, true);
         this.fakePlayer.doNotPush = true;
         this.fakePlayer.hideWhenInsideCamera = true;
         this.fakePlayer.spawn();
         this.ticks = 0;
         this.realPos.add(new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()));
         super.onEnable();
      } else {
         this.setEnable(false);
      }
   }

   @Override
   public void onDisable() {
      this.packets.forEach(this::sendTick);
      this.packets.clear();
      this.realPos.clear();
      this.ticks = 0;
      if (this.fakePlayer != null) {
         if (mc.field_1687 != null) {
            mc.field_1687.method_2945(this.fakePlayer.method_5628(), class_5529.field_26999);
         }

         this.fakePlayer = null;
      }

      super.onDisable();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.setEnable(false);
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      class_2596<?> packet = event.getPacket();
      if (!this.ignoreKeepAlive.getValue() || !(packet instanceof class_2827)) {
         if (event.getOrigin() == TransferOrigin.SEND) {
            if (this.packets.isEmpty()) {
               this.packets.add(new LinkedList<>());
            }

            this.packets.getLast().add(packet);
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.ticks++;
      this.packets.add(new ArrayList<>());
      if (mc.field_1724 != null) {
         this.realPos.add(new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()));
      }

      if (this.packets.size() >= this.maxPacketsValue.getValue().intValue() || this.ticks % 10 == 0 && slowReleaseValue.getValue()) {
         this.poll();
      }

      if (this.antiAimValue.getValue()) {
         while (true) {
            boolean dangerous = false;
            if (mc.field_1687 != null) {
               for (class_1297 entity : mc.field_1687.method_18112()) {
                  if (mc.field_1724 != null
                     && (
                        this.antiAimArrowValue.getValue() && entity instanceof class_1667 arrow && !arrow.method_65059()
                           || this.antiAimProjectileValue.getValue() && entity instanceof class_1680
                           || entity instanceof class_1681
                           || this.antiAimTNTValue.getValue() && entity instanceof class_1541
                           || this.antiAimPlayerValue.getValue()
                              && entity instanceof class_1657 player
                              && this.fakePlayer != null
                              && entity.method_5628() != this.fakePlayer.method_5628()
                              && player.method_5628() != mc.field_1724.method_5628()
                     )
                     && this.isDangerous(entity)) {
                     dangerous = true;
                     break;
                  }
               }
            }

            if (!dangerous || this.packets.size() < 3) {
               break;
            }

            this.poll();
         }
      }
   }

   private boolean isDangerous(class_1297 entity) {
      float width = 1.2F;
      float height = 2.2F;
      if (entity instanceof class_1676 projectile) {
         float motionSlowdown = 0.99F;
         float size = 1.2F;
         float gravity = 0.05F;
         if (!(projectile instanceof class_1667) && (projectile instanceof class_1680 || projectile instanceof class_1681)) {
            gravity = 1.2F;
            size = 0.25F;
         }

         return ProjectionUtil.predictBox(
            entity.method_23317(),
            entity.method_23318(),
            entity.method_23321(),
            entity.method_18798().field_1352,
            entity.method_18798().field_1351,
            entity.method_18798().field_1350,
            motionSlowdown,
            size,
            gravity,
            this.realPos,
            0.6F,
            2.2F
         );
      } else if (entity instanceof class_1541 tnt) {
         class_243 pos = this.realPos.get(0);
         return tnt.method_5649(pos.field_1352, pos.field_1351, pos.field_1350) <= 30.0 && tnt.method_6969() <= 10;
      } else if (!(entity instanceof class_1657 player)) {
         return false;
      } else {
         class_243 pos = this.realPos.get(0);
         return player.method_5649(pos.field_1352, pos.field_1351, pos.field_1350) <= 25.0 && player.method_5667() != mc.field_1724.method_5667();
      }
   }

   private void poll() {
      if (!this.packets.isEmpty()) {
         this.sendTick(this.packets.getFirst());
         this.packets.removeFirst();
      }
   }

   private void sendTick(List<class_2596<?>> tick) {
      if (Objects.requireNonNull(mc.method_1562()).method_48296() != null) {
         tick.forEach(packet -> {
            PacketUtils.sendPacketNoEvent((class_2596<?>)packet);
            this.handleFakePlayerPacket((class_2596<?>)packet);
         });
      }

      if (!this.realPos.isEmpty()) {
         this.realPos.remove(0);
      }
   }

   private void handleFakePlayerPacket(class_2596<?> packet) {
      if (packet instanceof class_2829 position) {
         this.fakePlayer.method_5814(position.field_12889, position.field_12886, position.field_12884);
         this.fakePlayer.method_36456(this.fakePlayer.method_36454());
         this.fakePlayer.method_36457(this.fakePlayer.method_36455());
         this.fakePlayer.method_24830(position.method_12273());
      } else if (packet instanceof class_2831 rotation) {
         this.fakePlayer.method_5814(this.fakePlayer.method_23317(), this.fakePlayer.method_23318(), this.fakePlayer.method_23321());
         this.fakePlayer.method_5847(rotation.method_12271(this.fakePlayer.method_36454()));
         this.fakePlayer.method_36457(rotation.method_12270(this.fakePlayer.method_36455()));
         this.fakePlayer.method_24830(rotation.method_12273());
         this.fakePlayer.method_5847(rotation.method_12271(this.fakePlayer.method_36454()));
         this.fakePlayer.method_36456(rotation.method_12271(this.fakePlayer.method_36454()));
         this.fakePlayer.method_36457(rotation.field_12885);
      } else if (packet instanceof class_2830 positionRotation) {
         this.fakePlayer.method_5814(positionRotation.field_12889, positionRotation.field_12886, positionRotation.field_12884);
         this.fakePlayer.method_36456(positionRotation.field_12887);
         this.fakePlayer.method_36457(positionRotation.field_12885);
         this.fakePlayer.method_24830(positionRotation.method_12273());
         this.fakePlayer.method_5847(positionRotation.method_12271(this.fakePlayer.method_36454()));
         this.fakePlayer.method_36456(positionRotation.method_12271(this.fakePlayer.method_36454()));
         this.fakePlayer.method_36457(positionRotation.field_12885);
      } else if (packet instanceof class_2848 action) {
         if (action.method_12365() == class_2849.field_12981) {
            this.fakePlayer.method_5728(true);
         } else if (action.method_12365() == class_2849.field_12985) {
            this.fakePlayer.method_5728(false);
         } else if (action.method_12365() == class_2849.field_12979) {
            this.fakePlayer.method_5660(true);
         } else if (action.method_12365() == class_2849.field_12984) {
            this.fakePlayer.method_5660(false);
         }
      } else if (packet instanceof class_2879 CNM) {
         this.fakePlayer.method_6104(CNM.method_12512());
      }
   }

   public static int getReleaseSpeedModifier(int ticks, int length) {
      if (length >= 195) {
         return 1;
      } else {
         return slowReleaseValue.getValue() ? (ticks % 10 == 0 ? 1 : 0) : 0;
      }
   }
}
