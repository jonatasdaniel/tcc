package br.furb.rma.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Square {

	private FloatBuffer vertexBuffer;
	//private int[] textures = new int[1];
	private int[] textures;
	private double spacingBetweenSlices;
	
	private List<Bitmap> bitmaps;

	private float vertices[] = {
			-1.0f, -1.0f,  0.0f,        // V1 - bottom left
			-1.0f,  1.0f,  0.0f,        // V2 - top left
			1.0f, -1.0f,  0.0f,        // V3 - bottom right
			1.0f,  1.0f,  0.0f         // V4 - top right
		};
	
	private byte indices[] = {
			//Faces definition
    		0,1,3, 0,3,2, 			//Face front
    		4,5,7, 4,7,6, 			//Face right
    		8,9,11, 8,11,10, 		//... 
    		12,13,15, 12,15,14, 	
    		16,17,19, 16,19,18, 	
    		20,21,23, 20,23,22, 	
								};
	
	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {    		
		// Mapping coordinates for the vertices
		0.0f, 1.0f,		// top left		(V2)
		0.0f, 0.0f,		// bottom left	(V1)
		1.0f, 1.0f,		// top right	(V4)
		1.0f, 0.0f		// bottom right	(V3)
	};
	private ByteBuffer indexBuffer;
	
	public Square(List<Bitmap> bitmaps) {		
		textures = new int[bitmaps.size()];
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
		
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	public void draw(GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		float z = 0.0078125f;
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glDepthMask(false);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		for (int i = 0; i < textures.length; i++) {
			gl.glTranslatef(0.0f, 0.0f, z);
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			
			gl.glFrontFace(GL10.GL_CW);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		}
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glDisable(GL10.GL_BLEND);
		gl.glDepthMask(true);
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	public void loadGLTextures(GL10 gl, double spacingBetweenSlices) {
		//this.spacingBetweenSlices = spacingBetweenSlices;
		Bitmap bitmap = null;
		gl.glGenTextures(textures.length, textures, 0);
		
		for (int i = 0; i < textures.length; i++) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
			
			bitmap = bitmaps.get(i);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		}
	}

	public int[] getTextures() {
		return textures;
	}
	
}