package mrc.decart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class MatrixPanel extends JPanel implements AbstractPanel {
  private static final long serialVersionUID = -5051910090203011273L;
  Decart T;
  
  TMatrixTableModel ttm = new TMatrixTableModel();
  JTable jtab_travelMatrixTable = new JTable(ttm);
  JScrollPane jsp_tmTable = new JScrollPane(jtab_travelMatrixTable);
  RowHeaderRenderer rowHeaderRenderer = new RowHeaderRenderer();
  CentreRenderer centreRenderer = new CentreRenderer();
  JLabel jl_title = new JLabel("<html><center>Visitor Count - see below for time unit. Row = source region. Column = recipient region.<br/>Right Click on table for Editing Options</center></html>");
  JComboBox<String> jcb_datasource = new JComboBox<String>();
  JButton jb_import = new JButton("Import");
  MatrixEventHandler meh = new MatrixEventHandler();
  
  JPopupMenu jpm_edit = new JPopupMenu("Edit");
  JMenuItem jmi_copycol = new JMenuItem("Copy to column");
  JMenuItem jmi_copyrow = new JMenuItem("Copy to row");
  JMenuItem jmi_avgcol = new JMenuItem("Average over column");
  JMenuItem jmi_avgrow = new JMenuItem("Average over row");
  JMenuItem jmi_copyclip = new JMenuItem("Copy to Clipboard");
  JMenuItem jmi_pasteclip = new JMenuItem("Paste from Clipboard");
  JRadioButton jrb_annual = new JRadioButton("Annual");
  JRadioButton jrb_study = new JRadioButton("Study Period");
  
  ButtonGroup bg_period = new ButtonGroup();
  
  int event_col=-1;
  int event_row=-1;
  
  public void saveConfig(PrintWriter PW) {
    String period = "period=\"";
    if (jrb_annual.isSelected()) period+="annual";
    else if (jrb_study.isSelected()) period+="study";
    PW.print("  <tmatrix "+period+" no=\""+ttm.getRowCount()+"\">\n");
    for (int j=0; j<ttm.getRowCount(); j++) {
      PW.print("    <row no=\""+j+"\" data=\"");
      for (int i=1; i<ttm.getColumnCount(); i++) {
        PW.print(ttm.getValueAt(j, i).toString());
        if (i<ttm.getColumnCount()-1) PW.print(",");
      }
      PW.print("\" />\n");
    }
    PW.print("  </tmatrix>\n");
  }
  
  public void loadConfig(Element xml) {
    Node n = Tools.getChildNo(xml, "tmatrix",0);
    if (Tools.getAttribute(n, "period")==null) {
      jrb_annual.setSelected(true);
    } else {
      jrb_annual.setSelected(Tools.getAttribute(n,"period").equals("annual"));
    }
    int no_rows = Integer.parseInt(Tools.getAttribute(n,"no"));
    for (int i=0; i<no_rows; i++) {
      Node nn = Tools.getTagWhereAttr(n, "row", "no", String.valueOf(i));
      String[] bits = Tools.getAttribute(nn,"data").split(",");
      for (int j=0; j<bits.length; j++) {
        ttm.setValueAtFast(Long.parseLong(bits[j]), i, j+1);
      }
    }
  }
  
  public void blank() { 
    // No need to do anything - removal of sources/recipients will do the trick.
  }
    
  
  public MatrixPanel(Decart _T) {
    super();
    T=_T;
    bg_period.add(jrb_annual);
    bg_period.add(jrb_study);
    setLayout(new BorderLayout());
    add(jsp_tmTable,BorderLayout.CENTER);
    JPanel jp_title = new JPanel(new FlowLayout(FlowLayout.CENTER));
    jp_title.add(jl_title);
    add(jp_title,BorderLayout.NORTH);
    jtab_travelMatrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JPanel jp_tableButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    jp_tableButtons.add(jcb_datasource);
    jcb_datasource.addItem("- No sources -");
    jb_import.setEnabled(false);
    jp_tableButtons.add(new JLabel("Travel Period: "));
    jp_tableButtons.add(jb_import);
    jp_tableButtons.add(jrb_annual);
    jp_tableButtons.add(jrb_study);
    add(jp_tableButtons,BorderLayout.SOUTH);
    jtab_travelMatrixTable.addMouseListener(meh);
    Tools.addMenuItem(jpm_edit,jmi_copycol,KeyEvent.VK_L,meh);
    Tools.addMenuItem(jpm_edit,jmi_copyrow,KeyEvent.VK_W,meh);
    jpm_edit.addSeparator();
    Tools.addMenuItem(jpm_edit,jmi_avgcol,KeyEvent.VK_R,meh);
    Tools.addMenuItem(jpm_edit,jmi_avgrow,KeyEvent.VK_G,meh);
    jpm_edit.addSeparator();
    Tools.addMenuItem(jpm_edit,jmi_copyclip,KeyEvent.VK_C,meh);
    Tools.addMenuItem(jpm_edit,jmi_pasteclip,KeyEvent.VK_V,meh);
    jtab_travelMatrixTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    jtab_travelMatrixTable.getTableHeader().setReorderingAllowed(false);
    jrb_annual.addActionListener(meh);
    jrb_study.addActionListener(meh);
    jrb_annual.setSelected(true);
  }
  
  // Sources = rows. Destinations = columns.
  // Hence, from A to B is kind of from left to right. Hopefully this is intuitive.
  
 
  class TMatrixTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 3983062024041246038L;
    ArrayList<ArrayList<Long>> data = new ArrayList<ArrayList<Long>>();
    ArrayList<String> recips = new ArrayList<String>();
    ArrayList<String> srcs = new ArrayList<String>();

       
    public String getColumnName(int col) { if (col>0) return recips.get(col-1); else return "From   \\   To";}
    public int getRowCount() { if (srcs==null) return 0; else return srcs.size(); }
    public int getColumnCount() { if (recips==null) return 0; else return 1+recips.size(); }
    public Object getValueAt(int row, int col) { 
      if (col==0) return srcs.get(row);
      else return data.get(row).get(col-1);
    }
    public boolean isCellEditable(int row, int col) { return (col>0); }
    
    public void setValueAt(Object value, int row, int col) {
      long v=0;
      boolean worked=false;
      try {
        v = Long.parseLong(value.toString());
        worked=true;
      } catch (Exception e ) { worked=false; }
      if (!worked) {
        try {
          v = (long) Math.round(Double.parseDouble(value.toString()));
          worked=true;
        } catch (Exception e) { worked=false; }
      }
      data.get(row).set(col-1,v);
      fireTableCellUpdated(row,col);
      fireTableDataChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }
    
    public void setValueAtFast(Object value, int row, int col) {
      long v=0;
      boolean worked=false;
      try {
        v = Long.parseLong(value.toString());
        worked=true;
      } catch (Exception e ) { worked=false; }
      if (!worked) {
        try {
          v = (long) Math.round(Double.parseDouble(value.toString()));
          worked=true;
        } catch (Exception e) { worked=false; }
      }
      data.get(row).set(col-1,v);
      fireTableCellUpdated(row,col);
      fireTableDataChanged();
      setRenderers();
    }

    public void setRenderers() {
      jtab_travelMatrixTable.getColumnModel().getColumn(0).setCellRenderer(rowHeaderRenderer);
      if (data.size()>0) {
        for (int i=0; i<data.get(0).size(); i++) {
          jtab_travelMatrixTable.getColumnModel().getColumn(i+1).setCellRenderer(centreRenderer);
        }
      }
    }
    
    public void addRow(String src) {
      ArrayList<Long> newRow = new ArrayList<Long>();
      for (int j=0; j<recips.size(); j++) newRow.add(new Long(0));
      data.add(newRow);
      srcs.add(src);
      fireTableRowsInserted(srcs.size()-1,srcs.size()-1);
      fireTableDataChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }

    public void addRow(int index,String src) {
      srcs.add(index,src);
      ArrayList<Long> newRow = new ArrayList<Long>();
      for (int j=0; j<recips.size(); j++) newRow.add(new Long(0));
      data.add(index,newRow);
      fireTableRowsInserted(index,index);
      fireTableDataChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }
    
    public void removeRow(int i) {
      data.remove(i);
      srcs.remove(i);
      fireTableRowsDeleted(i,i);
      fireTableDataChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }
  
    public void addColumn(String recipRegion) {
      recips.add(recipRegion);
      for (int i=0; i<data.size(); i++) {
        data.get(i).add(new Long(0));
      }
      fireTableRowsUpdated(0,data.size()-1);
      fireTableStructureChanged();
      fireTableDataChanged();
      setRenderers(); // (includes +1 for col zero)
      jtab_travelMatrixTable.repaint();
      
    }
      
    public void addColumn(int index, String recipRegion) {
      recips.add(index,recipRegion);
      for (int j=0; j<data.size(); j++) data.get(j).add(index,0L);
      fireTableRowsUpdated(0,data.size()-1);
      fireTableDataChanged();
      fireTableStructureChanged();
      setRenderers(); // (includes +1 for col zero)
      jtab_travelMatrixTable.repaint();
      
      
    }
    
    public void removeColumn(int index) {
      recips.remove(index);
      for (int i=0; i<data.size(); i++) data.get(i).remove(index);
      fireTableRowsUpdated(0,data.size()-1);
      fireTableDataChanged();
      fireTableStructureChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }
    
    public void renameColumn(int index, String v) {
      recips.set(index, new String(v));
    }
    
    public void renameRow(int index, String v) {
      srcs.set(index, new String(v));
    }
    
    
    public void reInsertColumn(int col) {
      String colHead = recips.get(col);
      ArrayList<Long> temp = new ArrayList<Long>();
      for (int j=0; j<data.size(); j++) {
        temp.add(data.get(j).get(col));
      }
      recips.remove(col);
      for (int j=0; j<data.size(); j++) {
        data.get(j).remove(col);
      }
      boolean found=false;
      for (int i=0; i<recips.size(); i++) {
        if (recips.get(i).toString().compareTo(colHead)>0) {
          recips.add(i,colHead);
          for (int j=0; j<data.size(); j++) {
            data.get(j).add(i,temp.get(j));
          }
          found=true;
          i=recips.size();
        }
      } 
      if (!found) {
        recips.add(colHead);
        for (int j=0; j<data.size(); j++) {
          data.get(j).add(temp.get(j));
        }
      }
      fireTableDataChanged();
      fireTableStructureChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }
    
    public void reInsertRow(int col) {
      String rowHead = srcs.get(col);
      ArrayList<Long> temp = new ArrayList<Long>();
      for (int i=0; i<data.size(); i++) {
        for (int j=0; j<data.get(i).size(); j++) {
          temp.add(data.get(i).get(j));
        }
      }
      srcs.remove(col);
      data.remove(col);
      boolean found=false;
      for (int i=0; i<srcs.size(); i++) {
        if (srcs.get(i).toString().compareTo(rowHead)>0) {
          srcs.add(i,rowHead);
          data.add(i,temp);
          found=true;
        }
      } 
      if (!found) {
        srcs.add(rowHead);
        data.add(temp);
      }
      fireTableDataChanged();
      fireTableStructureChanged();
      setRenderers();
      jtab_travelMatrixTable.repaint();
    }

  }
  
  
  class RowHeaderRenderer extends DefaultTableCellRenderer {
    int align=JLabel.CENTER;
    private static final long serialVersionUID = 2098586471151714340L;

    public RowHeaderRenderer() { super(); setHorizontalAlignment(JLabel.RIGHT); }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (table != null) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
          setForeground(header.getForeground());
          setBackground(header.getBackground());
          setFont(header.getFont());
        }
      }

      if (isSelected) setFont(getFont().deriveFont(Font.BOLD));

      setValue(value);
      return this;
    }
  }
  
  class CentreRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 2098586471151714340L;
    public CentreRenderer() { super(); setHorizontalAlignment(JLabel.CENTER); }
    
  }
  
  class MatrixEventHandler implements MouseListener, ActionListener {

    private void doPop(MouseEvent e){
      jpm_edit.show(e.getComponent(), e.getX(), e.getY());
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
      if (e.getSource()==jtab_travelMatrixTable) {
        if (SwingUtilities.isRightMouseButton(e)){ 
          event_row=jtab_travelMatrixTable.rowAtPoint(e.getPoint());
          event_col=jtab_travelMatrixTable.columnAtPoint(e.getPoint());
          doPop(e);
        }
        
      }
    }

    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src==jmi_avgcol) {
        Long v = Long.parseLong(ttm.getValueAt(event_row, event_col).toString());
        double cc = (double)v/(double)ttm.getRowCount();
        double tally=0.0;
        double previous=0.0;
        for (int i=0; i<ttm.getRowCount(); i++) {
          tally+=cc;
          ttm.setValueAt(new Long((long)(tally-previous)),i,event_col);
          previous=(long)tally;
          
        }
        
      } else if (src==jmi_avgrow) {
        Long v = Long.parseLong(ttm.getValueAt(event_row, event_col).toString());
        double cc = (double)v/(double)(ttm.getColumnCount()-1);
        double tally=0.0;
        double previous=0.0;
        for (int i=1; i<ttm.getColumnCount(); i++) {
          tally+=cc;
          ttm.setValueAt(new Long((long)(tally-previous)),event_row,i);
          previous=(long)tally;
        }
        
      } else if (src==jmi_copycol) {
        Long v = Long.parseLong(ttm.getValueAt(event_row, event_col).toString());
        for (int i=0; i<ttm.getRowCount(); i++) {
          ttm.setValueAt(new Long(v),i,event_col);
        }
        
      } else if (src==jmi_copyrow) {
        Long v = Long.parseLong(ttm.getValueAt(event_row, event_col).toString());
        for (int i=1; i<ttm.getColumnCount(); i++) {
          ttm.setValueAt(new Long(v),event_row,i);
        }
      } else if (src==jmi_copyclip) {
        StringBuffer sbf=new StringBuffer();
        // Check to ensure we have selected only a contiguous block of
        // cells
        int numrows=jtab_travelMatrixTable.getSelectedRowCount();
        int[] rowsselected=jtab_travelMatrixTable.getSelectedRows();
        sbf.append("From \\ To\t");
        for (int j=1; j<ttm.getColumnCount(); j++) {
          sbf.append(ttm.recips.get(j-1));
          if (j<ttm.getColumnCount()-1) sbf.append("\t");
        }
        sbf.append("\n");
        for (int i=0; i<numrows; i++) {
          for (int j=0; j<ttm.getColumnCount(); j++) {
            sbf.append(jtab_travelMatrixTable.getValueAt(rowsselected[i],j));
            if (j<ttm.getColumnCount()-1) sbf.append("\t");
          }
          sbf.append("\n");
        }
        StringSelection stsel  = new StringSelection(sbf.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel,stsel);
      
      } else if (src==jmi_pasteclip) { 
        int startRow=(jtab_travelMatrixTable.getSelectedRows())[0];
        int startCol=(jtab_travelMatrixTable.getSelectedColumns())[0];
        try {
          String trstring= (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor));
          StringTokenizer st1=new StringTokenizer(trstring,"\n");
          for (int i=0; st1.hasMoreTokens(); i++) {
            String rowstring=st1.nextToken();
            StringTokenizer st2=new StringTokenizer(rowstring,"\t");
            for(int j=0; st2.hasMoreTokens(); j++) {
               String value=(String)st2.nextToken();
               if (startRow+i<jtab_travelMatrixTable.getRowCount() && startCol+j< jtab_travelMatrixTable.getColumnCount())
                 ttm.setValueAtFast(value,startRow+i,startCol+j);
            }
          }
          jtab_travelMatrixTable.repaint();
        } catch(Exception ex) { ex.printStackTrace(); }    
      } 
    }
    
  }



}
