package org.openbaton.tosca.tests;

import org.junit.Test;
import org.openbaton.tosca.parser.CSARParser;
import org.openbaton.utils.Utils;

import java.io.FileNotFoundException;

/**
 * Created by rvl on 26.08.16.
 */
public class CSARTest {

  @Test
  public void testFileList() throws Exception {

    CSARParser csarParser = new CSARParser();

    csarParser.parseNSDCSAR("src/main/resources/Testing/iperf.csar");
  }

  @Test
  public void testFileToNSD() throws FileNotFoundException {

    System.out.println(
        Utils.fileToNSDTemplate("src/main/resources/Testing/testNSDIperf.yaml").toString());
  }
}
