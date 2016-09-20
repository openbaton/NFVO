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
