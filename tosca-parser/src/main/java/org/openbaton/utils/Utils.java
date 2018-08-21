/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
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

package org.openbaton.utils;

import java.io.*;
import java.util.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF.VNFAutoscaling;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;

/** Created by dbo on 31/01/16. */
public final class Utils {

  private static final Logger log = LoggerFactory.getLogger(Utils.class);

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

  /**
   * Since the yaml representing a VNFDTemplate does not match the Java object (namely the
   * VNFAutoscaling class) we have to write a custom constructor which can be passed to a Yaml
   * object so that it can parse the yaml file and construct a VNFDTemplate.
   */
  private static class VnfdTemplateConstructor extends Constructor {

    public VnfdTemplateConstructor() {
      this.yamlClassConstructors.put(NodeId.mapping, new ConstructVnfdTemplate());
    }

    private class ConstructVnfdTemplate extends Constructor.ConstructMapping {

      /**
       * Parse a snakeyaml Node object to an Object (a primitive type, HashMap or ArrayList
       * depending on the type of Node).
       *
       * @param node
       * @return
       * @throws BadFormatException
       */
      private Object yamlNodeToMap(Node node) throws BadFormatException {
        if (node instanceof ScalarNode) {
          ScalarNode scalarNode = (ScalarNode) node;
          if (scalarNode.getTag().equals(Tag.FLOAT))
            return Double.parseDouble(scalarNode.getValue());
          else if (scalarNode.getTag().equals(Tag.INT))
            return Integer.parseInt(scalarNode.getValue());
          else if (scalarNode.getTag().equals(Tag.BOOL))
            return Boolean.parseBoolean(scalarNode.getValue());
          else return scalarNode.getValue();
        } else if (node instanceof MappingNode) {
          Map<String, Object> nodeMap = new HashMap<String, Object>();
          MappingNode mappingNode = (MappingNode) node;
          for (NodeTuple nodeTuple : mappingNode.getValue()) {
            if (nodeTuple.getKeyNode() instanceof ScalarNode) {
              String key = ((ScalarNode) nodeTuple.getKeyNode()).getValue();
              nodeMap.put(key, yamlNodeToMap(nodeTuple.getValueNode()));
            }
          }
          return nodeMap;
        } else if (node instanceof SequenceNode) {
          List<Object> nodeList = new ArrayList<Object>();
          SequenceNode sequenceNode = (SequenceNode) node;
          for (Node n : sequenceNode.getValue()) nodeList.add(yamlNodeToMap(n));
          return nodeList;
        }
        throw new BadFormatException("This should never happen and is probably a bug.");
      }

      /**
       * This method checks if a snakeyaml Node is of type VNFAutoscaling and applies the custom
       * parsing for it. Otherwise the default is used.
       *
       * @param node
       * @param object
       * @return
       */
      @Override
      protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
        if (node.getType().equals(VNFAutoscaling.class)) {
          Object nodeMap = null;
          try {
            nodeMap = yamlNodeToMap(node);
          } catch (BadFormatException e) {
            e.printStackTrace();
          }
          VNFAutoscaling vnfAutoscaling = new VNFAutoscaling(nodeMap);
          return vnfAutoscaling;
        }
        return super.constructJavaBean2ndStep(node, object);
      }
    }
  }

  public static VNFDTemplate bytesToVNFDTemplate(ByteArrayOutputStream b) {
    Constructor constructor = new VnfdTemplateConstructor();
    TypeDescription projectDesc = new TypeDescription(VNFDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    return yaml.loadAs(new ByteArrayInputStream(b.toByteArray()), VNFDTemplate.class);
  }

  public static NSDTemplate bytesToNSDTemplate(ByteArrayOutputStream b) throws BadFormatException {

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription projectDesc = new TypeDescription(NSDTemplate.class);

    constructor.addTypeDescription(projectDesc);

    Yaml yaml = new Yaml(constructor);
    try {
      return yaml.loadAs(new ByteArrayInputStream(b.toByteArray()), NSDTemplate.class);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
      throw new BadFormatException(
          "Problem parsing the descriptor. Check if yaml is formatted correctly.");
    }
  }
}
