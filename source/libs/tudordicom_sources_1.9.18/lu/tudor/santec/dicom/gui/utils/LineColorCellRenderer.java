/*******************************************************************************
 * This file is part of GECAMed.
 * 
 * GECAMed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (L-GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GECAMed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (L-GPL)
 * along with GECAMed.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * GECAMed is Copyrighted by the Centre de Recherche Public Henri Tudor (http://www.tudor.lu)
 * (c) CRP Henri Tudor, Luxembourg, 2008
 *******************************************************************************/
package lu.tudor.santec.dicom.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class LineColorCellRenderer extends DefaultTableCellRenderer {

	//**************************************************************************
	// *
	// * Class Constants *
	//**************************************************************************
	// *

	private static final long serialVersionUID = 1L;

	public Color c_OddColor;
	public Color c_EvenColor;

	private Font textFont;

	private boolean centerText;

	public LineColorCellRenderer() {
//		c_OddColor = new Color(237, 243, 254);
//		c_EvenColor = new Color(255, 255, 255);
		this(false);
		
	}
	
	/**
	 * 
	 * @param invertColors true = start with white background
	 */
	public LineColorCellRenderer(boolean invertColors) {
		/* ================================================== */
		if (invertColors) {
			/* ------------------------------------------------------- */
			c_EvenColor = new Color(237, 243, 254);
			c_OddColor = new Color(255, 255, 255);
			/* ------------------------------------------------------- */
		} else {
			/* ------------------------------------------------------- */
			c_OddColor = new Color(237, 243, 254);
			c_EvenColor = new Color(255, 255, 255);
			/* ------------------------------------------------------- */
		}
		/* ================================================== */
	}

	public LineColorCellRenderer(int alpha) {
		c_OddColor = new Color(237, 243, 254, alpha);
		c_EvenColor = new Color(255, 255, 255, alpha);
		
	}

	public LineColorCellRenderer(int alpha, Font font, boolean centerText) {
		c_OddColor = new Color(237, 243, 254, alpha);
		c_EvenColor = new Color(255, 255, 255, alpha);
		this.textFont = font;
		this.centerText = centerText;
		if (centerText)
			this.setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable p_Table, Object value, boolean p_IsSelected,
			boolean hasFocus, int p_Row, int p_Column) {
		/* ------------------------------------------------------- */
		super.getTableCellRendererComponent(p_Table, value, p_IsSelected,
				hasFocus, p_Row, p_Column);
		Color l_Background;
		
		
		l_Background = (p_Row % 2 == 0) ? c_OddColor : c_EvenColor;
		if (p_IsSelected)
			this.setBackground(p_Table.getSelectionBackground());
		else
			this.setBackground(l_Background);

		if (textFont != null)
			this.setFont(textFont);

		if (centerText)
			this.setHorizontalAlignment(JLabel.CENTER);

		try {
		    setSize(p_Table.getColumnModel().getColumn(p_Column).getWidth(),
			    getPreferredSize().height);
		    if (p_Table.getRowHeight(p_Row) < getPreferredSize().height) {
			p_Table.setRowHeight(p_Row, getPreferredSize().height);
		    }
		} catch (Exception e) {}
		
		return this;
	}
}
