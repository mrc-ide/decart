package mrc.decart;
import java.io.PrintWriter;

import org.w3c.dom.Element;


public interface AbstractPanel {
  // This interface defines what all the panels must be able to do
  // ie, clear/load/save
  
  public void blank();
  public void loadConfig(Element xml);
  public void saveConfig(PrintWriter PW);

}
