
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

import java.net.*;
import java.net.URLConnection.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class uploader implements Runnable
{

  java.text.SimpleDateFormat time_format;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// user configured information
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
  boolean IS_MP3=false; //when false, convert to m4a (AAC Audio) if supported

  //////////////////////////////////////////////////////////////////////
  String charset = "UTF-8";
  String requestURL = "https://api.broadcastify.com/call-upload";
  String ts="";
  String tg="";
  String freq_str="";
  String freq="";
  String call_duration="";
  String src="";

  int rec_mod=0;
  boolean is_done=true;

  Config cfg;

  /////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////
  public uploader(Config c)
  {
    time_format = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    cfg = c;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////
  //public static void main(String[] args) {
  public void run()
  {

    if( is_m4a_supported() ) {
      System.out.println("system supports m4a audio conversion.");
      IS_MP3=false;
    } else {
      System.out.println("system doesn't support m4a audio conversion. using mp3 conversion");
      IS_MP3=true;
    }

    while(true) {
      try {
        if(cfg.enabled) scan_files();
        SLEEP(50);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////
  public void scan_files()
  {
    try {
      File in_files = new File(cfg.base_path);
      File[] file_list = in_files.listFiles();
      for(int i=0; i<file_list.length; i++) {
        File f = file_list[i];
        if( f.getAbsolutePath().endsWith(".wav")) {
          System.out.println( "\r\nConverting: "+f.getName());

          StringTokenizer st = new StringTokenizer(f.getName(), "_");
          if(st.countTokens()==8) {
            st.nextToken(); //skip
            tg = st.nextToken(); //TG
            ts = st.nextToken(); //TS
            String sys = st.nextToken(); //p25 sys_id


            freq_str = st.nextToken(); //freq
            long freq_long = Long.valueOf(freq_str);

            freq = String.format("%3.5f", ((double) freq_long)/1000000.0 );
            src = st.nextToken();
            call_duration = st.nextToken();

            System.out.println("TG:"+tg);
            System.out.println("TS:"+ts);
            System.out.println("NODE-ID:"+cfg.node_id);
            System.out.println("SYS-ID:"+cfg.p25_sysid);
            System.out.println("FREQ:"+freq);
            System.out.println("RID:"+src);
            System.out.println("DURATION:"+call_duration);

            if(sys.equals(cfg.p25_sysid)) {
              convert_and_upload(f, IS_MP3);
            } else {
              System.out.println("WARNING!!: Wrong P25_SYS_ID for "+f.getName()+" Removing without send");
              //wrong system. delete it.
              f.delete();
            }
          }
        } else if(f.getAbsolutePath().contains("_aud.out")) {
          if(rec_mod++%5==0) {
            if(is_done) {
              is_done=false;
              java.util.Date d = new java.util.Date();
              String ctime = time_format.format(d);
              System.out.println("\r\nCurrent Time: "+ctime);
              String node_mac="";
              try {
                StringTokenizer st = new StringTokenizer(f.getName(), "_");
                if(st!=null && st.countTokens()==3) {
                  st.nextToken();
                  node_mac = st.nextToken();
                }
              } catch(Exception e) {
              }
              System.out.print(String.format("Recording node %s ", node_mac));
            }
            System.out.print(".");
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  /////////////////////////////////////////////////////////////////////////////////////////////
  //    enable-libfdk-aac
  /////////////////////////////////////////////////////////////////////////////////////////////
  public boolean is_m4a_supported()
  {
    try {
      byte[] buffer = new byte[128000];
      Process proc = Runtime.getRuntime().exec(cfg.ffmpeg_bin);
      InputStream is = proc.getErrorStream();
      int off=0;
      int len=0;
      int avail=0;

      proc.waitFor();

      while( is.available() > 0) {
        avail = is.available();
        if(avail>0) {
          len = is.read(buffer,off,avail);
          off += len;
        }
      }
      String ret = new String(buffer);
      ret = ret.trim();

      if( ret.contains("enable-libfdk-aac") ) return true;
    } catch(Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  /////////////////////////////////////////////////////////////////////////////////////////////
  //    enable-libfdk-aac
  /////////////////////////////////////////////////////////////////////////////////////////////
  public boolean is_mp3_supported()
  {
    try {
      byte[] buffer = new byte[128000];
      Process proc = Runtime.getRuntime().exec(cfg.ffmpeg_bin);
      InputStream is = proc.getErrorStream();
      int off=0;
      int len=0;
      int avail=0;

      proc.waitFor();

      while( is.available() > 0) {
        avail = is.available();
        if(avail>0) {
          len = is.read(buffer,off,avail);
          off += len;
        }
      }
      String ret = new String(buffer);
      ret = ret.trim();

      if( ret.contains("enable-libmp3lame") ) return true;
    } catch(Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////
  public void convert_and_upload(File f, boolean is_mp3)
  {
    File ufile=null;
    try {
      java.util.Date d = new java.util.Date();
      long ts = d.getTime();
      String ts_str = String.format("%d", ts);

      String ffmpeg_cmd = "";
      if(is_mp3) {
        ufile = new File("test_"+ts_str+".mp3");
        //MP3 audio
        ffmpeg_cmd = cfg.ffmpeg_bin+" -f wav -ac 1 -guess_layout_max 0 -i "+f.getAbsolutePath()+" "+"-b:a 32k -cutoff 18000 test_"+ts_str+".mp3";
      } else {
        ufile = new File("test_"+ts_str+".m4a");
        //AAC audio
        ffmpeg_cmd = cfg.ffmpeg_bin+" -f wav -ac 1 -guess_layout_max 0 -i "+f.getAbsolutePath()+" "+"-c:a libfdk_aac -b:a 32k -cutoff 18000 test_"+ts_str+".m4a";
      }
      System.out.println("Running FFMPEG");
      Runtime.getRuntime().exec(ffmpeg_cmd);

      upload_file( ufile, is_mp3 );

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      f.delete();
      ufile.delete();
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////
  public void upload_file(File f, boolean is_mp3)
  {

    try {
      Multipart multipart = new Multipart(requestURL, charset);

      multipart.addFormField("apiKey", cfg.apikey);
      multipart.addFormField("systemId", cfg.node_id);  //this is really the assigned node id
      multipart.addFormField("callDuration", call_duration);
      multipart.addFormField("ts", ts);
      multipart.addFormField("tg", tg);
      multipart.addFormField("src", src);
      multipart.addFormField("freq", freq);
      if(is_mp3) {
        multipart.addFormField("enc", "mp3");
      } else {
        multipart.addFormField("enc", "m4a");
      }

      List<String> response = multipart.close_connection();

      String errcode="";
      String url="";

      for (String line : response) {
        StringTokenizer st = new StringTokenizer(line," ");
        if(st!=null && st.countTokens()>=2) {
          errcode = st.nextToken();
          url = st.nextToken();

        }
      }

      //0 = no error
      if(errcode.trim().startsWith("0")) {
        System.out.println("Received One-time Upload URL");

        System.out.print("Sending file...");
        multipart.send_file(url, f, is_mp3); //url, file, is_mp3

        List<String> response2 = multipart.close_connection();
        for (String line : response2) {
          System.out.println(response2);
        }

        System.out.println(" Call Completed.");
        is_done=true;
      } else {
        System.out.println("WARNING!!!!: SERVER ERROR CODE: "+errcode +" Description: "+url);
      }


    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  ////////////////////////////////////////////////////
  ////////////////////////////////////////////////////
  public void SLEEP(long val)
  {
    try {

      long start_ms = new java.util.Date().getTime();

      while (true) {
        long end_ms = new java.util.Date().getTime();
        if(end_ms-start_ms >= val) return;
        Thread.sleep(1);
      }

    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
