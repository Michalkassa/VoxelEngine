#version 330 core
out vec3 Direction;
uniform mat4 inverse_view_projection;

// One triangle big enough to cover the whole screen, built from the vertex index alone
void main()
{
    vec2 position = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2)) * 2.0 - 1.0;
    gl_Position = vec4(position, 1.0, 1.0);
    // Turns the screen position back into the direction the camera is looking through it
    vec4 world = inverse_view_projection * vec4(position, 1.0, 1.0);
    Direction = world.xyz / world.w;
}
