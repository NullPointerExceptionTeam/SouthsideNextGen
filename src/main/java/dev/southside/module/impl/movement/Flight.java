package dev.southside.module.impl.movement;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.MotionEvent;
import dev.southside.event.events.MoveEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.player.FakePlayerEntity;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.world.EntityUtils;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2848;
import net.minecraft.class_2879;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2848.class_2849;

public class Flight extends Module {
   public final Dropdown mode = new Dropdown("Mode", "Vanilla", "Vanilla", "Cubecraft", "CubeZoom");
   public Slider hSpeed = new Slider("Horizontal", 1.0F, 0.0F, 10.0F);
   public Slider vSpeed = new Slider("Vertical", 0.78F, 0.0F, 5.0F);
   private final Slider cubeSpeed = new Slider("Speed", 0.33, 0.1, 0.34, 0.01);
   private final Slider blinkDelay = new Slider("Release Delay", 300, 100, 2000, 50);
   private final Switch viewBobbing = new Switch("View Bobbing", true);
   private final MSTimer blinkTimer = new MSTimer();
   private final MSTimer hoverTimer = new MSTimer();
   private boolean cancelTick = false;
   private boolean isBlinking = false;
   public FakePlayerEntity fakePlayer;

   public Flight() {
      super(Category.Movement, "Flight");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(this.hSpeed.getLabel(), () -> this.mode.isMode("Cubecraft"));
      this.hideIf(this.vSpeed.getLabel(), () -> this.mode.isMode("Cubecraft"));
      this.hideIf(this.cubeSpeed.getLabel(), () -> !this.mode.isMode("Cubecraft"));
      this.hideIf(this.blinkDelay.getLabel(), () -> !this.mode.isMode("CubeZoom"));
      this.hideIf(this.viewBobbing.getLabel(), () -> !this.mode.isMode("CubeZoom"));
   }

