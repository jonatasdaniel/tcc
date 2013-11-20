package br.furb.rma.view;

import br.furb.rma.models.Dicom;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class Container extends LinearLayout {

	private Dicom dicom;
	
	public Container(Context context, Dicom dicom, int layoutId) {
		super(context);
		
		this.dicom = dicom;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(layoutId, this);
	}
	
	protected Dicom getDicom() {
		return dicom;
	}


}