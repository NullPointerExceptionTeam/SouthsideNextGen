package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.UpdateHeldItemEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.render.Render3D;
import dev.southside.utils.player.BedwarsCheckUtils;
import dev.southside.utils.player.BlockBreakUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;

public class BedAura
extends Module {
    private final Slider range = new Slider("Range", (Number)4.5, (Number)1.0, (Number)8.0, 0.1);
    private final Slider rate = new Slider("Rate", (Number)0.2, (Number)0.05, (Number)3.0, 0.05);
    private final Slider fov = new Slider("FOV", 360, 30, 360, 5);
    private final Dropdown breakMode = new Dropdown("Break Mode", "BreakNearBlock", "BreakNearBlock", "Full", "OnlyBed");
    private final Switch autoTool = new Switch("Auto Tool", true);
    private final Switch ignoreSelf = new Switch("Ignore Self", false);
    private final Switch pauseKillAura = new Switch("Pause KillAura", true);
    private final Switch renderBox = new Switch("Render Box", true);
    private final cc.polyfrost.oneconfig.config.options.impl.Color bedSideColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Bed Side Color", true, new OneColor(80, 255, 80, 60));
    private final cc.polyfrost.oneconfig.config.options.impl.Color bedLineColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Bed Line Color", true, new OneColor(80, 255, 80, 170));
    private final cc.polyfrost.oneconfig.config.options.impl.Color currentBreakSideColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Current Break Side Color", true, new OneColor(255, 90, 90, 90));
    private final cc.polyfrost.oneconfig.config.options.impl.Color currentBreakLineColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Current Break Line Color", true, new OneColor(255, 90, 90, 180));
    private final cc.polyfrost.oneconfig.config.options.impl.Color pathSideColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Path Side Color", true, new OneColor(80, 220, 255, 55));
    private final cc.polyfrost.oneconfig.config.options.impl.Color pathLineColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Path Line Color", true, new OneColor(80, 220, 255, 160));
    private BlockPos[] bedPos;
    private BlockPos currentBlock;
    private BlockPos previousBlockBroken;
    private float breakProgress;
    private boolean isBreaking;
    private long nextBreakTime;
    private long lastScanTime;
    private long nextAimTime;
    private int toolSlot;
    private int previousSlot = -1;
    private ProgressBarOverlay.ProgressHandle progressHandle;
    private boolean silentToolActive;
    private List<BlockPos> currentPath = new ArrayList<BlockPos>();

    public BedAura() {
        super(Category.Combat, "BedAura", "Breaks enemy beds");
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.cancelProgressBar();
        this.bedPos = null;
        this.lastScanTime = 0L;
        this.nextAimTime = 0L;
    }

    @Override
    public void onDisable() {
        this.fullReset();
        this.cancelProgressBar();
        this.switchBackToPreviousTool();
        this.bedPos = null;
        super.onDisable();
    }

    @Override
    public void initPostRunnable() {
        super.initPostRunnable();
        this.hideIf(this.bedSideColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
        this.hideIf(this.bedLineColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
        this.hideIf(this.currentBreakSideColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
        this.hideIf(this.currentBreakLineColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
        this.hideIf(this.pathSideColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
        this.hideIf(this.pathLineColor.getLabel(), () -> (Boolean)this.renderBox.getValue() == false);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        BlockPos targetBlock;
        if (event.getState() == Stage.POST) {
            return;
        }
        if (BedAura.mc.player == null || BedAura.mc.world == null || BedAura.mc.interactionManager == null) {
            return;
        }
        this.updateProgressBar();
        if (((Boolean)this.pauseKillAura.getValue()).booleanValue() && KillAura.target != null) {
            this.fullReset();
            return;
        }
        if (this.currentBlock != null && !this.inRange(this.currentBlock)) {
            this.fullReset();
            this.bedPos = null;
            return;
        }
        if (this.bedPos == null) {
            long now = System.currentTimeMillis();
            if (!this.isBreaking && now - this.lastScanTime >= (long)(((Number)this.rate.getValue()).doubleValue() * 1000.0)) {
                this.lastScanTime = now;
                this.bedPos = this.scanForBed();
                this.currentPath.clear();
            }
            if (this.bedPos == null) {
                this.cancelProgressBar();
                return;
            }
        } else {
            BlockState footState = BedAura.mc.world.getBlockState(this.bedPos[0]);
            if (!(footState.getBlock() instanceof BedBlock)) {
                this.fullReset();
                this.bedPos = null;
                return;
            }
        }
        if (!this.isBreaking && System.currentTimeMillis() < this.nextBreakTime) {
            this.cancelProgressBar();
            return;
        }
        switch (this.breakMode.getMode()) {
            case "OnlyBed": {
                targetBlock = this.bedPos[0];
                break;
            }
            case "Full": {
                targetBlock = this.getFullModeTarget();
                break;
            }
            default: {
                targetBlock = this.getBreakNearBlockTarget();
            }
        }
        if (targetBlock == null) {
            this.fullReset();
            return;
        }
        this.processBreak(targetBlock);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        BlockState state;
        if (!((Boolean)this.renderBox.getValue()).booleanValue() || BedAura.mc.world == null) {
            return;
        }
        if (this.bedPos != null) {
            for (BlockPos pos : this.bedPos) {
                BlockState state2;
                if (pos == null || !((state2 = BedAura.mc.world.getBlockState(pos)).getBlock() instanceof BedBlock)) continue;
                this.renderStateBox(pos, state2, ((OneColor)this.bedSideColor.getValue()).toJavaColor(), ((OneColor)this.bedLineColor.getValue()).toJavaColor());
            }
        }
        for (BlockPos pos : this.currentPath) {
            BlockState state3;
            if (this.currentBlock != null && this.currentBlock.equals((Object)pos) || (state3 = BedAura.mc.world.getBlockState(pos)).isAir()) continue;
            this.renderStateBox(pos, state3, ((OneColor)this.pathSideColor.getValue()).toJavaColor(), ((OneColor)this.pathLineColor.getValue()).toJavaColor());
        }
        if (this.currentBlock != null && !(state = BedAura.mc.world.getBlockState(this.currentBlock)).isAir()) {
            this.renderStateBox(this.currentBlock, state, ((OneColor)this.currentBreakSideColor.getValue()).toJavaColor(), ((OneColor)this.currentBreakLineColor.getValue()).toJavaColor());
        }
    }

    @EventHandler
    private void onHeldItem(UpdateHeldItemEvent event) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue() || !this.silentToolActive || BedAura.mc.player == null) {
            return;
        }
        if (event.getHand() != Hand.MAIN_HAND || this.previousSlot == -1) {
            return;
        }
        ItemStack originalItem = BedAura.mc.player.getInventory().getStack(this.previousSlot);
        if (!originalItem.isEmpty()) {
            event.setItem(originalItem);
        }
    }

    private void processBreak(BlockPos blockPos) {
        if (blockPos == null || BedAura.mc.player == null || BedAura.mc.world == null) {
            this.fullReset();
            return;
        }
        if (!this.inRange(blockPos)) {
            this.fullReset();
            return;
        }
        if (((Number)this.fov.getValue()).doubleValue() < 360.0 && !this.isInFov(blockPos)) {
            return;
        }
        BlockState state = BedAura.mc.world.getBlockState(blockPos);
        if (state.isAir()) {
            this.fullReset();
            return;
        }
        this.currentBlock = blockPos;
        this.prepareBestTool(state);
        long now = System.currentTimeMillis();
        long aimDelayMs = (long)(((Number)this.rate.getValue()).doubleValue() * 1000.0);
        if (!this.isBreaking) {
            if (now < this.nextAimTime) {
                return;
            }
            BlockHitResult startHit = this.getAimedHit(blockPos);
            if (startHit == null) {
                return;
            }
            Rotation rotation = RotationUtils.getRotationBlock(blockPos, 0.0f);
            RotationUtils.setRotation(rotation, 2);
            this.runWithBestTool(() -> BlockBreakUtils.startBreaking(startHit));
            this.isBreaking = true;
            this.breakProgress = 0.0f;
            this.nextAimTime = now + aimDelayMs;
        }
        float progressPerCall = this.getBlockBreakProgress(state);
        this.breakProgress += progressPerCall;
        this.breakProgress = Math.min(1.0f, this.breakProgress);
        BedAura.mc.world.setBlockBreakingInfo(BedAura.mc.player.getId(), blockPos, Math.min(9, Math.max(0, (int)(this.breakProgress * 10.0f) - 1)));
        this.updateProgressBar();
        if (this.breakProgress < 1.0f && !BedAura.mc.world.getBlockState(blockPos).isAir()) {
            return;
        }
        if (now < this.nextAimTime) {
            return;
        }
        BlockHitResult finishHit = this.getAimedHit(blockPos);
        if (finishHit == null) {
            return;
        }
        Rotation rotation = RotationUtils.getRotationBlock(blockPos, 0.0f);
        RotationUtils.setRotation(rotation, 2);
        this.runWithBestTool(() -> BlockBreakUtils.finishBreaking(finishHit));
        this.nextAimTime = now + aimDelayMs;
        this.onBlockFinished(blockPos);
    }

    private void runWithBestTool(Runnable action) {
        if (BedAura.mc.player == null || BedAura.mc.interactionManager == null || !((Boolean)this.autoTool.getValue()).booleanValue() || this.toolSlot == BedAura.mc.player.getInventory().selectedSlot) {
            action.run();
            return;
        }
        int oldSlot = BedAura.mc.player.getInventory().selectedSlot;
        BedAura.mc.player.getInventory().selectedSlot = this.toolSlot;
        BedAura.mc.interactionManager.syncSelectedSlot();
        try {
            action.run();
        }
        finally {
            BedAura.mc.player.getInventory().selectedSlot = oldSlot;
            BedAura.mc.interactionManager.syncSelectedSlot();
        }
    }

    private BlockHitResult getAimedHit(BlockPos blockPos) {
        Rotation rotation = RotationUtils.getRotationBlock(blockPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return rayTraceResult != null && rayTraceResult.getBlockPos().equals((Object)blockPos) ? rayTraceResult : this.createSyntheticHit(blockPos);
    }

    private void onBlockFinished(BlockPos brokenPos) {
        BlockPos brokenBlock = this.previousBlockBroken = brokenPos == null ? null : brokenPos.toImmutable();
        if (BedAura.mc.player != null && BedAura.mc.world != null && brokenPos != null) {
            BedAura.mc.world.setBlockBreakingInfo(BedAura.mc.player.getId(), brokenPos, -1);
        }
        if (this.breakMode.isMode("Full") && !this.currentPath.isEmpty() && this.currentPath.getFirst().equals((Object)brokenPos)) {
            this.currentPath.removeFirst();
        }
        this.cancelProgressBar();
        this.resetState();
        if (!this.breakMode.isMode("Full") || this.currentPath.isEmpty()) {
            this.bedPos = null;
        }
        this.nextBreakTime = System.currentTimeMillis() + 250L;
    }

    private void resetState() {
        this.breakProgress = 0.0f;
        this.currentBlock = null;
        this.isBreaking = false;
        this.toolSlot = BedAura.mc.player != null ? BedAura.mc.player.getInventory().selectedSlot : 0;
        this.silentToolActive = false;
    }

    private void fullReset() {
        if (this.currentBlock != null && BedAura.mc.world != null && BedAura.mc.player != null) {
            BedAura.mc.world.setBlockBreakingInfo(BedAura.mc.player.getId(), this.currentBlock, -1);
        }
        BlockBreakUtils.stopBreaking();
        this.cancelProgressBar();
        this.currentPath.clear();
        this.resetState();
    }

    private void updateProgressBar() {
        if (!this.isBreaking || this.breakProgress <= 0.0f) {
            this.cancelProgressBar();
            return;
        }
        ProgressBarOverlay overlay = Southside.get().getProgressBarOverlay();
        if (overlay == null) {
            return;
        }
        if (this.progressHandle == null || !this.progressHandle.isActive()) {
            this.progressHandle = overlay.show(1000, "BedAura");
        }
        this.progressHandle.update((int)(Math.min(1.0f, this.breakProgress) * 1000.0f));
    }

    private void cancelProgressBar() {
        if (this.progressHandle != null) {
            this.progressHandle.cancel();
            this.progressHandle = null;
        }
    }

    private BlockPos[] scanForBed() {
        if (BedAura.mc.player == null || BedAura.mc.world == null) {
            return null;
        }
        BlockPos playerPos = BedAura.mc.player.getBlockPos();
        int radius = (int)Math.ceil(((Number)this.range.getValue()).doubleValue());
        BlockPos selfBed = (Boolean)this.ignoreSelf.getValue() != false ? BedwarsCheckUtils.INSTANCE.getSelfBed() : null;
        ArrayList<BlockPos> foundBeds = new ArrayList<BlockPos>();
        for (int y = radius; y >= -radius; --y) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockState state;
                    BlockPos pos2 = playerPos.add(x, y, z);
                    if (!this.inRange(pos2) || !((state = BedAura.mc.world.getBlockState(pos2)).getBlock() instanceof BedBlock) || state.get((Property)BedBlock.PART) != BedPart.FOOT || selfBed != null && this.isSameBed(pos2, selfBed, state) || ((Number)this.fov.getValue()).doubleValue() < 360.0 && !this.isInFov(pos2)) continue;
                    foundBeds.add(pos2.toImmutable());
                }
            }
        }
        foundBeds.sort(Comparator.comparingDouble(pos -> BedAura.mc.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)pos))));
        if (foundBeds.isEmpty()) {
            return null;
        }
        BlockPos foot = (BlockPos)foundBeds.getFirst();
        BlockPos head = this.getBedHeadPos(foot, BedAura.mc.world.getBlockState(foot));
        if (head == null) {
            return null;
        }
        return new BlockPos[]{foot, head};
    }

    private boolean isSameBed(BlockPos bedFoot, BlockPos selfBed, BlockState footState) {
        if (bedFoot.equals((Object)selfBed)) {
            return true;
        }
        BlockPos selfHead = this.getBedHeadPos(selfBed, BedAura.mc.world.getBlockState(selfBed));
        BlockPos bedHead = this.getBedHeadPos(bedFoot, footState);
        if (selfHead == null || bedHead == null) {
            return false;
        }
        return bedFoot.equals((Object)selfHead) || bedHead.equals((Object)selfBed) || bedHead.equals((Object)selfHead);
    }

    private BlockPos getBedHeadPos(BlockPos footPos, BlockState footState) {
        if (!(footState.getBlock() instanceof BedBlock)) {
            return null;
        }
        Direction facing = (Direction)footState.get((Property)BedBlock.FACING);
        return footState.get((Property)BedBlock.PART) == BedPart.HEAD ? footPos.toImmutable() : footPos.offset(facing).toImmutable();
    }

    private boolean isCovered(BlockPos pos) {
        if (BedAura.mc.world == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (!BedAura.mc.world.getBlockState(offset).isAir()) continue;
            return false;
        }
        return true;
    }

    private BlockPos getBreakNearBlockTarget() {
        if (this.bedPos == null || BedAura.mc.world == null || BedAura.mc.player == null) {
            return null;
        }
        if (!this.isCovered(this.bedPos[0]) || !this.isCovered(this.bedPos[1])) {
            return this.bedPos[0];
        }
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        HashSet<BlockPos> bedSet = new HashSet<BlockPos>(List.of(this.bedPos));
        for (BlockPos bed : this.bedPos) {
            if (bed == null) continue;
            for (Direction direction : Direction.values()) {
                BlockState state;
                BlockPos candidate;
                if (direction == Direction.DOWN || bedSet.contains(candidate = bed.offset(direction)) || !this.inRange(candidate) || (state = BedAura.mc.world.getBlockState(candidate)).isAir()) continue;
                double score = BedAura.mc.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)candidate));
                if (candidate.equals((Object)this.previousBlockBroken)) {
                    score -= 1.5;
                }
                if (!(score < bestScore)) continue;
                bestScore = score;
                best = candidate.toImmutable();
            }
        }
        return best;
    }

    private BlockPos getFullModeTarget() {
        if (this.bedPos == null || BedAura.mc.world == null || BedAura.mc.player == null) {
            return null;
        }
        if (this.isBedDirectlyBreakable(this.bedPos[0])) {
            this.currentPath.clear();
            return this.bedPos[0];
        }
        if (this.currentPath.isEmpty()) {
            this.currentPath = this.findLegitLikePath(this.bedPos[0]);
        }
        while (!this.currentPath.isEmpty() && BedAura.mc.world.getBlockState(this.currentPath.getFirst()).isAir()) {
            this.currentPath.removeFirst();
        }
        if (this.currentPath.isEmpty()) {
            return this.getBreakNearBlockTarget();
        }
        return this.currentPath.getFirst();
    }

    private boolean isBedDirectlyBreakable(BlockPos bedPos) {
        if (BedAura.mc.player == null || BedAura.mc.world == null) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return hit != null && hit.getBlockPos().equals((Object)bedPos);
    }

    private List<BlockPos> findLegitLikePath(BlockPos bedPos) {
        if (BedAura.mc.player == null || BedAura.mc.world == null) {
            return List.of();
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (hit == null || hit.getBlockPos().equals((Object)bedPos)) {
            return List.of();
        }
        BlockPos startPos = hit.getBlockPos();
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        HashMap<BlockPos, BlockPos> parent = new HashMap<BlockPos, BlockPos>();
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        queue.add(startPos);
        visited.add(startPos);
        parent.put(startPos, null);
        while (!queue.isEmpty()) {
            BlockPos current = (BlockPos)queue.poll();
            if (this.isAdjacentToBed(current, bedPos)) {
                ArrayDeque<BlockPos> reversed = new ArrayDeque<BlockPos>();
                BlockPos cursor = current;
                while (cursor != null) {
                    reversed.push(cursor.toImmutable());
                    cursor = (BlockPos)parent.get(cursor);
                }
                return new ArrayList<BlockPos>(reversed);
            }
            for (Direction direction : Direction.values()) {
                BlockState state;
                BlockPos next;
                if (direction == Direction.DOWN || !visited.add(next = current.offset(direction)) || !this.inRange(next) || (state = BedAura.mc.world.getBlockState(next)).isAir() || state.getBlock() instanceof BedBlock) continue;
                parent.put(next, current);
                queue.add(next);
            }
        }
        return List.of(startPos.toImmutable());
    }

    private boolean isAdjacentToBed(BlockPos pos, BlockPos bedPos) {
        for (Direction direction : Direction.values()) {
            if (!pos.offset(direction).equals((Object)bedPos)) continue;
            return true;
        }
        return false;
    }

    private void prepareBestTool(BlockState state) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue() || BedAura.mc.player == null) {
            this.toolSlot = BedAura.mc.player != null ? BedAura.mc.player.getInventory().selectedSlot : 0;
            this.silentToolActive = false;
            return;
        }
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = BedAura.mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (stack.isSuitableFor(state)) {
                speed += 5.0f;
            }
            if (!(speed > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        if (this.previousSlot == -1) {
            this.previousSlot = BedAura.mc.player.getInventory().selectedSlot;
        }
        if (bestSlot != -1) {
            this.toolSlot = bestSlot;
            this.silentToolActive = bestSlot != this.previousSlot;
        } else {
            this.toolSlot = BedAura.mc.player.getInventory().selectedSlot;
            this.silentToolActive = false;
        }
    }

    private void switchBackToPreviousTool() {
        this.previousSlot = -1;
        this.silentToolActive = false;
    }

    private float getBlockBreakProgress(BlockState state) {
        float hardness;
        float speed;
        ItemStack stack;
        if (BedAura.mc.player == null || BedAura.mc.world == null) {
            return 0.0f;
        }
        if (this.toolSlot >= 0 && this.toolSlot < 9 && !(stack = BedAura.mc.player.getInventory().getStack(this.toolSlot)).isEmpty() && (speed = stack.getMiningSpeedMultiplier(state)) > 1.0f && stack.isSuitableFor(state) && (hardness = state.getHardness((BlockView)BedAura.mc.world, this.currentBlock == null ? BlockPos.ORIGIN : this.currentBlock)) > 0.0f) {
            return speed / hardness / 30.0f;
        }
        return state.calcBlockBreakingDelta((PlayerEntity)BedAura.mc.player, (BlockView)BedAura.mc.world, this.currentBlock == null ? BlockPos.ORIGIN : this.currentBlock);
    }

    private boolean inRange(BlockPos pos) {
        return BedAura.mc.player != null && BedAura.mc.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)pos)) <= ((Number)this.range.getValue()).doubleValue();
    }

    private boolean isInFov(BlockPos pos) {
        if (BedAura.mc.player == null) {
            return false;
        }
        Vec3d eyePos = BedAura.mc.player.getEyePos();
        Vec3d target = Vec3d.ofCenter((Vec3i)pos);
        Vec3d diff = target.subtract(eyePos);
        double targetYaw = Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0;
        double yawDiff = Math.abs(this.wrapDegrees(targetYaw - (double)BedAura.mc.player.getYaw()));
        return yawDiff <= ((Number)this.fov.getValue()).doubleValue() / 2.0;
    }

    private double wrapDegrees(double value) {
        double wrapped = value % 360.0;
        if (wrapped >= 180.0) {
            wrapped -= 360.0;
        }
        if (wrapped < -180.0) {
            wrapped += 360.0;
        }
        return wrapped;
    }

    private BlockHitResult createSyntheticHit(BlockPos pos) {
        if (BedAura.mc.player == null) {
            return null;
        }
        Vec3d eye = BedAura.mc.player.getEyePos();
        Vec3d center = Vec3d.ofCenter((Vec3i)pos);
        Vec3d diff = center.subtract(eye);
        Direction side = Direction.getFacing((double)diff.x, (double)diff.y, (double)diff.z).getOpposite();
        return new BlockHitResult(center, side, pos, false);
    }

    private void renderStateBox(BlockPos pos, BlockState state, Color side, Color line) {
        VoxelShape shape = state.getOutlineShape((BlockView)BedAura.mc.world, pos);
        Box box = shape.isEmpty() ? new Box(pos) : shape.getBoundingBox().offset(pos);
        Render3D.boxSides(box, side);
        Render3D.boxLines(box, line);
    }
}

