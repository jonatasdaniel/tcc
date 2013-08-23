package lu.tudor.santec.dicom.gui.utils;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TimeRenderer extends DefaultTableCellRenderer{
	private static final long serialVersionUID = 1L;
	DateFormat df = new SimpleDateFormat("HH:mm");
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String time = "----";
		try {
			time = df.format((Date) value);
		} catch (Exception e) {
		}
		return super.getTableCellRendererComponent(table, time, isSelected, hasFocus,
				row, column);
	}

}
