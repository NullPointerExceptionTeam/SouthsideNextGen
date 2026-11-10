package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.ItemUseEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.combat.Velocity;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.item.MaceItem;

public class AutoBucket
extends Module {
    private final Switch antiPause = new Switch("AntiPause", true);
    private final Slider minDistance = new Slider("MinDistance", 3, 3, 10);
    private final Switch mlg = new Switch("AutoMLG", false);
    private final Switch fire = new Switch("AntiFire", false);
    private final Switch macePriority = new Switch("Mace Priority", true);
    public final Switch fireballCooldown = new Switch("Fireball Cooldown", true);
    private int coolDownTicks = 0;
    public boolean interactRequired = false;
    int oldSlot = -1;
    public boolean shouldReceive = false;
    private int busyTicks = 0;

    public AutoBucket() {
        super(Category.Player, "AutoBucket", "\u6211\u4f1a\u843d\u5730\u6c34\uff0c");
    }

    public boolean isBusy() {
        if (this.interactRequired || this.shouldReceive) {
            ++this.busyTicks;
            if (this.busyTicks > 20) {
                this.shouldReceive = false;
                this.interactRequired = false;
                this.busyTicks = 0;
                if (this.oldSlot != -1) {
                    AutoBucket.mc.player.getInventory().selectedSlot = this.oldSlot;
                    this.oldSlot = -1;
                }
                return false;
            }
            return true;
        }
        this.busyTicks = 0;
        return false;
    }

    private boolean isAuraWorking() {
        if (!((Boolean)this.antiPause.getValue()).booleanValue()) {
            return false;
        }
        ModuleManager moduleManager = Southside.INSTANCE.getModuleManager();
        boolean killAuraBusy = KillAura.target != null && (double)AutoBucket.mc.player.distanceTo(KillAura.target) <= ((Number)moduleManager.get(KillAura.class).range.getValue()).doubleValue();
        boolean velocityBusy = moduleManager.get(Velocity.class).isEnabled() && Velocity.gotKB;
        return killAuraBusy || velocityBusy;
    }

    @EventHandler(priority=-200)
    public void onItemUse(ItemUseEvent event) {
        if (((Boolean)this.fireballCooldown.getValue()).booleanValue() && !event.isCancelled() && AutoBucket.mc.player != null && (AutoBucket.mc.player.getOffHandStack().getItem().equals(Items.FIRE_CHARGE) || AutoBucket.mc.player.getMainHandStack().getItem().equals(Items.FIRE_CHARGE))) {
            this.coolDownTicks = 10;
        }
    }

    @EventHandler
    public void onTick(RotationEvent event) {
        boolean shouldPlaceWater;
        if (AutoBucket.mc.player == null || PlayerUtils.ticksSinceTeleport < 3) {
            return;
        }
        if (this.coolDownTicks > 0) {
            --this.coolDownTicks;
        }
        if (AutoBucket.mc.player.isCreative() || Southside.get().getModuleManager().get(LongJump.class).isEnabled()) {
            return;
        }
        if (AutoBucket.mc.player.isUsingItem() || AutoBucket.mc.currentScreen != null) {
            return;
        }
        boolean shouldMLG = (Boolean)this.mlg.getValue() != false && this.willTakeFallDamage() && !AutoBucket.mc.player.isInsideWaterOrBubbleColumn() && !AutoBucket.nextTickWillLanding();
        boolean shouldAntiFire = (Boolean)this.fire.getValue() != false && AutoBucket.mc.player.isOnFire() && !AutoBucket.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && !AutoBucket.mc.player.isInsideWaterOrBubbleColumn() && !this.isAuraWorking() && this.coolDownTicks <= 0;
        boolean bl = shouldPlaceWater = shouldMLG || shouldAntiFire;
        if (shouldPlaceWater) {
            int waterBucketSlot = this.getBucketSlot(Items.WATER_BUCKET);
            if (waterBucketSlot == -1) {
                shouldPlaceWater = false;
            }
            this.placeWaterBucket(waterBucketSlot);
        }
        if (!shouldPlaceWater && this.hasEmptyBucket() && this.shouldReceive) {
            this.retrieveWaterBlock();
        }
    }

    @EventHandler
    public void onRotationApplied(RotationAppliedEvent event) {
        if (RotationUtils.getRotation() == null) {
            return;
        }
        if (this.interactRequired) {
            BlockHitResult result = ClientRayTraceUtil.getFacedBlock(RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, blockState -> {
                if (blockState.getBlock() instanceof FluidBlock) {
                    return false;
                }
                return ClientRayTraceUtil.isIgnoredBlock(blockState);
            });
            if (result == null) {
                if (AutoBucket.mc.player.getInventory().getStack(AutoBucket.mc.player.getInventory().selectedSlot).getItem().equals(Items.BUCKET)) {
                    this.shouldReceive = true;
                }
                if (this.oldSlot != -1) {
                    AutoBucket.mc.player.getInventory().selectedSlot = this.oldSlot;
                    this.oldSlot = -1;
                }
                return;
            }
            this.interactRequired = false;
            AutoBucket.mc.crosshairTarget = result;
            ActionResult res = AutoBucket.mc.interactionManager.interactItem((PlayerEntity)AutoBucket.mc.player, Hand.MAIN_HAND);
            PacketOrderManager.rightClicking = true;
            if (res.isAccepted()) {
                AutoBucket.mc.player.swingHand(Hand.MAIN_HAND);
            }
            if (AutoBucket.mc.player.getInventory().getStack(AutoBucket.mc.player.getInventory().selectedSlot).getItem().equals(Items.BUCKET)) {
                this.shouldReceive = true;
            }
            if (this.oldSlot != -1) {
                AutoBucket.mc.player.getInventory().selectedSlot = this.oldSlot;
                this.oldSlot = -1;
            }
        }
    }

    private boolean hasEmptyBucket() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoBucket.mc.player.getInventory().getStack(i);
            if (stack == null || stack.getItem() != Items.BUCKET) continue;
            return true;
        }
        return false;
    }

    private PlaceData findBestPlacePos() {
        ArrayList<PlaceData> possibleBoxes = new ArrayList<PlaceData>();
        Box playerBox = AutoBucket.mc.player.getBoundingBox();
        playerBox.offset(AutoBucket.mc.player.getVelocity());
        for (int x = -AutoBucket.getXMotion(); x <= AutoBucket.getXMotion(); ++x) {
            for (int z = -AutoBucket.getZMotion(); z <= AutoBucket.getZMotion(); ++z) {
                for (int y = 0; y <= AutoBucket.getYMotion(); ++y) {
                    BlockPos pos = AutoBucket.mc.player.getBlockPos().add(x, 0, z).down(y);
                    BlockState state = AutoBucket.mc.world.getBlockState(pos);
                    Box bb = new Box(pos).withMaxY((double)(pos.getY() + 3));
                    if (!bb.intersects(playerBox) || state.isAir()) continue;
                    possibleBoxes.add(new PlaceData(pos, bb, new AtomicBoolean(false)));
                }
            }
        }
        possibleBoxes.sort(Comparator.comparingDouble(p -> p.pos.getY()));
        Collections.reverse(possibleBoxes);
        PlaceData best = (PlaceData)possibleBoxes.getFirst();
        possibleBoxes.removeIf(p -> p.pos.getY() != best.pos().getY());
        possibleBoxes.sort(Comparator.comparingDouble(p -> Vec3d.of((Vec3i)p.pos()).squaredDistanceTo(playerBox.getCenter().withAxis(Direction.Axis.Y, playerBox.minY))));
        possibleBoxes.sort(Comparator.comparing(p -> !p.bb.contains(AutoBucket.mc.player.getPos())));
        return (PlaceData)possibleBoxes.getFirst();
    }

    private void placeWaterBucket(int waterBucketSlot) {
        if (waterBucketSlot == -1) {
            return;
        }
        this.oldSlot = AutoBucket.mc.player.getInventory().selectedSlot;
        PlaceData best = this.findBestPlacePos();
        Rotation r = AutoBucket.getLookAtWaterBlock(best.pos());
        if (best.pitchOnly().get()) {
            r.setYaw(AutoBucket.mc.player.getYaw());
            r.setPitch(90.0f);
        }
        AutoBucket.mc.player.getInventory().selectedSlot = waterBucketSlot;
        this.interactItem(r);
        this.shouldReceive = true;
    }

    public static boolean isAirBlocksBelow(int high) {
        ArrayList<PlaceData> possibleBoxes = new ArrayList<PlaceData>();
        for (int x = -AutoBucket.getXMotion(); x <= AutoBucket.getXMotion(); ++x) {
            for (int z = -AutoBucket.getZMotion(); z <= AutoBucket.getZMotion(); ++z) {
                for (int y = 0; y <= high; ++y) {
                    BlockPos pos = AutoBucket.mc.player.getBlockPos().add(x, 0, z).down(y);
                    BlockState state = AutoBucket.mc.world.getBlockState(pos);
                    Box bb = new Box(pos).withMaxY((double)(pos.getY() + 3));
                    if (!bb.intersects(AutoBucket.mc.player.getBoundingBox()) || state.isAir()) continue;
                    possibleBoxes.add(new PlaceData(pos, bb, new AtomicBoolean(false)));
                }
            }
        }
        return possibleBoxes.isEmpty();
    }

    private static int getYMotion() {
        return (int)Math.ceil(AutoBucket.mc.player.getVelocity().y * AutoBucket.mc.player.getVelocity().y);
    }

    private static int getXMotion() {
        return (int)Math.ceil(AutoBucket.mc.player.getVelocity().x * AutoBucket.mc.player.getVelocity().x);
    }

    private static int getZMotion() {
        return (int)Math.ceil(AutoBucket.mc.player.getVelocity().z * AutoBucket.mc.player.getVelocity().z);
    }

    private static boolean nextTickWillLanding() {
        return AutoBucket.isAirBlocksBelow(AutoBucket.getYMotion());
    }

    private void retrieveWaterBlock() {
        this.oldSlot = AutoBucket.mc.player.getInventory().selectedSlot;
        int emptyBucketSlot = this.getBucketSlot(Items.BUCKET);
        if (emptyBucketSlot == -1) {
            return;
        }
        BlockPos pos = AutoBucket.findScoopableWaterBlock();
        if (pos == null) {
            return;
        }
        Rotation r = AutoBucket.getLookAtWaterBlock(pos);
        AutoBucket.mc.player.getInventory().selectedSlot = emptyBucketSlot;
        this.interactItem(r);
        this.shouldReceive = false;
    }

    private int getBucketSlot(Item item) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoBucket.mc.player.getInventory().getStack(i);
            if (stack == null || stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private static Rotation getLookAtWaterBlock(BlockPos targetPos) {
        if (AutoBucket.mc.player == null || targetPos == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return RotationUtils.getRandomRotationTo(targetPos, Direction.UP);
    }

    public void interactItem(Rotation rotation) {
        RotationUtils.setRotation(rotation, this.rotationPriority());
        this.interactRequired = true;
    }

    public static BlockPos findScoopableWaterBlock() {
        World world = AutoBucket.mc.player.getWorld();
        BlockPos playerBlockPos = AutoBucket.mc.player.getBlockPos();
        double maxReachDistance = AutoBucket.mc.player.getBlockInteractionRange();
        int reach = (int)Math.ceil(maxReachDistance);
        ArrayList<BlockPos> possible = new ArrayList<BlockPos>();
        for (int x = -reach; x <= reach; ++x) {
            for (int y = -reach; y <= reach; ++y) {
                for (int z = -reach; z <= reach; ++z) {
                    BlockPos currentPos = playerBlockPos.add(x, y, z);
                    FluidState fluidState = world.getFluidState(currentPos);
                    if (!fluidState.isStill() || fluidState.getFluid() != Fluids.WATER) continue;
                    possible.add(currentPos);
                }
            }
        }
        possible.removeIf(blockPos -> {
            Rotation rotation = AutoBucket.getLookAtWaterBlock(blockPos);
            return !ClientRayTraceUtil.didHitBlockFace((PlayerEntity)AutoBucket.mc.player, rotation.yaw, rotation.pitch, blockPos, Direction.UP, false, blockState -> {
                if (blockState.getBlock() instanceof FluidBlock) {
                    return false;
                }
                return ClientRayTraceUtil.isIgnoredBlock(blockState);
            });
        });
        if (possible.isEmpty()) {
            return null;
        }
        return (BlockPos)possible.getFirst();
    }

    private boolean willTakeFallDamage() {
        StatusEffectInstance effect;
        float safeDistance = ((Number)this.minDistance.getValue()).floatValue();
        if (AutoBucket.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && (effect = AutoBucket.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST)) != null) {
            int jumpBoostLevel = effect.getAmplifier() + 1;
            safeDistance += (float)jumpBoostLevel;
        }
        return AutoBucket.mc.player.fallDistance > safeDistance;
    }

    @Override
    public int rotationPriority() {
        ItemStack item;
        if (((Boolean)this.macePriority.getValue()).booleanValue() && (item = AutoBucket.mc.player.getMainHandStack()) != null && item.getItem() instanceof MaceItem && MaceItem.shouldDealAdditionalDamage((LivingEntity)AutoBucket.mc.player)) {
            KillAura killaura = Southside.get().getModuleManager().get(KillAura.class);
            return killaura.rotationPriority() - 1;
        }
        return 2 + this.extraRotationPriority();
    }

    @Override
    public int extraRotationPriority() {
        return 495;
    }

    record PlaceData(BlockPos pos, Box bb, AtomicBoolean pitchOnly) {
    }
}

