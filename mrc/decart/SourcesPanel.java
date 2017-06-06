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


public class SourcesPanel extends JPanel implements AbstractPanel {
  private static final long serialVersionUID = -5051910090203011273L;
  Decart T;
  JComboBox<String> jcb_datasources = new JComboBox<String>();
  DefaultListModel<String> lm_regions = new DefaultListModel<String>();
  JList<String> jl_regions = new JList<String>(lm_regions);
  JScrollPane jsp_regions = new JScrollPane(jl_regions);
  JButton jb_addRegions = new JButton("Add Regions");
  SourcesTableModel stm = new SourcesTableModel();
  JTable jtab_sourcesTable = new JTable(stm);
  SourcesEventHandler seh = new SourcesEventHandler();
  
  JTextField jt_manual_regions = new JTextField();
  JTextField jt_manual_pops = new JTextField();
  JButton jb_addManual = new JButton("Add Regions");
  JButton jb_removeRegions = new JButton("Remove Regions");
  JLabel jl_pops = new JLabel("Populations:",SwingConstants.RIGHT);
  JLabel jl_regs = new JLabel("Regions:",SwingConstants.RIGHT);
  JLabel jl_mantitle = new JLabel("Or, type comma-separated:-",SwingConstants.CENTER);
  
  public void saveConfig(PrintWriter PW) {
    PW.print("  <sources no=\""+stm.getRowCount()+"\">\n");
    for (int i=0; i<stm.getRowCount(); i++) {
      PW.print("    <source no=\""+i+"\" name=\""+stm.getValueAt(i, 0).toString()+"\" pop=\""+stm.getValueAt(i,1).toString()+"\"/>\n");
    }
    PW.print("  </sources>\n");
  }
  
  public void loadConfig(Element xml) {
    Node n = Tools.getChildNo(xml, "sources",0);
    int no_sources = Integer.parseInt(Tools.getAttribute(n,"no"));
    for (int i=0; i<no_sources; i++) {
      Node nn = Tools.getTagWhereAttr(n, "source", "no", String.valueOf(i));
      stm.addRow(new String[] {Tools.getAttribute(nn,"name"),Tools.getAttribute(nn,"pop")});
    }
  }
  
  public void blank() {
    while (stm.getRowCount()>0) stm.removeRow(0);  
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
  
  public SourcesPanel(Decart _T) {
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
    JPanel manualPopEntry = new JPanel(new FlowLayout());
    manualPopEntry.add(jl_pops);
    manualPopEntry.add(jt_manual_pops);
    manualCentre.add(manualNameEntry);
    manualCentre.add(manualPopEntry);
    manualCentre.setBorder(new EmptyBorder(10,0,10,0));
    manualPanel.add(manualCentre,BorderLayout.CENTER);
    manualPanel.add(jb_addManual,BorderLayout.SOUTH);
    jl_mantitle.setBorder(new EmptyBorder(10,0,0,0));
    jt_manual_regions.setPreferredSize(new Dimension(120,24));
    jt_manual_pops.setPreferredSize(new Dimension(120,24));
    jl_regs.setPreferredSize(new Dimension(70,24));
    jl_pops.setPreferredSize(new Dimension(70,24));
    
    rightPanel.add(manualPanel,BorderLayout.SOUTH);
    add(rightPanel,BorderLayout.EAST);
    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());
    tablePanel.add(new JScrollPane(jtab_sourcesTable),BorderLayout.CENTER);
    JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    tableButtons.add(jb_removeRegions);
    tablePanel.add(tableButtons,BorderLayout.SOUTH);
    add(tablePanel,BorderLayout.CENTER);
    
    jb_addRegions.setEnabled(false);
    jb_addRegions.addActionListener(seh);
    jb_addManual.setEnabled(false);
    jb_addManual.addActionListener(seh);
    jb_removeRegions.setEnabled(false);
    jb_removeRegions.addActionListener(seh);
    jcb_datasources.addItemListener(seh);
    jt_manual_pops.addCaretListener(seh);
    jl_regions.addListSelectionListener(seh);
    jtab_sourcesTable.getSelectionModel().addListSelectionListener(seh);
    jtab_sourcesTable.getTableHeader().setReorderingAllowed(false);    
    manualPanel.setBorder(new EtchedBorder());
    
  }
  
