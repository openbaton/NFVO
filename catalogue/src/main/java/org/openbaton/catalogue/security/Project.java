package org.openbaton.catalogue.security;

import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.catalogue.util.IdGenerator;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by lto on 24/05/16.
 */
@Entity
public class Project {
    @Id
    private String id;
    @Column(unique = true)
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<VimInstance> allowedVimInstances;
    private Quota quota;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> networkServiceDescriptors;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> networkServiceRecords;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> vnfPackages;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> events;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> virtualNetworkFunctionDescriptors;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> virtualNetworkFunctionRecords;

    public Set<String> getNetworkServiceDescriptors() {
        return networkServiceDescriptors;
    }

    public void setNetworkServiceDescriptors(Set<String> networkServiceDescriptors) {
        this.networkServiceDescriptors = networkServiceDescriptors;
    }

    public Set<String> getNetworkServiceRecords() {
        return networkServiceRecords;
    }

    public void setNetworkServiceRecords(Set<String> networkServiceRecords) {
        this.networkServiceRecords = networkServiceRecords;
    }

    public Set<String> getVnfPackages() {
        return vnfPackages;
    }

    public void setVnfPackages(Set<String> vnfPackages) {
        this.vnfPackages = vnfPackages;
    }

    public Set<String> getEvents() {
        return events;
    }

    public void setEvents(Set<String> events) {
        this.events = events;
    }

    public Set<String> getVirtualNetworkFunctionDescriptors() {
        return virtualNetworkFunctionDescriptors;
    }

    public void setVirtualNetworkFunctionDescriptors(Set<String> virtualNetworkFunctionDescriptors) {
        this.virtualNetworkFunctionDescriptors = virtualNetworkFunctionDescriptors;
    }

    public Set<String> getVirtualNetworkFunctionRecords() {
        return virtualNetworkFunctionRecords;
    }

    public void setVirtualNetworkFunctionRecords(Set<String> virtualNetworkFunctionRecords) {
        this.virtualNetworkFunctionRecords = virtualNetworkFunctionRecords;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<VimInstance> getAllowedVimInstances() {
        return allowedVimInstances;
    }

    public void setAllowedVimInstances(Set<VimInstance> allowedVimInstances) {
        this.allowedVimInstances = allowedVimInstances;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    @PrePersist
    public void ensureId() {
        id = IdGenerator.createUUID();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", allowedVimInstances=" + allowedVimInstances +
                ", quota=" + quota +
                ", networkServiceDescriptors=" + networkServiceDescriptors +
                ", networkServiceRecords=" + networkServiceRecords +
                ", vnfPackages=" + vnfPackages +
                ", events=" + events +
                ", virtualNetworkFunctionDescriptors=" + virtualNetworkFunctionDescriptors +
                ", virtualNetworkFunctionRecords=" + virtualNetworkFunctionRecords +
                '}';
    }
}