   @Override
   public void onEnable() {
      this.cancelTick = false;
      if (this.mode.isMode("CubeZoom")) {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            this.fakePlayer = new FakePlayerEntity(mc.field_1724, mc.field_1724.method_7334().getName(), 20.0F, true);
            this.fakePlayer.doNotPush = true;
            this.fakePlayer.hideWhenInsideCamera = true;
            this.fakePlayer.spawn();
         }

         BlinkUtils.blink();
         this.isBlinking = true;
         this.blinkTimer.reset();
      }
   }

   @Override
   public void onDisable() {
      this.cancelTick = false;
      if (this.isBlinking) {
         BlinkUtils.stopBlink();
         BlinkUtils.releasePacket();
         this.isBlinking = false;
      }

      if (this.fakePlayer != null) {
         if (mc.field_1687 != null) {
            mc.field_1687.method_2945(this.fakePlayer.method_5628(), class_5529.field_26999);
         }

         this.fakePlayer = null;
      }
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.cancelTick = false;
      if (this.fakePlayer != null) {
         if (mc.field_1687 != null) {
            mc.field_1687.method_2945(this.fakePlayer.method_5628(), class_5529.field_26999);
         }

         this.fakePlayer = null;
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1724 != null) {
         class_2596<?> packet = event.getPacket();
         String var3 = this.mode.getMode();
         switch (var3) {
            case "CubeZoom":
               if (this.blinkTimer.hasPassed(this.blinkDelay.getValue().intValue()) && this.fakePlayer != null) {
                  this.handleFakePlayerPacket(packet);
               }
            case "Vanilla":
         }
      }
   }

   @EventHandler
   public void onMotion(MotionEvent event) {
      this.setSuffix(this.mode.getMode());
      if (mc.field_1724 != null) {
         if (this.mode.isMode("Vanilla")) {
            if (PlayerUtils.isMoving()) {
               double[] dir = PlayerUtils.forward(this.hSpeed.getValue().floatValue());
               mc.field_1724.method_18800(dir[0], 0.0, dir[1]);
            } else {
               mc.field_1724.method_18800(0.0, 0.0, 0.0);
            }

            if (mc.field_1690.field_1903.method_1434()) {
               mc.field_1724.method_18799(mc.field_1724.method_18798().method_1031(0.0, this.vSpeed.getValue().floatValue(), 0.0));
            }

            if (mc.field_1690.field_1832.method_1434()) {
               mc.field_1724.method_18799(mc.field_1724.method_18798().method_1031(0.0, -this.vSpeed.getValue().floatValue(), 0.0));
            }
         } else if (this.mode.isMode("CubeZoom")) {
            if (PlayerUtils.isMoving()) {
               double[] dir = PlayerUtils.forward(this.hSpeed.getValue().floatValue());
               mc.field_1724.method_18800(dir[0], 0.0, dir[1]);
               if (mc.field_1690.field_1903.method_1434()) {
                  mc.field_1724.method_18799(mc.field_1724.method_18798().method_1031(0.0, this.vSpeed.getValue().floatValue(), 0.0));
               }

               if (mc.field_1690.field_1832.method_1434()) {
                  mc.field_1724.method_18799(mc.field_1724.method_18798().method_1031(0.0, -this.vSpeed.getValue().floatValue(), 0.0));
               }

               if (this.blinkTimer.hasPassed(this.blinkDelay.getValue().intValue())) {
                  BlinkUtils.releasePacket();
                  BlinkUtils.blink();
                  this.blinkTimer.reset();
                  if (this.fakePlayer != null) {
                     this.fakePlayer.method_5814(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
                     this.fakePlayer.method_36456(mc.field_1724.method_36454());
                     this.fakePlayer.method_36457(mc.field_1724.method_36455());
                  }
               }
            } else {
               BlinkUtils.releasePacket();
               BlinkUtils.stopBlink();
               this.isBlinking = false;
            }
         }
      }
   }

   @EventHandler
   public void onMove(MoveEvent event) {
      if (this.mode.isMode("Cubecraft")) {
         class_243 vec3d = EntityUtils.withStrafe(new class_243(event.getX(), event.getY(), event.getZ()), 1.0);
         event.setX(vec3d.field_1352);
         event.setY(vec3d.field_1351);
         event.setZ(vec3d.field_1350);
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1724 != null) {
         String var2 = this.mode.getMode();
         switch (var2) {
            case "CubeZoom":
               if (mc.method_1542()) {
                  this.setEnable(false);
               }

               if (this.viewBobbing.getValue()) {
               }

               if (mc.field_1724.method_24828() || PlayerUtils.isMoving()) {
                  this.hoverTimer.reset();
               } else if (this.hoverTimer.hasPassed(3000L)) {
                  ChatUtils.info("想死号?!");
                  this.setEnable(false);
               }
               break;
            case "Cubecraft":
               if (mc.field_1724.method_24828()) {
                  return;
               }

               if (mc.field_1724.method_5715()) {
                  mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, -0.4, mc.field_1724.method_18798().field_1350);
               } else if (mc.field_1724.field_3913.field_54155.comp_3163()) {
                  mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.42, mc.field_1724.method_18798().field_1350);
               } else {
                  mc.field_1724.method_18800(mc.field_1724.method_18798().field_1352, 0.2, mc.field_1724.method_18798().field_1350);
               }

               mc.field_1724
                  .method_18799(EntityUtils.withStrafe(mc.field_1724.method_18798(), RandomUtils.random(this.cubeSpeed.getMin(), this.cubeSpeed.getMax())));
               if (!this.cancelTick) {
                  PlayerUtils.setSkipTicks(6);
                  this.cancelTick = true;
               }
         }
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
      } else if (packet instanceof class_2879 handSwingC2SPacket) {
         this.fakePlayer.method_6104(handSwingC2SPacket.method_12512());
      }
   }
}
