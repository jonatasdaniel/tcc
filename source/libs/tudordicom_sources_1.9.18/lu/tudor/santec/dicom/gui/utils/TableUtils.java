package lu.tudor.santec.dicom.gui.utils;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author Patrick Harpes
 * 
 * 
 * @version
 * 
 * <br>
 * $Log: TableUtils.java,v $
 * Revision 1.2  2009-06-17 08:35:33  hermen
 * *** empty log message ***
 *
 * Revision 1.1  2009-05-28 12:54:08  hermen
 * changed a lot
 *
 * Revision 1.1  2007-12-11 07:24:32  moll
 * *** empty log message ***
 *
 * Revision 1.1  2007-01-29 14:16:15  arens
 * initial import
 * <br>
 */
public class TableUtils
{
	static private int columnHeaderWidth(JTable l_Table, TableColumn col)
	{
		TableCellRenderer renderer = l_Table.getTableHeader().getDefaultRenderer();

		Component comp = renderer.getTableCellRendererComponent(l_Table, col.getHeaderValue(), false, false, 0, 0);
		return comp.getPreferredSize().width;
	}

	static private int widestCellInColumn(JTable l_Table, TableColumn col)
	{
		int c = col.getModelIndex(), width = 0, maxw = 0;
		for (int r = 0; r < l_Table.getRowCount(); ++r)
		{
			TableCellRenderer renderer = l_Table.getCellRenderer(r, c);
			Component comp = renderer.getTableCellRendererComponent(l_Table, l_Table.getValueAt(r, c), false, false, r, c);
			width = comp.getPreferredSize().width;
			maxw = width > maxw ? width : maxw;
		}

		return maxw;
	}

	static private int getPreferredWidthForColumn(JTable l_Table, TableColumn col)
	{
		int hw = columnHeaderWidth(l_Table, col);
		int cw = widestCellInColumn(l_Table, col);

		return hw > cw ? hw : cw;
	}
	
	static public void adjustColWidth(JTable l_Table)
	{
		int i, width;
		TableColumn l_Col;

		for (i = 0; i < l_Table.getColumnCount(); i++)
		{			
			l_Col = l_Table.getColumn(l_Table.getColumnName(i));			
			width = getPreferredWidthForColumn(l_Table, l_Col) + 20;

			l_Col.setMinWidth(width);
			l_Col.setMaxWidth(width);

		}
	}
	
	
	static public void adjustColWidth(JTable l_Table, JTable slaveTable)
	{
		int i, width;
		TableColumn l_Col;

		for (i = 0; i < l_Table.getColumnCount(); i++)
		{			
			l_Col = l_Table.getColumn(l_Table.getColumnName(i));			
			width = getPreferredWidthForColumn(l_Table, l_Col) + 20;

			l_Col.setMinWidth(width);
			l_Col.setMaxWidth(width);
			
			TableColumn slaveColumn = slaveTable.getColumn(l_Table.getColumnName(i));
			slaveColumn.setMinWidth(width);
			slaveColumn.setMaxWidth(width);
		}
	}
	
	static public void adjustColWidthByHeader(JTable l_Table)
	{
		int i, width;
		TableColumn l_Col;

		for (i = 0; i < l_Table.getColumnCount(); i++)
		{			
			l_Col = l_Table.getColumn(l_Table.getColumnName(i));			
			width = columnHeaderWidth(l_Table, l_Col) + 6;

			l_Col.setMinWidth(width);
			l_Col.setMaxWidth(width);

		}
	}

}
