#version 330 core
out vec4 FragColor;
in vec2 TexCoord;
in float Ambient;
in vec3 RelativePos;
uniform sampler2D ourTexture;
uniform sampler2DShadow shadow_map;
uniform vec3 sun_direction;
uniform vec3 camera_position;
uniform mat4 light_space;
uniform float shadow_texel;
uniform samplerCube sky;
uniform float fog_start;
uniform float fog_end;

// Light every face gets, and the extra light a face gets when it points straight at the sun
const float SKY_LIGHT = 0.5;
const float SUN_LIGHT = 0.6;

// How far the shadow lookup is pushed out of the face, in blocks. Without it a face shadows itself in stripes.
const float NORMAL_OFFSET = 0.08;
// Fog takes its colour from a blurred mip level of the sky, so distant terrain melts into the horizon behind it
const float FOG_SKY_BLUR = 4.0;

// Shadows fade out over the outer part of the shadow map so its edge doesn't show as a line
const float SHADOW_FADE_START = 0.85;

// 1 when the sun reaches this point, 0 when something nearer the sun blocks it
float sunVisibility(vec3 worldPos, vec3 normal)
{
    vec4 lightPos = light_space * vec4(worldPos + normal * NORMAL_OFFSET, 1.0);
    vec3 coords = lightPos.xyz * 0.5 + 0.5;
    if (coords.z > 1.0) return 1.0;

    // Average a 3 x 3 block of depth tests for soft edges
    float lit = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            lit += texture(shadow_map, vec3(coords.xy + vec2(x, y) * shadow_texel, coords.z));
        }
    }
    lit /= 9.0;

    vec2 fromCentre = abs(coords.xy * 2.0 - 1.0);
    float fade = smoothstep(SHADOW_FADE_START, 1.0, max(fromCentre.x, fromCentre.y));
    return mix(lit, 1.0, fade);
}

void main()
{
    // Voxel faces are flat, so the change in position across neighbouring pixels gives the exact face normal
    vec3 normal = normalize(cross(dFdx(RelativePos), dFdy(RelativePos)));
    // Visible faces always point back towards the camera
    if (dot(normal, RelativePos) > 0.0) normal = -normal;

    float sunlight = max(dot(normal, sun_direction), 0.0);
    // Faces turned away from the sun get no sunlight anyway, so they skip the shadow lookup
    if (sunlight > 0.0) {
        sunlight *= sunVisibility(RelativePos + camera_position, normal);
    }
    float light = SKY_LIGHT + SUN_LIGHT * sunlight;

    vec4 colour = texture(ourTexture, TexCoord);
    vec3 lit = colour.rgb * Ambient * light;

    // Distant terrain fades into the sky so the edge of the render distance doesn't show
    float distance = length(RelativePos);
    float fog = smoothstep(fog_start, fog_end, distance);
    vec3 fogColour = textureLod(sky, RelativePos / distance, FOG_SKY_BLUR).rgb;
    FragColor = vec4(mix(lit, fogColour, fog), colour.a);
}
