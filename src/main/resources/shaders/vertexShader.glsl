#version 330 core
layout (location = 0) in uint aPacked;
out vec2 TexCoord;
out float Ambient;
uniform vec3 chunk_offset;
uniform float voxel_scale;
uniform mat4 view_transform;
uniform mat4 projection_transform;

// Brightness for each ambient occlusion level, from fully enclosed (0) to fully open (3)
const float AO_BRIGHTNESS[4] = float[4](0.45, 0.65, 0.82, 1.0);

void main()
{
    vec3 local = vec3(float(aPacked & 31u), float((aPacked >> 5u) & 511u), float((aPacked >> 14u) & 31u));
    vec3 worldPos = chunk_offset + local * voxel_scale - 0.5;
    gl_Position = projection_transform * view_transform * vec4(worldPos, 1.0);
    TexCoord = vec2(float((aPacked >> 19u) & 31u), float((aPacked >> 24u) & 31u)) / 16.0;
    Ambient = AO_BRIGHTNESS[(aPacked >> 29u) & 3u];
}
