package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.render.Render3D;
import dev.southside.utils.misc.Pool;
import dev.southside.utils.player.EnchantmentUtils;
import dev.southside.utils.world.ProjectileEntitySimulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1753;
import net.minecraft.class_1764;
import net.minecraft.class_1771;
import net.minecraft.class_1776;
import net.minecraft.class_1779;
import net.minecraft.class_1787;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1823;
import net.minecraft.class_1835;
import net.minecraft.class_1893;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4537;
import net.minecraft.class_4587;
import net.minecraft.class_7923;
import net.minecraft.class_239.class_240;
import org.joml.Vector3d;

public class Trajectories extends Module {
   private static final double MULTISHOT_OFFSET = Math.toRadians(10.0);
   private final List<class_1792> items = new ArrayList<>();
   private final Switch otherPlayers = new Switch("Other Players", true);
   private final Switch firedProjectiles = new Switch("Fired Projectiles", false);
   private final Switch firedProjectilesOnlyPearl = new Switch("Fired Projectiles Only Pearl", false);
   private final Switch pearlLandingTag = new Switch("Pearl Landing Tag", true);
   private final Slider pearlTagScale = new Slider("Pearl Tag Scale", 0.8, 0.2, 2.0, 0.1);
   private final Slider pearlTagBgAlpha = new Slider("Pearl Tag Bg Alpha", 0.25, 0.05, 1.0, 0.05);
   private final Switch accurate = new Switch("Accurate", false);
   private final Switch tnt = new Switch("Tnt", true);
   private final Slider simulationSteps = new Slider("Simulation Steps", 500, 50, 5000, 1);
   private final Color sideColor = new Color("Side Color", true, new OneColor(255, 0, 0, 35));
   private final Color lineColor = new Color("Line Color", true, new OneColor(255, 0, 0, 35));
   private final ProjectileEntitySimulator simulator = new ProjectileEntitySimulator();
   private final Pool<Vector3d> vec3s = new Pool<>(Vector3d::new);
   private final List<Trajectories.Path> paths = new ArrayList<>();
   private final List<Trajectories.PearlLandingInfo> pearlLandingInfos = new ArrayList<>();
   private final Map<Integer, Trajectories.PearlLandingInfo> cachedPearlLandings = new HashMap<>();

   public Trajectories() {
      super(Category.Render, "Trajectories", "Projectile trajectory prediction.");
      this.hideIf(this.firedProjectilesOnlyPearl.getLabel(), () -> !this.firedProjectiles.getValue());
      this.hideIf(this.pearlTagScale.getLabel(), () -> !this.pearlLandingTag.getValue());
      this.hideIf(this.pearlTagBgAlpha.getLabel(), () -> !this.pearlLandingTag.getValue());
   }

   private boolean itemFilter(class_1792 item) {
      return item instanceof class_1753
         || item instanceof class_1764
         || item instanceof class_1787
         || item instanceof class_1835
         || item instanceof class_1823
         || item instanceof class_1771
         || item instanceof class_1776
         || item instanceof class_1779
         || item instanceof class_4537
         || class_1802.field_8626.equals(item);
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.items.clear();
      this.items.addAll(this.getDefaultItems());
   }

   private List<class_1792> getDefaultItems() {
      List<class_1792> defaults = new ArrayList<>();

      for (class_1792 item : class_7923.field_41178) {
         if (this.itemFilter(item)) {
            defaults.add(item);
         }
      }

      return defaults;
   }

   private Trajectories.Path getEmptyPath() {
      for (Trajectories.Path path : this.paths) {
         if (path.points.isEmpty()) {
            return path;
         }
      }

      Trajectories.Path pathx = new Trajectories.Path();
      this.paths.add(pathx);
      return pathx;
   }

   private void clearPaths() {
      for (Trajectories.Path path : this.paths) {
         path.clear();
      }
   }

