package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.hud.Position;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.gui.hud.TargetHud;
import dev.southside.gui.hud.TargetHud2;
import dev.southside.gui.hud.TargetHud3;
import dev.southside.gui.hud.TargetHud4;
import dev.southside.gui.hud.TargetHud5;
import dev.southside.gui.hud.TargetHud6;
import dev.southside.gui.hud.TargetHud7;
import dev.southside.gui.hud.TargetHud8;
import dev.southside.gui.hud.TargetHudSimple;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.misc.BezierUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class TargetHUD extends Module {
   public final Dropdown mode = new Dropdown("Mode", "Vanilla", "Vanilla", "Retro", "ModernVanilla", "Old", "Plus", "Myau", "New", "Southside", "Simple");
   public final Switch multipleTargets = new Switch("Multiple Targets", true);
   private final BezierUtil southsideIndicator = new BezierUtil(4.0F, 0.0F);
   public final cc.polyfrost.oneconfig.config.options.impl.HUD hud = new cc.polyfrost.oneconfig.config.options.impl.HUD(
      "Target HUD", new Hud(100.0F, 0.0F, Position.AnchorPosition.MIDDLE_CENTER.ordinal(), 1.0F) {
         @Override
         protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            String var7 = TargetHUD.this.mode.getMode();
            switch (var7) {
               case "Vanilla":
                  this.drawVanilla(context, matrices, x, y, scale, example);
                  break;
               case "Retro":
                  this.drawRetro(context, matrices, x, y, scale, example);
                  break;
               case "ModernVanilla":
                  this.drawModern(context, matrices, x, y, scale, example);
                  break;
               case "Old":
                  this.drawOld(context, matrices, x, y, scale, example);
                  break;
               case "Plus":
                  this.drawPlus(context, matrices, x, y, scale, example);
                  break;
               case "Myau":
                  this.drawMyau(context, matrices, x, y, scale, example);
                  break;
               case "New":
                  this.drawNew(context, matrices, x, y, scale, example);
                  break;
               case "Simple":
                  this.drawSimple(context, matrices, x, y, scale, example);
                  break;
               default:
                  this.drawSouthside(context, matrices, x, y, scale, example);
            }
         }

         private void drawRetro(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               Scissor scissor = scissorHelper.scissor(vg, x - 5.0F, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
               float targetY = 0.0F;
               ArrayList<TargetHud8.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets8.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud8.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 67.5F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }

               scissorHelper.resetScissor(vg, scissor);
            });
            context.getMatrices().push();
            context.enableScissor((int)(x * scale), (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud8.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets8.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawBigHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawVanilla(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               Scissor scissor = scissorHelper.scissor(vg, x - 5.0F, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
               float targetY = 0.0F;
               ArrayList<TargetHud.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 57.5F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }

               scissorHelper.resetScissor(vg, scissor);
            });
            context.getMatrices().push();
            context.enableScissor((int)(x - 5.0F * scale), (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawBigHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawModern(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHud2.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets2.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud2.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 50.0F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }
            });
            context.getMatrices().push();
            context.enableScissor((int)x, (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud2.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets2.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawBigHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawOld(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHud3.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets3.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud3.TargetHUDComponent component = components.get(i);
                  if (component.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                     if (component.updateTimer.hasReached(1000.0)) {
                        component.y.update(-50.0F * scale);
                     } else {
                        component.y.update(targetY);
                        targetY += 50.0F * scale;
                     }

                     component.draw(vg, scale, x, y);
                  }
               }
            });
         }

         private void drawPlus(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHud4.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets4.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud4.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 50.0F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }
            });
            context.getMatrices().push();
            context.enableScissor((int)x, (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud4.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets4.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawAvatar(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawMyau(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHud5.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets5.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud5.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 50.0F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }
            });
            context.getMatrices().push();
            context.enableScissor((int)x, (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud5.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets5.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawBigHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawNew(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHud6.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets6.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud6.TargetHUDComponent component = components.get(i);
                  if (component.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                     if (component.updateTimer.hasReached(1000.0)) {
                        component.y.update(-88.0F * scale);
                     } else {
                        component.y.update(targetY);
                        targetY += 100.0F * scale;
                     }

                     component.draw(vg, scale, x, y);
                  }
               }
            });
         }

         private void drawSouthside(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               Scissor scissor = scissorHelper.scissor(vg, x - 5.0F * scale, y, UResolution.getScaledWidth(), UResolution.getScaledHeight());
               float targetY = 0.0F;
               float indicatorTarget = -50.0F * scale;
               ArrayList<TargetHud7.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targets7.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHud7.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 50.0F * scale;
                     }

                     if (componentx.entity == KillAura.target) {
                        indicatorTarget = componentx.y.get();
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }

               TargetHUD.this.southsideIndicator.update(indicatorTarget);
               if (TargetHUD.this.southsideIndicator.get() > -50.0F * scale) {
                  float indicatorX = x - 5.0F * scale;
                  float indicatorY = y + TargetHUD.this.southsideIndicator.get() + 4.0F * scale;
                  float indicatorHeight = Math.max(6.0F * scale, 42.0F * scale - 8.0F * scale);
                  nanoVGHelper.drawRoundedRect(vg, indicatorX, indicatorY, 2.0F * scale, indicatorHeight, Color.WHITE.getRGB(), 1.0F * scale);
               }

               scissorHelper.resetScissor(vg, scissor);
            });
            context.getMatrices().push();
            context.enableScissor((int)(x - 5.0F * scale), (int)y, UResolution.getScaledWidth(), UResolution.getScaledHeight());

            for (TargetHud7.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targets7.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         private void drawSimple(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
            NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
            nanoVGHelper.setupAndDraw(true, vg -> {
               float targetY = 0.0F;
               ArrayList<TargetHudSimple.TargetHUDComponent> components = new ArrayList<>(TargetHUD.this.targetsSimple.values());

               for (int i = 0; i < components.size(); i++) {
                  TargetHudSimple.TargetHUDComponent componentx = components.get(i);
                  if (componentx.updateTimer.hasReached(10000.0)) {
                     components.remove(i);
                     i--;
                  } else if (TargetHUD.this.multipleTargets.getValue() || componentx.entity == KillAura.target) {
                     if (componentx.updateTimer.hasReached(1000.0)) {
                        componentx.y.update(-50.0F * scale);
                     } else {
                        componentx.y.update(targetY);
                        targetY += 48.0F * scale;
                     }

                     componentx.draw(vg, scale, x, y);
                  }
               }
            });
            context.getMatrices().push();
            context.enableScissor((int)x, (int)y, (int)(x + 170.0F * scale), (int)(y + 999.0F));

            for (TargetHudSimple.TargetHUDComponent component : new ArrayList<>(TargetHUD.this.targetsSimple.values())) {
               if (TargetHUD.this.multipleTargets.getValue() || component.entity == KillAura.target) {
                  component.drawBigHead(context, scale, x, y);
               }
            }

            context.disableScissor();
            context.getMatrices().pop();
         }

         @Override
         protected float getWidth(float scale, boolean example) {
            String var3 = TargetHUD.this.mode.getMode();

            return switch (var3) {
               case "Vanilla", "Retro" -> 197.5F * scale;
               case "ModernVanilla", "Myau" -> 160.0F * scale;
               case "Simple" -> 170.0F * scale;
               case "Old", "Plus" -> 200.0F * scale;
               case "New" -> 280.0F * scale;
               case "Southside" -> 120.0F * scale;
               default -> 120.0F * scale;
            };
         }

         @Override
         protected float getHeight(float scale, boolean example) {
            String var3 = TargetHUD.this.mode.getMode();

            return switch (var3) {
               case "Vanilla" -> 52.5F * scale;
               case "Retro" -> 62.5F * scale;
               case "ModernVanilla", "Old", "Plus", "Myau" -> 40.0F * scale;
               case "Simple" -> 44.0F * scale;
               case "New" -> 88.0F * scale;
               case "Southside" -> 42.0F * scale;
               default -> 42.0F * scale;
            };
         }
      }
   );
   public final HashMap<UUID, TargetHud.TargetHUDComponent> targets = new HashMap<>();
   public final HashMap<UUID, TargetHud2.TargetHUDComponent> targets2 = new HashMap<>();
   public final HashMap<UUID, TargetHud3.TargetHUDComponent> targets3 = new HashMap<>();
   public final HashMap<UUID, TargetHud4.TargetHUDComponent> targets4 = new HashMap<>();
   public final HashMap<UUID, TargetHud5.TargetHUDComponent> targets5 = new HashMap<>();
   public final HashMap<UUID, TargetHud6.TargetHUDComponent> targets6 = new HashMap<>();
   public final HashMap<UUID, TargetHud7.TargetHUDComponent> targets7 = new HashMap<>();
   public final HashMap<UUID, TargetHud8.TargetHUDComponent> targets8 = new HashMap<>();
   public final HashMap<UUID, TargetHudSimple.TargetHUDComponent> targetsSimple = new HashMap<>();

   public TargetHUD() {
      super(Category.Render, "TargetHUD", "目标显示");
   }

   private void updateTarget(AbstractClientPlayerEntity player) {
      if (!this.targets.containsKey(player.getUuid())) {
         this.targets.put(player.getUuid(), new TargetHud.TargetHUDComponent(player));
      } else {
         this.targets.get(player.getUuid()).entity = player;
      }

      this.targets.get(player.getUuid()).updateTimer.reset();
      if (!this.targets2.containsKey(player.getUuid())) {
         this.targets2.put(player.getUuid(), new TargetHud2.TargetHUDComponent(player));
      } else {
         this.targets2.get(player.getUuid()).entity = player;
      }

      this.targets2.get(player.getUuid()).updateTimer.reset();
      if (!this.targets3.containsKey(player.getUuid())) {
         this.targets3.put(player.getUuid(), new TargetHud3.TargetHUDComponent(player));
      } else {
         this.targets3.get(player.getUuid()).entity = player;
      }

      this.targets3.get(player.getUuid()).updateTimer.reset();
      if (!this.targets4.containsKey(player.getUuid())) {
         this.targets4.put(player.getUuid(), new TargetHud4.TargetHUDComponent(player));
      } else {
         this.targets4.get(player.getUuid()).entity = player;
      }

      this.targets4.get(player.getUuid()).updateTimer.reset();
      if (!this.targets5.containsKey(player.getUuid())) {
         this.targets5.put(player.getUuid(), new TargetHud5.TargetHUDComponent(player));
      } else {
         this.targets5.get(player.getUuid()).entity = player;
      }

      this.targets5.get(player.getUuid()).updateTimer.reset();
      if (!this.targets6.containsKey(player.getUuid())) {
         this.targets6.put(player.getUuid(), new TargetHud6.TargetHUDComponent(player));
      } else {
         this.targets6.get(player.getUuid()).entity = player;
      }

      this.targets6.get(player.getUuid()).updateTimer.reset();
      if (!this.targets7.containsKey(player.getUuid())) {
         this.targets7.put(player.getUuid(), new TargetHud7.TargetHUDComponent(player));
      } else {
         this.targets7.get(player.getUuid()).entity = player;
      }

      this.targets7.get(player.getUuid()).updateTimer.reset();
      if (!this.targets8.containsKey(player.getUuid())) {
         this.targets8.put(player.getUuid(), new TargetHud8.TargetHUDComponent(player));
      } else {
         this.targets8.get(player.getUuid()).entity = player;
      }

      this.targets8.get(player.getUuid()).updateTimer.reset();
      if (!this.targetsSimple.containsKey(player.getUuid())) {
         this.targetsSimple.put(player.getUuid(), new TargetHudSimple.TargetHUDComponent(player));
      } else {
         this.targetsSimple.get(player.getUuid()).entity = player;
      }

      this.targetsSimple.get(player.getUuid()).updateTimer.reset();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.target instanceof AbstractClientPlayerEntity player) {
         this.updateTarget(player);
      }
   }

   @EventHandler(
      priority = -350234
   )
   public void onRotate(RotationEvent event) {
      if (KillAura.targets.isEmpty()) {
         this.clearTargets();
      }

      if (this.multipleTargets.getValue()) {
         if (KillAura.targets == null) {
            return;
         }

         KillAura.targets.forEach(entityLivingBase -> {
            if (entityLivingBase instanceof AbstractClientPlayerEntity playerx) {
               this.updateTarget(playerx);
            }
         });
      } else if (KillAura.target instanceof AbstractClientPlayerEntity player) {
         this.updateTarget(player);
      }
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.clearTargets();
   }

   private void clearTargets() {
      this.targets.clear();
      this.targets2.clear();
      this.targets3.clear();
      this.targets4.clear();
      this.targets5.clear();
      this.targets6.clear();
      this.targets7.clear();
      this.targets8.clear();
      this.targetsSimple.clear();
   }
}
