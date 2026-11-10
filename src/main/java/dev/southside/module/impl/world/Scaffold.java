package dev.southside.module.impl.world;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Color;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import dev.southside.event.Stage;
import dev.southside.event.events.MovementInputEvent;
import dev.southside.event.events.PlaceEvent;
import dev.southside.event.events.PlayerSlowdownEvent;
import dev.southside.event.events.Render2DEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.StrafeEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.UpdateFoVEvent;
import dev.southside.event.events.UpdateHeldItemEvent;
import dev.southside.manager.PacketOrderManager;
import dev.southside.manager.ServerPacketManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.nanovg.NanoUtil;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.BlockData;
import dev.southside.utils.ChatUtils;
import dev.southside.utils.InventoryUtil;
import dev.southside.utils.math.MathUtils;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.BlinkUtils;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.FallingPlayer;
import dev.southside.utils.player.MovementUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.player.SlotUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.types.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class Scaffold extends Module {
    public final Dropdown mode = new Dropdown("Mode", "Telly", "Telly", "Snap", "Normal");
    public final Switch alwaysUpdateRot = new Switch("Always Update Rotation", false);
    public final Slider placeTick = new Slider("PlaceTick", 1, 1, 5, 1);
    public final Slider rotTick = new Slider("RotationTick", 1, 1, 5, 1);
    public final Switch spoofItem = new Switch("Spoof Item", true);
    public final Switch noSwing = new Switch("No Swing", false);
    public final Switch eagle = new Switch("Eagle", false);
    public final Switch snap = new Switch("Snap", false);
    public final Switch noUptelly = new Switch("No Uptelly", true);
    public final Switch godBridge = new Switch("GodBridge", false);
    public final Switch smoothed = new Switch("Heypixel UpTelly", true);
    public final Switch safeMode = new Switch("Safe Mode", false);
    public final Switch testOnGround = new Switch("Test OnGround", false);
    public final Switch fixRotation = new Switch("Fix Rotation", true);
    public final Switch randomSlow = new Switch("SlowUpTelly", false);
    public final Switch blockFly = new Switch("Block Fly", false);
    public final Switch abuseRotation = new Switch("Abuse Rotation", true);
    public final Dropdown blockSlotMode = new Dropdown("Block Slot Mode", "Farthest", "Farthest", "Most Blocks");
    public final Dropdown jumpMode = new Dropdown("Jump Mode", "Normal", "Parkour", "Normal", "None");
    public final Slider safeDistance = new Slider("Clutch Safe Distance", 4.5, 1.0, 5.0, 0.25);
    public final Slider tellyEagleTick = new Slider("EagleTick", 1, 1, 5, 1);
    public final Slider keepEagleSneakTick = new Slider("KeepEagleTick", 1, 1, 5, 1);
    public final Switch dbgV = new Switch("Debug", false);
    public final Switch keepFoV = new Switch("Keep FoV", true);
    public final Slider fovValue = new Slider("Fov", 1.1, 1.0, 2.1, 0.05);
    public final Switch mark = new Switch("Mark", true);
    private final Switch duplicateRotPlace = new Switch("DuplicateRotPlace", true);
    private final Switch interactItem = new Switch("Interact Item Before Place", false);
    private final Color markSideColor = new Color("Mark Side Color", true, new OneColor(255, 255, 255, 70));
    private final Color markLineColor = new Color("Mark Line Color", true, new OneColor(255, 255, 255, 150));
    public final Switch blockCount = new Switch("BlockCount", "BlockCount", true);
    public final Dropdown blockCountStyle = new Dropdown("BlockCount Style", "Retro", "Retro", "Old", "Dark");
    public final Slider blockCountOffset = new Slider("BlockCount Y Offset", 0, 0, 200, 1);
    public static final List<Block> invalidBlocks = Arrays.asList(
            Blocks.ENCHANTING_TABLE,
            Blocks.OAK_SIGN,
            Blocks.CHEST,
            Blocks.ENDER_CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ANVIL,
            Blocks.SAND,
            Blocks.COBWEB,
            Blocks.TORCH,
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.WATER_CAULDRON,
            Blocks.DISPENSER,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.BAMBOO_PRESSURE_PLATE,
            Blocks.NOTE_BLOCK,
            Blocks.DROPPER,
            Blocks.TNT,
            Blocks.REDSTONE_TORCH,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.OAK_SIGN,
            Blocks.BIRCH_SIGN,
            Blocks.SPRUCE_SIGN,
            Blocks.JUNGLE_SIGN,
            Blocks.ACACIA_SIGN,
            Blocks.DARK_OAK_SIGN,
            Blocks.MANGROVE_SIGN,
            Blocks.CHERRY_SIGN,
            Blocks.BAMBOO_SIGN,
            Blocks.CRIMSON_SIGN,
            Blocks.WARPED_SIGN,
            Blocks.OAK_HANGING_SIGN,
            Blocks.BIRCH_HANGING_SIGN,
            Blocks.SPRUCE_HANGING_SIGN,
            Blocks.JUNGLE_HANGING_SIGN,
            Blocks.ACACIA_HANGING_SIGN,
            Blocks.DARK_OAK_HANGING_SIGN,
            Blocks.MANGROVE_HANGING_SIGN,
            Blocks.CHERRY_HANGING_SIGN,
            Blocks.BAMBOO_HANGING_SIGN,
            Blocks.CRIMSON_HANGING_SIGN,
            Blocks.WARPED_HANGING_SIGN
    );
    private Scaffold.SlotData slot;
    private Scaffold.SlotData blockSlot;
    int count;
    int lastCount = 0;
    private int startHotbarCount = 1;
    private boolean canPlace;
    private BlockData blockData;
    private BlockData lastBlockData;
    private int rotateCount = 0;
    private boolean holdingInput = false;
    private double posY;
    private BlockPos lastPlacePosition = null;
    private int tellyJumpTicks;
    private boolean waitingForEagleSneak;
    private Rotation lastRotation;
    private Rotation rot;
    private double boxExpand = 0.15;
    int oldSlot;
    int placeCount = 0;
    int ups = 0;

    public Scaffold() {
        super(Category.World, "Scaffold", "自动打撸");
    }

    @Override
    public void onEnable() {
        this.placeCount = 0;
        this.ups = 0;
        if (mc.player != null) {
            if (this.blockFly.getValue()) {
                ServerPacketManager.reset(true);
            }

            this.boxExpand = RandomUtils.nextDouble(0.1, 0.2);
            this.lastRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
            this.slot = new Scaffold.SlotData(mc.player.getInventory().selectedSlot, Hand.MAIN_HAND);
            this.oldSlot = mc.player.getInventory().selectedSlot;
            this.blockSlot = null;
            this.startHotbarCount = Math.max(1, this.getBlockCountHotbar());
            this.blockData = null;
            this.canPlace = true;
            this.lastPlacePosition = null;
            this.tellyJumpTicks = 0;
            this.waitingForEagleSneak = false;
            this.rot = null;
            super.onEnable();
        }
    }

    @Override
    public void initPostRunnable() {
        super.initPostRunnable();
        this.hideIf(this.jumpMode.getLabel(), () -> !this.mode.isMode("Telly"));
        this.hideIf(this.placeTick.getLabel(), () -> !this.mode.isMode("Telly"));
        this.hideIf(this.smoothed.getLabel(), () -> !this.mode.isMode("Telly"));
        this.hideIf(this.safeMode.getLabel(), () -> !this.smoothed.getValue());
        this.hideIf(this.godBridge.getLabel(), () -> !this.mode.isMode("Normal"));
        this.hideIf(this.fovValue.getLabel(), () -> !this.keepFoV.getValue());
        this.hideIf(this.markLineColor.getLabel(), () -> !this.mark.getValue());
        this.hideIf(this.markSideColor.getLabel(), () -> !this.mark.getValue());
        this.hideIf(this.blockCountStyle.getLabel(), () -> !this.blockCount.getValue());
        this.hideIf(this.blockCountOffset.getLabel(), () -> !this.blockCount.getValue());
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getInventory().selectedSlot = this.slot.slot();
            mc.player.getInventory().selectedSlot = this.oldSlot;
            mc.options.sneakKey.setPressed(false);
            MovementUtils.resetMove();
            super.onDisable();
        }
    }

    @EventHandler
    public void onFoV(UpdateFoVEvent event) {
        if (this.keepFoV.getValue() && PlayerUtils.isMoving()) {
            event.setFov(this.fovValue.getValue().floatValue() + PlayerUtils.getMoveSpeedEffectAmplifier() * 0.13F);
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.lastPlacePosition != null && this.mark.getValue()) {
            Box box = new Box(this.lastPlacePosition);
            Render3D.boxLines(box, this.markLineColor.getValue().toJavaColor());
            Render3D.boxSides(box, this.markSideColor.getValue().toJavaColor());
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (this.isEnabled() && mc.player != null && this.blockCount.getValue()) {
            int newCount = Math.max(0, this.getBlockCountHotbar());
            if (newCount > this.startHotbarCount) {
                this.startHotbarCount = newCount;
            }

            float rate = MathHelper.clamp((float)newCount / this.startHotbarCount, 0.0F, 1.0F);
            float centerX = mc.getWindow().getScaledWidth() / 2.0F;
            float centerY = mc.getWindow().getScaledHeight() / 2.0F;
            float y = centerY + 15.0F + this.blockCountOffset.getValue().floatValue();
            if (this.blockCountStyle.isMode("Old")) {
                String text = newCount + " Blocks";
                int x = Math.round(centerX - mc.textRenderer.getWidth(text) / 2.0F);
                event.getDrawContext().drawText(mc.textRenderer, text, x, Math.round(y), this.getBlockCountColor(newCount), true);
                this.lastCount = newCount;
                this.count = newCount;
            } else {
                if (this.blockCountStyle.isMode("Retro")) {
                    ItemStack displayStack = this.getHeldBlockStack();
                    float scale = 1.0F;
                    float height = 26.0F * scale;
                    float radius = 5.0F * scale;
                    float pad = 5.0F * scale;
                    float itemSize = 18.0F * scale;
                    float itemOffsetX = 4.0F * scale;
                    float textSize = 10.0F * scale;
                    float labelSize = 10.0F * scale;
                    float textOffsetY = 0.5F * scale;
                    NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
                    ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
                    float[] textWidth = new float[]{0.0F};
                    String label = "Blocks";
                    String countText = String.valueOf(newCount);
                    float gap = 3.0F * scale;
                    nanoVGHelper.setupAndDraw(
                            true,
                            vg -> {
                                float labelWidth = nanoVGHelper.getTextWidth(vg, label, labelSize, Fonts.REGULAR);
                                float countWidth = nanoVGHelper.getTextWidth(vg, countText, textSize, Fonts.SEMIBOLD);
                                textWidth[0] = labelWidth + gap + countWidth;
                                float widthx = itemOffsetX + itemSize + pad + textWidth[0] + pad;
                                float xx = centerX - widthx / 2.0F;
                                int bg = new java.awt.Color(0, 0, 0, 90).getRGB();
                                int revert = new java.awt.Color(255, 255, 255, 90).getRGB();
                                int textColor = this.getBlockCountColor(newCount);
                                int revertedTextColor = this.getBlockCountColor_Black(newCount);
                                labelWidth = nanoVGHelper.getTextWidth(vg, label, labelSize, Fonts.REGULAR);
                                NVGPaint shadowPaint = NVGPaint.calloc();
                                NanoVG.nvgBoxGradient(
                                        vg,
                                        xx,
                                        y,
                                        widthx,
                                        height,
                                        radius,
                                        7.0F * scale,
                                        NanoUtil.getColor(new java.awt.Color(0, 0, 0, 50)),
                                        NanoUtil.getColor(new java.awt.Color(0, 0, 0, 0)),
                                        shadowPaint
                                );
                                NanoVG.nvgBeginPath(vg);
                                NanoVG.nvgRect(vg, xx - 7.0F * scale, y - 7.0F * scale, widthx + 14.0F * scale, height + 14.0F * scale);
                                NanoVG.nvgRoundedRect(vg, xx, y, widthx, height, radius);
                                NanoVG.nvgPathWinding(vg, 2);
                                NanoVG.nvgFillPaint(vg, shadowPaint);
                                NanoVG.nvgFill(vg);
                                shadowPaint.free();
                                float rightAreaLeft = xx + itemOffsetX + itemSize + pad;
                                float rightAreaRight = xx + widthx - pad;
                                float rightAreaWidth = Math.max(0.0F, rightAreaRight - rightAreaLeft);
                                float groupWidth = textWidth[0];
                                float groupX = rightAreaLeft + Math.max(0.0F, (rightAreaWidth - groupWidth) / 2.0F);
                                float labelY = y + height / 2.0F + textOffsetY;
                                float countY = y + height / 2.0F + textOffsetY;
                                Scissor scissor = scissorHelper.scissor(vg, xx + widthx * rate, y, widthx - widthx * rate, height);
                                nanoVGHelper.drawRoundedRect(vg, xx, y, widthx, height, bg, radius);
                                nanoVGHelper.drawText(vg, label, groupX, labelY, new java.awt.Color(220, 220, 220).getRGB(), labelSize, Fonts.REGULAR);
                                nanoVGHelper.drawText(vg, countText, groupX + labelWidth + gap, countY, textColor, textSize, Fonts.SEMIBOLD);
                                scissorHelper.resetScissor(vg, scissor);
                                Scissor scissor1 = scissorHelper.scissor(vg, xx, y, widthx * rate, height);
                                nanoVGHelper.drawRoundedRect(vg, xx, y, widthx, height, revert, radius);
                                nanoVGHelper.drawText(vg, label, groupX, labelY, new java.awt.Color(0, 0, 0, 204).getRGB(), labelSize, Fonts.REGULAR);
                                nanoVGHelper.drawText(vg, countText, groupX + labelWidth + gap, countY, revertedTextColor, textSize, Fonts.SEMIBOLD);
                                scissorHelper.resetScissor(vg, scissor1);
                                float fillW = widthx * rate;
                                if (fillW > 1.0F) {
                                    NVGPaint bloomPaint = NVGPaint.calloc();
                                    NanoVG.nvgBoxGradient(
                                            vg,
                                            xx,
                                            y,
                                            fillW,
                                            height,
                                            radius,
                                            5.0F * scale,
                                            NanoUtil.getColor(new java.awt.Color(255, 255, 255, 30)),
                                            NanoUtil.getColor(new java.awt.Color(0, 0, 0, 0)),
                                            bloomPaint
                                    );
                                    NanoVG.nvgBeginPath(vg);
                                    NanoVG.nvgRect(vg, xx - 5.0F * scale, y - 5.0F * scale, fillW + 10.0F * scale, height + 10.0F * scale);
                                    NanoVG.nvgRoundedRect(vg, xx, y, fillW, height, radius);
                                    NanoVG.nvgPathWinding(vg, 2);
                                    NanoVG.nvgFillPaint(vg, bloomPaint);
                                    NanoVG.nvgFill(vg);
                                    bloomPaint.free();
                                }
                            }
                    );
                    float width = itemOffsetX + itemSize + pad + textWidth[0] + pad;
                    float x = centerX - width / 2.0F;
                    float itemX = x + itemOffsetX;
                    float itemY = y + (height - itemSize) / 2.0F;
                    float itemScale = itemSize / 16.0F;
                    if (!displayStack.isEmpty() && this.getBlockCountHotbar() > 0) {
                        MatrixStack matrices = event.getDrawContext().getMatrices();
                        matrices.push();
                        matrices.translate(itemX, itemY, 0.0F);
                        matrices.scale(itemScale, itemScale, 1.0F);
                        event.getDrawContext().drawItem(displayStack, 0, 0);
                        matrices.pop();
                    } else {
                        MatrixStack matrices = event.getDrawContext().getMatrices();
                        matrices.push();
                        matrices.translate(itemX, itemY, 0.0F);
                        matrices.scale(itemScale, itemScale, 1.0F);
                        event.getDrawContext().drawItem(new ItemStack(Items.BARRIER), 0, 0);
                        matrices.pop();
                    }
                }

                if (this.blockCountStyle.isMode("Dark")) {
                    ItemStack displayStack = this.getHeldBlockStack();
                    float scale = 1.0F;
                    float height = 22.0F * scale;
                    float pad = 5.0F * scale;
                    float itemSize = 14.0F * scale;
                    float itemPad = 5.0F * scale;
                    float textSize = 10.0F * scale;
                    float accentWidth = 2.0F * scale;
                    float accentGap = 5.0F * scale;
                    NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
                    ScissorHelper scissorHelper = ScissorHelper.INSTANCE;
                    String countText = String.valueOf(newCount);
                    float[] countWidthArr = new float[]{0.0F};
                    nanoVGHelper.setupAndDraw(
                            true,
                            vg -> {
                                float countWidth = nanoVGHelper.getTextWidth(vg, countText, textSize, Fonts.SEMIBOLD);
                                countWidthArr[0] = countWidth;
                                float contentWidthx = itemPad + accentWidth + accentGap + itemSize + pad + countWidth + pad;
                                float radiusx = height / 2.0F;
                                float xx = centerX - contentWidthx / 2.0F;
                                NVGPaint shadowPaint = NVGPaint.calloc();
                                NanoVG.nvgBoxGradient(
                                        vg,
                                        xx,
                                        y,
                                        contentWidthx,
                                        height,
                                        radiusx,
                                        8.0F * scale,
                                        NanoUtil.getColor(new java.awt.Color(0, 0, 0, 60)),
                                        NanoUtil.getColor(new java.awt.Color(0, 0, 0, 0)),
                                        shadowPaint
                                );
                                NanoVG.nvgBeginPath(vg);
                                NanoVG.nvgRect(vg, xx - 8.0F * scale, y - 8.0F * scale, contentWidthx + 16.0F * scale, height + 16.0F * scale);
                                NanoVG.nvgRoundedRect(vg, xx, y, contentWidthx, height, radiusx);
                                NanoVG.nvgPathWinding(vg, 2);
                                NanoVG.nvgFillPaint(vg, shadowPaint);
                                NanoVG.nvgFill(vg);
                                shadowPaint.free();
                                NanoVG.nvgBeginPath(vg);
                                NanoVG.nvgRoundedRect(vg, xx, y, contentWidthx, height, radiusx);
                                NanoVG.nvgFillColor(vg, NanoUtil.getColor(new java.awt.Color(12, 12, 18, 190)));
                                NanoVG.nvgFill(vg);
                                float fillW = contentWidthx * rate;
                                if (fillW > 1.0F) {
                                    Scissor fillScissor = scissorHelper.scissor(vg, xx, y, fillW, height);
                                    NVGPaint fillPaint = NVGPaint.calloc();
                                    NanoVG.nvgLinearGradient(
                                            vg,
                                            xx,
                                            y,
                                            xx + fillW,
                                            y,
                                            NanoUtil.getColor(new java.awt.Color(255, 255, 255, 18)),
                                            NanoUtil.getColor(new java.awt.Color(255, 255, 255, 8)),
                                            fillPaint
                                    );
                                    NanoVG.nvgBeginPath(vg);
                                    NanoVG.nvgRoundedRect(vg, xx, y, contentWidthx, height, radiusx);
                                    NanoVG.nvgFillPaint(vg, fillPaint);
                                    NanoVG.nvgFill(vg);
                                    fillPaint.free();
                                    scissorHelper.resetScissor(vg, fillScissor);
                                }

                                float accentX = xx + itemPad;
                                float accentY = y + 4.0F * scale;
                                float accentH = height - 8.0F * scale;
                                int accentRGB = newCount < 16
                                        ? new java.awt.Color(255, 75, 75).getRGB()
                                        : (newCount < 32 ? new java.awt.Color(255, 195, 55).getRGB() : new java.awt.Color(90, 200, 255).getRGB());
                                nanoVGHelper.drawRoundedRect(vg, accentX, accentY, accentWidth, accentH, accentRGB, accentWidth / 2.0F);
                                NVGPaint accentBloom = NVGPaint.calloc();
                                NanoVG.nvgBoxGradient(
                                        vg,
                                        accentX - 3.0F * scale,
                                        accentY,
                                        accentWidth + 6.0F * scale,
                                        accentH,
                                        accentWidth,
                                        5.0F * scale,
                                        NanoUtil.getColor(
                                                new java.awt.Color(
                                                        newCount < 16 ? 255 : (newCount < 32 ? 255 : 90),
                                                        newCount < 16 ? 75 : (newCount < 32 ? 195 : 200),
                                                        newCount < 16 ? 75 : (newCount < 32 ? 55 : 255),
                                                        40
                                                )
                                        ),
                                        NanoUtil.getColor(new java.awt.Color(0, 0, 0, 0)),
                                        accentBloom
                                );
                                NanoVG.nvgBeginPath(vg);
                                NanoVG.nvgRect(vg, accentX - 5.0F * scale, accentY - 5.0F * scale, accentWidth + 10.0F * scale, accentH + 10.0F * scale);
                                NanoVG.nvgRoundedRect(vg, accentX, accentY, accentWidth, accentH, accentWidth / 2.0F);
                                NanoVG.nvgPathWinding(vg, 2);
                                NanoVG.nvgFillPaint(vg, accentBloom);
                                NanoVG.nvgFill(vg);
                                accentBloom.free();
                                float textX = xx + itemPad + accentWidth + accentGap + itemSize + pad;
                                float textY = y + (height - textSize) / 2.0F;
                                nanoVGHelper.drawText(vg, countText, textX, textY, accentRGB, textSize, Fonts.SEMIBOLD);
                            }
                    );
                    float contentWidth = itemPad + accentWidth + accentGap + itemSize + pad + countWidthArr[0] + pad;
                    float radius = height / 2.0F;
                    float x = centerX - contentWidth / 2.0F;
                    float itemX = x + itemPad + accentWidth + accentGap;
                    float itemY = y + (height - itemSize) / 2.0F;
                    float itemScale = itemSize / 16.0F;
                    if (!displayStack.isEmpty() && this.getBlockCountHotbar() > 0) {
                        MatrixStack matrices = event.getDrawContext().getMatrices();
                        matrices.push();
                        matrices.translate(itemX, itemY, 0.0F);
                        matrices.scale(itemScale, itemScale, 1.0F);
                        event.getDrawContext().drawItem(displayStack, 0, 0);
                        matrices.pop();
                    } else {
                        MatrixStack matrices = event.getDrawContext().getMatrices();
                        matrices.push();
                        matrices.translate(itemX, itemY, 0.0F);
                        matrices.scale(itemScale, itemScale, 1.0F);
                        event.getDrawContext().drawItem(new ItemStack(Items.BARRIER), 0, 0);
                        matrices.pop();
                    }
                }

                this.lastCount = newCount;
                this.count = newCount;
            }
        }
    }

    @EventHandler
    public void onHeldItem(UpdateHeldItemEvent event) {
        if (this.spoofItem.getValue() && event.getHand() == Hand.MAIN_HAND) {
            event.setItem(mc.player.getInventory().getStack(this.oldSlot));
        }
    }

    @EventHandler
    public void onMoveInput(StrafeEvent event) {
        this.setSuffix(this.mode.getMode());
        if (this.blockSlot != null && !this.blockSlot.check()) {
            if (PlayerUtils.onGroundTicks > (this.smoothed.getValue() && this.safeMode.getValue() && !this.testOnGround.getValue() ? 1 : 0)
                    && !mc.options.jumpKey.isPressed()
                    && PlayerUtils.isMoving()
                    && this.mode.isMode("Telly")) {
                String var2 = this.jumpMode.getMode();
                switch (var2) {
                    case "None":
                    default:
                        break;
                    case "Normal":
                        mc.player.jump();
                        this.spoofJumpInput();
                        break;
                    case "Parkour":
                        double yaw = Math.toRadians(mc.player.getYaw());
                        double forwardX = -Math.sin(yaw);
                        double forwardZ = Math.cos(yaw);
                        BlockPos frontPos1 = new BlockPos(
                                (int)(mc.player.getX() + forwardX), (int)(mc.player.getY() - 0.1), (int)(mc.player.getZ() + forwardZ)
                        );
                        BlockPos frontPos2 = new BlockPos(
                                (int)(mc.player.getX() + forwardX * 2.0),
                                (int)(mc.player.getY() - 0.1),
                                (int)(mc.player.getZ() + forwardZ * 2.0)
                        );
                        if (mc.world.getBlockState(frontPos1).getBlock() instanceof AirBlock
                                || mc.world.getBlockState(frontPos2).getBlock() instanceof AirBlock) {
                            mc.player.jump();
                            this.spoofJumpInput();
                        }
                }

                if (this.eagle.getValue() && this.mode.isMode("Telly")) {
                    this.waitingForEagleSneak = true;
                    this.tellyJumpTicks = 0;
                }
            }
        }
    }

    private void spoofJumpInput() {
        if (mc.player != null) {
            PlayerInput current = mc.player.input.playerInput;

            mc.player.input.playerInput = new PlayerInput(
                    current.forward(), current.backward(), current.left(), current.right(), true, current.sneak(), current.sprint()
            );
        }
    }

    private Rotation getBRot(boolean forceRotation) {
        Rotation rotation = this.blockData != null
                ? RotationUtils.getClosestToBlockFace(
                this.blockData.pos(), this.blockData.facing(), RotationUtils.getServerRotation().yaw, RotationUtils.getServerRotation().pitch
        )
                : null;
        if (rotation == null) {
            if (RotationUtils.normalizeYawDiff(mc.player.getYaw() + 100.0F, RotationUtils.getServerRotation().yaw)
                    < RotationUtils.normalizeYawDiff(mc.player.getYaw() - 100.0F, RotationUtils.getServerRotation().yaw)) {
                rotation = new Rotation(mc.player.getYaw() + 100.0F, RotationUtils.getServerRotation().pitch);
            } else {
                rotation = new Rotation(mc.player.getYaw() - 100.0F, RotationUtils.getServerRotation().pitch);
            }
        }

        if (this.holdingInput || MovementUtils.cancelMove) {
            return this.blockData == null
                    ? rotation
                    : RotationUtils.getClosestToBlockFace(
                    this.blockData.pos(), this.blockData.facing(), RotationUtils.getServerRotation().yaw, RotationUtils.getServerRotation().pitch
            );
        } else {
            double diff = RotationUtils.yawDiffDirectly(rotation.yaw, RotationUtils.getServerRotation().yaw);
            if (this.mode.isMode("Telly")) {
                if (mc.options.jumpKey.isPressed() && this.noUptelly.getValue()) {
                    return rotation;
                }

                if (mc.options.jumpKey.isPressed() && this.randomSlow.getValue()) {
                    this.ups++;
                    if (this.ups % 2 == 0) {
                        return rotation;
                    }
                }

                if (!this.smoothed.getValue() || PlayerUtils.offGroundTicks >= this.rotTick.getValue().intValue() && !this.safeMode.getValue()) {
                    if (this.snap.getValue() && mc.options.jumpKey.isPressed()) {
                        if (this.lastBlockData == null || PlayerUtils.offGroundTicks < this.rotTick.getValue().intValue()) {
                            return new Rotation(mc.player.getYaw(), 85.0F + (float)Math.random());
                        }
                    } else if (PlayerUtils.offGroundTicks < this.rotTick.getValue().intValue()) {
                        return new Rotation(mc.player.getYaw(), 85.0F + (float)Math.random());
                    }
                } else if (PlayerUtils.onGroundTicks > 0) {
                    if (!this.safeMode.getValue() || this.testOnGround.getValue() && !mc.options.jumpKey.isPressed()) {
                        return new Rotation(mc.player.getYaw(), 75.5F);
                    }

                    switch (PlayerUtils.onGroundTicks) {
                        case 1:
                            if (!forceRotation) {
                                rotation.yaw = RotationUtils.getServerRotation().getYaw() + RotationUtils.smooth((float)diff, (float)(diff / 2.0));
                                rotation.pitch = 75.5F;
                            } else if (this.blockData != null) {
                                rotation = RotationUtils.getClosestToBlockFace(
                                        this.blockData.pos(), this.blockData.facing(), mc.player.getYaw(), RotationUtils.getServerRotation().pitch
                                );
                            }

                            mc.player.jumpingCooldown = 2;
                            break;
                        case 2:
                            return new Rotation(mc.player.getYaw(), 75.5F);
                    }
                } else {
                    float smooth = switch (PlayerUtils.offGroundTicks) {
                        case 1 -> 80.0F;
                        default -> 50.0F;
                    };
                    smooth -= RandomUtils.generateRandomFloat(0.001F, 0.005F);
                    rotation.yaw = RotationUtils.getServerRotation().getYaw() + RotationUtils.smooth((float)diff, smooth);
                }
            }

            if (this.lastRotation != null
                    && this.blockData != null
                    && ClientRayTraceUtil.didHitBlockFace(
                    mc.player, this.lastRotation.yaw, this.lastRotation.pitch, this.blockData.pos(), this.blockData.facing(), true
            )) {
                return this.lastRotation;
            } else if (this.blockData != null
                    && !this.alwaysUpdateRot.getValue()
                    && PlayerUtils.offGroundTicks >= this.rotTick.getValue().intValue()
                    && !ClientRayTraceUtil.didHitBlockFace(mc.player, rotation.yaw, rotation.pitch, this.blockData.pos(), this.blockData.facing(), true)
                    && PlayerUtils.offGroundTicks >= this.rotTick.getValue().intValue()) {
                this.lastRotation.yaw = this.lastRotation.yaw + (float)Math.random();
                return this.lastRotation;
            } else {
                this.lastRotation = rotation;
                return rotation;
            }
        }
    }

    private void place() {
        if (this.blockData != null && !BlinkUtils.blinking) {
            if (RotationUtils.getRotation() == null) {
                return;
            }

            if (mc.interactionManager == null) {
                return;
            }

            if (!this.canPlace) {
                return;
            }

            BlockHitResult block = ClientRayTraceUtil.getFacedBlock(RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch);
            if (!ClientRayTraceUtil.didHitBlockFace(
                    mc.player, RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, this.blockData.pos(), this.blockData.facing(), true
            )) {
                return;
            }

            if (this.blockSlot.hand().equals(Hand.MAIN_HAND)) {
                mc.player.getInventory().selectedSlot = this.blockSlot.slot();
            }

            if (this.duplicateRotPlace.getValue() && PlayerUtils.lastPitchDiff > 2.0) {
                double xDiff = Math.abs(PlayerUtils.lastPitchDiff - PlayerUtils.lastPlacePitchDiff);
                if (xDiff < 1.0E-4) {
                    return;
                }
            }

            if (this.blockFly.getValue()) {
                if (!ServerPacketManager.deSyncing) {
                    ServerPacketManager.setup();
                }

                if (this.placeCount == 0) {
                    PacketUtils.sendPacketNoEvent(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
                    PacketOrderManager.swap = true;
                }
            }

            if (this.interactItem.getValue()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }

            ActionResult result = mc.interactionManager.interactBlock(mc.player, this.blockSlot.hand(), block);
            PacketOrderManager.rightClicking = true;
            if (result == ActionResult.SUCCESS) {
                this.placeCount++;
                this.lastPlacePosition = this.blockData.pos().offset(this.blockData.facing());
                if (PlayerUtils.lastPitchDiff > 0.0) {
                    PlayerUtils.lastPlacePitchDiff = PlayerUtils.lastPitchDiff;
                }

                if (this.noSwing.getValue()) {
                    PacketUtils.sendPacket(new HandSwingC2SPacket(this.blockSlot.hand()));
                } else {
                    mc.player.swingHand(this.blockSlot.hand());
                }
            }
        }
    }

    @EventHandler
    public void onPlace(PlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onRotationApplied(RotationAppliedEvent event) {
        if (this.abuseRotation.getValue() && RotationUtils.rotation != null) {
            this.rotationAbuse(30.0F, RotationUtils.rotation.yaw);
        }

        this.place();
    }

    @EventHandler
    public void onPostTick(TickEvent event) {
        if (event.getState().equals(Stage.POST) && this.blockFly.getValue() && ServerPacketManager.deSyncTick > 16) {
            ServerPacketManager.releaseTick(true);
        }
    }

    @EventHandler
    public void onRotation(RotationEvent event) {
        this.blockSlot = null;
        if (InventoryUtil.isFullBlock(mc.player.getOffHandStack()) && this.isValid(mc.player.getOffHandStack().getItem())) {
            this.blockSlot = new Scaffold.SlotData(45, Hand.OFF_HAND);
        }

        if (this.blockSlot == null
                && !this.blockSlotMode.isMode("Most Blocks")
                && InventoryUtil.isFullBlock(mc.player.getMainHandStack())
                && this.isValid(mc.player.getMainHandStack().getItem())) {
            this.blockSlot = new Scaffold.SlotData(mc.player.getInventory().selectedSlot, Hand.MAIN_HAND);
        }

        if (this.blockSlot == null) {
            int hotbarSlot = this.getHotbarBlockSlot();
            if (hotbarSlot != -1) {
                this.blockSlot = new Scaffold.SlotData(hotbarSlot, Hand.MAIN_HAND);
            }
        }

        if (this.blockSlot != null && !this.blockSlot.check()) {
            if (RotationUtils.accepted(this.rotationPriority())) {
                if (mc.player.isOnGround()) {
                    this.posY = Math.floor(mc.player.getY() - 1.0);
                }

                if (mc.options.jumpKey.isPressed()) {
                    this.posY = mc.player.getBlockY() - 1;
                }

                BlockData possible = ClientRayTraceUtil.isIgnoredBlock(
                        mc.world
                                .getBlockState(
                                        new BlockPos(
                                                (int)Math.floor(mc.player.getX()),
                                                (int)Math.floor(mc.player.getY()),
                                                (int)Math.floor(mc.player.getZ())
                                        )
                                )
                )
                        ? this.getBlockData(new BlockPos((int)Math.floor(mc.player.getX()), (int)this.posY, (int)Math.floor(mc.player.getZ())))
                        : null;
                if (possible != null) {
                    this.blockData = possible;
                }

                this.lastBlockData = possible;
                if (this.mode.isMode("Normal")) {
                    this.canPlace = true;
                } else if (this.mode.isMode("Snap")) {
                    this.canPlace = this.doesNotContainBlock(1);
                } else {
                    this.canPlace = PlayerUtils.offGroundTicks >= this.placeTick.getValue().intValue();
                    if (this.safeMode.getValue() && this.testOnGround.getValue() && !this.canPlace && mc.options.jumpKey.isPressed()) {
                        this.canPlace = PlayerUtils.onGroundTicks == 1;
                    }
                }

                if (this.blockSlot.hand().equals(Hand.MAIN_HAND)) {
                    mc.player.getInventory().selectedSlot = this.blockSlot.slot;
                }

                FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
                boolean reachable = true;
                fallingPlayer.calculate(1);
                Vec3d nextEyePos = fallingPlayer.getEyePos();
                fallingPlayer.calculate(1);
                BlockData placement = this.getBlockData(
                        new BlockPos((int)Math.floor(mc.player.getX()), mc.player.getBlockY() - 1, (int)Math.floor(mc.player.getZ()))
                );
                boolean forceRotation = false;
                if (placement != null) {
                    if (this.safeMode.getValue() && this.testOnGround.getValue() && PlayerUtils.onGroundTicks == 1 && mc.options.jumpKey.isPressed()) {
                        forceRotation = true;
                    }

                    double distance = nextEyePos.distanceTo(placement.pos().toCenterPos());
                    if (distance >= this.safeDistance.getDouble() || placement.pos().getY() > fallingPlayer.getY()) {
                        this.canPlace = true;
                        reachable = false;
                        this.blockData = this.lastBlockData = placement;
                    }
                }

                if (this.blockData != null) {
                    Box box = new Box(this.blockData.pos())
                            .withMinY(this.blockData.pos().getY() - 1)
                            .withMaxY(this.blockData.pos().getY() + 1);
                    if (this.blockData.pos().getY() > fallingPlayer.getY() && !box.contains(mc.player.getPos())) {
                        this.canPlace = true;
                        reachable = false;
                        this.posY = mc.player.getBlockY() - 1;
                        this.blockData = this.lastBlockData = this.getBlockData(
                                new BlockPos((int)Math.floor(mc.player.getX()), (int)Math.floor(this.posY), (int)Math.floor(mc.player.getZ()))
                        );
                    }
                }

                if (!reachable && this.rotateCount < 8) {
                    if (this.dbgV.getValue() && this.rotateCount == 1) {
                        ChatUtils.info("working");
                    }

                    this.holdingInput = true;
                    MovementUtils.cancelMove();
                    MovementUtils.requestInputHold();
                    this.rotateCount++;
                } else {
                    this.holdingInput = false;
                    this.rotateCount = 0;
                }

                this.rot = this.getBRot(forceRotation);
                if (this.duplicateRotPlace.getValue()) {
                    this.rot.pitch = this.rot.pitch - RandomUtils.generateRandomFloat(0.001F, 0.003F);
                    this.rot.yaw = this.rot.yaw - RandomUtils.generateRandomFloat(1.0E-4F, 3.0E-4F);

                    do {
                        this.rot.pitch = this.rot.pitch - RandomUtils.generateRandomFloat(0.001F, 0.003F);
                    } while (this.rot.pitch > 90.0F);

                    if (this.rot.pitch < -90.0F) {
                        this.rot.pitch = -90.0F;
                    }
                }

                if (this.blockData == null || !ClientRayTraceUtil.didHitBlockFace(this.rot, this.blockData.pos(), this.blockData.facing(), true)) {
                    this.holdingInput = false;
                    this.rotateCount = 0;
                    MovementUtils.resetMove();
                }

                if (this.fixRotation.getValue() && this.rot != null) {
                    this.rot.fixedSensitivity(((Double)mc.options.getMouseSensitivity().getValue()).floatValue());
                }

                RotationUtils.setRotation(this.rot, this.rotationPriority(), MoveFixMode.Silent);
                if (this.blockData != null) {
                    if (mc.player.isSpectator()) {
                        this.setEnable(false);
                    }

                    if (!this.mode.isMode("Telly")) {
                        if (this.waitingForEagleSneak) {
                            this.tellyJumpTicks++;
                            if (this.tellyJumpTicks == this.tellyEagleTick.getValue().intValue() && !mc.options.sneakKey.isPressed()) {
                                mc.options.sneakKey.setPressed(true);
                            }

                            if (this.tellyJumpTicks == this.tellyEagleTick.getValue().intValue() + this.keepEagleSneakTick.getValue().intValue()) {
                                mc.options.sneakKey.setPressed(false);
                                this.waitingForEagleSneak = false;
                                this.tellyJumpTicks = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMoveInput(MovementInputEvent event) {
        if (this.mode.isMode("Telly") && this.eagle.getValue()) {
            event.setSneak(this.placeCount % 4 == 0);
        }
    }

    @EventHandler
    public void onSlow(PlayerSlowdownEvent event) {
        if (PlayerUtils.onGroundTicks == 1
                && this.testOnGround.getValue()
                && this.smoothed.getValue()
                && !this.noUptelly.getValue()
                && this.safeMode.getValue()
                && mc.options.jumpKey.isPressed()) {
            mc.player.input.movementForward *= 0.2F;
            mc.player.input.movementSideways *= 0.2F;
        }
    }

    public boolean doesNotContainBlock(int down) {
        return PlayerUtils.blockRelativeToPlayer(0.0, -down, 0.0).getDefaultState().isTransparent();
    }

    public static Vec3d getVec3(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == Direction.WEST || face == Direction.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }

        return new Vec3d(x, y, z);
    }

    public void getBlock(int switchSlot) {
        for (int i = 9; i < 45; i++) {
            if (mc.player.getInventory().getStack(i).isStackable() && mc.currentScreen == null) {
                ItemStack is = mc.player.getInventory().getStack(i);
                if (is.getItem() instanceof BlockItem block && this.isValid(block)) {
                    if (36 + switchSlot != i) {
                        InventoryUtil.swap(i, switchSlot);
                    }
                    break;
                }
            }
        }
    }

    public Rotation getRotation(Vec3d vec, Vec3d eyes) {
        double diffX = vec.x - eyes.x;
        double diffY = vec.y - eyes.y;
        double diffZ = vec.z - eyes.z;
        return new Rotation(
                MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F),
                MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)))))
        );
    }

    private boolean isValid(Item item) {
        return item instanceof BlockItem && !invalidBlocks.contains(((BlockItem)item).getBlock()) && SlotUtils.isGoodForBridging(item);
    }

    private int getHotbarBlockSlot() {
        if (this.blockSlotMode.isMode("Most Blocks")) {
            return this.getMostBlocksHotbarSlot();
        } else {
            int slot = -1;

            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (InventoryUtil.isFullBlock(stack) && this.isValid(stack.getItem())) {
                    slot = i;
                }
            }

            return slot;
        }
    }

    private int getMostBlocksHotbarSlot() {
        int selectedSlot = mc.player.getInventory().selectedSlot;
        int bestSlot = -1;
        int bestCount = -1;
        ItemStack selectedStack = mc.player.getInventory().getStack(selectedSlot);
        if (InventoryUtil.isFullBlock(selectedStack) && this.isValid(selectedStack.getItem())) {
            bestSlot = selectedSlot;
            bestCount = selectedStack.getCount();
        }

        for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (InventoryUtil.isFullBlock(stack) && this.isValid(stack.getItem()) && stack.getCount() > bestCount) {
                bestSlot = i;
                bestCount = stack.getCount();
            }
        }

        return bestSlot;
    }

    private ItemStack getHeldBlockStack() {
        if (mc.player == null) {
            return ItemStack.EMPTY;
        } else {
            if (this.blockSlot != null) {
                ItemStack blockSlotStack;
                if (this.blockSlot.hand() == Hand.OFF_HAND) {
                    blockSlotStack = mc.player.getOffHandStack();
                } else {
                    blockSlotStack = mc.player.getInventory().getStack(this.blockSlot.slot());
                }

                if (this.isDisplayBlock(blockSlotStack)) {
                    return blockSlotStack;
                }
            }

            ItemStack main = mc.player.getMainHandStack();
            if (this.isDisplayBlock(main)) {
                return main;
            } else {
                ItemStack offhand = mc.player.getOffHandStack();
                if (this.isDisplayBlock(offhand)) {
                    return offhand;
                } else {
                    int hotbarSlot = this.getHotbarBlockSlot();
                    return hotbarSlot != -1 ? mc.player.getInventory().getStack(hotbarSlot) : ItemStack.EMPTY;
                }
            }
        }
    }

    private boolean isDisplayBlock(ItemStack stack) {
        return stack != null && !stack.isEmpty() ? this.isValid(stack.getItem()) : false;
    }

    private int getBlockCountColor(int count) {
        if (count < 16) {
            return new java.awt.Color(255, 80, 80).getRGB();
        } else {
            return count < 32 ? new java.awt.Color(255, 220, 80).getRGB() : java.awt.Color.WHITE.getRGB();
        }
    }

    private int getBlockCountColor_Black(int count) {
        if (count < 16) {
            return new java.awt.Color(255, 80, 80).getRGB();
        } else {
            return count < 32 ? new java.awt.Color(255, 220, 80).getRGB() : java.awt.Color.BLACK.getRGB();
        }
    }

    public int getBlockCountInventory() {
        int blockCount = 0;

        for (int i = 9; i < 45; i++) {
            if (mc.player == null) {
                return -1;
            }

            if (mc.player.getInventory().getStack(i).isStackable()) {
                ItemStack is = mc.player.getInventory().getStack(i);
                if (is.getItem() instanceof BlockItem block && this.isValid(block)) {
                    blockCount += is.getCount();
                }
            }
        }

        return blockCount;
    }

    public int getBlockCountHotbar() {
        if (mc.player == null) {
            return 0;
        } else {
            int blockCount = 0;

            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && this.isValid(blockItem)) {
                    blockCount += stack.getCount();
                }
            }

            ItemStack offhandStack = mc.player.getOffHandStack();
            if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem offhandBlock && this.isValid(offhandBlock)) {
                blockCount += offhandStack.getCount();
            }

            return blockCount;
        }
    }

    private BlockData getBlockData(BlockPos pos) {
        BlockData data;
        if (this.getPos(pos) == null) {
            BlockPos blockPos = this.getBlockPos();
            if (blockPos == null) {
                return null;
            }

            Direction direction = this.getPlaceSide(blockPos);
            if (direction == null) {
                return null;
            }

            data = new BlockData(blockPos, direction);
        } else {
            data = this.getPos(pos);
        }

        return ClientRayTraceUtil.isIgnoredBlock(mc.world.getBlockState(data.pos().offset(data.facing()))) ? data : null;
    }

    private Direction getPlaceSide(BlockPos blockPos) {
        List<BlockData> blockData = new ArrayList<>();
        BlockPos pos = new BlockPos(
                (int)Math.floor(mc.player.getX()), (int)Math.floor(mc.player.getY()), (int)Math.floor(mc.player.getZ())
        );
        if (this.isAirBlock(blockPos.east()) && !blockPos.east().equals(pos)) {
            blockData.add(new BlockData(blockPos.east(), Direction.EAST));
        }

        if (this.isAirBlock(blockPos.north()) && !blockPos.north().equals(pos)) {
            blockData.add(new BlockData(blockPos.north(), Direction.NORTH));
        }

        if (this.isAirBlock(blockPos.south()) && !blockPos.south().equals(pos)) {
            blockData.add(new BlockData(blockPos.south(), Direction.SOUTH));
        }

        if (this.isAirBlock(blockPos.west()) && !blockPos.west().equals(pos)) {
            blockData.add(new BlockData(blockPos.west(), Direction.WEST));
        }

        if (blockData.isEmpty()) {
            return null;
        } else {
            blockData.sort(Comparator.comparingDouble(vec3 -> vec3.pos().getSquaredDistance(pos)));
            blockData.removeIf(blockData1 -> !ClientRayTraceUtil.isIgnoredBlock(mc.world.getBlockState(blockData1.pos().offset(blockData1.facing()))));
            return ((BlockData)blockData.getFirst()).facing();
        }
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(
                (int)Math.floor(mc.player.getX()), (int)Math.floor(mc.player.getY()), (int)Math.floor(mc.player.getZ())
        );
        ArrayList<BlockPos> positions = new ArrayList<>();
        Map<BlockPos, Block> searchBlock = this.searchBlocks(5);

        for (Entry<BlockPos, Block> block : searchBlock.entrySet()) {
            if (this.isPosSolid(block.getKey())) {
                positions.add(block.getKey());
            }
        }

        positions.removeIf(pos -> pos.getY() >= playerPos.getY());
        if (positions.isEmpty()) {
            return null;
        } else {
            positions.sort(Comparator.comparingDouble(vec3 -> vec3.getSquaredDistance(playerPos)));
            return (BlockPos)positions.getFirst();
        }
    }

    public boolean isAirBlock(BlockPos blockPos) {
        return ClientRayTraceUtil.isIgnoredBlock(mc.world.getBlockState(blockPos));
    }

    public float distanceTo(Entity entity, BlockPos pos) {
        float f = (float)(entity.getX() - pos.getX());
        float g = (float)(entity.getY() - pos.getY());
        float h = (float)(entity.getZ() - pos.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public Map<BlockPos, Block> searchBlocks(int radius) {
        Map<BlockPos, Block> blocks = new HashMap<>();
        PlayerEntity player = mc.player;
        if (player == null) {
            return blocks;
        } else {
            for (int x = radius; x >= -radius + 1; x--) {
                for (int y = radius; y >= -radius + 1; y--) {
                    for (int z = radius; z >= -radius + 1; z--) {
                        BlockPos blockPos = new BlockPos(player.getBlockX() + x, player.getBlockY() + y, player.getBlockZ() + z);
                        Block block = this.getBlock(blockPos);
                        if (block != null) {
                            blocks.put(blockPos, block);
                        }
                    }
                }
            }

            return blocks;
        }
    }

    public BlockData getPos(BlockPos pos) {
        if (this.isPosSolid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), Direction.EAST);
        } else if (this.isPosSolid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), Direction.WEST);
        } else if (this.isPosSolid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), Direction.NORTH);
        } else if (this.isPosSolid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), Direction.SOUTH);
        } else {
            return this.isPosSolid(pos.add(0, -1, 0)) ? new BlockData(pos.add(0, -1, 0), Direction.UP) : null;
        }
    }

    public boolean isPosSolid(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return !(block instanceof TrapdoorBlock) && !(block instanceof DoorBlock) && !(block instanceof FenceGateBlock)
                ? !Arrays.asList(
                        Blocks.ANVIL,
                        Blocks.AIR,
                        Blocks.WATER,
                        Blocks.FIRE,
                        Blocks.LAVA,
                        Blocks.SKELETON_SKULL,
                        Blocks.OAK_SIGN,
                        Blocks.TRAPPED_CHEST,
                        Blocks.CHEST,
                        Blocks.ENCHANTING_TABLE,
                        Blocks.ENDER_CHEST,
                        Blocks.CRAFTING_TABLE,
                        Blocks.DAYLIGHT_DETECTOR,
                        Blocks.COBWEB,
                        Blocks.SHORT_GRASS,
                        Blocks.FLOWER_POT,
                        Blocks.CHORUS_FLOWER,
                        Blocks.SUNFLOWER,
                        Blocks.CORNFLOWER,
                        Blocks.TORCHFLOWER,
                        Blocks.OAK_BUTTON,
                        Blocks.ACACIA_BUTTON,
                        Blocks.BIRCH_BUTTON,
                        Blocks.CRIMSON_BUTTON,
                        Blocks.CHERRY_BUTTON,
                        Blocks.DARK_OAK_BUTTON,
                        Blocks.JUNGLE_BUTTON,
                        Blocks.STONE_BUTTON,
                        Blocks.WARPED_BUTTON,
                        Blocks.SPRUCE_BUTTON,
                        Blocks.NOTE_BLOCK,
                        Blocks.PLAYER_HEAD
                )
                .contains(block)
                && !ClientRayTraceUtil.isIgnoredBlock(mc.world.getBlockState(pos))
                : false;
    }

    public void rotationAbuse(float step, float targetYaw) {
        double change = RotationUtils.yawDiffDirectly(RotationUtils.getLastRotation().yaw, targetYaw);
        int times = (int)(Math.abs(change) / step);
        float currentYaw = RotationUtils.getLastRotation().yaw;

        for (int i = 0; i < times; i++) {
            float smooth = RotationUtils.smooth((float)change, step);
            currentYaw += smooth;
            RotationUtils.rotation.yaw = currentYaw;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            PacketOrderManager.rightClicking = true;
        }

        RotationUtils.rotation.yaw = targetYaw;
    }

    @Override
    public int rotationPriority() {
        return 5;
    }

    record SlotData(int slot, Hand hand) {
        public boolean check() {
            if (Scaffold.mc.player == null) {
                return false;
            } else if (this.hand.equals(Hand.OFF_HAND)) {
                ItemStack stack = Scaffold.mc.player.getOffHandStack();
                return stack.isEmpty() || !(stack.getItem() instanceof BlockItem);
            } else {
                return Scaffold.mc.player.getInventory().getStack(this.slot).isEmpty()
                        || !(Scaffold.mc.player.getInventory().getStack(this.slot).getItem() instanceof BlockItem);
            }
        }
    }
}