   private void calculatePath(class_1657 player, float tickDelta) {
      this.clearPaths();
      class_1799 itemStack = player.method_6047();
      if (itemStack == null || itemStack.method_7960() || !this.items.contains(itemStack.method_7909())) {
         itemStack = player.method_6079();
      }

      if (itemStack != null && !itemStack.method_7960()) {
         if (this.items.contains(itemStack.method_7909())) {
            if (this.simulator.set(player, itemStack, 0.0, this.accurate.getValue(), this.tnt.getValue(), false, tickDelta)) {
               this.getEmptyPath().calculate();
               if (itemStack.method_7909() instanceof class_1764 && EnchantmentUtils.getLevel(class_1893.field_9108, itemStack) > 0) {
                  if (!this.simulator.set(player, itemStack, MULTISHOT_OFFSET, this.accurate.getValue(), this.tnt.getValue(), false, tickDelta)) {
                     return;
                  }

                  this.getEmptyPath().calculate();
                  if (!this.simulator.set(player, itemStack, -MULTISHOT_OFFSET, this.accurate.getValue(), this.tnt.getValue(), false, tickDelta)) {
                     return;
                  }

                  this.getEmptyPath().calculate();
               }
            }
         }
      }
   }

   private void calculateFiredPath(class_1297 entity, float tickDelta) {
      this.clearPaths();
      if (this.simulator.set(entity, this.accurate.getValue(), this.tnt.getValue(), tickDelta)) {
         this.getEmptyPath().setStart(entity, tickDelta).calculate();
      }
   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.pearlLandingInfos.clear();

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (this.otherPlayers.getValue() || player == mc.field_1724) {
               this.calculatePath(player, event.getTickCounter());

               for (Trajectories.Path path : this.paths) {
                  path.render(event);
               }
            }
         }

         if (this.firedProjectiles.getValue() || this.pearlLandingTag.getValue()) {
            List<Integer> alivePearlIds = new ArrayList<>();
            Iterator var11 = mc.field_1687.method_18112().iterator();

            while (true) {
               boolean shouldRenderPath;
               boolean shouldCollectTag;
               class_1297 entity;
               while (true) {
                  if (!var11.hasNext()) {
                     this.cachedPearlLandings.keySet().removeIf(id -> !alivePearlIds.contains(id));
                     return;
                  }

                  entity = (class_1297)var11.next();
                  if (entity instanceof class_1676) {
                     boolean isPearl = entity instanceof class_1684;
                     shouldRenderPath = this.firedProjectiles.getValue() && (!this.firedProjectilesOnlyPearl.getValue() || isPearl);
                     shouldCollectTag = this.pearlLandingTag.getValue() && isPearl;
                     if (shouldRenderPath || shouldCollectTag) {
                        if (isPearl) {
                           alivePearlIds.add(entity.method_5628());
                        }

                        if (!shouldCollectTag || !this.cachedPearlLandings.containsKey(entity.method_5628())) {
                           break;
                        }

                        this.pearlLandingInfos.add(this.cachedPearlLandings.get(entity.method_5628()));
                        if (shouldRenderPath) {
                           break;
                        }
                     }
                  }
               }

               this.calculateFiredPath(entity, event.getTickCounter());
               if (shouldRenderPath) {
                  for (Trajectories.Path path : this.paths) {
                     path.render(event);
                  }
               }

               if (shouldCollectTag && !this.cachedPearlLandings.containsKey(entity.method_5628())) {
                  this.collectPearlLandingInfo((class_1684)entity);
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (this.pearlLandingTag.getValue() && !this.pearlLandingInfos.isEmpty()) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            for (Trajectories.PearlLandingInfo info : this.pearlLandingInfos) {
               class_241 screenPos = ProjectionUtil.worldScreenPos(new class_243(info.x, info.y + 0.25, info.z));
               if (screenPos != null) {
                  this.renderPearlLandingTag(event.getDrawContext(), screenPos, info);
               }
            }
         }
      }
   }

   private void collectPearlLandingInfo(class_1684 pearl) {
      if (!this.paths.isEmpty()) {
         Trajectories.Path path = this.paths.get(0);
         if (path.hasImpactPoint()) {
            Vector3d impact = path.getImpactPoint();
            int blockX = class_3532.method_15357(impact.x);
            int blockY = class_3532.method_15357(impact.y);
            int blockZ = class_3532.method_15357(impact.z);
            String ownerName = "Unknown";
            class_1297 owner = pearl.method_24921();
            if (owner != null) {
               ownerName = owner.method_5477().getString();
            }

            Trajectories.PearlLandingInfo info = new Trajectories.PearlLandingInfo(ownerName, impact.x, impact.y, impact.z, blockX, blockY, blockZ);
            this.cachedPearlLandings.put(pearl.method_5628(), info);
            this.pearlLandingInfos.add(info);
         }
      }
   }

