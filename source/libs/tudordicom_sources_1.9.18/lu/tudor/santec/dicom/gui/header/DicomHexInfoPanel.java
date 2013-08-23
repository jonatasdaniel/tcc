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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.utils.DICOMHexDump;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHexInfoPanel extends JPanel implements ActionListener, CaretListener {
	
	private static final long serialVersionUID = 1L;

	protected static final String FILE_CHANGED = "FILE_CHANGED";

	private static final int BYTES = 16000;

	private static final int LINEBREAK = 20;
	
	JTextArea linesField;
	JTextArea hexField;
	JTextArea textField;
	
	Font font = new Font("Courier",Font.PLAIN, 12);
	
	private JTextField searchField;
	private JButton searchButton;

	private JScrollPane jsp;
	
	Highlighter.HighlightPainter infoHighlightPainter = new MyHighlightPainter(new Color(255,255,0));
	
	Highlighter.HighlightPainter diffHighlightPainter = new MyHighlightPainter(new Color(255,0,0,40));

	Highlighter.HighlightPainter selectionHighlightPainter = new MyHighlightPainter(new Color(184,207,229));
	
	
	private AdjustmentListener listener;

	protected File f;
   

	public DicomHexInfoPanel() {
		this.setLayout(new  BorderLayout());
		this.buildPanel();
	}
	
	private void buildPanel() {

	       JPanel textPanel = new JPanel(new BorderLayout(3,3));
	       textPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    
		linesField = new JTextArea(BYTES/LINEBREAK ,8) {
				private static final long serialVersionUID = 1L;
				public void replaceSelection(String content) {
					try {
						content = content.replaceAll("file:/", "");
						f = new File(content);
						setFile(f);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		linesField.setLineWrap(true);
		linesField.setFont(font);	
		textPanel.add(linesField, BorderLayout.WEST);
		
		hexField = new JTextArea(BYTES/LINEBREAK, LINEBREAK*3) {
			private static final long serialVersionUID = 1L;
			public void replaceSelection(String content) {
				try {
					content = content.replaceAll("file:/", "");
					f = new File(content);
					setFile(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		hexField.addCaretListener(this);
		hexField.setFont(font);	
		hexField.setLineWrap(true);
		textPanel.add(hexField, BorderLayout.CENTER);
		
            	textField = new JTextArea(BYTES/LINEBREAK, LINEBREAK) {
            		private static final long serialVersionUID = 1L;
            		public void replaceSelection(String content) {
            			try {
            				content = content.replaceAll("file:/", "");
            				f = new File(content);
            				setFile(f);
            			} catch (Exception e) {
            				e.printStackTrace();
            			}
            		}
            	};
            	textField.addCaretListener(this);
            	textField.setFont(font);	
            	textField.setLineWrap(true);
            	textPanel.add(textField, BorderLayout.EAST);
			
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(new JLabel("search:"), BorderLayout.WEST);
		this.searchField = new JTextField();
		jp.add(searchField, BorderLayout.CENTER);
		this.searchButton = new JButton(DicomIcons.getIcon16(DicomIcons.ICON_SEARCH));
		jp.add(searchButton, BorderLayout.EAST);
		this.searchButton.addActionListener(this);
		this.searchField.addActionListener(this);
		this.add(jp, BorderLayout.NORTH);
		
		
		jsp = new JScrollPane(textPanel);
		this.add(jsp, BorderLayout.CENTER);
		this.setSize(700,700);
	}

	
	public void setFile(File f) {
	    	this.f = f;
	    	String[] hexArr = DICOMHexDump.dump2HEX(f, BYTES, LINEBREAK);
		this.linesField.setText(hexArr[0]);
		this.linesField.setCaretPosition(0);
		this.hexField.setText(hexArr[1]);
		this.hexField.setCaretPosition(0);
		this.textField.setText(hexArr[2]);
		this.textField.setCaretPosition(0);
		firePropertyChange(FILE_CHANGED, null, f);
	}
	
	
	private void search() {
		highlight(searchField.getText());
		
	}

	public void actionPerformed(ActionEvent arg0) {
		search();
	}
	
    // Creates highlights around all occurrences of pattern in textComp
    public void highlight(String pattern) {
        // First remove all old highlights
        removeHighlights(linesField, infoHighlightPainter);
        removeHighlights(hexField, infoHighlightPainter);
        removeHighlights(textField, infoHighlightPainter);
        
        if (pattern == null || pattern.equals(""))
        	return;
        
        // highlight tag
        if( pattern.matches("[\\p{XDigit}]{4,4},[\\p{XDigit}]{4,4}")) {
//            pattern = pattern.toUpperCase();
            pattern = pattern.substring(2,4) + "[\\n\\s]"  + pattern.substring(0,2) + "[\\n\\s]" + 
            pattern.substring(7,9) + "[\\n\\s]"  + pattern.substring(5,7);
            System.out.println(pattern);
            
            try {
        	Highlighter hexHilite = hexField.getHighlighter();
        	Highlighter textHilite = textField.getHighlighter();
        	Document doc = hexField.getDocument();
        	String text = doc.getText(0, doc.getLength()).toLowerCase();
        	
        	Pattern p = Pattern.compile(pattern.toLowerCase(), Pattern.MULTILINE);
        	Matcher m = p.matcher(CharBuffer.wrap(text.toCharArray()));
        	while (m.find()) {
        	    hexHilite.addHighlight(m.start(), m.end(), infoHighlightPainter);
        	    int start = (m.start()/3);
        	    int end = (m.end()/3);
        	    textHilite.addHighlight(start+ (start/LINEBREAK), end + (end/LINEBREAK)+1, infoHighlightPainter);
        	}
            } catch (BadLocationException e) {
            }
        } else { // highlite Text
            try {
        	Highlighter hexHilite = hexField.getHighlighter();
        	Highlighter textHilite = textField.getHighlighter();
        	Document doc = textField.getDocument();
        	String text = doc.getText(0, doc.getLength()).toLowerCase();
        	
        	Pattern p = Pattern.compile(pattern.toLowerCase(), Pattern.MULTILINE);
        	Matcher m = p.matcher(CharBuffer.wrap(text.toCharArray()));
        	while (m.find()) {
        	    textHilite.addHighlight(m.start(), m.end(), infoHighlightPainter);
        	    int start = m.start();
        	    int line = (start/(LINEBREAK+1));
        	    start = start-line;
        	    int end = m.end()-line;
        	    hexHilite.addHighlight(start*3, end*3-1, infoHighlightPainter);
        	}
            } catch (BadLocationException e) {
            }
            
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
        removeHighlights(linesField, diffHighlightPainter);
    	for (Iterator<String> iter = diff.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			diff(this.linesField, element);
		}
    }
    
    public String getText() {
    	return this.linesField.getText();
    }
    
    public File getFile(){
    	return f;
    }

    public void caretUpdate(CaretEvent e) {
	removeHighlights(textField, selectionHighlightPainter);
	removeHighlights(hexField, selectionHighlightPainter);
	
	int pos1 = e.getDot();
	int pos2 = e.getMark();
	int start = Math.min(pos1, pos2);
	int end = Math.max(pos1, pos2);
	if (start == end)
	    return;
	
	if (e.getSource().equals(this.hexField)) {
	    start = (start/3);
	    end = (end/3);
	    Highlighter textHilite = textField.getHighlighter();
	    try {
		textHilite.addHighlight(start+ (start/LINEBREAK), end + (end/LINEBREAK)+1, selectionHighlightPainter);
	    } catch (BadLocationException e1) {}
	} else if (e.getSource().equals(this.textField)) {
	    removeHighlights(hexField, selectionHighlightPainter);
	    
	    int line = (start/(LINEBREAK+1));
	    start = start-line;
	    end = end-line;
	    Highlighter hexHilite = hexField.getHighlighter();
	    try {
		hexHilite.addHighlight(start*3, end*3-1, selectionHighlightPainter);
	    } catch (BadLocationException e1) {}
	}
    }
    
}
