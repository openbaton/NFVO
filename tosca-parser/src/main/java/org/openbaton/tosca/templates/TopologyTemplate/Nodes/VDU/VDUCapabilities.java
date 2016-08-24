package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.Map;

/**
 * Created by rvl on 17.08.16.
 */
public class VDUCapabilities {

    private String mem_page_size = "any";
    private String numa_node_count = null;
    private VDUCPUAllocation cpu_allocation;
    private VDUNumaNodes numa_nodes = null;

    public VDUCapabilities(Object capabilities){

        Map<String, Object> capabilitiesMap = (Map<String, Object>) capabilities;

        if(capabilitiesMap.containsKey("nfv_compute")){
            Map<String, Object> nfv_computeMap = (Map<String, Object>) capabilitiesMap.get("nfv_compute");

            if(nfv_computeMap.containsKey("properties")){
                Map<String, Object> propertiesMap = (Map<String, Object>) nfv_computeMap.get("properties");

                if(propertiesMap.containsKey("cpu_allocation")){

                    this.cpu_allocation = new VDUCPUAllocation(propertiesMap.get("cpu_allocation"));
                }

                if(propertiesMap.containsKey("cpu_allocation")){

                    this.cpu_allocation = new VDUCPUAllocation(propertiesMap.get("cpu_allocation"));
                }

                if(propertiesMap.containsKey("numa_nodes")){
                    this.numa_nodes = new VDUNumaNodes(propertiesMap.get("numa_nodes"));
                }
            }
        }
    }

    public VDUCPUAllocation getCpu_allocation() {
        return cpu_allocation;
    }

    public void setCpu_allocation(VDUCPUAllocation cpu_allocation) {
        this.cpu_allocation = cpu_allocation;
    }

    public String getNuma_node_count() {
        return numa_node_count;
    }

    public void setNuma_node_count(String numa_node_count) {
        this.numa_node_count = numa_node_count;
    }

    public String getMem_page_size() {
        return mem_page_size;
    }

    public void setMem_page_size(String mem_page_size) {
        this.mem_page_size = mem_page_size;
    }

    @Override
    public String toString(){
        return "Capabilities: \n" +
                "mem_page_size: " + mem_page_size + "\n" +
                "cpu_allocation: " + cpu_allocation + "\n" +
                "numa_nodes: " + numa_nodes + "\n" +
                "numa_node_count: \n" + numa_node_count;
    }
}
