package lu.tudor.santec.dicom.gui.query;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterField extends JPanel implements ItemListener, ActionListener, PropertyChangeListener {

	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(FilterField.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public static final LinkedHashSet<String> OPERATORS = new LinkedHashSet<String>(); 
	static {
		OPERATORS.add("=");
		OPERATORS.add("<");
		OPERATORS.add(">");
	}

	public static final String DEFAULT = "DEFAULT";
	public static final String DELIM = "#";

	private HashMap<String, Class<?>> columns;
	private HashMap<String, Set<String>> operatorHash = new HashMap<String, Set<String>>();
	private JComboBox columnChooser;
	private JComboBox operator1;
	private JTextField value1;
	private JComboBox operator2;
	private JTextField value2;
	private Class<?> oldClazz = null;
	private boolean listenersActive = true;

	private JLabel groupOperator;

	private JButton addButton;

	private JButton deleteButton;

	private FilterPanel panel;

	private static FormLayout layout;
	private static CellConstraints cc = new CellConstraints();

	public FilterField(FilterPanel panel, boolean isFirst) {
		this.panel = panel;
		if (layout == null) {
			layout = new FormLayout(
					"25dlu, 2dlu, pref, 2dlu, pref, 2dlu, fill:pref:grow, 2dlu, pref, 2dlu, pref, 2dlu, fill:pref:grow, 2dlu, 10dlu, 2dlu", 
					"fill:pref");			
		}
		
		this.setLayout(layout);
		

		try {
			if (! isFirst) {
//				this.groupOperator = new JComboBox(new String[] {"AND" } );
				this.groupOperator = new JLabel("AND");
				this.add(this.groupOperator, cc.xy(1,1));
			} else {
				this.add(new JLabel("WHERE"), cc.xy(1,1));
			}
			this.columnChooser = new JComboBox();
			this.columnChooser.addItemListener(this);
			this.add(columnChooser, cc.xy(3,1));
			this.operator1 = new JComboBox();
			this.operator1.addActionListener(this);
			this.add(operator1, cc.xy(5,1));
			this.value1 = new JTextField();
			this.value1.addPropertyChangeListener(this);

			this.add(value1, cc.xy(7,1));
			
			
			this.add(new JLabel("AND"), cc.xy(9,1));
			this.operator2 = new JComboBox();
			this.operator2.addActionListener(this);
			this.add(operator2, cc.xy(11,1));
			this.value2 = new JTextField();
			this.value2.addPropertyChangeListener(this);
			
			this.add(value2, cc.xy(13,1));
			if (isFirst) {
				this.addButton = new JButton(DicomIcons.getIcon16(DicomIcons.FILTER_ADD));
				this.addButton.addActionListener(this);
				this.add(addButton, cc.xy(15,1));
			} else {
				this.deleteButton = new JButton(DicomIcons.getIcon16(DicomIcons.FILTER_REMOVE));
				this.deleteButton.addActionListener(this);
				this.add(deleteButton, cc.xy(15,1));
			}
		} catch (Exception e) {
		}
	}

	public void setKeys(LinkedHashMap<String, Class<?>> columns) {
		this.columns = columns;
		this.columnChooser.removeAllItems();
		for (String col : columns.keySet()) {			
			this.columnChooser.addItem(col);
		}
		
	}

	public void setOperators(LinkedHashSet<String> operators) {
		this.operatorHash.put(DEFAULT, operators);
		this.operator1.removeAllItems();
		this.operator2.removeAllItems();
		for (String op : operators) {			
			this.operator1.addItem(op);
			this.operator2.addItem(op);
		}
	}
	
	public void setOperators(Class<?> clazz, LinkedHashSet<String> operators) {
		this.operatorHash.put(clazz.getName(), operators);
	}
	
	public void setOperators(String clazzName, LinkedHashSet<String> operators) {
		this.operatorHash.put(clazzName, operators);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(this.columnChooser)) {
			listenersActive = false;
			String column = (String) columnChooser.getSelectedItem();
			Class<?> clazz = this.columns.get(column);
			if (oldClazz != clazz) {
				try {
					oldClazz = clazz;
					this.operator1.removeAllItems();
					this.operator2.removeAllItems();
					Set<String> operators = operatorHash.get(clazz.getName());
					if (operators == null|| operators.size() == 0) {
						operators = operatorHash.get(DEFAULT);
					}
					
					for (String op : operators) {			
						this.operator1.addItem(op);
						this.operator2.addItem(op);
					}
					
					
					this.remove(this.value1);
					this.remove(this.value2);

					this.value1 = new JTextField();
					this.value1.addPropertyChangeListener(this);
					this.add(value1, cc.xy(7, 1));
					this.value2 = new JTextField();	
					this.value2.addPropertyChangeListener(this);
					this.add(value2, cc.xy(13, 1));
					
					this.validate();
					this.repaint();
				} catch (Exception e2) {
					System.out.println(e2);
					logger.error("Error creating fields for " + column + " type " + clazz);
				}
			}
			listenersActive = true;
		}
	}
	
	public String getFilter() {
		StringBuffer sb = new StringBuffer();
		if (groupOperator != null) 
			sb.append(groupOperator.getText());
		else
			sb.append("WHERE");
		sb.append(DELIM);	
		sb.append(columnChooser.getSelectedItem());
		sb.append(DELIM);	
		sb.append(operator1.getSelectedItem());
		sb.append(DELIM);	
		try {
			sb.append(value1.getText());			
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append(DELIM);	
		sb.append(operator2.getSelectedItem());
		sb.append(DELIM);	
		try {
			sb.append(value2.getText());			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	public void setFilter(String filter) {
		String[] strings = filter.split(DELIM);
		if (strings.length == 6) {
			if (groupOperator != null) {
				groupOperator.setText(strings[0]);
			}
			columnChooser.setSelectedItem(strings[1]);
			operator1.setSelectedItem(strings[2]);
			try {
				value1.setText(strings[3]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			operator2.setSelectedItem(strings[4]);
			try {
				value2.setText(strings[5]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String printFilter() {
		return printFilter(getFilter());
	}
	
	public static String printFilter(String filter) {
		StringBuffer sb = new StringBuffer();
		String[] strings = filter.split(DELIM);
		if (strings.length == 6) {
			if (strings[0] != null) 
				sb.append(strings[0]);
			else
				sb.append("WHERE");
			sb.append(" (");
			if (strings[3] != null && strings[3].length() > 0 && !"null".equals(strings[3])) {
				sb.append(strings[1]).append(strings[2]).append(strings[3]);
			}
			if (strings[5] != null && strings[5].length() > 0 && !"null".equals(strings[5])) {
				sb.append(" AND ");
				sb.append(strings[1]).append(strings[4]).append(strings[5]);				
			}
			sb.append(")");
		}
		
		return sb.toString();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.addButton) && this.panel != null) {
			panel.addItem(this);
		} else if (e.getSource().equals(this.deleteButton) && this.panel != null) {
			panel.removeItem(this);
		} else if (e.getSource().equals(this.operator1) || e.getSource().equals(this.operator2) ) {
			if (listenersActive) {
				firePropertyChange("VALUE", 0, 1);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			Translatrix.addBundle("lu.tudor.santec.iveu.gui.resources.Translatrix");
			Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
			Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
			Translatrix.addBundle(SwingLocalizer.getBundle());
			Translatrix.setDefaultWhenMissing(true);
			SwingLocalizer.localizeJFileChooser();
			SwingLocalizer.localizeJOptionPane();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		try {
			LinkedHashMap<String, Class<?>> columns = new LinkedHashMap<String, Class<?>>();
			columns.put("String", String.class);
			columns.put("Integer", Integer.class);
			columns.put("Double", Double.class);
			columns.put("Date", Date.class);
			
			LinkedHashSet<String> operators = new LinkedHashSet<String>();
			operators.add("=");
			operators.add("<");
			operators.add(">");
			operators.add("!=");
			
    		JFrame jf = new JFrame("filterfield test");
    		jf.setLayout(new GridLayout(0, 1));
    		
    		FilterField filter1 = new FilterField(null, true);
    		filter1.setKeys(columns);
    		filter1.setOperators(operators);
    		jf.add(filter1);
    		
    		FilterField filter2 = new FilterField(null, false);
    		filter2.setKeys(columns);
    		filter2.setOperators(operators);
    		jf.add(filter2);
    		
    		FilterField filter3 = new FilterField(null, false);
    		filter3.setKeys(columns);
    		filter3.setOperators(operators);
    		jf.add(filter3);
    		
    		jf.setSize(400,150);
    		jf.setVisible(true);
    		
    		
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		if (FieldEditPanel.VALUE.equals(evt.getPropertyName())) {
//			firePropertyChange(FieldEditPanel.VALUE, 0, 1);			
//		}
	}


}
