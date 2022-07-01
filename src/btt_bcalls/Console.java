
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

import java.io.*;
import java.awt.*;
import java.awt.Color.*;
import java.awt.event.*;
import javax.swing.*;

public class Console extends JPanel implements Runnable
{
  private JTextArea textArea;
  private Thread reader;
  private Thread reader2;
  private boolean quit;

  private final PipedInputStream pin=new PipedInputStream();
  private final PipedInputStream pin2=new PipedInputStream();

  public Console()
  {
    textArea=new JTextArea();
    textArea.setLineWrap(true);
    textArea.setEditable(true);
    textArea.setBackground( Color.black );
    textArea.setForeground( Color.white );
    textArea.setFont(new java.awt.Font("Mono", 0, 14));
    JButton button=new JButton("Clear Console");

    setLayout(new BorderLayout());
    add(new JScrollPane(textArea),BorderLayout.CENTER);

    try {
      PipedOutputStream pout=new PipedOutputStream(this.pin);
      System.setOut(new PrintStream(pout,true));
    } catch (java.io.IOException io) {
    } catch (SecurityException se) {
    }

    try {
      PipedOutputStream pout2=new PipedOutputStream(this.pin2);
      System.setErr(new PrintStream(pout2,true));
    } catch (java.io.IOException io) {
    } catch (SecurityException se) {
    }

    quit=false;

    reader=new Thread(this);
    reader.setDaemon(true);
    reader.start();

    reader2=new Thread(this);
    reader2.setDaemon(true);
    reader2.start();
  }


  public synchronized void run()
  {
    try {
      while (Thread.currentThread()==reader) {
        try {
          this.wait(100);
        } catch(InterruptedException ie) {}
        if (pin.available()!=0) {
          String input=this.readLine(pin);
          textArea.append(input);

          if( textArea.getText().length() > 128000 ) {
            textArea.replaceRange("",0,64000);
          }
          textArea.setCaretPosition( textArea.getDocument().getLength() );

        }
        if (quit) return;
      }

      while (Thread.currentThread()==reader2) {
        try {
          this.wait(100);
        } catch(InterruptedException ie) {}
        if (pin2.available()!=0) {
          String input=this.readLine(pin2);
          textArea.append(input);

          if( textArea.getText().length() > 128000 ) {
            textArea.replaceRange("",0,64000);
          }

          textArea.setCaretPosition( textArea.getDocument().getLength() );
        }
        if (quit) return;
      }
    } catch (Exception e) {
    }

  }

  public synchronized String readLine(PipedInputStream in) throws IOException
  {
    String input="";
    do {
      int available=in.available();
      if (available==0) break;
      byte b[]=new byte[available];
      in.read(b);
      input=input+new String(b,0,b.length);
    } while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
    return input;
  }

}
