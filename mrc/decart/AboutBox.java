package mrc.decart;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


public class AboutBox extends JDialog {
 private static final long serialVersionUID = -6411477814813742981L;
  JEditorPane jep_content = new JEditorPane();
  JButton jb_close = new JButton("OK");
  AboutBoxEventHandler abeh = new AboutBoxEventHandler();
  
  public AboutBox() {
    super();
    setTitle("About Decart "+Decart.version);
    setSize(new Dimension(550,500));
    getContentPane().setLayout(new BorderLayout());
    JPanel jp_flow = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JScrollPane jsp_content = new JScrollPane(jep_content);
    jsp_content.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jp_flow.add(jsp_content);
    jsp_content.setPreferredSize(new Dimension(500,400));
    getContentPane().add(jp_flow,BorderLayout.CENTER);
    JPanel jp_button = new JPanel(new FlowLayout(FlowLayout.CENTER));
    jp_button.add(jb_close);
    getContentPane().add(jp_button,BorderLayout.SOUTH);
    jep_content.setContentType("text/html");
    StringBuffer sb = new StringBuffer();
    try {
      InputStream input = getClass().getResourceAsStream("AboutDecart.html");
      BufferedReader br = new BufferedReader(new InputStreamReader(input));
      String s = br.readLine();
      while (s!=null) {
        sb.append(s);
        s=br.readLine();
      }
      br.close();
    } catch (Exception e) {}
    
    jep_content.setText(sb.toString());
    jep_content.setEditable(false);
    jep_content.addHyperlinkListener(abeh);
    jb_close.addActionListener(abeh);
    
        
  }

  class AboutBoxEventHandler implements ActionListener, HyperlinkListener {
    public void actionPerformed(ActionEvent e) {
      if (e.getSource()==jb_close) {
        setVisible(false);
      }
      
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getSource()==jep_content) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
          System.out.println(e.getURL());
          Desktop desktop = Desktop.getDesktop();
          try {
            desktop.browse(e.getURL().toURI());
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
      
    }
  }
}
