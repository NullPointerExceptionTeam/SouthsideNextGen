package dev.southside.nanovg.gl.state;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;

public final class State {
   private final int glVersion;
   private final Properties props;

   public State(int glVersion) {
      this.glVersion = glVersion;
      this.props = new Properties();
   }

   public void push() {
      GL45.glGetIntegerv(34016, this.props.getLastActiveTexture());
      GL45.glActiveTexture(33984);
      GL45.glGetIntegerv(35725, this.props.getLastProgram());
      GL45.glGetIntegerv(32873, this.props.getLastTexture());
      if (this.glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
         GL45.glGetIntegerv(35097, this.props.getLastSampler());
      }

      GL45.glGetIntegerv(34964, this.props.getLastArrayBuffer());
      GL45.glGetIntegerv(34229, this.props.getLastVertexArrayObject());
      if (this.glVersion >= 200) {
         GL45.glGetIntegerv(2880, this.props.getLastPolygonMode());
      }

      GL45.glGetIntegerv(2978, this.props.getLastViewport());
      GL45.glGetIntegerv(3088, this.props.getLastScissorBox());
      GL45.glGetIntegerv(32969, this.props.getLastBlendSrcRgb());
      GL45.glGetIntegerv(32968, this.props.getLastBlendDstRgb());
      GL45.glGetIntegerv(32971, this.props.getLastBlendSrcAlpha());
      GL45.glGetIntegerv(32970, this.props.getLastBlendDstAlpha());
      GL45.glGetIntegerv(32777, this.props.getLastBlendEquationRgb());
      GL45.glGetIntegerv(34877, this.props.getLastBlendEquationAlpha());
      this.props.setLastEnableBlend(GL45.glIsEnabled(3042));
      this.props.setLastEnableCullFace(GL45.glIsEnabled(2884));
      this.props.setLastEnableDepthTest(GL45.glIsEnabled(2929));
      this.props.setLastEnableStencilTest(GL45.glIsEnabled(2960));
      this.props.setLastEnableScissorTest(GL45.glIsEnabled(3089));
      if (this.glVersion >= 310) {
         this.props.setLastEnablePrimitiveRestart(GL45.glIsEnabled(36765));
      }

      this.props.setLastDepthMask(GL45.glGetBoolean(2930));
   }

   public void pop() {
      GL45.glUseProgram(this.props.getLastProgram()[0]);
      GL45.glBindTexture(3553, this.props.getLastTexture()[0]);
      if (this.glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
         GL45.glBindSampler(0, this.props.getLastSampler()[0]);
      }

      GL45.glActiveTexture(this.props.getLastActiveTexture()[0]);
      GL45.glBindVertexArray(this.props.getLastVertexArrayObject()[0]);
      GL45.glBindBuffer(34962, this.props.getLastArrayBuffer()[0]);
      GL45.glBlendEquationSeparate(this.props.getLastBlendEquationRgb()[0], this.props.getLastBlendEquationAlpha()[0]);
      GL45.glBlendFuncSeparate(
         this.props.getLastBlendSrcRgb()[0], this.props.getLastBlendDstRgb()[0], this.props.getLastBlendSrcAlpha()[0], this.props.getLastBlendDstAlpha()[0]
      );
      if (this.props.isLastEnableBlend()) {
         GL45.glEnable(3042);
      } else {
         GL45.glDisable(3042);
      }

      if (this.props.isLastEnableCullFace()) {
         GL45.glEnable(2884);
      } else {
         GL45.glDisable(2884);
      }

      if (this.props.isLastEnableDepthTest()) {
         GL45.glEnable(2929);
      } else {
         GL45.glDisable(2929);
      }

      if (this.props.isLastEnableStencilTest()) {
         GL45.glEnable(2960);
      } else {
         GL45.glDisable(2960);
      }

      if (this.props.isLastEnableScissorTest()) {
         GL45.glEnable(3089);
      } else {
         GL45.glDisable(3089);
      }

      if (this.glVersion >= 310) {
         if (this.props.isLastEnablePrimitiveRestart()) {
            GL45.glEnable(36765);
         } else {
            GL45.glDisable(36765);
         }
      }

      if (this.glVersion >= 200) {
         GL45.glPolygonMode(1032, this.props.getLastPolygonMode()[0]);
      }

      GL45.glViewport(this.props.getLastViewport()[0], this.props.getLastViewport()[1], this.props.getLastViewport()[2], this.props.getLastViewport()[3]);
      GL45.glScissor(this.props.getLastScissorBox()[0], this.props.getLastScissorBox()[1], this.props.getLastScissorBox()[2], this.props.getLastScissorBox()[3]);
      GL45.glDepthMask(this.props.isLastDepthMask());
   }
}
