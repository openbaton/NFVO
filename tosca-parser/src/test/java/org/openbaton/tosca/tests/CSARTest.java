package org.openbaton.tosca.tests;

import com.google.gson.Gson;
import org.junit.Test;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.tosca.parser.CSARParser;

/**
 * Created by rvl on 26.08.16.
 */
public class CSARTest {

  @Test
  public void testNSD() throws Exception {

    CSARParser csarParser = new CSARParser();

    NetworkServiceDescriptor nsd = csarParser.parseNSDCSAR("src/main/resources/Testing/iperf.csar");

    Gson gson = new Gson();
    System.out.println(gson.toJson(nsd));
  }

  @Test
  public void testVNFD() throws Exception {

    CSARParser csarParser = new CSARParser();

    csarParser.parseVNFCSAR("src/main/resources/Testing/iperf-server.csar");
  }
}
