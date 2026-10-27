#version 330 core
out vec4 FragColor;
in vec2 TexCoord;
in float Ambient;
uniform sampler2D ourTexture;
void main()
{
    vec4 colour = texture(ourTexture, TexCoord);
    FragColor = vec4(colour.rgb * Ambient, colour.a);
}
