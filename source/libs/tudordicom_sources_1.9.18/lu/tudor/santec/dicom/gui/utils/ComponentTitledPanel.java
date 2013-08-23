package lu.tudor.santec.dicom.gui.utils;

/*
 * ComponentBorder.java 
 * Created on Mar 9, 2005
 * 
 * Adapted from http://www.objects.com.au/java/examples/src/border/*.java
 *
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ComponentTitledPanel extends JPanel {

private static final long serialVersionUID = 1L;
protected ComponentBorder border;
  protected JComponent component;
  protected JComponent contentComponent;
  
  public ComponentTitledPanel(JComponent component, JComponent contentComponent) {
    this.component = component;
    border = new ComponentBorder(component);
    setBorder(border);
    this.setOpaque(false);
    this.contentComponent = contentComponent;
    setLayout(null);
    add(component);
    add(contentComponent); 
  }
  
  public ComponentTitledPanel(Border b, JComponent component, JComponent contentComponent) {
	    this.component = component;
	    border = new ComponentBorder(b, component);
	    setBorder(border);
	    this.setOpaque(false);
	    this.contentComponent = contentComponent;
	    setLayout(null);
	    add(component);
	    add(contentComponent); 
	  }
  
  
  
  
/* (non-Javadoc)
 * @see javax.swing.JComponent#getPreferredSize()
 */
public Dimension getPreferredSize() {
	Insets insets = border.getBorderInsets(contentComponent);
	int height = contentComponent.getPreferredSize().height + insets.top + insets.bottom;
	int width = contentComponent.getPreferredSize().width + insets.left + insets.right;
	Dimension s = new Dimension(width, height);
	return s;
}




public static void main(String[] args) {
	  
	  JFrame jf = new JFrame();
	  
	  jf.setLayout(new FormLayout("fill:pref", "fill:pref"));
	  CellConstraints cc = new CellConstraints();
//	  
	  
	  
	  
	  final JCheckBox hallowelt = new JCheckBox("hallo welt");
	  hallowelt.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
//			System.out.println(hallowelt.isSelected());
		}
		  
	  });
	  
	  ComponentTitledPanel cp = new ComponentTitledPanel(hallowelt, new JLabel("content"));
	  
	  JPanel jp = new JPanel(new BorderLayout());
	  jp.add(cp, BorderLayout.CENTER);
	  
	  jf.add(jp, cc.xy(1,1));
	  
	  jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  jf.pack();
	  
	  jf.setVisible(true);
}
  
