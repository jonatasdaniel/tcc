package lu.tudor.santec.dicom.gui;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class ButtonRenderer implements TableCellRenderer {

	public ButtonRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JButton jb = (JButton) value;
		if (jb == null)
			return null;
		
		if (isSelected) {
			jb.setForeground(table.getSelectionForeground());
			jb.setBackground(table.getSelectionBackground());
		} else {
			jb.setForeground(table.getForeground());
			jb.setBackground(UIManager.getColor("Button.background"));
		}
		return jb;
	}
}