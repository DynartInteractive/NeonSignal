#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;

void main() 
{
	gl_FragColor = texture2D(u_texture, v_texCoords) * texture2D(u_texture2, v_texCoords);
}
