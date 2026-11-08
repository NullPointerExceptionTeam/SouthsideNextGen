package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.events.Render3DEvent;
import dev.southside.mixininterfaces.IMinecraftClient;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.render.animation.TimerUtil;
import net.minecraft.class_1747;
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
      if (mc.field_1724 != null) {
         if (this.leftClick.getValue()
            && this.leftTimer.hasReached(this.nextLeftDelay)
            && mc.field_1690.field_1886.method_1434()
            && (!this.stopWhileBreak.getValue() || !mc.field_1761.method_2923())) {
            this.leftTimer.reset();
            ((IMinecraftClient)mc).setAttackCooldown(0);
            ((IMinecraftClient)mc).leftClick();
            this.nextLeftDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minLeftCps.getValue().doubleValue(), this.maxLeftCps.getValue().doubleValue()));
         }

         if (this.rightClick.getValue()
            && this.rightTimer.hasReached(this.nextRightDelay)
            && mc.field_1690.field_1904.method_1434()
            && (
               !this.onlyBlocks.getValue()
                  || mc.field_1724.method_6047().method_7909() instanceof class_1747
                  || mc.field_1724.method_6079().method_7909() instanceof class_1747
            )) {
            this.rightTimer.reset();
            ((IMinecraftClient)mc).rightClick();
            this.nextRightDelay = (long)(1000.0 / RandomUtils.nextDouble(this.minRightCps.getValue().doubleValue(), this.maxRightCps.getValue().doubleValue()));
         }
      }
   }
}
