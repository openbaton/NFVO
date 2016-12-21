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

package org.openbaton.tosca.tests;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.tosca.exceptions.NotFoundException;
import org.openbaton.tosca.parser.TOSCAParser;
import org.openbaton.tosca.templates.NSDTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP.CPNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU.VDUNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL.VLNodeTemplate;
import org.openbaton.tosca.templates.TopologyTemplate.TopologyTemplate;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/** Created by rvl on 16.08.16. */
public class ToscaTest {

  @Test
  public void testVNFDTemplate() throws FileNotFoundException {

    InputStream vnfdFile =
        new FileInputStream(new File("src/main/resources/Testing/testVNFDTemplate.yaml"));

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(VNFDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    VNFDTemplate vnfdInput = yaml.loadAs(vnfdFile, VNFDTemplate.class);

    System.out.println(vnfdInput.toString());
  }

  @Test
  public void testGetNodesFromVNFDTemplate() throws FileNotFoundException, NotFoundException {

    InputStream vnfdFile =
        new FileInputStream(new File("src/main/resources/Testing/testVNFDTemplate.yaml"));

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(VNFDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    VNFDTemplate vnfdInput = yaml.loadAs(vnfdFile, VNFDTemplate.class);

    System.out.println(vnfdInput.getTopology_template().getVDUNodes());
    System.out.println(vnfdInput.getTopology_template().getCPNodes());
    System.out.println(vnfdInput.getTopology_template().getVNFNodes());
  }

  @Test
  public void testGettingVDUNodes() throws FileNotFoundException {

    InputStream cpFile =
        new FileInputStream(new File("src/main/resources/Testing/testTopologyTemplate.yaml"));

    Constructor constructor = new Constructor(TopologyTemplate.class);
    TypeDescription typeDescription = new TypeDescription(TopologyTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    TopologyTemplate cpInput = yaml.loadAs(cpFile, TopologyTemplate.class);

    List<VDUNodeTemplate> vduNodes = cpInput.getVDUNodes();

    for (VDUNodeTemplate n : vduNodes) {
      System.out.println(n.toString());
    }
  }

  @Test
  public void testGettingVLNodes() throws FileNotFoundException {

    InputStream cpFile =
        new FileInputStream(new File("src/main/resources/Testing/testVNFDTemplate.yaml"));

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(VNFDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    VNFDTemplate cpInput = yaml.loadAs(cpFile, VNFDTemplate.class);

    List<VLNodeTemplate> vlNodes = cpInput.getTopology_template().getVLNodes();

    for (VLNodeTemplate n : vlNodes) {
      System.out.println(n.toString());
    }
  }

  @Test
  public void testTopologyTemplate() throws FileNotFoundException {

    InputStream cpFile =
        new FileInputStream(new File("src/main/resources/Testing/testTopologyTemplate.yaml"));

    Constructor constructor = new Constructor(TopologyTemplate.class);
    TypeDescription typeDescription = new TypeDescription(TopologyTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    TopologyTemplate cpInput = yaml.loadAs(cpFile, TopologyTemplate.class);

    System.out.println(cpInput.toString());
  }

  @Test
  public void testGettingCPNodes() throws FileNotFoundException {

    InputStream cpFile =
        new FileInputStream(new File("src/main/resources/Testing/testTopologyTemplate.yaml"));

    Constructor constructor = new Constructor(TopologyTemplate.class);
    TypeDescription typeDescription = new TypeDescription(TopologyTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    TopologyTemplate cpInput = yaml.loadAs(cpFile, TopologyTemplate.class);

    List<CPNodeTemplate> cpNodes = cpInput.getCPNodes();

    for (CPNodeTemplate n : cpNodes) {
      System.out.println(n.toString());
    }
  }

  @Test
  public void testCreatingVNFDInstance() throws FileNotFoundException, NotFoundException {

    InputStream vnfdFile =
        new FileInputStream(new File("src/main/resources/Testing/testVNFDTemplate.yaml"));

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(VNFDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    VNFDTemplate vnfdInput = yaml.loadAs(vnfdFile, VNFDTemplate.class);

    TOSCAParser parser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = parser.parseVNFDTemplate(vnfdInput);

    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testVNFDServerIperf() throws NotFoundException, FileNotFoundException {

    InputStream vnfdFile =
        new FileInputStream(new File("src/main/resources/Testing/vnfd_server_iperf.yaml"));

    Constructor constructor = new Constructor(VNFDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(VNFDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    VNFDTemplate vnfdInput = yaml.loadAs(vnfdFile, VNFDTemplate.class);

    TOSCAParser parser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = parser.parseVNFDTemplate(vnfdInput);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testNSDIperfTemplate() throws FileNotFoundException, NotFoundException {

    InputStream nsdFile =
        new FileInputStream(new File("src/main/resources/Testing/testNSDIperf.yaml"));

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(NSDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    NSDTemplate nsdInput = yaml.loadAs(nsdFile, NSDTemplate.class);

    TOSCAParser parser = new TOSCAParser();

    NetworkServiceDescriptor nsd = parser.parseNSDTemplate(nsdInput);

    Gson gson = new Gson();
    System.out.println(gson.toJson(nsd));
  }

  @Test
  public void testMultipleVLs() throws FileNotFoundException, NotFoundException {

    InputStream nsdFile =
        new FileInputStream(new File("src/main/resources/Testing/tosca-ns-example.yaml"));

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(NSDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    NSDTemplate nsdInput = yaml.loadAs(nsdFile, NSDTemplate.class);

    TOSCAParser parser = new TOSCAParser();

    NetworkServiceDescriptor nsd = parser.parseNSDTemplate(nsdInput);

    Gson gson = new Gson();
    System.out.println(gson.toJson(nsd));
  }

  @Test
  public void testNSDIperfASTemplate() throws FileNotFoundException, NotFoundException {

    InputStream nsdFile =
        new FileInputStream(new File("src/main/resources/Testing/testNSDIperfAutoscaling.yaml"));

    Constructor constructor = new Constructor(NSDTemplate.class);
    TypeDescription typeDescription = new TypeDescription(NSDTemplate.class);
    constructor.addTypeDescription(typeDescription);

    Yaml yaml = new Yaml(constructor);
    NSDTemplate nsdInput = yaml.loadAs(nsdFile, NSDTemplate.class);

    TOSCAParser parser = new TOSCAParser();

    NetworkServiceDescriptor nsd = parser.parseNSDTemplate(nsdInput);

    Gson gson = new Gson();
    System.out.println(gson.toJson(nsd));
  }
}
