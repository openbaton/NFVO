/*
 * Copyright (c) 2016 Fraunhofer FOKUS
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.utils;

import java.io.*;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by dbo on 31/01/16.
 */
public final class Utils {
  public static void addFileToFolder(String sourceFile, String targetDir) throws IOException {
    File dir = new File(targetDir);
    if (!dir.exists()) dir.mkdir();
    try {
      FileInputStream in = new FileInputStream(sourceFile);
      FileOutputStream out = new FileOutputStream(targetDir + (new File(sourceFile)).getName());
      byte[] buf = new byte[1024];
      int i = 0;
      while ((i = in.read(buf)) != -1) {
        out.write(buf, 0, i);
      }
      in.close();
      out.close();
    } catch (IOException e) {
      System.out.println("Error copying file");
    }
  }

  public static void createTar(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<File>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zout = new ZipOutputStream(out);
      res = zout;
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (File kid : directory.listFiles()) {
          String name = base.relativize(kid.toURI()).getPath();
          if (kid.isDirectory()) {
            queue.push(kid);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            Utils.copy(kid, zout);
            zout.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  public static void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  public static void copy(InputStream in, File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try {
      copy(in, out);
    } finally {
      out.close();
    }
  }
}
