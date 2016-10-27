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
import org.junit.Test;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.tosca.parser.CSARParser;
import org.openbaton.tosca.parser.TOSCAParser;
import org.openbaton.tosca.templates.VNFDTemplate;
import org.openbaton.utils.Utils;

import java.io.FileNotFoundException;

/**
 * Created by rvl on 31.08.16.
 */
public class IMSCoreTests {

  @Test
  public void testBind9() throws FileNotFoundException {

    VNFDTemplate vnfdTemplate = Utils.fileToVNFDTemplate("src/main/resources/IMS/bind9.yaml");

    TOSCAParser toscaParser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testFHOSS() throws FileNotFoundException {

    VNFDTemplate vnfdTemplate = Utils.fileToVNFDTemplate("src/main/resources/IMS/fhoss.yaml");

    TOSCAParser toscaParser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testICSCF() throws FileNotFoundException {

    VNFDTemplate vnfdTemplate = Utils.fileToVNFDTemplate("src/main/resources/IMS/icscf.yaml");

    TOSCAParser toscaParser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testSCSCF() throws FileNotFoundException {

    VNFDTemplate vnfdTemplate = Utils.fileToVNFDTemplate("src/main/resources/IMS/scscf.yaml");

    TOSCAParser toscaParser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void testPCSCF() throws FileNotFoundException {

    VNFDTemplate vnfdTemplate = Utils.fileToVNFDTemplate("src/main/resources/IMS/pcscf.yaml");

    TOSCAParser toscaParser = new TOSCAParser();

    VirtualNetworkFunctionDescriptor vnfd = toscaParser.parseVNFDTemplate(vnfdTemplate);
    Gson gson = new Gson();
    System.out.println(gson.toJson(vnfd));
  }

  @Test
  public void createCSARs() throws Exception {

    CSARParser csarParser = new CSARParser();

    csarParser.parseVNFCSAR("src/main/resources/IMS/bind9.csar");
  }
}
