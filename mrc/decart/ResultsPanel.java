package mrc.decart;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;


public class ResultsPanel extends JPanel implements AbstractPanel {
  private static final long serialVersionUID = 5012744232289704405L;
  Decart T;
  ResultsTableModel rtm = new ResultsTableModel();
  JTable jtab_resultsTable = new JTable(rtm);
  double total_days=0;
  ResultsEventHandler reh = new ResultsEventHandler();
  JPopupMenu jpm_edit = new JPopupMenu("Copy");
  JMenuItem jmi_copyclip = new JMenuItem("Copy to Clipboard");
  
  public void saveConfig(PrintWriter PW) {
  }
  
  public void loadConfig(Element xml) {
    calculateResults();
  }
  
  public void blank() {
  }
  
  public ResultsPanel(Decart _T) {
    super();
    T=_T;
    setLayout(new BorderLayout());
  
    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());
    tablePanel.add(new JScrollPane(jtab_resultsTable),BorderLayout.CENTER);
    add(tablePanel,BorderLayout.CENTER);
    Tools.addMenuItem(jpm_edit,jmi_copyclip,KeyEvent.VK_C,reh);
    jtab_resultsTable.addMouseListener(reh);
    jtab_resultsTable.getTableHeader().setReorderingAllowed(false);
    
  }
  
  public double getBound(double cases, int fact_x, double target,boolean upper) {
    double step=1.0;
    double result = cases;
    while (step>0.000001) {
      while (-Math.log((Math.exp(-result)*Math.pow(result,cases))/(double)fact_x)<target) {
        result+=upper?step:-step;
      }
      result-=upper?step:-step;
      step/=2.0;
    }
    return result;
  }
  
  public void calculateResults() {
    Cursor C = T.getCursor();
    T.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    T.basicPanel.gc.set(GregorianCalendar.HOUR,1);
    T.basicPanel.gc.set(GregorianCalendar.MINUTE,1);
    T.basicPanel.gc.set(GregorianCalendar.YEAR,Integer.parseInt(T.basicPanel.jcb_end_year.getSelectedItem().toString()));
    T.basicPanel.gc.set(GregorianCalendar.MONTH,1+T.basicPanel.jcb_end_month.getSelectedIndex());
    T.basicPanel.gc.set(GregorianCalendar.DATE,1+T.basicPanel.jcb_end_day.getSelectedIndex());
    total_days=T.basicPanel.gc.getTimeInMillis();
    T.basicPanel.gc.set(GregorianCalendar.YEAR,Integer.parseInt(T.basicPanel.jcb_start_year.getSelectedItem().toString()));
    T.basicPanel.gc.set(GregorianCalendar.MONTH,1+T.basicPanel.jcb_start_month.getSelectedIndex());
    T.basicPanel.gc.set(GregorianCalendar.DATE,1+T.basicPanel.jcb_start_day.getSelectedIndex());
    total_days-=T.basicPanel.gc.getTimeInMillis();
    total_days=Math.round((double)total_days/(1000.0*3600.0*24.0));
    //System.out.println("total_days="+total_days);
    double total_visits=0;
    for (int source=0; source<T.sourcesPanel.stm.getRowCount(); source++) {
      for (int recip=0; recip<T.recipientsPanel.rtm.getRowCount(); recip++) {
        total_visits+=T.matrixPanel.ttm.data.get(recip).get(source);
      }
    }
    double visitor_days=0;
    if (T.matrixPanel.jrb_annual.isSelected()) visitor_days=365;
    else if (T.matrixPanel.jrb_study.isSelected()) visitor_days=total_days;
    total_visits*=(total_days/visitor_days);
    rtm.total_cases=0;
    rtm.total_upper=0;
    rtm.total_lower=0;
    for (int source=0; source<T.sourcesPanel.stm.getRowCount(); source++) {
      double pop=(double)Long.parseLong(T.sourcesPanel.stm.getValueAt(source, 1).toString());
      
      for (int recip=0; recip<T.recipientsPanel.rtm.getRowCount(); recip++) {
        double cases=Long.parseLong(T.recipientsPanel.rtm.getValueAt(recip, 1).toString());
        double duration=T.durationPanel.dtm.data.get(recip).get(source);
        double predicted_cases = cases*((pop*total_days)/(duration*total_visits));
        rtm.total_cases+=predicted_cases;

        // Calculate bounds
        double chi_95 = 3.84146;
        double target = chi_95/2.0;
        int fact_x = (int)Math.round(cases);
        int i=fact_x-1;
        while (i>1) {
          fact_x*=i;
          i--;
        }
        double max_llk = -Math.log((Math.exp(-cases)*Math.pow(cases,cases))/(double)fact_x);
        target+=max_llk;
        
        double upper_cases=getBound(cases,fact_x,target,true);
        upper_cases=(predicted_cases*(upper_cases))/cases;
        double lower_cases=getBound(cases,fact_x,target,false);
        lower_cases=(predicted_cases*(lower_cases))/cases;
        rtm.setValueAt(new Long(Math.round(predicted_cases)),source,1);
        rtm.uppers.set(source, (int)Math.round(upper_cases));
        rtm.lowers.set(source, (int)Math.round(lower_cases));
        double total_upper=getBound(cases,fact_x,target,true);
        rtm.total_upper=(int)Math.round((rtm.total_cases*total_upper)/cases);
        double total_lower=getBound(cases,fact_x,target,false);
        rtm.total_lower=(int)Math.round((rtm.total_cases*total_lower)/cases);
        
      }
    }
    T.setCursor(C);
    
  }
  
  class ResultsTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -652828096276359012L;
    public ArrayList<String> regions = new ArrayList<String>();
    public ArrayList<Long> cases = new ArrayList<Long>();
    public ArrayList<Integer> uppers = new ArrayList<Integer>();
    public ArrayList<Integer> lowers = new ArrayList<Integer>();
    double total_cases=0;
    double total_upper=0;
    double total_lower=0;
    
    public ResultsTableModel() {
      super();
      regions = new ArrayList<String>();
      cases = new ArrayList<Long>();
      uppers = new ArrayList<Integer>();
      lowers = new ArrayList<Integer>();
    }
    
    public String getColumnName(int col) {
      if (col==0) return new String("Region");
      else if (col==1) return new String("Estimated Cases");
      else if (col==2) return new String("95% Lower Bound");
      else if (col==3) return new String("95% Upper Bound");
      else return null;
    }
    public int getRowCount() { if (regions==null) return 3; else return 3+regions.size(); }
    public int getColumnCount() { return 4; }
    
    public Object getValueAt(int row, int col) {
      if (row<getRowCount()-3) {
        if (col==0) return regions.get(row);
        else if (col==1) return cases.get(row);
        else if (col==2) return lowers.get(row);
        else if (col==3) return uppers.get(row);
        else return null;
      } else if (row==getRowCount()-3) {
        return "";
      } else if (row==getRowCount()-2) {
        if (col==0) return "                Total";
        else if (col==1) return (int) Math.round(total_cases);
        else if (col==2) return (int) Math.round(total_lower);
        else if (col==3) return (int) Math.round(total_upper);
        else return "";
      } else if (row==getRowCount()-1) {
        if (col==0) return "                Dates";
        else if (col==1) return T.basicPanel.jcb_start_day.getSelectedItem().toString()+" "+
                                              T.basicPanel.jcb_start_month.getSelectedItem().toString()+" "+
                                              T.basicPanel.jcb_start_year.getSelectedItem().toString()+" - ";
        else if (col==2) return T.basicPanel.jcb_end_day.getSelectedItem().toString()+" "+
                                              T.basicPanel.jcb_end_month.getSelectedItem().toString()+" "+
                                              T.basicPanel.jcb_end_year.getSelectedItem().toString();
        else if (col==3) return (int)total_days+" days";
        else return "";
      } else return "";
    }
    
    public void setValueAt(Object value, int row, int col) {
      if (col==0) {
        regions.set(row, (String)value);
      }
      else if (col==1) {
        try {
          long ll = Long.parseLong(value.toString());
          cases.set(row,ll);
        } catch (Exception e) {}
      }
      
      fireTableCellUpdated(row,col);
      
    }
    
    public boolean isCellEditable(int row, int col) { 
      return false;
    }
  
    public void addRow(String region) {
      regions.add(region);
      cases.add(0L);
      uppers.add(0);
      lowers.add(0);
      fireTableRowsInserted(regions.size()-1,regions.size()+1);
      fireTableDataChanged();
      
    }
    public void addRow(int index, String region) {
      regions.add(index,region);
      cases.add(index,0L);
      uppers.add(index,0);
      lowers.add(index,0);
      fireTableRowsInserted(index,index);
      fireTableDataChanged();
    }
    
    public void removeRow(int i) {
      regions.remove(i);
      cases.remove(i);
      uppers.remove(i);
      lowers.remove(i);

      fireTableRowsDeleted(i,i);
      fireTableDataChanged();
     
    }
    
    public void renameRow(int index, String v) {
      regions.set(index, new String(v));
    }
    
    public void reInsertRow(int row) {
      String rowHead = regions.get(row);
      long rowCase = cases.get(row);
      int upper = uppers.get(row);
      int lower = lowers.get(row);
      regions.remove(row);
      cases.remove(row);
      uppers.remove(row);
      lowers.remove(row);
      boolean found=false;
      for (int i=0; i<regions.size(); i++) {
        if (regions.get(i).toString().compareTo(rowHead)>0) {
          regions.add(i,rowHead);
          cases.add(i,rowCase);
          uppers.add(i,upper);
          lowers.add(i,lower);
          found=true;
          i=regions.size();
        }
      } 
      if (!found) {
        regions.add(rowHead);
        cases.add(rowCase);
        uppers.add(upper);
        lowers.add(lower);
      }
      fireTableDataChanged();
    }

  }
  
  class ResultsEventHandler implements ActionListener, MouseListener {
    private void doPop(MouseEvent e){
      jpm_edit.show(e.getComponent(), e.getX(), e.getY());
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
      if (e.getSource()==jtab_resultsTable) {
        if (SwingUtilities.isRightMouseButton(e)) { 
          doPop(e);
        }
        
      }
    }
    public void actionPerformed(ActionEvent e) {
      if (e.getSource()==jmi_copyclip) {
        StringBuffer sbf=new StringBuffer();
        int numrows=jtab_resultsTable.getSelectedRowCount();
        int[] rowsselected=jtab_resultsTable.getSelectedRows();
        for (int i=0; i<numrows; i++) {
          for (int j=0; j<rtm.getColumnCount(); j++) {
            sbf.append(jtab_resultsTable.getValueAt(rowsselected[i],j));
            if (j<rtm.getColumnCount()-1) sbf.append("\t");
          }
          sbf.append("\n");
        }
        StringSelection stsel  = new StringSelection(sbf.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel,stsel);
      }
   
    }
    
  }
  
}
