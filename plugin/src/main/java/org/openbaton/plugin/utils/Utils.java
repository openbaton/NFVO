/*
 * Copyright (c) 2016 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.plugin.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Created by gca on 15/12/16. */
public class Utils {

  public static Process executePlugin(
      String path,
      String name,
      String brokerIp,
      String port,
      int consumers,
      String username,
      String password,
      String pluginLogPath)
      throws IOException {
    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "java", "-jar", path, name, brokerIp, port, "" + consumers, username, password);
    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
    File dir = new File(pluginLogPath);
    if (!dir.exists()) dir.mkdirs();
    File file = new File(pluginLogPath + "/plugin-" + name + "_" + ft.format(dNow) + ".log");
    processBuilder.redirectErrorStream(true);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.to(file));
    return processBuilder.start();
  }
}
