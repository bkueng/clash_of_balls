precision mediump float;       	// Set the default precision to medium. We don't need as high of a 
								// precision in the fragment shader.
uniform sampler2D u_Texture;    // The input texture.
varying float v_time;
  
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

  
// The entry point for our fragment shader.
void main()                    		
{                              

/*
	float PI = 3.14159265358979323846264;

	vec2 tex_coord = v_TexCoordinate;
	float xoff = sin(tex_coord.y*2.0*PI + v_time*3.0)*0.05;
	float yoff = cos(tex_coord.x*2.0*PI + v_time*3.0)*0.05;
	tex_coord.x+=xoff;
	tex_coord.y+=yoff;
    vec4 color = texture2D(u_Texture, tex_coord);

	if(tex_coord.x > 1.0 || tex_coord.x < 0.0
		|| tex_coord.y > 1.0 || tex_coord.y < 0.0) {
		color = vec4(0.0);
	}

	gl_FragColor = color;

//*/

//*
	//warp like water waves
	vec2 middle = vec2(0.5, 0.5);
	vec2 d = v_TexCoordinate-middle;
	vec2 tex_coord = middle + d*(1.0 + sin(length(d)*50.0 - v_time*4.0)/5.0);

    gl_FragColor = texture2D(u_Texture, tex_coord);

	//*/


}                                                                     	

