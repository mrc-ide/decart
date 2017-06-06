package mrc.decart;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class Tools {
  public static void addMenuItem(JMenu parent, JMenuItem kid, int mnemonic, ActionListener e) {
    parent.add(kid);
    kid.setMnemonic(mnemonic);
    kid.addActionListener(e);
  }
  
  public static void addMenuItem(JPopupMenu parent, JMenuItem kid, int mnemonic, ActionListener e) {
    parent.add(kid);
    kid.setMnemonic(mnemonic);
    kid.addActionListener(e);
  }
  
  public static Element loadDocumentFromJar(String file, Decart d) {
    Element root=null;
    try {
      InputStream input = d.getClass().getResourceAsStream(file);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(input);
      root=doc.getDocumentElement();
      root.normalize();
    } catch (Exception e) { e.printStackTrace(); }
    return root; 
  }
  
  public static Element loadDocument(String file) {
    Element root = null;
    try {
      File f = new File(file);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(f);
      root=doc.getDocumentElement();
      root.normalize();
    } catch (Exception e) { e.printStackTrace(); }
    return root;
  }
  
  public static String getAttribute(Node parent, String attname)  {
    Node n = parent.getAttributes().getNamedItem(attname);
    if (n==null) return null;
    else return n.getTextContent();
  }

  public static int countChildren(Node parent) {
    int i=0;
    for (int j=0; j<parent.getChildNodes().getLength(); j++) {
      if (parent.getChildNodes().item(j).getNodeType()==Node.ELEMENT_NODE) i++;
    }
    return i;
  }
  
  public static int countChildren(Node parent,String tag) {
    int i=0;
    for (int j=0; j<parent.getChildNodes().getLength(); j++) {
      if (parent.getChildNodes().item(j).getNodeType()==Node.ELEMENT_NODE) {
        if (parent.getChildNodes().item(j).getNodeName().equals(tag)) i++;
      }
    }
    return i;
  }
  public static Node getChildNo(Node parent,String tag,int n) {
    int i=0;
    Node result=null;
    for (int j=0; j<parent.getChildNodes().getLength(); j++) {
      if (parent.getChildNodes().item(j).getNodeType()==Node.ELEMENT_NODE) {
        if (parent.getChildNodes().item(j).getNodeName().equals(tag)) {
          if (i==n) {
            result = parent.getChildNodes().item(j);
            j=parent.getChildNodes().getLength();
          }
          i++;
        }
      }
    }
    return result;
  }
  
  public static Node getChildNo(Node parent,int n) {
    int i=0;
    Node result=null;
    for (int j=0; j<parent.getChildNodes().getLength(); j++) {
      if (parent.getChildNodes().item(j).getNodeType()==Node.ELEMENT_NODE) {
        if (i==n) {
          result = parent.getChildNodes().item(j);
          j=parent.getChildNodes().getLength();
        }
        i++;
      }
    }
    return result;
  }
  
  public static Node getTagWhereAttr(Node parent, String tag, String attr, String attrValue) {
    Node resultNode = null;
    int count = countChildren(parent,tag);
    for (int i=0; i<count; i++) {
      Node n = getChildNo(parent,tag,i);
      if (n.getAttributes().getNamedItem(attr).getTextContent().equals(attrValue)) {
        resultNode=n;
        i=count;
      }
    }
    return resultNode;
  }
  
  public static void selectJCB(String item, JComboBox<String> jcb) {
    for (int i=0; i<jcb.getItemCount(); i++) {
      if (jcb.getItemAt(i).toString().equals(item)) {
        jcb.setSelectedIndex(i);
        i=jcb.getItemCount();
      }
    }
  }
   


  
}
