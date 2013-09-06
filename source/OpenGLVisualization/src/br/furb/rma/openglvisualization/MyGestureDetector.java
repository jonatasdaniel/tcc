package br.furb.rma.openglvisualization;

import br.furb.rma.models.Dicom;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;

public class MyGestureDetector extends SimpleOnGestureListener {
	
	private ImageView imageView;
	private Dicom dicom;
	private int index = 0;
	
	static int WIDTH = 100;
	
	public MyGestureDetector(ImageView imageView, Dicom dicom) {
		super();
		this.imageView = imageView;
		this.dicom = dicom;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		
		if(e1.getX() - e2.getX() > WIDTH) {
			if(index + 1 < dicom.getImages().size()) {
				imageView.setImageBitmap(dicom.getImages().get(++index).createBitmap());
			}
		} else if(e2.getX() - e1.getX() > WIDTH) {
			if(index > 0) {
				imageView.setImageBitmap(dicom.getImages().get(--index).createBitmap());
			}
			
		}
		
		return super.onFling(e1, e2, velocityX, velocityY);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

}
