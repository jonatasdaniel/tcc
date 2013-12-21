package br.furb.rma.view;

import br.furb.rma.models.Dicom;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class Container extends LinearLayout {

	private Dicom dicom;
	private ContainerListener listener;
	
	public Container(Context context, Dicom dicom, int layoutId) {
		super(context);
		
		this.dicom = dicom;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(layoutId, this);
	}
	
	public void setListener(ContainerListener listener) {
		this.listener = listener;
	}
	
	protected ContainerListener getListener() {
		return listener;
	}
	
	protected Dicom getDicom() {
		return dicom;
	}


}