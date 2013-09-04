package br.furb.dicomreader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

public class GLRenderer implements GLSurfaceView.Renderer {

	private Bitmap image;
	private FloatBuffer mFVertexBuffer;
	
	public GLRenderer(Bitmap image) {
		this.image = image;
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); 
		vbb.order(ByteOrder.nativeOrder()); 
		mFVertexBuffer = vbb.asFloatBuffer(); 
		mFVertexBuffer.put(vertices); 
		mFVertexBuffer.position(0); 
	}
	
	private int textures[] = new int[1];
	
	private float vertices[] =
			{
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f
			};
	
	private float triangleCoords[] = { // in counterclockwise order:
	         0.0f,  0.622008459f, 0.0f,   // top
	         -0.5f, -0.311004243f, 0.0f,   // bottom left
	          0.5f, -0.311004243f, 0.0f    // bottom right
	     };
	
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); 
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
		image.recycle();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
	}

}
