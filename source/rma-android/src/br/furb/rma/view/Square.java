package br.furb.rma.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import br.furb.rma.models.Dicom;

public class Square {

	private Dicom dicom;
	private GL10 gl;
	private Context context;
	
	private FloatBuffer vertexBuffer;
	//private int[] textures = new int[1];
	private int[] textures;
	
	private List<Bitmap> bitmaps;

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
	
	public Square(Dicom dicom, List<Bitmap> bitmaps) {
		this.dicom = dicom;
		
		textures = new int[dicom.getImages().size()];
		this.bitmaps = new ArrayList<Bitmap>();
		//usado para pintar da ultima imagem para a primeira
		for (int i = 0; i < bitmaps.size(); i++) {
			this.bitmaps.add(bitmaps.get(bitmaps.size()-(i+1)));
		}
		
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
	
	public void draw(GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		/*gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);*/
		
		float z = 0.0078125f;
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glDepthMask(false);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		for (int i = 0; i < textures.length; i++) {
			gl.glTranslatef(0.0f, 0.0f, z);
			
			// bind the previously generated texture
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

			// Point to our buffers
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Set the face rotation
			gl.glFrontFace(GL10.GL_CW);

			// Point to our vertex buffer
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

			// Draw the vertices as triangle strip
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
			
			//y += inc;
		}
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDepthMask(true);

		// Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	public void loadGLTextures(GL10 gl, Context context) {
		Bitmap bitmap = null;
		gl.glGenTextures(textures.length, textures, 0);
		
		for (int i = 0; i < textures.length; i++) {
			// generate one texture pointer
			
			// ...and bind it to our array
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			
			// create nearest filtered texture
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
			
			// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
			bitmap = bitmaps.get(i);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		}
		
		// Clean up
		//bitmap.recycle();
	}
	
	public Dicom getDicom() {
		return dicom;
	}

	public void rotate(int progress) {
		
	}
	
}
