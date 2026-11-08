package dev.southside.utils.render.jello;

import dev.southside.MinecraftAccessor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.class_1041;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import org.lwjgl.opengl.GL20;

public class ShaderUtil implements MinecraftAccessor {
   private final int programID;
   private static final String jelloBlur = "#version 110\n\nuniform sampler2D DiffuseSampler;\n\nvarying vec2 texCoord;\nvarying vec2 oneTexel;\n\nuniform vec2 InSize;\n\nuniform vec2 BlurDir;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    float totalStrength = 0.0;\n    float totalAlpha = 0.0;\n    float totalSamples = 0.0;\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n        vec4 sampleValue = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);\n\n\t\t// Accumulate average alpha\n        totalAlpha = totalAlpha + sampleValue.a;\n        totalSamples = totalSamples + 1.0;\n\n\t\t// Accumulate smoothed blur\n        float strength = 1.0 - abs(r / Radius);\n        totalStrength = totalStrength + strength;\n        blurred = blurred + sampleValue;\n    }\n    gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);\n}\n";
   private static final String outlineShader = "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure;\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    for (float r = 1.0; r <= 1.0; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2);\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n";
   private static final String kawaseUpGlow = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform bool check;\nuniform float lastPass;\nuniform float exposure;\n\nvoid main() {\n    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));\n}";
   private static final String glowShader = "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    innerColor *= weights[0];\n    for (float r = 1.0; r <= radius; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n";
   private static final String chams = "#version 120\n\nuniform sampler2D textureIn;\nuniform vec4 color;\nvoid main() {\n    float alpha = texture2D(textureIn, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(color.rgb, color.a * mix(0.0, alpha, step(0.0, alpha)));\n}\n";
   private static final String roundRectTexture = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}";
   private static final String roundRectOutline = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}";
   private static final String kawaseUpBloom = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));\n}";
   private static final String kawaseDownBloom = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}";
   private static final String kawaseUp = "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(0, halfpixel.y) * r);\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, 0) * r);\n   }\n   gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 * 2.0 + 1.0), mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n";
   private static final String kawaseDown = "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum += texture2D(inTexture, uv);\n    gl_FragColor = vec4(sum.rgb * .5, 1.0);\n}\n";
   private static final String gradientMask = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\n#define radiusS 4.0\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(0.0019607843, -0.0019607843, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a * alpha;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);\n}";
   private static final String gradientGlow = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius;\nuniform float soft;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius);\n    float smoothedAlpha = (1.0 - smoothstep(-soft, soft, distance));\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha);\n}";
   private static final String roundRect = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(color1, smoothedAlpha * alpha);\n}";
   private static final String roundRect2 = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha * alpha);\n}";
   private static final String mask = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D u_texture, u_texture2;\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;\n    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;\n    gl_FragColor = vec4(tex2Color, texColorAlpha);\n}";
   private static final String gradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n#define NOISE .5/255.0\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    gl_FragColor = createGradient(coords, color1, color2, color3, color4);\n}";
   private static final String roundedRectGradient = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n   // use the bottom leftColor as the alpha\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}";
   private static final String roundedRect = "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\n\nvoid main() {\n    vec2 rectHalf = rectSize * .5;\n    // Smooth the result (free antialiasing).\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n\n}";

   public ShaderUtil(String fragmentShaderLoc, String vertexShaderLoc) {
      int program = GL20.glCreateProgram();

      try {
         int fragmentShaderID = switch (fragmentShaderLoc) {
            case "jelloBlur" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 110\n\nuniform sampler2D DiffuseSampler;\n\nvarying vec2 texCoord;\nvarying vec2 oneTexel;\n\nuniform vec2 InSize;\n\nuniform vec2 BlurDir;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    float totalStrength = 0.0;\n    float totalAlpha = 0.0;\n    float totalSamples = 0.0;\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n        vec4 sampleValue = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);\n\n\t\t// Accumulate average alpha\n        totalAlpha = totalAlpha + sampleValue.a;\n        totalSamples = totalSamples + 1.0;\n\n\t\t// Accumulate smoothed blur\n        float strength = 1.0 - abs(r / Radius);\n        totalStrength = totalStrength + strength;\n        blurred = blurred + sampleValue;\n    }\n    gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "roundRect2" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha * alpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "gradientGlow" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\nuniform float radius;\nuniform float soft;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius);\n    float smoothedAlpha = (1.0 - smoothstep(-soft, soft, distance));\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), smoothedAlpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "roundRect" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1;\nuniform float alpha;\nuniform float radius2;\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (rectSize / 2.0), rectSize / 2.0, radius2);\n    float smoothedAlpha = 1 - smoothstep(-1.0, 1.0, distance);\n    gl_FragColor = vec4(color1, smoothedAlpha * alpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "kawaseUpGlow" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform bool check;\nuniform float lastPass;\nuniform float exposure;\n\nvoid main() {\n    if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, 1.0 - exp(-result.a * exposure), step(0.0, lastPass)));\n}"
                     .getBytes()
               ),
               35632
            );
            case "glow" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure, radius;\nuniform float weights[256];\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    innerColor *= weights[0];\n    for (float r = 1.0; r <= radius; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2) * weights[int(r)];\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "chams" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D textureIn;\nuniform vec4 color;\nvoid main() {\n    float alpha = texture2D(textureIn, gl_TexCoord[0].st).a;\n    gl_FragColor = vec4(color.rgb, color.a * mix(0.0, alpha, step(0.0, alpha)));\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "roundRectTexture" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D textureIn;\nuniform float radius, alpha;\n\nfloat roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) -size, 0.)) - radius;\n}\n\n\nvoid main() {\n    float distance = roundedBoxSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2.0, distance)) * alpha;\n    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "roundRectOutline" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color, outlineColor;\nuniform float radius, outlineThickness;\n\nfloat roundedSDF(vec2 centerPos, vec2 size, float radius) {\n    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;\n}\n\nvoid main() {\n    float distance = roundedSDF(gl_FragCoord.xy - location - (rectSize * .5), (rectSize * .5) + (outlineThickness *.5) - 1.0, radius);\n\n    float blendAmount = smoothstep(0., 2., abs(distance) - (outlineThickness * .5));\n\n    vec4 insideColor = (distance < 0.) ? color : vec4(outlineColor.rgb,  0.0);\n    gl_FragColor = mix(outlineColor, insideColor, blendAmount);\n\n}"
                     .getBytes()
               ),
               35632
            );
            case "kawaseUpBloom" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\n\nvoid main() {\n  //  if(check && texture2D(textureToCheck, gl_TexCoord[0].st).a > 0.0) discard;\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n\n    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);\n    sum.rgb *= sum.a;\n    vec4 smpl1 =  texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset);\n    smpl1.rgb *= smpl1.a;\n    sum += smpl1 * 2.0;\n    vec4 smp2 = texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3 * 2.0;\n    vec4 smp4 = texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 smp5 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp5.rgb *= smp5.a;\n    sum += smp5 * 2.0;\n    vec4 smp6 = texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);\n    smp6.rgb *= smp6.a;\n    sum += smp6;\n    vec4 smp7 = texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset);\n    smp7.rgb *= smp7.a;\n    sum += smp7 * 2.0;\n    vec4 result = sum / 12.0;\n    gl_FragColor = vec4(result.rgb / result.a, mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a),check));\n}"
                     .getBytes()
               ),
               35632
            );
            case "outline" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D textureIn, textureToCheck;\nuniform vec2 texelSize, direction;\nuniform vec3 color;\nuniform bool avoidTexture;\nuniform float exposure;\n\n#define offset direction * texelSize\n\nvoid main() {\n    if (direction.y == 1 && avoidTexture) {\n        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;\n    }\n    vec4 innerColor = texture2D(textureIn, gl_TexCoord[0].st);\n    innerColor.rgb *= innerColor.a;\n    for (float r = 1.0; r <= 1.0; r++) {\n        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);\n        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);\n\n        colorCurrent1.rgb *= colorCurrent1.a;\n        colorCurrent2.rgb *= colorCurrent2.a;\n\n        innerColor += (colorCurrent1 + colorCurrent2);\n    }\n\n    gl_FragColor = vec4(innerColor.rgb / innerColor.a, mix(innerColor.a, 1.0 - exp(-innerColor.a * exposure), step(0.0, direction.y)));\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "kawaseDownBloom" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum.rgb *= sum.a;\n    sum *= 4.0;\n    vec4 smp1 = texture2D(inTexture, uv - halfpixel.xy * offset);\n    smp1.rgb *= smp1.a;\n    sum += smp1;\n    vec4 smp2 = texture2D(inTexture, uv + halfpixel.xy * offset);\n    smp2.rgb *= smp2.a;\n    sum += smp2;\n    vec4 smp3 = texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp3.rgb *= smp3.a;\n    sum += smp3;\n    vec4 smp4 = texture2D(inTexture, uv - vec2(halfpixel.x, -halfpixel.y) * offset);\n    smp4.rgb *= smp4.a;\n    sum += smp4;\n    vec4 result = sum / 8.0;\n    gl_FragColor = vec4(result.rgb / result.a, result.a);\n}"
                     .getBytes()
               ),
               35632
            );
            case "kawaseUp" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(0, halfpixel.y) * r);\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, 0) * r);\n   }\n   gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 * 2.0 + 1.0), mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "kawaseDown" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform sampler2D inTexture;\nuniform vec2 offset, halfpixel, iResolution;\n\nvoid main() {\n    vec2 uv = vec2(gl_FragCoord.xy / iResolution);\n    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);\n    sum += texture2D(inTexture, uv);\n    gl_FragColor = vec4(sum.rgb * .5, 1.0);\n}\n"
                     .getBytes()
               ),
               35632
            );
            case "gradientMask" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec3 color1, color2, color3, color4;\nuniform float alpha;\n#define radiusS 4.0\n#define NOISE .5/255.0\n\nfloat roundedBoxSDF(vec2 center, vec2 size, float radius) {\n    return length(max(abs(center) - size + radius, 0.0)) - radius;\n}\n\nvec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\n    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\n    //Dithering the color from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(0.0019607843, -0.0019607843, fract(sin(dot(coords.xy, vec2(12.9898,78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a * alpha;\n    gl_FragColor = vec4(createGradient(coords, color1, color2, color3, color4), texColorAlpha * alpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "mask" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D u_texture, u_texture2;\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    float texColorAlpha = texture2D(u_texture, gl_TexCoord[0].st).a;\n    vec3 tex2Color = texture2D(u_texture2, gl_TexCoord[0].st).rgb;\n    gl_FragColor = vec4(tex2Color, texColorAlpha);\n}"
                     .getBytes()
               ),
               35632
            );
            case "gradient" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform sampler2D tex;\nuniform vec4 color1, color2, color3, color4;\n#define NOISE .5/255.0\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 coords = (gl_FragCoord.xy - location) / rectSize;\n    gl_FragColor = createGradient(coords, color1, color2, color3, color4);\n}"
                     .getBytes()
               ),
               35632
            );
            case "roundedRect" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color;\nuniform float radius;\nuniform bool blur;\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b, 0.0)) - r;\n}\n\n\nvoid main() {\n    vec2 rectHalf = rectSize * .5;\n    // Smooth the result (free antialiasing).\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\n    gl_FragColor = vec4(color.rgb, smoothedAlpha);// mix(quadColor, shadowColor, 0.0);\n\n}"
                     .getBytes()
               ),
               35632
            );
            case "roundedRectGradient" -> this.createShader(
               new ByteArrayInputStream(
                  "#version 120\n\nuniform vec2 location, rectSize;\nuniform vec4 color1, color2, color3, color4;\nuniform float radius;\n\n#define NOISE .5/255.0\n\nfloat roundSDF(vec2 p, vec2 b, float r) {\n    return length(max(abs(p) - b , 0.0)) - r;\n}\n\nvec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){\n    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);\n    //Dithering the color\n    // from https://shader-tutorial.dev/advanced/color-banding-dithering/\n    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\n    return color;\n}\n\nvoid main() {\n    vec2 st = gl_TexCoord[0].st;\n    vec2 halfSize = rectSize * .5;\n    \n   // use the bottom leftColor as the alpha\n    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius)));\n    vec4 gradient = createGradient(st, color1, color2, color3, color4);    gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);\n}"
                     .getBytes()
               ),
               35632
            );
            default -> this.createShader(((class_3298)mc.method_1478().method_14486(class_2960.method_60654(fragmentShaderLoc)).get()).method_14482(), 35632);
         };
         GL20.glAttachShader(program, fragmentShaderID);
         int vertexShaderID = this.createShader(
            ((class_3298)mc.method_1478().method_14486(class_2960.method_60654(vertexShaderLoc)).get()).method_14482(), 35633
         );
         GL20.glAttachShader(program, vertexShaderID);
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      GL20.glLinkProgram(program);
      int status = GL20.glGetProgrami(program, 35714);
      if (status == 0) {
         throw new IllegalStateException("Shader failed to link!");
      } else {
         this.programID = program;
      }
   }

   public ShaderUtil(int ignoredProgramID) {
      int program = GL20.glCreateProgram();
      int vertexShaderID = this.createShader(
         new ByteArrayInputStream(
            "#version 120\n\nuniform sampler2D inTexture, textureToCheck;\nuniform vec2 halfpixel, offset, iResolution;\nuniform int check;\nuniform float Radius;\n\nvoid main() {\n    vec4 blurred = vec4(0.0);\n    for(float r = -Radius; r <= Radius; r += 1.0) {\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(0, halfpixel.y) * r);\n       blurred += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, 0) * r);\n   }\n   gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 * 2.0 + 1.0), mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));\n}\n"
               .getBytes()
         ),
         35633
      );
      GL20.glAttachShader(program, vertexShaderID);
      GL20.glLinkProgram(program);
      int status = GL20.glGetProgrami(program, 35714);
      if (status == 0) {
         throw new IllegalStateException("Shader failed to link!");
      } else {
         this.programID = program;
      }
   }

   public ShaderUtil(String fragmentShaderLoc) {
      this(fragmentShaderLoc, "shaders/vertex.vsh");
   }

   public void init() {
      GL20.glUseProgram(this.programID);
   }

   public void unload() {
      GL20.glUseProgram(0);
   }

   public int getUniform(String name) {
      return GL20.glGetUniformLocation(this.programID, name);
   }

   public void setUniformf(String name, float... args) {
      int loc = GL20.glGetUniformLocation(this.programID, name);
      switch (args.length) {
         case 1:
            GL20.glUniform1f(loc, args[0]);
            break;
         case 2:
            GL20.glUniform2f(loc, args[0], args[1]);
            break;
         case 3:
            GL20.glUniform3f(loc, args[0], args[1], args[2]);
            break;
         case 4:
            GL20.glUniform4f(loc, args[0], args[1], args[2], args[3]);
      }
   }

   public void setUniformi(String name, int... args) {
      int loc = GL20.glGetUniformLocation(this.programID, name);
      if (args.length > 1) {
         GL20.glUniform2i(loc, args[0], args[1]);
      } else {
         GL20.glUniform1i(loc, args[0]);
      }
   }

   public static void drawQuads(float x, float y, float width, float height) {
      GL20.glBegin(7);
      GL20.glTexCoord2f(0.0F, 0.0F);
      GL20.glVertex2f(x, y);
      GL20.glTexCoord2f(0.0F, 1.0F);
      GL20.glVertex2f(x, y + height);
      GL20.glTexCoord2f(1.0F, 1.0F);
      GL20.glVertex2f(x + width, y + height);
      GL20.glTexCoord2f(1.0F, 0.0F);
      GL20.glVertex2f(x + width, y);
      GL20.glEnd();
   }

   public static void drawQuads() {
      class_1041 sr = mc.method_22683();
      float width = sr.method_4486();
      float height = sr.method_4502();
      GL20.glBegin(7);
      GL20.glTexCoord2f(0.0F, 1.0F);
      GL20.glVertex2f(0.0F, 0.0F);
      GL20.glTexCoord2f(0.0F, 0.0F);
      GL20.glVertex2f(0.0F, height);
      GL20.glTexCoord2f(1.0F, 0.0F);
      GL20.glVertex2f(width, height);
      GL20.glTexCoord2f(1.0F, 1.0F);
      GL20.glVertex2f(width, 0.0F);
      GL20.glEnd();
   }

   public static void drawQuads(float width, float height) {
      GL20.glBegin(7);
      GL20.glTexCoord2f(0.0F, 1.0F);
      GL20.glVertex2f(0.0F, 0.0F);
      GL20.glTexCoord2f(0.0F, 0.0F);
      GL20.glVertex2f(0.0F, height);
      GL20.glTexCoord2f(1.0F, 0.0F);
      GL20.glVertex2f(width, height);
      GL20.glTexCoord2f(1.0F, 1.0F);
      GL20.glVertex2f(width, 0.0F);
      GL20.glEnd();
   }

   public static String readInputStream(InputStream inputStream) {
      StringBuilder stringBuilder = new StringBuilder();

      try {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

         String line;
         while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append('\n');
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return stringBuilder.toString();
   }

   private int createShader(InputStream inputStream, int shaderType) {
      int shader = GL20.glCreateShader(shaderType);
      GL20.glShaderSource(shader, readInputStream(inputStream));
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, 35713) == 0) {
         System.out.println(GL20.glGetShaderInfoLog(shader, 4096));
         throw new IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType));
      } else {
         return shader;
      }
   }
}
