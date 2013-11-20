package br.furb.rma.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.activities.VolumetricViewerActivity;
import br.furb.rma.models.Dicom;

public class VolumetricContainer extends Container {

	private TextView tvCurrentImage;
	private Button btnVolumetric;
	
	public VolumetricContainer(Context context, Dicom dicom) {
		super(context, dicom, R.layout.volumetric_container);
		
		tvCurrentImage = (TextView) findViewById(R.volumetric.current_image);
		btnVolumetric = (Button) findViewById(R.volumetric.button);
		btnVolumetric.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getContext().startActivity(new Intent(getContext(), VolumetricViewerActivity.class));
			}
		});
	}

}