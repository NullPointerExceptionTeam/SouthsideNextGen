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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.TridentItem;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.HitResult.Type;
import org.joml.Vector3d;

public class Trajectories extends Module {
   private static final double MULTISHOT_OFFSET = Math.toRadians(10.0);
   private final List<Item> items = new ArrayList<>();
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

   private boolean itemFilter(Item item) {
      return item instanceof BowItem
         || item instanceof CrossbowItem
         || item instanceof FishingRodItem
         || item instanceof TridentItem
         || item instanceof SnowballItem
         || item instanceof EggItem
         || item instanceof EnderPearlItem
         || item instanceof ExperienceBottleItem
         || item instanceof ThrowablePotionItem
         || Items.TNT.equals(item);
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.items.clear();
      this.items.addAll(this.getDefaultItems());
   }

   private List<Item> getDefaultItems() {
      List<Item> defaults = new ArrayList<>();

      for (Item item : Registries.ITEM) {
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

   private void calculatePath(PlayerEntity player, float tickDelta) {
      this.clearPaths();
      ItemStack itemStack = player.getMainHandStack();
      if (itemStack == null || itemStack.isEmpty() || !this.items.contains(itemStack.getItem())) {
         itemStack = player.getOffHandStack();
      }

      if (itemStack != null && !itemStack.isEmpty()) {
         if (this.items.contains(itemStack.getItem())) {
            if (this.simulator.set(player, itemStack, 0.0, this.accurate.getValue(), this.tnt.getValue(), false, tickDelta)) {
               this.getEmptyPath().calculate();
               if (itemStack.getItem() instanceof CrossbowItem && EnchantmentUtils.getLevel(Enchantments.MULTISHOT, itemStack) > 0) {
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

   private void calculateFiredPath(Entity entity, float tickDelta) {
      this.clearPaths();
      if (this.simulator.set(entity, this.accurate.getValue(), this.tnt.getValue(), tickDelta)) {
         this.getEmptyPath().setStart(entity, tickDelta).calculate();
      }
   }

   @EventHandler
   private void onRender3D(Render3DEvent event) {
      if (mc.world != null && mc.player != null) {
         this.pearlLandingInfos.clear();

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (this.otherPlayers.getValue() || player == mc.player) {
               this.calculatePath(player, event.getTickCounter());

               for (Trajectories.Path path : this.paths) {
                  path.render(event);
               }
            }
         }

         if (this.firedProjectiles.getValue() || this.pearlLandingTag.getValue()) {
            List<Integer> alivePearlIds = new ArrayList<>();
            Iterator var11 = mc.world.getEntities().iterator();

            while (true) {
               boolean shouldRenderPath;
               boolean shouldCollectTag;
               Entity entity;
               while (true) {
                  if (!var11.hasNext()) {
                     this.cachedPearlLandings.keySet().removeIf(id -> !alivePearlIds.contains(id));
                     return;
                  }

                  entity = (Entity)var11.next();
                  if (entity instanceof ProjectileEntity) {
                     boolean isPearl = entity instanceof EnderPearlEntity;
                     shouldRenderPath = this.firedProjectiles.getValue() && (!this.firedProjectilesOnlyPearl.getValue() || isPearl);
                     shouldCollectTag = this.pearlLandingTag.getValue() && isPearl;
                     if (shouldRenderPath || shouldCollectTag) {
                        if (isPearl) {
                           alivePearlIds.add(entity.getId());
                        }

                        if (!shouldCollectTag || !this.cachedPearlLandings.containsKey(entity.getId())) {
                           break;
                        }

                        this.pearlLandingInfos.add(this.cachedPearlLandings.get(entity.getId()));
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

               if (shouldCollectTag && !this.cachedPearlLandings.containsKey(entity.getId())) {
                  this.collectPearlLandingInfo((EnderPearlEntity)entity);
               }
            }
         }
      }
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      if (this.pearlLandingTag.getValue() && !this.pearlLandingInfos.isEmpty()) {
         if (mc.world != null && mc.player != null) {
            for (Trajectories.PearlLandingInfo info : this.pearlLandingInfos) {
               Vec2f screenPos = ProjectionUtil.worldScreenPos(new Vec3d(info.x, info.y + 0.25, info.z));
               if (screenPos != null) {
                  this.renderPearlLandingTag(event.getDrawContext(), screenPos, info);
               }
            }
         }
      }
   }

   private void collectPearlLandingInfo(EnderPearlEntity pearl) {
      if (!this.paths.isEmpty()) {
         Trajectories.Path path = this.paths.get(0);
         if (path.hasImpactPoint()) {
            Vector3d impact = path.getImpactPoint();
            int blockX = MathHelper.floor(impact.x);
            int blockY = MathHelper.floor(impact.y);
            int blockZ = MathHelper.floor(impact.z);
            String ownerName = "Unknown";
            Entity owner = pearl.getOwner();
            if (owner != null) {
               ownerName = owner.getName().getString();
            }

            Trajectories.PearlLandingInfo info = new Trajectories.PearlLandingInfo(ownerName, impact.x, impact.y, impact.z, blockX, blockY, blockZ);
            this.cachedPearlLandings.put(pearl.getId(), info);
            this.pearlLandingInfos.add(info);
         }
      }
   }

   private void renderPearlLandingTag(DrawContext context, Vec2f screenPos, Trajectories.PearlLandingInfo info) {
      String line1 = info.ownerName + " Pearl";
      String line2 = "XYZ: " + info.blockX + ", " + info.blockY + ", " + info.blockZ;
      int fontHeight = 9;
      int lineHeight = fontHeight + 2;
      int textWidth = Math.max(mc.textRenderer.getWidth(line1), mc.textRenderer.getWidth(line2));
      int paddingX = 4;
      int paddingY = 2;
      int boxWidth = textWidth + paddingX * 2;
      int boxHeight = lineHeight * 2 + paddingY * 2;
      int left = -boxWidth / 2;
      int top = -(boxHeight + 2);
      int right = left + boxWidth;
      int bottom = top + boxHeight;
      float alpha = MathHelper.clamp(this.pearlTagBgAlpha.getValue().floatValue(), 0.0F, 1.0F);
      int bg = ((int)(alpha * 255.0F) & 0xFF) << 24;
      int textColor = -1;
      float scale = this.pearlTagScale.getValue().floatValue();
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(screenPos.x, screenPos.y, 0.0F);
      matrices.scale(scale, scale, 1.0F);
      context.fill(left, top, right, bottom, bg);
      int y = top + paddingY;
      context.drawText(mc.textRenderer, Text.literal(line1), left + (boxWidth - mc.textRenderer.getWidth(line1)) / 2, y, textColor, true);
      y += lineHeight;
      context.drawText(mc.textRenderer, Text.literal(line2), left + (boxWidth - mc.textRenderer.getWidth(line2)) / 2, y, textColor, true);
      matrices.pop();
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
      private Entity collidingEntity;
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
            HitResult result = Trajectories.this.simulator.tick();
            if (result != null) {
               this.processHitResult(result);
               break;
            }

            this.addPoint();
         }
      }

      public Trajectories.Path setStart(Entity entity, double tickDelta) {
         return this;
      }

      private void addPoint() {
         this.points.add(Trajectories.this.vec3s.get().set(Trajectories.this.simulator.pos));
      }

      private void processHitResult(HitResult result) {
         this.impactPoint = new Vector3d(result.getPos().x, result.getPos().y, result.getPos().z);
         if (result.getType() == Type.BLOCK) {
            BlockHitResult r = (BlockHitResult)result;
            this.hitQuad = true;
            this.hitQuadX1 = r.getPos().x;
            this.hitQuadY1 = r.getPos().y;
            this.hitQuadZ1 = r.getPos().z;
            this.hitQuadX2 = r.getPos().x;
            this.hitQuadY2 = r.getPos().y;
            this.hitQuadZ2 = r.getPos().z;
            if (r.getSide() == Direction.UP || r.getSide() == Direction.DOWN) {
               this.hitQuadHorizontal = true;
               this.hitQuadX1 -= 0.25;
               this.hitQuadZ1 -= 0.25;
               this.hitQuadX2 += 0.25;
               this.hitQuadZ2 += 0.25;
            } else if (r.getSide() != Direction.NORTH && r.getSide() != Direction.SOUTH) {
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

            this.points.add(Trajectories.this.vec3s.get().set(Trajectories.this.simulator.with(result.getPos())));
         } else if (result.getType() == Type.ENTITY) {
            this.collidingEntity = ((EntityHitResult)result).getEntity();
            this.points
               .add(
                  Trajectories.this.vec3s
                     .get()
                     .set(Trajectories.this.simulator.with(result.getPos()))
                     .add(0.0, this.collidingEntity.getHeight() / 2.0, 0.0)
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

         if (this.collidingEntity != null && this.collidingEntity != Trajectories.mc.player) {
            double x = (this.collidingEntity.getX() - this.collidingEntity.prevX) * event.getTickCounter();
            double y = (this.collidingEntity.getY() - this.collidingEntity.prevY) * event.getTickCounter();
            double z = (this.collidingEntity.getZ() - this.collidingEntity.prevZ) * event.getTickCounter();
            Box box = this.collidingEntity.getBoundingBox();
            Render3D.boxLines(
               x + box.minX,
               y + box.minY,
               z + box.minZ,
               x + box.maxX,
               y + box.maxY,
               z + box.maxZ,
               Trajectories.this.lineColor.getValue().toJavaColor()
            );
            Render3D.boxSides(
               x + box.minX,
               y + box.minY,
               z + box.minZ,
               x + box.maxX,
               y + box.maxY,
               z + box.maxZ,
               Trajectories.this.sideColor.getValue().toJavaColor()
            );
         }
      }
   }

   private record PearlLandingInfo(String ownerName, double x, double y, double z, int blockX, int blockY, int blockZ) {
   }
}
