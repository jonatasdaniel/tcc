package br.furb.rma.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;

public class DicomFileAdapter extends BaseListAdapter<Dicom> {

	public DicomFileAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dicom_file_item, null);
		TextView textView = (TextView) layout.findViewById(R.dicom_file.name);
		Dicom dicomFile = getItem(position);
		textView.setText(dicomFile.getName());
		
		return layout;
	}

}
