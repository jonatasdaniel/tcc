package lu.tudor.santec.dicom.gui.header.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TagFilterPanel extends JPanel implements KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private JTextField tagVRField;
	private JTextField tagNrField;
	private JTextField tagNameField;
	private JTextField tagFilterRegexpField;
	private JTextField tagFilterTextField;
	private JTextField tagTestField;
	private JLabel tagTestLabel;

	private JRadioButton rbStartsWith;

	private JRadioButton rbContains;

	private JRadioButton rbEndsWith;

	// private JCheckBox checkInvert;

	private JButton testButton;

	private HeaderTag ht;

	private JCheckBox complexRegExp;

	private ButtonGroup bg;

	public TagFilterPanel() {

		this.setOpaque(false);

		CellConstraints cc = new CellConstraints();
		this.setLayout(new FormLayout("30px, 2dlu, 90px, 2dlu, 150dlu:grow", "pref, 3dlu, pref, center:10dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, center:10dlu, pref, 3dlu, pref:grow"));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.Tag.VR")), cc.xy(1, 1));
		this.tagVRField = new JTextField();
		this.tagVRField.setEditable(false);
		this.add(tagVRField, cc.xy(1, 3));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.Tag.NR")), cc.xy(3, 1));
		this.tagNrField = new JTextField();
		this.tagNrField.addKeyListener(this);
		this.add(tagNrField, cc.xy(3, 3));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.Tag.Name")), cc.xy(5, 1));
		this.tagNameField = new JTextField();
		this.tagNameField.setEditable(false);
		this.add(tagNameField, cc.xy(5, 3));

		this.add(new JSeparator(), cc.xyw(1, 4, 5));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.FilterTagPanel.Filter")), cc.xyw(1, 5, 3));
		this.tagFilterTextField = new JTextField();
		this.add(tagFilterTextField, cc.xy(5, 5));
		this.tagFilterTextField.addKeyListener(this);

		JPanel optionsPanel = new JPanel(new GridLayout(1, 0));
		optionsPanel.setOpaque(false);
		// this.checkInvert = new
		// JCheckBox(Translatrix.getTranslationString("dicom.FilterTagPanel.invert"));
		// optionsPanel.add(checkInvert);
		// checkInvert.addActionListener(this);
		optionsPanel.add(new JLabel());

		bg = new ButtonGroup();
		this.rbStartsWith = new JRadioButton(Translatrix.getTranslationString("dicom.FilterTagPanel.startsWith"));
		this.rbStartsWith.setOpaque(false);
		bg.add(rbStartsWith);
		optionsPanel.add(rbStartsWith);
		rbStartsWith.addActionListener(this);

		this.rbContains = new JRadioButton(Translatrix.getTranslationString("dicom.FilterTagPanel.contains"));
		this.rbContains.setOpaque(false);
		bg.add(rbContains);
		optionsPanel.add(rbContains);
		rbContains.addActionListener(this);

		this.rbEndsWith = new JRadioButton(Translatrix.getTranslationString("dicom.FilterTagPanel.endsWith"));
		this.rbEndsWith.setOpaque(false);
		bg.add(rbEndsWith);
		optionsPanel.add(rbEndsWith);
		rbEndsWith.addActionListener(this);

		this.add(optionsPanel, cc.xyw(1, 7, 5));

		this.complexRegExp = new JCheckBox(Translatrix.getTranslationString("dicom.FilterTagPanel.ComplexRegexp"));
		this.complexRegExp.setOpaque(false);
		this.complexRegExp.addActionListener(this);
		this.add(complexRegExp, cc.xyw(1, 9, 5));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.FilterTagPanel.Regexp")), cc.xyw(1, 11, 3));
		this.tagFilterRegexpField = new JTextField();
		this.tagFilterRegexpField.setEditable(false);
		this.add(tagFilterRegexpField, cc.xy(5, 11));

		this.add(new JSeparator(), cc.xyw(1, 12, 5));

		this.add(new JLabel(Translatrix.getTranslationString("dicom.FilterTagPanel.Test")), cc.xyw(1, 13, 3));

		JPanel testPanel = new JPanel(new BorderLayout());
		testPanel.setOpaque(false);

		this.tagTestField = new JTextField();
		this.add(tagTestField, cc.xyw(3, 13, 3));
		testPanel.add(tagTestField, BorderLayout.CENTER);

		this.testButton = new JButton("test");
		this.testButton.addActionListener(this);
		testPanel.add(testButton, BorderLayout.EAST);

		this.add(testPanel, cc.xy(5, 13));

		this.tagTestLabel = new JLabel();
		this.tagTestLabel.setBackground(Color.WHITE);
		this.tagTestLabel.setOpaque(true);
		this.tagTestLabel.setHorizontalAlignment(JLabel.CENTER);
		this.add(tagTestLabel, cc.xyw(1, 15, 5));

	}

	public void setHeaderTag(HeaderTag ht) {

		this.ht = ht;

		this.tagFilterTextField.setText("");
		this.tagFilterRegexpField.setText("");
		this.complexRegExp.setSelected(false);
		this.tagTestField.setText("");
		this.tagTestLabel.setText("");
		this.tagTestLabel.setOpaque(false);
		this.bg.clearSelection();

		if (ht == null) {
			this.tagVRField.setText("");
			this.tagNrField.setText("");
			this.tagNameField.setText("");

		} else {
			this.tagVRField.setText(ht.getTagVR());
			this.tagNrField.setText(ht.getTagNr());
			this.tagNameField.setText(ht.getTagName());
			if (ht.getTagValue() != null && !ht.getTagValue().equals("")) {
				this.tagFilterTextField.setText(ht.getTagValue());
				this.tagFilterRegexpField.setText(ht.getTagValue());
			}
		}

	}

	public void setRegExp(String regExp) {
		this.complexRegExp.setSelected(true);
		this.tagFilterRegexpField.setText(regExp);
		this.tagFilterTextField.setText(ht.getTagValue());
	}

	public HeaderTag getTag() {
		if (this.ht != null) {
			this.ht.tagValue = this.tagFilterRegexpField.getText();			
		} else {
			this.ht = new HeaderTag();
			this.ht.tagNr = this.tagNrField.getText();
			this.ht.tagValue = this.tagFilterRegexpField.getText();
		}
		return this.ht;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(this.tagNrField)) {
			tagVRField.setText("");
			tagNameField.setText("");
			this.ht = null;
		} else {
			buildRegexp();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.testButton)) {
			testRegExp();
		} else if (e.getSource().equals(this.complexRegExp)) {
			disablePanel();
		} else {
			buildRegexp();
		}

	}

	private void disablePanel() {
		boolean selected = this.complexRegExp.isSelected();
		// this.checkInvert.setEnabled(! selected);
		this.rbStartsWith.setEnabled(!selected);
		this.rbContains.setEnabled(!selected);
		this.rbEndsWith.setEnabled(!selected);
		this.tagFilterTextField.setEnabled(!selected);
		this.tagFilterRegexpField.setEditable(selected);
	}

	private boolean testRegExp() {
		try {
			Pattern p = Pattern.compile(tagFilterRegexpField.getText(), Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(tagTestField.getText());
			if (m.matches()) {
				this.tagTestLabel.setOpaque(true);
				this.tagTestLabel.setBackground(Color.GREEN);
				this.tagTestLabel.setText("Match");
				return true;
			} else {
				this.tagTestLabel.setOpaque(true);
				this.tagTestLabel.setBackground(Color.RED);
				this.tagTestLabel.setText("Failed");
				return false;
			}
		} catch (Exception e) {
			this.tagTestLabel.setOpaque(true);
			this.tagTestLabel.setBackground(Color.YELLOW);
			this.tagTestLabel.setText(e.getLocalizedMessage());
			return false;
		}
	}

	private void buildRegexp() {
		this.tagTestLabel.setBackground(Color.WHITE);
		this.tagTestLabel.setText("");

		String matchText = tagFilterTextField.getText();

		matchText = matchText.replaceAll("\\.", "\\\\.");

		StringBuffer sb = new StringBuffer();
		// if (checkInvert.isSelected()) {
		// matchText = "?!(" + matchText + ")";
		// }

		if (rbStartsWith.isSelected()) {
			sb.append(matchText);
			sb.append(".*");
		} else if (rbContains.isSelected()) {
			sb.append(".*");
			sb.append(matchText);
			sb.append(".*");
		} else if (rbEndsWith.isSelected()) {
			sb.append(".*");
			sb.append(matchText);
		} else {
			sb.append(matchText);
		}

		tagFilterRegexpField.setText(sb.toString());
	}

}
