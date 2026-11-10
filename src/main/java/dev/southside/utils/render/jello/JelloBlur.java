package dev.southside.utils.render.jello;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.southside.MinecraftAccessor;
import dev.southside.utils.render.jello.ShaderUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class JelloBlur
implements MinecraftAccessor {
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");
    public static Framebuffer framebuffer = new SimpleFramebuffer(1, 1, false);
    private static int currentIterations;
    private static final List<Framebuffer> framebufferList;

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return JelloBlur.createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (JelloBlur.needsNewFramebuffer(framebuffer) && mc.getWindow().getFramebufferWidth() != 0 && mc.getWindow().getFramebufferHeight() != 0) {
            if (framebuffer != null) {
                framebuffer.delete();
            }
            return new SimpleFramebuffer(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.textureWidth != mc.getWindow().getFramebufferWidth() || framebuffer.textureHeight != mc.getWindow().getFramebufferHeight();
    }

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.delete();
        }
        framebufferList.clear();
        framebuffer = JelloBlur.createFrameBuffer(null);
        framebufferList.add(framebuffer);
        int i = 1;
        while ((float)i <= iterations) {
            SimpleFramebuffer currentBuffer = new SimpleFramebuffer((int)((double)mc.getWindow().getFramebufferWidth() / Math.pow(2.0, i)), (int)((double)mc.getWindow().getFramebufferWidth() / Math.pow(2.0, i)), false);
            currentBuffer.setTexFilter(9729);
            GlStateManager._bindTexture((int)currentBuffer.getColorAttachment());
            GL11.glTexParameteri((int)3553, (int)10242, (int)33648);
            GL11.glTexParameteri((int)3553, (int)10243, (int)33648);
            GlStateManager._bindTexture((int)0);
            framebufferList.add((Framebuffer)currentBuffer);
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
        if (mc.getWindow().getFramebufferWidth() != 0 && mc.getWindow().getFramebufferHeight() != 0 && (currentIterations != iterations || JelloBlur.framebuffer.textureWidth != mc.getWindow().getFramebufferWidth() || JelloBlur.framebuffer.textureHeight != mc.getWindow().getFramebufferHeight())) {
            JelloBlur.initFramebuffers(iterations);
            currentIterations = iterations;
        }
        int x_ = (int)((double)x * mc.getWindow().getScaleFactor());
        int y_ = (int)((double)((int)((double)mc.getWindow().getFramebufferHeight() - (double)height * mc.getWindow().getScaleFactor())) - (double)y * mc.getWindow().getScaleFactor());
        try {
            JelloBlur.renderFBO(framebufferList.get(1), mc.getFramebuffer().getColorAttachment(), kawaseDown, offset);
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
        }
        GL11.glPushMatrix();
        GL11.glEnable((int)3089);
        GL11.glScissor((int)x_, (int)y_, (int)((int)((double)width * mc.getWindow().getScaleFactor())), (int)((int)((double)height * mc.getWindow().getScaleFactor())));
        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.clear();
        lastBuffer.beginWrite(false);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformf("Radius", offset);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / (float)lastBuffer.textureWidth, 1.0f / (float)lastBuffer.textureHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.textureWidth, lastBuffer.textureHeight);
        GL13.glActiveTexture((int)34000);
        GlStateManager._bindTexture((int)stencilFrameBufferTexture);
        GL13.glActiveTexture((int)33984);
        GlStateManager._bindTexture((int)framebufferList.get(1).getColorAttachment());
        ShaderUtil.drawQuads();
        kawaseUp.unload();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager._disableBlend();
        GlStateManager._bindTexture((int)0);
        GL11.glDisable((int)3089);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glEnable((int)3089);
        GL11.glScissor((int)x_, (int)y_, (int)((int)((double)width * mc.getWindow().getScaleFactor())), (int)((int)((double)height * mc.getWindow().getScaleFactor())));
        mc.getFramebuffer().beginWrite(true);
        GlStateManager._bindTexture((int)framebufferList.get(0).getColorAttachment());
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

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.clear();
        framebuffer.beginWrite(false);
        shader.init();
        GlStateManager._bindTexture((int)framebufferTexture);
        if (shader == kawaseUp) {
            shader.setUniformf("Radius", offset);
        }
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / (float)framebuffer.textureWidth, 1.0f / (float)framebuffer.textureHeight);
        shader.setUniformf("iResolution", framebuffer.textureWidth, framebuffer.textureHeight);
        ShaderUtil.drawQuads();
        shader.unload();
    }

    static {
        framebufferList = new ArrayList<Framebuffer>();
    }
}