  class SourcesTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -652828096276359012L;
    public ArrayList<String> regions = new ArrayList<String>();
    public ArrayList<Long> pops = new ArrayList<Long>();
    
    public SourcesTableModel() {
      super();
      regions = new ArrayList<String>();
      pops = new ArrayList<Long>();
    }
    
    public String getColumnName(int col) {
      if (col==0) return new String("Source Region");
      else if (col==1) return new String("Population");
      else return null;
    }
    public int getRowCount() { if (regions==null) return 0; else return regions.size(); }
    public int getColumnCount() { return 2; }
    public Object getValueAt(int row, int col) {
      if (col==0) return regions.get(row);
      else if (col==1) return pops.get(row);
      else return null;
    }
    public boolean isCellEditable(int row, int col) { 
      return true;
    }
  
    public void setValueAt(Object value, int row, int col) {
      if (col==0) {
        boolean okToSet = true;
        if (value.toString().trim().length()==0) {
          JOptionPane.showMessageDialog(SourcesPanel.this,"Invalid region name: '"+value.toString()+"'");
          okToSet=false;
        }
        for (int i=0; i<regions.size(); i++) {
          if (value.toString().equals(regions.get(i))) {
            JOptionPane.showMessageDialog(SourcesPanel.this,"Region '"+value.toString()+"' already exists");
            okToSet=false;
            i=regions.size();
          }
          
        }
        if (okToSet) {
          regions.set(row, (String)value);
          T.matrixPanel.ttm.renameColumn(row,value.toString());
          T.durationPanel.dtm.renameColumn(row,value.toString());
          T.resultPanel.rtm.renameRow(row,value.toString());
              
          boolean ordered=true;
          if (row>0) if (regions.get(row-1).toString().compareTo(value.toString())>0) ordered=false;
          if (row<regions.size()-1) if (regions.get(row+1).toString().compareTo(value.toString())<0) ordered=false;
          if (!ordered) {
            T.matrixPanel.ttm.reInsertColumn(row);
            T.durationPanel.dtm.reInsertColumn(row);
            T.resultPanel.rtm.reInsertRow(row);
            String region=(String) value;
            long pop = (Long) pops.get(row);
            regions.remove(row);
            pops.remove(row);
            fireTableRowsDeleted(row, row);
            boolean found=false;
            for (int j=0; j<regions.size(); j++) {
              if (regions.get(j).compareTo(region)>0) {
                found=true;
                regions.add(j,region);
                pops.add(j,pop);
                j=stm.getRowCount();
                fireTableRowsInserted(j, j);
              }
            }
            if (!found) { 
              regions.add(region);
              pops.add(pop);
              fireTableRowsInserted(regions.size()-1,regions.size()-1);
            }
          }
        }
      } else if (col==1) {
        try {
          long ll = Long.parseLong(value.toString());
          pops.set(row,ll);
          fireTableCellUpdated(row,1);
          
        } catch (Exception e) {
          JOptionPane.showMessageDialog(SourcesPanel.this, "Number format problem: '"+value.toString()+"'");
        }
      }
      fireTableDataChanged();
      jtab_sourcesTable.repaint();
    }

    public void addRow(Object[] values) {
      T.matrixPanel.ttm.addColumn(values[0].toString());
      T.durationPanel.dtm.addColumn(values[0].toString());
      T.resultPanel.rtm.addRow(values[0].toString());
      regions.add((String)values[0]);
      pops.add(Long.parseLong(values[1].toString()));
      fireTableRowsInserted(regions.size()-1,regions.size()-1);
      fireTableDataChanged();
      
    }
    public void addRow(int index, Object[] values) {
      T.matrixPanel.ttm.addColumn(index, values[0].toString());
      T.durationPanel.dtm.addColumn(index, values[0].toString());
      T.resultPanel.rtm.addRow(index,values[0].toString());
      regions.add(index,(String)values[0]);
      pops.add(index,Long.parseLong(values[1].toString()));
      fireTableRowsInserted(index,index);
      fireTableDataChanged();
    }
    
