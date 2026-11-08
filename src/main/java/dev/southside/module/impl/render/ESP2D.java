package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import dev.southside.event.events.Render2DEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.render.ProjectionUtil;
import dev.southside.utils.render.RenderUtils;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1427;
import net.minecraft.class_1429;
import net.minecraft.class_1542;
import net.minecraft.class_1621;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import org.joml.Vector4d;

public class ESP2D
extends Module {
    public final Switch players = new Switch("Players", true);
    public final Switch animals = new Switch("Animals", true);
    public final Switch mobs = new Switch("Mobs", false);
    public final Switch invisible = new Switch("Invisible", false);
    public final Switch passives = new Switch("Passives", true);
    public final Switch droppedItems = new Switch("Dropped Items", false);
    public final Switch health = new Switch("Health Bar", true);
    public final Switch tags = new Switch("Tags", true);
    public final Switch filled = new Switch("Filled", true);
    public final Switch box = new Switch("Box", true);
    public final Dropdown boxMode = new Dropdown("BoxMode", "Box", "Box", "Corners");
    public final Slider tagSize = new Slider("Tag Size", (Number)5.0, (Number)1.0, (Number)20.0, 0.1);
    public final Slider thickness = new Slider("Thickness", (Number)Float.valueOf(1.5f), (Number)Float.valueOf(0.25f), (Number)Float.valueOf(5.0f), Float.valueOf(0.25f));
    private final int black = new Color(0, 0, 0, 150).getRGB();

    public ESP2D() {
        super(Category.Render, "ESP2D", "\u5565\u90fd\u89c6\u5978 (2D\u7248\u672c)");
    }

    @Override
    public void initPostRunnable() {
        super.initPostRunnable();
        this.hideIf(this.boxMode.getLabel(), () -> (Boolean)this.box.getValue() == false);
        this.hideIf(this.tagSize.getLabel(), () -> (Boolean)this.tags.getValue() == false);
    }

    @EventHandler
    public final void onRender2DEvent(Render2DEvent event) {
        NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        nanoVGHelper.setupAndDraw(true, vg -> {
            double scaleFactor = mc.method_22683().method_4495();
            float scaling = (float)(scaleFactor / Math.pow(scaleFactor, 2.0));
            for (class_1297 entity : ESP2D.mc.field_1687.method_18112()) {
                if (!this.isValid(entity) || !RenderUtils.isInViewFrustrum(entity)) continue;
                class_243 lerpedPos = entity.method_30950(event.getRenderTick());
                double x = lerpedPos.field_1352;
                double y = lerpedPos.field_1351;
                double z = lerpedPos.field_1350;
                double width = (double)entity.method_17681() / 1.5;
                double height = (double)entity.method_17682() + (entity.method_5715() ? -0.3 : 0.2);
                class_238 aabb = new class_238(x - width, y, z - width, x + width, y + height + 0.05, z + width);
                List<class_243> vectors = Arrays.asList(new class_243(aabb.field_1323, aabb.field_1322, aabb.field_1321), new class_243(aabb.field_1323, aabb.field_1325, aabb.field_1321), new class_243(aabb.field_1320, aabb.field_1322, aabb.field_1321), new class_243(aabb.field_1320, aabb.field_1325, aabb.field_1321), new class_243(aabb.field_1323, aabb.field_1322, aabb.field_1324), new class_243(aabb.field_1323, aabb.field_1325, aabb.field_1324), new class_243(aabb.field_1320, aabb.field_1322, aabb.field_1324), new class_243(aabb.field_1320, aabb.field_1325, aabb.field_1324));
                Vector4d position = null;
                for (class_243 vector : vectors) {
                    if ((vector = ProjectionUtil.project(vector)) == null || !(vector.field_1350 >= 0.0) || !(vector.field_1350 < 1.0)) continue;
                    if (position == null) {
                        position = new Vector4d(vector.field_1352, vector.field_1351, vector.field_1352, vector.field_1351);
                    }
                    position.x = Math.min(vector.field_1352, position.x);
                    position.y = Math.min(vector.field_1351, position.y);
                    position.z = Math.max(vector.field_1352, position.z);
                    position.w = Math.max(vector.field_1351, position.w);
                }
                if (ESP2D.mc.field_1724.method_5739(entity) < 1.0f || position == null) continue;
                Color clr = new Color(this.getColor());
                double posX = position.x;
                double posY = position.y;
                double endPosX = position.z;
                double endPosY = position.w;
                double thickness2 = ((Number)this.thickness.getValue()).doubleValue();
                if (((Boolean)this.health.getValue()).booleanValue() && entity instanceof class_1309) {
                    class_1309 livingBase = (class_1309)entity;
                    double hpPercentage = livingBase.method_6032() / livingBase.method_6063();
                    if (hpPercentage > 1.0) {
                        hpPercentage = 1.0;
                    } else if (hpPercentage < 0.0) {
                        hpPercentage = 0.0;
                    }
                    float health = livingBase.method_6032();
                    double hpHeight = (endPosY - posY) * hpPercentage;
                    double difference = posY - endPosY + 0.5;
                    if (health > 0.0f) {
                        nanoVGHelper.drawOutline(vg, posX - 4.0, posY - 0.5, 2.0, endPosY - posY + 0.5, 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 4.0, posY - 0.5, posX - 2.0, endPosY + 0.5, new Color(0, 0, 0, 70).getRGB());
                        nanoVGHelper.drawRect1(vg, posX - 3.5, endPosY - hpHeight, posX - 2.5, endPosY, this.getHealthColor(livingBase));
                    }
                    if (-difference > 50.0) {
                        for (int i = 1; i < 10; ++i) {
                            double increment = difference / 10.0 * (double)i;
                            nanoVGHelper.drawRect1(vg, posX - 3.5, endPosY - 0.5 + increment, posX - 2.5, endPosY - 0.5 + increment - 1.0, this.black);
                        }
                    }
                }
                int color2 = new Color(255, 255, 255).getRGB();
                if (((Boolean)this.tags.getValue()).booleanValue()) {
                    String name;
                    Font wqy = Fonts.WQY;
                    float size = ((Number)this.tagSize.getValue()).floatValue();
                    double dif = (endPosX - posX) / 2.0;
                    if (entity instanceof class_1542) {
                        class_1542 item = (class_1542)entity;
                        name = item.method_6983().method_7964().getString();
                    } else {
                        name = entity.method_5476().getString();
                    }
                    float textX = (float)(posX + dif) - nanoVGHelper.getTextWidth(vg, name, size, wqy) / 2.0f;
                    float textY = (float)(posY - 12.0 + 2.0);
                    nanoVGHelper.drawRect(vg, textX, textY, nanoVGHelper.getTextWidth(vg, name, size, wqy), nanoVGHelper.getTextHeight(vg, size, wqy), new Color(0, 0, 0, 100).getRGB());
                    nanoVGHelper.drawRawTextWithFormatting(vg, name, textX, textY, -1, size, wqy);
                }
                if (((Boolean)this.filled.getValue()).booleanValue()) {
                    nanoVGHelper.drawRect1(vg, posX, posY, endPosX, endPosY, new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 120).getRGB());
                }
                if (!((Boolean)this.box.getValue()).booleanValue()) continue;
                switch (this.boxMode.getMode()) {
                    case "Box": {
                        nanoVGHelper.drawRect1(vg, posX - 1.0, posY, posX + thickness2, endPosY + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 1.0, posY - 0.5, endPosX + 0.5, posY + 0.5 + thickness2, this.black);
                        nanoVGHelper.drawRect1(vg, endPosX - 0.5 - thickness2, posY, endPosX + 0.5, endPosY + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 1.0, endPosY - thickness2 - 0.5, endPosX + 0.5, endPosY + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 0.5, posY, posX + thickness2 - 0.5, endPosY, color2);
                        nanoVGHelper.drawRect1(vg, posX, endPosY - thickness2, endPosX, endPosY, color2);
                        nanoVGHelper.drawRect1(vg, posX - 0.5, posY, endPosX, posY + thickness2, color2);
                        nanoVGHelper.drawRect1(vg, endPosX - thickness2, posY, endPosX, endPosY, color2);
                        break;
                    }
                    case "Corners": {
                        nanoVGHelper.drawRect1(vg, posX + 0.5, posY, posX - 1.0, posY + (endPosY - posY) / 4.0 + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 1.0, endPosY, posX + 0.5, endPosY - (endPosY - posY) / 4.0 - 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 1.0, posY - 0.5, posX + (endPosX - posX) / 3.0 + 0.5, posY + 1.0, this.black);
                        nanoVGHelper.drawRect1(vg, endPosX - (endPosX - posX) / 3.0 - 0.5, posY - 0.5, endPosX, posY + 1.0, this.black);
                        nanoVGHelper.drawRect1(vg, endPosX - 1.0, posY, endPosX + 0.5, posY + (endPosY - posY) / 4.0 + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, endPosX - 1.0, endPosY, endPosX + 0.5, endPosY - (endPosY - posY) / 4.0 - 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX - 1.0, endPosY - 1.0, posX + (endPosX - posX) / 3.0 + 0.5, endPosY + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, endPosX - (endPosX - posX) / 3.0 - 0.5, endPosY - 1.0, endPosX + 0.5, endPosY + 0.5, this.black);
                        nanoVGHelper.drawRect1(vg, posX, posY, posX - 0.5, posY + (endPosY - posY) / 4.0, color2);
                        nanoVGHelper.drawRect1(vg, posX, endPosY, posX - 0.5, endPosY - (endPosY - posY) / 4.0, color2);
                        nanoVGHelper.drawRect1(vg, posX - 0.5, posY, posX + (endPosX - posX) / 3.0, posY + 0.5, color2);
                        nanoVGHelper.drawRect1(vg, endPosX - (endPosX - posX) / 3.0, posY, endPosX, posY + 0.5, color2);
                        nanoVGHelper.drawRect1(vg, endPosX - 0.5, posY, endPosX, posY + (endPosY - posY) / 4.0, color2);
                        nanoVGHelper.drawRect1(vg, endPosX - 0.5, endPosY, endPosX, endPosY - (endPosY - posY) / 4.0, color2);
                        nanoVGHelper.drawRect1(vg, posX, endPosY - 0.5, posX + (endPosX - posX) / 3.0, endPosY, color2);
                        nanoVGHelper.drawRect1(vg, endPosX - (endPosX - posX) / 3.0, endPosY - 0.5, endPosX - 0.5, endPosY, color2);
                    }
                }
            }
        });
    }

    private int getColor() {
        return new Color(255, 255, 255).getRGB();
    }

    private boolean isValid(class_1297 entity) {
        if (entity instanceof class_1309) {
            class_1309 livingBase = (class_1309)entity;
            return this.isValidType(livingBase) && entity.method_5805() && (!entity.method_5767() || (Boolean)this.invisible.getValue() != false);
        }
        return (Boolean)this.droppedItems.getValue() != false && entity instanceof class_1542;
    }

    private boolean isValidType(class_1309 entity) {
        return (Boolean)this.players.getValue() != false && entity instanceof class_1657 || (Boolean)this.mobs.getValue() != false && (entity instanceof class_1308 || entity instanceof class_1621) || (Boolean)this.passives.getValue() != false && (entity instanceof class_1646 || entity instanceof class_1427) || (Boolean)this.animals.getValue() != false && entity instanceof class_1429;
    }

    private int getHealthColor(class_1309 player) {
        return Color.HSBtoRGB(Math.max(0.0f, Math.min(player.method_6032(), player.method_6063()) / player.method_6063()) / 3.0f, 1.0f, 0.8f) | 0xFF000000;
    }
}

