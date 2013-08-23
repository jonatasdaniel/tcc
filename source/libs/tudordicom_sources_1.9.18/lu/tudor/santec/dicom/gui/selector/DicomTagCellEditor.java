package lu.tudor.santec.dicom.gui.selector;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import lu.tudor.santec.dicom.gui.AutoCompletion;
//import lu.tudor.santec.dicom.gui.header.DicomDictionary;
import lu.tudor.santec.dicom.gui.header.DicomHeader;

public class DicomTagCellEditor extends AbstractCellEditor implements TableCellEditor{

	private static final long serialVersionUID = 1L;
	JComboBox cb = new JComboBox();
	
	public DicomTagCellEditor() {
		
//		SortedSet set = new TreeSet(DicomDictionary.getDictionary().keySet());
		
//		for (Iterator iter = set.iterator(); iter.hasNext();) {
//			String tagNr = (String) iter.next();
//			cb.addItem(tagNr.substring(0,4) + "," + tagNr.substring(4,8));
//		};
		
		Field[] fields = Tag.class.getFields();
		DicomObject de = new BasicDicomObject();
		for (int i = 0; i < fields.length; i++) {
			String name = fields[i].getName();
			int tag = Tag.forName(name);
			String tagNr = DicomHeader.toTagString(tag);
			cb.addItem(tagNr);
		}
		
		
		cb.setEditable(true);
		AutoCompletion.enableWithFreeText(cb);
		cb.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				String tagName = DicomHeader.getHeaderName((String)value);
				String text = "<html>" + value + "<br><span style=\"font-size:7px\">" + tagName + "</span>";
				JLabel c = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
				c.setToolTipText(tagName);
				return c;
			}
		});
	}
	
	public Object getCellEditorValue() {
		return cb.getSelectedItem();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		cb.setSelectedItem(value);
		return cb;
	}

}
