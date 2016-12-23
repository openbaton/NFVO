/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/** Created by dbo on 31/01/16. */
public final class Utils {

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

  public static VNFDTemplate fileToVNFDTemplate(String fileName) throws FileNotFoundException {

    InputStream tosca = new FileInputStream(new File(fileName));
    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(VNFDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    return yaml.loadAs(tosca, VNFDTemplate.class);
  }

  public static NSDTemplate stringToNSDTemplate(String someYaml) {

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(NSDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    return yaml.loadAs(someYaml, NSDTemplate.class);
  }

  public static VNFDTemplate bytesToVNFDTemplate(ByteArrayOutputStream b) {

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(VNFDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    return yaml.loadAs(new ByteArrayInputStream(b.toByteArray()), VNFDTemplate.class);
  }

  public static NSDTemplate bytesToNSDTemplate(ByteArrayOutputStream b) {

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(NSDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    return yaml.loadAs(new ByteArrayInputStream(b.toByteArray()), NSDTemplate.class);
  }
}
