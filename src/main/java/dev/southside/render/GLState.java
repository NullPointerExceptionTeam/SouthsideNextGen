package dev.southside.render;

public class GLState {
   public boolean depthSaved;
   public boolean blendSaved;
   public boolean cullSaved;
   public boolean scissorSaved;
   public GLState child;
   public GLState parent;
}
