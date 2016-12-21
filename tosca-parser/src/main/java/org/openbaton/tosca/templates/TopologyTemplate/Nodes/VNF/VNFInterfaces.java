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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;

/** Created by rvl on 19.08.16. */
public class VNFInterfaces {

  private Object lifecycle = null;

  public Object getLifecycle() {
    return lifecycle;
  }

  public Set<LifecycleEvent> getOpLifecycle() {

    Map<String, Object> lifecycleMap = (Map<String, Object>) lifecycle;
    Set<LifecycleEvent> lifecycleEvents = new HashSet<>();

    if (lifecycle != null) {
      for (String lifecycleName : lifecycleMap.keySet()) {

        LifecycleEvent lifecycleEvent = new LifecycleEvent();

        switch (lifecycleName.toLowerCase()) {
          case "instantiate":
            lifecycleEvent.setEvent(Event.INSTANTIATE);
            break;
          case "start":
            lifecycleEvent.setEvent(Event.START);
            break;
          case "configure":
            lifecycleEvent.setEvent(Event.CONFIGURE);
            break;
          case "delete":
            lifecycleEvent.setEvent(Event.TERMINATE);
            break;
          case "stop":
            lifecycleEvent.setEvent(Event.STOP);
            break;
        }

        lifecycleEvent.setLifecycle_events((ArrayList<String>) lifecycleMap.get(lifecycleName));

        lifecycleEvents.add(lifecycleEvent);
      }
    }

    return lifecycleEvents;
  }

  public void setLifecycle(Object openbaton_lifecycle) {
    this.lifecycle = openbaton_lifecycle;
  }
}
