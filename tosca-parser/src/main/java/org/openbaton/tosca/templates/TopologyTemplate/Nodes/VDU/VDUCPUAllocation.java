package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.Map;

/**
 * Created by rvl on 18.08.16.
 */
public class VDUCPUAllocation {

  //TODO: CHECK VALUES

  private String cpu_affinity = null;
  private String thread_allocation = null;
  private int socket_count = 0;
  private int core_count = 0;
  private int thread_count = 0;

  public VDUCPUAllocation(Object cpu_allocation) {

    Map<String, Object> cpuMap = (Map<String, Object>) cpu_allocation;

    if (cpuMap.containsKey("cpu_affinity")) {
      cpu_affinity = (String) cpuMap.get("cpu_affinity");
    }

    if (cpuMap.containsKey("thread_allocation")) {
      thread_allocation = (String) cpuMap.get("thread_allocation");
    }

    if (cpuMap.containsKey("socket_count")) {
      socket_count = (Integer) cpuMap.get("socket_count");
    }

    if (cpuMap.containsKey("core_count")) {
      core_count = (Integer) cpuMap.get("core_count");
    }

    if (cpuMap.containsKey("thread_count")) {
      thread_count = (Integer) cpuMap.get("thread_count");
    }
  }

  public String getCpu_affinity() {
    return cpu_affinity;
  }

  public void setCpu_affinity(String cpu_affinity) {
    this.cpu_affinity = cpu_affinity;
  }

  public int getThread_count() {
    return thread_count;
  }

  public void setThread_count(int thread_count) {
    this.thread_count = thread_count;
  }

  public int getCore_count() {
    return core_count;
  }

  public void setCore_count(int core_count) {
    this.core_count = core_count;
  }

  public int getSocket_count() {
    return socket_count;
  }

  public void setSocket_count(int socket_count) {
    this.socket_count = socket_count;
  }

  public String getThread_allocation() {
    return thread_allocation;
  }

  public void setThread_allocation(String thread_allocation) {
    this.thread_allocation = thread_allocation;
  }

  @Override
  public String toString() {
    return "cpu_allocation: \n"
        + "cpu_affinity: "
        + cpu_affinity
        + "\n"
        + "thread_allocation: "
        + thread_allocation
        + "\n"
        + "core_count: "
        + core_count
        + "\n"
        + "thread_count: "
        + thread_count
        + "\n"
        + "socket_count: \n"
        + socket_count;
  }
}
