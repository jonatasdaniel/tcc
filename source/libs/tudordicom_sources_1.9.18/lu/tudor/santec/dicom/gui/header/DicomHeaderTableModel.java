package lu.tudor.santec.dicom.gui.header;

import java.util.Vector;

import lu.tudor.santec.dicom.gui.utils.FilterTableModel;
import lu.tudor.santec.dicom.gui.utils.TableFilter;
import lu.tudor.santec.dicom.gui.utils.TableFulltextFilter;
import lu.tudor.santec.i18n.Translatrix;

public class DicomHeaderTableModel extends FilterTableModel {

	private static final long serialVersionUID = 1L;

	private Vector<HeaderTag> headerTags = new Vector<HeaderTag>();

	private String[] columns = { Translatrix.getTranslationString("dicom.Tag.VR"), Translatrix.getTranslationString("dicom.Tag.NR"), Translatrix.getTranslationString("dicom.Tag.Name"), Translatrix.getTranslationString("dicom.Tag.Value") };

	private int[] columnSizes = { 30, 90, 200, 200 };

	private boolean showValue;

	private TableFulltextFilter nrFilter;

	private TableFulltextFilter nameFilter;

	private boolean editable;

	public DicomHeaderTableModel(Vector<HeaderTag> headerTags, boolean showValue, boolean editable) {
		setTags(headerTags);
		this.showValue = showValue;
		this.editable = editable;
	}

	public int getColumnCount() {
		if (showValue)
			return 4;
		else
			return 3;
	}

	public int getRowCount() {
		if (isFiltering()) {
			return getFilteredRowCount();
		}
		return headerTags.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {

		if (isFiltering()) {
			rowIndex = filteredToModel(rowIndex);
		}
		HeaderTag ht = headerTags.get(rowIndex);
		if (ht == null)
			return "";
		switch (columnIndex) {
		case 0:
			return ht.getTagVR();
		case 1:
			return ht.getTagNr();
		case 2:
			return ht.getTagName();
		case 3:
			return ht.getTagValue();
		default:
			return "";
		}
	}

	public void setTags(Vector<HeaderTag> headerTags) {
		this.headerTags = headerTags;
		if (this.headerTags == null)
			this.headerTags = new Vector<HeaderTag>();
		setTableData(headerTags);
		this.fireTableDataChanged();
	}

	public void addTag(HeaderTag headerTag) {
		this.headerTags.add(headerTag);
		setTableData(headerTags);
		this.fireTableDataChanged();
	}

	public void removeTag(HeaderTag headerTag) {
		this.headerTags.remove(headerTag);
		setTableData(headerTags);
		this.fireTableDataChanged();
	}

	public HeaderTag getHeaderTag(int line) {
		try {
			if (isFiltering()) {
				line = filteredToModel(line);
			}
			return headerTags.get(line);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	public int getColumnSize(int column) {
		return columnSizes[column];
	}

	public TableFilter getNrFilter() {
		if (nrFilter == null) {
			this.nrFilter = new TableFulltextFilter();
			installFilter(nrFilter, 1);
		}
		return nrFilter;
	}

	public TableFilter getNameFilter() {
		if (nameFilter == null) {
			this.nameFilter = new TableFulltextFilter();
			installFilter(nameFilter, 2);
		}
		return nameFilter;
	}

	public Vector<HeaderTag> getHeaderTags() {
		return this.headerTags;
	}

	public void moveRow(int rowFrom, int toRow) {
		HeaderTag ht = headerTags.get(rowFrom);
		headerTags.remove(rowFrom);
		headerTags.add(toRow, ht);
		fireTableDataChanged();
	}

	/**
	 * @return the showValue
	 */
	public boolean isShowValue() {
		return showValue;
	}

	/**
	 * @param showValue
	 *            the showValue to set
	 */
	public void setShowValue(boolean showValue) {
		this.showValue = showValue;
		this.fireTableStructureChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0 || columnIndex == 2)
			return false;
		return this.editable && columnIndex > 0;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (isFiltering()) {
			rowIndex = filteredToModel(rowIndex);
		}
		try {
			HeaderTag ht = headerTags.get(rowIndex);
			if (ht == null)
				return;
			switch (columnIndex) {
			case 1:
				ht.setTagNr((String) aValue);
				String name = DicomHeader.getHeaderName((String) aValue);
				if (name == null || "".equals(name)) 
					name = (String) aValue;
				ht.setTagName((String) aValue);
				break;
			case 2:
				ht.setTagName((String) aValue);
				break;
			case 3:
				ht.setTagValue((String) aValue);
				break;
			default:
				return;
			}
		} catch (Exception e) {
		}
	}

	public int getRow(String tagNr) {
		for (int i = 0; i < getRowCount(); i++) {
			if (tagNr.equals(getValueAt(i, 1)))
				return i;
		}
		return -1;
	}

}
