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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.state.property.Property;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.component.type.DyedColorComponent;

public class BedBreaker
extends Module {
    private final List<Block> bedBlocks = new ArrayList<Block>(Arrays.asList(Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED));
    private static final double RANGE = 4.0;
    private static final double WALL_RANGE = 4.0;
    private final Slider delay = new Slider("Delay", 0, 0, 20, 1);
    private final Dropdown mode = new Dropdown("Mode", "Instant", "Instant", "Legit", "Heypixel");
    private final Dropdown heypixelPath = new Dropdown("Heypixel Path", "Top", "Top", "Forward");
    private final Switch prioritizeOverKillAura = new Switch("Prioritize Over KillAura", false);
    public Switch autoTool = new Switch("Auto Tool", true);
    public Switch checkTeamsBed = new Switch("Check Teams Bed", true);
    private static final Map<Block, DyeColor> bedColorMap = new HashMap<Block, DyeColor>(){
        {
            this.put(Blocks.WHITE_BED, DyeColor.WHITE);
            this.put(Blocks.ORANGE_BED, DyeColor.ORANGE);
            this.put(Blocks.MAGENTA_BED, DyeColor.MAGENTA);
            this.put(Blocks.LIGHT_BLUE_BED, DyeColor.LIGHT_BLUE);
            this.put(Blocks.YELLOW_BED, DyeColor.YELLOW);
            this.put(Blocks.LIME_BED, DyeColor.LIME);
            this.put(Blocks.PINK_BED, DyeColor.PINK);
            this.put(Blocks.GRAY_BED, DyeColor.GRAY);
            this.put(Blocks.LIGHT_GRAY_BED, DyeColor.LIGHT_GRAY);
            this.put(Blocks.CYAN_BED, DyeColor.CYAN);
            this.put(Blocks.PURPLE_BED, DyeColor.PURPLE);
            this.put(Blocks.BLUE_BED, DyeColor.BLUE);
            this.put(Blocks.BROWN_BED, DyeColor.BROWN);
            this.put(Blocks.GREEN_BED, DyeColor.GREEN);
            this.put(Blocks.RED_BED, DyeColor.RED);
            this.put(Blocks.BLACK_BED, DyeColor.BLACK);
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
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null || BedBreaker.mc.interactionManager == null) {
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
        BlockState state = BedBreaker.mc.world.getBlockState(this.currentTarget.pos);
        if (state.isAir()) {
            return;
        }
        Rotation rotation = RotationUtils.getRotationBlock(this.currentTarget.pos, 0.0f);
        RotationUtils.setRotation(rotation, (Boolean)this.prioritizeOverKillAura.getValue() != false ? 2 : 1);
        BlockHitResult rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (rayTraceResult == null || rayTraceResult.getType() != HitResult.Type.BLOCK) {
            BlockHitResult synthetic;
            if ((this.isInstantMode() || this.isHeypixelMode()) && (synthetic = this.createSyntheticHit(this.currentTarget.pos)) != null) {
                this.switchToBestTool(this.currentTarget.pos);
                BlockBreakUtils.doBreak(synthetic, false, true);
            }
            return;
        }
        if (!rayTraceResult.getBlockPos().equals((Object)this.currentTarget.pos) || !ClientRayTraceUtil.didHitBlockFace((PlayerEntity)BedBreaker.mc.player, rotation.yaw, rotation.pitch, this.currentTarget.pos, rayTraceResult.getSide(), false)) {
            BlockHitResult synthetic;
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
        List<BlockPos> possibleBeds = this.searchPossibleTargetPositions();
        this.validateCurrentTarget(possibleBeds);
        if (possibleBeds.isEmpty()) {
            return;
        }
        double rangeValue = 4.0;
        double wallRangeValue = Math.min(rangeValue, 4.0);
        boolean foundDirect = false;
        if (this.isInstantMode()) {
            for (BlockPos bedPos : possibleBeds) {
                if (!Boolean.TRUE.equals(this.considerAsTarget(new DestroyerTarget(bedPos, true), rangeValue, wallRangeValue, false, false))) continue;
                foundDirect = true;
                break;
            }
        } else {
            for (BlockPos visibleBed : possibleBeds) {
                if (!this.isBedVisible(visibleBed) || !Boolean.TRUE.equals(this.considerAsTarget(new DestroyerTarget(visibleBed, true), rangeValue, wallRangeValue, false, true))) continue;
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
            for (BlockPos pos : possibleBeds) {
                BlockPos coverTarget = this.selectHeypixelCoverTarget(pos);
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
            for (BlockPos surroundBed : possibleBeds) {
                this.updateSurroundings(surroundBed, rangeValue, wallRangeValue);
            }
        }
    }

    private boolean isBedVisible(BlockPos bedPos) {
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(bedPos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        return rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK && rayTraceResult.getBlockPos().equals((Object)bedPos) && ClientRayTraceUtil.didHitBlockFace((PlayerEntity)BedBreaker.mc.player, rotation.yaw, rotation.pitch, bedPos, rayTraceResult.getSide(), false);
    }

    private BlockHitResult createSyntheticHit(BlockPos pos) {
        if (BedBreaker.mc.player == null) {
            return null;
        }
        Vec3d eye = BedBreaker.mc.player.getEyePos();
        Vec3d center = Vec3d.ofCenter((Vec3i)pos);
        Vec3d diff = center.subtract(eye);
        Direction face = Direction.getFacing((double)diff.x, (double)diff.y, (double)diff.z).getOpposite();
        return new BlockHitResult(center, face, pos, false);
    }

    private void validateCurrentTarget(List<BlockPos> possibleBeds) {
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

    private boolean matchesCurrentHeypixelCoverTarget(List<BlockPos> possibleBeds, BlockPos targetPos) {
        if (targetPos == null) {
            return false;
        }
        for (BlockPos bedPos : possibleBeds) {
            BlockPos cover = this.selectHeypixelCoverTarget(bedPos);
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

    private List<BlockPos> searchPossibleTargetPositions() {
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return Collections.emptyList();
        }
        double rangeValue = 4.0;
        int radius = (int)Math.ceil(rangeValue);
        Vec3d eyePos = BedBreaker.mc.player.getEyePos();
        ArrayList<BlockPos> possibleBeds = new ArrayList<BlockPos>();
        HashSet<Long> seenHeads = new HashSet<Long>();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    double distance;
                    BlockPos headPos;
                    BlockPos blockPos = BlockPos.ofFloored((double)(BedBreaker.mc.player.getX() + (double)x), (double)(BedBreaker.mc.player.getY() + (double)y), (double)(BedBreaker.mc.player.getZ() + (double)z));
                    BlockState state = BedBreaker.mc.world.getBlockState(blockPos);
                    if (!(state.getBlock() instanceof BedBlock) || this.checkTeamBed(state.getBlock()) || (headPos = this.getBedHeadPos(blockPos, state)) == null || !seenHeads.add(headPos.asLong()) || (distance = eyePos.distanceTo(Vec3d.ofCenter((Vec3i)headPos))) > rangeValue) continue;
                    possibleBeds.add(headPos.toImmutable());
                }
            }
        }
        possibleBeds.sort(Comparator.comparingDouble(pos -> eyePos.distanceTo(Vec3d.ofCenter((Vec3i)pos))));
        return possibleBeds;
    }

    private Boolean considerAsTarget(DestroyerTarget target, double rangeValue, double wallRangeValue, boolean isCurrentTarget, boolean requireVisible) {
        boolean canSee;
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return false;
        }
        BlockState state = BedBreaker.mc.world.getBlockState(target.pos);
        if (state.isAir() || state.getHardness((BlockView)BedBreaker.mc.world, target.pos) < 0.0f) {
            return false;
        }
        Rotation rotation = RotationUtils.getRotationBlock(target.pos, 0.0f);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult rayTraceResult = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        boolean bl = canSee = rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK && rayTraceResult.getBlockPos().equals((Object)target.pos) && ClientRayTraceUtil.didHitBlockFace((PlayerEntity)BedBreaker.mc.player, rotation.yaw, rotation.pitch, target.pos, rayTraceResult.getSide(), false);
        if (requireVisible && !canSee) {
            return false;
        }
        double distance = BedBreaker.mc.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)target.pos));
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

    private void updateSurroundings(BlockPos targetPos, double rangeValue, double wallRangeValue) {
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return;
        }
        Vec3d eyePos = BedBreaker.mc.player.getEyePos();
        Vec3d targetPoint = new Vec3d((double)targetPos.getX() + 0.5, (double)targetPos.getY() + 0.5, (double)targetPos.getZ() + 0.5);
        Rotation rotation = RotationUtils.getRotationByVector(eyePos, targetPoint, false);
        ClientRayTraceUtil.updateEyePos();
        BlockHitResult hit = ClientRayTraceUtil.getFacedBlock(rotation.yaw, rotation.pitch);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos startPos = hit.getBlockPos();
        if (startPos.equals((Object)targetPos)) {
            return;
        }
        List<BlockPos> path = this.traceWayToTarget(targetPos, startPos);
        double resistance = 0.0;
        for (BlockPos pos : path) {
            BlockState state = BedBreaker.mc.world.getBlockState(pos);
            if (state.isAir()) continue;
            resistance += this.miningDuration(pos);
        }
        this.considerAsTarget(new DestroyerTarget(startPos, false, resistance), rangeValue, wallRangeValue, false, false);
    }

    private List<BlockPos> traceWayToTarget(BlockPos target, BlockPos startPos) {
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return Collections.emptyList();
        }
        Vec3d eyePos = BedBreaker.mc.player.getEyePos();
        Vec3d targetPoint = new Vec3d((double)target.getX() + 0.5, (double)target.getY() + 0.5, (double)target.getZ() + 0.5);
        ArrayList<BlockPos> path = new ArrayList<BlockPos>();
        ArrayList<BlockPos> queue = new ArrayList<BlockPos>();
        queue.add(startPos);
        while (!queue.isEmpty()) {
            BlockPos current = (BlockPos)queue.removeFirst();
            if (current.equals((Object)target) || path.contains(current)) continue;
            BlockState state = BedBreaker.mc.world.getBlockState(current);
            if (!state.isAir() && !this.bedBlocks.contains(state.getBlock())) {
                path.add(current.toImmutable());
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.offset(direction);
                if (neighbor.equals((Object)target) || path.contains(neighbor) || queue.contains(neighbor) || !this.isBlockInLineOfSight(neighbor, eyePos, targetPoint)) continue;
                queue.add(neighbor);
            }
        }
        return path;
    }

    private boolean isBlockInLineOfSight(BlockPos blockPos, Vec3d start, Vec3d end) {
        BlockState state = BedBreaker.mc.world.getBlockState(blockPos);
        if (state.isAir()) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape((BlockView)BedBreaker.mc.world, blockPos);
        if (shape.isEmpty()) {
            return false;
        }
        Box box = shape.getBoundingBox().offset(blockPos);
        return box.raycast(start, end).isPresent();
    }

    private double miningDuration(BlockPos pos) {
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return Double.MAX_VALUE;
        }
        BlockState state = BedBreaker.mc.world.getBlockState(pos);
        if (state.isAir()) {
            return Double.MAX_VALUE;
        }
        float hardness = state.getHardness((BlockView)BedBreaker.mc.world, pos);
        if (hardness < 0.0f) {
            return Double.MAX_VALUE;
        }
        float bestMiningSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float speed;
            ItemStack stack = BedBreaker.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !((speed = stack.getMiningSpeedMultiplier(state)) > bestMiningSpeed)) continue;
            bestMiningSpeed = speed;
        }
        return hardness / bestMiningSpeed;
    }

    private BlockPos getBedHeadPos(BlockPos bedPos, BlockState state) {
        if (!(state.getBlock() instanceof BedBlock)) {
            return null;
        }
        Direction facing = (Direction)state.get((Property)BedBlock.FACING);
        BedPart part = (BedPart)state.get((Property)BedBlock.PART);
        return part == BedPart.HEAD ? bedPos.toImmutable() : bedPos.offset(facing).toImmutable();
    }

    private BedHeadInfo getBedHeadInfo(BlockPos bedPos, BlockState state) {
        if (!(state.getBlock() instanceof BedBlock)) {
            return null;
        }
        Direction facing = (Direction)state.get((Property)BedBlock.FACING);
        BlockPos head = this.getBedHeadPos(bedPos, state);
        if (head == null) {
            return null;
        }
        return new BedHeadInfo(head, facing);
    }

    private BlockPos selectHeypixelCoverTarget(BlockPos bedPos) {
        if (BedBreaker.mc.world == null || BedBreaker.mc.player == null) {
            return null;
        }
        BlockState state = BedBreaker.mc.world.getBlockState(bedPos);
        BedHeadInfo info = this.getBedHeadInfo(bedPos, state);
        if (info == null) {
            return null;
        }
        BlockPos coverPos = (Integer)this.heypixelPath.getValue() == 1 ? info.head.offset(info.facing) : info.head.up();
        return this.isBreakable(coverPos) ? coverPos.toImmutable() : null;
    }

    private boolean isBreakable(BlockPos pos) {
        if (BedBreaker.mc.world == null) {
            return false;
        }
        BlockState state = BedBreaker.mc.world.getBlockState(pos);
        return !state.isAir() && state.getHardness((BlockView)BedBreaker.mc.world, pos) >= 0.0f;
    }

    private DyeColor getArmorColor(ItemStack itemStack) {
        if (itemStack.isIn(ItemTags.DYEABLE) && itemStack.getItem() instanceof ArmorItem) {
            int i = DyedColorComponent.getColor((ItemStack)itemStack, (int)0);
            for (DyeColor dyeColor : DyeColor.values()) {
                if (i != dyeColor.getEntityColor()) continue;
                return dyeColor;
            }
        }
        return null;
    }

    private boolean checkTeamBed(Block block) {
        if (!this.bedBlocks.contains(block)) {
            return true;
        }
        DyeColor chestplateColor = this.getArmorColor(BedBreaker.mc.player.getInventory().getArmorStack(0));
        if (chestplateColor == null) {
            return false;
        }
        DyeColor color = bedColorMap.get(block);
        if (color == null) {
            return false;
        }
        if (!((Boolean)this.checkTeamsBed.getValue()).booleanValue()) {
            return false;
        }
        return color.equals((Object)chestplateColor);
    }

    private void switchToBestTool(BlockPos pos) {
        if (!((Boolean)this.autoTool.getValue()).booleanValue()) {
            return;
        }
        if (BedBreaker.mc.player == null || BedBreaker.mc.world == null) {
            return;
        }
        BlockState blockState = BedBreaker.mc.world.getBlockState(pos);
        if (blockState.isAir()) {
            return;
        }
        int bestSlot = this.findBestToolSlot(blockState);
        if (bestSlot != -1 && bestSlot != BedBreaker.mc.player.getInventory().selectedSlot) {
            this.previousSlot = BedBreaker.mc.player.getInventory().selectedSlot;
            BedBreaker.mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private void switchBackToPreviousTool() {
        if (this.previousSlot != -1 && BedBreaker.mc.player != null) {
            BedBreaker.mc.player.getInventory().selectedSlot = this.previousSlot;
            this.previousSlot = -1;
        }
    }

    private int findBestToolSlot(BlockState blockState) {
        if (BedBreaker.mc.player == null) {
            return -1;
        }
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float speed;
            ItemStack stack = BedBreaker.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !((speed = stack.getMiningSpeedMultiplier(blockState)) > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        return bestSlot;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (this.currentTarget != null && BedBreaker.mc.world != null && BedBreaker.mc.player != null) {
            Render3D.boxLines(new Box(this.currentTarget.pos), new Color(0, 255, 0, 255));
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
        final BlockPos pos;
        final boolean isTarget;
        final double resistance;

        DestroyerTarget(BlockPos pos, boolean isTarget) {
            this(pos, isTarget, 0.0);
        }

        DestroyerTarget(BlockPos pos, boolean isTarget, double resistance) {
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
        final BlockPos head;
        final Direction facing;

        BedHeadInfo(BlockPos head, Direction facing) {
            this.head = head;
            this.facing = facing;
        }
    }
}

