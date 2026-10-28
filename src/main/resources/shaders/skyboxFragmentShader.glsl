#version 330 core
out vec4 FragColor;
in vec3 Direction;
uniform samplerCube sky;

void main()
{
    FragColor = vec4(texture(sky, Direction).rgb, 1.0);
}
