
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

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Multipart
{

  private String boundary;
  private static final String CRLF = "\r\n";
  private HttpURLConnection httpConn;
  private String charset;
  private OutputStream outputStream;
  private PrintWriter writer;

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //  RFC1867 multipart/form-data POST request to the API Endpoint.
  ////////////////////////////////////////////////////////////////////////////////////////////////
  public Multipart(String requestURL, String charset)
  throws IOException
  {
    this.charset = charset;

    boundary = String.format("%d", System.currentTimeMillis());

    URL url = new URL(requestURL);
    httpConn = (HttpURLConnection) url.openConnection();
    httpConn.setDoOutput(true);
    httpConn.setDoInput(true);
    httpConn.setRequestProperty("User-Agent", "BTT-BCALLS");
    httpConn.setRequestProperty("Content-Type", "multipart/form-data, boundary="+boundary);
    outputStream = httpConn.getOutputStream();
    writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  public void send_file(String requestURL, File filename, boolean is_mp3) throws IOException
  {

    boundary = String.format("%d", System.currentTimeMillis());

    URL url = new URL(requestURL);
    httpConn = (HttpURLConnection) url.openConnection();
    httpConn.setDoOutput(true);
    httpConn.setDoInput(true);
    httpConn.setRequestProperty("User-Agent", "BTT-BCALLS");
    if( is_mp3 ) {
      httpConn.setRequestProperty("Content-Type", "audio/mpeg");  //use this for mp3
    } else {
      httpConn.setRequestProperty("Content-Type", "audio/aac");
    }
    httpConn.setRequestMethod("PUT");
    outputStream = httpConn.getOutputStream();

    FileInputStream inputStream = new FileInputStream(filename);
    byte[] buffer = new byte[4096];
    int bytesRead = -1;
    while ((bytesRead = inputStream.read(buffer,0,4096)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }
    outputStream.flush();
    inputStream.close();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  public void addFormField(String name, String value)
  {
    writer.append("--"+boundary).append(CRLF);
    writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(CRLF);
    writer.append(CRLF);
    writer.append(value).append(CRLF);
    writer.flush();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////
  public List<String> close_connection() throws IOException
  {
    List<String> response = new ArrayList<String>();

    writer.flush();
    writer.append("--" + boundary + "--").append(CRLF);
    writer.close();

    int status = httpConn.getResponseCode();
    if (status == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
            httpConn.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        response.add(line);
      }
      reader.close();
      httpConn.disconnect();
    } else {
      throw new IOException("status: " + status);
    }

    return response;
  }
}
