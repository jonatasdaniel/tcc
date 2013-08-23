package lu.tudor.santec.dicom.gui.selector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import lu.tudor.santec.dicom.gui.DicomURLChooser;
import lu.tudor.santec.dicom.gui.ErrorDialog;
import lu.tudor.santec.dicom.gui.ImagePreviewDicom;
import lu.tudor.santec.dicom.gui.TableSorter;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.sender.DicomSender;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class MatcherPanel extends JPanel implements ActionListener, ListSelectionListener, MouseListener {

	private static final long serialVersionUID = 1L;
	private JTable tagTable;
	private JTable resultTable;
	private SelectorPanel parent;
	private ImagePreviewDicom dicomImagePreview;
	private ButtonBarBuilder bbuilder;
	private JButton sendButton;
	private JButton movelocalButton;
	private JButton cancelButton;
	private FoundFilesTableModel resultTableModel;
	private DicomTagTableModel headerTableModel;
	private DicomMatcher dicomMatcher;
	private TableSorter resultTableSorter;
	private TableSorter tagTableSorter;
	private DcmURL[] senders;
	private DicomSender dicomSender;
	private ProgressMonitor progressMonitor;
	private DcmURL localDest;
	private JButton openButton;
	private JPopupMenu popup;
	private AbstractAction actionAddtag;
	private AbstractAction actionDeleteTag;
	
	public MatcherPanel(SelectorPanel panel, String[] dicomFields) {
		this.parent = panel;
		
		this.setLayout(new BorderLayout());
		
		dicomMatcher = new DicomMatcher(this);
		
		headerTableModel = new DicomTagTableModel();
		
//		HeaderTag tag = new HeaderTag("0008,0068", null, "FOR PROCESSING");
//		headerTableModel.addTag(tag);
		
		this.tagTableSorter = new TableSorter(headerTableModel);
		this.tagTable = new JTable(tagTableSorter);
		this.tagTable.addMouseListener(this);
		this.tagTableSorter.setTableHeader(this.tagTable.getTableHeader());
		this.tagTable.setRowHeight(30);
		this.tagTable.getColumnModel().getColumn(0).setMaxWidth(120);
		this.tagTable.getColumnModel().getColumn(0).setCellEditor(new DicomTagCellEditor());
		this.tagTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				String tagName = DicomHeader.getHeaderName((String)value);
				String text = "<html>" + value + "<br><span style=\"font-size:7px\">" + tagName + "</span>";
				JLabel c = (JLabel) super.getTableCellRendererComponent(table, text, isSelected, hasFocus,row, column);
				c.setToolTipText(tagName);
				return c;
			}
		});
		this.tagTable.getColumnModel().getColumn(1).setMaxWidth(120);
		JScrollPane jsp1 = new JScrollPane(tagTable);
		jsp1.addMouseListener(this);
		jsp1.setPreferredSize(new Dimension(240,30));
		jsp1.setBorder(new TitledBorder(Translatrix.getTranslationString("dicom.FilterTags")));
		this.add(jsp1, BorderLayout.WEST);
		
		this.resultTableModel = new FoundFilesTableModel();
		this.resultTableSorter = new TableSorter(resultTableModel);
		this.resultTable = new JTable(resultTableSorter);
		this.resultTableSorter.setTableHeader(this.resultTable.getTableHeader());
		
		this.resultTable.setRowSelectionAllowed(true);
		this.resultTable.setColumnSelectionAllowed(false);
		this.resultTable.getSelectionModel().addListSelectionListener(this);
		
		this.resultTable.getColumnModel().getColumn(0).setMaxWidth(30);
		this.resultTable.getColumnModel().getColumn(2).setMaxWidth(100);
		this.resultTable.getColumnModel().getColumn(3).setMaxWidth(100);
		this.resultTable.getColumnModel().getColumn(4).setMaxWidth(50);
		this.resultTable.getColumnModel().getColumn(5).setMaxWidth(50);
		JScrollPane jsp2 = new JScrollPane(resultTable);
		jsp2.setBorder(new TitledBorder(Translatrix.getTranslationString("dicom.MatchingFiles")));
		this.add(jsp2, BorderLayout.CENTER);
		
		dicomImagePreview = new ImagePreviewDicom(this, parent.getParentDialog(), dicomFields);
		this.add(dicomImagePreview, BorderLayout.EAST);
		
		bbuilder = new ButtonBarBuilder();
		bbuilder.addGlue();
		
		sendButton = new JButton(Translatrix.getTranslationString("dicom.Send"));
		sendButton.setEnabled(false);
		sendButton.addActionListener(this);
		bbuilder.addGridded(sendButton);
		
		bbuilder.addRelatedGap();
		movelocalButton = new JButton(Translatrix.getTranslationString("dicom.MoveLocal"));
		movelocalButton.setEnabled(false);
		movelocalButton.addActionListener(this);
		bbuilder.addGridded(movelocalButton);
		
		bbuilder.addRelatedGap();
		openButton = new JButton(Translatrix.getTranslationString("dicom.Open"));
		openButton.setEnabled(false);
		openButton.addActionListener(this);
		bbuilder.addGridded(openButton);
		
		bbuilder.addRelatedGap();
		cancelButton = new JButton(Translatrix
				.getTranslationString("dicom.Cancel"));
		cancelButton.addActionListener(this);
		bbuilder.addGridded(cancelButton);
		
		JPanel bPanel = bbuilder.getPanel();
		bPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		
		this.add(bPanel, BorderLayout.SOUTH);
		
		this.actionAddtag = new AbstractAction("add Tag") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				headerTableModel.addTag(new HeaderTag());
			}
		};
		this.actionDeleteTag = new AbstractAction("delete Tag") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				try {
					int row = tagTableSorter.modelIndex(tagTable.getSelectedRow());
					headerTableModel.removeTag(row);					
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
		};
		
	}

	public void setPath(final File dir) {
		new Thread() {
			public void run() {
				dicomMatcher.setHeaderTags(headerTableModel.getTags());
				Vector dFiles = dicomMatcher.findMachingFiles(dir);
				resultTableModel.setFiles(dFiles);
				dicomImagePreview.loadImage(null);						
			}
		}.start();
	}

	public void setDicomSenders(DcmURL[] senders) {
		this.senders = senders;
	}
	
	public void setLocalDest(DcmURL localDest) {
		this.localDest = localDest;
	}

	public void setDicomFilterTags(Vector headerTags) {
		for (Iterator iter = headerTags.iterator(); iter.hasNext();) {
			HeaderTag tag = (HeaderTag) iter.next();
			headerTableModel.addTag(tag);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.cancelButton)) {
			this.cancel();
		} else if (e.getSource().equals(this.movelocalButton)) {
			send(true);
		} else if (e.getSource().equals(this.sendButton)) {
			send(false);
		} 	else if (e.getSource().equals(this.openButton)) {
			this.open();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == true)
			return;
		
		try {
			DicomFile img = resultTableModel.getFile(resultTableSorter.modelIndex(resultTable.getSelectedRow()));
			this.dicomImagePreview.loadImage(img.getFile());	
		    sendButton.setEnabled(true);
		   movelocalButton.setEnabled(true);
		   openButton.setEnabled(true);
		} catch (Exception ee) {
		    sendButton.setEnabled(false);
			movelocalButton.setEnabled(false);
			openButton.setEnabled(false);
		}
	}

	
	/**
	 * sends the selected Files from the DICOMDIR
	 */
	private void send(boolean local) {
		
		if (! local) {
			DcmURL url = DicomURLChooser.showDialog(parent.getParentDialog(), senders);
			if (url == null )
				return;
			dicomSender = new DicomSender(url);
		} else {
			dicomSender = new DicomSender(localDest);
		}
		if (dicomSender == null )
			return;
		
		
		final int[] selectedRows = resultTable.getSelectedRows();
		
		progressMonitor = new ProgressMonitor(parent.getParentDialog(), Translatrix.getTranslationString("dicom.SendingFilesTo") +" \r\n"
				+ dicomSender.getUrl(), "", 0, selectedRows.length);
		new Thread() {
			public void run() {
				parent.getParentDialog().setWaitCursor(true);
				try {
					for (int i = 0; i < selectedRows.length; i++) {
						File element = (File) resultTableModel.getFile(
								resultTableSorter.modelIndex(selectedRows[i])).getFile();
						if (progressMonitor.isCanceled()) {
							break;
						}
						progressMonitor.setNote("img " + element.getName()
								+ " ( " + (i + 1) + " of " + (selectedRows.length + 1)
								+ " )");
						progressMonitor.setProgress(i);
						dicomSender.send(element);

					}
				} catch (UnknownHostException e1) {
					ErrorDialog.showErrorDialog(parent.getParentDialog() , "Send Error", "Dicom Reciever at Host: " + e1
						.getLocalizedMessage() + " does not exist!", e1);
					parent.getParentDialog().setWaitCursor(false);
				} catch (Exception e1) {
					ErrorDialog.showErrorDialog(parent.getParentDialog() , "Send Error", e1.getClass().getName()+" "+ e1
							.getLocalizedMessage(), e1);
					parent.getParentDialog().setWaitCursor(false);
				}
				progressMonitor.close();
				parent.getParentDialog().setWaitCursor(false);
			}
		}.start();
	}

	/**
	 * opens the selected Files from the DICOMDIR
	 */
	private void open() {
		this.parent.getParentDialog().setWaitCursor(true);
		try {
			final int[] selectedRows = resultTable.getSelectedRows();
			File[] files = new File[selectedRows.length];
			for (int i = 0; i < selectedRows.length; i++) {
				File element = (File) resultTableModel.getFile(
						resultTableSorter.modelIndex(selectedRows[i])).getFile();
				files[i] = element;
			}
			try {
				parent.getParentDialog().setSelectedFile(files[0]);				
			} catch (Exception e) {}
			parent.getParentDialog().setSelectedFiles(files);
			parent.getParentDialog().setRetValue(JFileChooser.APPROVE_OPTION);
			parent.getParentDialog().setVisible(false);
		} catch (Exception ee) {
			parent.getParentDialog().setSelectedFiles(null);
			parent.getParentDialog().setSelectedFile(null);
			parent.getParentDialog().setRetValue(JFileChooser.CANCEL_OPTION);
			parent.getParentDialog().setVisible(false);
		}
		this.parent.getParentDialog().setWaitCursor(false);
	}
	
	/**
	 * closes the dialog
	 */
	private void cancel() {
		parent.getParentDialog().setRetValue(JFileChooser.CANCEL_OPTION);
		parent.getParentDialog().setVisible(false);
		this.parent.getParentDialog().setWaitCursor(false);
	}
	
	/**
	 * creates the popupmenue
	 * @param c
	 * @param x
	 * @param y
	 */
	private void showPopup(Component c, int x, int y) {
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(this.actionAddtag);
			popup.add(this.actionDeleteTag);
		}
		popup.show(c, x, y);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		// show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void mouseReleased(MouseEvent e) {
		//	show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
		}
	}

	public Vector getDicomFilterTags() {
		return headerTableModel.getTagsVector();
	}
	
}
