package br.furb.rma.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Property;

public class DicomDetailAdapter extends BaseListAdapter<Property> {

	public DicomDetailAdapter(Context context, List<Property> itens) {
		super(context, itens);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Property p = getItem(position);
		
		LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dicom_details_item, null);
		TextView tvKey = (TextView) layout.findViewById(R.dicom.key);
		tvKey.setText(p.getKey());
		TextView tvValue = (TextView) layout.findViewById(R.dicom.value);
		tvValue.setText(p.getValue());
		
		return layout;
	}

}
