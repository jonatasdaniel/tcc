package br.furb.rma.view;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import br.furb.rma.models.Dicom;

public class Square3D {

	private final Dicom dicom;

	public Square3D(Dicom dicom) {
		super();
		this.dicom = dicom;
	}
	
	public int loadTexture() {
		Bitmap bitmap = dicom.getImages().get(0).getBitmap();
		
		int[] textureHandle = new int[1];
		if(textureHandle[0] != 0) {
			//bind texture
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			//set filering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        
	        //load bitmap into bound texture
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		}
		
		if(textureHandle[0] == 0) {
			throw new RuntimeException("Erro ao carregar textura");
		}
		
		return textureHandle[0];
	}

}