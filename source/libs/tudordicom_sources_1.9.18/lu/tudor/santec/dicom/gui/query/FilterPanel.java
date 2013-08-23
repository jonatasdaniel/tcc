package lu.tudor.santec.dicom.gui.query;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lu.tudor.santec.i18n.Translatrix;

public class FilterPanel extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	public static final String LINE_DELIM = "\n";
	public static final String FILTER_CHANGED = "FILTER_CHANGED";
	
	private LinkedHashMap<String, Class<?>> keys;
	private LinkedHashMap<String, LinkedHashSet<String>> operatorHash = new LinkedHashMap<String, LinkedHashSet<String>>();
	private Vector<FilterField> filterFields = new Vector<FilterField>();
	private JPanel parentPanel;
	private JPanel filterPanel;
//	private JPanel buttonPanel;
//	private JButton loadButton;
//	private JButton saveButton;
	
	public FilterPanel(JPanel parent) {
		setLayout(new BorderLayout());
		
//		CellConstraints cc = new CellConstraints();
//		this.buttonPanel = new JPanel(new FormLayout("15dlu","15dlu,15dlu,pref:grow"));
//		this.loadButton  = new JButton(IveuIcons.getIcon16(IveuIcons.IMPORT));
//		this.loadButton.setToolTipText(Translatrix.getTranslationString("Iveu.load"));
//		this.loadButton.addActionListener(this);
//		this.buttonPanel.add(this.loadButton, cc.xy(1,1));
//		this.saveButton  = new JButton(IveuIcons.getIcon16(IveuIcons.EXPORT));
//		this.saveButton.setToolTipText(Translatrix.getTranslationString("Iveu.save"));
//		this.saveButton.addActionListener(this);
//		this.buttonPanel.add(this.saveButton, cc.xy(1,2));
//		this.add(buttonPanel, BorderLayout.EAST);
		
		this.filterPanel = new JPanel(new GridLayout(0, 1));
		this.add(filterPanel, BorderLayout.CENTER);
		this.setBorder(new TitledBorder(Translatrix.getTranslationString("Iveu.filter")));
		this.parentPanel = parent;
	}

	public void setKeys(LinkedHashMap<String, Class<?>> columns) {
		this.keys = columns;
		filterFields.clear();
		this.filterPanel.removeAll();
		addFilterPanel();
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}

	public void setOperators(LinkedHashSet<String> operators) {
		this.operatorHash.put(FilterField.DEFAULT, operators);
	}
	
	public void setOperators(Class<?> clazz, LinkedHashSet<String> operators) {
		this.operatorHash.put(clazz.getName(), operators);
	}
	
	private void addFilterPanel() {
		FilterField fp = new FilterField(this, (filterFields.size() == 0));
		for (String key : operatorHash.keySet()) {
			fp.setOperators(key, operatorHash.get(key));
		}
		fp.setKeys(this.keys);
		fp.addPropertyChangeListener(this);
		this.filterPanel.add(fp);
		this.filterFields.add(fp);
	}

	public void addItem(FilterField filterField) {
		addFilterPanel();
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}

	public void removeItem(FilterField filterField) {
		this.filterPanel.remove(filterField);
		this.filterFields.remove(filterField);
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}
	
	public String getFilter() {
		StringBuffer sb = new StringBuffer();
		for (FilterField field : filterFields) {
			sb.append(field.getFilter()).append(LINE_DELIM);
		}
		return sb.toString();
	}
	
	public void setFilter(String filter) {
		String[] strings = filter.split(LINE_DELIM);
		filterFields.clear();
		this.filterPanel.removeAll();
		for (int i = 0; i < strings.length; i++) {
			addFilterPanel();
			filterFields.get(i).setFilter(strings[i]);
		}
		this.validate();
		
	}
	
	
	public String printFilter() {
		return printFilter(getFilter());
	}
	
	public static String printFilter(String filter) {
		StringBuffer sb = new StringBuffer();
		String[] strings = filter.split(LINE_DELIM);
		for (String filterLine : strings) {
			sb.append(FilterField.printFilter(filterLine)).append(LINE_DELIM);
		}
		return sb.toString();
	}

//	public void loadFilter() {
//		if (this.context != null) {
//			SearchFilterBean selectedSfb = filterSelectionDialog.selectFilter(this.context);
//			if (selectedSfb != null) {
//				this.sfb = selectedSfb;
//				
//				if (this.filterInterface != null) {
//					String columns = this.sfb.getColumns();
//					if (columns != null && columns.trim().length() > 0) {
//						this.filterInterface.setColumnFilter(this.sfb.getColumns());					
//					}					
//				}
//				
//				this.setFilter(this.sfb.getFilter());	
//			}
//		}
//	}

//	public void saveFilter() {
//		String name = (String)JOptionPane.showInputDialog(
//                this,
//                Translatrix.getTranslationString("Iveu.filterName"),
//                Translatrix.getTranslationString("Iveu.saveFilter"),
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                (this.sfb != null ? this.sfb.getName() : "")
//                );
//		
//		if (name == null) {
//			return;
//		}
//		
//		// if we do not have a bean or the new name is different from the old one
//		// store as new bean
//		if (sfb == null|| ! sfb.getName().equals(name)) {
//			this.sfb = new SearchFilterBean();			
//		}
//		
//		String filter = getFilter();
//		this.sfb.setFilter(filter);
//		if (this.context != null) {
//			this.sfb.setContext(context);			
//		}
//		if (name != null) {
//			this.sfb.setName(name);			
//		}
//		
//		if (this.filterInterface != null) {
//			String columns = this.filterInterface.getColumnFilter();
//			if (columns != null && columns.trim().length() > 0) {
//				this.sfb.setColumns(columns);
//			}
//			
//		}
//		
//		this.sfb = Iveu.instance.getDataAccess().mergeSearchFilter(sfb);
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (e.getSource().equals(this.loadButton)) {
//			loadFilter();
//		} else if (e.getSource().equals(this.saveButton)) {
//			saveFilter();
//		} 
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		if (FieldEditPanel.VALUE.equals(evt.getPropertyName())) {
//			firePropertyChange(FILTER_CHANGED, "old", "new");
//		}
	}
	
//	public static void main(String[] args) {
//	try {
//		Translatrix.addBundle("lu.tudor.santec.iveu.gui.resources.Translatrix");
//		Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
//		Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
//		Translatrix.addBundle(SwingLocalizer.getBundle());
//		Translatrix.setDefaultWhenMissing(true);
//		SwingLocalizer.localizeJFileChooser();
//		SwingLocalizer.localizeJOptionPane();
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	
//	
//	try {
//		LinkedHashMap<String, Class<?>> columns = new LinkedHashMap<String, Class<?>>();
//		columns.put("String", String.class);
//		columns.put("Integer", Integer.class);
//		columns.put("Double", Double.class);
//		columns.put("Date", Date.class);
//		
//		LinkedHashSet<String> operators = new LinkedHashSet<String>();
//		operators.add("=");
//		operators.add("<");
//		operators.add(">");
//		operators.add("!=");
//		
//		JFrame jf = new JFrame("filterpanel test");
//		jf.setLayout(new BorderLayout());
//		
//		
//		FilterPanel fp = new FilterPanel(new JPanel());
//		fp.setOperators(operators);
//		fp.setColumns(columns);
//		jf.add(fp, BorderLayout.CENTER);
//
//		jf.setSize(400,150);
//		jf.setVisible(true);
//		
//		
//		
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	
//	
//}
	
}
