package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.Render3DEvent;
import dev.southside.mixininterfaces.IMinecraftClient;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.render.animation.TimerUtil;
import net.minecraft.item.BlockItem;
import org.apache.commons.lang3.RandomUtils;

public class AutoClicker extends Module {
   public Switch leftClick = new Switch("Left Click", true);
   public Slider minLeftCps = new Slider("Min Left CPS", 8.0, 0.0, 20.0, 1.0);
   public Slider maxLeftCps = new Slider("Max Left CPS", 10.0, 0.0, 20.0, 1.0);
   public Switch stopWhileBreak = new Switch("Stop While Break", true);
   public Switch rightClick = new Switch("Right Click", true);
   public Slider minRightCps = new Slider("Min Right CPS", 8.0, 0.0, 20.0, 1.0);
   public Slider maxRightCps = new Slider("Max Right CPS", 10.0, 0.0, 20.0, 1.0);
   public Switch onlyBlocks = new Switch("Only Blocks", true);
   private final TimerUtil leftTimer = new TimerUtil();
   private final TimerUtil rightTimer = new TimerUtil();
   private long nextLeftDelay;
   private long nextRightDelay;

   public AutoClicker() {
      super(Category.Combat, "AutoClicker", "Auto Simulation Click");
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.addRangedValueRestrict(this.minLeftCps, this.maxLeftCps);
      this.addRangedValueRestrict(this.minRightCps, this.maxRightCps);
   }

   @EventHandler
   public void onRender3D(Render3DEvent event) {
      if (mc.player != null) {
         if (this.leftClick.getValue()
            && this.leftTimer.hasReached(this.nextLeftDelay)
            && mc.options.attackKey.isPressed()
            && (!this.stopWhileBreak.getValue() || !mc.interactionManager.isBreakingBlock())) {
            this.leftTimer.reset();
            ((IMinecraftClient)mc).setAttackCooldown(0);
            ((IMinecraftClient)mc).leftClick();
            this.nextLeftDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minLeftCps.getValue().doubleValue(), this.maxLeftCps.getValue().doubleValue()));
         }

         if (this.rightClick.getValue()
            && this.rightTimer.hasReached(this.nextRightDelay)
            && mc.options.useKey.isPressed()
            && (
               !this.onlyBlocks.getValue()
                  || mc.player.getMainHandStack().getItem() instanceof BlockItem
                  || mc.player.getOffHandStack().getItem() instanceof BlockItem
            )) {
            this.rightTimer.reset();
            ((IMinecraftClient)mc).rightClick();
            this.nextRightDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minRightCps.getValue().doubleValue(), this.maxRightCps.getValue().doubleValue()));
         }
      }
   }
}