    public void removeRow(int i) {
      T.matrixPanel.ttm.removeColumn(i);
      T.durationPanel.dtm.removeColumn(i);
      T.resultPanel.rtm.removeRow(i);
      regions.remove(i);
      pops.remove(i);
      fireTableRowsDeleted(i,i);
      fireTableDataChanged();
      
    }
  }
  
  class SourcesEventHandler implements ActionListener, ItemListener, ListSelectionListener, CaretListener {
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src==jb_addRegions) {
        Node dtag = null;
        int no_dtags = Tools.countChildren(T.resources,"d");
        for (int i=0; i<no_dtags; i++) {
          if (Tools.getAttribute(Tools.getChildNo(T.resources, "d",i),"name").equals(jcb_datasources.getSelectedItem().toString())) {
            dtag=Tools.getChildNo(T.resources,"d",i);
            i=no_dtags;
          }
        }
        int[] selection = jl_regions.getSelectedIndices();
        for (int i=0; i<selection.length; i++) {
          String name = lm_regions.get(selection[i]);
          Node nameTag = Tools.getTagWhereAttr(dtag,"c","name",name);
          Long pop = Long.parseLong(Tools.getAttribute(nameTag, "pop"));
          boolean found=false;
          for (int j=0; j<stm.getRowCount(); j++) {
            if (stm.getValueAt(j, 0).toString().equals(name)) {
              found=true;
              j=stm.getRowCount();
            }
          }
          if (!found) {
            for (int j=0; j<stm.getRowCount(); j++) {
              if (stm.getValueAt(j, 0).toString().compareTo(name)>0) {
                found=true;
                stm.addRow(j, new Object[] {new String(name),new Long(pop)});
                j=stm.getRowCount();
              }
            }
            if (!found) stm.addRow(new Object[] {new String(name),new Long(pop)});
          }
        }
        
        jtab_sourcesTable.repaint();

      } else if (src==jb_addManual) {
        String[] sregions = jt_manual_regions.getText().trim().split(",");
        String[] spops = jt_manual_pops.getText().trim().split(",");
        boolean ok=true;
        if (sregions.length!=spops.length) {
          JOptionPane.showMessageDialog(T, "Input problem: "+sregions.length+" region(s), but "+spops.length+" population(s).");
          ok=false;
          
        }
        if (ok) {
          for (int i=0; i<spops.length; i++) {
            try {
              Long.parseLong(spops[i]);
            } catch (Exception ex) {
              JOptionPane.showMessageDialog(T, "Numerical problem with population "+i+": "+spops[i]);
              ok=false;
              i=spops.length;
            }
          }
        }
        if (ok) {
          for (int i=0; i<spops.length; i++) {
            boolean found=false;
            for (int j=0; j<stm.getRowCount(); j++) {
              if (stm.getValueAt(j,0).toString().equals(sregions[i])) {
                found=true;
                stm.setValueAt(new String(spops[i]),j,1);
              }
              else if (stm.getValueAt(j, 0).toString().compareTo(sregions[i])>0) {
                found=true;
                stm.addRow(j, new Object[] {new String(sregions[i]),Long.parseLong(spops[i])});
                j=stm.getRowCount();
              }
            }
            if (!found) stm.addRow(new Object[] {new String(sregions[i]),Long.parseLong(spops[i])});
          }
          jtab_sourcesTable.repaint();
          jt_manual_pops.setText("");
          jt_manual_regions.setText("");
        }
      
      } else if (src==jb_removeRegions) {
        int[] rows = jtab_sourcesTable.getSelectedRows();
        for (int i=rows.length-1; i>=0; i--) {
          stm.removeRow(rows[i]);
          jtab_sourcesTable.repaint();
        }
        
      }
    }

    public void itemStateChanged(ItemEvent e) {
      if (e.getSource()==jcb_datasources) {
        if (e.getStateChange()==ItemEvent.SELECTED) {
          selectDataSource(jcb_datasources.getSelectedIndex());
        }
      }
      
    }

    public void valueChanged(ListSelectionEvent e) {
      if (e.getSource()==jl_regions) {
        jb_addRegions.setEnabled(jl_regions.getSelectedIndices().length>0);
      } else if (e.getSource()==jtab_sourcesTable.getSelectionModel()) {
        jb_removeRegions.setEnabled(jtab_sourcesTable.getSelectedRows().length>=1);
      }
    }

    public void caretUpdate(CaretEvent e) {
      jb_addManual.setEnabled(jt_manual_pops.getText().trim().length()>0 && jt_manual_regions.getText().trim().length()>0);      
    }
  }

}
