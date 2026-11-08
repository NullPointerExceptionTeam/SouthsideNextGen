package dev.southside.nanovg.gl.state;

import java.util.Stack;
import org.lwjgl.opengl.GL30;

public final class States {
   public static States INSTANCE = new States();
   private static final int glVersion;
   private static final Stack<State> states = new Stack<>();

   private States() {
   }

   public void push() {
      State currentState = new State(glVersion);
      currentState.push();
      states.add(currentState);
   }

   public void pop() {
      if (states.isEmpty()) {
         throw new IllegalStateException("No state to restore.");
      } else {
         State state = states.pop();
         state.pop();
      }
   }

   static {
      int[] major = new int[1];
      int[] minor = new int[1];
      GL30.glGetIntegerv(33307, major);
      GL30.glGetIntegerv(33308, minor);
      glVersion = major[0] * 100 + minor[0] * 10;
   }
}