   private void renderPearlLandingTag(class_332 context, class_241 screenPos, Trajectories.PearlLandingInfo info) {
      String line1 = info.ownerName + " Pearl";
      String line2 = "XYZ: " + info.blockX + ", " + info.blockY + ", " + info.blockZ;
      int fontHeight = 9;
      int lineHeight = fontHeight + 2;
      int textWidth = Math.max(mc.field_1772.method_1727(line1), mc.field_1772.method_1727(line2));
      int paddingX = 4;
      int paddingY = 2;
      int boxWidth = textWidth + paddingX * 2;
      int boxHeight = lineHeight * 2 + paddingY * 2;
      int left = -boxWidth / 2;
      int top = -(boxHeight + 2);
      int right = left + boxWidth;
      int bottom = top + boxHeight;
      float alpha = class_3532.method_15363(this.pearlTagBgAlpha.getValue().floatValue(), 0.0F, 1.0F);
      int bg = ((int)(alpha * 255.0F) & 0xFF) << 24;
      int textColor = -1;
      float scale = this.pearlTagScale.getValue().floatValue();
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      matrices.method_46416(screenPos.field_1343, screenPos.field_1342, 0.0F);
      matrices.method_22905(scale, scale, 1.0F);
      context.method_25294(left, top, right, bottom, bg);
      int y = top + paddingY;
      context.method_51439(mc.field_1772, class_2561.method_43470(line1), left + (boxWidth - mc.field_1772.method_1727(line1)) / 2, y, textColor, true);
      y += lineHeight;
      context.method_51439(mc.field_1772, class_2561.method_43470(line2), left + (boxWidth - mc.field_1772.method_1727(line2)) / 2, y, textColor, true);
      matrices.method_22909();
   }

   private class Path {
      private final List<Vector3d> points = new ArrayList<>();
      private boolean hitQuad;
      private boolean hitQuadHorizontal;
      private double hitQuadX1;
      private double hitQuadY1;
      private double hitQuadZ1;
      private double hitQuadX2;
      private double hitQuadY2;
      private double hitQuadZ2;
      private class_1297 collidingEntity;
      private Vector3d impactPoint;

      public void clear() {
         Trajectories.this.vec3s.freeAll(this.points);
         this.points.clear();
         this.hitQuad = false;
         this.collidingEntity = null;
         this.impactPoint = null;
      }

      public void calculate() {
         this.addPoint();

         for (int i = 0; i < (Trajectories.this.simulationSteps.getDouble() > 0.0 ? Trajectories.this.simulationSteps.getDouble() : 2.147483647E9); i++) {
            class_239 result = Trajectories.this.simulator.tick();
            if (result != null) {
               this.processHitResult(result);
               break;
            }

            this.addPoint();
         }
      }

      public Trajectories.Path setStart(class_1297 entity, double tickDelta) {
         return this;
      }

      private void addPoint() {
         this.points.add(Trajectories.this.vec3s.get().set(Trajectories.this.simulator.pos));
      }

