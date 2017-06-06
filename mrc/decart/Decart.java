package mrc.decart;

/*
 * Tool for analysing disease spread via non-resident travellers
 * Model/Supervision: Christl Donnelly
 * Code: Wes Hinsley
 * 
 * Version 0.1
 * 29th April 2014
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.w3c.dom.Element;

public class Decart extends JFrame {
  public static String version = "1.0";
  private static final long serialVersionUID = -3142390767528180694L;
  private static final String title = new String("DECART - Domestic case Estimates from Cases Among Returning Travellers - v"+version);
  public Element resources;
  
  Font menuFont = new Font("Calibri",Font.PLAIN,15);
  Font selectedFont = new Font("Calibri",Font.BOLD,15);
  
  // This class will be the main panel, with a top-menu for basic Save/Load etc,
  // and a side-menu for the various steps required to make a useful model.
    
  // The other panels:
  JPanel currentPanel = null;  // Remember which panel is currently displayed.
  BasicParamsPanel basicPanel;
  SourcesPanel sourcesPanel;
  RecipientsPanel recipientsPanel;
  MatrixPanel matrixPanel;
  DurationPanel durationPanel;
  ResultsPanel resultPanel;
   
  JMenuBar mb = new JMenuBar();                  // Top menu
  JMenu mConfig = new JMenu("Configuration");    // Perhaps just "File" ...
  JMenuItem mNew = new JMenuItem("New");         // Blank everything - with a warning.
  JMenuItem mLoad = new JMenuItem("Load");       // Load new XML with an unsaved warning.
  JMenuItem mSave = new JMenuItem("Save");       // Save current XML.
  JMenuItem mSaveAs = new JMenuItem("Save As");       // Save current XML. 
  JMenuItem mExit = new JMenuItem("Exit");       // Exit. With an unsaved warning.
  JMenu mHelp= new JMenu("Help");
  JMenuItem mAbout = new JMenuItem("About");
  JPanel pLeftMenu = new JPanel();
  AboutBox ab = new AboutBox();
  
  JJLabel lBasicParams;  // JJLabel is just a tiny extension of JLabel,
  JJLabel lSources;   // keeping track of which panel to show for each label,
  JJLabel lMatrix;       // and it's mouseover/clicked status for pretty hovering.
  JJLabel lDuration;     // See bottom of this class.
  JJLabel lResults;
  JJLabel lRecipients;
  
  JJLabel[] allLabels;
  
  // Main container
  JPanel pMain = new JPanel(new BorderLayout());
  JFileChooser jfc = new JFileChooser();         // Dialog for load/save
  FileFilter XMLfilter = new FileNameExtensionFilter("XML File","xml");
  MainEventHandler eh = new MainEventHandler();  // All events on this page.
  
  String this_filename="";
  String this_dir="C:\\Files\\Dev\\Eclipse\\Christl";
    
  void showPanel(JPanel j) {
    if (currentPanel!=null) pMain.remove(currentPanel);
    currentPanel=j;
    if (currentPanel==resultPanel) resultPanel.calculateResults();
    pMain.add(j,BorderLayout.CENTER);
    pMain.validate();
    pMain.repaint();
  }
  
  public void makePretty(JJLabel[] jl) {
    for (int i=0; i<jl.length; i++) {
      jl[i].setBorder(new EmptyBorder(10,20,0,0));
      jl[i].addMouseListener(eh);
      jl[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      jl[i].setFont(menuFont);
    }
  }
  
  public Decart() {
    super();
    jfc.setCurrentDirectory(new File("."));    
    resources = Tools.loadDocumentFromJar("Resources.xml",this);
    basicPanel = new BasicParamsPanel(this);
    sourcesPanel = new SourcesPanel(this);
    recipientsPanel = new RecipientsPanel(this);
    matrixPanel = new MatrixPanel(this);
    durationPanel = new DurationPanel(this);
    resultPanel = new ResultsPanel(this);
    lBasicParams = new JJLabel("Basic Parameters",basicPanel);
    lSources = new JJLabel("Source Regions",sourcesPanel);
    lRecipients = new JJLabel("Recipient Region",recipientsPanel);
    lMatrix = new JJLabel("Visitor Matrix",matrixPanel);
    lDuration = new JJLabel("Duration Matrix",durationPanel);
    lResults = new JJLabel("Results",resultPanel);
    allLabels = new JJLabel[] {lBasicParams,lSources,lRecipients,lMatrix,lDuration,lResults};
     
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(new Dimension(800,600));
    setTitle(title);
    getContentPane().setLayout(new BorderLayout());
    setJMenuBar(mb);
    mConfig.setMnemonic(KeyEvent.VK_C);
    mb.add(mConfig);
    Tools.addMenuItem(mConfig,mNew,KeyEvent.VK_N,eh);
    Tools.addMenuItem(mConfig,mLoad,KeyEvent.VK_L,eh);
    Tools.addMenuItem(mConfig,mSave,KeyEvent.VK_S,eh);
    Tools.addMenuItem(mConfig,mSaveAs,KeyEvent.VK_A,eh);
    Tools.addMenuItem(mConfig,mExit,KeyEvent.VK_X,eh);
    Tools.addMenuItem(mHelp,mAbout,KeyEvent.VK_A,eh);
    mHelp.setMnemonic(KeyEvent.VK_H);
    mb.add(mHelp);
    mHelp.setFont(menuFont);
    mSave.setEnabled(false);
    mAbout.setFont(menuFont);
    mConfig.setFont(menuFont);
    mNew.setFont(menuFont);
    mSave.setFont(menuFont);
    mSaveAs.setFont(menuFont);
    mLoad.setFont(menuFont);
    mExit.setFont(menuFont);
        
    pLeftMenu.setPreferredSize(new Dimension(170,600));
    pLeftMenu.setBorder(new EtchedBorder());
    pLeftMenu.setLayout(new BoxLayout(pLeftMenu,BoxLayout.Y_AXIS));
    pLeftMenu.add(lBasicParams);
    pLeftMenu.add(lSources);
    pLeftMenu.add(lRecipients);
    pLeftMenu.add(lMatrix);
    pLeftMenu.add(lDuration);
    pLeftMenu.add(lResults);
    makePretty(allLabels);
    getContentPane().add(pLeftMenu,BorderLayout.WEST);
    pMain.setPreferredSize(new Dimension(600,600));
    getContentPane().add(pMain,BorderLayout.CENTER);
    jfc.setCurrentDirectory(new File(this_dir));
    jfc.setFileFilter(XMLfilter);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable(){
      @Override
      public void run(){
        Decart T = new Decart();
        T.setVisible(true);
      }
    });
  }
  
  class JJLabel extends JLabel {
    private static final long serialVersionUID = -8310154012307598912L;
    boolean isOver=false;
    boolean isClicking=false;
    JPanel myPanel;
    public JJLabel(String s, JPanel j) { 
      super(s); 
      myPanel=j;
    }
    
    public void updateCol() {
      if (isClicking) setForeground(Color.RED);
      else if (isOver) setForeground(Color.BLUE);
      else setForeground(Color.BLACK);
    }
    
    public void select() {
      setFont(selectedFont);
      Decart.this.showPanel(myPanel);
      
    }
    
  }
  
  
  class MainEventHandler implements ActionListener, MouseListener {
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if (src==mExit) {
        System.exit(0);
      } else if (src==mLoad) {
                int result = jfc.showOpenDialog(Decart.this);
        if (result==JFileChooser.APPROVE_OPTION) {
          String try_filename=new String(jfc.getSelectedFile().toString());
          mSave.setEnabled(true);
          if ((try_filename==null) || (!new File(try_filename).exists())) {
            JOptionPane.showMessageDialog(Decart.this, "Sorry - error loading file");
          } else {
            this_filename=try_filename;
            Element xmlFile = Tools.loadDocument(this_filename);
            basicPanel.blank();
            sourcesPanel.blank();
            recipientsPanel.blank();
            matrixPanel.blank();
            durationPanel.blank();
            resultPanel.blank();
            basicPanel.loadConfig(xmlFile);
            sourcesPanel.loadConfig(xmlFile);
            recipientsPanel.loadConfig(xmlFile);
            matrixPanel.loadConfig(xmlFile);
            durationPanel.loadConfig(xmlFile);
            resultPanel.loadConfig(xmlFile);
            if (currentPanel==null) lBasicParams.select();

          }
        }
        
      } else if ((src==mSave) || (src==mSaveAs)) {
        boolean proceed=true;
        if (src==mSaveAs) {
          int result = jfc.showSaveDialog(Decart.this);
          if (result==JFileChooser.APPROVE_OPTION) {
            this_filename=jfc.getSelectedFile().toString();
            mSave.setEnabled(true);
          }
          else proceed=false;
        }
        if (proceed) {
          try {
              // It hardly seems worth building an XML file tree. Let's just dump text.
            String file = jfc.getSelectedFile().toString();
            if (!file.toUpperCase().endsWith(".XML")) file+=".xml";
            PrintWriter pw = new PrintWriter(new File(file));
            pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<model>\n");
            basicPanel.saveConfig(pw);
            sourcesPanel.saveConfig(pw);
            recipientsPanel.saveConfig(pw);
            matrixPanel.saveConfig(pw);
            durationPanel.saveConfig(pw);
            resultPanel.saveConfig(pw);
            pw.print("</model>\n");
            pw.flush();
            pw.close();
              
          } catch (Exception ex) { ex.printStackTrace(); }
          
        }
        
      } else if (src==mNew) {
        if (JOptionPane.showConfirmDialog(Decart.this,"This will lose any unsaved changes. OK?")==JOptionPane.YES_OPTION) {
          this_filename="";
          mSave.setEnabled(false);
          basicPanel.blank();
          sourcesPanel.blank();
          recipientsPanel.blank();
          matrixPanel.blank();
          durationPanel.blank();
          resultPanel.blank();
          lBasicParams.select();
        }
        
      } else if (src==mAbout) {
        ab.setVisible(true);
      }
    }

    public void mouseClicked(MouseEvent e) {
      Object src = e.getSource();
      if (src instanceof JJLabel) {
        for (int i=0; i<allLabels.length; i++) allLabels[i].setFont(menuFont);
        ((JJLabel)src).select();
      } 
    }

    public void mouseEntered(MouseEvent e) {
      Object src = e.getSource();
      if (src instanceof JJLabel) {
        ((JJLabel)src).isOver=true;
        ((JJLabel)src).updateCol();
      }
    }

    public void mouseExited(MouseEvent e) {
      Object src = e.getSource();
      if (src instanceof JJLabel) {
        ((JJLabel)src).isOver=false;
        ((JJLabel)src).updateCol();
      }
    }

    public void mousePressed(MouseEvent e) {
      Object src = e.getSource();
      if (src instanceof JJLabel) {
        ((JJLabel)src).isClicking=true;
        ((JJLabel)src).updateCol();
      }
    }

    public void mouseReleased(MouseEvent e) {
      Object src = e.getSource();
      if (src instanceof JLabel) {
        ((JJLabel)src).isClicking=false;
        ((JJLabel)src).updateCol();
      }    
    }
  }
}
