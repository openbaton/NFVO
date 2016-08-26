package org.openbaton.tosca.tests;

import org.junit.Test;
import org.openbaton.tosca.parser.CSARParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by rvl on 26.08.16.
 */
public class CSARTest {

  @Test
  public void testFileList() throws Exception {

    CSARParser csarParser = new CSARParser();

    InputStream input = new FileInputStream(new File("src/main/resources/Testing/iperf.csar"));

    csarParser.getFileList(input);
  }
}
