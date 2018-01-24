package org.openbaton.nfvo.api.configuration;

import java.io.*;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
  private String body;

  public CustomHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;
    try {
      InputStream inputStream = request.getInputStream();
      if (inputStream != null) {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        char[] charBuffer = new char[128];
        int bytesRead;
        try {
          while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
            stringBuilder.append(charBuffer, 0, bytesRead);
          }
        } catch (IOException e) {
          stringBuilder.append("");
        }
      } else {
        stringBuilder.append("");
      }
    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
    }
    body = stringBuilder.toString();
  }

  @Override
  public ServletInputStream getInputStream() {
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {
        throw new RuntimeException("Not implemented");
      }

      public int read() {
        return byteArrayInputStream.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(this.getInputStream()));
  }

  public String getBody() {
    return this.body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
