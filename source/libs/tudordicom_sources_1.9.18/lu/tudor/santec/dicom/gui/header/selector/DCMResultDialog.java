package lu.tudor.santec.dicom.gui.header.selector;

import ij.IJ;
import ij.ImageJ;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import org.apache.log4j.Logger;

import lu.tudor.santec.dicom.gui.TableSorter;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderInfoDialog;
import lu.tudor.santec.dicom.gui.utils.LineColorCellRenderer;
import lu.tudor.santec.dicom.gui.utils.TableUtils;
import lu.tudor.santec.dicom.receiver.DicomDirReader;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class DCMResultDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    /**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DCMResultDialog.class.getName());
    
    private DCMResultTableModel dcmResultTableModel;
    private JTable dcmResultTable;
    private JButton closeButton;
    private JButton csvButton;
    private JButton xlsButton;
    private JButton printButton;
    private static JFileChooser fileChooser = new JFileChooser();
    private DicomHeaderInfoDialog dhd;
    protected ImageJ imageJ;
	private JButton openImageButton;
	private JButton openImagesButton;
	private HeaderdataEvaluator evaluator;
	private TableSorter sorter;
	private JButton openFolderButton;
	private JButton copyFilesButton;
	private ProgressMonitor progressMonitor;
//    private DCMSummaryTableModel dcmSummaryTableModel;
//    private JTable dcmSummaryTable;

    public DCMResultDialog(HeaderdataEvaluator parent) {
	super(parent, Translatrix.getTranslationString("DCMResultDialog.title"));
	
	this.evaluator = parent;
	
	this.dhd = new DicomHeaderInfoDialog(this);
	
	this.setLayout(new BorderLayout(5,5));
	this.dcmResultTableModel = new DCMResultTableModel();
	
	this.sorter = new TableSorter(dcmResultTableModel);
	this.dcmResultTable = new JTable(sorter) {
	    private static final long serialVersionUID = 1L;
	//Implement table header tool tips.
	    protected JTableHeader createDefaultTableHeader() {
	        return new JTableHeader(columnModel) {
		    private static final long serialVersionUID = 1L;
		    public String getToolTipText(MouseEvent e) {
	                java.awt.Point p = e.getPoint();
	                int index = columnModel.getColumnIndexAtX(p.x);
	                int realIndex = 
	                        columnModel.getColumn(index).getModelIndex();
	                return dcmResultTableModel.getColumnTooltip(realIndex);
	            }
	        };
	    }
	};
	
	this.dcmResultTable.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_H) {
				int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
				if (row < 0) return;
			    showDicomHeader(dcmResultTableModel.getPathforRow(row)+ File.separator + dcmResultTableModel.getFilenameforRow(row));
			} else if (e.getKeyCode() == KeyEvent.VK_I) {
				int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
				if (row < 0) return;
				File[] f = new File[] {
						new File(dcmResultTableModel.getPathforRow(row) + File.separator + dcmResultTableModel.getFilenameforRow(row))						
				};
				showImage(f);				
			} else if (e.getKeyCode() == KeyEvent.VK_F) {
				int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
				if (row < 0) return;
					File[] f = new File(dcmResultTableModel.getPathforRow(row)).listFiles();						
				showImage(f);				
			}
	    }
	});
	
	sorter.setTableHeader(dcmResultTable.getTableHeader());
	this.dcmResultTable.setDefaultRenderer(String.class, new LineColorCellRenderer());
	this.dcmResultTable.setDefaultRenderer(Object.class, new LineColorCellRenderer());
	this.dcmResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	JScrollPane jsp = new JScrollPane(this.dcmResultTable);
	jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	this.add(jsp, BorderLayout.CENTER);
	
	
//	this.dcmSummaryTableModel = new DCMSummaryTableModel();
//	this.dcmSummaryTable = new JTable(dcmSummaryTableModel);
//	TableCellRenderer renderer = new LineColorCellRenderer() {
//	    @Override
//	    public Component getTableCellRendererComponent(JTable table,
//		    Object value, boolean isSelected, boolean hasFocus,
//		    int row, int column) {
//		JLabel label =  (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
//			row, column);
//		switch (row) {
//		case 0:
//		    label.setToolTipText("min");
//		    break;
//		case 1:
//		    label.setToolTipText("max");
//		    break;
//		case 2:
//		    label.setToolTipText("mean");
//		    break;
//		case 3:
//		    label.setToolTipText("stdev");
//		    break;
//		default:
//		    label.setToolTipText("");
//		    break;
//		}
//		return label;
//	    }
//
//	};
//	this.dcmSummaryTable.setDefaultRenderer(String.class, renderer);
//	this.dcmSummaryTable.setDefaultRenderer(Object.class, renderer);
//	this.dcmSummaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
//	JScrollPane jsp1 = new JScrollPane(this.dcmSummaryTable);
//	jsp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//	this.add(dcmSummaryTable, BorderLayout.SOUTH);
	
	ButtonBarBuilder bb = new ButtonBarBuilder();
	
	this.csvButton = new JButton(Translatrix.getTranslationString("dicom.export") + " .csv");
	this.csvButton.addActionListener(this);
	bb.addGridded(this.csvButton);
	bb.addRelatedGap();
	
//	this.xlsButton = new JButton(Translatrix.getTranslationString("dicom.export") + " .xls");
//	this.xlsButton.addActionListener(this);
//	this.xlsButton.setEnabled(false);
//	bb.addGridded(this.xlsButton);
//	bb.addRelatedGap();
	
//	this.printButton = new JButton(Translatrix.getTranslationString("dicom.print"));
//	this.printButton.addActionListener(this);
//	this.printButton.setEnabled(false);
//	bb.addGridded(this.printButton);
//	bb.addRelatedGap();
	
	bb.addRelatedGap();
		
		this.openImageButton = new JButton("View image");
		this.openImageButton.addActionListener(this);
		bb.addGridded(this.openImageButton);
		
		this.openImagesButton = new JButton("View images in folder");
		this.openImagesButton.addActionListener(this);
		bb.addGridded(this.openImagesButton);
		
		this.openFolderButton = new JButton("Open folder");
		this.openFolderButton.addActionListener(this);
		bb.addGridded(this.openFolderButton);
		
		this.copyFilesButton = new JButton("Copy Files");
		this.copyFilesButton.addActionListener(this);
		bb.addGridded(this.copyFilesButton);
		
	
	bb.addGlue();
	
	this.closeButton = new JButton(Translatrix.getTranslationString("dicom.close"));
	this.closeButton.addActionListener(this);
	bb.addGridded(this.closeButton);
	
	JPanel buttonPanel = bb.getPanel();
	this.add(buttonPanel, BorderLayout.SOUTH);
		
	this.setSize(1100,800);
    }
    
    
    public void showResult(String[] columns, String[] columnTooltips, Vector<String[]> data) {
	
	this.dcmResultTableModel.setColumns(columns);
	this.dcmResultTableModel.setColumnTooltips(columnTooltips);
	this.dcmResultTableModel.setData(data);
	
//	this.dcmSummaryTableModel.setColumns(columns);
//	this.dcmSummaryTableModel.setData(data);
	
	setTitle(Translatrix.getTranslationString("DCMResultDialog.title") 
		+ " " 
		+ data.size() 
		+ " " 
		+Translatrix.getTranslationString("DCMResultDialog.results") 
		);
		
	TableUtils.adjustColWidth(this.dcmResultTable);
//	TableUtils.adjustColWidth(this.dcmResultTable, this.dcmSummaryTable);
	
	this.setVisible(true);
	
    }
    
    public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.closeButton)) {
		    this.setVisible(false);
		} else if (e.getSource().equals(this.csvButton)) {
		    exportCSV();
		} else if (e.getSource().equals(this.copyFilesButton)) {
		    copyFiles();
		} else if (e.getSource().equals(this.xlsButton)) {
		    exportXLS();
		} else if (e.getSource().equals(this.printButton)) {
		    printTable();
		} else if (e.getSource().equals(this.openImageButton)) {
			int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
			if (row < 0) return;
			File[] f = new File[] {
					new File(dcmResultTableModel.getPathforRow(row) + File.separator + dcmResultTableModel.getFilenameforRow(row))						
			};
			showImage(f);				
		} else if (e.getSource().equals(this.openImagesButton)) {
			int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
			if (row < 0) return;
			File[] f = new File(dcmResultTableModel.getPathforRow(row)).listFiles();						
			showImage(f);				
		} else if (e.getSource().equals(this.openFolderButton)) {
			int row = sorter.modelIndex(dcmResultTable.getSelectedRow());
			if (row < 0) return;
			 File f = new File(dcmResultTableModel.getPathforRow(row));
			openFolder(f);				
		}
    }
    
    private void printTable() {
	try {
            MessageFormat headerFormat = new MessageFormat("Page {0}");
            MessageFormat footerFormat = new MessageFormat("- {0} -");
            this.dcmResultTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
          } catch (PrinterException pe) {
            System.err.println("Error printing: " + pe.getMessage());
          }
    }


    private void exportCSV() {
	File f = showExportDialog(this, "export.csv");
	    if (f != null) {
		try {
		    ExportToCSV cvsExporter = new ExportToCSV(f);
		    
		    String[] columns = this.dcmResultTableModel.getColumns();
		    String[] toolTips = this.dcmResultTableModel.getColumnTooltips();
		    for (int i = 0; i < columns.length; i++) {
			columns[i] += " ["+toolTips[i] + "]";
		    }
		    
		    cvsExporter.println(this.dcmResultTableModel.getColumns());
		    Vector<String[]> data = this.dcmResultTableModel.getData();
		    for (String[] strings : data) {
			cvsExporter.println(strings);
		    }
		    cvsExporter.close();
		    
		    JOptionPane.showMessageDialog(this,
				    "Exportet \n" + data.size() +" rows with " + this.dcmResultTableModel.getColumnCount() + "cols \nto:\n" +
				    		f.getAbsolutePath() + "\n Delimiter: ; Quote Char: \"");
		} catch (Exception e1) {
		    e1.printStackTrace();
		}
	    }
    }

    private void exportXLS() {
	File f = showExportDialog(this, "export.xls");
	    if (f != null) {
		try {
		    ExportToXSL xlsExporter = new ExportToXSL(f);
		    
		    String[] columns = this.dcmResultTableModel.getColumns();
		    String[] toolTips = this.dcmResultTableModel.getColumnTooltips();
		    for (int i = 0; i < columns.length; i++) {
			columns[i] += " ["+toolTips[i] + "]";
		    }
		    
		    xlsExporter.println(columns);
		    xlsExporter.writeStreamToBook();
		    
		    Vector<String[]> data = this.dcmResultTableModel.getData();
		    for (String[] strings : data) {
			xlsExporter.println(strings);
			xlsExporter.writeStreamToBook();
		    }
		    xlsExporter.close();
		    
		    JOptionPane.showMessageDialog(this,
				    "Exportet \n" + data.size() +" rows with " + this.dcmResultTableModel.getColumnCount() + "cols \nto:\n" +
				    		f.getAbsolutePath());
		} catch (Exception e1) {
		    e1.printStackTrace();
		}
	    }
    }

    public static File showExportDialog(Component parent, String fileName) {
	fileChooser.setMultiSelectionEnabled(false);
	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fileChooser.setSelectedFile(new File(fileName));
	
	if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
	    return fileChooser.getSelectedFile();
	}
	return null;
     }
    
    

    protected void showImage(File[] files) {
	
		    try {
		    	if (evaluator.dicomViewer != null) {
		    		evaluator.dicomViewer.setFiles(files);
		    	}
			} catch (Exception e) {
			    e.printStackTrace();
			}
    }

    public void showDicomHeader(final String path) {
		    try {
			DicomHeader dh = new DicomHeader(new File(path));
			dhd.setInfo(dh);
			dhd.setVisible(true);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
    }
    
    public void copyFiles() {
    	final int[] rows = this.dcmResultTable.getSelectedRows();
    	if (rows == null || rows.length == 0) {
    		JOptionPane.showMessageDialog(this,
    			    "Please select rows first!");
    	} else {
    		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    			new Thread() {
    				public void run() {
    					File folder =  fileChooser.getSelectedFile();
    					folder.mkdirs();
    					
    					progressMonitor = new ProgressMonitor(DCMResultDialog.this, "Copy Files", "", 0, rows.length);
    					progressMonitor.setMillisToDecideToPopup(5);
    					progressMonitor.setMillisToPopup(5);
    					
    					for (int i = 0; i < rows.length; i++) {
    						if (progressMonitor.isCanceled()) break;
    						
    						int row = sorter.modelIndex(rows[i]);
    						if (row < 0) continue;
    						
    						File oldFile = new File("");
    						File newFile = new File("");
    						try {
    							oldFile = new File(dcmResultTableModel.getPathforRow(row) + File.separator + dcmResultTableModel.getFilenameforRow(row));
    							progressMonitor.setNote("img " + oldFile.getName() + " ( " + (i + 1) + " of " + (rows.length + 1) + " )");
    							progressMonitor.setProgress(i);
    							newFile = new File(folder, DicomDirReader.createFileName(new DicomHeader(oldFile).getDicomObject()));
    							copyFile(oldFile, newFile);						
    						} catch (Exception e) {
    							logger.error("Error copying file: " +oldFile.getAbsolutePath() + " to " + newFile.getAbsolutePath(), e);
    							
    						}
    					}
    					progressMonitor.close();
    				}
    			}.start();
    		}
    		
    	}
    }
       
    
    public void openFolder(File folder) {
    	try {
		    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
		    	Desktop.getDesktop().open(folder);
		    } else {
				try {
				    Runtime rt = Runtime.getRuntime();
				    if (IJ.isWindows()) {
				    	String[] command = {
				    			"rundll32.exe",
				    			"url.dll,FileProtocolHandler",
				    			folder.getAbsolutePath()	    
				    	};
				    	rt.exec(command);			    	
				    } else if (IJ.isLinux()){
				    	String[] command = {
				    			"kfmclient",
				    			"openURL",
				    			folder.getAbsolutePath()	    
				    	};
				    	rt.exec(command);
				    }
				    
				} catch (Exception e1) {
				    e1.printStackTrace();
				}
			}
		} catch (Throwable t) {
		    t.printStackTrace();
			try {
			    Runtime rt = Runtime.getRuntime();
			    if (IJ.isWindows()) {
			    	String[] command = {
			    			"rundll32.exe",
			    			"url.dll,FileProtocolHandler",
			    			folder.getAbsolutePath()	    
			    	};
			    	rt.exec(command);			    	
			    } else if (IJ.isLinux()){
			    	String[] command = {
			    			"kfmclient",
			    			"openURL",
			    			folder.getAbsolutePath()	    
			    	};
			    	rt.exec(command);
			    }
			    
			} catch (Exception e1) {
			    e1.printStackTrace();
			}
		}
    }
    
    public class DCMResultTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] columns = new String[0];
	private Vector<String[]> data = new Vector<String[]>();
	private String[] columnTooltips = new String[0];
	
	public void setColumns(String[] columns) {
	    this.columns = columns;
	    this.fireTableStructureChanged();
	}

	public String getPathforRow(int selectedRow) {
	    return (String) getValueAt(selectedRow, getColumnCount()-2);
	}
	
	public String getFilenameforRow(int selectedRow) {
	    return (String) getValueAt(selectedRow, getColumnCount()-1);
	}

	public void setColumnTooltips(String[] columnTooltips) {
	    this.columnTooltips = columnTooltips;
	}

	public void setData(Vector<String[]> data) {
	    this.data = data;
	    this.fireTableDataChanged();
	}

	public int getColumnCount() {
	    return columns.length;
	}
	
	public String getColumnName(int columnIndex) {
	    return columns[columnIndex];
	}
	
	public String getColumnTooltip(int columnIndex) {
	    return columnTooltips[columnIndex];
	}

	public int getRowCount() {
	    return data.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
	    return data.get(rowIndex)[columnIndex];
	}
	
	public String[] getColumns() {
	    return columns;
	}
	
	public String[] getColumnTooltips() {
	    return columnTooltips;
	}
	
	public Vector<String[]> getData() {
	    return data;
	}

    }
    
    public class DCMSummaryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] columns = new String[0];
	private Vector<Double[]> data = new Vector<Double[]>();
	private NumberFormat nf = new DecimalFormat("#0.000");
	
	public void setColumns(String[] columns) {
	    this.columns = columns;
	    this.fireTableStructureChanged();
	}

	public void setData(Vector<String[]> data) {
	    this.data.clear();
	    
	    try {
		int columns = data.get(0).length;
		
		Double[] min = new Double[columns];
		Double[] max = new Double[columns];
		Double[] mean = new Double[columns];
		Double[] stdev = new Double[columns];
		
		for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
		    String[] strings = (String[]) iterator.next();
		    
		    for (int i = 0; i < strings.length; i++) {
			try {
			    double val = Double.parseDouble(strings[i]);
			    if (min[i] == null || min[i] > val) min[i] = val;
			    if (max[i] == null || max[i] < val) max[i] = val;
			    if (mean[i] == null) mean[i] = 0.0;
			    mean[i] +=val;
			} catch (Exception e) {
			}
		    }
		}
		
		this.data.add(min);
		this.data.add(max);
		    for (int i = 0; i < mean.length; i++) {
			try {
        			mean[i] = mean[i]/data.size();
        			double sum = 0;
        			for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
        			    String[] strings = (String[]) iterator.next();
        			    double val = Double.parseDouble(strings[i]);
        			    sum += Math.pow(val - mean[i],2); 
        			}
        			stdev[i] =(Math.sqrt(sum) / data.size());
			} catch (Exception e) {	
			    e.printStackTrace();
			}
		    }		    
		    this.data.add(mean);
		    this.data.add(stdev);			
		
		
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    
	    this.fireTableDataChanged();
	}

	public int getColumnCount() {
	    return columns.length;
	}
	
	public String getColumnName(int columnIndex) {
	    return columns[columnIndex];
	}

	public int getRowCount() {
	    return data.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
	    try {
		return nf.format(data.get(rowIndex)[columnIndex]);		
	    } catch (Exception e) {
		return "";
	    }
	}
	
	public String[] getColumns() {
	    return columns;
	}
	
	public Vector<Double[]> getData() {
	    return data;
	}

    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
    	destFile.getParentFile().mkdirs();
    	
//        if(!destFile.exists()) {
//            destFile.createNewFile();
//        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }


    public static void main(String[] args)  {
//	
//	String[] columns = {
//		"Bla", "Blub","Trallalla"
//	};
//	
//	Vector<String[]> data = new Vector<String[]>();
//	
//	String[] arr1 = {"1","2","3"};
//	String[] arr2 = {"3","4","5"};
//	String[] arr3 = {"6","7","8"};
//	
//	data.add( arr1);
//	data.add( arr2);
//	data.add( arr3);
//	
//	new DCMResultDialog(new JDialog()).showResult(columns, data);
	
    }

}
