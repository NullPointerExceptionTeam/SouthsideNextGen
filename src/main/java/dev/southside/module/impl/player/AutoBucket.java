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
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_2404;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_3965;
import net.minecraft.class_9362;

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
                    AutoBucket.mc.field_1724.method_31548().field_7545 = this.oldSlot;
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
        boolean killAuraBusy = KillAura.target != null && (double)AutoBucket.mc.field_1724.method_5739(KillAura.target) <= ((Number)moduleManager.get(KillAura.class).range.getValue()).doubleValue();
        boolean velocityBusy = moduleManager.get(Velocity.class).isEnabled() && Velocity.gotKB;
        return killAuraBusy || velocityBusy;
    }

    @EventHandler(priority=-200)
    public void onItemUse(ItemUseEvent event) {
        if (((Boolean)this.fireballCooldown.getValue()).booleanValue() && !event.isCancelled() && AutoBucket.mc.field_1724 != null && (AutoBucket.mc.field_1724.method_6079().method_7909().equals(class_1802.field_8814) || AutoBucket.mc.field_1724.method_6047().method_7909().equals(class_1802.field_8814))) {
            this.coolDownTicks = 10;
        }
    }

    @EventHandler
    public void onTick(RotationEvent event) {
        boolean shouldPlaceWater;
        if (AutoBucket.mc.field_1724 == null || PlayerUtils.ticksSinceTeleport < 3) {
            return;
        }
        if (this.coolDownTicks > 0) {
            --this.coolDownTicks;
        }
        if (AutoBucket.mc.field_1724.method_7337() || Southside.get().getModuleManager().get(LongJump.class).isEnabled()) {
            return;
        }
        if (AutoBucket.mc.field_1724.method_6115() || AutoBucket.mc.field_1755 != null) {
            return;
        }
        boolean shouldMLG = (Boolean)this.mlg.getValue() != false && this.willTakeFallDamage() && !AutoBucket.mc.field_1724.method_5816() && !AutoBucket.nextTickWillLanding();
        boolean shouldAntiFire = (Boolean)this.fire.getValue() != false && AutoBucket.mc.field_1724.method_5809() && !AutoBucket.mc.field_1724.method_6059(class_1294.field_5918) && !AutoBucket.mc.field_1724.method_5816() && !this.isAuraWorking() && this.coolDownTicks <= 0;
        boolean bl = shouldPlaceWater = shouldMLG || shouldAntiFire;
        if (shouldPlaceWater) {
            int waterBucketSlot = this.getBucketSlot(class_1802.field_8705);
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
            class_3965 result = ClientRayTraceUtil.getFacedBlock(RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, blockState -> {
                if (blockState.method_26204() instanceof class_2404) {
                    return false;
                }
                return ClientRayTraceUtil.isIgnoredBlock(blockState);
            });
            if (result == null) {
                if (AutoBucket.mc.field_1724.method_31548().method_5438(AutoBucket.mc.field_1724.method_31548().field_7545).method_7909().equals(class_1802.field_8550)) {
                    this.shouldReceive = true;
                }
                if (this.oldSlot != -1) {
                    AutoBucket.mc.field_1724.method_31548().field_7545 = this.oldSlot;
                    this.oldSlot = -1;
                }
                return;
            }
            this.interactRequired = false;
            AutoBucket.mc.field_1765 = result;
            class_1269 res = AutoBucket.mc.field_1761.method_2919((class_1657)AutoBucket.mc.field_1724, class_1268.field_5808);
            PacketOrderManager.rightClicking = true;
            if (res.method_23665()) {
                AutoBucket.mc.field_1724.method_6104(class_1268.field_5808);
            }
            if (AutoBucket.mc.field_1724.method_31548().method_5438(AutoBucket.mc.field_1724.method_31548().field_7545).method_7909().equals(class_1802.field_8550)) {
                this.shouldReceive = true;
            }
            if (this.oldSlot != -1) {
                AutoBucket.mc.field_1724.method_31548().field_7545 = this.oldSlot;
                this.oldSlot = -1;
            }
        }
    }

    private boolean hasEmptyBucket() {
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = AutoBucket.mc.field_1724.method_31548().method_5438(i);
            if (stack == null || stack.method_7909() != class_1802.field_8550) continue;
            return true;
        }
        return false;
    }

    private PlaceData findBestPlacePos() {
        ArrayList<PlaceData> possibleBoxes = new ArrayList<PlaceData>();
        class_238 playerBox = AutoBucket.mc.field_1724.method_5829();
        playerBox.method_997(AutoBucket.mc.field_1724.method_18798());
        for (int x = -AutoBucket.getXMotion(); x <= AutoBucket.getXMotion(); ++x) {
            for (int z = -AutoBucket.getZMotion(); z <= AutoBucket.getZMotion(); ++z) {
                for (int y = 0; y <= AutoBucket.getYMotion(); ++y) {
                    class_2338 pos = AutoBucket.mc.field_1724.method_24515().method_10069(x, 0, z).method_10087(y);
                    class_2680 state = AutoBucket.mc.field_1687.method_8320(pos);
                    class_238 bb = new class_238(pos).method_35578((double)(pos.method_10264() + 3));
                    if (!bb.method_994(playerBox) || state.method_26215()) continue;
                    possibleBoxes.add(new PlaceData(pos, bb, new AtomicBoolean(false)));
                }
            }
        }
        possibleBoxes.sort(Comparator.comparingDouble(p -> p.pos.method_10264()));
        Collections.reverse(possibleBoxes);
        PlaceData best = (PlaceData)possibleBoxes.getFirst();
        possibleBoxes.removeIf(p -> p.pos.method_10264() != best.pos().method_10264());
        possibleBoxes.sort(Comparator.comparingDouble(p -> class_243.method_24954((class_2382)p.pos()).method_1025(playerBox.method_1005().method_38499(class_2350.class_2351.field_11052, playerBox.field_1322))));
        possibleBoxes.sort(Comparator.comparing(p -> !p.bb.method_1006(AutoBucket.mc.field_1724.method_19538())));
        return (PlaceData)possibleBoxes.getFirst();
    }

    private void placeWaterBucket(int waterBucketSlot) {
        if (waterBucketSlot == -1) {
            return;
        }
        this.oldSlot = AutoBucket.mc.field_1724.method_31548().field_7545;
        PlaceData best = this.findBestPlacePos();
        Rotation r = AutoBucket.getLookAtWaterBlock(best.pos());
        if (best.pitchOnly().get()) {
            r.setYaw(AutoBucket.mc.field_1724.method_36454());
            r.setPitch(90.0f);
        }
        AutoBucket.mc.field_1724.method_31548().field_7545 = waterBucketSlot;
        this.interactItem(r);
        this.shouldReceive = true;
    }

    public static boolean isAirBlocksBelow(int high) {
        ArrayList<PlaceData> possibleBoxes = new ArrayList<PlaceData>();
        for (int x = -AutoBucket.getXMotion(); x <= AutoBucket.getXMotion(); ++x) {
            for (int z = -AutoBucket.getZMotion(); z <= AutoBucket.getZMotion(); ++z) {
                for (int y = 0; y <= high; ++y) {
                    class_2338 pos = AutoBucket.mc.field_1724.method_24515().method_10069(x, 0, z).method_10087(y);
                    class_2680 state = AutoBucket.mc.field_1687.method_8320(pos);
                    class_238 bb = new class_238(pos).method_35578((double)(pos.method_10264() + 3));
                    if (!bb.method_994(AutoBucket.mc.field_1724.method_5829()) || state.method_26215()) continue;
                    possibleBoxes.add(new PlaceData(pos, bb, new AtomicBoolean(false)));
                }
            }
        }
        return possibleBoxes.isEmpty();
    }

    private static int getYMotion() {
        return (int)Math.ceil(AutoBucket.mc.field_1724.method_18798().field_1351 * AutoBucket.mc.field_1724.method_18798().field_1351);
    }

    private static int getXMotion() {
        return (int)Math.ceil(AutoBucket.mc.field_1724.method_18798().field_1352 * AutoBucket.mc.field_1724.method_18798().field_1352);
    }

    private static int getZMotion() {
        return (int)Math.ceil(AutoBucket.mc.field_1724.method_18798().field_1350 * AutoBucket.mc.field_1724.method_18798().field_1350);
    }

    private static boolean nextTickWillLanding() {
        return AutoBucket.isAirBlocksBelow(AutoBucket.getYMotion());
    }

    private void retrieveWaterBlock() {
        this.oldSlot = AutoBucket.mc.field_1724.method_31548().field_7545;
        int emptyBucketSlot = this.getBucketSlot(class_1802.field_8550);
        if (emptyBucketSlot == -1) {
            return;
        }
        class_2338 pos = AutoBucket.findScoopableWaterBlock();
        if (pos == null) {
            return;
        }
        Rotation r = AutoBucket.getLookAtWaterBlock(pos);
        AutoBucket.mc.field_1724.method_31548().field_7545 = emptyBucketSlot;
        this.interactItem(r);
        this.shouldReceive = false;
    }

    private int getBucketSlot(class_1792 item) {
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = AutoBucket.mc.field_1724.method_31548().method_5438(i);
            if (stack == null || stack.method_7909() != item) continue;
            return i;
        }
        return -1;
    }

    private static Rotation getLookAtWaterBlock(class_2338 targetPos) {
        if (AutoBucket.mc.field_1724 == null || targetPos == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return RotationUtils.getRandomRotationTo(targetPos, class_2350.field_11036);
    }

    public void interactItem(Rotation rotation) {
        RotationUtils.setRotation(rotation, this.rotationPriority());
        this.interactRequired = true;
    }

    public static class_2338 findScoopableWaterBlock() {
        class_1937 world = AutoBucket.mc.field_1724.method_37908();
        class_2338 playerBlockPos = AutoBucket.mc.field_1724.method_24515();
        double maxReachDistance = AutoBucket.mc.field_1724.method_55754();
        int reach = (int)Math.ceil(maxReachDistance);
        ArrayList<class_2338> possible = new ArrayList<class_2338>();
        for (int x = -reach; x <= reach; ++x) {
            for (int y = -reach; y <= reach; ++y) {
                for (int z = -reach; z <= reach; ++z) {
                    class_2338 currentPos = playerBlockPos.method_10069(x, y, z);
                    class_3610 fluidState = world.method_8316(currentPos);
                    if (!fluidState.method_15771() || fluidState.method_15772() != class_3612.field_15910) continue;
                    possible.add(currentPos);
                }
            }
        }
        possible.removeIf(blockPos -> {
            Rotation rotation = AutoBucket.getLookAtWaterBlock(blockPos);
            return !ClientRayTraceUtil.didHitBlockFace((class_1657)AutoBucket.mc.field_1724, rotation.yaw, rotation.pitch, blockPos, class_2350.field_11036, false, blockState -> {
                if (blockState.method_26204() instanceof class_2404) {
                    return false;
                }
                return ClientRayTraceUtil.isIgnoredBlock(blockState);
            });
        });
        if (possible.isEmpty()) {
            return null;
        }
        return (class_2338)possible.getFirst();
    }

    private boolean willTakeFallDamage() {
        class_1293 effect;
        float safeDistance = ((Number)this.minDistance.getValue()).floatValue();
        if (AutoBucket.mc.field_1724.method_6059(class_1294.field_5913) && (effect = AutoBucket.mc.field_1724.method_6112(class_1294.field_5913)) != null) {
            int jumpBoostLevel = effect.method_5578() + 1;
            safeDistance += (float)jumpBoostLevel;
        }
        return AutoBucket.mc.field_1724.field_6017 > safeDistance;
    }

    @Override
    public int rotationPriority() {
        class_1799 item;
        if (((Boolean)this.macePriority.getValue()).booleanValue() && (item = AutoBucket.mc.field_1724.method_6047()) != null && item.method_7909() instanceof class_9362 && class_9362.method_58659((class_1309)AutoBucket.mc.field_1724)) {
            KillAura killaura = Southside.get().getModuleManager().get(KillAura.class);
            return killaura.rotationPriority() - 1;
        }
        return 2 + this.extraRotationPriority();
    }

    @Override
    public int extraRotationPriority() {
        return 495;
    }

    record PlaceData(class_2338 pos, class_238 bb, AtomicBoolean pitchOnly) {
    }
}

