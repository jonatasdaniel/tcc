package br.furb.rma.view;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;

public class AxialContainer extends Container {

	private SeekBar seekBar;
	private ImageView imageView;
	private TextView tvCurrentImage;
	
	public AxialContainer(Context context, Dicom dicom) {
		super(context, dicom, R.layout.axial_container);
		
		seekBar = (SeekBar) findViewById(R.axial.seek_bar);
		//seekBar.setMax(dicom.getImages().get(0).getColumns()-1);
		seekBar.setMax(dicom.getImages().get(0).getRows()-1);
		seekBar.setOnSeekBarChangeListener(seekBarListener());
		seekBar.setProgress(seekBar.getMax() / 2);
		imageView = (ImageView) findViewById(R.axial.image_view);
		tvCurrentImage = (TextView) findViewById(R.axial.current_image);
		
		setImage(seekBar.getProgress());
	}
	
	private void setImage(int index) {
		List<DicomImage> images = getDicom().getImages();
		
		final int CONST = 5;
		int[] pixels = new int[getDicom().getImages().get(0).getColumns() * getDicom().getImages().size() * CONST];
		int count = 0;
		
		for (int i = images.size() -1; i >= 0; i--) {
			DicomImage image = images.get(i);
			int[] pixelData = image.getPixelData();
			
			int indexIni = index * image.getColumns();
			for (int k = 0; k < CONST; k++) {
				for (int j = 0; j < image.getColumns(); j++) {
					pixels[count++] = pixelData[indexIni + j];
				}
			}
		}
		
		DicomImage image = new DicomImage();
		image.setColumns(getDicom().getImages().get(0).getColumns());
		image.setRows(getDicom().getImages().size() * CONST);
		
		Bitmap bitmap = image.createBitmap(pixels);
		
		imageView.setImageBitmap(bitmap);
		
		StringBuilder builder = new StringBuilder();
		builder.append("(").append(seekBar.getProgress() + 1).append("/").append(seekBar.getMax()+1).append(")");
		tvCurrentImage.setText(builder.toString());
	}
	
	private SeekBar.OnSeekBarChangeListener seekBarListener() {
		return new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setImage(seekBar.getProgress());
				getListener().onImageChanged(seekBar.getProgress());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
			}
		};
	}

}