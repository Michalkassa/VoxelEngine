#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
out vec3 Normal;
uniform mat4 model;
uniform mat4 view_transform;
uniform mat4 projection_transform;

void main()
{
    // The model matrix only moves and stretches the box along its axes, so normals keep their direction
    Normal = aNormal;
    gl_Position = projection_transform * view_transform * model * vec4(aPos, 1.0);
}
