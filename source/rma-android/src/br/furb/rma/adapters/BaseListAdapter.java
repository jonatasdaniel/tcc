package br.furb.rma.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter<T> extends BaseAdapter {

	private final Context context;
	private List<T> itens;
	private LayoutInflater inflater;
	
	public BaseListAdapter(Context context) {
		this.context = context;
	}
	
	public BaseListAdapter(Context context, List<T> itens) {
		this(context);
		this.itens = itens;
	}
	
	public void addItem(T item) {
		if(itens == null) {
			itens = new ArrayList<T>();
		}
		
		itens.add(item);
	}
	
	public Context getContext() {
		return context;
	}
	
	public LayoutInflater getLayoutInflater() {
		if(inflater == null) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		return inflater;
	}
	
	@Override
	public int getCount() {
		if(itens != null) {
			return itens.size();
		} else {
			return 0;
		}
	}

	@Override
	public T getItem(int position) {
		if(itens != null) {
			return itens.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
