#version 330 core
out vec4 FragColor;
in vec2 TexCoord;
in float Ambient;
in vec3 RelativePos;
uniform sampler2D ourTexture;
uniform vec3 sun_direction;

// Light every face gets, and the extra light a face gets when it points straight at the sun
const float SKY_LIGHT = 0.6;
const float SUN_LIGHT = 0.4;

void main()
{
    // Voxel faces are flat, so the change in position across neighbouring pixels gives the exact face normal
    vec3 normal = normalize(cross(dFdx(RelativePos), dFdy(RelativePos)));
    // Visible faces always point back towards the camera
    if (dot(normal, RelativePos) > 0.0) normal = -normal;

    float sunlight = max(dot(normal, sun_direction), 0.0);
    float light = SKY_LIGHT + SUN_LIGHT * sunlight;

    vec4 colour = texture(ourTexture, TexCoord);
    FragColor = vec4(colour.rgb * Ambient * light, colour.a);
}
