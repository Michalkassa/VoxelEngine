#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
out vec2 TexCoord;
uniform mat4 model_transform;
uniform mat4 view_transform;
uniform mat4 projection_transform;

void main()
{
    gl_Position = projection_transform * view_transform * model_transform * vec4(aPos, 1.0);
    TexCoord = aTexCoord;
}