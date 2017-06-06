package mrc.decart;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class BasicParamsPanel extends JPanel implements AbstractPanel {
  private static final long serialVersionUID = -5051910090203011273L;
  Decart T;
  
  public void saveConfig(PrintWriter PW) {
    PW.print("  <params>\n");
    PW.print("    <startdate day=\""+jcb_start_day.getSelectedItem().toString()+"\" month=\""+(1+jcb_start_month.getSelectedIndex())+"\" year=\""+jcb_start_year.getSelectedItem().toString()+"\" />\n");
    PW.print("    <enddate day=\""+jcb_end_day.getSelectedItem().toString()+"\" month=\""+(1+jcb_end_month.getSelectedIndex())+"\" year=\""+jcb_end_year.getSelectedItem().toString()+"\" />\n");
    PW.print("  </params>\n");
  }
  
  public void blank() { 
    // Hmm, leave this one - set dates to arbitrary date? What's the point?
  }
  
  public void loadConfig(Element xml) {
    Node n = Tools.getChildNo(xml,"params",0);
    Node sd = Tools.getChildNo(n,"startdate",0);
    Node ed = Tools.getChildNo(n,"enddate",0);
    initDate(jcb_start_day,jcb_start_month,jcb_start_year,
        Integer.parseInt(Tools.getAttribute(sd,"day")),Integer.parseInt(Tools.getAttribute(sd,"month")),Integer.parseInt(Tools.getAttribute(sd,"year")));
    initDate(jcb_end_day,jcb_end_month,jcb_end_year,
        Integer.parseInt(Tools.getAttribute(ed,"day")),Integer.parseInt(Tools.getAttribute(ed,"month")),Integer.parseInt(Tools.getAttribute(ed,"year")));
  }
    
  
  
  JComboBox<String> jcb_start_month = new JComboBox<String>();
  JComboBox<String> jcb_start_day = new JComboBox<String>();
  JComboBox<String> jcb_end_month = new JComboBox<String>();
  JComboBox<String> jcb_end_day = new JComboBox<String>();
  JComboBox<String> jcb_start_year = new JComboBox<String>();
  JComboBox<String> jcb_end_year = new JComboBox<String>();
  JLabel jl_start_date,jl_end_date;
  
  TimeZone tz = TimeZone.getTimeZone("GMT");
  GregorianCalendar gc = new GregorianCalendar(tz);
  BPPEventHandler eh = new BPPEventHandler();
  
  static String[] months = new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
  
  public void initDate(JComboBox<String> day, JComboBox<String>month, JComboBox<String> year, int dd, int mm, int yy) {
    year.setEditable(true);
    year.setSelectedItem(new String(String.valueOf(yy)));
    month.removeAllItems();
    for (int i=0; i<months.length; i++) month.addItem(months[i]);
    month.setSelectedIndex(mm-1);
    gc.set(GregorianCalendar.HOUR,8); // Just avoid any timezone/daylight saving maths error.
    gc.set(GregorianCalendar.YEAR, yy);
    gc.set(GregorianCalendar.MONTH, mm-1);
    int days_in_month=gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    while (day.getItemCount()<days_in_month) day.addItem(String.valueOf(day.getItemCount()+1));
    while (day.getItemCount()>days_in_month) day.removeItemAt(day.getItemCount()-1);
    day.setSelectedIndex(dd-1);
  }
  
  public void updateMonth(JComboBox<String> day, JComboBox<String> month, JComboBox<String> year) {
    gc.set(GregorianCalendar.HOUR,8); // Just avoid any timezone/daylight saving maths error.

    gc.set(GregorianCalendar.MONTH, month.getSelectedIndex());
    int days_in_month=gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    int day_selected = day.getSelectedIndex();
    while (day.getItemCount()<days_in_month) day.addItem(String.valueOf(day.getItemCount()));
    while (day.getItemCount()>days_in_month) day.removeItemAt(day.getItemCount()-1);
    day_selected=Math.min(day_selected,day.getItemCount()-1);
    day.setSelectedIndex(day_selected);   
    gc.set(GregorianCalendar.YEAR, Integer.parseInt(year.getSelectedItem().toString()));
  }
  
  public BasicParamsPanel(Decart _T) {
    super();
    T=_T;
    JPanel startDatePanel = new JPanel(new FlowLayout());
    jl_start_date=new JLabel("Start Date:");
    initDate(jcb_start_day,jcb_start_month,jcb_start_year,1,1,2008);
    initDate(jcb_end_day,jcb_end_month,jcb_end_year,31,12,2008);
    startDatePanel.add(jl_start_date);
    startDatePanel.add(jcb_start_day);
    startDatePanel.add(jcb_start_month);
    startDatePanel.add(jcb_start_year);
    this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    add(startDatePanel);
    JPanel endDatePanel = new JPanel(new FlowLayout());
    jl_end_date=new JLabel("End Date:");
    endDatePanel.add(jl_end_date);
    endDatePanel.add(jcb_end_day);
    endDatePanel.add(jcb_end_month);
    endDatePanel.add(jcb_end_year);
    add(endDatePanel);
    
    jcb_end_month.addItemListener(eh);
    jcb_start_month.addItemListener(eh);
    jcb_start_year.addItemListener(eh);
    jcb_end_year.addItemListener(eh);

  }
  
  class BPPEventHandler implements ActionListener,ItemListener {


    public void actionPerformed(ActionEvent e) {
      
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange()==ItemEvent.SELECTED) { 
        @SuppressWarnings("unchecked")
        JComboBox<String> jcb = (JComboBox<String>) (e.getSource());
        if ((jcb==jcb_start_year) || (jcb==jcb_end_year)) {
          int y=2008;
          try { y=Integer.parseInt(jcb.getSelectedItem().toString()); } catch (Exception ex) {}
          jcb.setSelectedItem(new String(String.valueOf(y)));
          if (jcb==jcb_start_year) updateMonth(jcb_start_day,jcb_start_month,jcb_start_year);
          else updateMonth(jcb_end_day,jcb_end_month,jcb_end_year);
        } 
        else if (jcb==jcb_start_month) updateMonth(jcb_start_day,jcb_start_month,jcb_start_year);
        else if (jcb==jcb_end_month) updateMonth(jcb_end_day,jcb_end_month,jcb_end_year); 
      }
    }
  }
}
