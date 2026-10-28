#version 330 core
layout (location = 0) in uint aPacked;
uniform vec3 chunk_offset;
uniform float voxel_scale;
uniform mat4 light_space;

// Same position unpacking as vertexShader.glsl, projected from the sun instead of the camera
void main()
{
    vec3 local = vec3(float(aPacked & 31u), float((aPacked >> 5u) & 511u), float((aPacked >> 14u) & 31u));
    vec3 worldPos = chunk_offset + local * voxel_scale - 0.5;
    gl_Position = light_space * vec4(worldPos, 1.0);
}
