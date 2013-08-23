package lu.tudor.santec.dicom.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

/**
 * A General Error Dialog to show Errors and Exceptions.
 * The details/exeptions can be shown or hided by clicking the details button
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class ErrorDialog extends  JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel messageLabel;
	private JTextArea detailsTextArea;
	private JScrollPane details;
	private JPanel buttonPanel;
	private JPanel bottomPanel;
	private JButton okButton;
	private JToggleButton detailsButton;
	private JComponent parent;
	private JPanel content;
	
	private static String DEFAULT_TITLE = "DICOM Error";
	private static Color BACKGROUND = Color.WHITE;
	private static ImageIcon ICON = DicomIcons.getIcon32(DicomIcons.ERROR);

	public ErrorDialog(JFrame parent, String title) {
		super(parent, title, true);
		init();
	}
		
	public ErrorDialog(JDialog parent, String title) {
		super(parent, title, true);
		init();
	}
	
	public void init() {
		this.getContentPane().setBackground(BACKGROUND);
		
		content = new JPanel(new BorderLayout());
		content.setBackground(BACKGROUND);
		content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		this.messageLabel = new JLabel(ICON);
		this.messageLabel.setHorizontalAlignment(JLabel.LEFT);
		this.messageLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.messageLabel.setBackground(BACKGROUND);
		this.messageLabel.setMinimumSize(new Dimension(300, 60));
		this.getContentPane().add(messageLabel, BorderLayout.NORTH);
		
		this.detailsTextArea = new JTextArea(60, 10);
		this.detailsTextArea.setEditable(false);
		this.detailsTextArea.setBackground(BACKGROUND);
		this.details = new JScrollPane(detailsTextArea);
		this.details.setPreferredSize(new Dimension(600, 200));
		
		this.bottomPanel = new JPanel(new BorderLayout());
		this.bottomPanel.setBackground(BACKGROUND);
		this.buttonPanel = new JPanel(new GridLayout(1,0));
		this.buttonPanel.setBackground(BACKGROUND);
		
		this.detailsButton = new JToggleButton("Details");
		this.detailsButton.addActionListener(this);
		this.buttonPanel.add(detailsButton);
		this.okButton = new JButton("OK");
		this.okButton.addActionListener(this);
		this.buttonPanel.add(okButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		content.add(bottomPanel, BorderLayout.SOUTH);
		
		this.getContentPane().add(content);
	}
	
	
	public static void showErrorDialog(JFrame parent, Throwable details) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bout);
		details.printStackTrace(pw);
		pw.flush();
		showErrorDialog(parent, DEFAULT_TITLE, details.getLocalizedMessage(), bout.toString());
	}
	
	public static void showErrorDialog(JFrame parent, String title, String message, Throwable details) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bout);
		details.printStackTrace(pw);
		pw.flush();
		showErrorDialog(parent, title, message, bout.toString());
	}
	
	public static void showErrorDialog(JFrame parent, String title, String message, String details) {
		
		ErrorDialog jd = new ErrorDialog(parent, title);
		try {
			jd.parent = (JComponent) parent.getComponent(0);			
		} catch (Exception e) {
		}
		jd.messageLabel.setText("<html><h3>" + message.replaceAll("\n", "<br>"));
		jd.detailsTextArea.setText(details);
		jd.validate();
		center(parent, jd);
		jd.setVisible(true);
	}
	
	
	
	public static void showErrorDialog(JDialog parent, Throwable details) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bout);
		details.printStackTrace(pw);
		pw.flush();
		showErrorDialog(parent, DEFAULT_TITLE, details.getLocalizedMessage(), bout.toString());
	}
	
	public static void showErrorDialog(JDialog parent, String title, String message, Throwable details) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bout);
		details.printStackTrace(pw);
		pw.flush();
		showErrorDialog(parent, title, message, bout.toString());
	}
	
	public static void showErrorDialog(JDialog parent, String title, String message, String details) {
		
		ErrorDialog jd = new ErrorDialog(parent, title);
		try {
			jd.parent = (JComponent) parent.getComponent(0);			
		} catch (Exception e) {
		}
		try {
			jd.messageLabel.setText("<html><h3>" + message.replaceAll("\n", "<br>"));			
		} catch (Exception e) {
		}
		jd.detailsTextArea.setText(details);
		jd.validate();
		center(parent, jd);
		jd.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(this.detailsButton)) {
			if (this.detailsButton.isSelected()) {
				content.add(details, BorderLayout.CENTER);
			} else {
				content.remove(details);
			}
			this.content.updateUI();
			this.content.validate();
			this.validate();
			center(parent, this);
			this.validate();
		} else if (event.getSource().equals(this.okButton)) {
			this.setVisible(false);
		}
	}
	
	private static void center(Component parent, JDialog dialog) {
		dialog.validate();
		dialog.pack();
		if (parent.isShowing())
		    dialog.setLocationRelativeTo(parent);
		else {
		    Dimension f = dialog.getSize();
		    Point d = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint(); 
		    dialog.setLocation(d.x - (f.width/ 2), d.y - (f.height / 2));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame p = new JFrame("Parent");
		p.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p.setSize(800,600);
		p.setVisible(true);
		
		try {
			new FileInputStream("bla");
		} catch (Throwable e) {
			e.printStackTrace();
			ErrorDialog.showErrorDialog(p, "error", "an error occured", e);
			ErrorDialog.showErrorDialog(p, e);
		}
	}
	
	public static void setBackgroundColor(Color c) {
		BACKGROUND = c;
	}
	
	public static void setIcon(ImageIcon i) {
		ICON = i;
	}

	public static void setDefaultTitle(String title) {
		DEFAULT_TITLE = title;
	}
	
}
