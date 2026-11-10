package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import dev.southside.Southside;
import dev.southside.mixin.hud.InGameHudAccessor;
import dev.southside.module.impl.render.ScoreBoard;
import java.util.NoSuchElementException;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;

public class ScoreBoardHud extends Hud {
   private static final Text COLON_TEXT = Text.literal(": ");
   public final Switch noScore = new Switch("No Score", true);

   public ScoreBoardHud(float x, float y, int positionAlignment, float scale) {
      super(x, y, positionAlignment, scale);
   }

   @Override
   protected void draw(DrawContext context, UMatrixStack matrices, float x, float y, float scale, boolean example) {
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

   private void drawModernStyle(DrawContext context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
      ScoreBoardHud.SidebarEntry[] entries = data.entries();
      float padding = 5.0F;
      float titleHeight = 16.0F;
      float entrySpacing = 9.0F;
      float contentHeight = entries.length > 0 ? entries.length * entrySpacing + padding * 2.0F : entrySpacing + padding * 2.0F;
      float totalHeight = titleHeight + contentHeight;
      float width = data.maxWidth() + padding * 2.0F;
      float radius = 5.0F;
      int backgroundColor = mc.options.getTextBackgroundColor(0.3F);
      int titleBackgroundColor = mc.options.getTextBackgroundColor(0.4F);
      NanoVGHelper helper = NanoVGHelper.INSTANCE;
      helper.setupAndDraw(true, vg -> {
         helper.drawRoundedRect(vg, x, y, width * scale, totalHeight * scale, backgroundColor, radius * scale);
         helper.drawRoundedRectVaried(vg, x, y, width * scale, titleHeight * scale, titleBackgroundColor, radius * scale, radius * scale, 0.0F, 0.0F);
      });
      MatrixStack matrixStack = context.getMatrices();
      matrixStack.push();
      matrixStack.translate(x, y, 0.0F);
      matrixStack.scale(scale, scale, 1.0F);
      int leftPadding = (int)padding;
      int titleX = (int)(leftPadding + (data.maxWidth() - data.titleWidth()) / 2.0F);
      context.drawText(mc.textRenderer, data.title(), titleX, 5, -1, false);
      float startY = titleHeight + padding;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)(startY + i * entrySpacing);
         context.drawText(mc.textRenderer, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.pop();
   }

   private void drawSimpleStyle(DrawContext context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
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
      MatrixStack matrixStack = context.getMatrices();
      matrixStack.push();
      matrixStack.translate(x, y, 0.0F);
      matrixStack.scale(scale, scale, 1.0F);
      int leftPadding = (int)padding;
      int titleX = (int)(leftPadding + (data.maxWidth() - data.titleWidth()) / 2.0F);
      context.drawText(mc.textRenderer, data.title(), titleX, 5, -1, false);
      float startY = titleHeight + padding;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)(startY + i * entrySpacing);
         context.drawText(mc.textRenderer, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.pop();
   }

   private void drawVanillaStyle(DrawContext context, ScoreBoardHud.ScoreboardRenderData data, float x, float y, float scale) {
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
      MatrixStack matrixStack = context.getMatrices();
      matrixStack.push();
      matrixStack.translate(x, y, 0.0F);
      matrixStack.scale(scale, scale, 1.0F);
      int titleX = (int)((width - data.titleWidth()) / 2.0F);
      context.drawText(mc.textRenderer, data.title(), titleX, 1, -1, false);
      int leftPadding = 4;

      for (int i = 0; i < entries.length; i++) {
         ScoreBoardHud.SidebarEntry entry = entries[i];
         int entryY = (int)((i + 1) * rowHeight);
         context.drawText(mc.textRenderer, entry.name, leftPadding, entryY, -1, false);
         this.drawScore(context, data, entry, leftPadding, entryY);
      }

      matrixStack.pop();
   }

   private void drawScore(DrawContext context, ScoreBoardHud.ScoreboardRenderData data, ScoreBoardHud.SidebarEntry entry, int leftPadding, int y) {
      if (!this.hideScores() && entry.scoreWidth > 0) {
         int scoreX = leftPadding + (data.maxWidth() - entry.scoreWidth);
         int colonX = scoreX - data.colonWidth();
         context.drawText(mc.textRenderer, COLON_TEXT, colonX, y, -1, false);
         context.drawText(mc.textRenderer, entry.score, scoreX, y, -1, false);
      }
   }

   private ScoreBoardHud.ScoreboardRenderData resolveRenderData(boolean example) {
      ScoreboardObjective objective = this.getObjective();
      InGameHudAccessor accessor = (InGameHudAccessor)mc.inGameHud;
      if (accessor == null) {
         return null;
      } else {
         TextRenderer renderer = accessor.invokeGetTextRenderer();
         if (objective == null) {
            return !example ? null : this.buildRenderData(Text.literal("Scoreboard"), this.getExampleEntries(renderer), accessor);
         } else {
            return this.buildRenderData(objective.getDisplayName(), this.getSidebarEntries(objective), accessor);
         }
      }
   }

   private ScoreBoardHud.ScoreboardRenderData buildRenderData(Text title, ScoreBoardHud.SidebarEntry[] entries, InGameHudAccessor accessor) {
      TextRenderer renderer = accessor.invokeGetTextRenderer();
      int titleWidth = renderer.getWidth(title);
      int colonWidth = renderer.getWidth(COLON_TEXT);
      boolean includeScores = !this.hideScores();
      int maxWidth = titleWidth;

      for (ScoreBoardHud.SidebarEntry entry : entries) {
         int entryWidth = renderer.getWidth(entry.name);
         if (includeScores && entry.scoreWidth > 0) {
            entryWidth += colonWidth + entry.scoreWidth;
         }

         maxWidth = Math.max(maxWidth, entryWidth);
      }

      return new ScoreBoardHud.ScoreboardRenderData(title, entries, maxWidth, titleWidth, colonWidth);
   }

   private ScoreBoardHud.SidebarEntry[] getExampleEntries(TextRenderer renderer) {
      return new ScoreBoardHud.SidebarEntry[]{
         this.createExampleEntry(renderer, "Player 1", "12"),
         this.createExampleEntry(renderer, "Player 2", "7"),
         this.createExampleEntry(renderer, "Player 3", "3")
      };
   }

   private ScoreBoardHud.SidebarEntry createExampleEntry(TextRenderer renderer, String name, String score) {
      Text nameText = Text.literal(name);
      Text scoreText = Text.literal(score);
      int scoreWidth = renderer.getWidth(scoreText);
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

   private ScoreboardObjective getObjective() {
      Scoreboard scoreboard = null;
      if (mc.world != null) {
         scoreboard = mc.world.getScoreboard();
      }

      Team team = null;
      if (scoreboard != null && mc.player != null) {
         team = scoreboard.getScoreHolderTeam(mc.player.getNameForScoreboard());
      }

      ScoreboardObjective scoreboardObjective = null;
      if (team != null) {
         ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
         if (slot != null) {
            scoreboardObjective = scoreboard.getObjectiveForSlot(slot);
         }
      }

      if (scoreboard != null) {
         return scoreboardObjective != null ? scoreboardObjective : scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      } else {
         return null;
      }
   }

   private ScoreBoardHud.SidebarEntry[] getSidebarEntries(ScoreboardObjective objective) {
      InGameHudAccessor accessor = (InGameHudAccessor)mc.inGameHud;
      NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
      return mc.world != null
         ? mc.world
            .getScoreboard()
            .getScoreboardEntries(objective)
            .stream()
            .filter(entry -> !entry.hidden())
            .sorted(InGameHudAccessor.getScoreboardEntryComparator())
            .limit(15L)
            .map(entry -> {
               Team team = mc.world.getScoreboard().getScoreHolderTeam(entry.owner());
               Text displayName = Team.decorateName(team, entry.name());
               Text scoreText = entry.formatted(numberFormat);
               int scoreWidth = accessor.invokeGetTextRenderer().getWidth(scoreText);
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

   record ScoreboardRenderData(Text title, ScoreBoardHud.SidebarEntry[] entries, int maxWidth, int titleWidth, int colonWidth) {
   }

   record SidebarEntry(Text name, Text score, int scoreWidth) {
   }
}
