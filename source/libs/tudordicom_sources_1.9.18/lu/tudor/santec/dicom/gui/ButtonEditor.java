package lu.tudor.santec.dicom.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class ButtonEditor extends DefaultCellEditor {

	public ButtonEditor() {
		super(new JCheckBox());
	}

	private static final long serialVersionUID = 1L;

	protected JButton button;

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.button = (JButton) value;
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} else {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		return button;
	}

	public Object getCellEditorValue() {
		return button;
	}

	public boolean stopCellEditing() {
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}