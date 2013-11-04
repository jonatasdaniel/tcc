package br.furb.rma.activities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import br.furb.rma.models.Camera;
import br.furb.rma.view.Square;

public class ViewerRenderer implements Renderer {

	private Square square;
	private Context context;
	private Camera camera;
	
	private boolean cameraChanged = true;
	
	public ViewerRenderer(Square square, Context context, Camera camera) {
		this.square = square;
		this.context = context;

		this.camera = camera;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL10.GL_STENCIL_BUFFER_BIT);
		
		// Reset the Modelview Matrix
		gl.glLoadIdentity();
		
		if(cameraChanged) {
			GLU.gluLookAt(gl, camera.getEyeX(), camera.getEyeY(), camera.getEyeZ(), 
					camera.getCenterX(), camera.getCenterY(), camera.getCenterZ(),
					camera.getUpX(), camera.getUpY(), camera.getUpZ());
		}
		
		// Draw the triangle
		square.draw(gl);    
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) {
			height = 1;
		}

		gl.glViewport(0, 0, width, height); // Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
		gl.glLoadIdentity(); // Reset The Projection Matrix
		
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				100.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
		gl.glLoadIdentity();
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		square.loadGLTextures(gl, this.context);

		gl.glEnable(GL10.GL_TEXTURE_2D); // Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); // Black Background
		gl.glClearDepthf(1.0f); // Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); // Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); // The Type Of Depth Testing To Do

		// Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
		cameraChanged = true;
	}
}
