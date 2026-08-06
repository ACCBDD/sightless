#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D LidarSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 lidar = texture(LidarSampler, texCoord);
    fragColor = vec4(lidar.rgb * lidar.a, 1.0);
}