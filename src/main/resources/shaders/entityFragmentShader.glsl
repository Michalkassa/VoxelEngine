#version 330 core
in vec3 Normal;
out vec4 FragColor;
uniform vec3 colour;
uniform vec3 sun_direction;

void main()
{
    float sunlight = max(dot(normalize(Normal), sun_direction), 0.0);
    FragColor = vec4(colour * (0.45 + 0.55 * sunlight), 1.0);
}
