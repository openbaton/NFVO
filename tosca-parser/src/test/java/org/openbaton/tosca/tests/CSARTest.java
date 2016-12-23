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
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.tosca.parser.CSARParser;

/** Created by rvl on 26.08.16. */
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
