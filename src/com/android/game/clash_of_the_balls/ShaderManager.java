package com.android.game.clash_of_the_balls;

import com.android.game.clash_of_the_balls.helper.RawResourceReader;
import com.android.game.clash_of_the_balls.helper.ShaderHelper;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;


/**
 * ShaderManager
 * this class handles all shaders. they are stored in raw resource folder
 *
 */
public class ShaderManager {
	private static final String LOG_TAG = "ShaderManager";
	
	private Context m_activity_context;
	
	public enum ShaderType {
		TypeDefault,
		TypeWarp,
		
		TypeN
	}
	
	private int[] m_programs;
	
	/* uniform & attribute handles of currently loaded shader: 
	 * depends on shader type */
	public int u_MVPMatrix_handle;
	public int u_Texture_handle;
	public int a_Position_handle;
	public int u_Color_handle;
	public int a_TexCoordinate_handle;
	public int u_time_handle;
	
	
	public ShaderManager(Context activity_context) {
		m_activity_context = activity_context;
		m_programs = new int[ShaderType.TypeN.ordinal()];
		for(int i=0; i<m_programs.length; ++i) m_programs[i]=-1;
	}
	
	public void onSurfaceChanged(int width, int height) {
		//we need to reload all shader programs
		for(int i=0; i<m_programs.length; ++i) m_programs[i]=-1;
	}
	
	public void useShader(ShaderType which) {
		
		if(which == ShaderType.TypeN) throw new RuntimeException("invalid Shader type");
		
		//load if not already loaded
		if(m_programs[which.ordinal()] == -1) {
			switch(which) {
			case TypeDefault:
				Log.i(LOG_TAG, "Loading Default Shader");
				m_programs[which.ordinal()] = loadShader(
					R.raw.shader_default_vert, 
					R.raw.shader_default_frag,
					new String[] {"a_Position", "a_TexCoordinate"});
				break;
			case TypeWarp:
				Log.i(LOG_TAG, "Loading Warp Shader");
				m_programs[which.ordinal()] = loadShader(
					R.raw.shader_warp_vert,
					R.raw.shader_warp_frag,
					new String[] {"a_Position", "a_TexCoordinate"});
				break;
			default:
			}
		}
		
		//apply the shader
		final int program_handle = m_programs[which.ordinal()];
        GLES20.glUseProgram(program_handle);
        
        // Set program handles
		switch(which) {
		case TypeDefault:
			u_MVPMatrix_handle = GLES20.glGetUniformLocation(program_handle, "u_MVPMatrix");
			u_Texture_handle = GLES20.glGetUniformLocation(program_handle, "u_Texture");
			a_Position_handle = GLES20.glGetAttribLocation(program_handle, "a_Position");
			u_Color_handle = GLES20.glGetUniformLocation(program_handle, "u_Color");
			a_TexCoordinate_handle = GLES20.glGetAttribLocation(program_handle, "a_TexCoordinate");
			u_time_handle = -1;
			break;
		case TypeWarp:
			u_MVPMatrix_handle = GLES20.glGetUniformLocation(program_handle, "u_MVPMatrix");
			u_Texture_handle = GLES20.glGetUniformLocation(program_handle, "u_Texture");
			a_Position_handle = GLES20.glGetAttribLocation(program_handle, "a_Position");
			u_Color_handle = -1;
			a_TexCoordinate_handle = GLES20.glGetAttribLocation(program_handle, "a_TexCoordinate");
			u_time_handle = GLES20.glGetUniformLocation(program_handle, "time");
			break;
		default:
		}
	}
	
	//tell the shader to use a texture (which=0 if only 1 texture is used)
	public void activateTexture(int which) {
        // Set the active texture unit to texture unit which.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+which);
        
        // Tell the texture uniform sampler to use this texture in the shader 
        // by binding to texture unit which.
        GLES20.glUniform1i(u_Texture_handle, which);
	}
	
	public void deactivateTexture() {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
	
	private int loadShader(int raw_res_id_vertex_shader
			, int raw_res_id_fragment_shader, String[] attributes) {
		
		String vertexShader = getShader(raw_res_id_vertex_shader);   		
 		String fragmentShader = getShader(raw_res_id_fragment_shader);			
		
		final int vertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShader);		
		
		return ShaderHelper.createAndLinkProgram(vertexShaderHandle
				, fragmentShaderHandle, attributes);
	}
	
	private String getShader(int raw_res_id) {
		return RawResourceReader.readTextFileFromRawResource(m_activity_context
				, raw_res_id);
	}


}
