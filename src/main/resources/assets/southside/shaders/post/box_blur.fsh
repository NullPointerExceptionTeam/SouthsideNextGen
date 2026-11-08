#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 sampleStep;

uniform vec2 InSize;
uniform float Radius;
uniform float RadiusMultiplier;
uniform float SampleRate;

out vec4 fragColor;

float gaussianWeight(float distance, float invTwoSigmaSq) {
    return exp(-distance * distance * invTwoSigmaSq);
}

vec4 sampleLinear(float offset, vec2 uvMin, vec2 uvMax) {
    vec2 uv = clamp(texCoord + sampleStep * offset, uvMin, uvMax);
    return texture(InSampler, uv);
}

void main() {
    float actualRadius = max(1.0, Radius * RadiusMultiplier);
    float stride = max(1.0, SampleRate);

    float sigma = max(actualRadius * 0.5, 1.0);
    float invTwoSigmaSq = 0.5 / (sigma * sigma);

    vec2 halfTexel = 0.5 / InSize;
    vec2 uvMin = halfTexel;
    vec2 uvMax = vec2(1.0) - halfTexel;

    vec4 blur = sampleLinear(0.0, uvMin, uvMax);
    float wsum = 1.0;

    for (float start = 0.0; start < actualRadius; start += stride) {
        float end = min(actualRadius, start + stride);
        float width = end - start;
        if (width <= 0.0001) {
            break;
        }

        float mainOffset = start + width * 0.5;
        float wMain = gaussianWeight(mainOffset, invTwoSigmaSq) * width;

        blur += sampleLinear(mainOffset, uvMin, uvMax) * wMain;
        blur += sampleLinear(-mainOffset, uvMin, uvMax) * wMain;
        wsum += 2.0 * wMain;

    }

    fragColor = vec4(blur.rgb / max(wsum, 1e-4), 1.0);
}
