package br.furb.rma.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.DicomFile;

public class DicomFileAdapter extends BaseListAdapter<DicomFile> {

	public DicomFileAdapter(Context context, List<DicomFile> itens) {
		super(context, itens);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dicom_file_item, null);
		TextView textView = (TextView) layout.findViewById(R.dicom_file.name);
		DicomFile dicomFile = getItem(position);
		textView.setText(dicomFile.getName());
		
		return layout;
	}

}
