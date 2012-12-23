package com.android.game.clash_of_the_balls.game;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.game.clash_of_the_balls.ShaderManager;

/**
 * RenderHelper
 * 
 * this class handles the projection & model view matrices. 
 * it can also select the shaders
 * it is used for drawing calls
 * 
 * Model view matrix: apply the transformations in reversed order!
 * 
 * apply projection view before doing ANY model transformations!
 *
 */
public class RenderHelper {
	private static final String LOG_TAG = "RenderHelper";
	
	private ShaderManager m_shader_manager;
	
	private float m_screen_width;
	private float m_screen_height;
	
	private static final int mat_size = 16; // = 4x4
	
	private float[] m_projection_mat = new float[mat_size];
	
	//model view matrix stack
	private float[] m_model_mat;
	private int m_cur_model_mat_pos;
	private int m_max_model_mat_pos;
	
	public float screenWidth() { return m_screen_width; }
	public float screenHeight() { return m_screen_height; }
	
	public ShaderManager shaderManager() { return m_shader_manager; }
	
	public RenderHelper(ShaderManager shader_manager, float screen_width, 
			float screen_height) {
		m_shader_manager = shader_manager;
		
		final int init_model_mat_count = 6;
		m_model_mat = new float[init_model_mat_count*mat_size];
		m_cur_model_mat_pos=0;
		m_max_model_mat_pos = mat_size*(init_model_mat_count - 1);
		Matrix.setIdentityM(m_model_mat, m_cur_model_mat_pos);
		
		onSurfaceChanged(screen_width, screen_height);
	}
	
	public void onSurfaceChanged(float screen_width, float screen_height) {
		m_screen_width = screen_width;
		m_screen_height = screen_height;
	}
	
	
	/* projection matrix */
	public void useOrthoProjection() {
		Matrix.orthoM(m_projection_mat, 0, 0.f, m_screen_width, 0.f
				, m_screen_height, 0.f, 1.f);
		modelMatSetIdentity();
	}
	
	
	/* Model view matrix stack */
	public float[] modelMat() { return m_model_mat; }
	public int modelMatPos() { return m_cur_model_mat_pos; }
	
	//returns the new modelMat position
	//creates a copy of the current matrix on top of the stack
	public int pushModelMat() {
		if(m_cur_model_mat_pos >= m_max_model_mat_pos)
			resizeModelMat(m_model_mat.length*2);
		for(int i=0; i<mat_size; ++i) {
			m_model_mat[m_cur_model_mat_pos+i+mat_size] = 
					m_model_mat[m_cur_model_mat_pos+i];
		}
		m_cur_model_mat_pos+=mat_size;
		return m_cur_model_mat_pos;
	}
	
	public int popModelMat() {
		
		m_cur_model_mat_pos-=mat_size;
		
		return m_cur_model_mat_pos;
	}
	
	//model matrix operations
	public void modelMatScale(float scale_x, float scale_y, float scale_z) {
		Matrix.scaleM(m_model_mat, m_cur_model_mat_pos, scale_x, scale_y, scale_z);
	}
	public void modelMatTranslate(float x, float y, float z) {
		Matrix.translateM(m_model_mat, m_cur_model_mat_pos, x, y, z);
	}
	private float[] m_tmp_rot_mat = new float[mat_size];
	
	public void modelMatRotate(float alpha_degree, float x, float y, float z) {
        Matrix.setRotateM(m_tmp_rot_mat, 0, alpha_degree, x, y, z);
        pushModelMat();
        Matrix.multiplyMM(m_model_mat, m_cur_model_mat_pos-mat_size
        		, m_model_mat, m_cur_model_mat_pos, m_tmp_rot_mat, 0);
        popModelMat();
	}
	public void modelMatSetIdentity() {
		//we can directly set the projection matrix here because
		// proj mat * Identity = proj mat
		for(int i=0; i<mat_size; ++i) 
			m_model_mat[m_cur_model_mat_pos+i] = m_projection_mat[i];
	}
	
	
	private void resizeModelMat(int new_size) {
		
		Log.w(LOG_TAG, "need to resize model view matrix. new size="+new_size);
		
		float new_mat[]=new float[new_size];
		for(int i=0; i<Math.min(new_size, m_model_mat.length); ++i)
			new_mat[i] = m_model_mat[i];
		m_model_mat = new_mat;
		
		m_max_model_mat_pos = m_model_mat.length-mat_size;
	}
	
	
	//call this right before rendering the object to apply the projection 
	//& model matrices
	public void apply() {
		
        // Pass in the matrix to the shader.
        GLES20.glUniformMatrix4fv(m_shader_manager.u_MVPMatrix_handle, 1, false
        		, m_model_mat, m_cur_model_mat_pos);
	}
}
