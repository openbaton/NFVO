package org.openbaton.tosca.templates.TopologyTemplate.Nodes.CP;

import java.util.Map;

/**
 * Created by rvl on 17.08.16.
 */
public class CPRequirements {

    //TODO: FIX FOR LIST OF PAIRS
    private String virtualLink = null;
    private String virtualBinding = null;


    public CPRequirements(Object requirements){
        Map<String, String> requirementsMap = (Map<String, String>) requirements;

        if(requirementsMap.containsKey("virtualLink")){
            this.virtualLink = requirementsMap.get("virtualLink");
        }

        if(requirementsMap.containsKey("virtualBinding")){
            this.virtualBinding = requirementsMap.get("virtualBinding");
        }
    }

    public String getVirtualLink() {
        return virtualLink;
    }

    public void setVirtualLink(String virtualLink) {
        this.virtualLink = virtualLink;
    }

    public String getVirtualBinding() {
        return virtualBinding;
    }

    public void setVirtualBinding(String virtualBinding) {
        this.virtualBinding = virtualBinding;
    }

    @Override
    public String toString(){
        return "CP Requirements: \n" +
                "VirtualBinding: " + virtualBinding + "\n" +
                "VirtualLink: " + virtualLink;
    }
}
