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
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_2769;
import net.minecraft.class_3965;

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
    private class_2338[] bedPos;
    private class_2338 currentBlock;
    private class_2338 previousBlockBroken;
    private float breakProgress;
    private boolean isBreaking;
    private long nextBreakTime;
    private long lastScanTime;
    private long nextAimTime;
    private int toolSlot;
    private int previousSlot = -1;
    private ProgressBarOverlay.ProgressHandle progressHandle;
    private boolean silentToolActive;
    private List<class_2338> currentPath = new ArrayList<class_2338>();

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
        class_2338 targetBlock;
        if (event.getState() == Stage.POST) {
            return;
        }
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null || BedAura.mc.field_1761 == null) {
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
            class_2680 footState = BedAura.mc.field_1687.method_8320(this.bedPos[0]);
            if (!(footState.method_26204() instanceof class_2244)) {
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
        class_2680 state;
        if (!((Boolean)this.renderBox.getValue()).booleanValue() || BedAura.mc.field_1687 == null) {
            return;
        }
        if (this.bedPos != null) {
            for (class_2338 pos : this.bedPos) {
                class_2680 state2;
                if (pos == null || !((state2 = BedAura.mc.field_1687.method_8320(pos)).method_26204() instanceof class_2244)) continue;
                this.renderStateBox(pos, state2, ((OneColor)this.bedSideColor.getValue()).toJavaColor(), ((OneColor)this.bedLineColor.getValue()).toJavaColor());
            }
        }
        for (class_2338 pos : this.currentPath) {
            class_2680 state3;
            if (this.currentBlock != null && this.currentBlock.equals((Object)pos) || (state3 = BedAura.mc.field_1687.method_8320(pos)).method_26215()) continue;
            this.renderStateBox(pos, state3, ((OneColor)this.pathSideColor.getValue()).toJavaColor(), ((OneColor)this.pathLineColor.getValue()).toJavaColor());
        }
        if (this.currentBlock != null && !(state = BedAura.mc.field_1687.method_8320(this.currentBlock)).method_26215()) {
            this.renderStateBox(this.currentBlock, state, ((OneColor)this.currentBreakSideColor.getValue()).toJavaColor(), ((OneColor)this.currentBreakLineColor.getValue()).toJavaColor());
        }
    }

    @EventHandler
    private void onHeldItem(UpdateHeldItemEvent event) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue() || !this.silentToolActive || BedAura.mc.field_1724 == null) {
            return;
        }
        if (event.getHand() != class_1268.field_5808 || this.previousSlot == -1) {
            return;
        }
        class_1799 originalItem = BedAura.mc.field_1724.method_31548().method_5438(this.previousSlot);
        if (!originalItem.method_7960()) {
            event.setItem(originalItem);
        }
    }

    private void processBreak(class_2338 blockPos) {
        if (blockPos == null || BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null) {
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
        class_2680 state = BedAura.mc.field_1687.method_8320(blockPos);
        if (state.method_26215()) {
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
            class_3965 startHit = this.getAimedHit(blockPos);
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
        BedAura.mc.field_1687.method_8517(BedAura.mc.field_1724.method_5628(), blockPos, Math.min(9, Math.max(0, (int)(this.breakProgress * 10.0f) - 1)));
        this.updateProgressBar();
        if (this.breakProgress < 1.0f && !BedAura.mc.field_1687.method_8320(blockPos).method_26215()) {
            return;
        }
        if (now < this.nextAimTime) {
            return;
        }
        class_3965 finishHit = this.getAimedHit(blockPos);
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
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1761 == null || !((Boolean)this.autoTool.getValue()).booleanValue() || this.toolSlot == BedAura.mc.field_1724.method_31548().field_7545) {
            action.run();
            return;
        }
        int oldSlot = BedAura.mc.field_1724.method_31548().field_7545;
        BedAura.mc.field_1724.method_31548().field_7545 = this.toolSlot;
        BedAura.mc.field_1761.method_2911();
        try {
            action.run();
        }
        finally {
            BedAura.mc.field_1724.method_31548().field_7545 = oldSlot;
            BedAura.mc.field_1761.method_2911();
        }
    }

    private class_3965 getAimedHit(class_2338 blockPos) {
        Rotation rotation = RotationUtils.getRotationBlock(blockPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        class_3965 rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return rayTraceResult != null && rayTraceResult.method_17777().equals((Object)blockPos) ? rayTraceResult : this.createSyntheticHit(blockPos);
    }

    private void onBlockFinished(class_2338 brokenPos) {
        class_2338 class_23382 = this.previousBlockBroken = brokenPos == null ? null : brokenPos.method_10062();
        if (BedAura.mc.field_1724 != null && BedAura.mc.field_1687 != null && brokenPos != null) {
            BedAura.mc.field_1687.method_8517(BedAura.mc.field_1724.method_5628(), brokenPos, -1);
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
        this.toolSlot = BedAura.mc.field_1724 != null ? BedAura.mc.field_1724.method_31548().field_7545 : 0;
        this.silentToolActive = false;
    }

    private void fullReset() {
        if (this.currentBlock != null && BedAura.mc.field_1687 != null && BedAura.mc.field_1724 != null) {
            BedAura.mc.field_1687.method_8517(BedAura.mc.field_1724.method_5628(), this.currentBlock, -1);
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

    private class_2338[] scanForBed() {
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null) {
            return null;
        }
        class_2338 playerPos = BedAura.mc.field_1724.method_24515();
        int radius = (int)Math.ceil(((Number)this.range.getValue()).doubleValue());
        class_2338 selfBed = (Boolean)this.ignoreSelf.getValue() != false ? BedwarsCheckUtils.INSTANCE.getSelfBed() : null;
        ArrayList<class_2338> foundBeds = new ArrayList<class_2338>();
        for (int y = radius; y >= -radius; --y) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    class_2680 state;
                    class_2338 pos2 = playerPos.method_10069(x, y, z);
                    if (!this.inRange(pos2) || !((state = BedAura.mc.field_1687.method_8320(pos2)).method_26204() instanceof class_2244) || state.method_11654((class_2769)class_2244.field_9967) != class_2742.field_12557 || selfBed != null && this.isSameBed(pos2, selfBed, state) || ((Number)this.fov.getValue()).doubleValue() < 360.0 && !this.isInFov(pos2)) continue;
                    foundBeds.add(pos2.method_10062());
                }
            }
        }
        foundBeds.sort(Comparator.comparingDouble(pos -> BedAura.mc.field_1724.method_33571().method_1022(class_243.method_24953((class_2382)pos))));
        if (foundBeds.isEmpty()) {
            return null;
        }
        class_2338 foot = (class_2338)foundBeds.getFirst();
        class_2338 head = this.getBedHeadPos(foot, BedAura.mc.field_1687.method_8320(foot));
        if (head == null) {
            return null;
        }
        return new class_2338[]{foot, head};
    }

    private boolean isSameBed(class_2338 bedFoot, class_2338 selfBed, class_2680 footState) {
        if (bedFoot.equals((Object)selfBed)) {
            return true;
        }
        class_2338 selfHead = this.getBedHeadPos(selfBed, BedAura.mc.field_1687.method_8320(selfBed));
        class_2338 bedHead = this.getBedHeadPos(bedFoot, footState);
        if (selfHead == null || bedHead == null) {
            return false;
        }
        return bedFoot.equals((Object)selfHead) || bedHead.equals((Object)selfBed) || bedHead.equals((Object)selfHead);
    }

    private class_2338 getBedHeadPos(class_2338 footPos, class_2680 footState) {
        if (!(footState.method_26204() instanceof class_2244)) {
            return null;
        }
        class_2350 facing = (class_2350)footState.method_11654((class_2769)class_2244.field_11177);
        return footState.method_11654((class_2769)class_2244.field_9967) == class_2742.field_12560 ? footPos.method_10062() : footPos.method_10093(facing).method_10062();
    }

    private boolean isCovered(class_2338 pos) {
        if (BedAura.mc.field_1687 == null) {
            return false;
        }
        for (class_2350 direction : class_2350.values()) {
            class_2338 offset = pos.method_10093(direction);
            if (!BedAura.mc.field_1687.method_8320(offset).method_26215()) continue;
            return false;
        }
        return true;
    }

    private class_2338 getBreakNearBlockTarget() {
        if (this.bedPos == null || BedAura.mc.field_1687 == null || BedAura.mc.field_1724 == null) {
            return null;
        }
        if (!this.isCovered(this.bedPos[0]) || !this.isCovered(this.bedPos[1])) {
            return this.bedPos[0];
        }
        class_2338 best = null;
        double bestScore = Double.MAX_VALUE;
        HashSet<class_2338> bedSet = new HashSet<class_2338>(List.of(this.bedPos));
        for (class_2338 bed : this.bedPos) {
            if (bed == null) continue;
            for (class_2350 direction : class_2350.values()) {
                class_2680 state;
                class_2338 candidate;
                if (direction == class_2350.field_11033 || bedSet.contains(candidate = bed.method_10093(direction)) || !this.inRange(candidate) || (state = BedAura.mc.field_1687.method_8320(candidate)).method_26215()) continue;
                double score = BedAura.mc.field_1724.method_33571().method_1022(class_243.method_24953((class_2382)candidate));
                if (candidate.equals((Object)this.previousBlockBroken)) {
                    score -= 1.5;
                }
                if (!(score < bestScore)) continue;
                bestScore = score;
                best = candidate.method_10062();
            }
        }
        return best;
    }

    private class_2338 getFullModeTarget() {
        if (this.bedPos == null || BedAura.mc.field_1687 == null || BedAura.mc.field_1724 == null) {
            return null;
        }
        if (this.isBedDirectlyBreakable(this.bedPos[0])) {
            this.currentPath.clear();
            return this.bedPos[0];
        }
        if (this.currentPath.isEmpty()) {
            this.currentPath = this.findLegitLikePath(this.bedPos[0]);
        }
        while (!this.currentPath.isEmpty() && BedAura.mc.field_1687.method_8320(this.currentPath.getFirst()).method_26215()) {
            this.currentPath.removeFirst();
        }
        if (this.currentPath.isEmpty()) {
            return this.getBreakNearBlockTarget();
        }
        return this.currentPath.getFirst();
    }

    private boolean isBedDirectlyBreakable(class_2338 bedPos) {
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return hit != null && hit.method_17777().equals((Object)bedPos);
    }

    private List<class_2338> findLegitLikePath(class_2338 bedPos) {
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null) {
            return List.of();
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (hit == null || hit.method_17777().equals((Object)bedPos)) {
            return List.of();
        }
        class_2338 startPos = hit.method_17777();
        ArrayDeque<class_2338> queue = new ArrayDeque<class_2338>();
        HashMap<class_2338, class_2338> parent = new HashMap<class_2338, class_2338>();
        HashSet<class_2338> visited = new HashSet<class_2338>();
        queue.add(startPos);
        visited.add(startPos);
        parent.put(startPos, null);
        while (!queue.isEmpty()) {
            class_2338 current = (class_2338)queue.poll();
            if (this.isAdjacentToBed(current, bedPos)) {
                ArrayDeque<class_2338> reversed = new ArrayDeque<class_2338>();
                class_2338 cursor = current;
                while (cursor != null) {
                    reversed.push(cursor.method_10062());
                    cursor = (class_2338)parent.get(cursor);
                }
                return new ArrayList<class_2338>(reversed);
            }
            for (class_2350 direction : class_2350.values()) {
                class_2680 state;
                class_2338 next;
                if (direction == class_2350.field_11033 || !visited.add(next = current.method_10093(direction)) || !this.inRange(next) || (state = BedAura.mc.field_1687.method_8320(next)).method_26215() || state.method_26204() instanceof class_2244) continue;
                parent.put(next, current);
                queue.add(next);
            }
        }
        return List.of(startPos.method_10062());
    }

    private boolean isAdjacentToBed(class_2338 pos, class_2338 bedPos) {
        for (class_2350 direction : class_2350.values()) {
            if (!pos.method_10093(direction).equals((Object)bedPos)) continue;
            return true;
        }
        return false;
    }

    private void prepareBestTool(class_2680 state) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue() || BedAura.mc.field_1724 == null) {
            this.toolSlot = BedAura.mc.field_1724 != null ? BedAura.mc.field_1724.method_31548().field_7545 : 0;
            this.silentToolActive = false;
            return;
        }
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = BedAura.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960()) continue;
            float speed = stack.method_7924(state);
            if (stack.method_7951(state)) {
                speed += 5.0f;
            }
            if (!(speed > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        if (this.previousSlot == -1) {
            this.previousSlot = BedAura.mc.field_1724.method_31548().field_7545;
        }
        if (bestSlot != -1) {
            this.toolSlot = bestSlot;
            this.silentToolActive = bestSlot != this.previousSlot;
        } else {
            this.toolSlot = BedAura.mc.field_1724.method_31548().field_7545;
            this.silentToolActive = false;
        }
    }

    private void switchBackToPreviousTool() {
        this.previousSlot = -1;
        this.silentToolActive = false;
    }

    private float getBlockBreakProgress(class_2680 state) {
        float hardness;
        float speed;
        class_1799 stack;
        if (BedAura.mc.field_1724 == null || BedAura.mc.field_1687 == null) {
            return 0.0f;
        }
        if (this.toolSlot >= 0 && this.toolSlot < 9 && !(stack = BedAura.mc.field_1724.method_31548().method_5438(this.toolSlot)).method_7960() && (speed = stack.method_7924(state)) > 1.0f && stack.method_7951(state) && (hardness = state.method_26214((class_1922)BedAura.mc.field_1687, this.currentBlock == null ? class_2338.field_10980 : this.currentBlock)) > 0.0f) {
            return speed / hardness / 30.0f;
        }
        return state.method_26165((class_1657)BedAura.mc.field_1724, (class_1922)BedAura.mc.field_1687, this.currentBlock == null ? class_2338.field_10980 : this.currentBlock);
    }

    private boolean inRange(class_2338 pos) {
        return BedAura.mc.field_1724 != null && BedAura.mc.field_1724.method_33571().method_1022(class_243.method_24953((class_2382)pos)) <= ((Number)this.range.getValue()).doubleValue();
    }

    private boolean isInFov(class_2338 pos) {
        if (BedAura.mc.field_1724 == null) {
            return false;
        }
        class_243 eyePos = BedAura.mc.field_1724.method_33571();
        class_243 target = class_243.method_24953((class_2382)pos);
        class_243 diff = target.method_1020(eyePos);
        double targetYaw = Math.toDegrees(Math.atan2(diff.field_1350, diff.field_1352)) - 90.0;
        double yawDiff = Math.abs(this.wrapDegrees(targetYaw - (double)BedAura.mc.field_1724.method_36454()));
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

    private class_3965 createSyntheticHit(class_2338 pos) {
        if (BedAura.mc.field_1724 == null) {
            return null;
        }
        class_243 eye = BedAura.mc.field_1724.method_33571();
        class_243 center = class_243.method_24953((class_2382)pos);
        class_243 diff = center.method_1020(eye);
        class_2350 side = class_2350.method_10142((double)diff.field_1352, (double)diff.field_1351, (double)diff.field_1350).method_10153();
        return new class_3965(center, side, pos, false);
    }

    private void renderStateBox(class_2338 pos, class_2680 state, Color side, Color line) {
        class_265 shape = state.method_26218((class_1922)BedAura.mc.field_1687, pos);
        class_238 box = shape.method_1110() ? new class_238(pos) : shape.method_1107().method_996(pos);
        Render3D.boxSides(box, side);
        Render3D.boxLines(box, line);
    }
}