public JComponent getTitleComponent() {
    return component;
  }
  
  public void setTitleComponent(JComponent newComponent) {
    remove(component);
    add(newComponent);
    border.setTitleComponent(newComponent);
    component = newComponent;
  }
  
  public JComponent getContentPane() {
    return contentComponent;
  }
  
  public void doLayout() {
    Insets insets = getInsets();
    Rectangle rect = getBounds();
    rect.x = 0;
    rect.y = 0;

    Rectangle compR = border.getComponentRect(rect,insets);
    component.setBounds(compR);
    rect.x += insets.left;
    rect.y += insets.top;
    rect.width  -= insets.left + insets.right;
    rect.height -= insets.top  + insets.bottom;
    contentComponent.setBounds(rect);   
  }

  public class ComponentBorder extends TitledBorder 
  {
	private static final long serialVersionUID = -2758720393831156600L;
	protected JComponent component;
    
    public ComponentBorder(JComponent component) {
      this(null, component, LEFT, TOP);
    }
    
    public ComponentBorder(Border border) {
      this(border, null, LEFT, TOP);
    }
    
    public ComponentBorder(Border border, JComponent component) {
      this(border, component, LEFT, TOP);
    }
    
    public ComponentBorder(Border     border,
                            JComponent component,
                            int        titleJustification,
                            int        titlePosition)      {
      super(border, null, titleJustification,
                          titlePosition, null, null);
      this.component = component;
      if (border == null) {
        this.border = super.getBorder();
      }
    }
    
    public void paintBorder(Component c, Graphics g,
                            int x, int y, int width, int height) {
     	
      Rectangle borderR = new Rectangle(x      +  EDGE_SPACING,
                                        y      +  EDGE_SPACING,
                                        width  - (EDGE_SPACING * 2),
                                        height - (EDGE_SPACING * 2));
      Insets borderInsets;
      if (border != null) {
        borderInsets = border.getBorderInsets(c);
      } else {
        borderInsets = new Insets(0, 0, 0, 0);
      }
      
      Rectangle rect = new Rectangle(x,y,width,height);
      Insets insets = getBorderInsets(c);
      Rectangle compR = getComponentRect(rect, insets);
      int diff;
      switch (titlePosition) {
      case ABOVE_TOP:
          diff = compR.height + TEXT_SPACING;
          borderR.y += diff;
          borderR.height -= diff;
          break;
        case TOP:
        case DEFAULT_POSITION:
          diff = insets.top/2 - borderInsets.top - EDGE_SPACING;
          borderR.y += diff;
          borderR.height -= diff;
          break;
        case BELOW_TOP:
        case ABOVE_BOTTOM:
          break;
        case BOTTOM:
          diff = insets.bottom/2 - borderInsets.bottom - EDGE_SPACING;
          borderR.height -= diff;
          break;
        case BELOW_BOTTOM:
          diff = compR.height + TEXT_SPACING;
          borderR.height -= diff;
          break;
      }
      super.paintBorder(c, g, borderR.x,     borderR.y, 
          borderR.width, borderR.height);    
      Color col = g.getColor();
      g.setColor(c.getBackground());
      g.fillRect(compR.x, compR.y, compR.width, compR.height);
      g.setColor(col);
      //component.repaint();
    }
    
    public Insets getBorderInsets(Component c, Insets insets) {
      Insets borderInsets;
      if (border != null) {
        borderInsets  = border.getBorderInsets(c);
      } else {
        borderInsets  = new Insets(0,0,0,0);
      }
      insets.top    = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
      insets.right  = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
      insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
      insets.left   = EDGE_SPACING + TEXT_SPACING + borderInsets.left;
      
      if (c == null || component == null) {
        return insets;
      }
      
      int compHeight = 0;
      if (component != null) {
        compHeight = component.getPreferredSize().height;
      }
      
      switch (titlePosition) {
      case ABOVE_TOP:
        insets.top    += compHeight + TEXT_SPACING;
        break;
      case TOP:
      case DEFAULT_POSITION:
        insets.top    += Math.max(compHeight,borderInsets.top) - borderInsets.top;
        break;
      case BELOW_TOP:
        insets.top    += compHeight + TEXT_SPACING;
        break;
      case ABOVE_BOTTOM:
        insets.bottom += compHeight + TEXT_SPACING;
        break;
      case BOTTOM:
        insets.bottom += Math.max(compHeight,borderInsets.bottom) - borderInsets.bottom;
        break;
      case BELOW_BOTTOM:
        insets.bottom += compHeight + TEXT_SPACING;
        break;
      }
      return insets;
    }
    
    public JComponent getTitleComponent() {
      return component;
    }
  
    public void setTitleComponent(JComponent component) 
    {
      this.component = component;
    }
  
    public Rectangle getComponentRect(Rectangle rect,Insets borderInsets) {
      Dimension compD = component.getPreferredSize();
      Rectangle compR = new Rectangle(0,0,compD.width,compD.height);
      switch (titlePosition) {
      case ABOVE_TOP:
        compR.y = EDGE_SPACING;
        break;
      case TOP:
      case DEFAULT_POSITION:
        compR.y = EDGE_SPACING + 
                 (borderInsets.top -EDGE_SPACING -TEXT_SPACING -compD.height)/2;
        break;
      case BELOW_TOP:
        compR.y = borderInsets.top - compD.height - TEXT_SPACING;
        break;
      case ABOVE_BOTTOM:
        compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
        break;
      case BOTTOM:
        compR.y = rect.height - borderInsets.bottom + TEXT_SPACING +
                 (borderInsets.bottom -EDGE_SPACING -TEXT_SPACING -compD.height)/2;
        break;
      case BELOW_BOTTOM:
        compR.y = rect.height - compD.height - EDGE_SPACING;
        break;
      }
      switch (titleJustification) {
      case LEFT:
      case DEFAULT_JUSTIFICATION:
        compR.x = TEXT_INSET_H + borderInsets.left;
        break;
      case RIGHT:
        compR.x = rect.width - borderInsets.right -TEXT_INSET_H -compR.width;
        break;
      case CENTER:
        compR.x = (rect.width - compR.width) / 2;
        break;
      }
      return compR;
    }
  }
  

  



}

