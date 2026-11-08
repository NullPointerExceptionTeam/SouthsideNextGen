package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import dev.southside.Southside;
import dev.southside.mixin.hud.InGameHudAccessor;
import dev.southside.module.impl.render.ScoreBoard;
import java.util.NoSuchElementException;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_8646;
import net.minecraft.class_9022;
import net.minecraft.class_9025;

public class ScoreBoardHud extends Hud {
   private static final class_2561 COLON_TEXT = class_2561.method_43470(": ");
   public final Switch noScore = new Switch("No Score", true);

   public ScoreBoardHud(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   @Override
   protected void draw(class_332 context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
      ScoreBoardHud.ScoreboardRenderData renderData = this.resolveRenderData(example);
      if (renderData != null) {
         if (this.isVanillaStyle()) {
            this.drawVanillaStyle(context, renderData, x, y, scale);
         } else if (this.isSimpleStyle()) {
            this.drawSimpleStyle(context, renderData, x, y, scale);
         } else {
            this.drawModernStyle(context, renderData, x, y, scale);
         }
      }
   }

   private void drawModernStyle(class_332 context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
      ScoreBoardHud.SidebarEntry[] entries = data.entries();
      float padding = 5.0F;
      float titleHeight = 16.0F;
      float entrySpacing = 9.0F;
      float contentHeight = entries.length > 0 ? entries.length * entrySpacing + padding * 2.0F : entrySpacing + padding * 2.0F;
      float totalHeight = titleHeight + contentHeight;
      float width = data.maxWidth() + padding * 2.0F;
      float radius = 5.0F;
      int backgroundColor = mc.field_1690.method_19345(0.3F);
      int titleBackgroundColor = mc.field_1690.method_19345(0.4F);
      NanoVGHelper helper = NanoVGHelper.INSTANCE;
      helper.setupAndDraw(true, vg -> {
         helper.drawRoundedRect(vg, x, y, width * scale, totalHeight * scale, backgroundColor, radius * scale);
         helper.drawRoundedRectVaried(vg, x, y, width * scale, titleHeight * scale, titleBackgroundColor, radius * scale, radius * scale, 0.0F, 0.0F);
      });
      class_4587 matrixStack = context.method_51448();
      matrixStack.method_22903();
      matrixStack.method_46416(x, y, 0.0F);
      matrixStack.method_22905(scale, scale, 1.0F);
      int leftPadding = (int)padding;
      int titleX = (int)(leftPadding + (data.maxWidth() - data.titleWidth()) / 2.0F);
      context.method_51439(mc.field_1772, data.title(), titleX, 5, -1, false);
      float startY = titleHeight + padding;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)(startY + i * entrySpacing);
         context.method_51439(mc.field_1772, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.method_22909();
   }

   private void drawSimpleStyle(class_332 context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
      ScoreBoardHud.SidebarEntry[] entries = data.entries();
      float padding = 5.0F;
      float titleHeight = 16.0F;
      float entrySpacing = 9.0F;
      float contentHeight = entries.length > 0 ? entries.length * entrySpacing + padding * 2.0F : entrySpacing + padding * 2.0F;
      float totalHeight = titleHeight + contentHeight;
      float width = data.maxWidth() + padding * 2.0F;
      int bgColor = -1275068416;
      NanoVGHelper helper = NanoVGHelper.INSTANCE;
      helper.setupAndDraw(true, vg -> {
         helper.drawRect(vg, x, y, width * scale, totalHeight * scale, bgColor);
         helper.drawRect(vg, x, y, width * scale, titleHeight * scale, 553648127);
      });
      class_4587 matrixStack = context.method_51448();
      matrixStack.method_22903();
      matrixStack.method_46416(x, y, 0.0F);
      matrixStack.method_22905(scale, scale, 1.0F);
      int leftPadding = (int)padding;
      int titleX = (int)(leftPadding + (data.maxWidth() - data.titleWidth()) / 2.0F);
      context.method_51439(mc.field_1772, data.title(), titleX, 5, -1, false);
      float startY = titleHeight + padding;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)(startY + i * entrySpacing);
         context.method_51439(mc.field_1772, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.method_22909();
   }

   private void drawVanillaStyle(class_332 context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
      ScoreBoardHud.SidebarEntry[] entries = data.entries();
      float rowHeight = 9.0F;
      float width = data.maxWidth() + 8.0F;
      float baseHeight = Math.max((entries.length + 1) * rowHeight, rowHeight + 4.0F);
      int backgroundColor = 1342177280;
      int headerColor = 1613770800;
      NanoVGHelper helper = NanoVGHelper.INSTANCE;
      helper.setupAndDraw(true, vg -> {
         helper.drawRect(vg, x, y, width * scale, baseHeight * scale, backgroundColor);
         helper.drawRect(vg, x, y, width * scale, rowHeight * scale, headerColor);
      });
      class_4587 matrixStack = context.method_51448();
      matrixStack.method_22903();
      matrixStack.method_46416(x, y, 0.0F);
      matrixStack.method_22905(scale, scale, 1.0F);
      int titleX = (int)((width - data.titleWidth()) / 2.0F);
      context.method_51439(mc.field_1772, data.title(), titleX, 1, -1, false);
      int leftPadding = 4;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)((i + 1) * rowHeight);
         context.method_51439(mc.field_1772, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.method_22909();
   }

   private void drawScore(class_332 context, ScoreBoardHud.ScoreboardRenderData data, ScoreBoardHud.SidebarEntry entry, int leftPadding, int y) {
      if (!this.hideScores() && entry.scoreWidth > 0) {
         int scoreX = leftPadding + (data.maxWidth() - entry.scoreWidth);
         int colonX = scoreX - data.colonWidth();
         context.method_51439(mc.field_1772, COLON_TEXT, colonX, y, -1, false);
         context.method_51439(mc.field_1772, entry.score, scoreX, y, -1, false);
      }
   }

   private ScoreBoardHud.ScoreboardRenderData resolveRenderData(boolean example) {
      class_266 objective = this.getObjective();
      InGameHudAccessor accessor = (InGameHudAccessor)mc.field_1705;
      if (accessor == null) {
         return null;
      } else {
         class_327 renderer = accessor.invokeGetTextRenderer();
         if (objective == null) {
            return !example ? null : this.buildRenderData(class_2561.method_43470("Scoreboard"), this.getExampleEntries(renderer), accessor);
         } else {
            return this.buildRenderData(objective.method_1114(), this.getSidebarEntries(objective), accessor);
         }
      }
   }

   private ScoreBoardHud.ScoreboardRenderData buildRenderData(class_2561 title, ScoreBoardHud.SidebarEntry[] entries, InGameHudAccessor accessor) {
      class_327 renderer = accessor.invokeGetTextRenderer();
      int titleWidth = renderer.method_27525(title);
      int colonWidth = renderer.method_27525(COLON_TEXT);
      boolean includeScores = !this.hideScores();
      int maxWidth = titleWidth;

      for (ScoreBoardHud.SidebarEntry entry : entries) {
         int entryWidth = renderer.method_27525(entry.name);
         if (includeScores && entry.scoreWidth > 0) {
            entryWidth += colonWidth + entry.scoreWidth;
         }

         maxWidth = Math.max(maxWidth, entryWidth);
      }

      return new ScoreBoardHud.ScoreboardRenderData(title, entries, maxWidth, titleWidth, colonWidth);
   }

   private ScoreBoardHud.SidebarEntry[] getExampleEntries(class_327 renderer) {
      return new ScoreBoardHud.SidebarEntry[]{
         this.createExampleEntry(renderer, "Player 1", "12"),
         this.createExampleEntry(renderer, "Player 2", "7"),
         this.createExampleEntry(renderer, "Player 3", "3")
      };
   }

   private ScoreBoardHud.SidebarEntry createExampleEntry(class_327 renderer, String name, String score) {
      class_2561 nameText = class_2561.method_43470(name);
      class_2561 scoreText = class_2561.method_43470(score);
      int scoreWidth = renderer.method_27525(scoreText);
      return new ScoreBoardHud.SidebarEntry(nameText, scoreText, scoreWidth);
   }

   private boolean isVanillaStyle() {
      ScoreBoard scoreBoard = this.getModule();
      return scoreBoard != null && scoreBoard.style.isMode("Vanilla");
   }

   private boolean isSimpleStyle() {
      ScoreBoard scoreBoard = this.getModule();
      return scoreBoard != null && scoreBoard.style.isMode("Simple");
   }

   private boolean hideScores() {
      return this.noScore == null || this.noScore.getValue();
   }

   private ScoreBoard getModule() {
      Southside southside = Southside.get();
      if (southside != null && southside.getModuleManager() != null) {
         try {
            return southside.getModuleManager().get(ScoreBoard.class);
         } catch (NoSuchElementException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private class_266 getObjective() {
      class_269 scoreboard = null;
      if (mc.field_1687 != null) {
         scoreboard = mc.field_1687.method_8428();
      }

      class_268 team = null;
      if (scoreboard != null && mc.field_1724 != null) {
         team = scoreboard.method_1164(mc.field_1724.method_5820());
      }

      class_266 scoreboardObjective = null;
      if (team != null) {
         class_8646 slot = class_8646.method_52622(team.method_1202());
         if (slot != null) {
            scoreboardObjective = scoreboard.method_1189(slot);
         }
      }

      if (scoreboard != null) {
         return scoreboardObjective != null ? scoreboardObjective : scoreboard.method_1189(class_8646.field_45157);
      } else {
         return null;
      }
   }

   private ScoreBoardHud.SidebarEntry[] getSidebarEntries(class_266 objective) {
      InGameHudAccessor accessor = (InGameHudAccessor)mc.field_1705;
      class_9022 numberFormat = objective.method_55380(class_9025.field_47567);
      return mc.field_1687 != null
         ? mc.field_1687
            .method_8428()
            .method_1184(objective)
            .stream()
            .filter(entry -> !entry.method_55385())
            .sorted(InGameHudAccessor.getScoreboardEntryComparator())
            .limit(15L)
            .map(entry -> {
               class_268 team = mc.field_1687.method_8428().method_1164(entry.comp_2127());
               class_2561 displayName = class_268.method_1142(team, entry.method_55387());
               class_2561 scoreText = entry.method_55386(numberFormat);
               int scoreWidth = accessor.invokeGetTextRenderer().method_27525(scoreText);
               return new ScoreBoardHud.SidebarEntry(displayName, scoreText, scoreWidth);
            })
            .toArray(ScoreBoardHud.SidebarEntry[]::new)
         : new ScoreBoardHud.SidebarEntry[0];
   }

   @Override
   protected float getWidth(float scale, boolean example) {
      ScoreBoardHud.ScoreboardRenderData data = this.resolveRenderData(example);
      if (data == null) {
         return 0.0F;
      } else {
         float baseWidth = this.isVanillaStyle() ? data.maxWidth() + 8.0F : data.maxWidth() + 10.0F;
         return baseWidth * scale;
      }
   }

   @Override
   protected float getHeight(float scale, boolean example) {
      ScoreBoardHud.ScoreboardRenderData data = this.resolveRenderData(example);
      if (data == null) {
         return 0.0F;
      } else {
         float height;
         if (this.isVanillaStyle()) {
            float rowHeight = 9.0F;
            height = Math.max((data.entries().length + 1) * rowHeight, rowHeight + 4.0F);
         } else {
            float padding = 5.0F;
            float titleHeight = 16.0F;
            float entrySpacing = 9.0F;
            float contentHeight = data.entries().length > 0 ? data.entries().length * entrySpacing + padding * 2.0F : entrySpacing + padding * 2.0F;
            height = titleHeight + contentHeight;
         }

         return height * scale;
      }
   }

   record ScoreboardRenderData(class_2561 title, ScoreBoardHud.SidebarEntry[] entries, int maxWidth, int titleWidth, int colonWidth) {
   }

   record SidebarEntry(class_2561 name, class_2561 score, int scoreWidth) {
   }
}
