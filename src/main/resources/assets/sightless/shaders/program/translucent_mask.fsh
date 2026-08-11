#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D SphereSampler;

uniform mat4 InvViewProjMat;
uniform int SphereCount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float depth = texture(DiffuseDepthSampler, texCoord).r;

    vec4 ndc = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 worldPos = InvViewProjMat * ndc;
    worldPos /= worldPos.w;

    bool revealed = false;
    for (int i = 0; i < SphereCount; i++) {
        vec4 sphere = texelFetch(SphereSampler, ivec2(i, 0), 0);
        vec3 d = worldPos.xyz - sphere.xyz;
        if (dot(d, d) < sphere.w) {
            revealed = true;
            break;
        }
    }

    vec4 sceneColor = texture(DiffuseSampler, texCoord); //alpha left alone so blending happens fr
    fragColor = revealed ? sceneColor : vec4(0.0, 0.0, 0.0, sceneColor.a);
}