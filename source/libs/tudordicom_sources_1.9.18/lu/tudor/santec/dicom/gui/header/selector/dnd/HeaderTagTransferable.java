package lu.tudor.santec.dicom.gui.header.selector.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class HeaderTagTransferable implements Transferable {
	    private Object data;
	    private static final DataFlavor[] flavors = new DataFlavor[1];
	    
	    static {flavors[0] = DataFlavor.stringFlavor;}
	   
	    public HeaderTagTransferable(final Object data) {
	        super();
	        this.data = data;
	    }
	    
	    public DataFlavor[] getTransferDataFlavors() {
	    	return flavors;
	    }
	    
	    public boolean isDataFlavorSupported(final DataFlavor flavor) {
	    	return true;
	    }
	    
	    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
	        return data;
	    }
	}
