package lu.tudor.santec.dicom.gui.header.selector.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTable;

import lu.tudor.santec.dicom.gui.header.DicomHeaderTableModel;
import lu.tudor.santec.dicom.gui.header.HeaderTag;

public class DicomHeaderTableDropTargetListener implements DropTargetListener{
    
    private JTable table;
    private DicomHeaderTableModel model;

    public DicomHeaderTableDropTargetListener(JTable table, DicomHeaderTableModel model) {
	this.table = table;
	this.model = model;
    }

    public void dragEnter(DropTargetDragEvent dtde) {}
    public void dragExit(DropTargetEvent dte) {   }
    public void dragOver(DropTargetDragEvent dtde) {   }
    public void dropActionChanged(DropTargetDragEvent dtde) {    }

    public void drop(DropTargetDropEvent dtde) {
	    try {
		Object[] types =  (Object[]) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
		for (int i = 0; i < types.length; i++) {
			HeaderTag ht = (HeaderTag) types[i];
			HeaderTag htNew = new HeaderTag();
			htNew.setTagNr(ht.getTagNr());
			htNew.setTagName(ht.getTagName());
			htNew.setTagValue(ht.getTagValue());
			htNew.setTagType(ht.getTagVR());
			model.addTag(ht);
		}
	} catch (Exception e) {
		e.printStackTrace();
	} 
	dtde.dropComplete(true);
    }

}
