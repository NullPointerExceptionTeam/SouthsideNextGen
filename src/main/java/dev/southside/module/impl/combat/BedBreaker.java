package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.player.BlockBreakUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1657;
import net.minecraft.class_1738;
import net.minecraft.class_1767;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2244;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_2769;
import net.minecraft.class_3489;
import net.minecraft.class_3965;
import net.minecraft.class_9282;

public class BedBreaker
extends Module {
    private final List<class_2248> bedBlocks = new ArrayList<class_2248>(Arrays.asList(class_2246.field_10120, class_2246.field_10410, class_2246.field_10230, class_2246.field_10621, class_2246.field_10356, class_2246.field_10180, class_2246.field_10610, class_2246.field_10141, class_2246.field_10326, class_2246.field_10109, class_2246.field_10019, class_2246.field_10527, class_2246.field_10288, class_2246.field_10561, class_2246.field_10069, class_2246.field_10461));
    private static final double RANGE = 4.0;
    private static final double WALL_RANGE = 4.0;
    private final Slider delay = new Slider("Delay", 0, 0, 20, 1);
    private final Dropdown mode = new Dropdown("Mode", "Instant", "Instant", "Legit", "Heypixel");
    private final Dropdown heypixelPath = new Dropdown("Heypixel Path", "Top", "Top", "Forward");
    private final Switch prioritizeOverKillAura = new Switch("Prioritize Over KillAura", false);
    public Switch autoTool = new Switch("Auto Tool", true);
    public Switch checkTeamsBed = new Switch("Check Teams Bed", true);
    private static final Map<class_2248, class_1767> bedColorMap = new HashMap<class_2248, class_1767>(){
        {
            this.put(class_2246.field_10120, class_1767.field_7952);
            this.put(class_2246.field_10410, class_1767.field_7946);
            this.put(class_2246.field_10230, class_1767.field_7958);
            this.put(class_2246.field_10621, class_1767.field_7951);
            this.put(class_2246.field_10356, class_1767.field_7947);
            this.put(class_2246.field_10180, class_1767.field_7961);
            this.put(class_2246.field_10610, class_1767.field_7954);
            this.put(class_2246.field_10141, class_1767.field_7944);
            this.put(class_2246.field_10326, class_1767.field_7967);
            this.put(class_2246.field_10109, class_1767.field_7955);
            this.put(class_2246.field_10019, class_1767.field_7945);
            this.put(class_2246.field_10527, class_1767.field_7966);
            this.put(class_2246.field_10288, class_1767.field_7957);
            this.put(class_2246.field_10561, class_1767.field_7942);
            this.put(class_2246.field_10069, class_1767.field_7964);
            this.put(class_2246.field_10461, class_1767.field_7963);
        }
    };
    private DestroyerTarget currentTarget;
    private DestroyerTarget oldTarget;
    private int delayTicksRemaining;
    private int previousSlot = -1;

    public BedBreaker() {
        super(Category.Combat, "BedBreaker", "Breaks beds");
    }

    private boolean isInstantMode() {
        return this.mode.isMode("Instant");
    }

    private boolean isLegitMode() {
        return this.mode.isMode("Legit");
    }

    private boolean isHeypixelMode() {
        return this.mode.isMode("Heypixel");
    }

    @EventHandler
    private void onUpdate(TickEvent event) {
        if (event.getState() == Stage.POST) {
            return;
        }
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null || BedBreaker.mc.field_1761 == null) {
            return;
        }
        ClientRayTraceUtil.updateEyePos();
        this.oldTarget = this.currentTarget;
        this.updateCurrentTarget();
        if (this.oldTarget != null && this.currentTarget == null) {
            BlockBreakUtils.stopBreaking();
            this.switchBackToPreviousTool();
            return;
        }
        if (this.oldTarget != this.currentTarget && ((Number)this.delay.getValue()).intValue() > 0) {
            BlockBreakUtils.stopBreaking();
            this.delayTicksRemaining = ((Number)this.delay.getValue()).intValue();
        }
        if (this.delayTicksRemaining > 0) {
            --this.delayTicksRemaining;
            return;
        }
        if (this.currentTarget == null) {
            BlockBreakUtils.stopBreaking();
            this.switchBackToPreviousTool();
            return;
        }
        class_2680 state = BedBreaker.mc.field_1687.method_8320(this.currentTarget.pos);
        if (state.method_26215()) {
            return;
        }
        Rotation rotation = RotationUtils.getRotationBlock(this.currentTarget.pos, 0.0f);
        RotationUtils.setRotation(rotation, (Boolean)this.prioritizeOverKillAura.getValue() != false ? 2 : 1);
        class_3965 rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (rayTraceResult == null || rayTraceResult.method_17783() != class_239.class_240.field_1332) {
            class_3965 synthetic;
            if ((this.isInstantMode() || this.isHeypixelMode()) && (synthetic = this.createSyntheticHit(this.currentTarget.pos)) != null) {
                this.switchToBestTool(this.currentTarget.pos);
                BlockBreakUtils.doBreak(synthetic, false, true);
            }
            return;
        }
        if (!rayTraceResult.method_17777().equals((Object)this.currentTarget.pos) || !ClientRayTraceUtil.didHitBlockFace((class_1657)BedBreaker.mc.field_1724, rotation.yaw, rotation.pitch, this.currentTarget.pos, rayTraceResult.method_17780(), false)) {
            class_3965 synthetic;
            if ((this.isInstantMode() || this.isHeypixelMode()) && (synthetic = this.createSyntheticHit(this.currentTarget.pos)) != null) {
                this.switchToBestTool(this.currentTarget.pos);
                BlockBreakUtils.doBreak(synthetic, false, true);
            }
            return;
        }
        this.switchToBestTool(this.currentTarget.pos);
        BlockBreakUtils.doBreak(rayTraceResult, false, true);
    }

    private void updateCurrentTarget() {
        List<class_2338> possibleBeds = this.searchPossibleTargetPositions();
        this.validateCurrentTarget(possibleBeds);
        if (possibleBeds.isEmpty()) {
            return;
        }
        double rangeValue = 4.0;
        double wallRangeValue = Math.min(rangeValue, 4.0);
        boolean foundDirect = false;
        if (this.isInstantMode()) {
            for (class_2338 class_23382 : possibleBeds) {
                if (!Boolean.TRUE.equals(this.considerAsTarget(new DestroyerTarget(class_23382, true), rangeValue, wallRangeValue, false, false))) continue;
                foundDirect = true;
                break;
            }
        } else {
            for (class_2338 class_23383 : possibleBeds) {
                if (!this.isBedVisible(class_23383) || !Boolean.TRUE.equals(this.considerAsTarget(new DestroyerTarget(class_23383, true), rangeValue, wallRangeValue, false, true))) continue;
                foundDirect = true;
                break;
            }
        }
        if (foundDirect || this.currentTarget != null) {
            return;
        }
        if (this.isInstantMode()) {
            if (!possibleBeds.isEmpty()) {
                this.considerAsTarget(new DestroyerTarget(possibleBeds.getFirst(), true), rangeValue, rangeValue, false, false);
            }
            return;
        }
        if (this.isHeypixelMode()) {
            boolean foundCoverTarget = false;
            for (class_2338 pos : possibleBeds) {
                class_2338 coverTarget = this.selectHeypixelCoverTarget(pos);
                if (coverTarget == null) continue;
                foundCoverTarget = true;
                this.considerAsTarget(new DestroyerTarget(coverTarget, false), rangeValue, rangeValue, false, false);
                if (this.currentTarget == null) continue;
                return;
            }
            if (!foundCoverTarget) {
                this.considerAsTarget(new DestroyerTarget(possibleBeds.getFirst(), true), rangeValue, rangeValue, false, false);
            }
            return;
        }
        if (this.isLegitMode()) {
            for (class_2338 class_23384 : possibleBeds) {
                this.updateSurroundings(class_23384, rangeValue, wallRangeValue);
            }
        }
    }

    private boolean isBedVisible(class_2338 bedPos) {
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        class_3965 rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return rayTraceResult != null && rayTraceResult.method_17783() == class_239.class_240.field_1332 && rayTraceResult.method_17777().equals((Object)bedPos) && ClientRayTraceUtil.didHitBlockFace((class_1657)BedBreaker.mc.field_1724, rotation.yaw, rotation.pitch, bedPos, rayTraceResult.method_17780(), false);
    }

    private class_3965 createSyntheticHit(class_2338 pos) {
        if (BedBreaker.mc.field_1724 == null) {
            return null;
        }
        class_243 eye = BedBreaker.mc.field_1724.method_33571();
        class_243 center = class_243.method_24953((class_2382)pos);
        class_243 diff = center.method_1020(eye);
        class_2350 face = class_2350.method_10142((double)diff.field_1352, (double)diff.field_1351, (double)diff.field_1350).method_10153();
        return new class_3965(center, face, pos, false);
    }

    private void validateCurrentTarget(List<class_2338> possibleBeds) {
        if (this.currentTarget == null) {
            return;
        }
        if (this.currentTarget.isTarget && !possibleBeds.contains(this.currentTarget.pos)) {
            this.clearCurrentTarget();
            return;
        }
        if (this.isHeypixelMode() && !this.currentTarget.isTarget && !this.matchesCurrentHeypixelCoverTarget(possibleBeds, this.currentTarget.pos)) {
            this.clearCurrentTarget();
            return;
        }
        Boolean result = this.considerAsTarget(this.currentTarget, 4.0, 4.0, true, this.isLegitMode());
        if (result == Boolean.FALSE) {
            this.clearCurrentTarget();
        }
    }

    private boolean matchesCurrentHeypixelCoverTarget(List<class_2338> possibleBeds, class_2338 targetPos) {
        if (targetPos == null) {
            return false;
        }
        for (class_2338 bedPos : possibleBeds) {
            class_2338 cover = this.selectHeypixelCoverTarget(bedPos);
            if (cover == null || !cover.equals((Object)targetPos)) continue;
            return true;
        }
        return false;
    }

    private void clearCurrentTarget() {
        BlockBreakUtils.stopBreaking();
        this.switchBackToPreviousTool();
        this.currentTarget = null;
    }

    private List<class_2338> searchPossibleTargetPositions() {
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return Collections.emptyList();
        }
        double rangeValue = 4.0;
        int radius = (int)Math.ceil(rangeValue);
        class_243 eyePos = BedBreaker.mc.field_1724.method_33571();
        ArrayList<class_2338> possibleBeds = new ArrayList<class_2338>();
        HashSet<Long> seenHeads = new HashSet<Long>();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    class_2338 headPos;
                    class_2338 blockPos = class_2338.method_49637((double)(BedBreaker.mc.field_1724.method_23317() + (double)x), (double)(BedBreaker.mc.field_1724.method_23318() + (double)y), (double)(BedBreaker.mc.field_1724.method_23321() + (double)z));
                    class_2680 state = BedBreaker.mc.field_1687.method_8320(blockPos);
                    if (!(state.method_26204() instanceof class_2244) || this.checkTeamBed(state.method_26204()) || (headPos = this.getBedHeadPos(blockPos, state)) == null || !seenHeads.add(headPos.method_10063()) || (distance = eyePos.method_1022(class_243.method_24953((class_2382)headPos))) > rangeValue) continue;
                    possibleBeds.add(headPos.method_10062());
                }
            }
        }
        possibleBeds.sort(Comparator.comparingDouble(pos -> eyePos.method_1022(class_243.method_24953((class_2382)pos))));
        return possibleBeds;
    }

    private Boolean considerAsTarget(DestroyerTarget target, double rangeValue, double wallRangeValue, boolean isCurrentTarget, boolean requireVisible) {
        boolean canSee;
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return false;
        }
        class_2680 state = BedBreaker.mc.field_1687.method_8320(target.pos);
        if (state.method_26215() || state.method_26214((class_1922)BedBreaker.mc.field_1687, target.pos) < 0.0f) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(target.pos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        class_3965 rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        boolean bl = canSee = rayTraceResult != null && rayTraceResult.method_17783() == class_239.class_240.field_1332 && rayTraceResult.method_17777().equals((Object)target.pos) && ClientRayTraceUtil.didHitBlockFace((class_1657)BedBreaker.mc.field_1724, rotation.yaw, rotation.pitch, target.pos, rayTraceResult.method_17780(), false);
        if (requireVisible && !canSee) {
            return false;
        }
        double distance = BedBreaker.mc.field_1724.method_33571().method_1022(class_243.method_24953((class_2382)target.pos));
        if (!canSee && distance > wallRangeValue) {
            return false;
        }
        if (!isCurrentTarget && this.currentTarget != null && target.compareTo(this.currentTarget) >= 0) {
            return null;
        }
        RotationUtils.setRotation(rotation, (Boolean)this.prioritizeOverKillAura.getValue() != false ? 2 : 1);
        this.currentTarget = target;
        return true;
    }

    private void updateSurroundings(class_2338 targetPos, double rangeValue, double wallRangeValue) {
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return;
        }
        class_243 eyePos = BedBreaker.mc.field_1724.method_33571();
        class_243 targetPoint = new class_243((double)targetPos.method_10263() + 0.5, (double)targetPos.method_10264() + 0.5, (double)targetPos.method_10260() + 0.5);
        Rotation rotation = RotationUtils.getRotationByVector(eyePos, targetPoint, false);
        ClientRayTraceUtil.updateEyePos();
        class_3965 hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (hit == null || hit.method_17783() != class_239.class_240.field_1332) {
            return;
        }
        class_2338 startPos = hit.method_17777();
        if (startPos.equals((Object)targetPos)) {
            return;
        }
        List<class_2338> path = this.traceWayToTarget(targetPos, startPos);
        double resistance = 0.0;
        for (class_2338 pos : path) {
            class_2680 state = BedBreaker.mc.field_1687.method_8320(pos);
            if (state.method_26215()) continue;
            resistance += this.miningDuration(pos);
        }
        this.considerAsTarget(new DestroyerTarget(startPos, false, resistance), rangeValue, wallRangeValue, false, false);
    }

    private List<class_2338> traceWayToTarget(class_2338 target, class_2338 startPos) {
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return Collections.emptyList();
        }
        class_243 eyePos = BedBreaker.mc.field_1724.method_33571();
        class_243 targetPoint = new class_243((double)target.method_10263() + 0.5, (double)target.method_10264() + 0.5, (double)target.method_10260() + 0.5);
        ArrayList<class_2338> path = new ArrayList<class_2338>();
        ArrayList<class_2338> queue = new ArrayList<class_2338>();
        queue.add(startPos);
        while (!queue.isEmpty()) {
            class_2338 current = (class_2338)queue.removeFirst();
            if (current.equals((Object)target) || path.contains(current)) continue;
            class_2680 state = BedBreaker.mc.field_1687.method_8320(current);
            if (!state.method_26215() && !this.bedBlocks.contains(state.method_26204())) {
                path.add(current.method_10062());
            }
            for (class_2350 direction : class_2350.values()) {
                class_2338 neighbor = current.method_10093(direction);
                if (neighbor.equals((Object)target) || path.contains(neighbor) || queue.contains(neighbor) || !this.isBlockInLineOfSight(neighbor, eyePos, targetPoint)) continue;
                queue.add(neighbor);
            }
        }
        return path;
    }

    private boolean isBlockInLineOfSight(class_2338 blockPos, class_243 start, class_243 end) {
        class_2680 state = BedBreaker.mc.field_1687.method_8320(blockPos);
        if (state.method_26215()) {
            return false;
        }
        class_265 shape = state.method_26220((class_1922)BedBreaker.mc.field_1687, blockPos);
        if (shape.method_1110()) {
            return false;
        }
        class_238 box = shape.method_1107().method_996(blockPos);
        return box.method_992(start, end).isPresent();
    }

    private double miningDuration(class_2338 pos) {
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return Double.MAX_VALUE;
        }
        class_2680 state = BedBreaker.mc.field_1687.method_8320(pos);
        if (state.method_26215()) {
            return Double.MAX_VALUE;
        }
        float hardness = state.method_26214((class_1922)BedBreaker.mc.field_1687, pos);
        if (hardness < 0.0f) {
            return Double.MAX_VALUE;
        }
        float bestMiningSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float speed;
            class_1799 stack = BedBreaker.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !((speed = stack.method_7924(state)) > bestMiningSpeed)) continue;
            bestMiningSpeed = speed;
        }
        return hardness / bestMiningSpeed;
    }

    private class_2338 getBedHeadPos(class_2338 bedPos, class_2680 state) {
        if (!(state.method_26204() instanceof class_2244)) {
            return null;
        }
        class_2350 facing = (class_2350)state.method_11654((class_2769)class_2244.field_11177);
        class_2742 part = (class_2742)state.method_11654((class_2769)class_2244.field_9967);
        return part == class_2742.field_12560 ? bedPos.method_10062() : bedPos.method_10093(facing).method_10062();
    }

    private BedHeadInfo getBedHeadInfo(class_2338 bedPos, class_2680 state) {
        if (!(state.method_26204() instanceof class_2244)) {
            return null;
        }
        class_2350 facing = (class_2350)state.method_11654((class_2769)class_2244.field_11177);
        class_2338 head = this.getBedHeadPos(bedPos, state);
        if (head == null) {
            return null;
        }
        return new BedHeadInfo(head, facing);
    }

    private class_2338 selectHeypixelCoverTarget(class_2338 bedPos) {
        if (BedBreaker.mc.field_1687 == null || BedBreaker.mc.field_1724 == null) {
            return null;
        }
        class_2680 state = BedBreaker.mc.field_1687.method_8320(bedPos);
        BedHeadInfo info = this.getBedHeadInfo(bedPos, state);
        if (info == null) {
            return null;
        }
        class_2338 coverPos = (Integer)this.heypixelPath.getValue() == 1 ? info.head.method_10093(info.facing) : info.head.method_10084();
        return this.isBreakable(coverPos) ? coverPos.method_10062() : null;
    }

    private boolean isBreakable(class_2338 pos) {
        if (BedBreaker.mc.field_1687 == null) {
            return false;
        }
        class_2680 state = BedBreaker.mc.field_1687.method_8320(pos);
        return !state.method_26215() && state.method_26214((class_1922)BedBreaker.mc.field_1687, pos) >= 0.0f;
    }

    private class_1767 getArmorColor(class_1799 itemStack) {
        if (itemStack.method_31573(class_3489.field_48803) && itemStack.method_7909() instanceof class_1738) {
            int i = class_9282.method_57470((class_1799)itemStack, (int)0);
            for (class_1767 dyeColor : class_1767.values()) {
                if (i != dyeColor.method_7787()) continue;
                return dyeColor;
            }
        }
        return null;
    }

    private boolean checkTeamBed(class_2248 block) {
        if (!this.bedBlocks.contains(block)) {
            return true;
        }
        class_1767 chestplateColor = this.getArmorColor(BedBreaker.mc.field_1724.method_31548().method_7372(0));
        if (chestplateColor == null) {
            return false;
        }
        class_1767 color = bedColorMap.get(block);
        if (color == null) {
            return false;
        }
        if (!((Boolean)this.checkTeamsBed.getValue()).booleanValue()) {
            return false;
        }
        return color.equals((Object)chestplateColor);
    }

    private void switchToBestTool(class_2338 pos) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue()) {
            return;
        }
        if (BedBreaker.mc.field_1724 == null || BedBreaker.mc.field_1687 == null) {
            return;
        }
        class_2680 blockState = BedBreaker.mc.field_1687.method_8320(pos);
        if (blockState.method_26215()) {
            return;
        }
        int bestSlot = this.findBestToolSlot(blockState);
        if (bestSlot != -1 && bestSlot != BedBreaker.mc.field_1724.method_31548().field_7545) {
            this.previousSlot = BedBreaker.mc.field_1724.method_31548().field_7545;
            BedBreaker.mc.field_1724.method_31548().field_7545 = bestSlot;
        }
    }

    private void switchBackToPreviousTool() {
        if (this.previousSlot != -1 && BedBreaker.mc.field_1724 != null) {
            BedBreaker.mc.field_1724.method_31548().field_7545 = this.previousSlot;
            this.previousSlot = -1;
        }
    }

    private int findBestToolSlot(class_2680 blockState) {
        if (BedBreaker.mc.field_1724 == null) {
            return -1;
        }
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float speed;
            class_1799 stack = BedBreaker.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !((speed = stack.method_7924(blockState)) > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        return bestSlot;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (this.currentTarget != null && BedBreaker.mc.field_1687 != null && BedBreaker.mc.field_1724 != null) {
            Render3D.boxLines(new class_238(this.currentTarget.pos), new Color(0, 255, 0, 255));
        }
    }

    @Override
    public void onDisable() {
        BlockBreakUtils.stopBreaking();
        this.switchBackToPreviousTool();
        super.onDisable();
    }

    private static class DestroyerTarget
    implements Comparable<DestroyerTarget> {
        final class_2338 pos;
        final boolean isTarget;
        final double resistance;

        DestroyerTarget(class_2338 pos, boolean isTarget) {
            this(pos, isTarget, 0.0);
        }

        DestroyerTarget(class_2338 pos, boolean isTarget, double resistance) {
            this.pos = pos;
            this.isTarget = isTarget;
            this.resistance = resistance;
        }

        @Override
        public int compareTo(DestroyerTarget other) {
            if (this.isTarget && !other.isTarget) {
                return -1;
            }
            if (!this.isTarget && other.isTarget) {
                return 1;
            }
            return Double.compare(this.resistance, other.resistance);
        }
    }

    private static class BedHeadInfo {
        final class_2338 head;
        final class_2350 facing;

        BedHeadInfo(class_2338 head, class_2350 facing) {
            this.head = head;
            this.facing = facing;
        }
    }
}

