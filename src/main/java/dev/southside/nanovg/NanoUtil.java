package dev.southside.nanovg;

import dev.southside.nanovg.NanoLoader;
import dev.southside.nanovg.gl.state.States;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

public class NanoUtil {
    public static void beginFrame() {
        States.INSTANCE.push();
        NanoVG.nvgSave((long)NanoLoader.ctx);
        NanoVG.nvgBeginFrame((long)NanoLoader.ctx, (float)MinecraftClient.getInstance().getWindow().getFramebufferWidth(), (float)MinecraftClient.getInstance().getWindow().getFramebufferHeight(), (float)1.0f);
        NanoUtil.scaleStart(0.0f, 0.0f, (float)MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static void endFrame() {
        NanoUtil.scaleEnd();
        NanoVG.nvgEndFrame((long)NanoLoader.ctx);
        NanoVG.nvgRestore((long)NanoLoader.ctx);
        States.INSTANCE.pop();
    }

    public static void beginPath() {
        NanoVG.nvgBeginPath((long)NanoLoader.ctx);
    }

    public static void closePath() {
        NanoVG.nvgClosePath((long)NanoLoader.ctx);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        NanoUtil.beginPath();
        NanoVG.nvgRoundedRect((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height, (float)radius);
        NanoUtil.fillColor(color);
        NanoVG.nvgFill((long)NanoLoader.ctx);
        NanoUtil.closePath();
    }

    public static void drawRing(float x, float y, float outerRadius, float innerRadius, Color color) {
        if (innerRadius <= 0.0f || outerRadius <= innerRadius) {
            return;
        }
        NanoUtil.beginPath();
        NanoVG.nvgArc((long)NanoLoader.ctx, (float)x, (float)y, (float)outerRadius, (float)0.0f, (float)((float)Math.PI * 2), (int)2);
        NanoVG.nvgArc((long)NanoLoader.ctx, (float)x, (float)y, (float)innerRadius, (float)0.0f, (float)((float)Math.PI * 2), (int)1);
        NanoUtil.fillColor(color);
        NanoVG.nvgFill((long)NanoLoader.ctx);
        NanoUtil.closePath();
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
        NanoUtil.beginPath();
        NanoVG.nvgRect((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height);
        NanoUtil.fillColor(color);
        NanoVG.nvgFill((long)NanoLoader.ctx);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float leftTopRadius, float rightTopRadius, float rightBottomRadius, float leftBottomRadius, Color color) {
        NanoUtil.beginPath();
        NanoVG.nvgRoundedRectVarying((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height, (float)leftTopRadius, (float)rightTopRadius, (float)rightBottomRadius, (float)leftBottomRadius);
        NanoUtil.fillColor(color);
        NanoVG.nvgFill((long)NanoLoader.ctx);
    }

    public static NVGColor getColor(Color color) {
        NVGColor nvgColor = NVGColor.calloc();
        nvgColor.r((float)color.getRed() / 255.0f).g((float)color.getGreen() / 255.0f).b((float)color.getBlue() / 255.0f).a((float)color.getAlpha() / 255.0f);
        return nvgColor;
    }

    public static void drawGradientRect(float x, float y, float width, float height, Color left, Color right) {
        NVGPaint gradient = NanoUtil.createGradient(x, y, x + width, y + height, left, right);
        NanoUtil.beginPath();
        NanoVG.nvgRect((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height);
        NanoVG.nvgFillPaint((long)NanoLoader.ctx, (NVGPaint)gradient);
        NanoVG.nvgFill((long)NanoLoader.ctx);
    }

    public static void drawGradientRoundedRect(float x, float y, float width, float height, float radius, Color left, Color right) {
        NVGPaint gradient = NanoUtil.createGradient(x, y, x + width, y + height, left, right);
        NanoUtil.beginPath();
        NanoVG.nvgRoundedRect((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height, (float)radius);
        NanoVG.nvgFillPaint((long)NanoLoader.ctx, (NVGPaint)gradient);
        NanoVG.nvgFill((long)NanoLoader.ctx);
    }

    public static void drawShadowRound(float x, float y, float width, float height, float shadowWidth, int startAlpha, float radius) {
        NVGPaint paint = NVGPaint.calloc();
        NanoVG.nvgBoxGradient((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height, (float)radius, (float)shadowWidth, (NVGColor)NanoUtil.getColor(new Color(0, 0, 0, startAlpha)), (NVGColor)NanoUtil.getColor(new Color(0, 0, 0, 0)), (NVGPaint)paint);
        NanoVG.nvgBeginPath((long)NanoLoader.ctx);
        NanoVG.nvgRect((long)NanoLoader.ctx, (float)(x - shadowWidth), (float)(y - shadowWidth), (float)(width + shadowWidth * 2.0f), (float)(height + shadowWidth * 2.0f));
        NanoVG.nvgRoundedRect((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height, (float)radius);
        NanoVG.nvgPathWinding((long)NanoLoader.ctx, (int)2);
        NanoVG.nvgFillPaint((long)NanoLoader.ctx, (NVGPaint)paint);
        NanoVG.nvgFill((long)NanoLoader.ctx);
        paint.free();
    }

    public static NVGPaint createGradient(float startX, float startY, float endX, float endY, Color left, Color right) {
        NVGPaint paint = NVGPaint.calloc();
        NanoVG.nvgLinearGradient((long)NanoLoader.ctx, (float)startX, (float)startY, (float)endX, (float)endY, (NVGColor)NanoUtil.getColor(right), (NVGColor)NanoUtil.getColor(left), (NVGPaint)paint);
        return paint;
    }

    public static void drawRoundedOutlineRect(float x, float y, float width, float height, float radius, float outlineWidth, Color fillColor, Color outlineColor) {
        NanoUtil.drawRoundedRect(x, y, width, height, radius, fillColor);
        NanoUtil.strokeColor(outlineColor);
        NanoVG.nvgStrokeWidth((long)NanoLoader.ctx, (float)outlineWidth);
        NanoVG.nvgStroke((long)NanoLoader.ctx);
    }

    public static void drawCircle(float centerX, float centerY, float radius, Color color) {
        NanoUtil.beginPath();
        NanoVG.nvgCircle((long)NanoLoader.ctx, (float)centerX, (float)centerY, (float)radius);
        NanoUtil.fillColor(color);
        NanoVG.nvgFill((long)NanoLoader.ctx);
    }

    public static void scaleStart(float centerX, float centerY, float scale) {
        NanoVG.nvgSave((long)NanoLoader.ctx);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)centerX, (float)centerY);
        NanoVG.nvgScale((long)NanoLoader.ctx, (float)scale, (float)scale);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)(-centerX), (float)(-centerY));
    }

    public static void scaleXStart(float centerX, float centerY, float scale) {
        NanoVG.nvgSave((long)NanoLoader.ctx);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)centerX, (float)centerY);
        NanoVG.nvgScale((long)NanoLoader.ctx, (float)scale, (float)1.0f);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)(-centerX), (float)(-centerY));
    }

    public static void scaleEnd() {
        NanoVG.nvgRestore((long)NanoLoader.ctx);
    }

    public static void drawPolyline(float[] points, int color, float lineWidth) {
        if (points.length < 4) {
            throw new IllegalArgumentException("At least two points are required to render a line.");
        }
        NanoVG.nvgBeginPath((long)NanoLoader.ctx);
        NanoUtil.strokeColor(new Color(color));
        NanoVG.nvgStrokeWidth((long)NanoLoader.ctx, (float)lineWidth);
        NanoVG.nvgMoveTo((long)NanoLoader.ctx, (float)points[0], (float)points[1]);
        for (int i = 2; i < points.length; i += 2) {
            NanoVG.nvgLineTo((long)NanoLoader.ctx, (float)points[i], (float)points[i + 1]);
        }
        NanoVG.nvgStroke((long)NanoLoader.ctx);
    }

    public static void rotateStart(float centerX, float centerY, float angle) {
        NanoVG.nvgSave((long)NanoLoader.ctx);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)centerX, (float)centerY);
        NanoVG.nvgRotate((long)NanoLoader.ctx, (float)NanoVG.nvgDegToRad((float)angle));
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)(-centerX), (float)(-centerY));
    }

    public static void rotateEnd() {
        NanoVG.nvgRestore((long)NanoLoader.ctx);
    }

    public static void transformStart(float x, float y) {
        NanoVG.nvgSave((long)NanoLoader.ctx);
        NanoVG.nvgTranslate((long)NanoLoader.ctx, (float)x, (float)y);
    }

    public static void transformEnd() {
        NanoVG.nvgRestore((long)NanoLoader.ctx);
    }

    public static void scissorStart(float x, float y, float width, float height) {
        NanoUtil.scissorStart(x, y, width, height, true);
    }

    public static void scissorStart(float x, float y, float width, float height, boolean reset) {
        NanoVG.nvgSave((long)NanoLoader.ctx);
        if (reset) {
            NanoVG.nvgScissor((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height);
        } else {
            NanoVG.nvgIntersectScissor((long)NanoLoader.ctx, (float)x, (float)y, (float)width, (float)height);
        }
    }

    public static void scissorEnd() {
        NanoVG.nvgRestore((long)NanoLoader.ctx);
    }

    public static void fillColor(Color color) {
        NVGColor nvgColor = NanoUtil.getColor(color);
        NanoVG.nvgFillColor((long)NanoLoader.ctx, (NVGColor)nvgColor);
        nvgColor.free();
    }

    public static void strokeColor(Color color) {
        NVGColor nvgColor = NanoUtil.getColor(color);
        NanoVG.nvgStrokeColor((long)NanoLoader.ctx, (NVGColor)nvgColor);
        nvgColor.free();
    }
}

