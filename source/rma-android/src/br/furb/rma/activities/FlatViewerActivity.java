package br.furb.rma.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;
import br.furb.rma.reader.DicomReader;

public class FlatViewerActivity extends Activity {

	private ImageView imageView;
	private TextView tvDescription;
	private SeekBar seekBar;
	private Dicom dicom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flat_viewer_activity);
		
		seekBar = (SeekBar) findViewById(R.flat_viewer.seek_bar);
		
		try {
			dicom = DicomReader.getLastDicomReaded();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		tvDescription = (TextView) findViewById(R.flat_viewer.description);
		
		seekBar.setMax(dicom.getImages().size());
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setImage(progress);
			}
		});

		imageView = (ImageView) findViewById(R.flat_viewer.image_view);
		setImage(0);
	}

	private void setImage(int index) {
		DicomImage image = dicom.getImages().get(index);
		imageView.setImageBitmap(image.getBitmap());
		StringBuilder builder = new StringBuilder();
		builder.append("Mostrando ").append(index+1).append(" de ").append(dicom.getImages().size());
		tvDescription.setText(builder.toString());
	}
	

}
