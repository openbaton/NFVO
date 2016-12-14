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

package org.openbaton.catalogue.mano.common.monitoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Created by mob on 18.11.15. */
public class ObjectSelection implements Serializable {

  /*
   * Identifies the object instances for which performance information should be collected.
   * The object instances for this information element will be virtualised resources.
   * These resources shall be known by the Virtualised Resource Management interface.
   * One of the two alternatives (objectType+ objectFilter or objectInstanceId) shall be present.
   * */
  //hostnames for the moment
  private List<String> objectInstanceIds;
  private List<String> objectTypes;
  private Object filter;

  public ObjectSelection() {
    objectInstanceIds = new ArrayList<>();
  }

  public void addObjectInstanceId(String objectInstanceId) {
    objectInstanceIds.add(objectInstanceId);
  }

  public List<String> getObjectInstanceIds() {
    return objectInstanceIds;
  }
}
