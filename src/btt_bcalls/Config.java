
//MIT License
//
//Copyright (c) 2022 bluetailtech
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.
package btt_bcalls;

import javax.swing.filechooser.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Config
{

  public String ffmpeg_bin = "";
  public String base_path = "";
  public String apikey="";
  public String node_id="";
  public String p25_sysid="";
  public boolean enabled=false;

  public Config()
  {
  }

  public void init()
  {
    try {
      File cfg_file;
      FileInputStream fis;
      Properties props = new Properties();

      cfg_file = new File("btt_bcalls.ini");

      if(cfg_file.exists()) {
        fis = new FileInputStream( cfg_file );
        props.load(fis);

        ffmpeg_bin = props.getProperty("ffmpeg_bin","");
        base_path = props.getProperty("base_path","");
        apikey = props.getProperty("apikey","");
        node_id = props.getProperty("node_id","");
        p25_sysid = props.getProperty("p25_sysid","");

        String en = props.getProperty("enable","");
        if(en.equals("true")) enabled=true;
        else enabled=false;

        fis.close();

      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void update()
  {
    try {
      File cfg_file;
      FileOutputStream fos;
      Properties props = new Properties();

      cfg_file = new File("btt_bcalls.ini");

      fos = new FileOutputStream( cfg_file );

      props.setProperty("ffmpeg_bin",ffmpeg_bin);
      props.setProperty("base_path",base_path);
      props.setProperty("apikey",apikey);
      props.setProperty("node_id",node_id);
      props.setProperty("p25_sysid",p25_sysid);


      String en ="";
      if(enabled) en="true";
      else en="false";
      props.setProperty("enable",en);

      props.store(fos,"");
      fos.close();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
