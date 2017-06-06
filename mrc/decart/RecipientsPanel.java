package mrc.decart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RecipientsPanel extends JPanel implements AbstractPanel {
  private static final long serialVersionUID = -5051910090203011273L;
  Decart T;
  JComboBox<String> jcb_datasources = new JComboBox<String>();
  DefaultListModel<String> lm_regions = new DefaultListModel<String>();
  JList<String> jl_regions = new JList<String>(lm_regions);
  JScrollPane jsp_regions = new JScrollPane(jl_regions);
  JButton jb_addRegions = new JButton("Add Region");
  
  RecipientsTableModel rtm = new RecipientsTableModel();
  JTable jtab_recipientsTable = new JTable(rtm);
  RecipientsEventHandler reh = new RecipientsEventHandler();
  
  JTextField jt_manual_regions = new JTextField();
  JTextField jt_manual_intros = new JTextField();
  JButton jb_addManual = new JButton("Add Region");
  JButton jb_removeRegions = new JButton("Remove Region");
  JLabel jl_intros = new JLabel("Introductions:",SwingConstants.RIGHT);
  JLabel jl_regs = new JLabel("Region:",SwingConstants.RIGHT);
  JLabel jl_mantitle = new JLabel("Or, type:-",SwingConstants.CENTER);
  
  public void saveConfig(PrintWriter PW) {
    PW.print("  <recipients no=\""+rtm.getRowCount()+"\">\n");
    for (int i=0; i<rtm.getRowCount(); i++) {
      PW.print("    <recipient no=\""+i+"\" name=\""+rtm.getValueAt(i, 0).toString()+"\" intros=\""+rtm.getValueAt(i,1).toString()+"\"/>\n");
    }
    PW.print("  </recipients>\n");
  }

  public void loadConfig(Element xml) {
    Node n = Tools.getChildNo(xml, "recipients",0);
    int no_recips = Integer.parseInt(Tools.getAttribute(n,"no"));
    for (int i=0; i<no_recips; i++) {
      Node nn = Tools.getTagWhereAttr(n, "recipient", "no", String.valueOf(i));
      rtm.addRow(new String[] {Tools.getAttribute(nn,"name"),Tools.getAttribute(nn,"intros")});
    }
  }

  
  public void blank() {
    while (rtm.getRowCount()>0) rtm.removeRow(0);
  }
  
  public void initData() {
    NodeList sources = T.resources.getChildNodes();
    for (int i=0; i<sources.getLength(); i++) {
      if (sources.item(i).getNodeName().equals("d")) {
        if (Tools.getAttribute(sources.item(i), "type").equals("pop")) {
          jcb_datasources.addItem(Tools.getAttribute(sources.item(i),"name"));
          selectDataSource(0);
        }
      }
    }
  }
  
  public void selectDataSource(int i) {
    Node src = Tools.getTagWhereAttr(T.resources, "d", "name", jcb_datasources.getItemAt(i));
    lm_regions.clear();
    int children = Tools.countChildren(src,"c");
    String[] entries = new String[children];
    for (int j=0; j<children; j++) {
      Node child = Tools.getChildNo(src, "c", j);
      entries[j]=Tools.getAttribute(child,"name");
    }
    Arrays.sort(entries);
    
    for (int j=0; j<children; j++) {
      lm_regions.addElement(entries[j]);
    }
    
  }
  
  public RecipientsPanel(Decart _T) {
    super();
    T=_T;
    setLayout(new BorderLayout());
    initData();
    JPanel rightPanel = new JPanel(new BorderLayout());
    
    JPanel dataPanel = new JPanel(new BorderLayout());
    dataPanel.add(jcb_datasources,BorderLayout.NORTH);
    dataPanel.add(jsp_regions,BorderLayout.CENTER);
    dataPanel.add(jb_addRegions,BorderLayout.SOUTH);
    rightPanel.add(dataPanel,BorderLayout.CENTER);
    
    JPanel manualPanel = new JPanel(new BorderLayout());
    manualPanel.add(jl_mantitle,BorderLayout.NORTH);
    

    JPanel manualCentre = new JPanel();
    manualCentre.setLayout(new BoxLayout(manualCentre,BoxLayout.Y_AXIS));
    JPanel manualNameEntry = new JPanel(new FlowLayout());
    manualNameEntry.add(jl_regs);
    manualNameEntry.add(jt_manual_regions);
    JPanel manualIntroEntry = new JPanel(new FlowLayout());
    manualIntroEntry.add(jl_intros);
    manualIntroEntry.add(jt_manual_intros);
    manualCentre.add(manualNameEntry);
    manualCentre.add(manualIntroEntry);
    manualCentre.setBorder(new EmptyBorder(10,0,10,0));
    manualPanel.add(manualCentre,BorderLayout.CENTER);
    manualPanel.add(jb_addManual,BorderLayout.SOUTH);
    jl_mantitle.setBorder(new EmptyBorder(10,0,0,0));
    jt_manual_regions.setPreferredSize(new Dimension(120,24));
    jt_manual_intros.setPreferredSize(new Dimension(120,24));
    jl_regs.setPreferredSize(new Dimension(70,24));
    jl_intros.setPreferredSize(new Dimension(70,24));
    
    rightPanel.add(manualPanel,BorderLayout.SOUTH);
    add(rightPanel,BorderLayout.EAST);
    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());
    tablePanel.add(new JScrollPane(jtab_recipientsTable),BorderLayout.CENTER);
    JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    tableButtons.add(jb_removeRegions);
    tablePanel.add(tableButtons,BorderLayout.SOUTH);
    add(tablePanel,BorderLayout.CENTER);
    
    jb_addRegions.setEnabled(false);
    jb_addRegions.addActionListener(reh);
    jb_addManual.setEnabled(false);
    jb_addManual.addActionListener(reh);
    jb_removeRegions.setEnabled(false);
    jb_removeRegions.addActionListener(reh);
    jcb_datasources.addItemListener(reh);
    jt_manual_intros.addCaretListener(reh);
    jl_regions.addListSelectionListener(reh);
    jl_regions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jtab_recipientsTable.getSelectionModel().addListSelectionListener(reh);
    jtab_recipientsTable.getTableHeader().setReorderingAllowed(false);
    manualPanel.setBorder(new EtchedBorder());
    
  }
  
  class RecipientsTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 5103044832597445602L;
    public ArrayList<String> regions = new ArrayList<String>();
    public ArrayList<Long> intros = new ArrayList<Long>();
    
    public RecipientsTableModel() {
      super();
      regions = new ArrayList<String>();
      intros = new ArrayList<Long>();
    }
    
    public String getColumnName(int col) {
      if (col==0) return new String("Recipient Region");
      else if (col==1) return new String("No. of Introductions");
      else return null;
    }
    public int getRowCount() { if (regions==null) return 0; else return regions.size(); }
    public int getColumnCount() { return 2; }
    public Object getValueAt(int row, int col) {
      if (col==0) return regions.get(row);
      else if (col==1) return intros.get(row);
      else return null;
    }
    public boolean isCellEditable(int row, int col) { 
      return true;
    }
  
    public void setValueAt(Object value, int row, int col) {
      if (col==0) {
        boolean okToSet = true;
        if (value.toString().trim().length()==0) {
          JOptionPane.showMessageDialog(RecipientsPanel.this,"Invalid region name: '"+value.toString()+"'");
          okToSet=false;
        }
        for (int i=0; i<regions.size(); i++) {
          if (value.toString().equals(regions.get(i))) {
            JOptionPane.showMessageDialog(RecipientsPanel.this,"Region '"+value.toString()+"' already exists");
            okToSet=false;
            i=regions.size();
          }
          
        }
        if (okToSet) {
          regions.set(row, (String)value);
          T.matrixPanel.ttm.renameRow(row,value.toString());
          T.durationPanel.dtm.renameRow(row,value.toString());
              
          boolean ordered=true;
          if (row>0) if (regions.get(row-1).toString().compareTo(value.toString())>0) ordered=false;
          if (row<regions.size()-1) if (regions.get(row+1).toString().compareTo(value.toString())<0) ordered=false;
          if (!ordered) {
            T.matrixPanel.ttm.reInsertRow(row);
            T.durationPanel.dtm.reInsertRow(row);
            String region=(String) value;
            long cases = (Long) intros.get(row);
            regions.remove(row);
            intros.remove(row);
            fireTableRowsDeleted(row, row);
            boolean found=false;
            for (int j=0; j<regions.size(); j++) {
              if (regions.get(j).compareTo(region)>0) {
                found=true;
                regions.add(j,region);
                intros.add(j,cases);
                fireTableRowsInserted(j,j);
                j=regions.size();
                
              }
            }
            if (!found) { 
              regions.add(region);
              intros.add(cases);
              fireTableRowsInserted(regions.size()-1,regions.size()-1);
            }
          }
        }
      } else if (col==1) {
        try {
          long ll = Long.parseLong(value.toString());
          intros.set(row,ll);
          fireTableRowsUpdated(row, row);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(RecipientsPanel.this, "Number format problem: '"+value.toString()+"'");
        }
      }
      fireTableDataChanged();
      jtab_recipientsTable.repaint();
    }
    
    public void addRow(Object[] values) {
      T.matrixPanel.ttm.addRow(values[0].toString());
      T.durationPanel.dtm.addRow(values[0].toString());
      regions.add((String)values[0]);
      intros.add(Long.parseLong(values[1].toString()));
      fireTableRowsInserted(regions.size()-1,regions.size()-1);
      fireTableDataChanged();
      
    }
    public void addRow(int index, Object[] values) {
      T.matrixPanel.ttm.addRow(index,values[0].toString());
      T.durationPanel.dtm.addRow(index,values[0].toString());
      regions.add(index,(String)values[0]);
      intros.add(index,Long.parseLong(values[1].toString()));
      fireTableRowsInserted(index,index);
      fireTableDataChanged();
    }
    
    public void removeRow(int i) {
      T.matrixPanel.ttm.removeRow(i);
      T.durationPanel.dtm.removeRow(i);
      regions.remove(i);
      intros.remove(i);
      fireTableRowsDeleted(i,i);
      fireTableDataChanged();
    }
  }
  
  class RecipientsEventHandler implements ActionListener, ItemListener, ListSelectionListener, CaretListener {
    public void updateButtons() {
      jb_addManual.setEnabled(jt_manual_intros.getText().trim().length()>0 && jt_manual_regions.getText().trim().length()>0);
      jb_addRegions.setEnabled((jl_regions.getSelectedIndices().length==1) && (jtab_recipientsTable.getRowCount()==0));
      jb_removeRegions.setEnabled(jtab_recipientsTable.getSelectedRows().length>=1);
    }
    
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src==jb_addRegions) {
        int[] selection = jl_regions.getSelectedIndices();
        for (int i=0; i<selection.length; i++) {
          String name = lm_regions.get(selection[i]);
          boolean found=false;
          for (int j=0; j<rtm.getRowCount(); j++) {
            if (rtm.getValueAt(j, 0).toString().equals(name)) {
              found=true;
              j=rtm.getRowCount();
            }
          }
          if (!found) {
            for (int j=0; j<rtm.getRowCount(); j++) {
              if (rtm.getValueAt(j, 0).toString().compareTo(name)>0) {
                found=true;
                rtm.addRow(j, new Object[] {new String(name),new Long(0)});
                j=rtm.getRowCount();
              }
            }
            if (!found) rtm.addRow(new Object[] {new String(name),new Long(0)});
          }
        }
        jtab_recipientsTable.repaint();
        updateButtons();

      } else if (src==jb_addManual) {
        boolean ok=true;
        if ((jt_manual_regions.getText().indexOf(",")>=0) || (jt_manual_intros.getText().indexOf(",")>=0)) {
          JOptionPane.showMessageDialog(T, "Multiple recipient regions is not supported yet.");
          ok=false;
        }
        String[] rregions = jt_manual_regions.getText().trim().split(",");
        String[] rintros = jt_manual_intros.getText().trim().split(",");

        if (rregions.length!=rintros.length) {
          JOptionPane.showMessageDialog(T, "Input problem: "+rregions.length+" region(s), but "+rintros.length+" introductions(s).");
          ok=false;
          
        }
        if (ok) {
          for (int i=0; i<rintros.length; i++) {
            try {
              Long.parseLong(rintros[i]);
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(T, "Numerical problem with region "+i+": "+rintros[i]+" introductions");
              ok=false;
              i=rintros.length;
            }
          }
        }
        if (ok) {
          for (int i=0; i<rintros.length; i++) {
            boolean found=false;
            for (int j=0; j<rtm.getRowCount(); j++) {
              if (rtm.getValueAt(j,0).toString().equals(rregions[i])) {
                found=true;
                rtm.setValueAt(new String(rintros[i]),j,1);
              }
              else if (rtm.getValueAt(j, 0).toString().compareTo(rregions[i])>0) {
                found=true;
                rtm.addRow(j, new Object[] {new String(rregions[i]),Long.parseLong(rintros[i])});
                j=rtm.getRowCount();
              }
            }
            if (!found) rtm.addRow(new Object[] {new String(rregions[i]),Long.parseLong(rintros[i])});
          }
          jtab_recipientsTable.repaint();
          jt_manual_intros.setText("");
          jt_manual_regions.setText("");
        }
        updateButtons();
      
      } else if (src==jb_removeRegions) {
        int[] rows = jtab_recipientsTable.getSelectedRows();
        for (int i=rows.length-1; i>=0; i--) {
          rtm.removeRow(rows[i]);
          jtab_recipientsTable.repaint();
        }
        jb_addRegions.setEnabled((jl_regions.getSelectedIndices().length==1) && (jtab_recipientsTable.getRowCount()==0));
      }        
      updateButtons();
    }

    public void itemStateChanged(ItemEvent e) {
      if (e.getSource()==jcb_datasources) {
        if (e.getStateChange()==ItemEvent.SELECTED) {
          selectDataSource(jcb_datasources.getSelectedIndex());
        }
      }
      updateButtons();
      
    }

    public void valueChanged(ListSelectionEvent e) {
      if (e.getSource()==jl_regions) {
        updateButtons();
      } else if (e.getSource()==jtab_recipientsTable.getSelectionModel()) {
        updateButtons();
      }
    }

    public void caretUpdate(CaretEvent e) {
      updateButtons();      
    }
  }

}
