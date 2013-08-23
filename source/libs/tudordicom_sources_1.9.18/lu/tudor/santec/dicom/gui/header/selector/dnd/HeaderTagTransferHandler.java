package lu.tudor.santec.dicom.gui.header.selector.dnd;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import lu.tudor.santec.dicom.gui.header.DicomHeaderTableModel;


public class HeaderTagTransferHandler  extends TransferHandler{

    	private DicomHeaderTableModel model;

	public HeaderTagTransferHandler(DicomHeaderTableModel model) {
    	    this.model = model;
    	}
    
	private static final long serialVersionUID = 1L;

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTable table = (JTable) c;
//		HeaderTag ht = model.getHeaderTag(table.getSelectedRow());
		int[] rows = table.getSelectedRows();
		Object[] items = new Object[rows.length];
		for (int i = 0; i < rows.length; i++) {
		    items[i] = model.getHeaderTag(rows[i]);
		}
		return new HeaderTagTransferable(items);
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}
}
