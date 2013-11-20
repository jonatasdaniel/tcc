package br.furb.rma.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;

public class AxialContainer extends Container {

	private SeekBar seekBar;
	private ImageView imageView;
	private TextView tvCurrentImage;
	
	public AxialContainer(Context context, Dicom dicom) {
		super(context, dicom, R.layout.axial_container);
		
		seekBar = (SeekBar) findViewById(R.axial.seek_bar);
		seekBar.setMax(dicom.getImages().size()-1);
		seekBar.setOnSeekBarChangeListener(seekBarListener());
		imageView = (ImageView) findViewById(R.axial.image_view);
		tvCurrentImage = (TextView) findViewById(R.axial.current_image);
		
		setImage(0);
	}
	
	private void setImage(int index) {
		imageView.setImageBitmap(getDicom().getImages().get(index).getBitmap());
		
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