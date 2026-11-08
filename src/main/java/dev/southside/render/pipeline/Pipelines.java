package dev.southside.render.pipeline;

import dev.southside.render.shader.CompiledStage;
import dev.southside.render.shader.Shader;
import dev.southside.render.shader.ShaderFactory;
import dev.southside.render.shader.ShaderType;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_293.class_5596;

public class Pipelines {
   public static Mesh POS_COLOR_3D;
   public static Mesh POS_COLOR_3D_LINES;

   public static void init() {
      CompiledStage pos_color_vert = ShaderFactory.compile(class_2960.method_60655("southside", "g/pos_color.vert"), "v_pos_color", ShaderType.VERTEX);
      CompiledStage pos_color_frag = ShaderFactory.compile(class_2960.method_60655("southside", "g/pos_color.frag"), "f_pos_color", ShaderType.FRAGMENT);
      Shader pos_color = ShaderFactory.make(pos_color_vert, pos_color_frag);
      POS_COLOR_3D = new Mesh(pos_color, class_290.field_1576, class_5596.field_27379);
      POS_COLOR_3D_LINES = new Mesh(pos_color, class_290.field_1576, class_5596.field_29344);
   }
}
