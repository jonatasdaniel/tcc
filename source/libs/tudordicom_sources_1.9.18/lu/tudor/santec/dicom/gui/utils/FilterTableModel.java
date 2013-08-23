package lu.tudor.santec.dicom.gui.utils;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

//***************************************************************************
//* Class Definition                                                        *
//***************************************************************************

/**
 * The FilterTableModel extends AbstractTableModel and adds functionality 
 * allowing the table model to define filters for every column. By doing
 * so it is possible to control the visible part of data contained in table
 * model.
 * @author nico.mack@tudor.lu
 */

public abstract class FilterTableModel extends AbstractTableModel
{
	protected Vector <?>		m_TableData;
	private TableFilter [] 	m_Filters;

	private int	[]			m_FilteringColumns;
	private int				m_FilteringColumnCount;

	private int	[]			m_FilteredIndexes;
	private int				m_FilteredCount;

//***************************************************************************
//* Class Constants                                                         *
//***************************************************************************

	private static final long serialVersionUID = 1L;

	private static final int c_NoColumn = -1;

//***************************************************************************
//* Constructor(s)                                                          *
//***************************************************************************

public FilterTableModel ()
	{
	m_Filters 				= new TableFilter [this.getColumnCount()];
	m_FilteringColumns  	= new int [this.getColumnCount()];
	
	this.disableAllFilters();
	}

//---------------------------------------------------------------------------
//***************************************************************************
//* Primitives				                                                *
//***************************************************************************
//---------------------------------------------------------------------------
/**
 * converts the specified row index from filtered view into actual index in
 * original (unfiltered) table model.
 * @param p_FilteredRow specifies the row index from filtered view.
 * @return the actual row index in original (unfiltered) table model.
 */
//---------------------------------------------------------------------------

public int filteredToModel (int p_FilteredRow)
	{
	if ( (m_FilteredIndexes != null) &&
	     (p_FilteredRow >= 0) && (p_FilteredRow < m_FilteredCount))
		 return m_FilteredIndexes [p_FilteredRow];
	else return p_FilteredRow;
	}

//---------------------------------------------------------------------------
/**
 * The findFilteringColumn returns the index the specified column occupies
 * in the internal filtering columns array. If the specified column is currently
 * filtering, then the method will return the position in the array, otherwise
 * the method will return <code>c_NoColumn</code>.
 * @param p_Column specifies the column to get position in filtering columns
 * array from.
 * @return The position in filtering columns array if specified column is
 * currently filtering, <code>c_NoColumn</code> otherwise.
 */
//---------------------------------------------------------------------------

private int findFilteringColumn (int p_Column)
	{
	int		l_Index = 0;
	boolean	l_Found = false;

	while ((l_Index < m_FilteringColumnCount) && (l_Found == false))
		{
		if (m_FilteringColumns [l_Index] == p_Column)
			 l_Found = true;
		else l_Index++;
		}

	if (l_Found)
		 return l_Index;
	else return c_NoColumn;
	}

//---------------------------------------------------------------------------
/**
 * Disabling filtering for a column just replaces the column index of disabled
 * column with the <code>c_NoColumn</code> value in the filtering columns array.
 * Doing so results in <i>gaps</i> in the filtering columns array. The 
 * reorganizeFilteringColumns method removes those <i>gaps</i> by consolidating
 * stored column indices. 
  */
//---------------------------------------------------------------------------

private void reorganizeFilteringColumns ()
	{
	int	l_Index;
	int	l_NextFreeSlot = -1;

	for (l_Index = 0; l_Index < m_FilteringColumns.length; l_Index++)
		{
		if (m_FilteringColumns [l_Index] != c_NoColumn)
			{
			if ((l_NextFreeSlot >= 0) && (l_Index - l_NextFreeSlot > 0))
				{
				m_FilteringColumns [l_NextFreeSlot] = m_FilteringColumns [l_Index];
				m_FilteringColumns [l_Index]		 = c_NoColumn;
				l_NextFreeSlot = l_Index;
				}		
			}
		else l_NextFreeSlot = l_Index;
		}
	}

//---------------------------------------------------------------------------
/**
 * Invokes the compile() method on filters defined for currently filtering
 * columns.
 * @see TableFilter#compile()
 */
//---------------------------------------------------------------------------

private void compileActiveFilters ()
	{
	int			l_Index;
	int			l_Column;
	TableFilter	l_Filter;

	for (l_Index = 0; l_Index < m_FilteringColumnCount; l_Index++)
		{
		l_Column = m_FilteringColumns [l_Index];
		l_Filter = m_Filters [l_Column];
		if (l_Filter.isFiltering()) l_Filter.compile();
		}
	}

//---------------------------------------------------------------------------
/**
 * The real workhorse for the FilterTableModel. Calling the applyFilter method
 * will first of all compile all filters specified for currently filtering
 * columns. Next the method will apply those filters to all rows currenlty
 * in the unfiltered table model. Only if column values of a row are accepted
 * by <b>ALL</b> active filters will the row appear in the filtered view.
 */
//---------------------------------------------------------------------------

private void applyFilter ()
	{
	int			l_Row;
	int			l_Column;
	int			l_FilteringColumn;
	int			l_TotalRows;

	Object		l_Value;
	TableFilter	l_Filter;
	boolean		l_Accepted;

	this.compileActiveFilters();

	if (m_TableData != null)
		 l_TotalRows = m_TableData.size();
	else l_TotalRows = 0;

	if (l_TotalRows > 0)
		{
		m_FilteredIndexes = new int [l_TotalRows];
		m_FilteredCount   = 0;

		for (l_Row = 0; l_Row < l_TotalRows; l_Row++)
			{
			l_FilteringColumn = 0;
			l_Accepted     	  = true;

			while ((l_FilteringColumn < m_FilteringColumnCount) && l_Accepted )
				{
				l_Column = m_FilteringColumns [l_FilteringColumn];
				l_Filter = m_Filters [l_Column];

				l_Value = this.getValueAt(l_Row, l_Column);
				l_Accepted &= l_Filter.accept (l_Value);

				l_FilteringColumn++;
				}

			if (l_Accepted) m_FilteredIndexes [m_FilteredCount++] = l_Row;
			}
		}
	}

//---------------------------------------------------------------------------
/**
 * enables filtering for the specified column.
 * @param p_Column specifies the column to enable filtering on.
 */
//---------------------------------------------------------------------------

private void enableFilter (int p_Column)
	{
	TableFilter		l_Filter;
	int				l_FilteringColumn;

	if ((p_Column >= 0) && (p_Column < m_Filters.length))
		{
		l_Filter = m_Filters [p_Column];
		if (l_Filter != null)
			{
			l_FilteringColumn = this.findFilteringColumn (p_Column);
			if (l_FilteringColumn == c_NoColumn)
				{
				m_FilteringColumns [m_FilteringColumnCount++] = p_Column;
				}
			}
		}
	}

//---------------------------------------------------------------------------
/**
 * disables filtering for the specified column.
 * @param p_Column specifies the column to disable filtering on.
 */
//---------------------------------------------------------------------------

private void disableFilter (int p_Column)
	{
	int				l_FilteringColumn;

	if (m_FilteringColumnCount == 0) return;

	if ((p_Column >= 0) && (p_Column < m_Filters.length))
		{
		l_FilteringColumn = this.findFilteringColumn (p_Column);
		if (l_FilteringColumn != c_NoColumn)
			{
			m_FilteringColumns [l_FilteringColumn] = c_NoColumn;
			this.reorganizeFilteringColumns();
			m_FilteringColumnCount--;
			}
		}
	}

//---------------------------------------------------------------------------
/**
 * disables <b>ALL</b> filters.
 */
//---------------------------------------------------------------------------

private void disableAllFilters ()
	{
	int	l_Index;

	m_FilteringColumnCount = 0;

	for (l_Index = 0; l_Index < m_FilteringColumns.length; l_Index++)
		{
		m_FilteringColumns	[l_Index] = c_NoColumn;
		}

	m_FilteredIndexes 	= null;
	m_FilteredCount   	= 0;
	}

//---------------------------------------------------------------------------
//***************************************************************************
//* Class Body		                                                		*
//***************************************************************************
//---------------------------------------------------------------------------
/**
 * Sets the unfiltered table data to be filtered by this table model.
 * @param p_TableData specifies the original Data to be filtered.
 */
//---------------------------------------------------------------------------

protected void setTableData (Vector <?> p_TableData)
	{
	m_TableData = p_TableData;
	this.update();
	}

//---------------------------------------------------------------------------
/**
 * Returns the number of rows currently visible.
 * @return the number of rows currently visible.
 */
//---------------------------------------------------------------------------

protected int getFilteredRowCount ()
	{
	if (this.isFiltering()) return m_FilteredCount;
	else if (m_TableData != null) return m_TableData.size();
	else return 0;
	}

//---------------------------------------------------------------------------
/**
 * installs the specified filter for the specified column. You can only
 * install one filter per column. Calling installFilter more than once results
 * in only the last filter being installed. Please note that installing a filter
 * does not yet enable the filter. Please use the setFilteringEnabled () method
 * to enable filtering on specified column.
 * @param p_Filter specifies the filter to install.
 * @param p_Column specifies the column to install for.
 * @see #setFilteringEnabled (boolean, int)
 */
//---------------------------------------------------------------------------

public void installFilter (TableFilter p_Filter, int p_Column)
	{
	if (p_Filter == null) return;

	if ((p_Column >= 0) && (p_Column < m_Filters.length))
		{
		m_Filters [p_Column] = p_Filter;
		}
	}

//---------------------------------------------------------------------------
/**
 * Returns the table filter defined for the specified column.
 * @param p_Column specifies the column to get defined filter from.
 * @return the filter defined for the specified column, <code>null</code> if
 * none was defined.
 */
//---------------------------------------------------------------------------

public TableFilter getFilter (int p_Column)
	{
	if ((p_Column >= 0) && (p_Column < m_Filters.length))
		{
		return m_Filters [p_Column];
		}
	else return null;
	}

//---------------------------------------------------------------------------
/**
 * disables and uninstalls <b>ALL</b> filters.
 */
//---------------------------------------------------------------------------

public void clearFilters ()
	{
	int l_Column;

	this.disableAllFilters();

	for (l_Column = 0; l_Column < m_Filters.length; l_Column++)
		{
		m_Filters [l_Column] = null;
		}
	}

//---------------------------------------------------------------------------
/**
 * enables or disables filtering for the specified column.
 * @param p_EnableIt specifies whether to enable or disable filtering on
 * specified column. Specify <code>true</code> to enable filtering, <code>false
 * </code> to turn filtering off.
 * @param p_Column specifies the column to either enable or disable filtering.
 */
//---------------------------------------------------------------------------

public void setFilteringEnabled (boolean p_EnableIt, int p_Column)
	{
	if (p_EnableIt) this.enableFilter(p_Column);
			   else this.disableFilter(p_Column);

	this.update ();
	}

//---------------------------------------------------------------------------
/**
 * Disables filtering on <b>every</b> column but filters remain installed.
 */
//---------------------------------------------------------------------------

public void resetFiltering ()
	{
	this.disableAllFilters();
	}

//---------------------------------------------------------------------------
/**
 * checks whether or not this table model is currently filtering, i.e. not all
 * entries may be visible, or not.
 * @return <code>true</code> if table model is currently filtering, <code>false</code>
 * otherwise.
 */
//---------------------------------------------------------------------------

public boolean isFiltering ()
	{
	return (m_FilteringColumnCount > 0);
	}

//---------------------------------------------------------------------------
/**
 * updates table model by applying all active filters.
 */
//---------------------------------------------------------------------------

public void update ()
	{
	if (this.isFiltering()) this.applyFilter();
	}

//---------------------------------------------------------------------------
//***************************************************************************
//* End of Class															*
//***************************************************************************
//---------------------------------------------------------------------------

}
