package lu.tudor.santec.dicom.gui.header;

/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2006 by SANTEC/TUDOR www.santec.tudor.lu                   
 *                                                                           
 *                                                                           
 *  This library is free software; you can redistribute it and/or modify it  
 *  under the terms of the GNU Lesser General Public License as published    
 *  by the Free Software Foundation; either version 2 of the License, or     
 *  (at your option) any later version.                                      
 *                                                                           
 *  This software is distributed in the hope that it will be useful, but     
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 *  Lesser General Public License for more details.                          
 *                                                                           
 *  You should have received a copy of the GNU Lesser General Public         
 *  License along with this library; if not, write to the Free Software      
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
 *                                                                           
 *****************************************************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;

import lu.tudor.santec.dicom.gui.DicomIcons;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHeaderInfoPanel extends JPanel implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;

	public static final String FILE_CHANGED = "FILE_CHANGED";
	
	JTextArea infoField;
	
	private JTextField searchField;
	private JButton searchButton;

	private JScrollPane jsp;
	
	Highlighter.HighlightPainter infoHighlightPainter = new MyHighlightPainter(new Color(255,255,0));
	
	Highlighter.HighlightPainter diffHighlightPainter = new MyHighlightPainter(new Color(255,0,0,40));

	private AdjustmentListener listener;

	protected File f;

	private DicomHeaderInfoDialog dicomHeaderInfoDialog;

	private int pos;
   

	public DicomHeaderInfoPanel(DicomHeaderInfoDialog dicomHeaderInfoDialog) {
	    	this.dicomHeaderInfoDialog = dicomHeaderInfoDialog;
		this.setLayout(new  BorderLayout());
		this.buildPanel();
		if (dicomHeaderInfoDialog != null)
		    this.dicomHeaderInfoDialog.addWindowListener(this);
	}
	
	private void buildPanel() {

			infoField = new JTextArea() {
				private static final long serialVersionUID = 1L;
				public void replaceSelection(String content) {
					try {
						content = content.replaceAll("file:/", "");
						f = new File(content);
						DicomHeader dh = new DicomHeader(f);
						setInfo(dh);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			if (dicomHeaderInfoDialog != null) {
        			this.infoField.addKeyListener(new KeyAdapter() {
        			    @Override
        			    public void keyTyped(KeyEvent e) {
        				if (e.getKeyChar() == 'h' || e.getKeyChar() == 'H') {
        				    dicomHeaderInfoDialog.setVisible(false);
        				} 
        			    }
        			    
        			    @Override
        			    public void keyReleased(KeyEvent e) {
        				if (e.getKeyCode() == KeyEvent.VK_F && e.isControlDown()) {
        				    searchField.requestFocus();
        				}
        			    }
        			});
			}
			this.infoField.setEditable(false);
		
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(new JLabel("search:"), BorderLayout.WEST);
		this.searchField = new JTextField();
		jp.add(searchField, BorderLayout.CENTER);
		this.searchButton = new JButton(DicomIcons.getIcon16(DicomIcons.ICON_SEARCH));
		jp.add(searchButton, BorderLayout.EAST);
		this.searchButton.addActionListener(this);
		this.searchField.addActionListener(this);
		this.add(jp, BorderLayout.NORTH);
		
		jsp = new JScrollPane(infoField);
		
		
		this.add(jsp, BorderLayout.CENTER);
		this.setSize(600,700);
	}

	public void setInfo(DicomHeader dh, boolean retainValues) {
	    	int scrollValue = this.jsp.getVerticalScrollBar().getValue();
	    	this.infoField.setText(dh.toString());
		this.infoField.setCaretPosition(0);
		firePropertyChange(FILE_CHANGED, null, f);
		if (retainValues) {
		    search();
		    this.jsp.getVerticalScrollBar().setValue(scrollValue);
		}
	}
	
	public void setInfo(DicomHeader dh) {	
	    setInfo(dh, false);
	}
	
	
	public void search() {
		highlight(this.infoField, searchField.getText());
	}

	public void actionPerformed(ActionEvent arg0) {
		search();
	}
	
    // Creates highlights around all occurrences of pattern in textComp
    public void highlight(JTextComponent textComp, String pattern) {
        // First remove all old highlights
        removeHighlights(textComp, infoHighlightPainter);
    
        if (pattern == null || pattern.equals(""))
        	return;
        
        try {
            Highlighter hilite = textComp.getHighlighter();
            Document doc = textComp.getDocument();
            String text = doc.getText(0, doc.getLength()).toLowerCase();
            
            Pattern p = Pattern.compile(pattern.toLowerCase(), Pattern.MULTILINE);
            Matcher m = p.matcher(CharBuffer.wrap(text.toCharArray()));
            boolean found = false;
            while (m.find()) {
                hilite.addHighlight(m.start(), m.end(), infoHighlightPainter);
                if (! found ) {
                    pos = m.start();
                    found = true;
                }
            }
            textComp.setCaretPosition(pos);
            textComp.requestFocus();
        } catch (Exception e) {
        }
    }
    
    // Creates highlights around all occurrences of pattern in textComp
    public void diff(JTextComponent textComp, String pattern) {   
        try {
            Highlighter hilite = textComp.getHighlighter();
            Document doc = textComp.getDocument();
            String text = doc.getText(0, doc.getLength());
            
            if (pattern.startsWith("---------"))
            	return;
            
            int fromIndex = 0;
            int start = text.indexOf(pattern);
            while (start != -1) {
            	fromIndex = start+pattern.length();
            	hilite.addHighlight(start, fromIndex, diffHighlightPainter);
            	start = text.indexOf(pattern, fromIndex);
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    // Removes only our private highlights
    public void removeHighlights(JTextComponent textComp, HighlightPainter highlighter) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
    
        for (int i=0; i<hilites.length; i++) {
            if (hilites[i].getPainter().equals(highlighter)) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }
    
    // A private subclass of the default highlight painter
    class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }
    
    public void addAdjustmentListener(AdjustmentListener listener) {
    	this.listener = listener;
		jsp.getVerticalScrollBar().addAdjustmentListener(listener);
    }
    
    public void setAdjustment(int value) {
    		jsp.getVerticalScrollBar().removeAdjustmentListener(listener);
		jsp.getVerticalScrollBar().setValue(value);
		jsp.getVerticalScrollBar().addAdjustmentListener(listener);
    }
    
    public void showDiff(Vector<String> diff) {
        // First remove all old highlights
        removeHighlights(infoField, diffHighlightPainter);
    	for (Iterator<String> iter = diff.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			diff(this.infoField, element);
		}
    }
    
    public String getText() {
    	return this.infoField.getText();
    }
    
    public File getFile(){
    	return f;
    }

    public void windowActivated(WindowEvent e) {
    }
    public void windowClosed(WindowEvent e) {
    }
    public void windowClosing(WindowEvent e) {
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
	this.infoField.requestFocus();
    }
    
}
