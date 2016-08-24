package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VDU;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by rvl on 18.08.16.
 */
public class VDUNumaNodes {

    private ArrayList<NumaNode> numaNodes = null;

    public VDUNumaNodes(Object numaNodes){
        Map<String, Object> nodes = (Map<String, Object>) numaNodes;
        this.numaNodes = new ArrayList<NumaNode>();

        if(!nodes.isEmpty()){
            for(Object node : nodes.values()){

                this.numaNodes.add(new NumaNode(node));
            }
        }
    }

    @Override
    public String toString(){
        String output = "";

        for(NumaNode node : numaNodes){

            output += node.toString();
        }
        return output;
    }

    public class NumaNode{

        private int id = 0;
        private ArrayList<Integer> vcpus = null;
        private String mem_size = "0";

        public NumaNode(Object numaNode){

            ArrayList<Map<String, Object>> numaNodeMap = (ArrayList<Map<String, Object>>) numaNode;

            if(numaNodeMap.get(0).containsKey("id")){
                id = (Integer) numaNodeMap.get(0).get("id");
            }

            if(numaNodeMap.get(1).containsKey("vcpus")){
                vcpus = (ArrayList<Integer>) numaNodeMap.get(1).get("vcpus");
            }

            if(numaNodeMap.get(2).containsKey("mem_size")){
                mem_size = (String) numaNodeMap.get(2).get("mem_size");
            }
        }

        public String getMem_size() {
            return mem_size;
        }

        public void setMem_size(String mem_size) {
            this.mem_size = mem_size;
        }

        public ArrayList<Integer> getVcpus() {
            return vcpus;
        }

        public void setVcpus(ArrayList<Integer> vcpus) {
            this.vcpus = vcpus;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString(){
            return "id: "  + id + "\n" +
                    "mem_size: "  + mem_size + "\n" +
                    "vcpus: "  + vcpus + "\n";
        }
    }
}
