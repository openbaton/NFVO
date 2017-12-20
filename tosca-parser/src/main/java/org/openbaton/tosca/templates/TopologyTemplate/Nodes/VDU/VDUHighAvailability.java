package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;
import org.openbaton.catalogue.mano.common.HighAvailability;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;

public class VDUHighAvailability {
    private HighAvailability highAvailability;

  @SuppressWarnings({"unsafe", "unchecked"})
  public VDUHighAvailability(Object o) {
    highAvailability = new HighAvailability();

    Map<String, Object> haMap = (Map<String, Object>) o;

    if (haMap.containsKey("resiliencyLevel")) {
      highAvailability.setResiliencyLevel(
          ResiliencyLevel.valueOf(((String) haMap.get("resiliencyLevel")).toUpperCase()));
    }
    if (haMap.containsKey("redundancyScheme")) {
      highAvailability.setRedundancyScheme((String) haMap.get("redundancyScheme"));
    }
  }

  public HighAvailability getHighAvailability() {
    return highAvailability;
  }
}
