package lu.tudor.santec.dicom.gui.utils;

public class TableFulltextFilter implements TableFilter{

	private String searchText = "";

	public boolean accept(Object p_Value) {
		/* ====================================================== */
		if (p_Value instanceof String) {
			/* ------------------------------------------------------- */
			boolean matching = false;
			/* ------------------------------------------------------- */
			// empty string is not matching anything
			/* ------------------------------------------------------- */
			if ("".equals(searchText))
				return false;
			/* ------------------------------------------------------- */
			// lookup in the text content
			/* ------------------------------------------------------- */
			if (compare((String) p_Value, searchText))
				matching = true;
			/* ------------------------------------------------------- */
			return matching;
		}
		return false;
		/* ====================================================== */
	}

	
	/**
	 * Checks if s1 contains s2
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean compare(String s1, String s2) {
		/* ================================================== */
		if (s1 == null || s2 == null)
			return false;
		/* ------------------------------------------------------- */
		return s1.toUpperCase().contains(s2.toUpperCase());
		/* ================================================== */
	}
	
	public void compile() {
		/* ====================================================== */
		
		/* ====================================================== */
	}

	public boolean isFiltering() {
		/* ====================================================== */
		return true;
		/* ====================================================== */
	}

	public void setFilter(Object p_FilterValue) {
		/* ====================================================== */
		if (p_FilterValue instanceof String) 
			this.searchText  = ((String) p_FilterValue).trim();
		/* ====================================================== */
	}

}
