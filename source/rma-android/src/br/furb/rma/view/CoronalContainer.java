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

public class CoronalContainer extends Container {

	private SeekBar seekBar;
	private ImageView imageView;
	private TextView tvCurrentImage;
	
	public CoronalContainer(Context context, Dicom dicom) {
		super(context, dicom, R.layout.coronal_container);
		
		seekBar = (SeekBar) findViewById(R.coronal.seek_bar);
		seekBar.setMax(dicom.getImages().get(0).getColumns()-1);
		seekBar.setOnSeekBarChangeListener(seekBarListener());
		seekBar.setProgress(seekBar.getMax() / 2);
		imageView = (ImageView) findViewById(R.coronal.image_view);
		tvCurrentImage = (TextView) findViewById(R.coronal.current_image);
		
		setImage(seekBar.getProgress());
	}
	
	private void setImage(int index) {
		List<DicomImage> images = getDicom().getImages();
		
		final int CONST = 5;
		int[] pixels = new int[getDicom().getImages().get(0).getRows() * getDicom().getImages().size() * CONST];
		int count = 0;
		
		int rows = getDicom().getImages().get(0).getMatrix().length;
		for (int row = 0; row < rows; row++) {
			for (int i = 0; i < images.size(); i++) {
				DicomImage image = images.get(i);
				int[][] matrix = image.getMatrix();
				for (int j = 0; j < CONST; j++) {
					pixels[count++] = matrix[row][index];
				}
			}
		}
		
		DicomImage image = new DicomImage();
		image.setColumns(getDicom().getImages().size() * CONST);
		image.setRows(rows);
		
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