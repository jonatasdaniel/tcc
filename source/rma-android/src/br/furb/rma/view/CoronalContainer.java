package br.furb.rma.view;

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
		imageView = (ImageView) findViewById(R.coronal.image_view);
		tvCurrentImage = (TextView) findViewById(R.coronal.current_image);
		
		setImage(0);
	}
	
	private void setImage(int index) {
		int x = 0;
		int[][] foda = new int[getDicom().getImages().size()][getDicom().getImages().get(0).getColumns()];
		for (DicomImage image : getDicom().getImages()) {
			int[][] matrix = image.getMatrix();
			foda[x++] = matrix[index];
		}
		
		DicomImage image = new DicomImage();
		image.setColumns(foda.length);
		image.setRows(foda[0].length);
		int[] pixelData = new int[image.getColumns() * image.getRows()];
		int count = 0;
		for (int i = 0; i < foda.length; i++) {
			for (int j = 0; j < foda[i].length; j++) {
				pixelData[count++] = foda[i][j];
			}
		}
		
		Bitmap bitmap = image.createBitmap(pixelData);
		
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