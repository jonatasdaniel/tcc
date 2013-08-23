package lu.tudor.santec.dicom.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.dcm4che.util.DcmURL;

public class DicomURLChooser extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel bottomPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTable urlList;
	private DcmURL url;
	protected DicomURLTableModel urlTableModel;

	public DicomURLChooser(JDialog parent, DcmURL[] urls) {
		super(parent, "Select Dicom Node", true);
		BorderLayout bl = new BorderLayout();
		this.getContentPane().setLayout(bl);
		this.bottomPanel = new JPanel(new BorderLayout());
		this.buttonPanel = new JPanel(new GridLayout(1,0));
		
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		this.buttonPanel.add(cancelButton);
		this.okButton = new JButton("OK");
		this.okButton.addActionListener(this);
		this.buttonPanel.add(okButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		JLabel jl = new JLabel("<html><h3>Send to Destination:");
		jl.setOpaque(false);
		
		this.getContentPane().add(jl, BorderLayout.NORTH);
		
		this.urlTableModel = new DicomURLTableModel(false);
		this.urlTableModel.setUrls(urls);
		this.urlList = new JTable(urlTableModel);
		
		this.urlList.getColumnModel().getColumn(3).setMaxWidth(50);
		this.urlList.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
		this.urlList.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor());
		this.urlList.getColumnModel().getColumn(4).setMaxWidth(30);
		
		this.urlList.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					url = (DcmURL) urlTableModel.getUrl(urlList.getSelectedRow());
					setVisible(false);
				}
			}
		});
		this.getContentPane().add(new JScrollPane(urlList), BorderLayout.CENTER);

		this.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) {
					urlList.setRowSelectionInterval(0,0);
//			    	try {
//						Thread.sleep(1500);
//					} catch (InterruptedException ee) {}
			    	urlTableModel.testURLs();
			    	urlList.setRowSelectionInterval(0,0);
			}
			
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
//			public void windowStateChanged(WindowEvent e) {
//				if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
//					urlList.setRowSelectionInterval(0,0);
////			    	try {
////						Thread.sleep(1500);
////					} catch (InterruptedException ee) {}
//			    	urlTableModel.testURLs();
//			    	urlList.setRowSelectionInterval(0,0);
//				}
//			}
//		});
		
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(this.cancelButton)) {
			url = null;
		} else if (arg0.getSource().equals(this.okButton)) {
			url = (DcmURL) urlTableModel.getUrl(urlList.getSelectedRow());
		}
		this.setVisible(false);
		
	}

	public static DcmURL showDialog(JDialog parent, DcmURL[] urls) {
		
		final DicomURLChooser dialog = new DicomURLChooser(parent, urls);
		dialog.setSize(480,320);
		
//		Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
//		dialog.setLocation(SCREEN_SIZE.width/2 - dialog.getWidth()/2, 
//		                SCREEN_SIZE.height/2 - dialog.getHeight()/2);


		dialog.setLocationRelativeTo(parent);
		
//		new Thread() {
//		    public void run() {
//		    	dialog.urlList.setRowSelectionInterval(0,0);
//		    	try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {}
//		    	dialog.urlTableModel.testURLs();
//		    	dialog.urlList.setRowSelectionInterval(0,0);
//		    }
//		}.start();
//		
		dialog.setVisible(true);
		return dialog.url;		
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JDialog jf = new JDialog();
		DcmURL[] urls = {
				new DcmURL("dicom://receiver:sender@localhost:8080"),
				new DcmURL("dicom://receiver:sender@localhost:8080"),
				new DcmURL("dicom://receiver:sender@localhost:8080"),
				new DcmURL("dicom://receiver:sender@localhost:8080")				
		};
		
		System.out.println(DicomURLChooser.showDialog(jf, urls));
	}

	
}
