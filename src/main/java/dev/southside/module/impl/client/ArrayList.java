package dev.southside.module.impl.client;

import cc.polyfrost.oneconfig.config.options.impl.HUD;
import dev.southside.gui.hud.ArrayListHud;
import dev.southside.module.Category;
import dev.southside.module.Module;

public class ArrayList extends Module {
   public final HUD hud = new HUD("ArrayList", new ArrayListHud(0.0F, 0.0F, 2, 1.0F));

   public ArrayList() {
      super(Category.Client, "ArrayList", true);
   }

   @Override
   public void initPostRunnable() {
      super.initPostRunnable();
      this.hideIf(((ArrayListHud)this.hud.getValue()).customColor1.getLabel(), () -> !((ArrayListHud)this.hud.getValue()).colorValue.getMode().equals("Custom"));
      this.hideIf(((ArrayListHud)this.hud.getValue()).customColor2.getLabel(), () -> !((ArrayListHud)this.hud.getValue()).colorValue.getMode().equals("Custom"));
      this.hideIf(((ArrayListHud)this.hud.getValue()).customSpeed.getLabel(), () -> !((ArrayListHud)this.hud.getValue()).colorValue.getMode().equals("Custom"));
      this.hideIf(ArrayListHud.removeShadow.getLabel(), () -> ArrayListHud.mode.isMode("SigmaJello"));
      this.hideIf(ArrayListHud.showSuffix.getLabel(), () -> ArrayListHud.mode.isMode("SigmaJello"));
      this.hideIf(ArrayListHud.background.getLabel(), () -> ArrayListHud.mode.isMode("SigmaJello"));
      this.hideIf(ArrayListHud.round.getLabel(), () -> {
         ArrayListHud var10000 = (ArrayListHud)this.hud.getValue();
         return !ArrayListHud.background.getValue() || ArrayListHud.mode.isMode("SigmaJello");
      });
      this.addListener(((ArrayListHud)this.hud.getValue()).customColor1.getLabel(), ((ArrayListHud)this.hud.getValue())::reloadRainbow);
      this.addListener(((ArrayListHud)this.hud.getValue()).customColor2.getLabel(), ((ArrayListHud)this.hud.getValue())::reloadRainbow);
   }
}
