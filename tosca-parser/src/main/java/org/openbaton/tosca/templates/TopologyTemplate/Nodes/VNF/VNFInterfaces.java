package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VNF;

import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.common.LifecycleEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by rvl on 19.08.16.
 */
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
