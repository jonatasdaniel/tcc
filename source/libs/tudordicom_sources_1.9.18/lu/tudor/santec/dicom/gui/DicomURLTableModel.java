package lu.tudor.santec.dicom.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import lu.tudor.santec.dicom.utils.DCMEcho;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.net.Status;

public class DicomURLTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	 private final static ImageIcon FAILED = DicomIcons.getIcon(DicomIcons.STATUS_FAILED);
	 private final static ImageIcon OK = DicomIcons.getIcon(DicomIcons.STATUS_OK);
	 private final static ImageIcon UNKNOWN = DicomIcons.getIcon(DicomIcons.STATUS_UNKNOWN);

	private String[] columns = {
			"Send To AET",
			"Send From AET",
			"Host",
			"Port",
			"Test"
	};
	

	
	private ArrayList urls = new ArrayList();
	private ArrayList<JButton> buttons = new ArrayList<JButton>();

	private boolean editable;
	
	public DicomURLTableModel(boolean editable) {
		this.editable = editable;
	}
	
	public int getRowCount() {
		return urls.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		DcmURL url = ((DcmURL) urls.get(rowIndex));
		switch (columnIndex) {
		case 0:
			return url.getCalledAET();
		case 1:
			String s = url.getCallingAET();
			if (s!= null )
				return  s;
			return "";
		case 2:
			return url.getHost();
		case 3:
			return url.getPort() + "";
		case 4:
			try {
				return buttons.get(rowIndex);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		default:
			return null;
		}
	}
	
	public JButton createTestButton(final int row) {
    	final JButton jb = new JButton(UNKNOWN);
    	jb.setToolTipText("Test connection");
    	jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DcmURL url = new DcmURL("dicom://" + 
							getValueAt(row, 0) + ":" +
							getValueAt(row, 1) + "@" +
							getValueAt(row, 2) + ":" +
							getValueAt(row, 3));
					if (DCMEcho.sendEcho(url) == Status.Success) {
						jb.setIcon(OK);
					} else 
						jb.setIcon(FAILED);					
				} catch (Exception ee) {}
			}
    	});
    	return jb;
    }
	
	public void setUrls(String s) {
		String[] ur = s.split("\n");
		this.urls = new ArrayList();
		this.buttons = new ArrayList();
		for (int i = 0; i < ur.length; i++) {
			urls.add(new DcmURL(ur[i]));
			buttons.add(createTestButton(i));
		}
		this.fireTableDataChanged();
	}
	
	public void setUrls(DcmURL[] ur) {
		this.urls = new ArrayList();
		this.buttons = new ArrayList();
		for (int i = 0; i < ur.length; i++) {
			urls.add(ur[i]);
			buttons.add(createTestButton(i));
		}
		this.fireTableDataChanged();
	}
	
	public DcmURL[] getUrls() {
		return (DcmURL[]) this.urls.toArray(new DcmURL[0]);
	}
	
	public void removeLine(int line) {
		this.urls.remove(line);
		this.buttons.remove(line);
		fireTableDataChanged();
	}
	
	public void addLine(DcmURL url) {
		this.urls.add(url);
		buttons.add(createTestButton(this.urls.size()));
		fireTableDataChanged();
	}
	
	
	public String getUrlsAsString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator iter = urls.iterator(); iter.hasNext();) {
			DcmURL element = (DcmURL) iter.next();
			sb.append(element + "\n");
		}
		return sb.toString();
	}

	public DcmURL getUrl(int line) {
		try {
			return (DcmURL) urls.get(line);	
		} catch (Exception e) {
			return null;
		}
		
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {	
		if (columnIndex == 4)
			return true;
		return editable;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		DcmURL url = ((DcmURL) urls.get(rowIndex));
		String called = url.getCalledAET();
		String calling = url.getCallingAET();
		String host = url.getHost();
		String port = url.getPort() + "";
		switch (columnIndex) {
		case 0:
			called = (String) aValue;
			break;
		case 1:
			calling = (String) aValue;
			break;
		case 2:
			host = (String) aValue;
			break;
		case 3:
			port = (String) aValue;
			break;
		default:
		}
		url = new DcmURL("dicom://" + called + ":" + calling + "@" + host + ":" + port);
		urls.set(rowIndex, url);
	}

	public void testURLs() {
	    try {
		for (JButton button : buttons) {
		    button.doClick();
		    fireTableDataChanged();
		}
	    } catch (Exception e) {}
	}
}

