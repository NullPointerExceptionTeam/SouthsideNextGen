package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.scissor.Scissor;
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.nanovg.NanoVG;

public class ScissorHelperImpl implements ScissorHelper {
   private final List<List<Scissor>> previousScissors = new ArrayList<>();
   private List<Scissor> scissors = new ArrayList<>();

   @Override
   public Scissor scissor(long vg, float x, float y, float width, float height) {
      Scissor scissor = new Scissor(x, y, width, height);
      if (this.scissors.contains(scissor)) {
         return scissor;
      } else {
         this.scissors.add(scissor);
         this.applyScissors(vg);
         return scissor;
      }
   }

   @Override
   public void resetScissor(long vg, Scissor scissor) {
      if (this.scissors.contains(scissor)) {
         this.scissors.remove(scissor);
         this.applyScissors(vg);
      }
   }

   @Override
   public void clearScissors(long vg) {
      this.scissors.clear();
      NanoVG.nvgResetScissor(vg);
   }

   @Override
   public void save() {
      this.previousScissors.add(new ArrayList<>(this.scissors));
   }

   @Override
   public void restore(long vg) {
      this.scissors = this.previousScissors.remove(0);
      this.applyScissors(vg);
   }

   private void applyScissors(long vg) {
      NanoVG.nvgResetScissor(vg);
      if (!this.scissors.isEmpty()) {
         Scissor finalScissor = this.getFinalScissor(this.scissors);
         NanoVG.nvgScissor(vg, finalScissor.x, finalScissor.y, finalScissor.width, finalScissor.height);
      }
   }

   private Scissor getFinalScissor(List<Scissor> scissors) {
      Scissor finalScissor = new Scissor(scissors.get(0));

      for (int i = 1; i < scissors.size(); i++) {
         Scissor scissor = scissors.get(i);
         float rightX = Math.min(scissor.x + scissor.width, finalScissor.x + finalScissor.width);
         float rightY = Math.min(scissor.y + scissor.height, finalScissor.y + finalScissor.height);
         finalScissor.x = Math.max(finalScissor.x, scissor.x);
         finalScissor.y = Math.max(finalScissor.y, scissor.y);
         finalScissor.width = rightX - finalScissor.x;
         finalScissor.height = rightY - finalScissor.y;
      }

      return finalScissor;
   }
}