      private void processHitResult(class_239 result) {
         this.impactPoint = new Vector3d(result.method_17784().field_1352, result.method_17784().field_1351, result.method_17784().field_1350);
         if (result.method_17783() == class_240.field_1332) {
            class_3965 r = (class_3965)result;
            this.hitQuad = true;
            this.hitQuadX1 = r.method_17784().field_1352;
            this.hitQuadY1 = r.method_17784().field_1351;
            this.hitQuadZ1 = r.method_17784().field_1350;
            this.hitQuadX2 = r.method_17784().field_1352;
            this.hitQuadY2 = r.method_17784().field_1351;
            this.hitQuadZ2 = r.method_17784().field_1350;
            if (r.method_17780() == class_2350.field_11036 || r.method_17780() == class_2350.field_11033) {
               this.hitQuadHorizontal = true;
               this.hitQuadX1 -= 0.25;
               this.hitQuadZ1 -= 0.25;
               this.hitQuadX2 += 0.25;
               this.hitQuadZ2 += 0.25;
            } else if (r.method_17780() != class_2350.field_11043 && r.method_17780() != class_2350.field_11035) {
               this.hitQuadHorizontal = false;
               this.hitQuadZ1 -= 0.25;
               this.hitQuadY1 -= 0.25;
               this.hitQuadZ2 += 0.25;
               this.hitQuadY2 += 0.25;
            } else {
               this.hitQuadHorizontal = false;
               this.hitQuadX1 -= 0.25;
               this.hitQuadY1 -= 0.25;
               this.hitQuadX2 += 0.25;
               this.hitQuadY2 += 0.25;
            }

            this.points.add(Trajectories.this.vec3s.get().set(Trajectories.this.simulator.with(result.method_17784())));
         } else if (result.method_17783() == class_240.field_1331) {
            this.collidingEntity = ((class_3966)result).method_17782();
            this.points
               .add(
                  Trajectories.this.vec3s
                     .get()
                     .set(Trajectories.this.simulator.with(result.method_17784()))
                     .add(0.0, this.collidingEntity.method_17682() / 2.0, 0.0)
               );
         }
      }

      public boolean hasImpactPoint() {
         return this.impactPoint != null;
      }

      public Vector3d getImpactPoint() {
         return this.impactPoint;
      }

      public void render(Render3DEvent event) {
         Vector3d prev = null;

         for (Vector3d point : this.points) {
            if (prev != null) {
               Render3D.line(prev.x, prev.y, prev.z, point.x, point.y, point.z, Trajectories.this.lineColor.getValue().getRGB());
            }

            prev = point;
         }

         if (this.hitQuad) {
            if (this.hitQuadHorizontal) {
               Render3D.sideHorizontalLines(
                  this.hitQuadX1,
                  this.hitQuadY1,
                  this.hitQuadZ1,
                  this.hitQuadX1 + 0.5,
                  this.hitQuadZ1 + 0.5,
                  Trajectories.this.lineColor.getValue().toJavaColor()
               );
               Render3D.sideHorizontalQuad(
                  this.hitQuadX1,
                  this.hitQuadY1,
                  this.hitQuadZ1,
                  this.hitQuadX1 + 0.5,
                  this.hitQuadZ1 + 0.5,
                  Trajectories.this.sideColor.getValue().toJavaColor()
               );
            } else {
               Render3D.sideVerticalLines(
                  this.hitQuadX1,
                  this.hitQuadY1,
                  this.hitQuadZ1,
                  this.hitQuadX2,
                  this.hitQuadY2,
                  this.hitQuadZ2,
                  Trajectories.this.lineColor.getValue().toJavaColor()
               );
               Render3D.sideVerticalQuad(
                  this.hitQuadX1,
                  this.hitQuadY1,
                  this.hitQuadZ1,
                  this.hitQuadX2,
                  this.hitQuadY2,
                  this.hitQuadZ2,
                  Trajectories.this.sideColor.getValue().toJavaColor()
               );
            }
         }

         if (this.collidingEntity != null && this.collidingEntity != Trajectories.mc.field_1724) {
            double x = (this.collidingEntity.method_23317() - this.collidingEntity.field_6014) * event.getTickCounter();
            double y = (this.collidingEntity.method_23318() - this.collidingEntity.field_6036) * event.getTickCounter();
            double z = (this.collidingEntity.method_23321() - this.collidingEntity.field_5969) * event.getTickCounter();
            class_238 box = this.collidingEntity.method_5829();
            Render3D.boxLines(
               x + box.field_1323,
               y + box.field_1322,
               z + box.field_1321,
               x + box.field_1320,
               y + box.field_1325,
               z + box.field_1324,
               Trajectories.this.lineColor.getValue().toJavaColor()
            );
            Render3D.boxSides(
               x + box.field_1323,
               y + box.field_1322,
               z + box.field_1321,
               x + box.field_1320,
               y + box.field_1325,
               z + box.field_1324,
               Trajectories.this.sideColor.getValue().toJavaColor()
            );
         }
      }
   }

   private record PearlLandingInfo(String ownerName, double x, double y, double z, int blockX, int blockY, int blockZ) {
   }
}
