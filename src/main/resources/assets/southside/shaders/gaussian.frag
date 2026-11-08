#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;
uniform vec2 Direction;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

float gaussian(float x, float sigma) {
    return 0.3989422804014327 / sigma * exp(-((x * x) / (2.0 * sigma * sigma)));
}

void main() {
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;
    float sigma = max(Radius / 2.0, 0.1);
    for (float i = -Radius; i <= Radius; i += 1.0) {
        float weight = gaussian(i, sigma);
        vec2 offset = i * TexelSize * Direction;
        color += texture(DiffuseSampler, texCoord + offset) * weight;
        totalWeight += weight;
    }

    fragColor = color / totalWeight;
}