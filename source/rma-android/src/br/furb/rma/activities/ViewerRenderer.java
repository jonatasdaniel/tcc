package br.furb.rma.activities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import br.furb.rma.models.DicomImage;
import br.furb.rma.view.Square;

public class ViewerRenderer implements Renderer {

	private Square square;
	private Context context;
	
	public ViewerRenderer(Square square, Context context) {
		this.square = square;
		this.context = context;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();

		// Drawing
		gl.glTranslatef(0.0f, 0.0f, -5.0f);     // move 5 units INTO the screen
												// is the same as moving the camera 5 units away
		square.draw(gl);                        // Draw the triangle
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) { // Prevent A Divide By Zero By
			height = 1; // Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); // Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); // Select The Projection Matrix
		gl.glLoadIdentity(); // Reset The Projection Matrix

		// Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); // Select The Modelview Matrix
		gl.glLoadIdentity();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Load the texture for the square
		
		DicomImage image = square.getDicom().getImages().get(0);
		int[] pixels = image.getPixelData();
		
		int linha = 0;
		int coluna = 0;
		for (int i = 0; i < pixels.length; i++) {
			linha = i / 512;
			coluna = i - (linha * 512);
			
			if(linha > 0 && linha < 60) {
				//pixels[i] = Color.TRANSPARENT;
			}
			if(coluna > 0 && coluna < 60) {
				//pixels[i] = Color.TRANSPARENT;
			}
		}
		
		Bitmap b = image.createBitmap();
		
		square.loadGLTexture(gl, this.context, b);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		//gl.glClearColor(1, 1, 1, 1); 	//White Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
	}
	
}
