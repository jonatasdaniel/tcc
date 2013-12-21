package br.furb.rma.activities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import br.furb.rma.models.Camera;
import br.furb.rma.models.Dicom;
import br.furb.rma.view.Square;

public class VolumetricViewerRenderer implements Renderer {

	private Square square;
	private Camera camera;
	private Dicom dicom;
	
	private float vertices[] = {
			-1.0f, -1.0f,  0.0f,        // V1 - bottom left
			-1.0f,  1.0f,  0.0f,        // V2 - top left
			1.0f, -1.0f,  0.0f,        // V3 - bottom right
			1.0f,  1.0f,  0.0f         // V4 - top right
		};
	
	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {    		
		// Mapping coordinates for the vertices
		0.0f, 1.0f,		// top left		(V2)
		0.0f, 0.0f,		// bottom left	(V1)
		1.0f, 1.0f,		// top right	(V4)
		1.0f, 0.0f		// bottom right	(V3)
	};
	
	private FloatBuffer vertexBuffer;
	
	private boolean cameraChanged = true;
	
	public VolumetricViewerRenderer(Square square, Dicom dicom, Camera camera) {
		this.square = square;
		this.dicom = dicom;
		this.camera = camera;
				
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4); 
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}
	
	public void onDrawFrame(GL10 gl) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();
		
		
		GLU.gluLookAt(gl, camera.getEyeX(), camera.getEyeY(), camera.getEyeZ(), 
			camera.getCenterX(), camera.getCenterY(), camera.getCenterZ(),
			camera.getUpX(), camera.getUpY(), camera.getUpZ());
			
		
		square.draw(gl);
	}
	
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

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		square.loadGLTextures(gl);
		
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
