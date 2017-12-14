package org.openbaton.catalogue.nfvo.viminstances;

import org.openbaton.catalogue.nfvo.images.AWSImage;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.AWSNetwork;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AmazonVimInstance extends BaseVimInstance {
    private String vpcName;

    private String vpcId;

    @NotNull
    private String accessKey;
    @NotNull
    private String secretKey;

    private String keyPair;

    private String region;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AWSImage> images;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AWSNetwork> networks;


    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> securityGroups;

    @Override
    public Set<? extends BaseNfvImage> getImages() {
        return null;
    }

    @Override
    public Set<? extends BaseNetwork> getNetworks() {
        return null;
    }

    @Override
    public void addAllNetworks(Collection<BaseNetwork> networks) {
        if (this.networks == null) this.networks = new HashSet<>();
        networks.forEach(n -> this.networks.add((AWSNetwork) n));
    }

    @Override
    public void addAllImages(Collection<BaseNfvImage> images) {

    }

    @Override
    public void removeAllNetworks(Collection<BaseNetwork> networks) {

    }

    @Override
    public void removeAllImages(Collection<BaseNfvImage> images) {

    }

    @Override
    public void addImage(BaseNfvImage image) {

    }

    @Override
    public void addNetwork(BaseNetwork network) {

    }

    public String getVpcName() {
        return vpcName;
    }

    public void setVpcName(String vpcName) {
        this.vpcName = vpcName;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    public void setImages(Set<AWSImage> images) {
        this.images = images;
    }

    public void setNetworks(Set<AWSNetwork> networks) {
        this.networks = networks;
    }

    public Set<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(Set<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
