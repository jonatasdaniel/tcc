package lu.tudor.santec.dicom.gui.utils;

//***************************************************************************
//* Interface Definition                                                    *
//***************************************************************************

/**
 * The TableFilter interface the method required to implement custom filters
 * to be used in conjunction with the FilterTableModel.
 * @author nico.mack@tudor.lu
 */

public interface TableFilter 
	{	
//---------------------------------------------------------------------------
/**
 * The accept method checks whether the specified value matches the criteria
 * defined by the filter.
 * @param p_Value specifies the value to be check for compliance or not.
 * @return <code>true</code> if instance of filter accepts the specified value,
 * i.e. the specified value match the criteria defined by the filter, 
 * <code>false</code> if filter rejects the specified value.
 */
//---------------------------------------------------------------------------
	
	abstract boolean accept (Object p_Value);
	
//---------------------------------------------------------------------------
/**
 * Depending on the nature of the filter, filter criteria may need to be
 * processed first. A good example are regular expression which need to be
 * compiled before they can be used. The compile method is the method of
 * choice to locate that kind of code.
 */
//---------------------------------------------------------------------------

	abstract void compile ();
	
//---------------------------------------------------------------------------
/**
 * Allows to check whether filter is ready to filter, i.e. ready to accept
 * values.
 * @return <code>true</code> if filter is ready to accept data, <code>false</code>
 * otherwise.
 */
//---------------------------------------------------------------------------
	
	abstract boolean isFiltering ();
	
//---------------------------------------------------------------------------
/**
 * Sets the filtering criteria for the filter.
 * @param p_FilterValue specifies the filtering criteria for the filter.
 */
//---------------------------------------------------------------------------
	
	abstract void setFilter (Object p_FilterValue);
	
//***************************************************************************
//* End of Interface														*
//***************************************************************************
	}
