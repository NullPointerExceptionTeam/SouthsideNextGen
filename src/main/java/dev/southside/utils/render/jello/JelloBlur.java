package dev.southside.utils.render.jello;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.southside.MinecraftAccessor;
import dev.southside.utils.render.jello.ShaderUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_276;
import net.minecraft.class_6367;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class JelloBlur
implements MinecraftAccessor {
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");
    public static class_276 framebuffer = new class_6367(1, 1, false);
    private static int currentIterations;
    private static final List<class_276> framebufferList;

    public static class_276 createFrameBuffer(class_276 framebuffer) {
        return JelloBlur.createFrameBuffer(framebuffer, false);
    }

    public static class_276 createFrameBuffer(class_276 framebuffer, boolean depth) {
        if (JelloBlur.needsNewFramebuffer(framebuffer) && mc.method_22683().method_4489() != 0 && mc.method_22683().method_4506() != 0) {
            if (framebuffer != null) {
                framebuffer.method_1238();
            }
            return new class_6367(mc.method_22683().method_4489(), mc.method_22683().method_4506(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(class_276 framebuffer) {
        return framebuffer == null || framebuffer.field_1482 != mc.method_22683().method_4489() || framebuffer.field_1481 != mc.method_22683().method_4506();
    }

    private static void initFramebuffers(float iterations) {
        for (class_276 framebuffer : framebufferList) {
            framebuffer.method_1238();
        }
        framebufferList.clear();
        framebuffer = JelloBlur.createFrameBuffer(null);
        framebufferList.add(framebuffer);
        int i = 1;
        while ((float)i <= iterations) {
            class_6367 currentBuffer = new class_6367((int)((double)mc.method_22683().method_4489() / Math.pow(2.0, i)), (int)((double)mc.method_22683().method_4489() / Math.pow(2.0, i)), false);
            currentBuffer.method_58226(9729);
            GlStateManager._bindTexture((int)currentBuffer.method_30277());
            GL11.glTexParameteri((int)3553, (int)10242, (int)33648);
            GL11.glTexParameteri((int)3553, (int)10243, (int)33648);
            GlStateManager._bindTexture((int)0);
            framebufferList.add((class_276)currentBuffer);
            ++i;
        }
    }

    public static void setAlphaLimit(float limit) {
        GL11.glAlphaFunc((int)516, (float)((float)((double)limit * 0.01)));
    }

    public static void startBlend() {
        GL11.glDisable((int)3008);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
    }

    public static void renderRectBlur(float x, float y, float width, float height, int stencilFrameBufferTexture, int iterations, float offset) {
        if (mc.method_22683().method_4489() != 0 && mc.method_22683().method_4506() != 0 && (currentIterations != iterations || JelloBlur.framebuffer.field_1482 != mc.method_22683().method_4489() || JelloBlur.framebuffer.field_1481 != mc.method_22683().method_4506())) {
            JelloBlur.initFramebuffers(iterations);
            currentIterations = iterations;
        }
        int x_ = (int)((double)x * mc.method_22683().method_4495());
        int y_ = (int)((double)((int)((double)mc.method_22683().method_4506() - (double)height * mc.method_22683().method_4495())) - (double)y * mc.method_22683().method_4495());
        try {
            JelloBlur.renderFBO(framebufferList.get(1), mc.method_1522().method_30277(), kawaseDown, offset);
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
        }
        GL11.glPushMatrix();
        GL11.glEnable((int)3089);
        GL11.glScissor((int)x_, (int)y_, (int)((int)((double)width * mc.method_22683().method_4495())), (int)((int)((double)height * mc.method_22683().method_4495())));
        class_276 lastBuffer = framebufferList.get(0);
        lastBuffer.method_1230();
        lastBuffer.method_1235(false);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformf("Radius", offset);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / (float)lastBuffer.field_1482, 1.0f / (float)lastBuffer.field_1481);
        kawaseUp.setUniformf("iResolution", lastBuffer.field_1482, lastBuffer.field_1481);
        GL13.glActiveTexture((int)34000);
        GlStateManager._bindTexture((int)stencilFrameBufferTexture);
        GL13.glActiveTexture((int)33984);
        GlStateManager._bindTexture((int)framebufferList.get(1).method_30277());
        ShaderUtil.drawQuads();
        kawaseUp.unload();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager._disableBlend();
        GlStateManager._bindTexture((int)0);
        GL11.glDisable((int)3089);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glEnable((int)3089);
        GL11.glScissor((int)x_, (int)y_, (int)((int)((double)width * mc.method_22683().method_4495())), (int)((int)((double)height * mc.method_22683().method_4495())));
        mc.method_1522().method_1235(true);
        GlStateManager._bindTexture((int)framebufferList.get(0).method_30277());
        JelloBlur.setAlphaLimit(0.0f);
        JelloBlur.startBlend();
        ShaderUtil.drawQuads();
        GlStateManager._bindTexture((int)0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager._disableBlend();
        GlStateManager._bindTexture((int)0);
        GL11.glDisable((int)3089);
        GL11.glPopMatrix();
    }

    private static void renderFBO(class_276 framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.method_1230();
        framebuffer.method_1235(false);
        shader.init();
        GlStateManager._bindTexture((int)framebufferTexture);
        if (shader == kawaseUp) {
            shader.setUniformf("Radius", offset);
        }
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / (float)framebuffer.field_1482, 1.0f / (float)framebuffer.field_1481);
        shader.setUniformf("iResolution", framebuffer.field_1482, framebuffer.field_1481);
        ShaderUtil.drawQuads();
        shader.unload();
    }

    static {
        framebufferList = new ArrayList<class_276>();
    }
}

