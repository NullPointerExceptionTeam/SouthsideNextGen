package dev.southside.render.pipeline;

import dev.southside.render.shader.CompiledStage;
import dev.southside.render.shader.Shader;
import dev.southside.render.shader.ShaderFactory;
import dev.southside.render.shader.ShaderType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.VertexFormat.DrawMode;

public class Pipelines {
   public static Mesh POS_COLOR_3D;
   public static Mesh POS_COLOR_3D_LINES;

   public static void init() {
      CompiledStage pos_color_vert = ShaderFactory.compile(Identifier.of("southside", "g/pos_color.vert"), "v_pos_color", ShaderType.VERTEX);
      CompiledStage pos_color_frag = ShaderFactory.compile(Identifier.of("southside", "g/pos_color.frag"), "f_pos_color", ShaderType.FRAGMENT);
      Shader pos_color = ShaderFactory.make(pos_color_vert, pos_color_frag);
      POS_COLOR_3D = new Mesh(pos_color, VertexFormats.POSITION_COLOR, DrawMode.TRIANGLES);
      POS_COLOR_3D_LINES = new Mesh(pos_color, VertexFormats.POSITION_COLOR, DrawMode.DEBUG_LINES);
   }
}
