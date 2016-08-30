package org.openbaton.tosca.tests;

import org.junit.Test;
import org.openbaton.tosca.parser.CSARParser;

/**
 * Created by rvl on 26.08.16.
 */
public class CSARTest {

  @Test
  public void testNSD() throws Exception {

    CSARParser csarParser = new CSARParser();

    csarParser.parseNSDCSAR("src/main/resources/Testing/iperf.csar");
  }

  @Test
  public void testVNFD() throws Exception {

    CSARParser csarParser = new CSARParser();

    csarParser.parseVNFCSAR("src/main/resources/Testing/iperf-server.csar");
  }
}
