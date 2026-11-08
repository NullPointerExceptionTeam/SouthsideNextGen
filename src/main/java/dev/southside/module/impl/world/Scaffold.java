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
import net.minecraft.class_10185;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2189;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2533;
import net.minecraft.class_2846;
import net.minecraft.class_2879;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_2846.class_2847;
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
    public static final List<class_2248> invalidBlocks = Arrays.asList(
            class_2246.field_10485,
            class_2246.field_10121,
            class_2246.field_10034,
            class_2246.field_10443,
            class_2246.field_10380,
            class_2246.field_10535,
            class_2246.field_10102,
            class_2246.field_10343,
            class_2246.field_10336,
            class_2246.field_9980,
            class_2246.field_10181,
            class_2246.field_27097,
            class_2246.field_10200,
            class_2246.field_10158,
            class_2246.field_40284,
            class_2246.field_10179,
            class_2246.field_10228,
            class_2246.field_10375,
            class_2246.field_10523,
            class_2246.field_10429,
            class_2246.field_10121,
            class_2246.field_10231,
            class_2246.field_10411,
            class_2246.field_10544,
            class_2246.field_10284,
            class_2246.field_10330,
            class_2246.field_37554,
            class_2246.field_42735,
            class_2246.field_40277,
            class_2246.field_22104,
            class_2246.field_22105,
            class_2246.field_40262,
            class_2246.field_40264,
            class_2246.field_40263,
            class_2246.field_40266,
            class_2246.field_40265,
            class_2246.field_40267,
            class_2246.field_40270,
            class_2246.field_42738,
            class_2246.field_40271,
            class_2246.field_40268,
            class_2246.field_40269
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
    private class_2338 lastPlacePosition = null;
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
        if (mc.field_1724 != null) {
            if (this.blockFly.getValue()) {
                ServerPacketManager.reset(true);
            }

            this.boxExpand = RandomUtils.nextDouble(0.1, 0.2);
            this.lastRotation = new Rotation(mc.field_1724.method_36454(), mc.field_1724.method_36455());
            this.slot = new Scaffold.SlotData(mc.field_1724.method_31548().field_7545, class_1268.field_5808);
            this.oldSlot = mc.field_1724.method_31548().field_7545;
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
        if (mc.field_1724 != null) {
            mc.field_1724.method_31548().field_7545 = this.slot.slot();
            mc.field_1724.method_31548().field_7545 = this.oldSlot;
            mc.field_1690.field_1832.method_23481(false);
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
            class_238 box = new class_238(this.lastPlacePosition);
            Render3D.boxLines(box, this.markLineColor.getValue().toJavaColor());
            Render3D.boxSides(box, this.markSideColor.getValue().toJavaColor());
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (this.isEnabled() && mc.field_1724 != null && this.blockCount.getValue()) {
            int newCount = Math.max(0, this.getBlockCountHotbar());
            if (newCount > this.startHotbarCount) {
                this.startHotbarCount = newCount;
            }

            float rate = class_3532.method_15363((float)newCount / this.startHotbarCount, 0.0F, 1.0F);
            float centerX = mc.method_22683().method_4486() / 2.0F;
            float centerY = mc.method_22683().method_4502() / 2.0F;
            float y = centerY + 15.0F + this.blockCountOffset.getValue().floatValue();
            if (this.blockCountStyle.isMode("Old")) {
                String text = newCount + " Blocks";
                int x = Math.round(centerX - mc.field_1772.method_1727(text) / 2.0F);
                event.getDrawContext().method_51433(mc.field_1772, text, x, Math.round(y), this.getBlockCountColor(newCount), true);
                this.lastCount = newCount;
                this.count = newCount;
            } else {
                if (this.blockCountStyle.isMode("Retro")) {
                    class_1799 displayStack = this.getHeldBlockStack();
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
                    if (!displayStack.method_7960() && this.getBlockCountHotbar() > 0) {
                        class_4587 matrices = event.getDrawContext().method_51448();
                        matrices.method_22903();
                        matrices.method_46416(itemX, itemY, 0.0F);
                        matrices.method_22905(itemScale, itemScale, 1.0F);
                        event.getDrawContext().method_51427(displayStack, 0, 0);
                        matrices.method_22909();
                    } else {
                        class_4587 matrices = event.getDrawContext().method_51448();
                        matrices.method_22903();
                        matrices.method_46416(itemX, itemY, 0.0F);
                        matrices.method_22905(itemScale, itemScale, 1.0F);
                        event.getDrawContext().method_51427(new class_1799(class_1802.field_8077), 0, 0);
                        matrices.method_22909();
                    }
                }

                if (this.blockCountStyle.isMode("Dark")) {
                    class_1799 displayStack = this.getHeldBlockStack();
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
                    if (!displayStack.method_7960() && this.getBlockCountHotbar() > 0) {
                        class_4587 matrices = event.getDrawContext().method_51448();
                        matrices.method_22903();
                        matrices.method_46416(itemX, itemY, 0.0F);
                        matrices.method_22905(itemScale, itemScale, 1.0F);
                        event.getDrawContext().method_51427(displayStack, 0, 0);
                        matrices.method_22909();
                    } else {
                        class_4587 matrices = event.getDrawContext().method_51448();
                        matrices.method_22903();
                        matrices.method_46416(itemX, itemY, 0.0F);
                        matrices.method_22905(itemScale, itemScale, 1.0F);
                        event.getDrawContext().method_51427(new class_1799(class_1802.field_8077), 0, 0);
                        matrices.method_22909();
                    }
                }

                this.lastCount = newCount;
                this.count = newCount;
            }
        }
    }

    @EventHandler
    public void onHeldItem(UpdateHeldItemEvent event) {
        if (this.spoofItem.getValue() && event.getHand() == class_1268.field_5808) {
            event.setItem(mc.field_1724.method_31548().method_5438(this.oldSlot));
        }
    }

    @EventHandler
    public void onMoveInput(StrafeEvent event) {
        this.setSuffix(this.mode.getMode());
        if (this.blockSlot != null && !this.blockSlot.check()) {
            if (PlayerUtils.onGroundTicks > (this.smoothed.getValue() && this.safeMode.getValue() && !this.testOnGround.getValue() ? 1 : 0)
                    && !mc.field_1690.field_1903.method_1434()
                    && PlayerUtils.isMoving()
                    && this.mode.isMode("Telly")) {
                String var2 = this.jumpMode.getMode();
                switch (var2) {
                    case "None":
                    default:
                        break;
                    case "Normal":
                        mc.field_1724.method_6043();
                        this.spoofJumpInput();
                        break;
                    case "Parkour":
                        double yaw = Math.toRadians(mc.field_1724.method_36454());
                        double forwardX = -Math.sin(yaw);
                        double forwardZ = Math.cos(yaw);
                        class_2338 frontPos1 = new class_2338(
                                (int)(mc.field_1724.method_23317() + forwardX), (int)(mc.field_1724.method_23318() - 0.1), (int)(mc.field_1724.method_23321() + forwardZ)
                        );
                        class_2338 frontPos2 = new class_2338(
                                (int)(mc.field_1724.method_23317() + forwardX * 2.0),
                                (int)(mc.field_1724.method_23318() - 0.1),
                                (int)(mc.field_1724.method_23321() + forwardZ * 2.0)
                        );
                        if (mc.field_1687.method_8320(frontPos1).method_26204() instanceof class_2189
                                || mc.field_1687.method_8320(frontPos2).method_26204() instanceof class_2189) {
                            mc.field_1724.method_6043();
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
        if (mc.field_1724 != null) {
            class_10185 current = mc.field_1724.field_3913.field_54155;

            mc.field_1724.field_3913.field_54155 = new class_10185(
                    current.comp_3159(), current.comp_3160(), current.comp_3161(), current.comp_3162(), true, current.comp_3164(), current.comp_3165()
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
            if (RotationUtils.normalizeYawDiff(mc.field_1724.method_36454() + 100.0F, RotationUtils.getServerRotation().yaw)
                    < RotationUtils.normalizeYawDiff(mc.field_1724.method_36454() - 100.0F, RotationUtils.getServerRotation().yaw)) {
                rotation = new Rotation(mc.field_1724.method_36454() + 100.0F, RotationUtils.getServerRotation().pitch);
            } else {
                rotation = new Rotation(mc.field_1724.method_36454() - 100.0F, RotationUtils.getServerRotation().pitch);
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
                if (mc.field_1690.field_1903.method_1434() && this.noUptelly.getValue()) {
                    return rotation;
                }

                if (mc.field_1690.field_1903.method_1434() && this.randomSlow.getValue()) {
                    this.ups++;
                    if (this.ups % 2 == 0) {
                        return rotation;
                    }
                }

                if (!this.smoothed.getValue() || PlayerUtils.offGroundTicks >= this.rotTick.getValue().intValue() && !this.safeMode.getValue()) {
                    if (this.snap.getValue() && mc.field_1690.field_1903.method_1434()) {
                        if (this.lastBlockData == null || PlayerUtils.offGroundTicks < this.rotTick.getValue().intValue()) {
                            return new Rotation(mc.field_1724.method_36454(), 85.0F + (float)Math.random());
                        }
                    } else if (PlayerUtils.offGroundTicks < this.rotTick.getValue().intValue()) {
                        return new Rotation(mc.field_1724.method_36454(), 85.0F + (float)Math.random());
                    }
                } else if (PlayerUtils.onGroundTicks > 0) {
                    if (!this.safeMode.getValue() || this.testOnGround.getValue() && !mc.field_1690.field_1903.method_1434()) {
                        return new Rotation(mc.field_1724.method_36454(), 75.5F);
                    }

                    switch (PlayerUtils.onGroundTicks) {
                        case 1:
                            if (!forceRotation) {
                                rotation.yaw = RotationUtils.getServerRotation().getYaw() + RotationUtils.smooth((float)diff, (float)(diff / 2.0));
                                rotation.pitch = 75.5F;
                            } else if (this.blockData != null) {
                                rotation = RotationUtils.getClosestToBlockFace(
                                        this.blockData.pos(), this.blockData.facing(), mc.field_1724.method_36454(), RotationUtils.getServerRotation().pitch
                                );
                            }

                            mc.field_1724.field_6228 = 2;
                            break;
                        case 2:
                            return new Rotation(mc.field_1724.method_36454(), 75.5F);
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
                    mc.field_1724, this.lastRotation.yaw, this.lastRotation.pitch, this.blockData.pos(), this.blockData.facing(), true
            )) {
                return this.lastRotation;
            } else if (this.blockData != null
                    && !this.alwaysUpdateRot.getValue()
                    && PlayerUtils.offGroundTicks >= this.rotTick.getValue().intValue()
                    && !ClientRayTraceUtil.didHitBlockFace(mc.field_1724, rotation.yaw, rotation.pitch, this.blockData.pos(), this.blockData.facing(), true)
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

            if (mc.field_1761 == null) {
                return;
            }

            if (!this.canPlace) {
                return;
            }

            class_3965 block = ClientRayTraceUtil.getFacedBlock(RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch);
            if (!ClientRayTraceUtil.didHitBlockFace(
                    mc.field_1724, RotationUtils.getRotation().yaw, RotationUtils.getRotation().pitch, this.blockData.pos(), this.blockData.facing(), true
            )) {
                return;
            }

            if (this.blockSlot.hand().equals(class_1268.field_5808)) {
                mc.field_1724.method_31548().field_7545 = this.blockSlot.slot();
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
                    PacketUtils.sendPacketNoEvent(new class_2846(class_2847.field_12969, new class_2338(0, 0, 0), class_2350.field_11033));
                    PacketOrderManager.swap = true;
                }
            }

            if (this.interactItem.getValue()) {
                mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            }

            class_1269 result = mc.field_1761.method_2896(mc.field_1724, this.blockSlot.hand(), block);
            PacketOrderManager.rightClicking = true;
            if (result == class_1269.field_5812) {
                this.placeCount++;
                this.lastPlacePosition = this.blockData.pos().method_10093(this.blockData.facing());
                if (PlayerUtils.lastPitchDiff > 0.0) {
                    PlayerUtils.lastPlacePitchDiff = PlayerUtils.lastPitchDiff;
                }

                if (this.noSwing.getValue()) {
                    PacketUtils.sendPacket(new class_2879(this.blockSlot.hand()));
                } else {
                    mc.field_1724.method_6104(this.blockSlot.hand());
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
        if (InventoryUtil.isFullBlock(mc.field_1724.method_6079()) && this.isValid(mc.field_1724.method_6079().method_7909())) {
            this.blockSlot = new Scaffold.SlotData(45, class_1268.field_5810);
        }

        if (this.blockSlot == null
                && !this.blockSlotMode.isMode("Most Blocks")
                && InventoryUtil.isFullBlock(mc.field_1724.method_6047())
                && this.isValid(mc.field_1724.method_6047().method_7909())) {
            this.blockSlot = new Scaffold.SlotData(mc.field_1724.method_31548().field_7545, class_1268.field_5808);
        }

        if (this.blockSlot == null) {
            int hotbarSlot = this.getHotbarBlockSlot();
            if (hotbarSlot != -1) {
                this.blockSlot = new Scaffold.SlotData(hotbarSlot, class_1268.field_5808);
            }
        }

        if (this.blockSlot != null && !this.blockSlot.check()) {
            if (RotationUtils.accepted(this.rotationPriority())) {
                if (mc.field_1724.method_24828()) {
                    this.posY = Math.floor(mc.field_1724.method_23318() - 1.0);
                }

                if (mc.field_1690.field_1903.method_1434()) {
                    this.posY = mc.field_1724.method_31478() - 1;
                }

                BlockData possible = ClientRayTraceUtil.isIgnoredBlock(
                        mc.field_1687
                                .method_8320(
                                        new class_2338(
                                                (int)Math.floor(mc.field_1724.method_23317()),
                                                (int)Math.floor(mc.field_1724.method_23318()),
                                                (int)Math.floor(mc.field_1724.method_23321())
                                        )
                                )
                )
                        ? this.getBlockData(new class_2338((int)Math.floor(mc.field_1724.method_23317()), (int)this.posY, (int)Math.floor(mc.field_1724.method_23321())))
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
                    if (this.safeMode.getValue() && this.testOnGround.getValue() && !this.canPlace && mc.field_1690.field_1903.method_1434()) {
                        this.canPlace = PlayerUtils.onGroundTicks == 1;
                    }
                }

                if (this.blockSlot.hand().equals(class_1268.field_5808)) {
                    mc.field_1724.method_31548().field_7545 = this.blockSlot.slot;
                }

                FallingPlayer fallingPlayer = new FallingPlayer(mc.field_1724);
                boolean reachable = true;
                fallingPlayer.calculate(1);
                class_243 nextEyePos = fallingPlayer.getEyePos();
                fallingPlayer.calculate(1);
                BlockData placement = this.getBlockData(
                        new class_2338((int)Math.floor(mc.field_1724.method_23317()), mc.field_1724.method_31478() - 1, (int)Math.floor(mc.field_1724.method_23321()))
                );
                boolean forceRotation = false;
                if (placement != null) {
                    if (this.safeMode.getValue() && this.testOnGround.getValue() && PlayerUtils.onGroundTicks == 1 && mc.field_1690.field_1903.method_1434()) {
                        forceRotation = true;
                    }

                    double distance = nextEyePos.method_1022(placement.pos().method_46558());
                    if (distance >= this.safeDistance.getDouble() || placement.pos().method_10264() > fallingPlayer.getY()) {
                        this.canPlace = true;
                        reachable = false;
                        this.blockData = this.lastBlockData = placement;
                    }
                }

                if (this.blockData != null) {
                    class_238 box = new class_238(this.blockData.pos())
                            .method_35575(this.blockData.pos().method_10264() - 1)
                            .method_35578(this.blockData.pos().method_10264() + 1);
                    if (this.blockData.pos().method_10264() > fallingPlayer.getY() && !box.method_1006(mc.field_1724.method_19538())) {
                        this.canPlace = true;
                        reachable = false;
                        this.posY = mc.field_1724.method_31478() - 1;
                        this.blockData = this.lastBlockData = this.getBlockData(
                                new class_2338((int)Math.floor(mc.field_1724.method_23317()), (int)Math.floor(this.posY), (int)Math.floor(mc.field_1724.method_23321()))
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
                    this.rot.fixedSensitivity(((Double)mc.field_1690.method_42495().method_41753()).floatValue());
                }

                RotationUtils.setRotation(this.rot, this.rotationPriority(), MoveFixMode.Silent);
                if (this.blockData != null) {
                    if (mc.field_1724.method_7325()) {
                        this.setEnable(false);
                    }

                    if (!this.mode.isMode("Telly")) {
                        if (this.waitingForEagleSneak) {
                            this.tellyJumpTicks++;
                            if (this.tellyJumpTicks == this.tellyEagleTick.getValue().intValue() && !mc.field_1690.field_1832.method_1434()) {
                                mc.field_1690.field_1832.method_23481(true);
                            }

                            if (this.tellyJumpTicks == this.tellyEagleTick.getValue().intValue() + this.keepEagleSneakTick.getValue().intValue()) {
                                mc.field_1690.field_1832.method_23481(false);
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
                && mc.field_1690.field_1903.method_1434()) {
            mc.field_1724.field_3913.field_3905 *= 0.2F;
            mc.field_1724.field_3913.field_3907 *= 0.2F;
        }
    }

    public boolean doesNotContainBlock(int down) {
        return PlayerUtils.blockRelativeToPlayer(0.0, -down, 0.0).method_9564().method_26167();
    }

    public static class_243 getVec3(class_2338 pos, class_2350 face) {
        double x = pos.method_10263() + 0.5;
        double y = pos.method_10264() + 0.5;
        double z = pos.method_10260() + 0.5;
        if (face != class_2350.field_11036 && face != class_2350.field_11033) {
            y += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == class_2350.field_11039 || face == class_2350.field_11034) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }

        if (face == class_2350.field_11035 || face == class_2350.field_11043) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }

        return new class_243(x, y, z);
    }

    public void getBlock(int switchSlot) {
        for (int i = 9; i < 45; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7946() && mc.field_1755 == null) {
                class_1799 is = mc.field_1724.method_31548().method_5438(i);
                if (is.method_7909() instanceof class_1747 block && this.isValid(block)) {
                    if (36 + switchSlot != i) {
                        InventoryUtil.swap(i, switchSlot);
                    }
                    break;
                }
            }
        }
    }

    public Rotation getRotation(class_243 vec, class_243 eyes) {
        double diffX = vec.field_1352 - eyes.field_1352;
        double diffY = vec.field_1351 - eyes.field_1351;
        double diffZ = vec.field_1350 - eyes.field_1350;
        return new Rotation(
                class_3532.method_15393((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F),
                class_3532.method_15393((float)(-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)))))
        );
    }

    private boolean isValid(class_1792 item) {
        return item instanceof class_1747 && !invalidBlocks.contains(((class_1747)item).method_7711()) && SlotUtils.isGoodForBridging(item);
    }

    private int getHotbarBlockSlot() {
        if (this.blockSlotMode.isMode("Most Blocks")) {
            return this.getMostBlocksHotbarSlot();
        } else {
            int slot = -1;

            for (int i = 0; i <= 8; i++) {
                class_1799 stack = mc.field_1724.method_31548().method_5438(i);
                if (InventoryUtil.isFullBlock(stack) && this.isValid(stack.method_7909())) {
                    slot = i;
                }
            }

            return slot;
        }
    }

    private int getMostBlocksHotbarSlot() {
        int selectedSlot = mc.field_1724.method_31548().field_7545;
        int bestSlot = -1;
        int bestCount = -1;
        class_1799 selectedStack = mc.field_1724.method_31548().method_5438(selectedSlot);
        if (InventoryUtil.isFullBlock(selectedStack) && this.isValid(selectedStack.method_7909())) {
            bestSlot = selectedSlot;
            bestCount = selectedStack.method_7947();
        }

        for (int i = 0; i <= 8; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (InventoryUtil.isFullBlock(stack) && this.isValid(stack.method_7909()) && stack.method_7947() > bestCount) {
                bestSlot = i;
                bestCount = stack.method_7947();
            }
        }

        return bestSlot;
    }

    private class_1799 getHeldBlockStack() {
        if (mc.field_1724 == null) {
            return class_1799.field_8037;
        } else {
            if (this.blockSlot != null) {
                class_1799 blockSlotStack;
                if (this.blockSlot.hand() == class_1268.field_5810) {
                    blockSlotStack = mc.field_1724.method_6079();
                } else {
                    blockSlotStack = mc.field_1724.method_31548().method_5438(this.blockSlot.slot());
                }

                if (this.isDisplayBlock(blockSlotStack)) {
                    return blockSlotStack;
                }
            }

            class_1799 main = mc.field_1724.method_6047();
            if (this.isDisplayBlock(main)) {
                return main;
            } else {
                class_1799 offhand = mc.field_1724.method_6079();
                if (this.isDisplayBlock(offhand)) {
                    return offhand;
                } else {
                    int hotbarSlot = this.getHotbarBlockSlot();
                    return hotbarSlot != -1 ? mc.field_1724.method_31548().method_5438(hotbarSlot) : class_1799.field_8037;
                }
            }
        }
    }

    private boolean isDisplayBlock(class_1799 stack) {
        return stack != null && !stack.method_7960() ? this.isValid(stack.method_7909()) : false;
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
            if (mc.field_1724 == null) {
                return -1;
            }

            if (mc.field_1724.method_31548().method_5438(i).method_7946()) {
                class_1799 is = mc.field_1724.method_31548().method_5438(i);
                if (is.method_7909() instanceof class_1747 block && this.isValid(block)) {
                    blockCount += is.method_7947();
                }
            }
        }

        return blockCount;
    }

    public int getBlockCountHotbar() {
        if (mc.field_1724 == null) {
            return 0;
        } else {
            int blockCount = 0;

            for (int i = 0; i <= 8; i++) {
                class_1799 stack = mc.field_1724.method_31548().method_5438(i);
                if (!stack.method_7960() && stack.method_7909() instanceof class_1747 blockItem && this.isValid(blockItem)) {
                    blockCount += stack.method_7947();
                }
            }

            class_1799 offhandStack = mc.field_1724.method_6079();
            if (!offhandStack.method_7960() && offhandStack.method_7909() instanceof class_1747 offhandBlock && this.isValid(offhandBlock)) {
                blockCount += offhandStack.method_7947();
            }

            return blockCount;
        }
    }

    private BlockData getBlockData(class_2338 pos) {
        BlockData data;
        if (this.getPos(pos) == null) {
            class_2338 blockPos = this.getBlockPos();
            if (blockPos == null) {
                return null;
            }

            class_2350 direction = this.getPlaceSide(blockPos);
            if (direction == null) {
                return null;
            }

            data = new BlockData(blockPos, direction);
        } else {
            data = this.getPos(pos);
        }

        return ClientRayTraceUtil.isIgnoredBlock(mc.field_1687.method_8320(data.pos().method_10093(data.facing()))) ? data : null;
    }

    private class_2350 getPlaceSide(class_2338 blockPos) {
        List<BlockData> blockData = new ArrayList<>();
        class_2338 pos = new class_2338(
                (int)Math.floor(mc.field_1724.method_23317()), (int)Math.floor(mc.field_1724.method_23318()), (int)Math.floor(mc.field_1724.method_23321())
        );
        if (this.isAirBlock(blockPos.method_10078()) && !blockPos.method_10078().equals(pos)) {
            blockData.add(new BlockData(blockPos.method_10078(), class_2350.field_11034));
        }

        if (this.isAirBlock(blockPos.method_10095()) && !blockPos.method_10095().equals(pos)) {
            blockData.add(new BlockData(blockPos.method_10095(), class_2350.field_11043));
        }

        if (this.isAirBlock(blockPos.method_10072()) && !blockPos.method_10072().equals(pos)) {
            blockData.add(new BlockData(blockPos.method_10072(), class_2350.field_11035));
        }

        if (this.isAirBlock(blockPos.method_10067()) && !blockPos.method_10067().equals(pos)) {
            blockData.add(new BlockData(blockPos.method_10067(), class_2350.field_11039));
        }

        if (blockData.isEmpty()) {
            return null;
        } else {
            blockData.sort(Comparator.comparingDouble(vec3 -> vec3.pos().method_10262(pos)));
            blockData.removeIf(blockData1 -> !ClientRayTraceUtil.isIgnoredBlock(mc.field_1687.method_8320(blockData1.pos().method_10093(blockData1.facing()))));
            return ((BlockData)blockData.getFirst()).facing();
        }
    }

    private class_2338 getBlockPos() {
        class_2338 playerPos = new class_2338(
                (int)Math.floor(mc.field_1724.method_23317()), (int)Math.floor(mc.field_1724.method_23318()), (int)Math.floor(mc.field_1724.method_23321())
        );
        ArrayList<class_2338> positions = new ArrayList<>();
        Map<class_2338, class_2248> searchBlock = this.searchBlocks(5);

        for (Entry<class_2338, class_2248> block : searchBlock.entrySet()) {
            if (this.isPosSolid(block.getKey())) {
                positions.add(block.getKey());
            }
        }

        positions.removeIf(pos -> pos.method_10264() >= playerPos.method_10264());
        if (positions.isEmpty()) {
            return null;
        } else {
            positions.sort(Comparator.comparingDouble(vec3 -> vec3.method_10262(playerPos)));
            return (class_2338)positions.getFirst();
        }
    }

    public boolean isAirBlock(class_2338 blockPos) {
        return ClientRayTraceUtil.isIgnoredBlock(mc.field_1687.method_8320(blockPos));
    }

    public float distanceTo(class_1297 entity, class_2338 pos) {
        float f = (float)(entity.method_23317() - pos.method_10263());
        float g = (float)(entity.method_23318() - pos.method_10264());
        float h = (float)(entity.method_23321() - pos.method_10260());
        return class_3532.method_15355(f * f + g * g + h * h);
    }

    public class_2248 getBlock(class_2338 pos) {
        return mc.field_1687.method_8320(pos).method_26204();
    }

    public Map<class_2338, class_2248> searchBlocks(int radius) {
        Map<class_2338, class_2248> blocks = new HashMap<>();
        class_1657 player = mc.field_1724;
        if (player == null) {
            return blocks;
        } else {
            for (int x = radius; x >= -radius + 1; x--) {
                for (int y = radius; y >= -radius + 1; y--) {
                    for (int z = radius; z >= -radius + 1; z--) {
                        class_2338 blockPos = new class_2338(player.method_31477() + x, player.method_31478() + y, player.method_31479() + z);
                        class_2248 block = this.getBlock(blockPos);
                        if (block != null) {
                            blocks.put(blockPos, block);
                        }
                    }
                }
            }

            return blocks;
        }
    }

    public BlockData getPos(class_2338 pos) {
        if (this.isPosSolid(pos.method_10069(-1, 0, 0))) {
            return new BlockData(pos.method_10069(-1, 0, 0), class_2350.field_11034);
        } else if (this.isPosSolid(pos.method_10069(1, 0, 0))) {
            return new BlockData(pos.method_10069(1, 0, 0), class_2350.field_11039);
        } else if (this.isPosSolid(pos.method_10069(0, 0, 1))) {
            return new BlockData(pos.method_10069(0, 0, 1), class_2350.field_11043);
        } else if (this.isPosSolid(pos.method_10069(0, 0, -1))) {
            return new BlockData(pos.method_10069(0, 0, -1), class_2350.field_11035);
        } else {
            return this.isPosSolid(pos.method_10069(0, -1, 0)) ? new BlockData(pos.method_10069(0, -1, 0), class_2350.field_11036) : null;
        }
    }

    public boolean isPosSolid(class_2338 pos) {
        class_2248 block = mc.field_1687.method_8320(pos).method_26204();
        return !(block instanceof class_2533) && !(block instanceof class_2323) && !(block instanceof class_2349)
                ? !Arrays.asList(
                        class_2246.field_10535,
                        class_2246.field_10124,
                        class_2246.field_10382,
                        class_2246.field_10036,
                        class_2246.field_10164,
                        class_2246.field_10481,
                        class_2246.field_10121,
                        class_2246.field_10380,
                        class_2246.field_10034,
                        class_2246.field_10485,
                        class_2246.field_10443,
                        class_2246.field_9980,
                        class_2246.field_10429,
                        class_2246.field_10343,
                        class_2246.field_10479,
                        class_2246.field_10495,
                        class_2246.field_10528,
                        class_2246.field_10583,
                        class_2246.field_9995,
                        class_2246.field_42734,
                        class_2246.field_10057,
                        class_2246.field_10278,
                        class_2246.field_10417,
                        class_2246.field_22100,
                        class_2246.field_42743,
                        class_2246.field_10493,
                        class_2246.field_10553,
                        class_2246.field_10494,
                        class_2246.field_22101,
                        class_2246.field_10066,
                        class_2246.field_10179,
                        class_2246.field_10432
                )
                .contains(block)
                && !ClientRayTraceUtil.isIgnoredBlock(mc.field_1687.method_8320(pos))
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
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            PacketOrderManager.rightClicking = true;
        }

        RotationUtils.rotation.yaw = targetYaw;
    }

    @Override
    public int rotationPriority() {
        return 5;
    }

    record SlotData(int slot, class_1268 hand) {
        public boolean check() {
            if (Scaffold.mc.field_1724 == null) {
                return false;
            } else if (this.hand.equals(class_1268.field_5810)) {
                class_1799 stack = Scaffold.mc.field_1724.method_6079();
                return stack.method_7960() || !(stack.method_7909() instanceof class_1747);
            } else {
                return Scaffold.mc.field_1724.method_31548().method_5438(this.slot).method_7960()
                        || !(Scaffold.mc.field_1724.method_31548().method_5438(this.slot).method_7909() instanceof class_1747);
            }
        }
    }
}
