package org.openbaton.catalogue.nfvo.networks;
//The network is still mapped to amazon subnet due to the topology structure


public class AWSNetwork extends BaseNetwork {
    private String ipv4cidr;
    private String state;
    private String vpcId;
    private String avZone;
    private boolean def;


    public String getIpv4cidr() {
        return ipv4cidr;
    }

    public void setIpv4cidr(String ipv4cidr) {
        this.ipv4cidr = ipv4cidr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getAvZone() {
        return avZone;
    }

    public void setAvZone(String avZone) {
        this.avZone = avZone;
    }

    public boolean isDef() {
        return def;
    }

    public void setDef(boolean def) {
        this.def = def;
    }
}
