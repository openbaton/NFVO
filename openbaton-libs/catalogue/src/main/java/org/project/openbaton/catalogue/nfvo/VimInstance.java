package org.project.openbaton.catalogue.nfvo;

import org.project.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.project.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by lto on 12/05/15.
 */
@Entity
public class VimInstance implements Serializable{
    @Id
    private String id = IdGenerator.createUUID();
    @Version
    private int version = 0;

    public VimInstance() {
    }

    private String name;
    private String authUrl;
    private String tenant;
    private String username;
    private String password;
    private String keyPair;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Location location;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> securityGroups;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    private Set<DeploymentFlavour> flavours;

    private String type;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<NFVImage> images;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Network> networks;

    @Override
    public String toString() {
        return "VimInstance{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", authUrl='" + authUrl + '\'' +
                ", tenant='" + tenant + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", keyPair='" + keyPair + '\'' +
                ", location=" + location +
                ", securityGroups=" + securityGroups +
                ", flavours=" + flavours +
                ", type='" + type + '\'' +
                ", images=" + images +
                ", networks=" + networks +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(Set<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public Set<DeploymentFlavour> getFlavours() {
        return flavours;
    }

    public void setFlavours(Set<DeploymentFlavour> flavours) {
        this.flavours = flavours;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<NFVImage> getImages() {
        return images;
    }

    public void setImages(Set<NFVImage> images) {
        this.images = images;
    }

    public Set<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(Set<Network> networks) {
        this.networks = networks;
    }
}
